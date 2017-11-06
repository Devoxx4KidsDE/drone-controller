//package de.devoxx4kids.dronecontroller.listener.multimedia;
//
//import java.awt.image.BufferedImage;
//import org.junit.gen5.api.BeforeEach;
//import org.junit.gen5.api.Test;
//
//import java.io.IOException;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.function.Consumer;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//
//import static org.hamcrest.core.Is.is;
//
//
///**
// * Unit test of {@link VideoListener}.
// *
// * @author  Tobias Schneider
// */
//class VideoListenerTest {
//
//    private VideoListener sut;
//
//    @BeforeEach
//    void initialize() {
//
//        sut = VideoListener.videoListener(new Consumer<BufferedImage>() {
//			@Override
//			public void accept(BufferedImage t) {
//				//do nothing (I do not understand how this test work)
//			}
//		});
//    }
//
//
//    @Test
//    void consume() throws IOException {
//
//        byte[] packetHeader = new byte[] { 3, 125, 19, -30, 98, 0, 0, 19, 0, 1, 0, 1 };
//        byte[] imagePayLoad = new byte[] {
//            -1, -40, -1, -37, 0, 67, 0, 6, 4, 5, 6, 5, 4, 6, 6, 5, 6, 7, 7, 6, 8, 10, 16, 10, 10, 9, 9, 10, 20, 14, 15,
//            12, 16, 23, 20, 24, 24, 23, 20, 22, 22, 26, 29, 37, 31, 26, 27, 35, 28, 22, 22, 32, 44, 32, 35, 38, 39, 41,
//            42, 41, 25, 31, 45, 48, 45, 40, 48, 37, 40, 41, 40, -1, -37, 0, 67, 1, 7, 7, 7, 10, 8, 10, 19, 10, 10, 19,
//            40, 26, 22, 26, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
//            40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40,
//            -1, -64, 0, 17, 8, 1, -32, 2, -128, 3, 1, 33, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 31, 0, 0, 1, 5, 1, 1, 1, 1,
//            1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -60, 0, -75, 16, 0, 2, 1, 3, 3, 2, 4,
//            3, 5, 5, 4, 4, 0, 0, 1, 125, 1
//        };
//
//        sut.consume(concatenateByteArrays(packetHeader, imagePayLoad));
//
//        Path path = Paths.get("frame.jpg");
//        byte[] savedImageAsBytes = Files.readAllBytes(path);
//        assertThat(savedImageAsBytes, is(imagePayLoad));
//
//        Files.delete(path);
//    }
//
//
//    @Test
//    void testTestIsVideoPacket() {
//
//        byte[] tcpInPacket = new byte[] { 3, 125, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -40 };
//
//        boolean test = sut.test(tcpInPacket);
//        assertThat(test, is(true));
//    }
//
//
//    @Test
//    void testTestIsNoVideoPacket() {
//
//        byte[] tcpInPacket = new byte[] { 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -40 };
//
//        boolean test = sut.test(tcpInPacket);
//        assertThat(test, is(false));
//    }
//
//
//    private byte[] concatenateByteArrays(byte[] packetHeader, byte[] imagePayLoad) {
//
//        byte[] packet = new byte[packetHeader.length + imagePayLoad.length];
//        System.arraycopy(packetHeader, 0, packet, 0, packetHeader.length);
//        System.arraycopy(imagePayLoad, 0, packet, packetHeader.length, imagePayLoad.length);
//
//        return packet;
//    }
//}
