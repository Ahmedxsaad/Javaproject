package com.insat.gl.robot;

import com.insat.gl.robot.exceptions.EnergieInsuffisanteException;
import com.insat.gl.robot.exceptions.MaintenanceRequiseException;
import com.insat.gl.robot.exceptions.RobotException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstraite représentant un robot générique.
 */
public abstract class Robot {
    protected String id;
    protected int x;
    protected int y;
    protected int energie; // 0-100
    protected int heuresUtilisation;
    protected boolean enMarche;
    protected List<String> historiqueActions;
    protected double totalCarbonEmitted; 
    private static final int MAX_HEURES_AVANT_MAINTENANCE = 100;
    private static final int MIN_ENERGIE_DEMARRAGE = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss");
    private static final double DEFAULT_CARBON_EMISSION_FACTOR = 0.5;

    /**
     * Constructeur pour la classe Robot.
     * @param id L'identifiant unique du robot.
     * @param x La position initiale en x.
     * @param y La position initiale en y.
     */
    public Robot(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.energie = 100;
        this.heuresUtilisation = 0;
        this.enMarche = false;
        this.historiqueActions = new ArrayList<>();
        this.totalCarbonEmitted = 0.0; 
        ajouterHistorique("Robot créé");
    }

    /**
     * Enregistre une action dans l'historique avec la date et l'heure actuelles.
     * @param action L'action effectuée par le robot.
     */
    protected void ajouterHistorique(String action) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        this.historiqueActions.add(timestamp + " " + action);
    }

    /**
     * Vérifie si le robot a suffisamment d'énergie pour une action.
     * @param energieRequise L'énergie nécessaire pour l'action.
     * @throws EnergieInsuffisanteException Si l'énergie est insuffisante.
     */
    protected void verifierEnergie(int energieRequise) throws EnergieInsuffisanteException {
        if (this.energie < energieRequise) {
            throw new EnergieInsuffisanteException("Énergie insuffisante : " + this.energie + "% requis : " + energieRequise + "%");
        }
    }

    /**
     * Vérifie si le robot nécessite une maintenance.
     * @throws MaintenanceRequiseException Si la maintenance est requise.
     */
    protected void verifierMaintenance() throws MaintenanceRequiseException {
        if (this.heuresUtilisation > MAX_HEURES_AVANT_MAINTENANCE) {
            throw new MaintenanceRequiseException("Maintenance requise : " + this.heuresUtilisation + " heures d'utilisation.");
        }
    }

    /**
     * Allume le robot.
     * @throws RobotException Si le démarrage échoue (manque d'énergie).
     */
    public void demarrer() throws RobotException {
        if (this.enMarche) {
            ajouterHistorique("Tentative de démarrage alors qu'il est déjà en marche.");
            return;
        }
        try {
            verifierEnergie(MIN_ENERGIE_DEMARRAGE);
            this.enMarche = true;
            ajouterHistorique("Démarrage du robot.");
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec du démarrage - énergie insuffisante.");
            throw new RobotException("Impossible de démarrer le robot : énergie insuffisante.");
        }
    }

    /**
     * Éteint le robot.
     */
    public void arreter() {
        if (!this.enMarche) {
            ajouterHistorique("Tentative d'arrêt alors qu'il est déjà éteint.");
            return;
        }
        this.enMarche = false;
        ajouterHistorique("Arrêt du robot.");
    }

    /**
     * Réduit l'énergie du robot.
     * @param quantite La quantité d'énergie à consommer.
     */
    protected void consommerEnergie(int quantite) {
        if (quantite <= 0) {
            return;
        }
        int energieAvantConsommation = this.energie;
        this.energie = Math.max(0, this.energie - quantite);
        int energieEffectivementConsumee = energieAvantConsommation - this.energie;

        if (energieEffectivementConsumee > 0) {
            this.totalCarbonEmitted += energieEffectivementConsumee * DEFAULT_CARBON_EMISSION_FACTOR;
            ajouterHistorique(String.format("Énergie consommée: %d. Impact CO2: %.2fg", energieEffectivementConsumee, energieEffectivementConsumee * DEFAULT_CARBON_EMISSION_FACTOR));
        }
    }

    /**
     * Recharge la batterie du robot.
     * @param quantite La quantité d'énergie à recharger.
     */
    public void recharger(int quantite) {
        this.energie = Math.min(100, this.energie + quantite);
        ajouterHistorique("Recharge de " + quantite + "%. Énergie actuelle : " + this.energie + "%");
    }

    /**
     * Méthode abstraite pour définir le déplacement spécifique du robot.
     * @param x Nouvelle coordonnée x.
     * @param y Nouvelle coordonnée y.
     * @throws RobotException Si le déplacement échoue.
     */
    public abstract void deplacer(int x, int y) throws RobotException;

    /**
     * Méthode abstraite pour définir le comportement spécifique du robot.
     * @throws RobotException Si l'exécution de la tâche échoue.
     */
    public abstract void effectuerTache() throws RobotException;

    /**
     * Retourne l'historique complet des actions du robot.
     * @return Une chaîne de caractères contenant l'historique.
     */
    public String getHistorique() {
        StringBuilder sb = new StringBuilder("Historique des actions pour le robot " + id + ":\n");
        for (String action : historiqueActions) {
            sb.append(action).append("\n");
        }
        return sb.toString();
    }

    /**
     * Retourne un résumé de l'état du robot.
     * @return Une chaîne de caractères décrivant le robot.
     */
    @Override
    public String toString() {
        return String.format("Robot [ID: %s, Position: (%d,%d), Énergie: %d%%, Heures: %d, En Marche: %b, CO2 émis: %.2fg]",
                id, x, y, energie, heuresUtilisation, enMarche, totalCarbonEmitted);
    }

    // Getters
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getEnergie() {
        return energie;
    }

    public int getHeuresUtilisation() {
        return heuresUtilisation;
    }

    public boolean isEnMarche() {
        return enMarche;
    }

    public double getTotalCarbonEmitted() {
        return totalCarbonEmitted;
    }
}
