package com.agenda.modele;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String nomComplet;
    private String role;
    private String email;
    private LocalDate dateInscription;
    private List<String> specialites;
    private String telephone;
    private String service;
    private String avatarColor;
    
    // Statistiques
    private int nombreEvenementsCrees;
    private int nombreEvenementsPartages;
    private LocalDate dernierAcces;

    public Utilisateur(String username, String nomComplet, String role, String email) {
        this.username = username;
        this.nomComplet = nomComplet;
        this.role = role != null ? role.toUpperCase() : "UTILISATEUR";
        this.email = email;
        this.dateInscription = LocalDate.now();
        this.specialites = new ArrayList<>();
        this.telephone = "";
        this.service = "";
        this.avatarColor = generateAvatarColor(username);
        this.nombreEvenementsCrees = 0;
        this.nombreEvenementsPartages = 0;
        this.dernierAcces = LocalDate.now();
    }
    
    public Utilisateur(String username, String nomComplet, String role, String email,
                      String telephone, String service, List<String> specialites) {
        this(username, nomComplet, role, email);
        this.telephone = telephone;
        this.service = service;
        if (specialites != null) {
            this.specialites = specialites;
        }
    }

    public String getUsername() { return username; }
    public String getNomComplet() { return nomComplet; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public LocalDate getDateInscription() { return dateInscription; }
    public List<String> getSpecialites() { return specialites; }
    public String getTelephone() { return telephone; }
    public String getService() { return service; }
    public String getAvatarColor() { return avatarColor; }
    public int getNombreEvenementsCrees() { return nombreEvenementsCrees; }
    public int getNombreEvenementsPartages() { return nombreEvenementsPartages; }
    public LocalDate getDernierAcces() { return dernierAcces; }
    
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setService(String service) { this.service = service; }
    public void setSpecialites(List<String> specialites) { this.specialites = specialites; }
    public void incrementerEvenementsCrees() { this.nombreEvenementsCrees++; }
    public void incrementerEvenementsPartages() { this.nombreEvenementsPartages++; }
    public void mettreAJourDernierAcces() { this.dernierAcces = LocalDate.now(); }
    
    // Méthodes de vérification des permissions
    public boolean peutConsulterEvenements() {
        return true; // Tous les utilisateurs peuvent consulter
    }

    public boolean peutVoirEvenementsPartages() {
        return true; // Tous les utilisateurs peuvent voir les événements partagés
    }

    public boolean peutPartagerEvenements() {
        return true; // Tous les utilisateurs peuvent partager
    }

    public boolean peutCreerEvenements() {
        return estAdministrateur() || estMedecin() || estInfirmier();
    }

    public boolean peutModifierEvenements() {
        return estAdministrateur();
    }

    public boolean peutSupprimerEvenements() {
        return estAdministrateur();
    }

    public boolean peutGererUtilisateurs() {
        return estAdministrateur();
    }

    public boolean peutExporterDonnees() {
        return estAdministrateur();
    }

    public boolean peutVoirStatistiquesCompletes() {
        return estAdministrateur();
    }

    public boolean peutCreerEvenement() {
        return peutCreerEvenements();
    }
    
    public boolean estAdministrateur() {
        return role != null && "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean estMedecin() {
        return role != null && "MEDECIN".equalsIgnoreCase(role);
    }
    
    public boolean estInfirmier() {
        return role != null && "INFIRMIERE".equalsIgnoreCase(role);
    }
    
    public boolean estPatient() {
        return role != null && "PATIENT".equalsIgnoreCase(role);
    }
    
    private String generateAvatarColor(String username) {
        // Génère une couleur basée sur le username
        int hash = username.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        
        // Assurer que la couleur est dans la palette Medisyns
        r = Math.min(150, Math.max(80, r));
        g = Math.min(100, Math.max(30, g));
        b = Math.min(220, Math.max(100, b));
        
        return String.format("#%02X%02X%02X", r, g, b);
    }
    
    public String getInitiales() {
        String[] parts = nomComplet.split(" ");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0);
        }
        return nomComplet.substring(0, Math.min(2, nomComplet.length())).toUpperCase();
    }
    
    public String getRoleDisplay() {
        if (role == null) return "👤 Utilisateur";
        switch(role.toUpperCase()) {
            case "ADMIN": return "👑 Administrateur";
            case "MEDECIN": return "👨‍⚕️ Médecin";
            case "INFIRMIERE": return "👩‍⚕️ Infirmière";
            case "ASSISTANT": return "👨‍💼 Assistant";
            case "PATIENT": return "👤 Patient";
            case "USER":
            case "UTILISATEUR": return "👤 Utilisateur simple";
            default: return "👤 Utilisateur";
        }
    }
    
    public String getStatistiques() {
        return String.format(
            "📊 Statistiques de %s:\n" +
            "• Événements créés: %d\n" +
            "• Événements partagés: %d\n" +
            "• Inscrit depuis: %s\n" +
            "• Dernier accès: %s",
            nomComplet, nombreEvenementsCrees, nombreEvenementsPartages,
            dateInscription, dernierAcces
        );
    }
    
    @Override
    public String toString() {
        return nomComplet + " (" + getRoleDisplay() + ")";
    }
}