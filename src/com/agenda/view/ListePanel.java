package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import com.agenda.modele.Utilisateur;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ListePanel extends JPanel {

    private final AgendaController controller;
    private final DefaultTableModel tableModel;
    private final JTable eventTable;
    private final JTextField searchField;

    public ListePanel(AgendaController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(new Color(250, 245, 255));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(new Color(240, 230, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel searchLabel = new JLabel("🔍 Rechercher : ");
        searchLabel.setForeground(new Color(80, 50, 120));
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        searchField = new JTextField();
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(new Color(80, 50, 120));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 100, 200), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton searchButton = new JButton("🔎 Filtrer");
        searchButton.setBackground(new Color(180, 100, 200));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setFocusPainted(false);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        String[] colonnes = {"📝 Titre", "👤 Responsable", "👥 Participants", "⏰ Heure", "📅 Date", "📄 Description"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        eventTable = new JTable(tableModel);
        eventTable.setBackground(Color.WHITE);
        eventTable.setForeground(new Color(80, 50, 120));
        eventTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eventTable.setRowHeight(28);
        eventTable.setSelectionBackground(new Color(220, 200, 240));
        eventTable.setSelectionForeground(new Color(80, 50, 120));
        eventTable.setGridColor(new Color(220, 220, 220));
        
        eventTable.getTableHeader().setBackground(new Color(180, 100, 200));
        eventTable.getTableHeader().setForeground(Color.WHITE);
        eventTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 100, 200), 2),
                "📋 Tous les Événements",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(80, 50, 120)
        ));

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> appliquerFiltre());
        searchField.addActionListener(e -> appliquerFiltre());

        // 🔥 AJOUT: Raccourcis clavier améliorés
        setupKeyboardShortcuts();

        // Interactions
        eventTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && eventTable.getSelectedRow() != -1) {
                    afficherDetailsEvenement();
                }
                
                if (SwingUtilities.isRightMouseButton(evt) && eventTable.getSelectedRow() != -1) {
                    showContextMenu(eventTable, evt.getX(), evt.getY());
                }
            }
        });

        refreshTable();
    }

    // 🔥 AJOUT: Configuration des raccourcis clavier
    private void setupKeyboardShortcuts() {
        // CTRL+N pour nouveau événement
        KeyStroke ctrlN = KeyStroke.getKeyStroke("control N");
        eventTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ctrlN, "nouvelEvenement");
        eventTable.getActionMap().put("nouvelEvenement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                creerNouvelEvenement();
            }
        });

        // SUPPR pour supprimer
        KeyStroke suppr = KeyStroke.getKeyStroke("DELETE");
        eventTable.getInputMap(JComponent.WHEN_FOCUSED).put(suppr, "supprimerEvenement");
        eventTable.getActionMap().put("supprimerEvenement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supprimerEvenementAvecConfirmation();
            }
        });

        // CTRL+F pour focus recherche
        KeyStroke ctrlF = KeyStroke.getKeyStroke("control F");
        eventTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ctrlF, "focusRecherche");
        eventTable.getActionMap().put("focusRecherche", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.requestFocus();
                searchField.selectAll();
            }
        });

        // ESC pour effacer recherche
        KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
        searchField.getInputMap(JComponent.WHEN_FOCUSED).put(esc, "effacerRecherche");
        searchField.getActionMap().put("effacerRecherche", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                appliquerFiltre();
            }
        });
    }

    // 🔥 AJOUT: Méthode pour créer un nouvel événement
    private void creerNouvelEvenement() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutCreerEvenements()) {
            showPermissionDeniedMessage("créer un nouvel événement");
            return;
        }
        
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            EventDialog dialog = new EventDialog((JFrame) window, controller, java.time.LocalDate.now());
            dialog.setVisible(true);
            refreshTable();
        }
    }

    private void showContextMenu(JTable table, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        
        Utilisateur currentUser = controller.getUtilisateurCourant();
        boolean canModify = currentUser != null && currentUser.peutModifierEvenements();
        boolean canDelete = currentUser != null && currentUser.peutSupprimerEvenements();
        boolean canShare = currentUser != null && currentUser.peutPartagerEvenements();
        
        JMenuItem details = new JMenuItem("🔍 Voir détails");
        details.addActionListener(e -> afficherDetailsEvenement());
        
        JMenuItem modifier = new JMenuItem("✏️ Modifier");
        modifier.setEnabled(canModify);
        modifier.addActionListener(e -> {
            if (canModify) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame) {
                    modifierEvenementSelectionne((JFrame) window);
                }
            } else {
                showPermissionDeniedMessage("modifier");
            }
        });
        
        JMenuItem supprimer = new JMenuItem("🗑️ Supprimer");
        supprimer.setEnabled(canDelete);
        supprimer.addActionListener(e -> {
            if (canDelete) {
                supprimerEvenementAvecConfirmation();
            } else {
                showPermissionDeniedMessage("supprimer");
            }
        });
        
        // 🔥 AJOUT: Option de partage dans le menu contextuel
        JMenuItem partager = new JMenuItem("🔗 Partager avec participants");
        partager.setEnabled(canShare);
        partager.addActionListener(e -> {
            if (canShare) {
                partagerEvenementSelectionne();
            } else {
                showPermissionDeniedMessage("partager");
            }
        });
        
        menu.add(details);
        menu.addSeparator();
        menu.add(modifier);
        menu.add(supprimer);
        menu.addSeparator();
        menu.add(partager);
        
        menu.show(table, x, y);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Evenement> events = controller.getEvenements();
        
        for (Evenement e : events) {
            String participants = "Aucun";
            if (e.getParticipants() != null && !e.getParticipants().isEmpty()) {
                participants = String.join(", ", e.getParticipants());
                if (participants.length() > 50) {
                    participants = participants.substring(0, 47) + "...";
                }
            }
            
            tableModel.addRow(new Object[]{
                    e.getTitre(),
                    e.getResponsable(),
                    participants,
                    e.getHeure().format(heureFmt),
                    e.getDate().format(dateFmt),
                    e.getDescription()
            });
        }
    }

    private void appliquerFiltre() {
        String texte = searchField.getText().toLowerCase().trim();
        tableModel.setRowCount(0);

        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Evenement> filtres = controller.getEvenements().stream()
                .filter(ev ->
                        ev.getTitre().toLowerCase().contains(texte) ||
                        ev.getResponsable().toLowerCase().contains(texte) ||
                        ev.getDate().toString().contains(texte) ||
                        (ev.getParticipants() != null && 
                         ev.getParticipants().toString().toLowerCase().contains(texte))
                ).collect(Collectors.toList());
        
        for (Evenement e : filtres) {
            String participants = "Aucun";
            if (e.getParticipants() != null && !e.getParticipants().isEmpty()) {
                participants = String.join(", ", e.getParticipants());
                if (participants.length() > 50) {
                    participants = participants.substring(0, 47) + "...";
                }
            }
            
            tableModel.addRow(new Object[]{
                    e.getTitre(),
                    e.getResponsable(),
                    participants,
                    e.getHeure().format(heureFmt),
                    e.getDate().format(dateFmt),
                    e.getDescription()
            });
        }
        
        if (filtres.isEmpty() && !texte.isEmpty()) {
            showInfoMessage("🔍 Aucun résultat pour: " + texte);
        }
    }

    // 🔥 AJOUT: Méthode pour partager un événement
    private void partagerEvenementSelectionne() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutPartagerEvenements()) {
            showPermissionDeniedMessage("partager un événement");
            return;
        }
        
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showRedAlert("Sélection requise", "Veuillez sélectionner un événement à partager.");
            return;
        }

        String titre = tableModel.getValueAt(row, 0).toString();
        String responsable = tableModel.getValueAt(row, 1).toString();
        String dateStr = tableModel.getValueAt(row, 4).toString();

        Evenement selectedEvent = controller.getEvenements().stream()
                .filter(ev ->
                        ev.getTitre().equals(titre) &&
                        ev.getResponsable().equals(responsable) &&
                        ev.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).equals(dateStr)
                ).findFirst().orElse(null);

        if (selectedEvent == null) {
            showRedAlert("Événement introuvable", "L'événement sélectionné n'a pas été trouvé.");
            return;
        }

        // 🔥 AJOUT: Dialogue de partage avec sélection multiple
        String[] participantsList = {"Patient","Parent / Accompagnant","Assistante Samira","Infirmière Lina",
                "Dr. Ahmed","Dr. Salima","Technicien Radio","Secrétaire Karima","Stagiaire",
                "Équipe médicale","Visiteur"};
        
        JList<String> participantsJList = new JList<>(participantsList);
        participantsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        participantsJList.setVisibleRowCount(6);
        participantsJList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane listScrollPane = new JScrollPane(participantsJList);
        listScrollPane.setPreferredSize(new Dimension(250, 120));
        
        // 🔥 AJOUT: Panel de partage stylisé
        JPanel sharePanel = new JPanel(new BorderLayout(5, 5));
        sharePanel.setBackground(new Color(250, 245, 255));
        sharePanel.add(new JLabel("Sélectionnez un ou plusieurs participants:"), BorderLayout.NORTH);
        sharePanel.add(listScrollPane, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
                this,
                new Object[]{
                    "🔗 Partager l'événement:",
                    "<html><b style='color: #503278; font-size: 14px;'>" + selectedEvent.getTitre() + "</b></html>",
                    sharePanel
                },
                "Partager l'événement - Medisyns",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedParticipants = participantsJList.getSelectedValuesList();
            if (!selectedParticipants.isEmpty()) {
                String participantsText = String.join(", ", selectedParticipants);
                
                // 🔥 AJOUT: Notification de succès stylisée
                String successHTML = String.format(
                    "<html>" +
                    "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 20px; border-radius: 12px; border: 2px solid #B464C8; width: 380px;'>" +
                    "<h3 style='margin: 0 0 15px 0; text-align: center; color: #6B46C1; font-size: 16px;'>✅ Partage Réussi</h3>" +
                    "<div style='text-align: center; color: #4A5568; font-size: 13px;'>" +
                    "<div style='background: #F8F5FF; padding: 10px; border-radius: 8px; margin: 10px 0; border: 1px solid #D6BCFA;'>" +
                    "<b style='color: #6B46C1;'>%s</b>" +
                    "</div>" +
                    "<div style='color: #718096; margin: 8px 0;'>Partagé avec :</div>" +
                    "<div style='background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #E2E8F0; color: #2D3748; font-weight: 500;'>" +
                    "%s" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</html>",
                    selectedEvent.getTitre(), participantsText
                );

                JLabel successLabel = new JLabel(successHTML);
                successLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JOptionPane.showMessageDialog(
                    this,
                    successLabel,
                    "💜 Medisyns - Partage",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                showRedAlert("Aucun participant sélectionné", "Veuillez sélectionner au moins un participant.");
            }
        }
    }

    // 🔥 AJOUT: Méthode pour les messages d'information
    private void showInfoMessage(String message) {
        String infoHTML = String.format(
            "<html>" +
            "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 15px; border-radius: 10px; color: #4A5568; width: 300px;'>" +
            "<div style='font-size: 12px; text-align: center;'>%s</div>" +
            "</div>" +
            "</html>",
            message
        );

        JLabel infoLabel = new JLabel(infoHTML);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, infoLabel, "💜 Medisyns - Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // 🚨 MÉTHODE POUR ALERTES EN ROUGE
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

    // AJOUT: Méthode pour afficher un message de permission refusée
    private void showPermissionDeniedMessage(String action) {
        Utilisateur user = controller.getUtilisateurCourant();
        String role = user != null ? user.getRoleDisplay() : "Non connecté";
        
        String message = String.format(
            "<html>" +
            "<div style='background: linear-gradient(135deg, #FED7D7, #FEB2B2); padding: 20px; border-radius: 12px; border: 2px solid #FC8181; width: 320px;'>" +
            "<h3 style='margin: 0 0 12px 0; text-align: center; color: #C53030; font-size: 16px;'>⛔ Permission refusée</h3>" +
            "<div style='text-align: center; color: #744210; font-size: 13px; background: #FFFFFF; padding: 12px; border-radius: 8px; border: 1px solid #FBD38D;'>" +
            "Vous ne pouvez pas %s.<br><br>" +
            "<b>Votre rôle:</b> %s<br>" +
            "<b>Action:</b> Réservée aux administrateurs" +
            "</div>" +
            "</div>" +
            "</html>",
            action, role
        );

        JLabel alertLabel = new JLabel(message);
        alertLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(
            this,
            alertLabel,
            "🚨 Permission refusée - Medisyns",
            JOptionPane.WARNING_MESSAGE
        );
    }

    public void supprimerEvenementAvecConfirmation() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutSupprimerEvenements()) {
            showPermissionDeniedMessage("supprimer un événement");
            return;
        }
        
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showRedAlert("Sélection requise", "Veuillez sélectionner un événement à supprimer.");
            return;
        }

        String titre = tableModel.getValueAt(row, 0).toString();
        String responsable = tableModel.getValueAt(row, 1).toString();
        String date = tableModel.getValueAt(row, 4).toString();

        Evenement toDelete = controller.getEvenements().stream()
                .filter(ev ->
                        ev.getTitre().equals(titre) &&
                        ev.getResponsable().equals(responsable) &&
                        ev.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).equals(date)
                ).findFirst().orElse(null);

        if (toDelete == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer l'événement \"" + titre + "\" ?",
                "💜 Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.supprimerEvenement(toDelete);
            refreshTable();
        }
    }

    private void afficherDetailsEvenement() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showRedAlert("Sélection requise", "Veuillez sélectionner un événement.");
            return;
        }

        String message = String.format(
            "📝 Titre : %s\n" +
            "👤 Responsable : %s\n" +
            "👥 Participants : %s\n" +
            "⏰ Heure : %s\n" +
            "📅 Date : %s\n" +
            "📄 Description : %s",
            tableModel.getValueAt(row, 0),
            tableModel.getValueAt(row, 1),
            tableModel.getValueAt(row, 2),
            tableModel.getValueAt(row, 3),
            tableModel.getValueAt(row, 4),
            tableModel.getValueAt(row, 5)
        );

        JOptionPane.showMessageDialog(this, message,
                "Détails - " + tableModel.getValueAt(row, 0),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void modifierEvenementSelectionne(JFrame parent) {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutModifierEvenements()) {
            showPermissionDeniedMessage("modifier un événement");
            return;
        }
        
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showRedAlert("Sélection requise", "Veuillez sélectionner un événement à modifier.");
            return;
        }

        String titre = tableModel.getValueAt(row, 0).toString();
        String responsable = tableModel.getValueAt(row, 1).toString();
        String dateStr = tableModel.getValueAt(row, 4).toString();

        Evenement toEdit = controller.getEvenements().stream()
                .filter(ev ->
                        ev.getTitre().equals(titre) &&
                        ev.getResponsable().equals(responsable) &&
                        ev.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).equals(dateStr)
                ).findFirst().orElse(null);

        if (toEdit == null) {
            showRedAlert("Événement introuvable", "L'événement sélectionné n'a pas été trouvé.");
            return;
        }

        EventDialog dialog = new EventDialog(parent, controller, toEdit.getDate());
        dialog.setEvenement(toEdit);
        dialog.setVisible(true);

        refreshTable();
    }
}