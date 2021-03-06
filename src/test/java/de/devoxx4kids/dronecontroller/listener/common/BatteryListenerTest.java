package de.devoxx4kids.dronecontroller.listener.common;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.core.Is.is;


/**
 * Unit test of {@link BatteryListener}.
 *
 * @author  Tobias Schneider
 */

class BatteryListenerTest {

    private BatteryListener sut;

    private byte batteryState;

    @BeforeEach
    void initialize() {

        sut = BatteryListener.batteryListener(b -> batteryState = b);
    }


    @Test
    void testTestIsBatteryPacket() {

        byte[] tcpInPacket = new byte[] { -1, -1, -1, -1, -1, -1, -1, 0, 5, 1, -1, 11 };

        boolean test = sut.test(tcpInPacket);
        assertThat(test, is(true));
    }


    @Test
    void testTestIsNoBatteryPacket() {

        byte[] tcpInPacket = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, 127, -1, 11 };

        boolean test = sut.test(tcpInPacket);
        assertThat(test, is(false));
    }


    @Test
    void consume() {

        byte expectedByte = 11;
        byte[] tcpInPacket = new byte[] { -1, -1, -1, -1, -1, -1, -1, 0, 5, 1, -1, expectedByte };

        sut.consume(tcpInPacket);
        assertThat(batteryState, is(expectedByte));
    }
}
