package com.agenda.view;

import com.agenda.controller.AgendaController;
import com.agenda.modele.Evenement;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MensuelPanel extends JPanel {

    private final AgendaController controller;
    private final JPanel gridPanel;
    private YearMonth currentMonth;
    private final JLabel monthLabel;
    private final JFrame parentFrame; // AJOUT: Référence au parent

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MensuelPanel(AgendaController controller, JFrame parent){
        this.controller = controller;
        this.parentFrame = parent; // AJOUT: Stocker la référence
        setLayout(new BorderLayout());
        setBackground(new Color(250, 245, 255));

        currentMonth = YearMonth.now();
        monthLabel = new JLabel("",SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI",Font.BOLD,20));
        monthLabel.setForeground(new Color(80, 50, 120));
        monthLabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(240, 230, 250));

        JButton prevBtn = new JButton("◀ Mois précédent");
        JButton nextBtn = new JButton("Mois suivant ▶");
        styliserBouton(prevBtn);
        styliserBouton(nextBtn);

        prevBtn.addActionListener(e -> { 
            currentMonth = currentMonth.minusMonths(1); 
            refreshCalendar(); // MODIFICATION: Sans paramètre
        });
        nextBtn.addActionListener(e -> { 
            currentMonth = currentMonth.plusMonths(1); 
            refreshCalendar(); // MODIFICATION: Sans paramètre
        });

        topBar.add(prevBtn, BorderLayout.WEST);
        topBar.add(monthLabel, BorderLayout.CENTER);
        topBar.add(nextBtn, BorderLayout.EAST);

        gridPanel = new JPanel(new GridLayout(0,7,2,2));
        gridPanel.setBackground(new Color(250, 245, 255));

        add(topBar, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        // AJOUT: Rafraîchissement automatique après chaque opération
        controller.addRefreshListener(() -> refreshCalendar());

        refreshCalendar(); // MODIFICATION: Sans paramètre
    }

    private void styliserBouton(JButton btn){
        btn.setBackground(new Color(180, 100, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI",Font.BOLD,14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
    }

    // MODIFICATION: Méthode sans paramètre pour rafraîchissement automatique
    public void refreshCalendar(){
        gridPanel.removeAll();
        monthLabel.setText("📅 " + currentMonth.getMonth() + " " + currentMonth.getYear());

        String[] jours = {"Lun","Mar","Mer","Jeu","Ven","Sam","Dim"};
        for(String j : jours){
            JLabel lbl = new JLabel(j,SwingConstants.CENTER);
            lbl.setForeground(new Color(80, 50, 120));
            lbl.setFont(new Font("Segoe UI",Font.BOLD,14));
            gridPanel.add(lbl);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int start = firstDay.getDayOfWeek().getValue();
        int totalDays = currentMonth.lengthOfMonth();

        for(int i=1;i<start;i++) gridPanel.add(new JLabel(""));

        for(int day=1; day<=totalDays; day++){
            LocalDate date = currentMonth.atDay(day);
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            
            Color bgColor = Color.WHITE;
            Color textColor = new Color(80, 50, 120);
            
            if(date.getDayOfWeek().getValue() == 5) {
                bgColor = new Color(245, 240, 250);
            } else if(date.getDayOfWeek().getValue() >= 6) {
                bgColor = new Color(250, 245, 255);
            }
            
            if(date.equals(LocalDate.now())) {
                bgColor = new Color(220, 200, 240);
            }
            
            dayPanel.setBackground(bgColor);

            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setForeground(textColor);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            dayPanel.add(dayLabel, BorderLayout.NORTH);

            List<Evenement> events = controller.getEvenementsPourDate(date);
            if(!events.isEmpty()){
                JPanel eventsPanel = new JPanel();
                eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
                eventsPanel.setBackground(bgColor);
                
                for(Evenement ev: events) {
                    JLabel evLabel = new JLabel("• " + ev.getTitre());
                    evLabel.setForeground(new Color(180, 100, 200));
                    evLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    evLabel.setToolTipText(ev.getTitre() + " - " + ev.getHeure().format(timeFormatter));
                    
                    // MENU CONTEXTUEL AVEC PARTAGE
                    evLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                JPopupMenu menu = new JPopupMenu();

                                JMenuItem voirInfo = new JMenuItem("🔍 Voir informations");
                                voirInfo.addActionListener(ae -> {
                                    String participantsText = (ev.getParticipants() != null && !ev.getParticipants().isEmpty())
                                            ? String.join(", ", ev.getParticipants())
                                            : "Aucun";

                                    String info = "<html>" +
                                            "<b>Titre:</b> " + ev.getTitre() + "<br>" +
                                            "<b>Description:</b> " + ev.getDescription() + "<br>" +
                                            "<b>Responsable:</b> " + ev.getResponsable() + "<br>" +
                                            "<b>Participants:</b> " + participantsText + "<br>" +
                                            "<b>Date:</b> " + ev.getDate() + "<br>" +
                                            "<b>Heure:</b> " + ev.getHeure() +
                                            "</html>";

                                    JOptionPane.showMessageDialog(evLabel, info,
                                            "Détails - " + ev.getTitre(),
                                            JOptionPane.INFORMATION_MESSAGE);
                                });

                                JMenuItem mod = new JMenuItem("✏️ Modifier");
                                JMenuItem sup = new JMenuItem("🗑️ Supprimer");
                                JMenuItem partager = new JMenuItem("🔗 Partager");

                                mod.addActionListener(ae -> {
                                    EventDialog dialog = new EventDialog(parentFrame, controller, ev.getDate());
                                    dialog.setEvenement(ev);
                                    dialog.setVisible(true);
                                    // RAFRAÎCHISSEMENT AUTOMATIQUE APRÈS MODIFICATION
                                    refreshCalendar();
                                });

                                sup.addActionListener(ae -> {
                                    int confirm = JOptionPane.showConfirmDialog(parentFrame, 
                                        "Voulez-vous supprimer l'événement \"" + ev.getTitre() + "\" ?", 
                                        "Confirmation", 
                                        JOptionPane.YES_NO_OPTION);
                                    if (confirm == JOptionPane.YES_OPTION) {
                                        controller.supprimerEvenement(ev);
                                        // RAFRAÎCHISSEMENT AUTOMATIQUE APRÈS SUPPRESSION
                                        refreshCalendar();
                                    }
                                });

                                partager.addActionListener(ae -> {
                                    partagerEvenement(ev, parentFrame);
                                });

                                menu.add(voirInfo);
                                menu.addSeparator();
                                menu.add(mod);
                                menu.add(sup);
                                menu.addSeparator();
                                menu.add(partager);

                                menu.show(evLabel, e.getX(), e.getY());
                            }
                        }
                    });
                    
                    eventsPanel.add(evLabel);
                }
                dayPanel.add(eventsPanel, BorderLayout.CENTER);
            }

            dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        EventDialog dialog = new EventDialog(parentFrame, controller, date);
                        dialog.setVisible(true);
                        // RAFRAÎCHISSEMENT AUTOMATIQUE APRÈS CRÉATION
                        refreshCalendar();
                    }
                }
            });

            gridPanel.add(dayPanel);
        }

        revalidate();
        repaint();
    }

    // 🆕 MÉTHODE POUR PARTAGER
    private void partagerEvenement(Evenement ev, JFrame parent) {
        String[] participantsList = {"Patient","Parent / Accompagnant","Assistante Samira","Infirmière Lina",
                "Dr. Ahmed","Dr. Salima","Technicien Radio","Secrétaire Karima","Stagiaire",
                "Équipe médicale","Visiteur"};
        
        JComboBox<String> userCombo = new JComboBox<>(participantsList);
        userCombo.setEditable(true);
        userCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        int result = JOptionPane.showConfirmDialog(
                parent,
                new Object[]{
                    "🔗 Partager l'événement:",
                    "<html><b>" + ev.getTitre() + "</b></html>",
                    "Avec:",
                    userCombo
                },
                "Partager l'événement",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String selectedUser = ((String) userCombo.getSelectedItem()).trim();
            if (!selectedUser.isEmpty()) {
                String successHTML = String.format(
                    "<html>" +
                    "<div style='background: linear-gradient(135deg, #B464C8, #8A2BE2); padding: 15px; border-radius: 10px; color: white; width: 300px;'>" +
                    "<h3 style='margin: 0 0 10px 0; text-align: center;'>✅ Partagé avec succès</h3>" +
                    "<div style='font-size: 12px; text-align: center;'>Événement <b>%s</b><br>partagé avec <b>%s</b></div>" +
                    "</div>" +
                    "</html>",
                    ev.getTitre(), selectedUser
                );

                JLabel successLabel = new JLabel(successHTML);
                successLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                JOptionPane.showMessageDialog(
                    parent,
                    successLabel,
                    "💜 Partage réussi",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
}