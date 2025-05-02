package com.insat.gl.robot.exceptions;

/**
 * Exception levée lorsque le robot n'a pas assez d'énergie pour une action.
 */
public class EnergieInsuffisanteException extends RobotException {
    /**
     * Constructeur pour EnergieInsuffisanteException.
     * @param message Le message détaillant l'erreur d'énergie.
     */
    public EnergieInsuffisanteException(String message) {
        super(message);
    }
}
