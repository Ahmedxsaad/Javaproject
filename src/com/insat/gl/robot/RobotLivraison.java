package com.insat.gl.robot;

import com.insat.gl.robot.exceptions.EnergieInsuffisanteException;
import com.insat.gl.robot.exceptions.RobotException;
import java.util.Scanner;

/**
 * Classe représentant un robot spécialisé dans la livraison de colis.
 * Hérite de RobotConnecte.
 */
public class RobotLivraison extends RobotConnecte {

    private String colisActuel;
    private String destination;
    private boolean enLivraison;

    public static final int ENERGIE_LIVRAISON = 15;
    public static final int ENERGIE_CHARGEMENT = 5;

    private static final double ENERGIE_PAR_UNITE_DISTANCE = 0.3;
    private static final int DISTANCE_PAR_HEURE = 10;
    private static final int MAX_DISTANCE_DEPLACEMENT = 100;

    /**
     * Constructeur pour RobotLivraison.
     * @param id L'identifiant unique du robot.
     * @param x La position initiale en x.
     * @param y La position initiale en y.
     */
    public RobotLivraison(String id, int x, int y) {
        super(id, x, y);
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        ajouterHistorique("RobotLivraison initialisé.");
    }

    /**
     * Exécute une tâche en fonction de l'état du robot (en livraison, en attente).
     * Cette implémentation utilise la console pour l'interaction, ce qui n'est pas idéal
     * pour une GUI, mais suit la description initiale.
     * @throws RobotException Si le robot n'est pas démarré ou si une autre erreur survient.
     */
    @Override
    public void effectuerTache() throws RobotException {
        if (!this.enMarche) {
            throw new RobotException("Le robot doit être démarré pour effectuer une tâche.");
        }

        verifierMaintenance();

        if (this.enLivraison) {
            // Simule la demande de coordonnées finales via la console (pour conformité au sujet)
            Scanner scanner = new Scanner(System.in);
            System.out.println("Robot " + id + " est en cours de livraison vers " + destination);
            System.out.print("Entrez la coordonnée x de la destination finale : ");
            int destX = scanner.nextInt();
            System.out.print("Entrez la coordonnée y de la destination finale : ");
            int destY = scanner.nextInt();
            try {
                deplacer(destX, destY);
                livraisonTerminee(); // Marquer comme terminée après déplacement réussi
            } catch (RobotException e) {
                ajouterHistorique("Échec de la finalisation de la livraison : " + e.getMessage());
                throw e;
            }
        } else {
            // Simule la demande de chargement via la console (pour conformité au sujet)
            Scanner scanner = new Scanner(System.in);
            System.out.print("Le robot " + id + " est disponible. Charger un nouveau colis ? (oui/non) : ");
            String reponse = scanner.nextLine().trim().toLowerCase();

            if ("oui".equals(reponse)) {
                System.out.print("Entrez le nom/description du colis : ");
                String nomColis = scanner.nextLine();
                System.out.print("Entrez la destination : ");
                String dest = scanner.nextLine();
                try {
                    chargerColis(nomColis, dest);
                    System.out.println("Colis chargé. Prêt pour la livraison vers " + destination);
                } catch (RobotException e) {
                    ajouterHistorique("Échec du chargement du colis : " + e.getMessage());
                    System.err.println("Erreur lors du chargement : " + e.getMessage());
                }
            } else {
                ajouterHistorique("En attente de colis.");
                System.out.println("Robot " + id + " en attente.");
            }
        }
    }

    /**
     * Marque la livraison comme terminée et réinitialise les attributs liés au colis.
     * Consomme l'énergie finale de livraison.
     */
    private void livraisonTerminee() {
        ajouterHistorique("Livraison terminée à (" + this.x + "," + this.y + ") pour la destination: " + destination);
        this.colisActuel = null;
        this.destination = null;
        this.enLivraison = false;
        try {
             verifierEnergie(ENERGIE_LIVRAISON);
             consommerEnergie(ENERGIE_LIVRAISON);
             ajouterHistorique("Consommation énergie fin de livraison: " + ENERGIE_LIVRAISON + "%. Restant: " + this.energie + "%");
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Avertissement: Énergie insuffisante pour enregistrer la fin de livraison correctement.");
        }
    }

    /**
     * Déplace le robot vers les coordonnées spécifiées.
     * @param destX Coordonnée x de destination.
     * @param destY Coordonnée y de destination.
     * @throws RobotException Si le déplacement est impossible (énergie, maintenance, distance > 100).
     */
    @Override
    public void deplacer(int destX, int destY) throws RobotException {
        if (!this.enMarche) {
            throw new RobotException("Le robot doit être démarré pour se déplacer.");
        }

        verifierMaintenance();

        double distance = Math.sqrt(Math.pow(destX - this.x, 2) + Math.pow(destY - this.y, 2));

        if (distance > MAX_DISTANCE_DEPLACEMENT) {
            throw new RobotException("Déplacement annulé : distance trop grande (" + String.format("%.2f", distance) + " unités, max: " + MAX_DISTANCE_DEPLACEMENT + ").");
        }

        int energieRequise = (int) Math.ceil(distance * ENERGIE_PAR_UNITE_DISTANCE);
        try {
            verifierEnergie(energieRequise);
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec du déplacement vers (" + destX + "," + destY + ") - énergie insuffisante.");
            throw new RobotException("Déplacement impossible vers (" + destX + "," + destY + ") : énergie insuffisante. Requis: " + energieRequise + "%, Actuelle: " + this.energie + "%");
        }

        int heuresAjoutees = (int) Math.ceil(distance / DISTANCE_PAR_HEURE);

        consommerEnergie(energieRequise);
        this.heuresUtilisation += heuresAjoutees;
        int oldX = this.x;
        int oldY = this.y;
        this.x = destX;
        this.y = destY;

        ajouterHistorique(String.format("Déplacement de (%d,%d) à (%d,%d). Distance: %.2f. Énergie consommée: %d%%. Heures ajoutées: %d.",
                oldX, oldY, this.x, this.y, distance, energieRequise, heuresAjoutees));

        // Si le robot était en livraison et atteint la destination exacte via cet appel, on termine la livraison.
        // Note: La méthode effectuerTache() appelle aussi livraisonTerminee(), ce qui pourrait être redondant.
        // Idéalement, la logique de fin de livraison devrait être centralisée.
        if (this.enLivraison && this.x == destX && this.y == destY) {
             // On pourrait appeler livraisonTerminee() ici, mais attention à la double consommation d'énergie
             // si effectuerTache l'appelle aussi. La conception actuelle est un peu ambiguë.
             // Pour l'instant, on laisse effectuerTache gérer la fin après le déplacement.
        }
    }

    /**
     * Charge un colis sur le robot pour une destination donnée.
     * @param colis Le nom ou la description du colis.
     * @param destination La destination de livraison.
     * @throws RobotException Si le robot est déjà en livraison, transporte déjà un colis, ou manque d'énergie.
     */
    public void chargerColis(String colis, String destination) throws RobotException {
        if (!this.enMarche) {
            throw new RobotException("Le robot doit être démarré pour charger un colis.");
        }
        if (this.enLivraison) {
            throw new RobotException("Impossible de charger : déjà en cours de livraison.");
        }
        if (this.colisActuel != null) {
            throw new RobotException("Impossible de charger : transporte déjà le colis '" + this.colisActuel + "'.");
        }

        verifierMaintenance();
        try {
            verifierEnergie(ENERGIE_CHARGEMENT);
        } catch (EnergieInsuffisanteException e) {
            ajouterHistorique("Échec du chargement du colis '" + colis + "' - énergie insuffisante.");
            throw new RobotException("Impossible de charger le colis : énergie insuffisante.");
        }

        consommerEnergie(ENERGIE_CHARGEMENT);
        this.colisActuel = colis;
        this.destination = destination;
        this.enLivraison = true;
        ajouterHistorique("Chargement du colis '" + colis + "' pour destination : " + destination + ". Énergie restante: " + this.energie + "%");
    }

    /**
     * Surcharge de toString pour inclure les informations spécifiques à la livraison.
     * @return Une chaîne de caractères décrivant le robot de livraison.
     */
    @Override
    public String toString() {
        String etatColis = enLivraison ? String.format("Colis: '%s', Destination: %s, EnLivraison: %b", colisActuel, destination, enLivraison) : "Disponible";
        String etatConnexion = connecte ? "Oui, Réseau: " + reseauConnecte : "Non";

        return String.format("RobotLivraison [ID: %s, Position: (%d,%d), Énergie: %d%%, Heures: %d, %s, Connecté: %s]",
                id, x, y, energie, heuresUtilisation, etatColis, etatConnexion);
    }

    // Getters spécifiques
    public String getColisActuel() {
        return colisActuel;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isEnLivraison() {
        return enLivraison;
    }
}
