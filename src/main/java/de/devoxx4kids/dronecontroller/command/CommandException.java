package de.devoxx4kids.dronecontroller.command;

/**
 * Exception that will be thrown in something goes wrong in {@link Command}s.
 *
 * @author  Tobias Schneider
 */
public class CommandException extends RuntimeException {

    public CommandException(String message, Throwable cause) {

        super(message, cause);
    }
}
