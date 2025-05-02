package com.insat.gl.robot;

import com.insat.gl.robot.exceptions.RobotException;

/**
 * Interface définissant les capacités de connexion pour un robot.
 */
public interface Connectable {
    /**
     * Connecte le robot à un réseau spécifié.
     * @param reseau Le nom du réseau auquel se connecter.
     * @throws RobotException Si la connexion échoue.
     */
    void connecter(String reseau) throws RobotException;

    /**
     * Déconnecte le robot du réseau actuel.
     */
    void deconnecter();

    /**
     * Envoie des données via le réseau connecté.
     * @param donnees Les données à envoyer.
     * @throws RobotException Si l'envoi échoue.
     */
    void envoyerDonnees(String donnees) throws RobotException;
}
