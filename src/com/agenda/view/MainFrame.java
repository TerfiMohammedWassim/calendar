package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {

    private final AgendaController controller;
    private final JTabbedPane tabbedPane;
    private HebdoPanel hebdoPanel;
    private ListePanel listePanel;
    private MensuelPanel mensuelPanel;
    private ProfilPanel profilPanel;
    private NotificationsPanel notificationsPanel;
    private JLabel headerUserInfo;
    private JLabel statusUserLabel;
    private static MainFrame instance;
    private static AgendaController sharedController;

    /**
     * Constructeur par défaut - crée un nouveau contrôleur
     */
    public MainFrame() {
        this(sharedController != null ? sharedController : new AgendaController());
    }
    
    /**
     * Constructeur avec contrôleur existant (utilisé après la connexion)
     */
    public MainFrame(AgendaController controller) {
        this.controller = controller;
        sharedController = controller;
        this.tabbedPane = new JTabbedPane();
        instance = this;
        
        System.out.println("DEBUG MainFrame constructor - controller reçu: " + controller);
        System.out.println("DEBUG MainFrame constructor - utilisateur: " + controller.getUtilisateurCourant());
        if (controller.getUtilisateurCourant() != null) {
            System.out.println("DEBUG MainFrame constructor - nom: " + controller.getUtilisateurCourant().getNomComplet());
            System.out.println("DEBUG MainFrame constructor - role: " + controller.getUtilisateurCourant().getRole());
        }
        
        initializeFrame();
        setupUI();
        setupGlobalShortcuts();
        setupWindowListener();
        
        // Enregistrer le listener de rafraîchissement global
        controller.addRefreshListener(this::onGlobalRefresh);
    }
    
    /**
     * Obtient l'instance singleton de MainFrame
     */
    public static MainFrame getInstance() {
        return instance;
    }
    
    /**
     * Appelé lors d'un rafraîchissement global
     */
    private void onGlobalRefresh() {
        SwingUtilities.invokeLater(() -> {
            refreshUserInfo();
            refreshAllTabs();
            updateNotificationBadge();
        });
    }
    
    /**
     * Met à jour les informations utilisateur affichées partout
     */
    public void refreshUserInfo() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null) {
            String roleIcon = "👤";
            if (user.estAdministrateur()) roleIcon = "👑";
            else if (user.estMedecin()) roleIcon = "👨‍⚕️";
            else if (user.estInfirmier()) roleIcon = "👩‍⚕️";
            
            if (headerUserInfo != null) {
                headerUserInfo.setText("<html><span style='font-size:12px;'>" + roleIcon + "</span> <b>" + user.getNomComplet() + "</b> <span style='color:#8B5CF6;'>•</span> " + user.getRoleDisplay() + "</html>");
            }
            if (statusUserLabel != null) {
                statusUserLabel.setText(user.getRoleDisplay());
            }
            // Mettre à jour le titre de la fenêtre
            setTitle("💜 Medisyns - " + user.getNomComplet() + " (" + user.getRoleDisplay() + ")");
        }
    }

    private void initializeFrame() {
        setTitle("💜 Medisyns - Agenda Collaboratif Médical");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
    }

    private void setupUI() {
        // Utiliser l'utilisateur connecté (ne pas créer de démo si connecté)
        Utilisateur utilisateurCourant = controller.getUtilisateurCourant();
        if (utilisateurCourant == null) {
            // Créer un utilisateur démo seulement si pas connecté
            utilisateurCourant = new Utilisateur("user", "Utilisateur Simple", "UTILISATEUR", "user@medisyns.com");
            controller.setUtilisateurCourant(utilisateurCourant);
        }
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(250, 245, 255));
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        setupMainPanels();
        add(createStatusBar(), BorderLayout.SOUTH);
        setJMenuBar(createMenuBar());
        
        // Appliquer les restrictions selon le rôle
        applyUserRestrictions();
        
        // Mettre à jour les infos utilisateur après création de l'UI
        refreshUserInfo();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(139, 92, 246), getWidth(), 0, new Color(109, 40, 217));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        
        // Titre avec style moderne
        JLabel titleLabel = new JLabel("💜 Medisyns", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        // Panneau utilisateur avec bouton Ajouter
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);
        
        // Bouton Ajouter un événement
        JButton addButton = new JButton("➕ Nouvel événement");
        styleAddButton(addButton);
        addButton.addActionListener(e -> createNewEvent());
        
        // Affichage de l'utilisateur connecté avec un style moderne
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.X_AXIS));
        userInfoPanel.setBackground(new Color(255, 255, 255, 40));
        userInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        
        headerUserInfo = new JLabel();
        if (controller.getUtilisateurCourant() != null) {
            Utilisateur user = controller.getUtilisateurCourant();
            String roleIcon = "👤";
            if (user.estAdministrateur()) roleIcon = "👑";
            else if (user.estMedecin()) roleIcon = "👨‍⚕️";
            else if (user.estInfirmier()) roleIcon = "👩‍⚕️";
            
            headerUserInfo.setText("<html><span style='font-size:12px;'>" + roleIcon + "</span> <b>" + user.getNomComplet() + "</b> <span style='color:#E0D4FF;'>•</span> " + user.getRoleDisplay() + "</html>");
        }
        headerUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        headerUserInfo.setForeground(Color.WHITE);
        
        userInfoPanel.add(headerUserInfo);
        
        userPanel.add(addButton);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(userInfoPanel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private void styleAddButton(JButton button) {
        Utilisateur user = controller.getUtilisateurCourant();
        boolean canCreate = user != null && user.peutCreerEvenements();
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(canCreate ? new Color(236, 72, 153) : new Color(150, 150, 150));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(canCreate ? new Color(219, 39, 119) : new Color(120, 120, 120), 2),
            BorderFactory.createEmptyBorder(10, 22, 10, 22)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(canCreate ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        button.setEnabled(canCreate);
        button.setOpaque(true);
        button.setToolTipText(canCreate ? 
            "Créer un nouvel événement (CTRL+N)" : 
            "Action réservée aux administrateurs et médecins");
        
        if (canCreate) {
            // Effet hover seulement si autorisé
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(219, 39, 119));
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(190, 24, 93), 2),
                        BorderFactory.createEmptyBorder(10, 22, 10, 22)
                    ));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(236, 72, 153));
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(219, 39, 119), 2),
                        BorderFactory.createEmptyBorder(10, 22, 10, 22)
                    ));
                }
            });
        }
    }
    
    private void styleProfileButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(140, 80, 180));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 50, 140), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(160, 100, 200));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(120, 70, 160), 2),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(140, 80, 180));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 50, 140), 1),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
    }

    private void setupMainPanels() {
        this.hebdoPanel = new HebdoPanel(controller, this);
        this.listePanel = new ListePanel(controller);
        this.mensuelPanel = new MensuelPanel(controller, this);
        this.profilPanel = new ProfilPanel(controller);
        this.notificationsPanel = new NotificationsPanel(controller);
        
        // Modern tabbed pane styling
        tabbedPane.setBackground(new Color(250, 245, 255));
        tabbedPane.setForeground(new Color(80, 50, 120));
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        
        // Custom UI for tabs
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                lightHighlight = new Color(139, 92, 246);
                shadow = new Color(200, 180, 220);
                darkShadow = new Color(139, 92, 246);
                focus = new Color(139, 92, 246);
            }
            
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    GradientPaint gp = new GradientPaint(x, y, new Color(139, 92, 246), x, y + h, new Color(109, 40, 217));
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 10, 10);
                } else {
                    g2d.setColor(new Color(245, 240, 255));
                    g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 10, 10);
                }
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                // No border
            }
            
            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // No focus indicator
            }
            
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                if (isSelected) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(new Color(80, 50, 120));
                }
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
        });
        
        tabbedPane.addTab("📅 Hebdomadaire", null, hebdoPanel, "Vue calendrier hebdomadaire");
        tabbedPane.addTab("📆 Mensuelle", null, mensuelPanel, "Vue calendrier mensuel");
        tabbedPane.addTab("📋 Liste", null, listePanel, "Liste des événements");
        tabbedPane.addTab("👤 Profil", null, profilPanel, "Mon profil");
        
        // Onglet Notifications avec badge
        int notifCount = notificationsPanel.getNombreNotificationsNonLues();
        String notifTabTitle = notifCount > 0 ? "🔔 (" + notifCount + ")" : "🔔 Notifications";
        tabbedPane.addTab(notifTabTitle, null, notificationsPanel, "Notifications");
        
        add(tabbedPane, BorderLayout.CENTER);
        
        tabbedPane.addChangeListener(e -> refreshCurrentTab());
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(230, 220, 240));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 100, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JLabel statusLabel = new JLabel("💜 Medisyns - Prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(80, 50, 120));
        
        JLabel eventCountLabel = new JLabel("Événements: " + controller.getEvenements().size());
        eventCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventCountLabel.setForeground(new Color(80, 50, 120));
        
        // Afficher l'utilisateur connecté dans la barre de statut
        statusUserLabel = new JLabel();
        if (controller.getUtilisateurCourant() != null) {
            statusUserLabel.setText(controller.getUtilisateurCourant().getRoleDisplay());
        }
        statusUserLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusUserLabel.setForeground(new Color(100, 65, 150));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(statusUserLabel, BorderLayout.CENTER);
        statusPanel.add(eventCountLabel, BorderLayout.EAST);
        
        return statusPanel;
    }

    private void setupGlobalShortcuts() {
        // Raccourci global CTRL+N pour nouvel événement
        KeyStroke ctrlN = KeyStroke.getKeyStroke("control N");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlN, "nouvelEvenement");
        getRootPane().getActionMap().put("nouvelEvenement", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewEvent();
            }
        });

        // Raccourci global F5 pour rafraîchir
        KeyStroke f5 = KeyStroke.getKeyStroke("F5");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "rafraichir");
        getRootPane().getActionMap().put("rafraichir", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAllTabs();
                showQuickNotification("🔄 Toutes les vues ont été rafraîchies");
            }
        });

        // Raccourci global CTRL+Q pour quitter
        KeyStroke ctrlQ = KeyStroke.getKeyStroke("control Q");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlQ, "quitter");
        getRootPane().getActionMap().put("quitter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutdownAndExit();
            }
        });

        // Raccourci global CTRL+1,2,3,4 pour changer d'onglet
        KeyStroke ctrl1 = KeyStroke.getKeyStroke("control 1");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrl1, "onglet1");
        getRootPane().getActionMap().put("onglet1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(0);
            }
        });

        KeyStroke ctrl2 = KeyStroke.getKeyStroke("control 2");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrl2, "onglet2");
        getRootPane().getActionMap().put("onglet2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(1);
            }
        });

        KeyStroke ctrl3 = KeyStroke.getKeyStroke("control 3");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrl3, "onglet3");
        getRootPane().getActionMap().put("onglet3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(2);
            }
        });

        KeyStroke ctrl4 = KeyStroke.getKeyStroke("control 4");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrl4, "onglet4");
        getRootPane().getActionMap().put("onglet4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(3);
            }
        });

        // Raccourci global CTRL+F pour recherche
        KeyStroke ctrlF = KeyStroke.getKeyStroke("control F");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlF, "recherche");
        getRootPane().getActionMap().put("recherche", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(2);
                if (listePanel != null) {
                    try {
                        java.lang.reflect.Field searchField = listePanel.getClass().getDeclaredField("searchField");
                        searchField.setAccessible(true);
                        JTextField field = (JTextField) searchField.get(listePanel);
                        field.requestFocus();
                        field.selectAll();
                    } catch (Exception ex) {}
                }
            }
        });
        
        // Raccourci global CTRL+P pour profil
        KeyStroke ctrlP = KeyStroke.getKeyStroke("control P");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlP, "profil");
        getRootPane().getActionMap().put("profil", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(3);
                showQuickNotification("👤 Profil utilisateur");
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(240, 230, 250));
        menuBar.setBorder(BorderFactory.createLineBorder(new Color(180, 100, 200)));
        
        JMenu fileMenu = new JMenu("📁 Fichier");
        styleMenu(fileMenu);
        
        JMenuItem newEventItem = new JMenuItem("➕ Nouvel Événement");
        newEventItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        JMenuItem refreshItem = new JMenuItem("🔄 Actualiser");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        JMenuItem statsItem = new JMenuItem("📊 Statistiques");
        JMenuItem exitItem = new JMenuItem("🚪 Quitter");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        
        newEventItem.addActionListener(e -> createNewEvent());
        refreshItem.addActionListener(e -> refreshAllTabs());
        statsItem.addActionListener(e -> showStatistics());
        exitItem.addActionListener(e -> shutdownAndExit());
        
        fileMenu.add(newEventItem);
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(statsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenu viewMenu = new JMenu("👁️ Affichage");
        styleMenu(viewMenu);
        
        JMenuItem weekViewItem = new JMenuItem("📅 Vue Hebdomadaire");
        weekViewItem.setAccelerator(KeyStroke.getKeyStroke("ctrl 1"));
        JMenuItem monthViewItem = new JMenuItem("📆 Vue Mensuelle");
        monthViewItem.setAccelerator(KeyStroke.getKeyStroke("ctrl 2"));
        JMenuItem listViewItem = new JMenuItem("📋 Vue Liste");
        listViewItem.setAccelerator(KeyStroke.getKeyStroke("ctrl 3"));
        JMenuItem profileViewItem = new JMenuItem("👤 Mon Profil");
        profileViewItem.setAccelerator(KeyStroke.getKeyStroke("ctrl 4"));
        JMenuItem goToMonthItem = new JMenuItem("📅 Aller à un mois spécifique");
        
        weekViewItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(0);
            showQuickNotification("📅 Vue hebdomadaire activée");
        });
        monthViewItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1);
            showQuickNotification("📆 Vue mensuelle activée");
        });
        listViewItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(2);
            showQuickNotification("📋 Vue liste activée");
        });
        profileViewItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(3);
            showQuickNotification("👤 Profil utilisateur activé");
        });
        goToMonthItem.addActionListener(e -> showMonthSelectorDialog());
        
        viewMenu.add(weekViewItem);
        viewMenu.add(monthViewItem);
        viewMenu.add(listViewItem);
        viewMenu.add(profileViewItem);
        viewMenu.addSeparator();
        viewMenu.add(goToMonthItem);
        
        JMenu helpMenu = new JMenu("❓ Aide");
        styleMenu(helpMenu);
        
        JMenuItem aboutItem = new JMenuItem("💜 À propos de Medisyns");
        JMenuItem helpItem = new JMenuItem("📖 Guide d'utilisation");
        JMenuItem shortcutsItem = new JMenuItem("⌨️ Raccourcis clavier");
        
        aboutItem.addActionListener(e -> showAboutDialog());
        helpItem.addActionListener(e -> showHelpDialog());
        shortcutsItem.addActionListener(e -> showShortcutsDialog());
        
        helpMenu.add(aboutItem);
        helpMenu.add(helpItem);
        helpMenu.addSeparator();
        helpMenu.add(shortcutsItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        // Appliquer les restrictions sur les menus
        applyMenuRestrictions(fileMenu, viewMenu, helpMenu);
        
        return menuBar;
    }

    private void styleMenu(JMenu menu) {
        menu.setForeground(new Color(80, 50, 120));
        menu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menu.setBackground(new Color(240, 230, 250));
    }

    private void applyMenuRestrictions(JMenu fileMenu, JMenu viewMenu, JMenu helpMenu) {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user == null) return;
        
        // Désactiver les items selon les permissions
        for (Component comp : fileMenu.getMenuComponents()) {
            if (comp instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) comp;
                String text = item.getText();
                
                if (text.contains("Nouvel Événement")) {
                    item.setEnabled(user.peutCreerEvenements());
                } else if (text.contains("Statistiques")) {
                    item.setEnabled(user.peutVoirStatistiquesCompletes());
                }
            }
        }
    }

    private void createNewEvent() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutCreerEvenements()) {
            showPermissionDeniedMessage("créer un événement");
            return;
        }
        
        EventDialog dialog = new EventDialog(this, controller, java.time.LocalDate.now());
        dialog.setVisible(true);
        refreshAllTabs();
        updateNotificationBadge();
        showQuickNotification("✅ Nouvel événement créé");
    }

    private void refreshAllTabs() {
        hebdoPanel.refreshCalendar();
        listePanel.refreshTable();
        mensuelPanel.refreshCalendar();
        if (profilPanel != null) {
            profilPanel.setUtilisateurCourant(controller.getUtilisateurCourant());
        }
        if (notificationsPanel != null) {
            notificationsPanel.refreshNotifications();
        }
        updateEventCount();
        updateNotificationBadge();
    }
    
    /**
     * Rafraîchit toutes les données de l'application (données + UI)
     * À appeler après une mise à jour importante
     */
    public void refreshAllData() {
        SwingUtilities.invokeLater(() -> {
            refreshUserInfo();
            refreshAllTabs();
            updateNotificationBadge();
        });
    }

    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            hebdoPanel.refreshCalendar();
        } else if (selectedIndex == 1) {
            mensuelPanel.refreshCalendar();
        } else if (selectedIndex == 2) {
            listePanel.refreshTable();
        } else if (selectedIndex == 3 && profilPanel != null) {
            profilPanel.setUtilisateurCourant(controller.getUtilisateurCourant());
        } else if (selectedIndex == 4 && notificationsPanel != null) {
            notificationsPanel.refreshNotifications();
            updateNotificationBadge();
        }
        updateEventCount();
    }
    
    /**
     * Met à jour le badge de notifications dans l'onglet
     */
    private void updateNotificationBadge() {
        if (notificationsPanel != null) {
            int notifCount = notificationsPanel.getNombreNotificationsNonLues();
            String notifTabTitle = notifCount > 0 ? "🔔 Notifications (" + notifCount + ")" : "🔔 Notifications";
            tabbedPane.setTitleAt(4, notifTabTitle);
        }
    }

    private void updateEventCount() {
        Component[] components = ((JPanel)getContentPane().getComponent(2)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText().contains("Événements:")) {
                    label.setText("Événements: " + controller.getEvenements().size());
                    break;
                }
            }
        }
    }

    private void showStatistics() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user != null && !user.peutVoirStatistiquesCompletes()) {
            showPermissionDeniedMessage("voir les statistiques complètes");
            return;
        }
        
        String stats = controller.getStatistiques();
        
        String statsHTML = "<html>" +
            "<div style='background: linear-gradient(135deg, #E6D7FF, #F0E8FF); padding: 20px; border-radius: 12px; border: 2px solid #B464C8; width: 350px;'>" +
            "<h3 style='margin: 0 0 15px 0; text-align: center; color: #6B46C1;'>📊 Statistiques Medisyns</h3>" +
            "<div style='background: #FFFFFF; padding: 15px; border-radius: 8px; border: 1px solid #E2E8F0; font-family: monospace; font-size: 12px; color: #4A5568;'>" +
            stats.replace("\n", "<br>") +
            "</div>" +
            "</div>" +
            "</html>";
        
        JLabel statsLabel = new JLabel(statsHTML);
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, statsLabel, 
            "📊 Statistiques - Medisyns", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showQuickNotification(String message) {
        System.out.println("💡 " + message);
    }

    private void showShortcutsDialog() {
        String shortcutsText = 
            "<html>" +
            "<div style='padding: 15px; max-width: 500px;'>" +
            "<h2 style='color: #6B46C1; text-align: center;'>⌨️ Raccourcis Clavier</h2>" +
            "<div style='background: #F8F5FF; padding: 15px; border-radius: 8px; margin: 10px 0;'>" +
            "<h3 style='color: #805078; margin-top: 0;'>📁 Général</h3>" +
            "<table style='width: 100%; font-size: 13px;'>" +
            "<tr><td><b>CTRL + N</b></td><td>Nouvel événement</td></tr>" +
            "<tr><td><b>F5</b></td><td>Rafraîchir toutes les vues</td></tr>" +
            "<tr><td><b>CTRL + Q</b></td><td>Quitter l'application</td></tr>" +
            "<tr><td><b>CTRL + F</b></td><td>Rechercher</td></tr>" +
            "<tr><td><b>CTRL + P</b></td><td>Accéder au profil</td></tr>" +
            "<tr><td><b>Bouton Ajouter</b></td><td>Créer un événement</td></tr>" +
            "</table>" +
            "</div>" +
            "<div style='background: #F8F5FF; padding: 15px; border-radius: 8px; margin: 10px 0;'>" +
            "<h3 style='color: #805078; margin-top: 0;'>👁️ Navigation</h3>" +
            "<table style='width: 100%; font-size: 13px;'>" +
            "<tr><td><b>CTRL + 1</b></td><td>Vue Hebdomadaire</td></tr>" +
            "<tr><td><b>CTRL + 2</b></td><td>Vue Mensuelle</td></tr>" +
            "<tr><td><b>CTRL + 3</b></td><td>Vue Liste</td></tr>" +
            "<tr><td><b>CTRL + 4</b></td><td>Mon Profil</td></tr>" +
            "</table>" +
            "</div>" +
            "<div style='background: #F8F5FF; padding: 15px; border-radius: 8px; margin: 10px 0;'>" +
            "<h3 style='color: #805078; margin-top: 0;'>📋 Liste des événements</h3>" +
            "<table style='width: 100%; font-size: 13px;'>" +
            "<tr><td><b>SUPPR</b></td><td>Supprimer l'événement sélectionné</td></tr>" +
            "<tr><td><b>ESC</b></td><td>Effacer la recherche</td></tr>" +
            "<tr><td><b>Double-clic</b></td><td>Voir les détails</td></tr>" +
            "</table>" +
            "</div>" +
            "<p style='text-align: center; color: #805078; font-style: italic; margin-top: 15px;'>" +
            "💜 Medisyns - Optimisez votre productivité !" +
            "</p>" +
            "</div>" +
            "</html>";
        
        JLabel shortcutsLabel = new JLabel(shortcutsText);
        shortcutsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, shortcutsLabel, 
            "⌨️ Raccourcis Clavier - Medisyns", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String aboutText = 
            "<html>" +
            "<div style='text-align: center; padding: 20px;'>" +
            "<h1 style='color: #6B46C1;'>💜 Medisyns</h1>" +
            "<h3 style='color: #805078;'>Agenda Collaboratif Médical</h3>" +
            "<p><b>Version:</b> 4.0.0</p>" +
            "<p><b>Nouveauté:</b> Système de profils avec permissions</p>" +
            "<p><b>Profils disponibles:</b></p>" +
            "<ul style='text-align: left;'>" +
            "<li>👑 <b>Administrateur:</b> Toutes les permissions</li>" +
            "<li>👨‍⚕️ <b>Médecin:</b> Peut créer des événements</li>" +
            "<li>👤 <b>Utilisateur simple:</b> Consultation et partage seulement</li>" +
            "</ul>" +
            "<p style='margin-top: 20px; color: #805078;'>" +
            "Optimisé pour les équipes médicales" +
            "</p>" +
            "</div>" +
            "</html>";
        
        JLabel aboutLabel = new JLabel(aboutText);
        aboutLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, aboutLabel, 
            "💜 À propos de Medisyns", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog() {
        String helpText = 
            "<html>" +
            "<div style='padding: 15px; max-width: 500px;'>" +
            "<h2 style='color: #6B46C1;'>📖 Guide d'utilisation Medisyns</h2>" +
            "<h3 style='color: #805078;'>Fonctionnalités principales:</h3>" +
            "<p><b>📅 Vue Hebdomadaire:</b></p>" +
            "<ul>" +
            "<li>Double-clic sur une cellule pour créer un événement</li>" +
            "<li>Drag & Drop pour déplacer les événements</li>" +
            "<li>Clic droit pour les options contextuelles</li>" +
            "</ul>" +
            "<p><b>📆 Vue Mensuelle:</b></p>" +
            "<ul>" +
            "<li>Vue d'ensemble du mois</li>" +
            "<li>Navigation facile entre les mois</li>" +
            "<li>Affichage des événements par jour</li>" +
            "</ul>" +
            "<p><b>📋 Vue Liste:</b></p>" +
            "<ul>" +
            "<li>Recherche en temps réel</li>" +
            "<li>Tri par colonnes</li>" +
            "<li>Menu contextuel avec partage</li>" +
            "<li>Raccourcis clavier (SUPPR, CTRL+N)</li>" +
            "</ul>" +
            "<p><b>👤 Mon Profil:</b></p>" +
            "<ul>" +
            "<li>Visualisation de vos informations personnelles</li>" +
            "<li>Liste de tous vos événements créés</li>" +
            "<li>Statistiques d'utilisation</li>" +
            "<li>Export de vos données</li>" +
            "<li>Modification des informations de contact</li>" +
            "</ul>" +
            "<p><b>🔐 Système de permissions:</b></p>" +
            "<ul>" +
            "<li><b>👑 Administrateur:</b> Toutes les permissions</li>" +
            "<li><b>👨‍⚕️ Médecin/Infirmière:</b> Peut créer des événements</li>" +
            "<li><b>👤 Utilisateur simple:</b> Consultation et partage seulement</li>" +
            "</ul>" +
            "<p style='margin-top: 20px; color: #805078; font-style: italic;'>" +
            "💜 Conçu pour simplifier la gestion des rendez-vous médicaux" +
            "</p>" +
            "</div>" +
            "</html>";
        
        JLabel helpLabel = new JLabel(helpText);
        helpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JOptionPane.showMessageDialog(this, helpLabel, 
            "📖 Aide - Medisyns", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownAndExit();
            }
        });
    }

    private void shutdownAndExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><div style='width: 300px;'>" +
            "Voulez-vous vraiment quitter Medisyns ?<br>" +
            "<span style='color: #6B46C1; font-size: 12px;'>" +
            "Tous les événements sont sauvegardés automatiquement." +
            "</span>" +
            "</div></html>",
            "💜 Quitter Medisyns",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            controller.shutdown();
            System.exit(0);
        }
    }

    // AJOUT: Méthodes pour la gestion des permissions
    private void applyUserRestrictions() {
        Utilisateur user = controller.getUtilisateurCourant();
        if (user == null) return;
        
        // Mettre à jour le titre avec le rôle
        updateTitleWithRole(user);
    }

    private void updateTitleWithRole(Utilisateur user) {
        String roleText = user.getRoleDisplay();
        setTitle("💜 Medisyns - " + user.getNomComplet() + " (" + roleText + ")");
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

    // AJOUT: Méthode pour le sélecteur de mois
    private void showMonthSelectorDialog() {
        JDialog dialog = new JDialog(this, "Sélectionner un mois", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(250, 245, 255));
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(250, 245, 255));
        
        String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                          "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        JComboBox<Integer> yearCombo = new JComboBox<>();
        
        int currentYear = java.time.Year.now().getValue();
        for (int year = currentYear - 5; year <= currentYear + 5; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(currentYear);
        
        monthCombo.setSelectedIndex(java.time.LocalDate.now().getMonthValue() - 1);
        
        styliserComboBox(monthCombo);
        styliserComboBox(yearCombo);
        
        panel.add(new JLabel("Mois:"));
        panel.add(monthCombo);
        panel.add(new JLabel("Année:"));
        panel.add(yearCombo);
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Annuler");
        
        okButton.addActionListener(e -> {
            int selectedMonth = monthCombo.getSelectedIndex() + 1;
            int selectedYear = (int) yearCombo.getSelectedItem();
            
            // Basculer vers la vue mensuelle
            tabbedPane.setSelectedIndex(1);
            
            // Mettre à jour le calendrier mensuel
            if (mensuelPanel != null) {
                try {
                    java.lang.reflect.Field field = mensuelPanel.getClass().getDeclaredField("currentMonth");
                    field.setAccessible(true);
                    field.set(mensuelPanel, java.time.YearMonth.of(selectedYear, selectedMonth));
                    
                    java.lang.reflect.Method method = mensuelPanel.getClass().getDeclaredMethod("refreshCalendar");
                    method.invoke(mensuelPanel);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            dialog.dispose();
            showQuickNotification("📅 Affichage du mois: " + monthCombo.getSelectedItem() + " " + selectedYear);
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(new Color(250, 245, 255));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void styliserComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(80, 50, 120));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 100, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    public AgendaController getController() {
        return controller;
    }
}