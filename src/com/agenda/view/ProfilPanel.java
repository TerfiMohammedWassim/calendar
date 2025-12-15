package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import com.agenda.modele.Utilisateur;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ProfilPanel extends JPanel {
    
    private final AgendaController controller;
    private Utilisateur utilisateurCourant;
    private final JLabel nomLabel;
    private final JLabel roleLabel;
    private final JLabel emailLabel;
    private final JLabel dateInscriptionLabel;
    private final JLabel statsLabel;
    private final JPanel avatarPanel;
    private final DefaultTableModel tableModel;
    private final JTable evenementsTable;
    private final JPanel permissionsPanel;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public ProfilPanel(AgendaController controller) {
        this.controller = controller;
        this.utilisateurCourant = controller.getUtilisateurCourant();
        
        setLayout(new BorderLayout());
        setBackground(new Color(250, 245, 255));
        
        // Initialiser les composants
        this.nomLabel = new JLabel("Chargement...");
        this.roleLabel = new JLabel("Chargement...");
        this.emailLabel = new JLabel("Chargement...");
        this.dateInscriptionLabel = new JLabel("Chargement...");
        this.statsLabel = new JLabel("Statistiques en chargement...");
        this.avatarPanel = createAvatarPanel();
        this.permissionsPanel = new JPanel();
        
        String[] colonnes = {"📝 Titre", "👤 Responsable", "👥 Participants", "📅 Date", "⏰ Heure", "🔔 Rappel"};
        this.tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        this.evenementsTable = new JTable(tableModel);
        configurerTable();
        
        // Panel supérieur avec informations du profil
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central avec les événements de l'utilisateur
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inférieur avec statistiques
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Rafraîchir les données
        if (utilisateurCourant != null) {
            updateProfileInfo();
            refreshEvenements();
            updatePermissionsPanel();
        }
    }
    
    private JPanel createAvatarPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (utilisateurCourant != null) {
                    // Cercle de l'avatar
                    Color avatarColor = Color.decode(utilisateurCourant.getAvatarColor());
                    g2d.setColor(avatarColor);
                    g2d.fillOval(10, 10, 80, 80);
                    
                    // Initiales
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    String initiales = utilisateurCourant.getInitiales();
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = 50 - fm.stringWidth(initiales) / 2;
                    int y = 50 + fm.getAscent() / 2;
                    g2d.drawString(initiales, x, y);
                }
            }
        };
    }
    
    private void configurerTable() {
        evenementsTable.setBackground(Color.WHITE);
        evenementsTable.setForeground(new Color(80, 50, 120));
        evenementsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        evenementsTable.setRowHeight(25);
        evenementsTable.setSelectionBackground(new Color(220, 200, 240));
        evenementsTable.setSelectionForeground(new Color(80, 50, 120));
        evenementsTable.setGridColor(new Color(220, 220, 220));
        
        evenementsTable.getTableHeader().setBackground(new Color(180, 100, 200));
        evenementsTable.getTableHeader().setForeground(Color.WHITE);
        evenementsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 230, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Configurer l'avatar
        avatarPanel.setPreferredSize(new Dimension(100, 100));
        avatarPanel.setBackground(new Color(240, 230, 250));
        
        // Informations du profil à droite
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(240, 230, 250));
        
        nomLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nomLabel.setForeground(new Color(80, 50, 120));
        
        roleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        roleLabel.setForeground(new Color(100, 65, 150));
        
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(new Color(120, 80, 170));
        
        dateInscriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateInscriptionLabel.setForeground(new Color(150, 120, 180));
        
        // Panel des permissions
        permissionsPanel.setLayout(new BoxLayout(permissionsPanel, BoxLayout.Y_AXIS));
        permissionsPanel.setBackground(new Color(240, 230, 250));
        permissionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 100, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        permissionsPanel.setMaximumSize(new Dimension(350, 150));
        
        infoPanel.add(nomLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(roleLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(emailLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateInscriptionLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(permissionsPanel);
        
        // Bouton modifier profil
        JButton editButton = new JButton("✏️ Modifier le profil");
        editButton.setBackground(new Color(180, 100, 200));
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editButton.setFocusPainted(false);
        editButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        editButton.addActionListener(e -> modifierProfil());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 230, 250));
        buttonPanel.add(editButton);
        
        panel.add(avatarPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updatePermissionsPanel() {
        permissionsPanel.removeAll();
        
        JLabel title = new JLabel("🔐 Vos Permissions:");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(80, 50, 120));
        
        JTextArea permissionsText = new JTextArea();
        permissionsText.setEditable(false);
        permissionsText.setBackground(new Color(240, 230, 250));
        permissionsText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        if (utilisateurCourant != null) {
            StringBuilder perms = new StringBuilder();
            
            // Permissions communes à tous
            perms.append("• ✅ Consulter mes événements\n");
            perms.append("• ✅ Voir les événements partagés\n");
            perms.append("• ✅ Partager des événements\n");
            
            // Permissions spécifiques selon le rôle
            if (utilisateurCourant.peutCreerEvenements()) {
                perms.append("• ✅ Créer de nouveaux événements\n");
            } else {
                perms.append("• ❌ Créer de nouveaux événements\n");
            }
            
            if (utilisateurCourant.peutModifierEvenements()) {
                perms.append("• ✅ Modifier les événements\n");
            } else {
                perms.append("• ❌ Modifier les événements\n");
            }
            
            if (utilisateurCourant.peutSupprimerEvenements()) {
                perms.append("• ✅ Supprimer les événements\n");
            } else {
                perms.append("• ❌ Supprimer les événements\n");
            }
            
            if (utilisateurCourant.estAdministrateur()) {
                perms.append("• 👑 Gestion des utilisateurs\n");
                perms.append("• 📊 Statistiques complètes\n");
                perms.append("• 💾 Export des données système\n");
            }
            
            if (utilisateurCourant.estMedecin() || utilisateurCourant.estInfirmier()) {
                perms.append("• 🏥 Accès professionnel\n");
            }
            
            permissionsText.setText(perms.toString());
        }
        
        permissionsPanel.add(title);
        permissionsPanel.add(Box.createVerticalStrut(5));
        permissionsPanel.add(permissionsText);
        
        permissionsPanel.revalidate();
        permissionsPanel.repaint();
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 245, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("📋 Mes Événements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 120));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JScrollPane scrollPane = new JScrollPane(evenementsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 220)));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 220, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 100, 200)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(80, 50, 120));
        
        // Boutons d'action
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(new Color(230, 220, 240));
        
        JButton refreshButton = new JButton("🔄 Actualiser");
        styleSmallButton(refreshButton);
        refreshButton.addActionListener(e -> refreshProfil());
        
        JButton exportButton = new JButton("💾 Exporter mes données");
        styleSmallButton(exportButton);
        exportButton.addActionListener(e -> exporterDonnees());
        
        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);
        
        panel.add(statsLabel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void styleSmallButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(140, 80, 180));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    public void setUtilisateurCourant(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        updateProfileInfo();
        refreshEvenements();
        updatePermissionsPanel();
    }
    
    private void updateProfileInfo() {
        if (utilisateurCourant == null) return;
        
        SwingUtilities.invokeLater(() -> {
            nomLabel.setText(utilisateurCourant.getNomComplet());
            roleLabel.setText(utilisateurCourant.getRoleDisplay());
            emailLabel.setText("📧 " + utilisateurCourant.getEmail());
            dateInscriptionLabel.setText("📅 Inscrit depuis: " + utilisateurCourant.getDateInscription().format(dateFormatter));
            statsLabel.setText(utilisateurCourant.getStatistiques());
            avatarPanel.repaint();
        });
    }
    
    private void refreshEvenements() {
        if (utilisateurCourant == null) return;
        
        tableModel.setRowCount(0);
        List<Evenement> evenementsUtilisateur = controller.getEvenements().stream()
            .filter(ev -> {
                try {
                    java.lang.reflect.Field field = ev.getClass().getDeclaredField("createurUsername");
                    field.setAccessible(true);
                    String createur = (String) field.get(ev);
                    return utilisateurCourant.getUsername().equals(createur);
                } catch (Exception e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        
        for (Evenement ev : evenementsUtilisateur) {
            String participants = "Aucun";
            if (ev.getParticipants() != null && !ev.getParticipants().isEmpty()) {
                participants = String.join(", ", ev.getParticipants());
                if (participants.length() > 30) {
                    participants = participants.substring(0, 27) + "...";
                }
            }
            
            String rappel = ev.getNotificationBeforeMinutes() > 0 ? 
                ev.getNotificationBeforeMinutes() + " min avant" : "Aucun";
            
            tableModel.addRow(new Object[]{
                ev.getTitre(),
                ev.getResponsable(),
                participants,
                ev.getDate().format(dateFormatter),
                ev.getHeure().format(timeFormatter),
                rappel
            });
        }
    }
    
    private void modifierProfil() {
        if (utilisateurCourant == null) return;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modifier le profil", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(250, 245, 255));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(250, 245, 255));
        
        JLabel titleLabel = new JLabel("✏️ Modifier le profil");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(80, 50, 120));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField telephoneField = new JTextField(utilisateurCourant.getTelephone());
        JTextField serviceField = new JTextField(utilisateurCourant.getService());
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        panel.add(createLabeledField("📱 Téléphone:", telephoneField));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createLabeledField("🏥 Service/Département:", serviceField));
        panel.add(Box.createVerticalStrut(30));
        
        JButton saveButton = new JButton("💾 Sauvegarder");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setBackground(new Color(180, 100, 200));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> {
            utilisateurCourant.setTelephone(telephoneField.getText().trim());
            utilisateurCourant.setService(serviceField.getText().trim());
            
            // Sauvegarder dans le JSON si possible
            sauvegarderDansJson();
            
            // Mettre à jour l'interface
            updateProfileInfo();
            
            // Notifier MainFrame pour rafraîchir toute l'application
            notifierMiseAJour();
            
            dialog.dispose();
            showSuccessMessage("Profil mis à jour avec succès !");
        });
        
        panel.add(saveButton);
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(250, 245, 255));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(80, 50, 120));
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 100, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshProfil() {
        updateProfileInfo();
        refreshEvenements();
        updatePermissionsPanel();
        showSuccessMessage("Profil actualisé !");
    }
    
    private void exporterDonnees() {
        if (utilisateurCourant == null) return;
        
        String donnees = String.format(
            "📋 PROFIL UTILISATEUR - Medisyns\n" +
            "=============================\n\n" +
            "👤 Informations personnelles:\n" +
            "• Nom complet: %s\n" +
            "• Rôle: %s\n" +
            "• Email: %s\n" +
            "• Téléphone: %s\n" +
            "• Service: %s\n" +
            "• Date d'inscription: %s\n" +
            "• Dernier accès: %s\n\n" +
            "📊 Statistiques:\n" +
            "• Événements créés: %d\n" +
            "• Événements partagés: %d\n\n" +
            "📅 Mes événements (%d au total):\n",
            utilisateurCourant.getNomComplet(),
            utilisateurCourant.getRoleDisplay(),
            utilisateurCourant.getEmail(),
            utilisateurCourant.getTelephone().isEmpty() ? "Non renseigné" : utilisateurCourant.getTelephone(),
            utilisateurCourant.getService().isEmpty() ? "Non renseigné" : utilisateurCourant.getService(),
            utilisateurCourant.getDateInscription().format(dateFormatter),
            utilisateurCourant.getDernierAcces().format(dateFormatter),
            utilisateurCourant.getNombreEvenementsCrees(),
            utilisateurCourant.getNombreEvenementsPartages(),
            tableModel.getRowCount()
        );
        
        // Ajouter la liste des événements
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            donnees += String.format(
                "\n%d. %s\n" +
                "   Responsable: %s\n" +
                "   Date: %s à %s\n" +
                "   Participants: %s\n",
                i + 1,
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 3),
                tableModel.getValueAt(i, 4),
                tableModel.getValueAt(i, 2)
            );
        }
        
        // Ajouter les permissions
        donnees += "\n\n🔐 Mes permissions:\n";
        if (utilisateurCourant.peutCreerEvenements()) donnees += "• ✅ Créer des événements\n";
        else donnees += "• ❌ Créer des événements\n";
        
        if (utilisateurCourant.peutModifierEvenements()) donnees += "• ✅ Modifier des événements\n";
        else donnees += "• ❌ Modifier des événements\n";
        
        if (utilisateurCourant.peutSupprimerEvenements()) donnees += "• ✅ Supprimer des événements\n";
        else donnees += "• ❌ Supprimer des événements\n";
        
        donnees += "• ✅ Consulter mes événements\n";
        donnees += "• ✅ Voir les événements partagés\n";
        donnees += "• ✅ Partager des événements\n";
        
        if (utilisateurCourant.estAdministrateur()) {
            donnees += "• 👑 Gestion des utilisateurs\n";
            donnees += "• 📊 Statistiques complètes\n";
            donnees += "• 💾 Export des données système\n";
        }
        
        JTextArea textArea = new JTextArea(donnees);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "💾 Données exportées - " + utilisateurCourant.getNomComplet(), 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSuccessMessage(String message) {
        String html = String.format(
            "<html>" +
            "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 15px; border-radius: 10px; width: 300px;'>" +
            "<div style='text-align: center; color: #6B46C1; font-size: 13px;'>✅ %s</div>" +
            "</div>" +
            "</html>",
            message
        );
        
        JLabel label = new JLabel(html);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, label, "💜 Succès", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Sauvegarde les modifications du profil dans le fichier JSON
     */
    private void sauvegarderDansJson() {
        if (utilisateurCourant == null) return;
        
        try {
            // Mettre à jour l'utilisateur dans le JSON via JsonManager
            com.agenda.controller.JsonManager.mettreAJourUtilisateur(
                utilisateurCourant.getEmail(),
                utilisateurCourant.getNomComplet(),
                utilisateurCourant.getTelephone(),
                utilisateurCourant.getService()
            );
            System.out.println("Profil sauvegardé dans JSON pour: " + utilisateurCourant.getEmail());
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde JSON profil: " + e.getMessage());
        }
    }
    
    /**
     * Notifie MainFrame pour rafraîchir toute l'application
     */
    private void notifierMiseAJour() {
        MainFrame mainFrame = MainFrame.getInstance();
        if (mainFrame != null) {
            mainFrame.refreshUserInfo();
        }
    }
}