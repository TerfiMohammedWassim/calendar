package com.agenda.view;

import com.agenda.modele.Utilisateur;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialogue de bienvenue affiché après connexion réussie
 */
public class BienvenuDialog extends JDialog {
    
    private static final Color PRIMARY_COLOR = new Color(102, 51, 153);
    private static final Color PRIMARY_LIGHT = new Color(147, 112, 219);
    private static final Color BACKGROUND = new Color(250, 248, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 70);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    
    private final Utilisateur utilisateur;
    private Timer autoCloseTimer;
    
    public BienvenuDialog(Frame parent, Utilisateur utilisateur) {
        super(parent, "Bienvenue", true);
        this.utilisateur = utilisateur;
        
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        
        // Forme arrondie
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, 480, 520, 20, 20));
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond dégradé
                GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND,
                    0, getHeight(), new Color(240, 230, 250)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Bordure
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(30, 35, 25, 35));
        
        // Icône de succès
        JLabel successIcon = new JLabel("✓");
        successIcon.setFont(new Font("Segoe UI", Font.BOLD, 50));
        successIcon.setForeground(SUCCESS_COLOR);
        successIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Titre bienvenue
        JLabel welcomeLabel = new JLabel("Bienvenue !");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Avatar et nom
        JPanel avatarPanel = createAvatarPanel();
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nom de l'utilisateur
        JLabel nameLabel = new JLabel(utilisateur.getNomComplet());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Informations panel
        JPanel infoPanel = createInfoPanel();
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Fonctionnalités panel
        JPanel functionalitiesPanel = createFunctionalitiesPanel();
        functionalitiesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Bouton Continuer
        JButton continueButton = new JButton("Continuer vers l'application");
        styleButton(continueButton);
        continueButton.addActionListener(e -> {
            if (autoCloseTimer != null) {
                autoCloseTimer.stop();
            }
            dispose();
        });
        
        mainPanel.add(successIcon);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(avatarPanel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(nameLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(functionalitiesPanel);
        mainPanel.add(Box.createVerticalStrut(18));
        mainPanel.add(continueButton);
        
        setContentPane(mainPanel);
        
        // Fermer avec Escape ou clic
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Cercle avatar
                Color avatarColor = Color.decode(utilisateur.getAvatarColor());
                g2d.setColor(avatarColor);
                g2d.fillOval(10, 5, 60, 60);
                
                // Initiales
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
                String initiales = utilisateur.getInitiales();
                FontMetrics fm = g2d.getFontMetrics();
                int x = 40 - fm.stringWidth(initiales) / 2;
                int y = 35 + fm.getAscent() / 2 - 2;
                g2d.drawString(initiales, x, y);
            }
        };
        panel.setPreferredSize(new Dimension(80, 70));
        panel.setMaximumSize(new Dimension(80, 70));
        panel.setOpaque(false);
        return panel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_LIGHT, 1, true),
            new EmptyBorder(12, 18, 12, 18)
        ));
        panel.setMaximumSize(new Dimension(380, 100));
        
        // Rôle
        JPanel roleRow = createInfoRow("👤 Rôle:", utilisateur.getRoleDisplay());
        
        // Accès
        String accessLevel = getAccessLevel();
        JPanel accessRow = createInfoRow("🔐 Niveau d'accès:", accessLevel);
        
        // Email
        JPanel emailRow = createInfoRow("📧 Email:", utilisateur.getEmail());
        
        panel.add(roleRow);
        panel.add(Box.createVerticalStrut(6));
        panel.add(accessRow);
        panel.add(Box.createVerticalStrut(6));
        panel.add(emailRow);
        
        return panel;
    }
    
    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(350, 22));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComp.setForeground(TEXT_COLOR);
        labelComp.setPreferredSize(new Dimension(120, 20));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueComp.setForeground(PRIMARY_COLOR);
        
        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);
        
        return row;
    }
    
    private String getAccessLevel() {
        if (utilisateur.estAdministrateur()) {
            return "Complet (Admin)";
        } else if (utilisateur.estMedecin()) {
            return "Professionnel (Médecin)";
        } else if (utilisateur.estInfirmier()) {
            return "Professionnel (Infirmier)";
        } else {
            return "Standard (Utilisateur)";
        }
    }
    
    private JPanel createFunctionalitiesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(PRIMARY_LIGHT, 1, true),
                "📋 Vos fonctionnalités",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11),
                PRIMARY_COLOR
            ),
            new EmptyBorder(5, 10, 8, 10)
        ));
        panel.setMaximumSize(new Dimension(380, 130));
        
        // Fonctionnalités selon le rôle
        if (utilisateur.estAdministrateur()) {
            panel.add(createFunctionalityItem("✅ Gérer tous les événements"));
            panel.add(createFunctionalityItem("✅ Créer, modifier, supprimer"));
            panel.add(createFunctionalityItem("✅ Gestion des utilisateurs"));
            panel.add(createFunctionalityItem("✅ Statistiques complètes"));
            panel.add(createFunctionalityItem("✅ Export des données"));
        } else if (utilisateur.estMedecin() || utilisateur.estInfirmier()) {
            panel.add(createFunctionalityItem("✅ Consulter les événements"));
            panel.add(createFunctionalityItem("✅ Créer des événements"));
            panel.add(createFunctionalityItem("✅ Partager des événements"));
            panel.add(createFunctionalityItem("❌ Modifier/Supprimer (Admin)"));
        } else {
            panel.add(createFunctionalityItem("✅ Consulter les événements"));
            panel.add(createFunctionalityItem("✅ Voir événements partagés"));
            panel.add(createFunctionalityItem("✅ Partager des événements"));
            panel.add(createFunctionalityItem("❌ Créer des événements (Admin/Médecin)"));
        }
        
        return panel;
    }
    
    private JLabel createFunctionalityItem(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(text.startsWith("✅") ? new Color(40, 120, 60) : new Color(150, 100, 100));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setMaximumSize(new Dimension(300, 45));
        button.setPreferredSize(new Dimension(300, 45));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_LIGHT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }
    
    /**
     * Affiche le dialogue avec fermeture automatique après un délai
     */
    public void showWithAutoClose(int delaySeconds) {
        autoCloseTimer = new Timer(delaySeconds * 1000, e -> dispose());
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
        setVisible(true);
    }
}
