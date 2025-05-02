package com.insat.gl.robot;

import com.insat.gl.robot.exceptions.EnergieInsuffisanteException;
import com.insat.gl.robot.exceptions.RobotException;

/**
 * Classe abstraite représentant un robot capable de se connecter à des réseaux.
 * Hérite de Robot et implémente Connectable.
 */
public abstract class RobotConnecte extends Robot implements Connectable {
    protected boolean connecte;
    protected String reseauConnecte;

    private static final int ENERGIE_CONNEXION = 5;
    private static final int ENERGIE_ENVOI_DONNEES = 3;

    /**
     * Constructeur pour RobotConnecte.
     * @param id L'identifiant unique du robot.
     * @param x La position initiale en x.
     * @param y La position initiale en y.
     */
    public RobotConnecte(String id, int x, int y) {
        super(id, x, y);
        this.connecte = false;
        this.reseauConnecte = null;
        ajouterHistorique("RobotConnecte initialisé.");
    }

    /**
     * Connecte le robot au réseau spécifié.
     * @param reseau Le nom du réseau auquel se connecter.
     * @throws RobotException Si la connexion échoue (énergie insuffisante ou déjà connecté).
     */
    @Override
    public void connecter(String reseau) throws RobotException {
        if (this.connecte) {
            ajouterHistorique("Tentative de connexion au réseau '" + reseau + "' alors qu'il est déjà connecté à '" + this.reseauConnecte + "'.");
            throw new RobotException("Déjà connecté au réseau : " + this.reseauConnecte);
        }
        try {
            verifierEnergie(ENERGIE_CONNEXION);
            consommerEnergie(ENERGIE_CONNEXION);
            this.connecte = true;
            this.reseauConnecte = reseau;
            ajouterHistorique("Connecté au réseau : " + reseau + ". Énergie restante: " + this.energie + "%");
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec de la connexion au réseau '" + reseau + "' - énergie insuffisante.");
            throw new RobotException("Impossible de se connecter au réseau : énergie insuffisante.");
        }
    }

    /**
     * Déconnecte le robot du réseau actuel.
     */
    @Override
    public void deconnecter() {
        if (!this.connecte) {
            ajouterHistorique("Tentative de déconnexion alors qu'il n'est pas connecté.");
            return;
        }
        String ancienReseau = this.reseauConnecte;
        this.connecte = false;
        this.reseauConnecte = null;
        ajouterHistorique("Déconnecté du réseau : " + ancienReseau);
    }

    /**
     * Envoie des données via le réseau connecté.
     * @param donnees Les données à envoyer.
     * @throws RobotException Si l'envoi échoue (non connecté ou énergie insuffisante).
     */
    @Override
    public void envoyerDonnees(String donnees) throws RobotException {
        if (!this.connecte) {
            ajouterHistorique("Échec de l'envoi de données - non connecté.");
            throw new RobotException("Impossible d'envoyer des données : non connecté à un réseau.");
        }
        try {
            verifierEnergie(ENERGIE_ENVOI_DONNEES);
            consommerEnergie(ENERGIE_ENVOI_DONNEES);
            ajouterHistorique("Envoi de données ('" + donnees + "') via le réseau '" + this.reseauConnecte + "'. Énergie restante: " + this.energie + "%");
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec de l'envoi de données ('" + donnees + "') - énergie insuffisante.");
            throw new RobotException("Impossible d'envoyer des données : énergie insuffisante.");
        }
    }

    /**
     * Surcharge de toString pour inclure l'état de connexion.
     * @return Une chaîne de caractères décrivant le robot connecté.
     */
    @Override
    public String toString() {
        String etatConnexion = connecte ? "Oui, Réseau: " + reseauConnecte : "Non";
        return String.format("%s, Connecté: %s", super.toString().replace("]", ""), etatConnexion + "]");
    }

    // Getters
    public boolean isConnecte() {
        return connecte;
    }

    public String getReseauConnecte() {
        return reseauConnecte;
    }
}
