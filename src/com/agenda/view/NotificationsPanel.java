package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.controller.JsonManager;
import com.agenda.controller.JsonManager.NotificationJson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Panel de notifications pour Medisyns
 * Affiche les notifications de l'utilisateur connecté depuis notifications.json
 * Permet de marquer les notifications comme lues
 */
public class NotificationsPanel extends JPanel {
    
    private final AgendaController controller;
    private final JPanel notificationsListPanel;
    private final JLabel countLabel;
    private final JButton markAllReadButton;
    private int currentUserId = -1;
    
    public NotificationsPanel(AgendaController controller) {
        this.controller = controller;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 245, 255));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Liste des notifications
        notificationsListPanel = new JPanel();
        notificationsListPanel.setLayout(new BoxLayout(notificationsListPanel, BoxLayout.Y_AXIS));
        notificationsListPanel.setBackground(new Color(250, 245, 255));
        
        JScrollPane scrollPane = new JScrollPane(notificationsListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 210, 240), 1));
        scrollPane.setBackground(new Color(250, 245, 255));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Labels et boutons
        countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        countLabel.setForeground(new Color(80, 50, 120));
        
        markAllReadButton = new JButton("✅ Tout marquer comme lu");
        styleButton(markAllReadButton);
        markAllReadButton.addActionListener(e -> marquerToutesCommeLues());
        
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(250, 245, 255));
        footerPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        footerPanel.add(countLabel, BorderLayout.WEST);
        footerPanel.add(markAllReadButton, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
        
        // Charger les notifications
        refreshNotifications();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 230, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 100, 200), 2),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("🔔 Mes Notifications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(80, 50, 120));
        
        JButton refreshButton = new JButton("🔄 Rafraîchir");
        styleButton(refreshButton);
        refreshButton.addActionListener(e -> refreshNotifications());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(106, 70, 193));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(124, 90, 210));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(106, 70, 193));
            }
        });
    }
    
    /**
     * Rafraîchit la liste des notifications
     */
    public void refreshNotifications() {
        notificationsListPanel.removeAll();
        
        // Trouver l'ID de l'utilisateur courant
        if (controller.getUtilisateurCourant() != null) {
            String email = controller.getUtilisateurCourant().getEmail();
            JsonManager.UserJson user = JsonManager.getUtilisateurParEmail(email);
            if (user != null) {
                currentUserId = user.id;
            }
        }
        
        if (currentUserId <= 0) {
            JLabel noUserLabel = new JLabel("⚠️ Connectez-vous pour voir vos notifications");
            noUserLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noUserLabel.setForeground(new Color(150, 100, 180));
            noUserLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
            notificationsListPanel.add(noUserLabel);
            countLabel.setText("");
            revalidate();
            repaint();
            return;
        }
        
        // Récupérer les notifications de l'utilisateur
        List<NotificationJson> notifications = JsonManager.getNotificationsUtilisateur(currentUserId);
        
        if (notifications.isEmpty()) {
            JPanel emptyPanel = createEmptyNotificationPanel();
            notificationsListPanel.add(emptyPanel);
            countLabel.setText("Aucune notification");
        } else {
            int nonLues = 0;
            
            // Afficher les non lues en premier
            for (NotificationJson notif : notifications) {
                if (!notif.lu) {
                    notificationsListPanel.add(createNotificationCard(notif));
                    notificationsListPanel.add(Box.createVerticalStrut(10));
                    nonLues++;
                }
            }
            
            // Puis les lues
            for (NotificationJson notif : notifications) {
                if (notif.lu) {
                    notificationsListPanel.add(createNotificationCard(notif));
                    notificationsListPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            countLabel.setText(notifications.size() + " notification(s) - " + nonLues + " non lue(s)");
            markAllReadButton.setEnabled(nonLues > 0);
        }
        
        revalidate();
        repaint();
    }
    
    private JPanel createEmptyNotificationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 245, 255));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        JLabel iconLabel = new JLabel("📭");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel textLabel = new JLabel("Aucune notification");
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textLabel.setForeground(new Color(150, 100, 180));
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTextLabel = new JLabel("Vous serez notifié quand quelqu'un partage un événement avec vous");
        subTextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTextLabel.setForeground(new Color(120, 120, 140));
        subTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(textLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subTextLabel);
        
        return panel;
    }
    
    private JPanel createNotificationCard(NotificationJson notif) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Couleurs différentes selon le statut lu/non lu
        Color bgColor = notif.lu ? new Color(245, 240, 250) : new Color(230, 220, 245);
        Color borderColor = notif.lu ? new Color(200, 190, 210) : new Color(138, 43, 226);
        
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, notif.lu ? 1 : 2),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        // Icône
        JLabel iconLabel = new JLabel(notif.lu ? "📨" : "📩");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        card.add(iconLabel, BorderLayout.WEST);
        
        // Message
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(bgColor);
        
        JLabel messageLabel = new JLabel("<html><body style='width: 350px'>" + notif.message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", notif.lu ? Font.PLAIN : Font.BOLD, 13));
        messageLabel.setForeground(new Color(60, 40, 90));
        
        JLabel statusLabel = new JLabel(notif.lu ? "✓ Lu" : "● Non lu");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(notif.lu ? new Color(100, 180, 100) : new Color(180, 100, 100));
        
        messagePanel.add(messageLabel);
        messagePanel.add(Box.createVerticalStrut(5));
        messagePanel.add(statusLabel);
        
        card.add(messagePanel, BorderLayout.CENTER);
        
        // Bouton marquer comme lu (si non lu)
        if (!notif.lu) {
            JButton markReadButton = new JButton("✓");
            markReadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            markReadButton.setForeground(new Color(100, 180, 100));
            markReadButton.setBackground(new Color(240, 255, 240));
            markReadButton.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 100), 1));
            markReadButton.setPreferredSize(new Dimension(40, 40));
            markReadButton.setToolTipText("Marquer comme lu");
            markReadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            markReadButton.addActionListener(e -> {
                JsonManager.marquerCommeLue(notif.notif_id);
                refreshNotifications();
            });
            
            card.add(markReadButton, BorderLayout.EAST);
        }
        
        return card;
    }
    
    private void marquerToutesCommeLues() {
        if (currentUserId > 0) {
            JsonManager.marquerToutesCommeLues(currentUserId);
            refreshNotifications();
            
            JOptionPane.showMessageDialog(this, 
                "✅ Toutes les notifications ont été marquées comme lues", 
                "Notifications", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Retourne le nombre de notifications non lues
     */
    public int getNombreNotificationsNonLues() {
        if (currentUserId <= 0 && controller.getUtilisateurCourant() != null) {
            String email = controller.getUtilisateurCourant().getEmail();
            JsonManager.UserJson user = JsonManager.getUtilisateurParEmail(email);
            if (user != null) {
                currentUserId = user.id;
            }
        }
        return JsonManager.compterNotificationsNonLues(currentUserId);
    }
}
