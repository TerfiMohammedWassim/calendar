package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventDialog extends JDialog {

    private JTextField titreField, descField, respField;
    private JCheckBox[] participantCheckboxes;
    private JSpinner dateSpinner;
    private JComboBox<Integer> heureCombo, minuteCombo;
    private JComboBox<Integer> notifCombo;
    private Evenement evenementOriginal;
    private JButton saveBtn;

    private final AgendaController controller;
    private String[] participantsList = {"Patient", "Parent / Accompagnant", "Assistante Samira", "Infirmière Lina",
            "Dr. Ahmed", "Dr. Salima", "Technicien Radio", "Secrétaire Karima", "Stagiaire",
            "Équipe médicale", "Visiteur"};

    // Modern color palette
    private static final Color PRIMARY = new Color(139, 92, 246);
    private static final Color PRIMARY_DARK = new Color(109, 40, 217);
    private static final Color PRIMARY_LIGHT = new Color(167, 139, 250);
    private static final Color BACKGROUND = new Color(250, 250, 255);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(30, 30, 46);
    private static final Color TEXT_SECONDARY = new Color(100, 100, 120);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color ACCENT = new Color(236, 72, 153);
    private static final Color SUCCESS = new Color(34, 197, 94);

    public EventDialog(JFrame parent, AgendaController controller, LocalDate date) {
        super(parent, "", true);
        this.controller = controller;

        setUndecorated(true);
        setSize(560, 820);
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, 560, 820, 25, 25));

        initUI(date);
    }

    private void initUI(LocalDate date) {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND, 0, getHeight(), new Color(245, 243, 255));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                g2d.setColor(new Color(139, 92, 246, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel(date);
        
        // Make content scrollable
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Style the scrollbar
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(139, 92, 246, 100);
                this.trackColor = new Color(240, 240, 250);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        addDragSupport(headerPanel);
        applyPermissions();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel iconLabel = new JLabel("📅");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String titleText = controller.getUtilisateurCourant().peutCreerEvenements() ? 
                          "Nouvel Événement" : "Consulter Événement";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Medisyns - Agenda Collaboratif");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.setForeground(TEXT_SECONDARY);
        closeBtn.setBackground(null);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(ACCENT); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(TEXT_SECONDARY); }
        });

        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createContentPanel(LocalDate date) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 25, 10, 25));

        // Title field
        JPanel titreCard = createFieldCard("📝", "Titre", true);
        titreField = getFieldFromCard(titreCard);
        content.add(titreCard);
        content.add(Box.createVerticalStrut(12));

        // Description field
        JPanel descCard = createFieldCard("📄", "Description", false);
        descField = getFieldFromCard(descCard);
        content.add(descCard);
        content.add(Box.createVerticalStrut(12));

        // Responsible field
        JPanel respCard = createFieldCard("👤", "Responsable", true);
        respField = getFieldFromCard(respCard);
        content.add(respCard);
        content.add(Box.createVerticalStrut(12));

        // Participants checkboxes
        content.add(createParticipantsCard());
        content.add(Box.createVerticalStrut(12));

        // Date and Time row
        content.add(createDateTimeCard(date));
        content.add(Box.createVerticalStrut(12));

        // Notification
        content.add(createNotificationCard());

        return content;
    }

    private JPanel createFieldCard(String icon, String label, boolean required) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(139, 92, 246, 30), 1, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ),
            new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel titleLabel = new JLabel(icon + " " + label + (required ? " *" : ""));
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(new Color(250, 248, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 190, 220), 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBackground(Color.WHITE);
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY, 2, true),
                    new EmptyBorder(12, 14, 12, 14)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBackground(new Color(250, 248, 255));
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 190, 220), 1, true),
                    new EmptyBorder(12, 14, 12, 14)
                ));
            }
        });

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(field);

        return card;
    }

    private JTextField getFieldFromCard(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JTextField) return (JTextField) c;
        }
        return null;
    }

    private JPanel createParticipantsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(139, 92, 246, 30), 1, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ),
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLabel = new JLabel("👥 Participants *");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));

        // Grid panel for checkboxes - 4 columns to fit all without scrolling
        JPanel checkPanel = new JPanel(new GridLayout(0, 3, 8, 6));
        checkPanel.setBackground(CARD_BG);
        checkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        participantCheckboxes = new JCheckBox[participantsList.length];
        
        Color[] avatarColors = {
            new Color(236, 72, 153),   // Pink
            new Color(139, 92, 246),   // Purple
            new Color(34, 197, 94),    // Green
            new Color(59, 130, 246),   // Blue
            new Color(249, 115, 22),   // Orange
            new Color(168, 85, 247),   // Violet
            new Color(20, 184, 166),   // Teal
            new Color(239, 68, 68),    // Red
            new Color(234, 179, 8),    // Yellow
            new Color(99, 102, 241),   // Indigo
            new Color(16, 185, 129)    // Emerald
        };

        for (int i = 0; i < participantsList.length; i++) {
            JPanel checkItem = createModernCheckItem(participantsList[i], avatarColors[i % avatarColors.length], i);
            checkPanel.add(checkItem);
        }

        card.add(checkPanel);

        return card;
    }

    private JPanel createModernCheckItem(String name, Color avatarColor, int index) {
        JPanel item = new JPanel(new BorderLayout(6, 0));
        item.setBackground(new Color(250, 248, 255));
        item.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 225, 245), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Custom styled checkbox
        participantCheckboxes[index] = new JCheckBox() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 18;
                int x = 0;
                int y = (getHeight() - size) / 2;
                
                if (isSelected()) {
                    g2.setColor(PRIMARY);
                    g2.fillRoundRect(x, y, size, size, 5, 5);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 4, y + 9, x + 7, y + 13);
                    g2.drawLine(x + 7, y + 13, x + 14, y + 5);
                } else {
                    g2.setColor(new Color(200, 195, 215));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(x, y, size - 1, size - 1, 5, 5);
                }
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };
        participantCheckboxes[index].setOpaque(false);
        participantCheckboxes[index].setFocusPainted(false);

        // Avatar with initial
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(avatarColor);
                g2.fillOval(0, 0, 22, 22);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String initial = name.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int tx = (22 - fm.stringWidth(initial)) / 2;
                int ty = (22 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, tx, ty);
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(22, 22);
            }
        };
        avatar.setOpaque(false);

        // Name label - truncate if too long
        String displayName = name.length() > 12 ? name.substring(0, 10) + ".." : name;
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setToolTipText(name);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(participantCheckboxes[index]);
        leftPanel.add(avatar);

        item.add(leftPanel, BorderLayout.WEST);
        item.add(nameLabel, BorderLayout.CENTER);

        // Click anywhere to toggle
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                participantCheckboxes[index].setSelected(!participantCheckboxes[index].isSelected());
                updateCheckItemStyle(item, participantCheckboxes[index].isSelected());
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!participantCheckboxes[index].isSelected()) {
                    item.setBackground(new Color(245, 240, 255));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                updateCheckItemStyle(item, participantCheckboxes[index].isSelected());
            }
        };
        item.addMouseListener(clickHandler);
        nameLabel.addMouseListener(clickHandler);
        avatar.addMouseListener(clickHandler);

        participantCheckboxes[index].addActionListener(e -> {
            updateCheckItemStyle(item, participantCheckboxes[index].isSelected());
        });

        return item;
    }

    private void updateCheckItemStyle(JPanel item, boolean selected) {
        if (selected) {
            item.setBackground(new Color(237, 233, 254));
            item.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_LIGHT, 1, true),
                new EmptyBorder(6, 8, 6, 8)
            ));
        } else {
            item.setBackground(new Color(250, 248, 255));
            item.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 225, 245), 1, true),
                new EmptyBorder(6, 8, 6, 8)
            ));
        }
        item.repaint();
    }

    private JPanel createDateTimeCard(LocalDate date) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(139, 92, 246, 30), 1, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ),
            new EmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Title row
        JLabel titleLabel = new JLabel("📅 Date et Heure *");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));

        // Date and Time row
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        rowPanel.setBackground(CARD_BG);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date spinner
        JPanel dateBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateBox.setBackground(CARD_BG);
        JLabel dateIcon = new JLabel("📆");
        dateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setValue(java.sql.Date.valueOf(date));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(130, 38));
        JComponent editor = dateSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(new Color(250, 248, 255));
            tf.setBorder(new EmptyBorder(8, 10, 8, 10));
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        dateBox.add(dateIcon);
        dateBox.add(dateSpinner);

        // Time selectors
        JPanel timeBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        timeBox.setBackground(CARD_BG);
        JLabel timeIcon = new JLabel("⏰");
        timeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        heureCombo = new JComboBox<>();
        minuteCombo = new JComboBox<>();
        for (int h = 0; h < 24; h++) heureCombo.addItem(h);
        for (int m = 0; m < 60; m += 5) minuteCombo.addItem(m);
        heureCombo.setSelectedItem(8);
        minuteCombo.setSelectedItem(0);

        styleComboBox(heureCombo);
        styleComboBox(minuteCombo);

        JLabel separator = new JLabel(" : ");
        separator.setFont(new Font("Segoe UI", Font.BOLD, 16));
        separator.setForeground(PRIMARY);

        timeBox.add(timeIcon);
        timeBox.add(heureCombo);
        timeBox.add(separator);
        timeBox.add(minuteCombo);

        rowPanel.add(dateBox);
        rowPanel.add(timeBox);
        card.add(rowPanel);

        return card;
    }

    private JPanel createNotificationCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(139, 92, 246, 30), 1, true),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ),
            new EmptyBorder(16, 18, 16, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JLabel titleLabel = new JLabel("🔔 Notification avant (min) *");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));

        notifCombo = new JComboBox<>();
        notifCombo.addItem(0);
        notifCombo.addItem(5);
        notifCombo.addItem(10);
        notifCombo.addItem(15);
        notifCombo.addItem(30);
        notifCombo.addItem(60);
        notifCombo.setSelectedItem(10);
        notifCombo.setPreferredSize(new Dimension(200, 38));
        notifCombo.setMaximumSize(new Dimension(250, 38));
        notifCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleComboBox(notifCombo);

        notifCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                int minutes = (Integer) value;
                if (minutes == 0) setText("🚫 Pas de rappel");
                else if (minutes == 60) setText("⏰ 1 heure avant");
                else setText("⏰ " + minutes + " minutes avant");
                if (isSelected) {
                    setBackground(PRIMARY_LIGHT);
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        card.add(notifCombo);

        return card;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(250, 248, 255));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 190, 220), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        combo.setPreferredSize(new Dimension(75, 38));
        combo.setFocusable(false);
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 25, 25, 25));

        JButton cancelBtn = new JButton("Annuler");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setForeground(TEXT_SECONDARY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 25, 10, 25)
        ));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        cancelBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelBtn.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent e) {
                cancelBtn.setBackground(Color.WHITE);
            }
        });

        saveBtn = new JButton("💾 Sauvegarder");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(PRIMARY);
        saveBtn.setBorder(new EmptyBorder(10, 25, 10, 25));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> handleSave());
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (saveBtn.isEnabled()) saveBtn.setBackground(PRIMARY_DARK);
            }
            public void mouseExited(MouseEvent e) {
                if (saveBtn.isEnabled()) saveBtn.setBackground(PRIMARY);
            }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);

        return footer;
    }

    private void addDragSupport(JPanel panel) {
        final Point[] dragPoint = {null};
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragPoint[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { dragPoint[0] = null; }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - dragPoint[0].x, current.y - dragPoint[0].y);
                }
            }
        });
    }

    private void applyPermissions() {
        com.agenda.modele.Utilisateur user = controller.getUtilisateurCourant();
        boolean isReadOnly = false;

        if (user != null) {
            if (!user.peutCreerEvenements() && evenementOriginal == null) {
                isReadOnly = true;
            } else if (evenementOriginal != null && !user.peutModifierEvenements()) {
                isReadOnly = true;
            }
        }

        if (isReadOnly) {
            titreField.setEditable(false);
            descField.setEditable(false);
            respField.setEditable(false);
            for (JCheckBox cb : participantCheckboxes) cb.setEnabled(false);
            dateSpinner.setEnabled(false);
            heureCombo.setEnabled(false);
            minuteCombo.setEnabled(false);
            notifCombo.setEnabled(false);
            saveBtn.setEnabled(false);
            saveBtn.setText("🔒 Consultation seule");
            saveBtn.setBackground(new Color(150, 150, 150));
        }
    }

    private void handleSave() {
        String titre = titreField.getText().trim();
        String desc = descField.getText().trim();
        String resp = respField.getText().trim();

        List<String> selectedParticipants = new ArrayList<>();
        for (int i = 0; i < participantCheckboxes.length; i++) {
            if (participantCheckboxes[i].isSelected()) {
                selectedParticipants.add(participantsList[i]);
            }
        }

        if (titre.isEmpty() || resp.isEmpty() || selectedParticipants.isEmpty()) {
            showModernAlert("Champs obligatoires",
                "Veuillez remplir tous les champs obligatoires:\n• Titre\n• Responsable\n• Au moins 1 participant");
            return;
        }

        LocalDate d = new java.sql.Date(((java.util.Date) dateSpinner.getValue()).getTime()).toLocalDate();
        LocalTime time = LocalTime.of((int) heureCombo.getSelectedItem(), (int) minuteCombo.getSelectedItem());

        if (!validerEvenement(titre, resp, selectedParticipants, d, time)) return;

        int notifMinutes = (int) notifCombo.getSelectedItem();
        String createurUsername = controller.getUtilisateurCourant() != null ?
                controller.getUtilisateurCourant().getUsername() : "System";

        Evenement nouvelEv = new Evenement(titre, desc, d, time, resp, new ArrayList<>(selectedParticipants), createurUsername);
        nouvelEv.setNotificationBeforeMinutes(notifMinutes);

        if (evenementOriginal != null) {
            controller.mettreAJourEvenement(evenementOriginal, nouvelEv);
        } else {
            controller.ajouterEvenement(nouvelEv);
        }

        dispose();
    }

    private void showModernAlert(String title, String message) {
        JDialog alert = new JDialog(this, true);
        alert.setUndecorated(true);
        alert.setSize(350, 200);
        alert.setLocationRelativeTo(this);
        alert.setShape(new RoundRectangle2D.Double(0, 0, 350, 200, 15, 15));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT, 2, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setForeground(TEXT_SECONDARY);
        messageArea.setBackground(Color.WHITE);
        messageArea.setEditable(false);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okBtn = new JButton("Compris");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okBtn.setForeground(Color.WHITE);
        okBtn.setBackground(ACCENT);
        okBtn.setBorder(new EmptyBorder(8, 30, 8, 30));
        okBtn.setFocusPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.addActionListener(e -> alert.dispose());

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(messageArea);
        panel.add(Box.createVerticalStrut(15));
        panel.add(okBtn);

        alert.setContentPane(panel);
        alert.setVisible(true);
    }

    public void setEvenement(Evenement toEdit) {
        this.evenementOriginal = toEdit;

        titreField.setText(toEdit.getTitre());
        descField.setText(toEdit.getDescription());
        respField.setText(toEdit.getResponsable());

        if (toEdit.getParticipants() != null && !toEdit.getParticipants().isEmpty()) {
            List<String> toSelect = toEdit.getParticipants();
            for (int i = 0; i < participantsList.length; i++) {
                participantCheckboxes[i].setSelected(toSelect.contains(participantsList[i]));
            }
        }

        dateSpinner.setValue(java.sql.Date.valueOf(toEdit.getDate()));
        heureCombo.setSelectedItem(toEdit.getHeure().getHour());
        minuteCombo.setSelectedItem(toEdit.getHeure().getMinute());
        notifCombo.setSelectedItem(toEdit.getNotificationBeforeMinutes());

        saveBtn.setText("💾 Modifier");
        applyPermissions();
    }

    private boolean validerEvenement(String titre, String resp, List<String> participants, LocalDate date, LocalTime heure) {
        if (titre.length() < 3) {
            showModernAlert("Titre invalide", "Le titre doit contenir au moins 3 caractères.");
            return false;
        }

        LocalDateTime eventDateTime = LocalDateTime.of(date, heure);
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "⚠️ Cet événement est dans le passé.\nVoulez-vous quand même le créer ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            return confirm == JOptionPane.YES_OPTION;
        }

        if (evenementOriginal == null) {
            List<Evenement> existants = controller.getEvenementsPourDateEtHeure(date, heure);
            if (!existants.isEmpty()) {
                StringBuilder conflits = new StringBuilder("Conflit détecté:\n");
                for (Evenement ev : existants) {
                    conflits.append("• ").append(ev.getTitre()).append("\n");
                }
                conflits.append("\nContinuer quand même ?");
                int confirm = JOptionPane.showConfirmDialog(this, conflits.toString(), "Conflit d'horaire", JOptionPane.YES_NO_OPTION);
                return confirm == JOptionPane.YES_OPTION;
            }
        }

        return true;
    }
}
