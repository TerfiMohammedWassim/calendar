package com.agenda.controller;

import com.agenda.modele.Evenement;
import com.agenda.modele.Utilisateur;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestionnaire de fichiers JSON pour Medisyns
 * Gère la lecture/écriture des fichiers users.json, events.json, notifications.json
 */
public class JsonManager {
    
    private static final String JSON_DIR = "src/json/";
    private static final String USERS_FILE = JSON_DIR + "users.json";
    private static final String EVENTS_FILE = JSON_DIR + "events.json";
    private static final String NOTIFICATIONS_FILE = JSON_DIR + "notifications.json";
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    // ==================== UTILISATEURS ====================
    
    /**
     * Classe interne pour représenter un utilisateur JSON
     */
    public static class UserJson {
        public int id;
        public String nom;
        public String email;
        public String password;
        public String role;
        
        public UserJson(int id, String nom, String email, String password, String role) {
            this.id = id;
            this.nom = nom;
            this.email = email;
            this.password = password;
            this.role = role;
        }
    }
    
    /**
     * Lit tous les utilisateurs depuis users.json
     */
    public static List<UserJson> lireUtilisateurs() {
        List<UserJson> users = new ArrayList<>();
        try {
            String content = lireFichier(USERS_FILE);
            if (content == null || content.trim().isEmpty()) {
                return users;
            }
            
            // Parser le JSON manuellement (sans bibliothèque externe)
            Pattern pattern = Pattern.compile(
                "\\{[^}]*\"id\"\\s*:\\s*(\\d+)[^}]*" +
                "\"nom\"\\s*:\\s*\"([^\"]*?)\"[^}]*" +
                "\"email\"\\s*:\\s*\"([^\"]*?)\"[^}]*" +
                "\"password\"\\s*:\\s*\"([^\"]*?)\"[^}]*" +
                "\"role\"\\s*:\\s*\"([^\"]*?)\"[^}]*\\}"
            );
            
            // Parser plus flexible pour différents ordres de propriétés
            String[] blocks = content.split("\\},\\s*\\{");
            for (String block : blocks) {
                block = block.replaceAll("[\\[\\]]", "").trim();
                if (!block.startsWith("{")) block = "{" + block;
                if (!block.endsWith("}")) block = block + "}";
                
                int id = extractInt(block, "id");
                String nom = extractString(block, "nom");
                String email = extractString(block, "email");
                String password = extractString(block, "password");
                String role = extractString(block, "role");
                
                if (id > 0 && nom != null && email != null) {
                    users.add(new UserJson(id, nom, email, password != null ? password : "", role != null ? role : "user"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de users.json: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Sauvegarde la liste des utilisateurs dans users.json
     */
    public static void sauvegarderUtilisateurs(List<UserJson> users) {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int i = 0; i < users.size(); i++) {
            UserJson user = users.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(user.id).append(",\n");
            json.append("    \"nom\": \"").append(escapeJson(user.nom)).append("\",\n");
            json.append("    \"email\": \"").append(escapeJson(user.email)).append("\",\n");
            json.append("    \"password\": \"").append(escapeJson(user.password)).append("\",\n");
            json.append("    \"role\": \"").append(escapeJson(user.role)).append("\"\n");
            json.append("  }");
            if (i < users.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("]\n");
        ecrireFichier(USERS_FILE, json.toString());
    }
    
    /**
     * Ajoute un nouvel utilisateur
     */
    public static boolean ajouterUtilisateur(String nom, String email, String password, String role) {
        List<UserJson> users = lireUtilisateurs();
        
        // Vérifier si l'email existe déjà
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) {
                return false; // Email déjà utilisé
            }
        }
        
        // Trouver le prochain ID
        int nextId = 1;
        for (UserJson user : users) {
            if (user.id >= nextId) nextId = user.id + 1;
        }
        
        users.add(new UserJson(nextId, nom, email, password, role));
        sauvegarderUtilisateurs(users);
        return true;
    }
    
    /**
     * Vérifie les identifiants de connexion
     */
    public static UserJson authentifier(String email, String password) {
        List<UserJson> users = lireUtilisateurs();
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Récupère un utilisateur par son ID
     */
    public static UserJson getUtilisateurParId(int id) {
        List<UserJson> users = lireUtilisateurs();
        for (UserJson user : users) {
            if (user.id == id) return user;
        }
        return null;
    }
    
    /**
     * Récupère un utilisateur par son email
     */
    public static UserJson getUtilisateurParEmail(String email) {
        List<UserJson> users = lireUtilisateurs();
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) return user;
        }
        return null;
    }
    
    /**
     * Met à jour les informations d'un utilisateur dans le JSON
     */
    public static boolean mettreAJourUtilisateur(String email, String nom, String telephone, String service) {
        List<UserJson> users = lireUtilisateurs();
        boolean updated = false;
        
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) {
                if (nom != null && !nom.isEmpty()) {
                    user.nom = nom;
                }
                // Note: telephone et service ne sont pas dans UserJson basique
                // On pourrait étendre UserJson pour les supporter
                updated = true;
                break;
            }
        }
        
        if (updated) {
            sauvegarderUtilisateurs(users);
        }
        return updated;
    }
    
    /**
     * Met à jour le nom d'un utilisateur
     */
    public static boolean mettreAJourNomUtilisateur(String email, String nouveauNom) {
        List<UserJson> users = lireUtilisateurs();
        
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) {
                user.nom = nouveauNom;
                sauvegarderUtilisateurs(users);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Met à jour le rôle d'un utilisateur
     */
    public static boolean mettreAJourRoleUtilisateur(String email, String nouveauRole) {
        List<UserJson> users = lireUtilisateurs();
        
        for (UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) {
                user.role = nouveauRole;
                sauvegarderUtilisateurs(users);
                return true;
            }
        }
        return false;
    }
    
    // ==================== ÉVÉNEMENTS ====================
    
    /**
     * Classe interne pour représenter un événement JSON
     */
    public static class EventJson {
        public int event_id;
        public String titre;
        public String description;
        public String date;
        public String heure;
        public int createur_id;
        public String responsable;
        public List<String> participants;
        public int notificationBeforeMinutes;
        public List<Integer> partage_avec;
        
        public EventJson() {
            this.participants = new ArrayList<>();
            this.partage_avec = new ArrayList<>();
        }
    }
    
    /**
     * Lit tous les événements depuis events.json
     */
    public static List<EventJson> lireEvenements() {
        List<EventJson> events = new ArrayList<>();
        try {
            String content = lireFichier(EVENTS_FILE);
            if (content == null || content.trim().isEmpty()) {
                return events;
            }
            
            // Parser le JSON
            String[] blocks = content.split("\\},\\s*\\{");
            for (String block : blocks) {
                block = block.replaceAll("[\\[\\]]", "").trim();
                if (!block.startsWith("{")) block = "{" + block;
                if (!block.endsWith("}")) block = block + "}";
                
                EventJson event = new EventJson();
                event.event_id = extractInt(block, "event_id");
                event.titre = extractString(block, "titre");
                event.description = extractString(block, "description");
                event.date = extractString(block, "date");
                event.heure = extractString(block, "heure");
                event.createur_id = extractInt(block, "createur_id");
                event.responsable = extractString(block, "responsable");
                event.notificationBeforeMinutes = extractInt(block, "notificationBeforeMinutes");
                event.participants = extractStringArray(block, "participants");
                event.partage_avec = extractIntArray(block, "partage_avec");
                
                if (event.event_id > 0 && event.titre != null) {
                    events.add(event);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de events.json: " + e.getMessage());
        }
        return events;
    }
    
    /**
     * Sauvegarde la liste des événements dans events.json
     */
    public static void sauvegarderEvenements(List<EventJson> events) {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int i = 0; i < events.size(); i++) {
            EventJson ev = events.get(i);
            json.append("  {\n");
            json.append("    \"event_id\": ").append(ev.event_id).append(",\n");
            json.append("    \"titre\": \"").append(escapeJson(ev.titre != null ? ev.titre : "")).append("\",\n");
            json.append("    \"description\": \"").append(escapeJson(ev.description != null ? ev.description : "")).append("\",\n");
            json.append("    \"date\": \"").append(ev.date != null ? ev.date : "").append("\",\n");
            json.append("    \"heure\": \"").append(ev.heure != null ? ev.heure : "").append("\",\n");
            json.append("    \"createur_id\": ").append(ev.createur_id).append(",\n");
            json.append("    \"responsable\": \"").append(escapeJson(ev.responsable != null ? ev.responsable : "")).append("\",\n");
            
            // Participants
            json.append("    \"participants\": [");
            if (ev.participants != null && !ev.participants.isEmpty()) {
                for (int j = 0; j < ev.participants.size(); j++) {
                    json.append("\"").append(escapeJson(ev.participants.get(j))).append("\"");
                    if (j < ev.participants.size() - 1) json.append(", ");
                }
            }
            json.append("],\n");
            
            json.append("    \"notificationBeforeMinutes\": ").append(ev.notificationBeforeMinutes).append(",\n");
            
            // Partage avec
            json.append("    \"partage_avec\": [");
            if (ev.partage_avec != null && !ev.partage_avec.isEmpty()) {
                for (int j = 0; j < ev.partage_avec.size(); j++) {
                    json.append(ev.partage_avec.get(j));
                    if (j < ev.partage_avec.size() - 1) json.append(", ");
                }
            }
            json.append("]\n");
            
            json.append("  }");
            if (i < events.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("]\n");
        ecrireFichier(EVENTS_FILE, json.toString());
    }
    
    /**
     * Ajoute un nouvel événement
     */
    public static int ajouterEvenement(String titre, String description, LocalDate date, 
            LocalTime heure, int createurId, String responsable, List<String> participants, 
            int notificationMinutes, List<Integer> partageAvec) {
        
        List<EventJson> events = lireEvenements();
        
        // Trouver le prochain ID
        int nextId = 1;
        for (EventJson event : events) {
            if (event.event_id >= nextId) nextId = event.event_id + 1;
        }
        
        EventJson newEvent = new EventJson();
        newEvent.event_id = nextId;
        newEvent.titre = titre;
        newEvent.description = description;
        newEvent.date = date.format(DATE_FORMAT);
        newEvent.heure = heure.format(TIME_FORMAT);
        newEvent.createur_id = createurId;
        newEvent.responsable = responsable;
        newEvent.participants = participants != null ? new ArrayList<>(participants) : new ArrayList<>();
        newEvent.notificationBeforeMinutes = notificationMinutes;
        newEvent.partage_avec = partageAvec != null ? new ArrayList<>(partageAvec) : new ArrayList<>();
        
        events.add(newEvent);
        sauvegarderEvenements(events);
        
        return nextId;
    }
    
    /**
     * Met à jour le partage d'un événement
     */
    public static void partagerEvenement(int eventId, List<Integer> userIds) {
        List<EventJson> events = lireEvenements();
        for (EventJson event : events) {
            if (event.event_id == eventId) {
                if (event.partage_avec == null) {
                    event.partage_avec = new ArrayList<>();
                }
                for (Integer userId : userIds) {
                    if (!event.partage_avec.contains(userId)) {
                        event.partage_avec.add(userId);
                    }
                }
                break;
            }
        }
        sauvegarderEvenements(events);
    }
    
    /**
     * Supprime un événement
     */
    public static boolean supprimerEvenement(int eventId) {
        List<EventJson> events = lireEvenements();
        boolean removed = events.removeIf(e -> e.event_id == eventId);
        if (removed) {
            sauvegarderEvenements(events);
        }
        return removed;
    }
    
    // ==================== NOTIFICATIONS ====================
    
    /**
     * Classe interne pour représenter une notification JSON
     */
    public static class NotificationJson {
        public int notif_id;
        public int user_id;
        public String message;
        public boolean lu;
        
        public NotificationJson(int notif_id, int user_id, String message, boolean lu) {
            this.notif_id = notif_id;
            this.user_id = user_id;
            this.message = message;
            this.lu = lu;
        }
    }
    
    /**
     * Lit toutes les notifications depuis notifications.json
     */
    public static List<NotificationJson> lireNotifications() {
        List<NotificationJson> notifications = new ArrayList<>();
        try {
            String content = lireFichier(NOTIFICATIONS_FILE);
            if (content == null || content.trim().isEmpty()) {
                return notifications;
            }
            
            String[] blocks = content.split("\\},\\s*\\{");
            for (String block : blocks) {
                block = block.replaceAll("[\\[\\]]", "").trim();
                if (!block.startsWith("{")) block = "{" + block;
                if (!block.endsWith("}")) block = block + "}";
                
                int notif_id = extractInt(block, "notif_id");
                int user_id = extractInt(block, "user_id");
                String message = extractString(block, "message");
                boolean lu = extractBoolean(block, "lu");
                
                if (notif_id > 0 && user_id > 0) {
                    notifications.add(new NotificationJson(notif_id, user_id, message, lu));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de notifications.json: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Sauvegarde les notifications dans notifications.json
     */
    public static void sauvegarderNotifications(List<NotificationJson> notifications) {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int i = 0; i < notifications.size(); i++) {
            NotificationJson notif = notifications.get(i);
            json.append("  {\n");
            json.append("    \"notif_id\": ").append(notif.notif_id).append(",\n");
            json.append("    \"user_id\": ").append(notif.user_id).append(",\n");
            json.append("    \"message\": \"").append(escapeJson(notif.message)).append("\",\n");
            json.append("    \"lu\": ").append(notif.lu).append("\n");
            json.append("  }");
            if (i < notifications.size() - 1) json.append(",");
            json.append("\n");
        }
        
        json.append("]\n");
        ecrireFichier(NOTIFICATIONS_FILE, json.toString());
    }
    
    /**
     * Ajoute une notification
     */
    public static void ajouterNotification(int userId, String message) {
        List<NotificationJson> notifications = lireNotifications();
        
        // Trouver le prochain ID
        int nextId = 1;
        for (NotificationJson notif : notifications) {
            if (notif.notif_id >= nextId) nextId = notif.notif_id + 1;
        }
        
        notifications.add(new NotificationJson(nextId, userId, message, false));
        sauvegarderNotifications(notifications);
    }
    
    /**
     * Récupère les notifications d'un utilisateur
     */
    public static List<NotificationJson> getNotificationsUtilisateur(int userId) {
        List<NotificationJson> allNotifs = lireNotifications();
        List<NotificationJson> userNotifs = new ArrayList<>();
        for (NotificationJson notif : allNotifs) {
            if (notif.user_id == userId) {
                userNotifs.add(notif);
            }
        }
        return userNotifs;
    }
    
    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public static int compterNotificationsNonLues(int userId) {
        List<NotificationJson> userNotifs = getNotificationsUtilisateur(userId);
        int count = 0;
        for (NotificationJson notif : userNotifs) {
            if (!notif.lu) count++;
        }
        return count;
    }
    
    /**
     * Marque une notification comme lue
     */
    public static void marquerCommeLue(int notifId) {
        List<NotificationJson> notifications = lireNotifications();
        for (NotificationJson notif : notifications) {
            if (notif.notif_id == notifId) {
                notif.lu = true;
                break;
            }
        }
        sauvegarderNotifications(notifications);
    }
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    public static void marquerToutesCommeLues(int userId) {
        List<NotificationJson> notifications = lireNotifications();
        for (NotificationJson notif : notifications) {
            if (notif.user_id == userId) {
                notif.lu = true;
            }
        }
        sauvegarderNotifications(notifications);
    }
    
    // ==================== UTILITAIRES ====================
    
    private static String lireFichier(String path) {
        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                // Créer le fichier avec un tableau vide
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, "[]".getBytes());
                return "[]";
            }
            return new String(Files.readAllBytes(filePath), "UTF-8");
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    private static void ecrireFichier(String path, String content) {
        try {
            Path filePath = Paths.get(path);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content.getBytes("UTF-8"));
            System.out.println("✅ Fichier sauvegardé: " + path);
        } catch (IOException e) {
            System.err.println("Erreur écriture fichier " + path + ": " + e.getMessage());
        }
    }
    
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    private static String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"").replace("\\n", "\n");
        }
        return null;
    }
    
    private static int extractInt(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
    
    private static boolean extractBoolean(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return false;
    }
    
    private static List<String> extractStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            Pattern itemPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher itemMatcher = itemPattern.matcher(arrayContent);
            while (itemMatcher.find()) {
                result.add(itemMatcher.group(1));
            }
        }
        return result;
    }
    
    private static List<Integer> extractIntArray(String json, String key) {
        List<Integer> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String arrayContent = matcher.group(1).trim();
            if (!arrayContent.isEmpty()) {
                String[] parts = arrayContent.split(",");
                for (String part : parts) {
                    try {
                        result.add(Integer.parseInt(part.trim()));
                    } catch (NumberFormatException e) {
                        // Ignorer les valeurs non numériques
                    }
                }
            }
        }
        return result;
    }
}
