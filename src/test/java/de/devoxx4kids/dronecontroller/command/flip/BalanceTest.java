package de.devoxx4kids.dronecontroller.command.flip;

import de.devoxx4kids.dronecontroller.command.PacketType;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;

import static de.devoxx4kids.dronecontroller.command.PacketType.DATA_WITH_ACK;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Unit test of {@link Balance}.
 *
 * @author  Tobias Schneider
 */

class BalanceTest {

    private Balance sut;

    @BeforeEach
    void initialize() {

        sut = Balance.balance();
    }


    @Test
    void getBytes() {

        byte[] bytesPackage = sut.getPacket(1);

        assertThat(bytesPackage, is(new byte[] { 4, 11, 1, 15, 0, 0, 0, 3, 0, 1, 0, 0, 0, 0, 0 }));
    }


    @Test
    void testToString() {

        assertThat(sut.toString(), is("Balance"));
    }


    @Test
    void getPacketType() {

        PacketType packetType = sut.getPacketType();

        assertThat(packetType, is(DATA_WITH_ACK));
    }


    @Test
    void waitingTime() {

        int waitingTime = sut.waitingTime();

        assertThat(waitingTime, is(500));
    }
}
