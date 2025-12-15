package com.agenda.modele;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Evenement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String titre;
    private String description;
    private LocalDate date;
    private LocalTime heure;
    private String responsable;
    private List<String> participants;
    private int notificationBeforeMinutes = 0;
    private String createurUsername; // Nouveau: l'utilisateur qui a créé l'événement

    public Evenement(String titre, String description, LocalDate date, LocalTime heure, 
                    String responsable, List<String> participants, String createurUsername) {
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.heure = heure;
        this.responsable = responsable;
        this.participants = participants;
        this.createurUsername = createurUsername;
    }

    public Evenement(String titre, String description, LocalDate date, LocalTime heure, 
                    String responsable, List<String> participants) {
        this(titre, description, date, heure, responsable, participants, "System");
    }

    public Evenement(String titre, String description, LocalDate date, LocalTime heure, String responsable) {
        this(titre, description, date, heure, responsable, null, "System");
    }

    // Getters
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public String getResponsable() { return responsable; }
    public List<String> getParticipants() { return participants; }
    public String getCreateurUsername() { return createurUsername; }

    // Setters
    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeure(LocalTime heure) { this.heure = heure; }
    public void setResponsable(String resp) { this.responsable = resp; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public void setCreateurUsername(String createurUsername) { this.createurUsername = createurUsername; }

    public int getNotificationBeforeMinutes() { return notificationBeforeMinutes; }
    public void setNotificationBeforeMinutes(int minutes) { this.notificationBeforeMinutes = minutes; }

    // Méthode pour vérifier les permissions
    public boolean peutModifier(String utilisateurUsername, Utilisateur utilisateur) {
        if (utilisateur == null) return false;
        
        // L'administrateur peut modifier tous les événements
        if (utilisateur.estAdministrateur()) return true;
        
        // Le créateur peut modifier son propre événement
        if (createurUsername != null && createurUsername.equals(utilisateurUsername)) return true;
        
        // Les médecins peuvent modifier les événements dont ils sont responsables
        if (utilisateur.estMedecin() && responsable != null && responsable.contains(utilisateur.getNomComplet())) {
            return true;
        }
        
        return false;
    }
    
    public boolean peutSupprimer(String utilisateurUsername, Utilisateur utilisateur) {
        return peutModifier(utilisateurUsername, utilisateur);
    }

    @Override
    public String toString() {
        return date + " " + heure + " " + titre + " - " + responsable + " (Créé par: " + createurUsername + ")";
    }
    
    // Alias pour compatibilité
    public String getCreateur() { return createurUsername; }
}
