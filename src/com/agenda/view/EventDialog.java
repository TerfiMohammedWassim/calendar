package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.Border;

public class EventDialog extends JDialog {

    private JTextField titreField, descField, respField;
    private JList<String> partList;
    private JSpinner dateSpinner;
    private JComboBox<Integer> heureCombo, minuteCombo;
    private JComboBox<Integer> notifCombo;
    private Evenement evenementOriginal;

    private final AgendaController controller;
    private String[] participantsList = {"Patient","Parent / Accompagnant","Assistante Samira","Infirmière Lina",
            "Dr. Ahmed","Dr. Salima","Technicien Radio","Secrétaire Karima","Stagiaire",
            "Équipe médicale","Visiteur"};

    public EventDialog(JFrame parent, AgendaController controller, LocalDate date) {
        super(parent, "💜 " + (controller.getUtilisateurCourant().peutCreerEvenements() ? 
              "Nouvel Événement" : "Consulter Événement") + " - Medisyns", true);
        this.controller = controller;

        setSize(450, 520);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(250, 245, 255));

        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        JPanel container = new JPanel(new GridLayout(9, 2, 10, 10));
        container.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        container.setBackground(new Color(250, 245, 255));
        setContentPane(container);

        Border fieldBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 100, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

        titreField = new JTextField(); 
        titreField.setFont(font); 
        titreField.setBorder(fieldBorder);
        titreField.setBackground(Color.WHITE);

        descField = new JTextField(); 
        descField.setFont(font); 
        descField.setBorder(fieldBorder);
        descField.setBackground(Color.WHITE);

        respField = new JTextField(); 
        respField.setFont(font); 
        respField.setBorder(fieldBorder);
        respField.setBackground(Color.WHITE);

        partList = new JList<>(participantsList);
        partList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        partList.setFont(font);
        partList.setBackground(Color.WHITE);
        partList.setForeground(new Color(80, 50, 120));
        partList.setVisibleRowCount(4);
        
        JScrollPane partScroll = new JScrollPane(partList);
        partScroll.setBorder(fieldBorder);
        partScroll.setBackground(Color.WHITE);

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(java.sql.Date.valueOf(date));
        ((JComponent) dateSpinner.getEditor()).setBorder(fieldBorder);
        ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setBackground(Color.WHITE);

        heureCombo = new JComboBox<>(); 
        minuteCombo = new JComboBox<>();
        for (int h = 0; h < 24; h++) heureCombo.addItem(h);
        for (int m = 0; m < 60; m += 5) minuteCombo.addItem(m);
        heureCombo.setSelectedItem(8); 
        minuteCombo.setSelectedItem(0);
        
        heureCombo.setBackground(Color.WHITE);
        minuteCombo.setBackground(Color.WHITE);

        notifCombo = new JComboBox<>();
        int[] notifTimes = {0, 5, 10, 15, 20, 30, 60};
        for (int t : notifTimes) notifCombo.addItem(t);
        notifCombo.setSelectedItem(10);
        notifCombo.setBackground(Color.WHITE);

        JButton addBtn = new JButton("💾 Sauvegarder"); 
        JButton cancelBtn = new JButton("❌ Annuler");
        
        addBtn.setBackground(new Color(180, 100, 200));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        cancelBtn.setBackground(new Color(150, 150, 150));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel titreLabel = new JLabel("📝 Titre * :");
        titreLabel.setForeground(new Color(80, 50, 120));
        
        JLabel descLabel = new JLabel("📄 Description :");
        descLabel.setForeground(new Color(80, 50, 120));
        
        JLabel respLabel = new JLabel("👤 Responsable * :");
        respLabel.setForeground(new Color(80, 50, 120));
        
        JLabel partLabel = new JLabel("👥 Participants * :");
        partLabel.setForeground(new Color(80, 50, 120));
        
        JLabel dateLabel = new JLabel("📅 Date * :");
        dateLabel.setForeground(new Color(80, 50, 120));
        
        JLabel heureLabel = new JLabel("⏰ Heure * :");
        heureLabel.setForeground(new Color(80, 50, 120));
        
        JLabel notifLabel = new JLabel("🔔 Notification avant (min) :");
        notifLabel.setForeground(new Color(80, 50, 120));
        
        // Afficher le créateur si disponible
        String createurInfo = "";
        if (controller.getUtilisateurCourant() != null) {
            createurInfo = "Créé par: " + controller.getUtilisateurCourant().getNomComplet();
        }
        JLabel createurLabel = new JLabel(createurInfo);
        createurLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        createurLabel.setForeground(new Color(100, 65, 150));

        container.add(titreLabel); container.add(titreField);
        container.add(descLabel); container.add(descField);
        container.add(respLabel); container.add(respField);
        container.add(partLabel); container.add(partScroll);
        container.add(dateLabel); container.add(dateSpinner);
        
        JPanel heurePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        heurePanel.setBackground(new Color(250, 245, 255));
        heurePanel.add(heureCombo); heurePanel.add(new JLabel(":")); heurePanel.add(minuteCombo);
        container.add(heureLabel); container.add(heurePanel);
        
        container.add(notifLabel); container.add(notifCombo);
        container.add(createurLabel); container.add(new JLabel(""));
        container.add(addBtn); container.add(cancelBtn);

        // Vérifier les permissions
        com.agenda.modele.Utilisateur user = controller.getUtilisateurCourant();
        boolean isReadOnly = false;
        
        if (user != null) {
            if (!user.peutCreerEvenements() && evenementOriginal == null) {
                // Mode consultation seulement pour création
                isReadOnly = true;
                addBtn.setEnabled(false);
                addBtn.setText("🔒 Consultation seule");
                addBtn.setBackground(new Color(150, 150, 150));
            } else if (evenementOriginal != null && !user.peutModifierEvenements()) {
                // Mode consultation seulement pour modification
                isReadOnly = true;
                addBtn.setEnabled(false);
                addBtn.setText("🔒 Modification non autorisée");
                addBtn.setBackground(new Color(150, 150, 150));
            }
        }
        
        if (isReadOnly) {
            titreField.setEditable(false);
            descField.setEditable(false);
            respField.setEditable(false);
            partList.setEnabled(false);
            dateSpinner.setEnabled(false);
            heureCombo.setEnabled(false);
            minuteCombo.setEnabled(false);
            notifCombo.setEnabled(false);
        }

        addBtn.addActionListener(e -> {
            String titre = titreField.getText().trim();
            String desc = descField.getText().trim();
            String resp = respField.getText().trim();
            List<String> selectedParticipants = partList.getSelectedValuesList();

            if(titre.isEmpty() || resp.isEmpty() || selectedParticipants.isEmpty()) {
                showRedAlert("Champs obligatoires", 
                    "Les champs suivants sont obligatoires:\n" +
                    "• Titre\n• Responsable\n• Au moins 1 participant");
                return;
            }

            // Valider l'événement
            if (!validerEvenement(titre, resp, selectedParticipants, 
                    new java.sql.Date(((java.util.Date)dateSpinner.getValue()).getTime()).toLocalDate(),
                    LocalTime.of((int)heureCombo.getSelectedItem(), (int)minuteCombo.getSelectedItem()))) {
                return;
            }

            LocalDate d = new java.sql.Date(((java.util.Date)dateSpinner.getValue()).getTime()).toLocalDate();
            LocalTime time = LocalTime.of((int)heureCombo.getSelectedItem(), (int)minuteCombo.getSelectedItem());
            int notifMinutes = (int) notifCombo.getSelectedItem();

            List<String> participantsObj = new ArrayList<>(selectedParticipants);
            
            // Récupérer le nom d'utilisateur du créateur
            String createurUsername = controller.getUtilisateurCourant() != null ? 
                                     controller.getUtilisateurCourant().getUsername() : "System";

            Evenement nouvelEv = new Evenement(titre, desc, d, time, resp, participantsObj, createurUsername);
            nouvelEv.setNotificationBeforeMinutes(notifMinutes);

            if (evenementOriginal != null) {
                controller.mettreAJourEvenement(evenementOriginal, nouvelEv);
            } else {
                controller.ajouterEvenement(nouvelEv);
            }
            
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());
        
        if (evenementOriginal != null) {
            setTitle("💜 Modifier Événement - Medisyns");
            addBtn.setText("💾 Modifier");
        }
    }

    private void showRedAlert(String title, String message) {
        String alertHTML = String.format(
            "<html>" +
            "<div style='background: linear-gradient(135deg, #FED7D7, #FEB2B2); padding: 20px; border-radius: 12px; border: 2px solid #FC8181; width: 320px;'>" +
            "<h3 style='margin: 0 0 12px 0; text-align: center; color: #C53030; font-size: 16px;'>⚠️ %s</h3>" +
            "<div style='text-align: center; color: #744210; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #FBD38D;'>%s</div>" +
            "</div>" +
            "</html>",
            title, message
        );

        JLabel alertLabel = new JLabel(alertHTML);
        alertLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(
            this,
            alertLabel,
            "🚨 Medisyns - Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public void setEvenement(Evenement toEdit){
        this.evenementOriginal = toEdit;
        
        titreField.setText(toEdit.getTitre());
        descField.setText(toEdit.getDescription());
        respField.setText(toEdit.getResponsable());
        if(toEdit.getParticipants() != null && !toEdit.getParticipants().isEmpty()) {
            List<String> toSelect = toEdit.getParticipants();
            int[] indices = new int[toSelect.size()];
            for(int i=0; i<toSelect.size(); i++) {
                for(int j=0; j<participantsList.length; j++) {
                    if(participantsList[j].equals(toSelect.get(i))) {
                        indices[i] = j;
                        break;
                    }
                }
            }
            partList.setSelectedIndices(indices);
        }
        dateSpinner.setValue(java.sql.Date.valueOf(toEdit.getDate()));
        heureCombo.setSelectedItem(toEdit.getHeure().getHour());
        minuteCombo.setSelectedItem(toEdit.getHeure().getMinute());
        notifCombo.setSelectedItem(toEdit.getNotificationBeforeMinutes());
        
        setTitle("💜 Modifier Événement - Medisyns");
    }
    
    private boolean validerEvenement(String titre, String resp, 
                                List<String> participants,
                                LocalDate date, LocalTime heure) {
        // Vérifier titre
        if (titre.isEmpty() || titre.length() < 3) {
            showRedAlert("Titre invalide", 
                "Le titre doit contenir au moins 3 caractères.");
            return false;
        }
        
        // Vérifier responsable
        if (resp.isEmpty()) {
            showRedAlert("Responsable requis", 
                "Vous devez spécifier un responsable.");
            return false;
        }
        
        // Vérifier participants
        if (participants.isEmpty()) {
            showRedAlert("Participants requis", 
                "Vous devez sélectionner au moins un participant.");
            return false;
        }
        
        // Vérifier date dans le passé
        LocalDateTime eventDateTime = LocalDateTime.of(date, heure);
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Cet événement est dans le passé.\nVoulez-vous quand même le créer ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION);
            return confirm == JOptionPane.YES_OPTION;
        }
        
        // Vérifier chevauchement d'événements
        if (evenementOriginal == null) {
            List<Evenement> existants = controller.getEvenementsPourDateEtHeure(date, heure);
            if (!existants.isEmpty()) {
                StringBuilder conflits = new StringBuilder("⚠️ Conflit détecté:\n");
                for (Evenement ev : existants) {
                    conflits.append("• ").append(ev.getTitre())
                           .append(" (").append(ev.getResponsable()).append(")\n");
                }
                conflits.append("\nVoulez-vous quand même créer cet événement ?");
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    conflits.toString(),
                    "Conflit d'horaire",
                    JOptionPane.YES_NO_OPTION);
                return confirm == JOptionPane.YES_OPTION;
            }
        }
        
        return true;
    }
}