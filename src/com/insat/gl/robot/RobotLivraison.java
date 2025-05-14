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
            if (this.destination == null) {
                ajouterHistorique("Erreur: En livraison mais pas de destination définie.");
                throw new RobotException("En livraison mais aucune destination n'est définie.");
            }
            Scanner scanner = new Scanner(System.in);
            System.out.println("Robot " + id + " est en cours de livraison vers " + destination);
            System.out.print("Confirmez la coordonnée x de la destination (" + this.destination + ") : ");
            int destX = scanner.nextInt();
            System.out.print("Confirmez la coordonnée y de la destination (" + this.destination + ") : ");
            int destY = scanner.nextInt();

            try {
                faireLivraison(destX, destY);
            } catch (RobotException e) {
                throw e;
            }
        } else {
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
     * Effectue la livraison d'un colis aux coordonnées spécifiées.
     * @param destX Coordonnée x de la destination finale.
     * @param destY Coordonnée y de la destination finale.
     * @throws RobotException Si la livraison échoue (déplacement, énergie, etc.).
     */
    public void faireLivraison(int destX, int destY) throws RobotException {
        if (!this.enMarche) {
            throw new RobotException("Le robot doit être démarré pour effectuer une livraison.");
        }
        if (!this.enLivraison || this.colisActuel == null) {
            throw new RobotException("Le robot n'est pas en cours de livraison ou n'a pas de colis.");
        }

        ajouterHistorique("Début de la tentative de livraison du colis '" + this.colisActuel + "' à (" + destX + "," + destY + ").");

        try {
            deplacer(destX, destY);

            String colisLivre = this.colisActuel;
            String destinationAtteinte = this.destination;

            this.colisActuel = null;
            this.enLivraison = false;
            this.destination = null;

            try {
                verifierEnergie(ENERGIE_LIVRAISON);
                consommerEnergie(ENERGIE_LIVRAISON);
                ajouterHistorique(String.format("Livraison du colis '%s' terminée à (%d,%d) pour la destination '%s'. Énergie finale consommée: %d%%. Restant: %d%%",
                        colisLivre, this.x, this.y, destinationAtteinte, ENERGIE_LIVRAISON, this.energie));
            } catch (EnergieInsuffisanteException e) {
                ajouterHistorique(String.format("Livraison du colis '%s' terminée à (%d,%d) pour '%s'. AVERTISSEMENT: Énergie insuffisante pour décompte final (%d%% requis).",
                        colisLivre, this.x, this.y, destinationAtteinte, ENERGIE_LIVRAISON));
            }

        } catch (RobotException e) {
            ajouterHistorique("Échec de la livraison du colis '" + this.colisActuel + "' vers (" + destX + "," + destY + ") : " + e.getMessage());
            throw e;
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
        String etatColis = enLivraison ? String.format("Colis: '%s', Destination: %s, EnLivraison: %b", colisActuel, destination != null ? destination : "N/A", enLivraison) : "Disponible";
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
