package com.agenda.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Splash Screen moderne pour Medisyns
 */
public class SplashScreen extends JWindow {
    
    private static final Color PRIMARY_COLOR = new Color(102, 51, 153);
    private static final Color PRIMARY_LIGHT = new Color(147, 112, 219);
    private static final Color BACKGROUND = new Color(250, 248, 255);
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public SplashScreen() {
        setSize(450, 350);
        setLocationRelativeTo(null);
        
        // Forme arrondie
        setShape(new RoundRectangle2D.Double(0, 0, 450, 350, 25, 25));
        
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Bordure
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 30, 40));
        
        // Logo coeur
        JLabel logoLabel = new JLabel("\u2665");
        logoLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 70));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Titre
        JLabel titleLabel = new JLabel("Medisyns");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Sous-titre
        JLabel subtitleLabel = new JLabel("Agenda Collaboratif Médical");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 65, 150));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Barre de progression
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(new Color(220, 210, 230));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(300, 8));
        progressBar.setMaximumSize(new Dimension(300, 8));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Status
        statusLabel = new JLabel("Initialisation...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(120, 100, 150));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version
        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(150, 130, 170));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(versionLabel);
        
        setContentPane(mainPanel);
    }
    
    public void setProgress(int value, String status) {
        progressBar.setValue(value);
        statusLabel.setText(status);
    }
    
    /**
     * Affiche le splash screen avec animation de chargement
     */
    public void showSplash(Runnable onComplete) {
        setVisible(true);
        
        new Thread(() -> {
            try {
                String[] messages = {
                    "Initialisation...",
                    "Chargement des modules...",
                    "Configuration de l'interface...",
                    "Préparation de l'agenda...",
                    "Prêt !"
                };
                
                for (int i = 0; i <= 100; i += 2) {
                    final int progress = i;
                    final String message = messages[Math.min(i / 25, messages.length - 1)];
                    
                    SwingUtilities.invokeLater(() -> setProgress(progress, message));
                    Thread.sleep(25);
                }
                
                Thread.sleep(300);
                
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                    dispose();
                    onComplete.run();
                });
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
