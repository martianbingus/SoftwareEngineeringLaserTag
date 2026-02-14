import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.*;

public class Gui extends JFrame {
    //Layout Components
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextArea actionDisplay; 

    //References to the hardware classes
    private final udpSend sender;
    private final udpReceive receiver;

    private JPanel centerPanel;


    public Gui(Laser laser) 
    {
        setTitle("Laser Tag System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        //Initialize Network Classes
        sender = new udpSend(this);
        receiver = new udpReceive(this);

        //Start Receiver Thread (Listener)
        Thread recThread = new Thread(receiver);
        recThread.start();

        //Initialize UI
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createSplashScreen(), "SPLASH");
        mainPanel.add(createPlayerEntryScreen(), "ENTRY");
        mainPanel.add(createGameActionScreen(), "GAME");

        add(mainPanel);

        //Start Flow
        cardLayout.show(mainPanel, "SPLASH");
        
        //Auto transition from Splash to Entry after 5 seconds
        Timer splashTimer = new Timer(5000, e -> cardLayout.show(mainPanel, "ENTRY"));
        splashTimer.setRepeats(false);
        splashTimer.start();
    }

    /*
    Public "Console-Like" Functions for UDP Classes
    Call this from udpReceive to print data to the game screen.
    Acts like System.out.println
    */
    public void consoleLog(String message) {
        SwingUtilities.invokeLater(() -> {
            actionDisplay.append(message + "\n");
            actionDisplay.setCaretPosition(actionDisplay.getDocument().getLength());
        });
    }

    //Screen builder methods
    private JPanel createSplashScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        
        JLabel title = new JLabel("LASER TAG", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 50));
        title.setForeground(Color.GREEN);
        
        JLabel sub = new JLabel("Loading System...", SwingConstants.CENTER);
        sub.setForeground(Color.WHITE);

        panel.add(title, BorderLayout.CENTER);
        panel.add(sub, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPlayerEntryScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        //Header
        JPanel headerContainer = new JPanel(new BorderLayout());
        
        //Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        JLabel title = new JLabel("EDIT GAME CONFIGURATION");
        title.setForeground(Color.GREEN);
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        titlePanel.add(title);
        
        //Add components to header
        headerContainer.add(titlePanel, BorderLayout.NORTH);
        headerContainer.add(createPlayerCountSelector(), BorderLayout.SOUTH);
        
        panel.add(headerContainer, BorderLayout.NORTH);

        //Center Panel
        centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Initialize with default of 20 players (10 per team)
        updateTeamPanels(20);

        panel.add(centerPanel, BorderLayout.CENTER);

        //Footer with Start Button
        JPanel footer = new JPanel();
        footer.setBackground(Color.DARK_GRAY);
        JButton startButton = new JButton("START GAME (F5)");
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.CYAN);
        startButton.addActionListener(e -> startGame());
        footer.add(startButton);
        panel.add(footer, BorderLayout.SOUTH);

        //F5 Keybinding to Start Game
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "startGame");
        am.put("startGame", new AbstractAction() { 
            @Override public void actionPerformed(ActionEvent e) { startGame(); } 
        });

        return panel;
    }

    private JPanel createPlayerCountSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel("TOTAL PLAYERS:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);

        //10 buttons for counts: 2, 4, 6 ... 20
        for (int i = 1; i <= 10; i++) {
            int count = i * 2;
            JButton box = new JButton(String.valueOf(count));
            box.setPreferredSize(new Dimension(50, 30));
            box.setBackground(Color.LIGHT_GRAY);
            box.setFocusPainted(false);
            
            //When clicked, update the lists
            box.addActionListener(e -> updateTeamPanels(count));
            
            panel.add(box);
        }
        return panel;
    }

    private JPanel createGameActionScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        actionDisplay = new JTextArea();
        actionDisplay.setEditable(false);
        actionDisplay.setBackground(Color.BLACK);
        actionDisplay.setForeground(Color.CYAN);
        actionDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        panel.add(new JScrollPane(actionDisplay), BorderLayout.CENTER);
        
        JButton stopButton = new JButton("Stop Game");
        stopButton.addActionListener(e -> {
            sender.send("221"); //Send Stop Code
            cardLayout.show(mainPanel, "ENTRY");
        });
        panel.add(stopButton, BorderLayout.SOUTH);
        
        return panel;
    }

    private void updateTeamPanels(int totalPlayers) {
        //Calculate players per team
        int playersPerTeam = totalPlayers / 2;

        //Clear existing components
        centerPanel.removeAll();

        //Redraw with new player counts
        centerPanel.add(createDynamicTeamPanel("RED TEAM", Color.RED, playersPerTeam));
        centerPanel.add(createDynamicTeamPanel("GREEN TEAM", Color.GREEN, playersPerTeam));

        //Reload UI
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    //Helper to create a panel with specific number of rows
    private JPanel createDynamicTeamPanel(String teamName, Color teamColor, int playerCount) {
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBackground(Color.DARK_GRAY); 
        
        //Border with team name
        teamPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(teamColor, 3), 
            teamName,
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Dialog", Font.BOLD, 20),
            teamColor
        ));

        //Columns: ID (20%), Name (60%), Hardware ID (20%)
        JPanel list = new JPanel(new GridBagLayout()); 
        list.setBackground(Color.DARK_GRAY);
        
        //Setup Constraints for the layout
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 1.0;
        c.gridy = 0;

        //ID Header
        c.gridx = 0;
        c.weightx = 0.2; 
        JLabel h1 = new JLabel("ID", SwingConstants.CENTER);
        h1.setForeground(teamColor); 
        h1.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h1, c);
        
        //Name Header
        c.gridx = 1;
        c.weightx = 0.6;
        JLabel h2 = new JLabel("NAME", SwingConstants.CENTER);
        h2.setForeground(teamColor); 
        h2.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h2, c);

        //Hardware ID Header
        c.gridx = 2;
        c.weightx = 0.2;
        JLabel h3 = new JLabel("HW ID", SwingConstants.CENTER);
        h3.setForeground(teamColor); 
        h3.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h3, c);

        //Dark Team Color for the name box
        Color boxColor;
        if (teamColor.equals(Color.RED)) {
            boxColor = new Color(100, 0, 0);
        } else {
            boxColor = new Color(0, 100, 0);
        }

        //Add Rows
        for(int i = 0; i < playerCount; i++) {
            c.gridy = i + 1; //Move to next row
            
            //ID Column (Non editable Text Field)
            c.gridx = 0;
            c.weightx = 0.2; //20% Width
            
            JLabel idLabel = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            idLabel.setForeground(Color.WHITE);
            idLabel.setFont(new Font("Arial", Font.BOLD, 16));
            idLabel.setOpaque(true);
            idLabel.setBackground(Color.DARK_GRAY.brighter()); 
            idLabel.setBorder(BorderFactory.createLineBorder(teamColor, 1));
            
            list.add(idLabel, c);

            //Name Column (Editable Text Field)
            c.gridx = 1;
            c.weightx = 0.6; //60% Width
            
            JTextField name = new JTextField();
            name.setBackground(boxColor);
            name.setForeground(Color.WHITE);
            name.setCaretColor(Color.WHITE);
            name.setFont(new Font("Arial", Font.BOLD, 16));
            name.setBorder(BorderFactory.createLineBorder(teamColor, 2));
            name.setHorizontalAlignment(JTextField.CENTER);

            list.add(name, c);

            //Hardware ID Column (Editable Text Field)
            c.gridx = 2;
            c.weightx = 0.2; //20% Width
            JTextField hardwareId = new JTextField();
            hardwareId.setBackground(boxColor);
            hardwareId.setForeground(Color.WHITE);
            hardwareId.setCaretColor(Color.WHITE);
            hardwareId.setFont(new Font("Arial", Font.BOLD, 16));
            hardwareId.setBorder(BorderFactory.createLineBorder(teamColor, 2));
            hardwareId.setHorizontalAlignment(JTextField.CENTER);

            list.add(hardwareId, c);
        }
        
        teamPanel.add(list, BorderLayout.CENTER); 
        
        return teamPanel;
    }

    private void startGame() {
        // // gather player info from the text fields, send to laser class to store in database, then switch to game screen
        // List<String> playerNames = new ArrayList<>();
        // List<Integer> playerIds = new ArrayList<>();

        // // FIX GO BACK HERE

        cardLayout.show(mainPanel, "GAME");
        actionDisplay.setText("Game Started. Waiting for data...\n");
        sender.send("202"); //Send Start Code
    }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(() -> {
    //         Gui gui = new Gui();
    //         gui.setVisible(true);
    //     });
    // }
}