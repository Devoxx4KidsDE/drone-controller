package de.devoxx4kids.dronecontroller.command.animation;

import de.devoxx4kids.dronecontroller.command.Acknowledge;
import de.devoxx4kids.dronecontroller.command.ChannelType;
import de.devoxx4kids.dronecontroller.command.Command;
import de.devoxx4kids.dronecontroller.command.CommandKey;
import de.devoxx4kids.dronecontroller.command.PacketType;


/**
 * TODO: how to end this command?
 *
 * @author  Alexander Bischof
 * @author  Tobias Schneider
 */
public final class SpinToPosture implements Command {

    private final CommandKey commandKey = CommandKey.commandKey(3, 2, 4);

    private SpinToPosture() {

        // use fabric method
    }

    public static SpinToPosture spinToPosture() {

        return new SpinToPosture();
    }


    @Override
    public byte[] getBytes(int sequence) {

        return new byte[] {
                (byte) PacketType.DATA_WITH_ACK.ordinal(), ChannelType.JUMPINGSUMO_CONTROLLER_TO_DEVICE_ACK_ID.getId(),
                (byte) sequence, 15, 0, 0, 0, commandKey.getProjectId(), commandKey.getClazzId(),
                commandKey.getCommandId(), 0, 7, 0, 0, 0
            };
    }


    @Override
    public Acknowledge getAcknowledge() {

        return Acknowledge.AckBefore;
    }


    @Override
    public String toString() {

        return "SpinToPosture";
    }


    @Override
    public int waitingTime() {

        return 3000;
    }
}
