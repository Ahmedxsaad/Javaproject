package com.insat.gl.robot.exceptions;

/**
 * Exception levée lorsque le robot nécessite une maintenance.
 */
public class MaintenanceRequiseException extends RobotException {
    /**
     * Constructeur pour MaintenanceRequiseException.
     * @param message Le message indiquant la nécessité de maintenance.
     */
    public MaintenanceRequiseException(String message) {
        super(message);
    }
}
