package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.controller.JsonManager;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Interface d'inscription moderne pour Medisyns
 */
public class InscriptionFrame extends JFrame {
    
    private final AgendaController controller;
    private final ConnexionFrame parentFrame;
    
    private JTextField nomField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JLabel messageLabel;
    private JButton registerButton;
    
    // Couleurs du theme
    private static final Color PRIMARY_COLOR = new Color(102, 51, 153);
    private static final Color PRIMARY_LIGHT = new Color(147, 112, 219);
    private static final Color BACKGROUND = new Color(250, 248, 255);
    private static final Color TEXT_COLOR = new Color(50, 50, 70);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    
    public InscriptionFrame(AgendaController controller, ConnexionFrame parent) {
        this.controller = controller;
        this.parentFrame = parent;
        
        setTitle("Medisyns - Inscription");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI();
        
        // Retour a connexion si on ferme
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.showFrame();
            }
        });
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        // Titre
        JLabel titleLabel = new JLabel("Creer un compte");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Rejoignez Medisyns");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        
        // Champ Nom complet
        mainPanel.add(createLabel("Nom complet"));
        mainPanel.add(Box.createVerticalStrut(5));
        nomField = new JTextField(20);
        styleTextField(nomField);
        mainPanel.add(nomField);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Champ Email
        mainPanel.add(createLabel("Email"));
        mainPanel.add(Box.createVerticalStrut(5));
        emailField = new JTextField(20);
        styleTextField(emailField);
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Champ Mot de passe
        mainPanel.add(createLabel("Mot de passe"));
        mainPanel.add(Box.createVerticalStrut(5));
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Confirmer mot de passe
        mainPanel.add(createLabel("Confirmer mot de passe"));
        mainPanel.add(Box.createVerticalStrut(5));
        confirmPasswordField = new JPasswordField(20);
        styleTextField(confirmPasswordField);
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Role
        mainPanel.add(createLabel("Type de compte"));
        mainPanel.add(Box.createVerticalStrut(5));
        String[] roles = {"Utilisateur", "Administrateur"};
        roleCombo = new JComboBox<>(roles);
        styleComboBox(roleCombo);
        mainPanel.add(roleCombo);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Message
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Bouton Inscription
        registerButton = new JButton("S'inscrire");
        styleButton(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
        mainPanel.add(registerButton);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Lien retour
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        loginPanel.setBackground(BACKGROUND);
        
        JLabel loginText = new JLabel("Deja un compte ?");
        loginText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginText.setForeground(TEXT_COLOR);
        
        JButton loginLink = new JButton("Se connecter");
        styleLinkButton(loginLink);
        loginLink.addActionListener(e -> retourConnexion());
        
        loginPanel.add(loginText);
        loginPanel.add(loginLink);
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(loginPanel);
        
        getRootPane().setDefaultButton(registerButton);
        setContentPane(mainPanel);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 210), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 210), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setPreferredSize(new Dimension(300, 40));
        combo.setBackground(Color.WHITE);
        combo.setBorder(new LineBorder(new Color(200, 200, 210), 1, true));
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(300, 45));
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
    
    private void handleRegistration() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        String confirmPassword = String.valueOf(confirmPasswordField.getPassword());
        String roleSelection = (String) roleCombo.getSelectedItem();
        
        // Validation
        if (nom.isEmpty()) {
            showError("Veuillez entrer votre nom");
            return;
        }
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Format d'email invalide");
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer un mot de passe");
            return;
        }
        if (password.length() < 4) {
            showError("Mot de passe trop court (min 4 caracteres)");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }
        
        // Desactiver le bouton
        registerButton.setEnabled(false);
        registerButton.setText("Inscription...");
        
        // Role pour JSON
        String roleJson = roleSelection.equals("Administrateur") ? "admin" : "user";
        
        // Sauvegarder dans users.json
        boolean success = JsonManager.ajouterUtilisateur(nom, email, password, roleJson);
        
        if (success) {
            showSuccess("Compte cree avec succes !");
            
            Timer timer = new Timer(1500, e -> retourConnexion());
            timer.setRepeats(false);
            timer.start();
        } else {
            showError("Cet email est deja utilise");
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
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
    
    private void retourConnexion() {
        dispose();
        parentFrame.showFrame();
    }
}
