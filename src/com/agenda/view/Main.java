package com.agenda.view;

import com.agenda.controller.AgendaController;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Configuration pour meilleur rendu
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        SwingUtilities.invokeLater(() -> {
            // Afficher le splash screen
            SplashScreen splash = new SplashScreen();
            splash.showSplash(() -> {
                // Créer le contrôleur
                AgendaController controller = new AgendaController();
                
                // Démarrer avec l'écran de connexion
                ConnexionFrame connexionFrame = new ConnexionFrame(controller);
                connexionFrame.setVisible(true);
                
                System.out.println("💜 Medisyns démarré avec système de profils utilisateurs complet");
                System.out.println("📋 Fonctionnalités:");
                System.out.println("   ✅ Création de profils avec inscription");
                System.out.println("   ✅ Connexion sécurisée");
                System.out.println("   ✅ Événements liés aux utilisateurs");
                System.out.println("   ✅ Statistiques par profil");
                System.out.println("   ✅ Gestion des droits (Admin/Utilisateur)");
            });
        });
    }
}