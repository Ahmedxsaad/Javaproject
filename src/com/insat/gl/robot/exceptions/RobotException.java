package com.insat.gl.robot.exceptions;

/**
 * Exception de base pour les erreurs liées aux robots.
 */
public class RobotException extends Exception {
    /**
     * Constructeur pour RobotException.
     * @param message Le message détaillant l'erreur.
     */
    public RobotException(String message) {
        super(message);
    }
}
