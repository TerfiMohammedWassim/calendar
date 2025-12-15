package com.agenda.controller;

import com.agenda.modele.Evenement;
import com.agenda.modele.Utilisateur;
import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class AgendaController {

    private final List<Evenement> evenements;
    private final ScheduledExecutorService scheduler;
    private final List<Runnable> refreshListeners = new ArrayList<>();
    private final Timer notificationTimer;
    
    private static final String SAVE_FILE = "medisyns_data.ser";
    private static final String BACKUP_FILE = "medisyns_data.ser.backup";
    private static final String USERS_FILE = "medisyns_users.ser";
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Utilisateur utilisateurCourant;
    private final Map<String, Utilisateur> utilisateurs = new HashMap<>();
    private final List<String> notificationsEnvoyees = new ArrayList<>();
    private final List<EvenementPartage> evenementsPartages = new ArrayList<>();

    public AgendaController() {
        evenements = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        
        notificationTimer = new Timer(30000, e -> verifierNotificationsImmediates());
        notificationTimer.start();
        
        chargerDonnees();
        chargerUtilisateurs();
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::sauvegarderDonnees));
        Runtime.getRuntime().addShutdownHook(new Thread(this::sauvegarderUtilisateurs));
        
        System.out.println("Système de notifications Medisyns activé");
    }

    public boolean seConnecter(String username, String password) {
        if (username != null && !username.isEmpty()) {
            Utilisateur utilisateur = utilisateurs.get(username);
            if (utilisateur != null) {
                // En démonstration, on accepte n'importe quel mot de passe
                // En production, il faudrait vérifier le mot de passe crypté
                utilisateur.mettreAJourDernierAcces();
                utilisateurCourant = utilisateur;
                System.out.println("Connexion réussie pour: " + username + " (" + utilisateur.getRoleDisplay() + ")");
                return true;
            }
        }
        System.out.println("Échec de connexion");
        return false;
    }
    
    public void seDeconnecter() {
        if (utilisateurCourant != null) {
            System.out.println("Déconnexion de: " + utilisateurCourant.getUsername());
            utilisateurCourant = null;
        }
    }
    
    public boolean verifierSession() {
        return utilisateurCourant != null;
    }

    public boolean inscrire(String username, String email, String password, String role) {
        if (username == null || username.isEmpty() || email == null || email.isEmpty() || 
            password == null || password.isEmpty()) {
            System.out.println("Inscription échouée: champs manquants");
            return false;
        }
        
        if (utilisateurs.containsKey(username)) {
            System.out.println("Inscription échouée: utilisateur existe déjà");
            return false;
        }
        
        Utilisateur nouvelUtilisateur = new Utilisateur(username, username, role, email);
        utilisateurs.put(username, nouvelUtilisateur);
        sauvegarderUtilisateurs();
        
        System.out.println("Inscription réussie pour: " + username);
        return true;
    }
    
    public boolean validerEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    public boolean validerMotDePasse(String password) {
        return password != null && password.length() >= 6;
    }

    public void setUtilisateurCourant(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        if (utilisateur != null && !utilisateurs.containsKey(utilisateur.getUsername())) {
            utilisateurs.put(utilisateur.getUsername(), utilisateur);
        }
        // Notifier tous les listeners pour rafraîchir l'UI
        notifyRefreshListeners();
    }

    public Utilisateur getUtilisateurCourant() {
        return utilisateurCourant;
    }
    
    public List<Utilisateur> getAllUtilisateurs() {
        return new ArrayList<>(utilisateurs.values());
    }
    
    public Utilisateur getUtilisateur(String username) {
        return utilisateurs.get(username);
    }
    
    public boolean creerProfil(String username, String nomComplet, String email, String password, String role) {
        if (username == null || username.isEmpty() || nomComplet == null || nomComplet.isEmpty() || 
            email == null || email.isEmpty() || password == null || password.isEmpty() || 
            role == null || role.isEmpty()) {
            System.out.println("Création de profil échouée: champs manquants");
            return false;
        }
        
        if (utilisateurs.containsKey(username)) {
            System.out.println("Création de profil échouée: utilisateur existe déjà");
            return false;
        }
        
        // Créer l'utilisateur avec le rôle choisi
        Utilisateur nouvelUtilisateur;
        
        if ("ADMIN".equals(role)) {
            // Administrateur avec des permissions spéciales
            nouvelUtilisateur = new Utilisateur(username, nomComplet, "ADMIN", email,
                                               "", "Administration", java.util.Arrays.asList("Administration"));
        } else {
            // Utilisateur simple
            nouvelUtilisateur = new Utilisateur(username, nomComplet, "UTILISATEUR", email);
        }
        
        utilisateurs.put(username, nouvelUtilisateur);
        sauvegarderUtilisateurs();
        
        System.out.println("Profil créé: " + username + " (" + role + ")");
        return true;
    }
    
    public boolean mettreAJourProfil(String username, String telephone, String service, List<String> specialites) {
        Utilisateur utilisateur = utilisateurs.get(username);
        if (utilisateur == null) {
            return false;
        }
        
        utilisateur.setTelephone(telephone);
        utilisateur.setService(service);
        utilisateur.setSpecialites(specialites);
        sauvegarderUtilisateurs();
        
        System.out.println("Profil mis à jour: " + username);
        return true;
    }

    private void verifierNotificationsImmediates() {
        LocalDateTime now = LocalDateTime.now();
        
        for (Evenement ev : new ArrayList<>(evenements)) {
            if (ev.getNotificationBeforeMinutes() <= 0) continue;

            LocalDateTime eventTime = LocalDateTime.of(ev.getDate(), ev.getHeure());
            LocalDateTime notifyTime = eventTime.minusMinutes(ev.getNotificationBeforeMinutes());

            if (eventTime.isBefore(now)) {
                continue;
            }

            boolean isTimeForNotification = 
                (now.isAfter(notifyTime) || now.isEqual(notifyTime)) && 
                now.isBefore(eventTime);
            
            if (isTimeForNotification) {
                String notificationKey = ev.getTitre() + "_" + ev.getDate() + "_" + 
                                       ev.getHeure() + "_" + ev.getNotificationBeforeMinutes();
                if (!notificationDejaEnvoyee(notificationKey)) {
                    envoyerNotificationImmediate(ev);
                    marquerNotificationEnvoyee(notificationKey);
                }
            }
        }
    }
    
    private boolean notificationDejaEnvoyee(String key) {
        return notificationsEnvoyees.contains(key);
    }
    
    private void marquerNotificationEnvoyee(String key) {
        notificationsEnvoyees.add(key);
    }

    private void envoyerNotificationImmediate(Evenement ev) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                "<html>" +
                "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 20px; border-radius: 12px; border: 2px solid #6E44FF; width: 360px;'>" +
                "<h3 style='margin: 0 0 15px 0; text-align: center; color: #6B46C1; font-size: 16px;'>Rappel Médical</h3>" +
                "<div style='color: #4A5568; font-size: 13px; background: #FFFFFF; padding: 15px; border-radius: 8px; border: 1px solid #E2E8F0;'>" +
                "<div style='margin-bottom: 8px;'><b style='color: #6B46C1;'>Événement :</b> %s</div>" +
                "<div style='margin-bottom: 8px;'><b style='color: #6B46C1;'>Date :</b> %s à %s</div>" +
                "<div style='margin-bottom: 8px;'><b style='color: #6B46C1;'>Dans :</b> %d minutes</div>" +
                "<div style='margin-bottom: 8px;'><b style='color: #6B46C1;'>Responsable :</b> %s</div>" +
                "<div><b style='color: #6B46C1;'>Participants :</b> %s</div>" +
                "</div>" +
                "</div>" +
                "</html>",
                ev.getTitre(),
                ev.getDate().format(dateFormatter),
                ev.getHeure().format(timeFormatter),
                ev.getNotificationBeforeMinutes(),
                ev.getResponsable(),
                ev.getParticipants() != null ? String.join(", ", ev.getParticipants()) : "Aucun"
            );

            JLabel notificationLabel = new JLabel(message);
            notificationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(
                null,
                notificationLabel,
                "Medisyns - Rappel",
                JOptionPane.WARNING_MESSAGE
            );
            
            System.out.println("Rappel envoyé: " + ev.getTitre() + " - " + LocalDateTime.now());
        });
    }

    private void showMedisynsNotification(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            String notificationHTML = String.format(
                "<html>" +
                "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 20px; border-radius: 12px; border: 2px solid #B464C8; width: 320px;'>" +
                "<h3 style='margin: 0 0 12px 0; text-align: center; color: #6B46C1; font-size: 16px;'>%s</h3>" +
                "<div style='text-align: center; color: #4A5568; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #E2E8F0;'>%s</div>" +
                "</div>" +
                "</html>",
                title, message
            );

            JLabel notificationLabel = new JLabel(notificationHTML);
            notificationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(
                null,
                notificationLabel,
                "Medisyns",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private void showRedAlert(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            String alertHTML = String.format(
                "<html>" +
                "<div style='background: linear-gradient(135deg, #FED7D7, #FEB2B2); padding: 20px; border-radius: 12px; border: 2px solid #FC8181; width: 320px;'>" +
                "<h3 style='margin: 0 0 12px 0; text-align: center; color: #C53030; font-size: 16px;'>%s</h3>" +
                "<div style='text-align: center; color: #744210; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #FBD38D;'>%s</div>" +
                "</div>" +
                "</html>",
                title, message
            );

            JLabel alertLabel = new JLabel(alertHTML);
            alertLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(
                null,
                alertLabel,
                "Medisyns - Alerte",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    private void showSuccess(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            String successHTML = String.format(
                "<html>" +
                "<div style='background: linear-gradient(135deg, #C6F6D5, #9AE6B4); padding: 20px; border-radius: 12px; border: 2px solid #48BB78; width: 320px;'>" +
                "<h3 style='margin: 0 0 12px 0; text-align: center; color: #2F855A; font-size: 16px;'>%s</h3>" +
                "<div style='text-align: center; color: #22543D; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #9AE6B4;'>%s</div>" +
                "</div>" +
                "</html>",
                title, message
            );

            JLabel successLabel = new JLabel(successHTML);
            successLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(
                null,
                successLabel,
                "Medisyns - Succès",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    public void addRefreshListener(Runnable listener) {
        refreshListeners.add(listener);
    }

    private void notifyRefreshListeners() {
        for (Runnable r : refreshListeners) r.run();
    }

    public void ajouterEvenement(Evenement e) {
        // Lier l'événement à l'utilisateur courant
        if (utilisateurCourant != null) {
            // Utiliser la réflexion pour définir le createurUsername
            try {
                java.lang.reflect.Field field = e.getClass().getDeclaredField("createurUsername");
                field.setAccessible(true);
                field.set(e, utilisateurCourant.getUsername());
            } catch (Exception ex) {
                System.err.println("Erreur lors de la liaison de l'événement à l'utilisateur: " + ex.getMessage());
            }
            
            // Incrémenter le compteur d'événements créés
            utilisateurCourant.incrementerEvenementsCrees();
        }
        
        evenements.add(e);
        planifierNotification(e);
        notifyRefreshListeners();
        sauvegarderDonnees();
        sauvegarderUtilisateurs();
        
        showSuccess("Événement créé", "L'événement \"" + e.getTitre() + "\" a été créé avec succès.");
        System.out.println("Événement créé: " + e.getTitre() + " par " + 
                          (utilisateurCourant != null ? utilisateurCourant.getUsername() : "System"));
    }

    public void supprimerEvenement(Evenement e) {
        if (evenements.remove(e)) {
            notifyRefreshListeners();
            sauvegarderDonnees();
            showSuccess("Événement supprimé", "L'événement \"" + e.getTitre() + "\" a été supprimé.");
            System.out.println("Événement supprimé: " + e.getTitre());
        } else {
            showRedAlert("Erreur", "Impossible de supprimer l'événement \"" + e.getTitre() + "\".");
        }
    }

    public void mettreAJourEvenement(Evenement ancien, Evenement nouveau) {
        int index = evenements.indexOf(ancien);
        if (index != -1) {
            // Conserver le createurUsername de l'ancien événement
            try {
                java.lang.reflect.Field oldField = ancien.getClass().getDeclaredField("createurUsername");
                oldField.setAccessible(true);
                String createurUsername = (String) oldField.get(ancien);
                
                java.lang.reflect.Field newField = nouveau.getClass().getDeclaredField("createurUsername");
                newField.setAccessible(true);
                newField.set(nouveau, createurUsername);
            } catch (Exception ex) {
                System.err.println("Erreur lors de la conservation du createurUsername: " + ex.getMessage());
            }
            
            evenements.set(index, nouveau);
            planifierNotification(nouveau);
            notifyRefreshListeners();
            sauvegarderDonnees();
            
            showSuccess("Événement modifié", "L'événement \"" + nouveau.getTitre() + "\" a été modifié avec succès.");
            System.out.println("Événement modifié: " + ancien.getTitre() + " -> " + nouveau.getTitre());
        } else {
            showRedAlert("Erreur", "Impossible de trouver l'événement à modifier: " + ancien.getTitre());
        }
    }

    public List<Evenement> getEvenements() {
        return new ArrayList<>(evenements);
    }
    
    public List<Evenement> getEvenementsParCreateur(String username) {
        return evenements.stream()
                .filter(ev -> username.equals(ev.getCreateurUsername()))
                .collect(Collectors.toList());
    }

    public List<Evenement> getEvenementsPourDate(LocalDate date) {
        return evenements.stream()
                .filter(ev -> ev.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Evenement> getEvenementsPourDateEtHeure(LocalDate date, LocalTime heure) {
        return evenements.stream()
                .filter(ev -> ev.getDate().equals(date) && ev.getHeure().equals(heure))
                .collect(Collectors.toList());
    }

    public List<Evenement> getEvenementsPourMois(YearMonth mois) {
        return evenements.stream()
                .filter(ev -> YearMonth.from(ev.getDate()).equals(mois))
                .collect(Collectors.toList());
    }

    public Evenement getEvenementParTitre(String titre) {
        return evenements.stream()
                .filter(ev -> ev.getTitre().equals(titre))
                .findFirst()
                .orElse(null);
    }

    private void planifierNotification(Evenement e) {
        if (e.getNotificationBeforeMinutes() <= 0) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventTime = LocalDateTime.of(e.getDate(), e.getHeure());
        LocalDateTime notifTime = eventTime.minusMinutes(e.getNotificationBeforeMinutes());

        if (notifTime.isBefore(now)) return;

        long delaySeconds = Duration.between(now, notifTime).getSeconds();

        scheduler.schedule(() -> {
            SwingUtilities.invokeLater(() -> {
                if (evenements.contains(e)) {
                    String message = String.format(
                        "<html>" +
                        "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 20px; border-radius: 12px; border: 2px solid #B464C8; width: 320px;'>" +
                        "<h3 style='margin: 0 0 12px 0; text-align: center; color: #6B46C1; font-size: 16px;'>Rappel</h3>" +
                        "<div style='color: #4A5568; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #E2E8F0;'>" +
                        "<b>%s</b><br>" +
                        "Le %s à %s<br>" +
                        "Avec %s<br>" +
                        "Dans %d minutes" +
                        "</div>" +
                        "</div>" +
                        "</html>",
                        e.getTitre(),
                        e.getDate().format(dateFormatter),
                        e.getHeure().format(timeFormatter),
                        e.getResponsable(),
                        e.getNotificationBeforeMinutes()
                    );
                    
                    showMedisynsNotification("Rappel d'événement", message);
                }
            });
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public void replanifierToutesNotifications() {
        for (Evenement e : evenements) {
            if (e.getNotificationBeforeMinutes() > 0) {
                planifierNotification(e);
            }
        }
    }

    public void partagerEvenement(Evenement ev, List<String> utilisateurs) {
        EvenementPartage partage = new EvenementPartage(
            ev.getTitre(), 
            ev.getDate(), 
            ev.getHeure(), 
            utilisateurs
        );
        evenementsPartages.add(partage);
        
        // Incrémenter le compteur d'événements partagés pour le createur
        if (utilisateurCourant != null && utilisateurCourant.getUsername().equals(ev.getCreateurUsername())) {
            utilisateurCourant.incrementerEvenementsPartages();
            sauvegarderUtilisateurs();
        }
        
        System.out.println("Événement partagé: " + ev.getTitre() + 
                         " avec " + utilisateurs.size() + " utilisateur(s)");
    }

    public List<EvenementPartage> getEvenementsPartages() {
        return new ArrayList<>(evenementsPartages);
    }

    public void exporterVersCSV(String fichier) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier))) {
            writer.println("Titre,Description,Date,Heure,Responsable,Participants,Notification(min),Createur");
            
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");
            
            for (Evenement ev : evenements) {
                String participants = (ev.getParticipants() != null) 
                    ? String.join(";", ev.getParticipants()) 
                    : "";
                
                String createur = ev.getCreateurUsername() != null ? ev.getCreateurUsername() : "System";
                
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\"%n",
                    ev.getTitre(),
                    ev.getDescription(),
                    ev.getDate().format(dateFmt),
                    ev.getHeure().format(heureFmt),
                    ev.getResponsable(),
                    participants,
                    ev.getNotificationBeforeMinutes(),
                    createur
                );
            }
            
            showSuccess("Export réussi", 
                "Les données ont été exportées vers: " + fichier);
            System.out.println("Export CSV réussi: " + fichier);
        }
    }

    public String exporterVersJSON() {
        StringBuilder json = new StringBuilder("[\n");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");
        
        for (int i = 0; i < evenements.size(); i++) {
            Evenement ev = evenements.get(i);
            json.append("  {\n");
            json.append("    \"titre\": \"").append(escapeJSON(ev.getTitre())).append("\",\n");
            json.append("    \"description\": \"").append(escapeJSON(ev.getDescription())).append("\",\n");
            json.append("    \"date\": \"").append(ev.getDate().format(dateFmt)).append("\",\n");
            json.append("    \"heure\": \"").append(ev.getHeure().format(heureFmt)).append("\",\n");
            json.append("    \"responsable\": \"").append(escapeJSON(ev.getResponsable())).append("\",\n");
            json.append("    \"createur\": \"").append(escapeJSON(ev.getCreateurUsername() != null ? ev.getCreateurUsername() : "System")).append("\",\n");
            json.append("    \"participants\": [");
            if (ev.getParticipants() != null) {
                for (int j = 0; j < ev.getParticipants().size(); j++) {
                    json.append("\"").append(escapeJSON(ev.getParticipants().get(j))).append("\"");
                    if (j < ev.getParticipants().size() - 1) json.append(", ");
                }
            }
            json.append("],\n");
            json.append("    \"notificationBeforeMinutes\": ").append(ev.getNotificationBeforeMinutes()).append("\n");
            json.append("  }");
            if (i < evenements.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJSON(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private void sauvegarderDonnees() {
        try {
            Path saveFilePath = Paths.get(SAVE_FILE);
            if (Files.exists(saveFilePath)) {
                Files.copy(saveFilePath, Paths.get(BACKUP_FILE), 
                          StandardCopyOption.REPLACE_EXISTING);
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(SAVE_FILE))) {
                oos.writeObject(evenements);
                System.out.println("Données sauvegardées: " + evenements.size() + " événements");
            }
        } catch (IOException e) {
            System.err.println("Erreur de sauvegarde: " + e.getMessage());
            showRedAlert("Erreur de sauvegarde", 
                "Impossible de sauvegarder les données: " + e.getMessage());
        }
    }
    
    private void sauvegarderUtilisateurs() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(USERS_FILE))) {
                oos.writeObject(new ArrayList<>(utilisateurs.values()));
                System.out.println("Utilisateurs sauvegardés: " + utilisateurs.size() + " utilisateurs");
            }
        } catch (IOException e) {
            System.err.println("Erreur de sauvegarde utilisateurs: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void chargerDonnees() {
        try {
            if (Files.exists(Paths.get(SAVE_FILE))) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
                    List<Evenement> loadedEvents = (List<Evenement>) ois.readObject();
                    evenements.clear();
                    evenements.addAll(loadedEvents);
                    replanifierToutesNotifications();
                    System.out.println("Données chargées: " + loadedEvents.size() + " événements");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur chargement: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void chargerUtilisateurs() {
        try {
            if (Files.exists(Paths.get(USERS_FILE))) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
                    List<Utilisateur> loadedUsers = (List<Utilisateur>) ois.readObject();
                    utilisateurs.clear();
                    for (Utilisateur user : loadedUsers) {
                        utilisateurs.put(user.getUsername(), user);
                    }
                    System.out.println("Utilisateurs chargés: " + loadedUsers.size() + " utilisateurs");
                }
            } else {
                // Créer des utilisateurs par défaut
                creerUtilisateursParDefaut();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur chargement utilisateurs: " + e.getMessage());
            creerUtilisateursParDefaut();
        }
    }
    
    private void creerUtilisateursParDefaut() {
        // Administrateur par défaut
        Utilisateur admin = new Utilisateur("admin", "Administrateur Medisyns", "ADMIN", "admin@medisyns.com",
                                           "0123456789", "Direction", java.util.Arrays.asList("Administration"));
        
        // Utilisateur simple par défaut
        Utilisateur user = new Utilisateur("user", "Utilisateur Simple", "UTILISATEUR", "user@medisyns.com",
                                          "0234567891", "", new ArrayList<>());
        
        // Médecin par défaut
        Utilisateur medecin = new Utilisateur("medecin", "Dr. Ahmed", "MEDECIN", "ahmed@medisyns.com",
                                             "0345678912", "Cardiologie", java.util.Arrays.asList("Cardiologie", "Médecine générale"));
        
        // Infirmière par défaut
        Utilisateur infirmiere = new Utilisateur("infirmiere", "Infirmière Lina", "INFIRMIERE", "lina@medisyns.com",
                                                "0456789123", "Soins", java.util.Arrays.asList("Soins infirmiers"));
        
        // Patient par défaut
        Utilisateur patient = new Utilisateur("patient", "Patient Test", "PATIENT", "patient@medisyns.com",
                                             "0567891234", "", new ArrayList<>());
        
        utilisateurs.put(admin.getUsername(), admin);
        utilisateurs.put(user.getUsername(), user);
        utilisateurs.put(medecin.getUsername(), medecin);
        utilisateurs.put(infirmiere.getUsername(), infirmiere);
        utilisateurs.put(patient.getUsername(), patient);
        
        sauvegarderUtilisateurs();
        System.out.println("Utilisateurs par défaut créés");
    }

    public boolean restaurerBackup() {
        try {
            if (Files.exists(Paths.get(BACKUP_FILE))) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(BACKUP_FILE))) {
                    @SuppressWarnings("unchecked")
                    List<Evenement> loadedEvents = (List<Evenement>) ois.readObject();
                    evenements.clear();
                    evenements.addAll(loadedEvents);
                    replanifierToutesNotifications();
                    notifyRefreshListeners();
                    showSuccess("Restauration réussie", 
                        "Backup restauré avec succès: " + loadedEvents.size() + " événements");
                    System.out.println("Backup restauré: " + loadedEvents.size() + " événements");
                    return true;
                }
            } else {
                showRedAlert("Pas de backup", "Aucun fichier de backup trouvé.");
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            showRedAlert("Erreur de restauration", 
                "Impossible de restaurer le backup: " + e.getMessage());
            return false;
        }
    }

    public String getStatistiques() {
        long avecNotifications = evenements.stream()
                .filter(e -> e.getNotificationBeforeMinutes() > 0)
                .count();
        
        long aujourdhui = evenements.stream()
                .filter(e -> e.getDate().equals(LocalDate.now()))
                .count();
        
        long passes = evenements.stream()
                .filter(e -> e.getDate().isBefore(LocalDate.now()))
                .count();
        
        long futurs = evenements.stream()
                .filter(e -> e.getDate().isAfter(LocalDate.now()))
                .count();
        
        // Compter les utilisateurs par rôle
        long adminCount = utilisateurs.values().stream()
                .filter(u -> u.estAdministrateur())
                .count();
        
        long userCount = utilisateurs.values().stream()
                .filter(u -> "UTILISATEUR".equals(u.getRole()))
                .count();
        
        long medecinCount = utilisateurs.values().stream()
                .filter(u -> u.estMedecin())
                .count();
        
        long infirmiereCount = utilisateurs.values().stream()
                .filter(u -> u.estInfirmier())
                .count();
        
        long patientCount = utilisateurs.values().stream()
                .filter(u -> u.estPatient())
                .count();
        
        return String.format(
            "📊 STATISTIQUES MEDISYNS\n" +
            "=======================\n\n" +
            "📅 Événements:\n" +
            "• Totaux: %d événements\n" +
            "• Aujourd'hui: %d\n" +
            "• Passés: %d\n" +
            "• À venir: %d\n" +
            "• Avec notifications: %d\n\n" +
            "👥 Utilisateurs (%d au total):\n" +
            "• 👑 Administrateurs: %d\n" +
            "• 👨‍⚕️ Médecins: %d\n" +
            "• 👩‍⚕️ Infirmières: %d\n" +
            "• 👤 Utilisateurs simples: %d\n" +
            "• 👤 Patients: %d\n\n" +
            "💾 Système:\n" +
            "• Fichier de données: %s\n" +
            "• Fichier utilisateurs: %s",
            evenements.size(),
            aujourdhui,
            passes,
            futurs,
            avecNotifications,
            utilisateurs.size(),
            adminCount,
            medecinCount,
            infirmiereCount,
            userCount,
            patientCount,
            SAVE_FILE,
            USERS_FILE
        );
    }

    public int nettoyerEvenementsPasses() {
        LocalDate aujourdhui = LocalDate.now();
        List<Evenement> passes = evenements.stream()
            .filter(ev -> ev.getDate().isBefore(aujourdhui))
            .collect(Collectors.toList());
        
        int count = passes.size();
        passes.forEach(this::supprimerEvenement);
        
        System.out.println(count + " événements passés supprimés");
        return count;
    }

    public Evenement getDernierEvenement() {
        if (evenements.isEmpty()) return null;
        return evenements.get(evenements.size() - 1);
    }

    public void shutdown() {
        sauvegarderDonnees();
        sauvegarderUtilisateurs();
        notificationTimer.stop();
        scheduler.shutdown();
        System.out.println("Système de notifications Medisyns arrêté");
    }

    public static class EvenementPartage implements Serializable {
        private static final long serialVersionUID = 1L;
        private String evenementTitre;
        private LocalDate evenementDate;
        private LocalTime evenementHeure;
        private List<String> utilisateursPartages;
        private LocalDateTime datePartage;
        
        public EvenementPartage(String titre, LocalDate date, LocalTime heure, 
                               List<String> utilisateurs) {
            this.evenementTitre = titre;
            this.evenementDate = date;
            this.evenementHeure = heure;
            this.utilisateursPartages = new ArrayList<>(utilisateurs);
            this.datePartage = LocalDateTime.now();
        }
        
        public String getEvenementTitre() { return evenementTitre; }
        public LocalDate getEvenementDate() { return evenementDate; }
        public LocalTime getEvenementHeure() { return evenementHeure; }
        public List<String> getUtilisateursPartages() { return new ArrayList<>(utilisateursPartages); }
        public LocalDateTime getDatePartage() { return datePartage; }
        
        @Override
        public String toString() {
            return String.format("%s (%s %s) - Partagé avec %d utilisateur(s)",
                evenementTitre, evenementDate, evenementHeure, utilisateursPartages.size());
        }
    }
}