package com.insat.gl.app;

import com.insat.gl.robot.RobotLivraison;
import com.insat.gl.robot.exceptions.RobotException;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Interface graphique Swing pour la simulation de robots de livraison.
 */
public class SimulationGUI extends JFrame {

    private JTextArea logTextArea;
    private JTextField robotIdField, posXField, posYField, destXField, destYField, colisField, destField, reseauField, rechargeField;
    private JButton createRobotButton, startRobotButton, stopRobotButton, chargeColisButton, lancerLivraisonButton, connecterButton, deconnecterButton, rechargerButton, afficherHistoriqueButton;
    private JPanel controlPanel, robotPanel, mapPanel;
    private JComboBox<String> robotSelector;

    private List<RobotLivraison> robots;
    private RobotLivraison selectedRobot;
    private SimulationMap simulationMap;

    public SimulationGUI() {
        super("Simulation de Robots de Livraison");
        robots = new ArrayList<>();
        initComponents();
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));

        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createRobotInputs();
        add(controlPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));

        simulationMap = new SimulationMap(robots);
        mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBorder(BorderFactory.createTitledBorder("Carte de Simulation"));
        mapPanel.add(simulationMap, BorderLayout.CENTER);
        centerPanel.add(mapPanel);

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Logs et Historique"));
        centerPanel.add(logScrollPane);

        add(centerPanel, BorderLayout.CENTER);

        robotPanel = new JPanel();
        robotPanel.setLayout(new BoxLayout(robotPanel, BoxLayout.Y_AXIS));
        robotPanel.setBorder(BorderFactory.createTitledBorder("Contrôle du Robot Sélectionné"));
        createRobotControls();
        add(robotPanel, BorderLayout.SOUTH);

        updateRobotSelector();
        updateRobotControlsState();
    }

    private void createRobotInputs() {
        controlPanel.add(new JLabel("ID:"));
        robotIdField = new JTextField("R", 3);
        controlPanel.add(robotIdField);
        controlPanel.add(new JLabel("X:"));
        posXField = new JTextField("0", 3);
        controlPanel.add(posXField);
        controlPanel.add(new JLabel("Y:"));
        posYField = new JTextField("0", 3);
        controlPanel.add(posYField);

        createRobotButton = new JButton("Créer Robot");
        createRobotButton.addActionListener(e -> createRobot());
        controlPanel.add(createRobotButton);

        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));

        controlPanel.add(new JLabel("Sélectionner Robot:"));
        robotSelector = new JComboBox<>();
        robotSelector.addActionListener(e -> selectRobot());
        controlPanel.add(robotSelector);
    }

    private void createRobotControls() {
        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startRobotButton = new JButton("Démarrer");
        startRobotButton.addActionListener(e -> startSelectedRobot());
        line1.add(startRobotButton);

        stopRobotButton = new JButton("Arrêter");
        stopRobotButton.addActionListener(e -> stopSelectedRobot());
        line1.add(stopRobotButton);

        line1.add(new JSeparator(SwingConstants.VERTICAL));

        line1.add(new JLabel("Recharger (%):"));
        rechargeField = new JTextField("20", 3);
        line1.add(rechargeField);
        rechargerButton = new JButton("Recharger");
        rechargerButton.addActionListener(e -> rechargeSelectedRobot());
        line1.add(rechargerButton);

        robotPanel.add(line1);

        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line2.add(new JLabel("Colis:"));
        colisField = new JTextField(10);
        line2.add(colisField);
        line2.add(new JLabel("Destination (Nom):"));
        destField = new JTextField(10);
        line2.add(destField);
        chargeColisButton = new JButton("Charger Colis");
        chargeColisButton.addActionListener(e -> chargeColisForSelectedRobot());
        line2.add(chargeColisButton);
        robotPanel.add(line2);

        JPanel line3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line3.add(new JLabel("Livraison vers X:"));
        destXField = new JTextField("10", 3);
        line3.add(destXField);
        line3.add(new JLabel("Y:"));
        destYField = new JTextField("10", 3);
        line3.add(destYField);
        lancerLivraisonButton = new JButton("Lancer/Déplacer vers Destination");
        lancerLivraisonButton.addActionListener(e -> deliverWithSelectedRobot());
        line3.add(lancerLivraisonButton);
        robotPanel.add(line3);

        JPanel line4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line4.add(new JLabel("Réseau:"));
        reseauField = new JTextField("WiFi-Entrepôt", 10);
        line4.add(reseauField);
        connecterButton = new JButton("Connecter");
        connecterButton.addActionListener(e -> connectSelectedRobot());
        line4.add(connecterButton);
        deconnecterButton = new JButton("Déconnecter");
        deconnecterButton.addActionListener(e -> disconnectSelectedRobot());
        line4.add(deconnecterButton);
        robotPanel.add(line4);

        JPanel line5 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        afficherHistoriqueButton = new JButton("Afficher Historique du Robot");
        afficherHistoriqueButton.addActionListener(e -> showHistory());
        line5.add(afficherHistoriqueButton);
        robotPanel.add(line5);
    }

    private void log(String message) {
        logTextArea.append(message + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    private void createRobot() {
        try {
            String id = robotIdField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "L'ID du robot ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (RobotLivraison r : robots) {
                if (r.getId().equals(id)) {
                    JOptionPane.showMessageDialog(this, "L'ID du robot existe déjà.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int x = Integer.parseInt(posXField.getText());
            int y = Integer.parseInt(posYField.getText());

            RobotLivraison newRobot = new RobotLivraison(id, x, y);
            robots.add(newRobot);
            log("Robot créé: " + newRobot);
            updateRobotSelector();
            robotSelector.setSelectedItem(id);
            simulationMap.repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Position X et Y doivent être des nombres entiers.", "Erreur de Format", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la création du robot: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            log("Erreur création: " + ex.getMessage());
        }
    }

    private void updateRobotSelector() {
        String previouslySelected = (String) robotSelector.getSelectedItem();
        robotSelector.removeAllItems();
        for (RobotLivraison robot : robots) {
            robotSelector.addItem(robot.getId());
        }
        if (previouslySelected != null) {
            robotSelector.setSelectedItem(previouslySelected);
        }
        selectRobot();
    }

    private void selectRobot() {
        String selectedId = (String) robotSelector.getSelectedItem();
        selectedRobot = null;
        if (selectedId != null) {
            for (RobotLivraison robot : robots) {
                if (robot.getId().equals(selectedId)) {
                    selectedRobot = robot;
                    break;
                }
            }
        }
        updateRobotControlsState();
        simulationMap.setSelectedRobot(selectedRobot);
        simulationMap.repaint();
        if (selectedRobot != null) {
            log("Robot sélectionné: " + selectedRobot.getId());
        }
    }

    private void updateRobotControlsState() {
        boolean robotIsSelected = (selectedRobot != null);
        startRobotButton.setEnabled(robotIsSelected && !selectedRobot.isEnMarche());
        stopRobotButton.setEnabled(robotIsSelected && selectedRobot.isEnMarche());
        chargeColisButton.setEnabled(robotIsSelected && selectedRobot.isEnMarche() && !selectedRobot.isEnLivraison());
        lancerLivraisonButton.setEnabled(robotIsSelected && selectedRobot.isEnMarche());
        connecterButton.setEnabled(robotIsSelected && selectedRobot.isEnMarche() && !selectedRobot.isConnecte());
        deconnecterButton.setEnabled(robotIsSelected && selectedRobot.isEnMarche() && selectedRobot.isConnecte());
        rechargerButton.setEnabled(robotIsSelected);
        afficherHistoriqueButton.setEnabled(robotIsSelected);

        colisField.setEnabled(robotIsSelected);
        destField.setEnabled(robotIsSelected);
        destXField.setEnabled(robotIsSelected);
        destYField.setEnabled(robotIsSelected);
        reseauField.setEnabled(robotIsSelected);
        rechargeField.setEnabled(robotIsSelected);
    }

    private void handleRobotAction(Runnable action) {
        if (selectedRobot == null) {
            JOptionPane.showMessageDialog(this, "Aucun robot sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            action.run();
            log("Action réussie pour le robot " + selectedRobot.getId());
            log(selectedRobot.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des nombres valides pour les coordonnées ou la recharge.", "Erreur de Format", JOptionPane.ERROR_MESSAGE);
            log("Erreur Format: " + ex.getMessage());
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof RobotException) {
                RobotException robotEx = (RobotException) ex.getCause();
                JOptionPane.showMessageDialog(this, "Erreur Robot: " + robotEx.getMessage(), "Erreur d'Action", JOptionPane.ERROR_MESSAGE);
                log("Erreur Robot " + selectedRobot.getId() + ": " + robotEx.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, "Erreur d'exécution: " + ex.getMessage(), "Erreur d'Exécution", JOptionPane.ERROR_MESSAGE);
                log("Erreur Exécution: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur inattendue: " + ex.getMessage(), "Erreur Système", JOptionPane.ERROR_MESSAGE);
            log("Erreur Système: " + ex.getMessage());
            ex.printStackTrace();
        }
        updateRobotControlsState();
        simulationMap.repaint();
    }

    private void startSelectedRobot() {
        handleRobotAction(() -> {
            try {
                selectedRobot.demarrer();
            } catch (RobotException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void stopSelectedRobot() {
        handleRobotAction(() -> selectedRobot.arreter());
    }

    private void rechargeSelectedRobot() {
        handleRobotAction(() -> {
            int quantite = Integer.parseInt(rechargeField.getText());
            selectedRobot.recharger(quantite);
        });
    }

    private void chargeColisForSelectedRobot() {
        handleRobotAction(() -> {
            String colis = colisField.getText().trim();
            String destination = destField.getText().trim();
            if (colis.isEmpty() || destination.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du colis et la destination sont requis.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                selectedRobot.chargerColis(colis, destination);
            } catch (RobotException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void deliverWithSelectedRobot() {
        handleRobotAction(() -> {
            int destX = Integer.parseInt(destXField.getText());
            int destY = Integer.parseInt(destYField.getText());
            try {
                log("Déplacement/Livraison vers (" + destX + "," + destY + ")");
                selectedRobot.deplacer(destX, destY);
                if (selectedRobot.isEnLivraison() && selectedRobot.getX() == destX && selectedRobot.getY() == destY) {
                     log("Arrivé à destination. La livraison devrait être marquée comme terminée par le robot.");
                }
            } catch (RobotException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void connectSelectedRobot() {
        handleRobotAction(() -> {
            String reseau = reseauField.getText().trim();
            if (reseau.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom du réseau est requis.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                selectedRobot.connecter(reseau);
            } catch (RobotException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void disconnectSelectedRobot() {
        handleRobotAction(() -> selectedRobot.deconnecter());
    }

    private void showHistory() {
        if (selectedRobot != null) {
            log("\n--- Historique pour Robot " + selectedRobot.getId() + " ---");
            log(selectedRobot.getHistorique());
            log("--- Fin Historique ---\n");
        } else {
            JOptionPane.showMessageDialog(this, "Aucun robot sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Classe interne pour dessiner la carte de simulation.
     */
    class SimulationMap extends JPanel {
        private List<RobotLivraison> robotsToDraw;
        private RobotLivraison selectedRobotToDraw;
        private final int PADDING = 20;
        private final int ROBOT_SIZE = 10;

        public SimulationMap(List<RobotLivraison> robots) {
            this.robotsToDraw = robots;
            setBackground(Color.WHITE);
        }

        public void setSelectedRobot(RobotLivraison robot) {
            this.selectedRobotToDraw = robot;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2d.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < width; i += 20) {
                g2d.drawLine(i, 0, i, height);
            }
            for (int i = 0; i < height; i += 20) {
                g2d.drawLine(0, i, width, i);
            }

            if (robotsToDraw != null) {
                for (RobotLivraison robot : robotsToDraw) {
                    int drawX = PADDING + robot.getX();
                    int drawY = PADDING + robot.getY();

                    if (robot == selectedRobotToDraw) {
                        g2d.setColor(Color.BLUE);
                    } else if (robot.isEnLivraison()) {
                        g2d.setColor(Color.ORANGE);
                    } else if (!robot.isEnMarche()) {
                        g2d.setColor(Color.GRAY);
                    } else {
                        g2d.setColor(Color.GREEN);
                    }

                    g2d.fillOval(drawX - ROBOT_SIZE / 2, drawY - ROBOT_SIZE / 2, ROBOT_SIZE, ROBOT_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(robot.getId(), drawX + ROBOT_SIZE, drawY);
                    g2d.drawString(robot.getEnergie() + "%", drawX + ROBOT_SIZE, drawY + 12);
                }
            }

            g2d.setColor(Color.RED);
            g2d.fillOval(PADDING - 2, PADDING - 2, 4, 4);
            g2d.drawString("(0,0)", PADDING + 5, PADDING - 5);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulationGUI gui = new SimulationGUI();
            gui.setVisible(true);
        });
    }
}
