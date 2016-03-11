package de.devoxx4kids.dronecontroller.network.handshake;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.Socket;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;


/**
 * Service for the Handshake and establishing the connection to the drone.
 *
 * @author  Alexander Bischof
 * @author  Tobias Schneider
 */
public class TcpHandshakeService implements HandShakeService {

    private final Socket tcpSocket;
    private final PrintWriter tcpOut;
    private final BufferedReader tcpIn;

    public TcpHandshakeService(String deviceIp, int tcpPort) throws IOException {

        tcpSocket = new Socket(deviceIp, tcpPort);
        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
    }

    @Override
    public HandshakeResponse shake(HandshakeRequest handshakeRequest) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(INDENT_OUTPUT, true);

        StringWriter shakeData = new StringWriter();
        objectMapper.writeValue(shakeData, handshakeRequest);

        return tcpHandshakeResult(shakeData.toString());
    }


    private HandshakeResponse tcpHandshakeResult(String shakeData) throws IOException {

        // Send to device
        tcpOut.println(shakeData);

        // Reads json response
        String responseLine;
        HandshakeResponse deviceAnswer = null;
        ObjectMapper objectMapper = new ObjectMapper();

        while ((responseLine = tcpIn.readLine()) != null) {
            responseLine = responseLine.substring(0, responseLine.lastIndexOf('}') + 1);
            deviceAnswer = objectMapper.readValue(responseLine, HandshakeResponse.class);
        }

        return deviceAnswer;
    }


    @Override
    public void close() throws IOException {

        tcpOut.close();
        tcpSocket.close();
        tcpIn.close();
    }
}