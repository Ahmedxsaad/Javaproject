# Système de Gestion de Robots

Ce projet implémente un système de gestion de robots en Java avec une interface graphique Swing. Il a été développé dans le cadre du projet universitaire de l'INSAT (2024-2025).

## Structure du Projet

Le projet est organisé selon le pattern MVC (Modèle-Vue-Contrôleur) :

```
src/
  └── com/
      └── insat/
          └── gl/
              ├── app/
              │   └── SimulationGUI.java            # Interface graphique principale
              └── robot/
                  ├── Robot.java                    # Classe abstraite de base
                  ├── Connectable.java              # Interface pour la connectivité
                  ├── RobotConnecte.java            # Robot avec capacités réseau
                  ├── RobotLivraison.java           # Robot spécialisé en livraison
                  └── exceptions/
                      ├── RobotException.java        # Exception de base
                      ├── EnergieInsuffisanteException.java
                      └── MaintenanceRequiseException.java
```

## Hiérarchie des Classes

Le projet utilise l'héritage et le polymorphisme pour modéliser différents types de robots :

1. `Robot` (classe abstraite)
   - Gère les attributs et comportements communs: position, énergie, historique d'actions
   - Définit les méthodes abstraites `deplacer()` et `effectuerTache()`

2. `Connectable` (interface)
   - Définit les méthodes de connectivité réseau (`connecter()`, `deconnecter()`, `envoyerDonnees()`)

3. `RobotConnecte` (classe abstraite)
   - Hérite de `Robot` et implémente `Connectable`
   - Ajoute des fonctionnalités réseau comme la connexion à un réseau et l'envoi de données

4. `RobotLivraison` (classe concrète)
   - Hérite de `RobotConnecte`
   - Implémente la logique de livraison: chargement de colis, déplacement vers une destination

## Gestion des Erreurs

Le système utilise une hiérarchie d'exceptions personnalisées :

- `RobotException` : Exception générique pour les robots
  - `EnergieInsuffisanteException` : Levée quand l'énergie est insuffisante
  - `MaintenanceRequiseException` : Levée quand la maintenance est nécessaire (>100h)

## Interface Graphique (GUI)

L'interface graphique est développée avec Swing et comprend :

1. **Panneau de création de robot** : Créer un nouveau robot avec ID et coordonnées initiales
2. **Carte interactive** : Visualisation des robots sur une grille 2D avec code couleur
   - Gris : Robot éteint
   - Bleu : Robot sélectionné
   - Vert : Robot en marche
   - Orange : Robot en cours de livraison
3. **Panneau de contrôle** : Actions sur le robot sélectionné
   - Marche/Arrêt : Démarrer/éteindre le robot
   - Recharge d'énergie : Augmenter le niveau d'énergie du robot
   - Gestion des colis : Charger un colis et spécifier la destination
   - Déplacement : Déplacer le robot vers des coordonnées spécifiques
   - Connectivité : Connecter/déconnecter le robot d'un réseau

4. **Zone de logs** : Affichage de l'historique des actions et événements

## Aspect Écologique

Le système intègre une dimension écologique à travers :

1. **Gestion optimale de l'énergie** :
   - Chaque action consomme une quantité précise d'énergie
   - Le déplacement consomme de l'énergie proportionnellement à la distance (0.3% par unité)
   - La connectivité réseau consomme de l'énergie (5% pour la connexion, 3% pour l'envoi de données)
   - Les déplacements sur de longues distances sont limités (max 100 unités)

2. **Politique de maintenance** :
   - Maintenance obligatoire après 100 heures d'utilisation
   - Comptabilisation précise des heures d'utilisation (1h par 10 unités parcourues)

3. **Livraisons efficientes** :
   - Le robot optimise ses déplacements en évitant les longs trajets
   - La planification des trajets réduit la consommation d'énergie

## Fonctionnalités Principales

### Gestion des Robots
- **Création** : Instancier des robots avec des identifiants uniques
- **Démarrage/Arrêt** : Contrôler l'état de fonctionnement
- **Recharge** : Réapprovisionner l'énergie du robot
- **Suivi** : Visualiser les robots sur la carte et consulter leur historique

### Livraisons
- **Chargement de colis** : Associer un colis et une destination au robot
- **Déplacement** : Diriger le robot vers des coordonnées précises
- **Calcul de distance** : Utiliser la distance euclidienne pour déterminer les trajets
- **Finalisation** : Marquer une livraison comme terminée à destination

### Connectivité
- **Connexion réseau** : Établir une connexion à un réseau spécifique
- **Envoi de données** : Transmettre des informations via le réseau
- **Déconnexion** : Fermer une connexion réseau établie

## Comment Exécuter le Projet

1. Compiler le projet :
```bash
mkdir -p bin && javac -d bin -cp src src/com/insat/gl/robot/exceptions/*.java src/com/insat/gl/robot/*.java src/com/insat/gl/app/*.java
```

2. Exécuter l'application :
```bash
java -cp bin com.insat.gl.app.SimulationGUI
```

## Utilisation de l'Application

1. **Créer un robot** :
   - Entrez un ID unique et les coordonnées initiales (X, Y)
   - Cliquez sur "Créer Robot"

2. **Contrôler le robot** :
   - Sélectionnez un robot dans la liste déroulante
   - Démarrez le robot avec le bouton "Démarrer"
   - Rechargez l'énergie si nécessaire

3. **Effectuer une livraison** :
   - Entrez le nom du colis et la destination
   - Cliquez sur "Charger Colis"
   - Spécifiez les coordonnées de livraison (X, Y)
   - Cliquez sur "Lancer/Déplacer vers Destination"

4. **Connectivité réseau** :
   - Entrez le nom du réseau (ex: "WiFi-Entrepôt")
   - Cliquez sur "Connecter"
   - Envoyez des données si nécessaire

5. **Consulter l'historique** :
   - Cliquez sur "Afficher Historique du Robot" pour voir toutes les actions

## Limites et Précautions

- Le robot ne peut pas se déplacer au-delà de 100 unités en une seule fois
- La recharge est nécessaire quand l'énergie devient basse (<10%)
- La maintenance est obligatoire après 100 heures d'utilisation
- Les actions comme démarrer, charger un colis ou se connecter nécessitent un minimum d'énergie

## Extensions Possibles

- Ajout de nouveaux types de robots (industriels, médicaux)
- Implémentation d'un système de collision entre robots
- Intégration d'un algorithme de routage pour optimiser les trajets
- Développement d'une fonctionnalité de recharge automatique
- Simulation d'obstacles sur la carte

## Aspects Techniques

Le projet utilise plusieurs concepts de programmation orientée objet :
- **Héritage** : Réutilisation et extension des comportements
- **Polymorphisme** : Adaptation des comportements selon le type de robot
- **Encapsulation** : Protection des attributs avec accesseurs appropriés
- **Classes abstraites** : Définition de comportements partiels à compléter
- **Interfaces** : Spécification de contrats à respecter
- **Exceptions** : Gestion des erreurs avec une hiérarchie adaptée
- **Swing** : Interface graphique interactive et réactive