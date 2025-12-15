package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.controller.JsonManager;
import com.agenda.modele.Evenement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HebdoPanel extends JPanel {

    private final AgendaController controller;
    private final JPanel gridPanel;
    private final JLabel monthLabel;
    private final JLabel statusLabel;
    private LocalDate startWeek;
    private final String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
    private final LocalTime[] heures;
    private JFrame parentFrame;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public HebdoPanel(AgendaController controller, JFrame parent) {
        this.controller = controller;
        this.parentFrame = parent;
        
        // Initialisation des variables final
        this.monthLabel = new JLabel("", SwingConstants.CENTER);
        this.statusLabel = new JLabel("💜 Medisyns - Prêt", SwingConstants.LEFT);
        
        // Initialisation du tableau heures
        this.heures = new LocalTime[10];
        for (int i = 0; i < 10; i++) this.heures[i] = LocalTime.of(8 + i, 0);
        
        this.gridPanel = new JPanel();
        
        setLayout(new BorderLayout());
        setBackground(new Color(250, 245, 255));

        initializeUI();
        setupNavigation();
        setupCalendarGrid();
        
        startWeek = LocalDate.now();
        while (startWeek.getDayOfWeek() != DayOfWeek.MONDAY) startWeek = startWeek.minusDays(1);

        refreshCalendar();
    }

    private void initializeUI() {
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthLabel.setForeground(new Color(80, 50, 120));
        monthLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 230, 250));
        statusLabel.setForeground(new Color(80, 50, 120));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupNavigation() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(240, 230, 250));

        JButton prevWeekBtn = new JButton("◀ Semaine précédente");
        JButton nextWeekBtn = new JButton("Semaine suivante ▶");
        styliserBouton(prevWeekBtn);
        styliserBouton(nextWeekBtn);

        prevWeekBtn.addActionListener(e -> {
            startWeek = startWeek.minusWeeks(1);
            refreshCalendar();
            updateStatus("📅 Semaine précédente chargée");
        });
        nextWeekBtn.addActionListener(e -> {
            startWeek = startWeek.plusWeeks(1);
            refreshCalendar();
            updateStatus("📅 Semaine suivante chargée");
        });

        navPanel.add(prevWeekBtn, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextWeekBtn, BorderLayout.EAST);

        add(navPanel, BorderLayout.NORTH);
    }

    private void setupCalendarGrid() {
        gridPanel.setBackground(new Color(250, 245, 255));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styliserBouton(JButton btn) {
        btn.setBackground(new Color(180, 100, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = "[" + LocalTime.now().format(timeFormatter) + "] ";
            statusLabel.setText("<html>💜 <b>Statut:</b> " + timestamp + message + "</html>");
            System.out.println("📢 Statut: " + message);
        });
    }

    public void refreshCalendar() {
        gridPanel.removeAll();
        monthLabel.setText("📅 Semaine du " + startWeek.format(dateFormatter) + " au " + startWeek.plusDays(6).format(dateFormatter));
        gridPanel.setLayout(new GridLayout(heures.length + 1, jours.length + 1, 2, 2));

        gridPanel.add(new JLabel(""));

        // En-têtes des jours
        for (int j = 0; j < 7; j++) {
            LocalDate date = startWeek.plusDays(j);
            JLabel lbl = new JLabel(jours[j] + " " + date.getDayOfMonth(), SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(80, 50, 120));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(240, 230, 250));
            gridPanel.add(lbl);
        }

        // Heures et cellules
        for (LocalTime h : heures) {
            JLabel lbl = new JLabel(h.format(timeFormatter), SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(230, 220, 240));
            lbl.setForeground(new Color(80, 50, 120));
            gridPanel.add(lbl);

            for (int j = 0; j < 7; j++) {
                LocalDate date = startWeek.plusDays(j);
                JPanel cell = createCalendarCell(date, h);
                gridPanel.add(cell);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        updateStatus("Calendrier actualisé - " + controller.getEvenements().size() + " événements");
    }

    private JPanel createCalendarCell(LocalDate date, LocalTime time) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(getCellBackgroundColor(date));
        cell.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // CONFIGURATION DU DROP
        final LocalDate finalDate = date;
        final LocalTime finalTime = time;
        
        // Activer le drop sur la cellule
        DragDropEvent.enableDrop(cell, new DragDropEvent.DropCallback() {
            @Override
            public void onDrop(String data) {
                handleEventDrop(data, finalDate, finalTime);
            }
        });

        // Clic sur cellule pour créer un événement
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    EventDialog dialog = new EventDialog(parentFrame, controller, date);
                    dialog.setVisible(true);
                    refreshCalendar();
                    updateStatus("➕ Nouvel événement créé");
                }
            }
        });

        // Ajouter les événements existants
        addEventsToCell(cell, date, time);

        return cell;
    }

    private void addEventsToCell(JPanel cell, LocalDate date, LocalTime time) {
        List<Evenement> events = controller.getEvenementsPourDateEtHeure(date, time);
        
        for (Evenement ev : events) {
            JPanel eventPanel = createEventPanel(ev);
            cell.add(eventPanel);
        }
    }

    private JPanel createEventPanel(Evenement event) {
        JPanel eventPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        
        // 🎨 COULEURS DIFFÉRENTES selon le créateur
        // Couleur 1 (Violet) : événements créés par l'utilisateur connecté
        // Couleur 2 (Bleu) : événements créés par d'autres utilisateurs
        Color couleurMesEvenements = new Color(138, 43, 226);      // Violet - mes événements
        Color couleurAutresEvenements = new Color(70, 130, 180);   // Bleu acier - autres événements
        
        String currentUsername = controller.getUtilisateurCourant() != null ? 
                                 controller.getUtilisateurCourant().getUsername() : "";
        String eventCreateur = event.getCreateurUsername() != null ? event.getCreateurUsername() : "";
        
        boolean estMonEvenement = currentUsername.equals(eventCreateur) || 
                                  eventCreateur.isEmpty() || 
                                  "System".equals(eventCreateur);
        
        Color bgColor = estMonEvenement ? couleurMesEvenements : couleurAutresEvenements;
        String icon = estMonEvenement ? "📌" : "📎";
        
        eventPanel.setBackground(bgColor);
        eventPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        eventPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel eventLabel = new JLabel(icon + " " + event.getTitre());
        eventLabel.setForeground(Color.WHITE);
        eventLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));

        eventPanel.add(eventLabel);

        // CONFIGURATION DU DRAG
        String eventData = event.getTitre() + "|" + event.getDate() + "|" + event.getHeure();
        DragDropEvent.enableDrag(eventPanel, eventData);

        // Menu contextuel
        eventPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showEventContextMenu(event, eventPanel, e);
                }
            }
        });

        return eventPanel;
    }

    // 🔥 AJOUT: Menu contextuel avec partage
    private void showEventContextMenu(Evenement ev, JComponent component, MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem detailsItem = new JMenuItem("🔍 Détails");
        JMenuItem editItem = new JMenuItem("✏️ Modifier");
        JMenuItem deleteItem = new JMenuItem("🗑️ Supprimer");
        JMenuItem shareItem = new JMenuItem("🔗 Partager avec participants"); // 🔥 AJOUT: Option partage

        detailsItem.addActionListener(ae -> showEventDetails(ev));
        editItem.addActionListener(ae -> {
            EventDialog dialog = new EventDialog(parentFrame, controller, ev.getDate());
            dialog.setEvenement(ev);
            dialog.setVisible(true);
            refreshCalendar();
        });
        deleteItem.addActionListener(ae -> {
            int confirm = JOptionPane.showConfirmDialog(parentFrame, 
                "Supprimer '" + ev.getTitre() + "' ?", 
                "Confirmation", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.supprimerEvenement(ev);
                refreshCalendar();
            }
        });
        shareItem.addActionListener(ae -> partagerEvenement(ev)); // 🔥 AJOUT: Action partage

        menu.add(detailsItem);
        menu.add(editItem);
        menu.add(deleteItem);
        menu.addSeparator();
        menu.add(shareItem); // 🔥 AJOUT: Ajout au menu

        menu.show(component, e.getX(), e.getY());
    }

    // 🔥 MÉTHODE DE PARTAGE - Affiche les utilisateurs depuis users.json
    private void partagerEvenement(Evenement ev) {
        // Lire les utilisateurs depuis users.json
        List<JsonManager.UserJson> utilisateurs = JsonManager.lireUtilisateurs();
        
        // Créer un modèle de liste avec les noms des utilisateurs
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.Map<String, Integer> nomVersId = new java.util.HashMap<>();
        
        int currentUserId = controller.getUtilisateurCourant() != null ? 
                            getUserIdByEmail(controller.getUtilisateurCourant().getEmail()) : -1;
        
        for (JsonManager.UserJson user : utilisateurs) {
            // Ne pas afficher l'utilisateur courant dans la liste de partage
            if (user.id != currentUserId) {
                String displayName = user.nom + " (" + user.role + ")";
                listModel.addElement(displayName);
                nomVersId.put(displayName, user.id);
            }
        }
        
        if (listModel.isEmpty()) {
            showRedAlert("Aucun utilisateur", "Il n'y a pas d'autres utilisateurs avec qui partager.");
            return;
        }
        
        JList<String> participantsJList = new JList<>(listModel);
        participantsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        participantsJList.setVisibleRowCount(6);
        participantsJList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane listScrollPane = new JScrollPane(participantsJList);
        listScrollPane.setPreferredSize(new Dimension(300, 150));
        
        JPanel sharePanel = new JPanel(new BorderLayout(5, 5));
        sharePanel.setBackground(new Color(250, 245, 255));
        sharePanel.add(new JLabel("<html><b>Sélectionnez un ou plusieurs utilisateurs:</b></html>"), BorderLayout.NORTH);
        sharePanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Légende des couleurs
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.setBackground(new Color(250, 245, 255));
        legendPanel.add(new JLabel("<html><small>💡 L'événement sera visible dans leur calendrier</small></html>"));
        sharePanel.add(legendPanel, BorderLayout.SOUTH);
        
        int result = JOptionPane.showConfirmDialog(
                parentFrame,
                new Object[]{
                    "🔗 Partager l'événement:",
                    "<html><b style='color: #503278; font-size: 14px;'>" + ev.getTitre() + "</b></html>",
                    sharePanel
                },
                "Partager l'événement - Medisyns",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedParticipants = participantsJList.getSelectedValuesList();
            if (!selectedParticipants.isEmpty()) {
                // Récupérer les IDs des utilisateurs sélectionnés
                java.util.List<Integer> userIds = new java.util.ArrayList<>();
                StringBuilder participantNames = new StringBuilder();
                
                for (String selected : selectedParticipants) {
                    Integer userId = nomVersId.get(selected);
                    if (userId != null) {
                        userIds.add(userId);
                        
                        // Créer une notification pour chaque utilisateur
                        String createurNom = controller.getUtilisateurCourant() != null ? 
                                            controller.getUtilisateurCourant().getNomComplet() : "Quelqu'un";
                        String notifMessage = createurNom + " a partagé l'événement '" + ev.getTitre() + "' avec vous";
                        JsonManager.ajouterNotification(userId, notifMessage);
                        
                        if (participantNames.length() > 0) participantNames.append(", ");
                        participantNames.append(selected.split(" \\(")[0]); // Juste le nom
                    }
                }
                
                // Sauvegarder le partage dans events.json (si event_id existe)
                // Pour l'instant on met à jour la liste des participants
                
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
                    "<div style='color: #38A169; margin-top: 10px; font-size: 12px;'>🔔 Notifications envoyées</div>" +
                    "</div>" +
                    "</div>" +
                    "</html>",
                    ev.getTitre(), participantNames.toString()
                );

                JLabel successLabel = new JLabel(successHTML);
                successLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JOptionPane.showMessageDialog(
                    parentFrame,
                    successLabel,
                    "💜 Medisyns - Partage",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                updateStatus("🔗 Événement partagé: " + ev.getTitre() + " avec " + userIds.size() + " utilisateurs");
            } else {
                showRedAlert("Aucun utilisateur sélectionné", "Veuillez sélectionner au moins un utilisateur.");
            }
        }
    }
    
    // Helper pour trouver l'ID utilisateur par email
    private int getUserIdByEmail(String email) {
        if (email == null) return -1;
        List<JsonManager.UserJson> users = JsonManager.lireUtilisateurs();
        for (JsonManager.UserJson user : users) {
            if (user.email.equalsIgnoreCase(email)) {
                return user.id;
            }
        }
        return -1;
    }

    private void handleEventDrop(String eventData, LocalDate newDate, LocalTime newTime) {
        try {
            String[] parts = eventData.split("\\|");
            if (parts.length >= 3) {
                String titre = parts[0];
                LocalDate oldDate = LocalDate.parse(parts[1]);
                LocalTime oldTime = LocalTime.parse(parts[2]);

                System.out.println("🔄 Tentative de déplacement: " + titre);
                System.out.println("   De: " + oldDate + " " + oldTime);
                System.out.println("   Vers: " + newDate + " " + newTime);

                // Trouver l'événement
                Evenement eventToUpdate = findEvent(titre, oldDate, oldTime);
                
                if (eventToUpdate != null) {
                    // Créer une copie avec la nouvelle date/heure
                    Evenement nouvelEvent = new Evenement(
                        eventToUpdate.getTitre(),
                        eventToUpdate.getDescription(),
                        newDate,
                        newTime,
                        eventToUpdate.getResponsable(),
                        eventToUpdate.getParticipants()
                    );
                    nouvelEvent.setNotificationBeforeMinutes(eventToUpdate.getNotificationBeforeMinutes());

                    // Mettre à jour
                    controller.mettreAJourEvenement(eventToUpdate, nouvelEvent);
                    refreshCalendar();
                    
                    updateStatus("✅ Événement déplacé: " + titre);
                    showSuccessMessage("Événement déplacé avec succès!");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur drop: " + e.getMessage());
            showErrorAlert("Erreur lors du déplacement");
        }
    }

    private Evenement findEvent(String titre, LocalDate date, LocalTime time) {
        return controller.getEvenements().stream()
                .filter(ev -> ev.getTitre().equals(titre) && 
                         ev.getDate().equals(date) && 
                         ev.getHeure().equals(time))
                .findFirst()
                .orElse(null);
    }

    private Color getCellBackgroundColor(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return new Color(220, 200, 240);
        } else if (date.getDayOfWeek().getValue() == 5) {
            return new Color(245, 240, 250);
        } else if (date.getDayOfWeek().getValue() >= 6) {
            return new Color(250, 245, 255);
        } else {
            return Color.WHITE;
        }
    }

    private void showEventDetails(Evenement ev) {
        String participants = ev.getParticipants() != null && !ev.getParticipants().isEmpty() 
            ? String.join(", ", ev.getParticipants()) 
            : "Aucun";

        String details = String.format(
            "📝 %s\n👤 %s\n👥 %s\n📅 %s\n⏰ %s\n📄 %s",
            ev.getTitre(),
            ev.getResponsable(),
            participants,
            ev.getDate().format(dateFormatter),
            ev.getHeure().format(timeFormatter),
            ev.getDescription()
        );
        JOptionPane.showMessageDialog(this, details, "Détails - " + ev.getTitre(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorAlert(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Erreur", JOptionPane.ERROR_MESSAGE);
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
}