package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import com.agenda.modele.Utilisateur;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
    private final JLabel statsEventsLabel;
    private final JLabel statsSharedLabel;
    private final JPanel avatarPanel;
    private final DefaultTableModel tableModel;
    private final JTable evenementsTable;
    private JPanel permissionsListPanel;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(102, 51, 153);
    private static final Color PRIMARY_LIGHT = new Color(147, 112, 219);
    private static final Color BACKGROUND = new Color(250, 248, 255);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(50, 50, 70);
    private static final Color TEXT_SECONDARY = new Color(100, 100, 120);
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    public ProfilPanel(AgendaController controller) {
        this.controller = controller;
        this.utilisateurCourant = controller.getUtilisateurCourant();
        
        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        // Initialize components
        this.nomLabel = new JLabel("Chargement...");
        this.roleLabel = new JLabel("Chargement...");
        this.emailLabel = new JLabel("Chargement...");
        this.dateInscriptionLabel = new JLabel("Chargement...");
        this.statsEventsLabel = new JLabel("0");
        this.statsSharedLabel = new JLabel("0");
        this.avatarPanel = createAvatarPanel();
        
        String[] colonnes = {"Titre", "Responsable", "Participants", "Date", "Heure"};
        this.tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        this.evenementsTable = new JTable(tableModel);
        configurerTable();
        
        // Build UI
        add(createHeaderCard(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        
        // Refresh data
        if (utilisateurCourant != null) {
            updateProfileInfo();
            refreshEvenements();
            updatePermissionsList();
        }
    }
    
    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (utilisateurCourant != null) {
                    // Shadow
                    g2d.setColor(new Color(0, 0, 0, 30));
                    g2d.fillOval(7, 7, 86, 86);
                    
                    // Avatar circle with gradient
                    Color avatarColor = Color.decode(utilisateurCourant.getAvatarColor());
                    GradientPaint gradient = new GradientPaint(5, 5, avatarColor.brighter(), 85, 85, avatarColor.darker());
                    g2d.setPaint(gradient);
                    g2d.fillOval(5, 5, 80, 80);
                    
                    // Border
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(5, 5, 80, 80);
                    
                    // Initials
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    String initiales = utilisateurCourant.getInitiales();
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = 45 - fm.stringWidth(initiales) / 2;
                    int y = 45 + fm.getAscent() / 2 - 2;
                    g2d.drawString(initiales, x, y);
                }
            }
        };
        panel.setPreferredSize(new Dimension(95, 95));
        panel.setOpaque(false);
        return panel;
    }
    
    private JPanel createHeaderCard() {
        // Main card panel
        JPanel card = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 20, 20);
                
                // Card background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 20, 20);
                
                // Top accent gradient
                GradientPaint accent = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, PRIMARY_LIGHT);
                g2d.setPaint(accent);
                g2d.fillRoundRect(0, 0, getWidth() - 3, 8, 20, 20);
                g2d.fillRect(0, 4, getWidth() - 3, 8);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));
        
        // Left side - Avatar
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(avatarPanel, BorderLayout.CENTER);
        
        // Center - User info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        nomLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        nomLabel.setForeground(TEXT_PRIMARY);
        nomLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(PRIMARY_COLOR);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailLabel.setForeground(TEXT_SECONDARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dateInscriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateInscriptionLabel.setForeground(TEXT_SECONDARY);
        dateInscriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        centerPanel.add(nomLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(roleLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(emailLabel);
        centerPanel.add(Box.createVerticalStrut(3));
        centerPanel.add(dateInscriptionLabel);
        
        // Right side - Stats cards
        JPanel rightPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(createStatCard("📅", "Événements", statsEventsLabel, new Color(102, 126, 234)));
        rightPanel.add(createStatCard("🔗", "Partagés", statsSharedLabel, new Color(118, 75, 162)));
        
        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createStatCard(String icon, String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Border
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        card.setPreferredSize(new Dimension(100, 80));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textLabel.setForeground(TEXT_SECONDARY);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(valueLabel);
        card.add(textLabel);
        
        return card;
    }
    
    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 15, 0));
        content.setOpaque(false);
        
        // Left - Permissions card
        content.add(createPermissionsCard());
        
        // Right - Events card
        content.add(createEventsCard());
        
        return content;
    }
    
    private JPanel createPermissionsCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);
                
                // Background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel title = new JLabel("🔐 Vos Permissions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Permissions list
        permissionsListPanel = new JPanel();
        permissionsListPanel.setLayout(new BoxLayout(permissionsListPanel, BoxLayout.Y_AXIS));
        permissionsListPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(permissionsListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updatePermissionsList() {
        if (permissionsListPanel == null || utilisateurCourant == null) return;
        
        permissionsListPanel.removeAll();
        
        permissionsListPanel.add(createPermissionItem("Consulter les événements", true));
        permissionsListPanel.add(Box.createVerticalStrut(8));
        permissionsListPanel.add(createPermissionItem("Voir événements partagés", true));
        permissionsListPanel.add(Box.createVerticalStrut(8));
        permissionsListPanel.add(createPermissionItem("Partager des événements", true));
        permissionsListPanel.add(Box.createVerticalStrut(8));
        permissionsListPanel.add(createPermissionItem("Créer des événements", utilisateurCourant.peutCreerEvenements()));
        permissionsListPanel.add(Box.createVerticalStrut(8));
        permissionsListPanel.add(createPermissionItem("Modifier les événements", utilisateurCourant.peutModifierEvenements()));
        permissionsListPanel.add(Box.createVerticalStrut(8));
        permissionsListPanel.add(createPermissionItem("Supprimer les événements", utilisateurCourant.peutSupprimerEvenements()));
        
        if (utilisateurCourant.estAdministrateur()) {
            permissionsListPanel.add(Box.createVerticalStrut(12));
            permissionsListPanel.add(createPermissionItem("👑 Gestion des utilisateurs", true));
            permissionsListPanel.add(Box.createVerticalStrut(8));
            permissionsListPanel.add(createPermissionItem("📊 Statistiques complètes", true));
        }
        
        permissionsListPanel.revalidate();
        permissionsListPanel.repaint();
    }
    
    private JPanel createPermissionItem(String text, boolean enabled) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        JLabel icon = new JLabel(enabled ? "✅" : "❌");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(enabled ? TEXT_PRIMARY : TEXT_SECONDARY);
        
        item.add(icon, BorderLayout.WEST);
        item.add(label, BorderLayout.CENTER);
        
        return item;
    }
    
    private JPanel createEventsCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);
                
                // Background
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel title = new JLabel("📋 Mes Événements");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Table
        JScrollPane scrollPane = new JScrollPane(evenementsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        
        return card;
    }
    
    private void configurerTable() {
        evenementsTable.setBackground(Color.WHITE);
        evenementsTable.setForeground(TEXT_PRIMARY);
        evenementsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        evenementsTable.setRowHeight(32);
        evenementsTable.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 40));
        evenementsTable.setSelectionForeground(TEXT_PRIMARY);
        evenementsTable.setGridColor(new Color(240, 240, 245));
        evenementsTable.setShowGrid(true);
        evenementsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Header styling
        evenementsTable.getTableHeader().setBackground(new Color(250, 248, 255));
        evenementsTable.getTableHeader().setForeground(PRIMARY_COLOR);
        evenementsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        evenementsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_LIGHT));
        
        // Center align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < evenementsTable.getColumnCount(); i++) {
            evenementsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        
        JButton refreshButton = createStyledButton("🔄 Actualiser", new Color(100, 100, 110));
        refreshButton.addActionListener(e -> refreshProfil());
        
        JButton exportButton = createStyledButton("💾 Exporter", PRIMARY_COLOR);
        exportButton.addActionListener(e -> exporterDonnees());
        
        footer.add(refreshButton);
        footer.add(exportButton);
        
        return footer;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    public void setUtilisateurCourant(Utilisateur utilisateur) {
        this.utilisateurCourant = utilisateur;
        updateProfileInfo();
        refreshEvenements();
        updatePermissionsList();
    }
    
    private void updateProfileInfo() {
        if (utilisateurCourant == null) return;
        
        SwingUtilities.invokeLater(() -> {
            nomLabel.setText(utilisateurCourant.getNomComplet());
            roleLabel.setText(utilisateurCourant.getRoleDisplay());
            emailLabel.setText("📧 " + utilisateurCourant.getEmail());
            dateInscriptionLabel.setText("📅 Membre depuis " + utilisateurCourant.getDateInscription().format(dateFormatter));
            statsEventsLabel.setText(String.valueOf(utilisateurCourant.getNombreEvenementsCrees()));
            statsSharedLabel.setText(String.valueOf(utilisateurCourant.getNombreEvenementsPartages()));
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
            String participants = "-";
            if (ev.getParticipants() != null && !ev.getParticipants().isEmpty()) {
                participants = String.join(", ", ev.getParticipants());
                if (participants.length() > 25) {
                    participants = participants.substring(0, 22) + "...";
                }
            }
            
            tableModel.addRow(new Object[]{
                ev.getTitre(),
                ev.getResponsable(),
                participants,
                ev.getDate().format(dateFormatter),
                ev.getHeure().format(timeFormatter)
            });
        }
        
        // Update stats
        statsEventsLabel.setText(String.valueOf(evenementsUtilisateur.size()));
    }
    
    private void refreshProfil() {
        updateProfileInfo();
        refreshEvenements();
        updatePermissionsList();
        showNotification("✅ Profil actualisé");
    }
    
    private void exporterDonnees() {
        if (utilisateurCourant == null) return;
        
        StringBuilder donnees = new StringBuilder();
        donnees.append("═══════════════════════════════════════\n");
        donnees.append("       PROFIL UTILISATEUR - Medisyns\n");
        donnees.append("═══════════════════════════════════════\n\n");
        
        donnees.append("👤 INFORMATIONS PERSONNELLES\n");
        donnees.append("───────────────────────────────────────\n");
        donnees.append(String.format("   Nom complet: %s\n", utilisateurCourant.getNomComplet()));
        donnees.append(String.format("   Rôle: %s\n", utilisateurCourant.getRoleDisplay()));
        donnees.append(String.format("   Email: %s\n", utilisateurCourant.getEmail()));
        donnees.append(String.format("   Téléphone: %s\n", utilisateurCourant.getTelephone().isEmpty() ? "Non renseigné" : utilisateurCourant.getTelephone()));
        donnees.append(String.format("   Service: %s\n", utilisateurCourant.getService().isEmpty() ? "Non renseigné" : utilisateurCourant.getService()));
        donnees.append(String.format("   Inscrit le: %s\n\n", utilisateurCourant.getDateInscription().format(dateFormatter)));
        
        donnees.append("📊 STATISTIQUES\n");
        donnees.append("───────────────────────────────────────\n");
        donnees.append(String.format("   Événements créés: %d\n", utilisateurCourant.getNombreEvenementsCrees()));
        donnees.append(String.format("   Événements partagés: %d\n\n", utilisateurCourant.getNombreEvenementsPartages()));
        
        donnees.append("📋 MES ÉVÉNEMENTS\n");
        donnees.append("───────────────────────────────────────\n");
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            donnees.append(String.format("   %d. %s\n", i + 1, tableModel.getValueAt(i, 0)));
            donnees.append(String.format("      Responsable: %s\n", tableModel.getValueAt(i, 1)));
            donnees.append(String.format("      Date: %s à %s\n\n", tableModel.getValueAt(i, 3), tableModel.getValueAt(i, 4)));
        }
        
        if (tableModel.getRowCount() == 0) {
            donnees.append("   Aucun événement créé\n");
        }
        
        JTextArea textArea = new JTextArea(donnees.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 252));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "💾 Export - " + utilisateurCourant.getNomComplet(), 
            JOptionPane.PLAIN_MESSAGE);
    }
    
    private void showNotification(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Medisyns");
        dialog.setModal(false);
        dialog.setVisible(true);
        
        Timer timer = new Timer(1500, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
