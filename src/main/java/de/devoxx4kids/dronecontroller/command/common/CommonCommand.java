package de.devoxx4kids.dronecontroller.command.common;

import de.devoxx4kids.dronecontroller.command.Command;


/**
 * Interface for basic/common commands for internal conversations to the drone. If you only want to control the drone
 * you never need this commands
 *
 * @author  Tobias Schneider - schneider@synyx.de
 */
public interface CommonCommand extends Command {

    /**
     * Time to wait to send the next command after this on.
     *
     * <p>Define the time to wait after a command was send to the drone to wait until the next command should be fired.
     * This will be send at 1/10 Hz</p>
     *
     * @return  time to wait until send next command to the drone
     */
    @Override
    default int waitingTime() {

        return 100;
    }
}
