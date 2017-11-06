package de.devoxx4kids.dronecontroller.network;

import de.devoxx4kids.dronecontroller.command.Command;
import de.devoxx4kids.dronecontroller.command.CommandException;
import de.devoxx4kids.dronecontroller.command.PacketType;
import de.devoxx4kids.dronecontroller.command.common.CommonCommand;
import de.devoxx4kids.dronecontroller.command.common.Pong;
import de.devoxx4kids.dronecontroller.command.movement.Pcmd;
import de.devoxx4kids.dronecontroller.listener.EventListener;
import de.devoxx4kids.dronecontroller.listener.common.CommonEventListener;
import de.devoxx4kids.dronecontroller.network.handshake.HandshakeRequest;
import de.devoxx4kids.dronecontroller.network.handshake.HandshakeResponse;
import de.devoxx4kids.dronecontroller.network.handshake.TcpHandshakeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static de.devoxx4kids.dronecontroller.command.common.CurrentDate.currentDate;
import static de.devoxx4kids.dronecontroller.command.common.CurrentTime.currentTime;
import static java.net.InetAddress.getByName;
import java.net.SocketException;
import java.net.UnknownHostException;
import static java.util.Arrays.copyOfRange;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Represents the queue wireless lan connection to the drone.
 *
 * @author Tobias Schneider
 * @author Stefan Höhn
 */
public class WirelessLanDroneConnection implements DroneConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ConcurrentLinkedQueue<Command> commonCommandQueue = new ConcurrentLinkedQueue<>();
	private final ConcurrentLinkedQueue<Command> commandQueue = new ConcurrentLinkedQueue<>();
	private final List<EventListener> commonEventListeners = new ArrayList<>();
	private final List<EventListener> eventListeners = new ArrayList<>();

	private final String deviceIp;
	private final int tcpPort;
	private final String wirelessLanName;
	private final Clock clock;
	private final byte[] nextSequenceNumbers;

	private boolean listenToResponse = true;
	private boolean sendCommands = true;

	private int devicePort;

	// If waiting time is longer, drone will be sent additional Null-Pcmds to fullfil the requirement of the "link estimator".
	// see http://forum.developer.parrot.com/t/sumo-drone-protocol-sending-a-movement-command-stops-video-transfer/4157
	private final static int maxWaitingTime = 100; // at least every x ms a command is sent to the drone

	public WirelessLanDroneConnection(String deviceIp, int tcpPort, String wirelessLanName) {

		LOGGER.info("Creating {} for {}:{}...", this.getClass().getSimpleName(), deviceIp, tcpPort);

		this.deviceIp = deviceIp;
		this.tcpPort = tcpPort;
		this.wirelessLanName = wirelessLanName;
		this.clock = Clock.systemDefaultZone();

		nextSequenceNumbers = new byte[PacketType.values().length];

	}

	@Override
	public void connect() throws ConnectionException {

		LOGGER.debug("Connecting to drone...");
		listenToResponse = true;

		HandshakeResponse handshakeResponse;

		try {
			handshakeResponse = new TcpHandshakeService(deviceIp, tcpPort).shake(new HandshakeRequest(wirelessLanName));
		} catch (IOException e) {
			throw new ConnectionException("Error while trying to connect to the drone - check your connection", e);
		}

		devicePort = handshakeResponse.getC2d_port();
		LOGGER.info("Connected to drone - Handshake completed with {}", handshakeResponse);

		sendCommand(currentDate(clock));
		sendCommand(currentTime(clock));

		runResponseHandler();
		startSender(commandQueue, true); // only run heartbeat here
		startSender(commonCommandQueue, false); // don't use long running split commands here.
	}

	@Override
	public void disconnect() {

		LOGGER.debug("Disconnecting from drone...");
		listenToResponse = false;
		sendCommands = false;
		LOGGER.debug("Disconnected from drone");
	}

	@Override
	public void sendCommand(Command command) {
		int waitingTime = command.waitingTime();

		/*
            (only) PCmds need to send a continuous stream of commands to keep the drone busy for that time
		 */
		if (command instanceof Pcmd) {
			LOGGER.debug("sending Pcmd's to drone {} ", command);

			while (waitingTime > 0) { // generate enough Pcmds for the expected waiting time. Also, do not remove that loop, the drone will execute the first command again and again
				Command newCommand = ((Pcmd) command).clone((waitingTime > maxWaitingTime) ? maxWaitingTime : waitingTime);
				/* 
				TEST 
				 */
				try (DatagramSocket sumoSocket = new DatagramSocket()) {
					byte[] packet = command.getPacket(getNextSequenceNumber(command));
					sumoSocket.send(new DatagramPacket(packet, packet.length, getByName(deviceIp), devicePort));
				} catch (SocketException ex) {
				} catch (UnknownHostException ex) {
				} catch (IOException ex) {
				}
//				commandQueue.offer(newCommand);
				waitingTime = waitingTime - maxWaitingTime;
			}
		} else {
			LOGGER.debug("sending command to drone directly {}", command);
//			if (command instanceof CommonCommand) {
//				commonCommandQueue.offer(command);
//			} else {
//				commandQueue.offer(command);
//			}
				try (DatagramSocket sumoSocket = new DatagramSocket()) {
					byte[] packet = command.getPacket(getNextSequenceNumber(command));
					sumoSocket.send(new DatagramPacket(packet, packet.length, getByName(deviceIp), devicePort));
				} catch (SocketException ex) {
				} catch (UnknownHostException ex) {
				} catch (IOException ex) {
				}

		}

	}

	@Override
	public void addEventListener(EventListener eventListener) {

		if (eventListener instanceof CommonEventListener) {
			LOGGER.debug("adding eventListener common: {}", eventListener.getClass().getName());
			this.commonEventListeners.add(eventListener);
		} else {
			LOGGER.debug("adding eventListener       : {}", eventListener.getClass().getName());
			this.eventListeners.add(eventListener);
		}
	}

	/**
	 * Drone response handler.
	 *
	 * <p>
	 * Will listen to the udp packages sent from the drone to the receiver and
	 * looks up what command it is and react to it</p>
	 */
	private void runResponseHandler() {

		new Thread(() -> {
			try (DatagramSocket sumoSocket = new DatagramSocket(devicePort)) {
				LOGGER.debug("Listing for response on port {}", devicePort);

				while (listenToResponse) {
					byte[] buffer = new byte[65000];

					DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
					sumoSocket.receive(datagramPacket);

					byte[] packet = datagramPacket.getData();
					LOGGER.trace("Receiving packet {}", convertAndCutPacket(packet, false));

					commonEventListeners.stream().filter(e -> e.test(packet)).forEach(e -> e.consume(packet));

					// Answer with a Pong
					if (packet[0] == 4 || packet[0] == 2) {
						sendCommand(Pong.pong(packet[2]));

						continue;
					}

					eventListeners.stream().filter(e -> e.test(packet)).forEach(e -> e.consume(packet));
				}

				LOGGER.debug("Stopped listening drone packets on port {}", devicePort);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				LOGGER.error("Error occurred while receiving packets from the drone.");
			}
		}).start();
	}

	/**
	 * Starts a new thread as sender.
	 *
	 * @param queue
	 * @param heartbeat sends ongoing packets to drone to prevent drone to slow
	 * down video. ONLY One Sender thread must do that!
	 */
	private void startSender(ConcurrentLinkedQueue<Command> queue, boolean heartbeat) {

		LOGGER.debug("Creating a command queue consumer");

		new Thread(() -> {
			try (DatagramSocket sumoSocket = new DatagramSocket()) {
				while (sendCommands) {
					try {
						Command command = queue.poll();
						if (command != null) {
							byte[] packet = command.getPacket(getNextSequenceNumber(command));

							LOGGER.debug("Sending command '{}' with packet {}", command, convertAndCutPacket(packet, false));

							sumoSocket.send(new DatagramPacket(packet, packet.length, getByName(deviceIp), devicePort));

							int waitingTime = 0;
//							int waitingTime = command.waitingTime();
							LOGGER.debug("Waiting time until send next packet is {}", waitingTime);
							MILLISECONDS.sleep(waitingTime);

						} else { // send "null" PCmd (0,0) =  ("go by 0 speed") when queue is empty
							if (heartbeat) {
								Pcmd nullCommand = Pcmd.pcmd(0, 0, maxWaitingTime);
								byte[] nullMovePacket = nullCommand.getPacket(getNextSequenceNumber(nullCommand));
								LOGGER.trace("empty queue,  sending null command '{}' with packet {}", nullCommand, convertAndCutPacket(nullMovePacket, false));
								sumoSocket.send(new DatagramPacket(nullMovePacket, nullMovePacket.length, getByName(deviceIp), devicePort));
							}
							MILLISECONDS.sleep(maxWaitingTime);
						}

					} catch (InterruptedException e) {
						throw new CommandException("Got interrupted while taking a command", e);
					}
				}

				LOGGER.debug("Stopped command queue consumer");
			} catch (IOException e) {
				LOGGER.error("Error occurred while sending packets to the drone.");
			}
		});
	}

	private synchronized int getNextSequenceNumber(final Command command) {

		byte packetTypeId = command.getPacketType().toByte();

		byte nextSequenceNumber = nextSequenceNumbers[packetTypeId];
		nextSequenceNumbers[packetTypeId] = (byte) (nextSequenceNumber + 1);

		return nextSequenceNumber;
	}

	/**
	 * Converts the complete packet into decimals and cuts off all parts without
	 * information.
	 *
	 * @param packet to convert and cut
	 * @param toDecimal convert packet to decimal
	 *
	 * @return converter and cut packet
	 */
	private String convertAndCutPacket(byte[] packet, boolean toDecimal) {

		byte[] thisPacket = packet.clone();

		if (toDecimal) {
			thisPacket = convertPacket(thisPacket);
		}

		return Arrays.toString(copyOfRange(thisPacket, 0, convertByte(packet[3])));
	}

	/**
	 * Converts a signed two complement byte, as used in java, to a decimal.
	 *
	 * @param b byte to convert into decimal system
	 *
	 * @return converted byte into decimal system
	 */
	private int convertByte(byte b) {

		return b & 0xFF;
	}

	private byte[] convertPacket(byte[] packets) {

		byte[] newArray = new byte[packets.length - 1];

		for (int i = 0; i < packets.length - 1; i++) {
			newArray[i] = (byte) (convertByte(packets[i]));
		}

		return newArray;
	}
}
