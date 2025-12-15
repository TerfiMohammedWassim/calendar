package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.controller.JsonManager;
import com.agenda.modele.Utilisateur;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Interface de connexion moderne et epuree pour Medisyns
 */
public class ConnexionFrame extends JFrame {
    
    private final AgendaController controller;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginButton;
    
    // Couleurs du theme
    private static final Color PRIMARY_COLOR = new Color(102, 51, 153);
    private static final Color PRIMARY_LIGHT = new Color(147, 112, 219);
    private static final Color BACKGROUND = new Color(250, 248, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 70);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    
    public ConnexionFrame(AgendaController controller) {
        this.controller = controller;
        
        setTitle("Medisyns - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        // Logo et Titre
        JLabel logoLabel = new JLabel("\u2665");
        logoLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 50));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("Medisyns");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Agenda Collaboratif");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        
        // Champ Email
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLabel.setForeground(TEXT_COLOR);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        emailField = new JTextField(20);
        styleTextField(emailField);
        
        mainPanel.add(emailLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Champ Mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        
        mainPanel.add(passwordLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Message d'erreur/succes
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Bouton Connexion
        loginButton = new JButton("Se connecter");
        styleButton(loginButton);
        loginButton.addActionListener(e -> handleLogin());
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Lien Inscription
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerPanel.setBackground(BACKGROUND);
        
        JLabel registerText = new JLabel("Pas encore de compte ?");
        registerText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerText.setForeground(TEXT_COLOR);
        
        JButton registerLink = new JButton("S'inscrire");
        styleLinkButton(registerLink);
        registerLink.addActionListener(e -> openInscription());
        
        registerPanel.add(registerText);
        registerPanel.add(registerLink);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(registerPanel);
        
        mainPanel.add(Box.createVerticalStrut(25));
        
        // Info comptes de test
        JPanel infoPanel = createInfoPanel();
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoPanel);
        
        // Raccourci Enter
        getRootPane().setDefaultButton(loginButton);
        
        setContentPane(mainPanel);
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 210), 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 210), 1, true),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setPreferredSize(new Dimension(300, 50));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_LIGHT);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });
    }
    
    private void styleLinkButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(PRIMARY_LIGHT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(PRIMARY_COLOR);
            }
        });
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 243, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 215, 230), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        panel.setMaximumSize(new Dimension(320, 100));
        
        JLabel infoTitle = new JLabel("Comptes de test :");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        infoTitle.setForeground(PRIMARY_COLOR);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel info1 = new JLabel("Admin: admin@medisyns.com / admin123");
        info1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info1.setForeground(TEXT_COLOR);
        info1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel info2 = new JLabel("User: patient@medisyns.com / patient123");
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info2.setForeground(TEXT_COLOR);
        info2.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(infoTitle);
        panel.add(Box.createVerticalStrut(5));
        panel.add(info1);
        panel.add(Box.createVerticalStrut(3));
        panel.add(info2);
        
        return panel;
    }
    
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        
        // Validation
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe");
            return;
        }
        
        // Desactiver le bouton
        loginButton.setEnabled(false);
        loginButton.setText("Connexion...");
        messageLabel.setText(" ");
        
        // Authentification via users.json
        JsonManager.UserJson userJson = JsonManager.authentifier(email, password);
        
        if (userJson != null) {
            // Succes
            showSuccess("Bienvenue " + userJson.nom + " !");
            
            String role = convertRole(userJson.role);
            System.out.println("DEBUG ConnexionFrame - userJson.role: " + userJson.role);
            System.out.println("DEBUG ConnexionFrame - role converti: " + role);
            
            Utilisateur utilisateur = new Utilisateur(
                String.valueOf(userJson.id),
                userJson.nom,
                role,
                userJson.email
            );
            
            System.out.println("DEBUG ConnexionFrame - utilisateur créé: " + utilisateur.getNomComplet());
            System.out.println("DEBUG ConnexionFrame - utilisateur role: " + utilisateur.getRole());
            System.out.println("DEBUG ConnexionFrame - utilisateur getRoleDisplay: " + utilisateur.getRoleDisplay());
            System.out.println("DEBUG ConnexionFrame - estAdministrateur: " + utilisateur.estAdministrateur());
            
            controller.setUtilisateurCourant(utilisateur);
            
            System.out.println("DEBUG ConnexionFrame - controller.getUtilisateurCourant: " + controller.getUtilisateurCourant());
            
            Timer timer = new Timer(800, e -> {
                System.out.println("DEBUG Timer - avant MainFrame, controller user: " + controller.getUtilisateurCourant());
                dispose();
                MainFrame mainFrame = new MainFrame(controller);
                mainFrame.setVisible(true);
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            // Echec
            showError("Email ou mot de passe incorrect");
            loginButton.setEnabled(true);
            loginButton.setText("Se connecter");
            passwordField.setText("");
        }
    }
    
    private String convertRole(String jsonRole) {
        if (jsonRole == null) return "UTILISATEUR";
        switch (jsonRole.toLowerCase()) {
            case "admin": return "ADMIN";
            case "medecin": return "MEDECIN";
            case "infirmiere": return "INFIRMIERE";
            case "patient": return "PATIENT";
            default: return "UTILISATEUR";
        }
    }
    
    private void showError(String message) {
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setText(message);
    }
    
    private void showSuccess(String message) {
        messageLabel.setForeground(SUCCESS_COLOR);
        messageLabel.setText(message);
    }
    
    private void openInscription() {
        InscriptionFrame inscriptionFrame = new InscriptionFrame(controller, this);
        inscriptionFrame.setVisible(true);
        setVisible(false);
    }
    
    public void showFrame() {
        emailField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
        setVisible(true);
    }
}
