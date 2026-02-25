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

    // lists to hold references to the text fields for player names, player ids, and hardware ids
    private ArrayList<JTextField> redPlayerId = new ArrayList<>();
    private ArrayList<JTextField> redPlayerName = new ArrayList<>();
    private ArrayList<JTextField> redPlayerHwId = new ArrayList<>();
    
    private ArrayList<JTextField> greenPlayerId = new ArrayList<>();
    private ArrayList<JTextField> greenPlayerName = new ArrayList<>();
    private ArrayList<JTextField> greenPlayerHwId = new ArrayList<>();

    private Laser laser; //Reference to the main Laser class

    private JTextField ipField;

    public Gui(Laser laser, udpSend sender, udpReceive receiver) 
    {
        this.sender = sender;
        this.receiver = receiver;
        this.receiver.setGui(this);
        this.laser = laser;

        setTitle("Laser Tag System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

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
        
        // --- TIMER ENABLED ---
        // Auto transition from Splash to Entry after 3 seconds
        Timer splashTimer = new Timer(3000, e -> cardLayout.show(mainPanel, "ENTRY"));
        splashTimer.setRepeats(false);
        splashTimer.start();
    }

    /*
    Public "Console-Like" Functions for UDP Classes
    Call this from udpReceive to print data to the game screen.
    */
    public void consoleLog(String message) {
        SwingUtilities.invokeLater(() -> {
            actionDisplay.append(message + "\n");
            actionDisplay.setCaretPosition(actionDisplay.getDocument().getLength());
        });
    }

    //Screen builder methods
    private JPanel createSplashScreen() {
        // 1. Load the Image (Ensure "logo.png" is in your project folder)
        ImageIcon originalIcon = new ImageIcon("logo.png");
        Image splashImage = originalIcon.getImage();

        // 2. Create a custom JPanel that paints the image to fill the entire area
        // This overrides the paintComponent method to draw the image at the panel's current width/height
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the image starting at (0,0) and stretching to current width/height
                if (splashImage != null) {
                    g.drawImage(splashImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };
        
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BorderLayout());
        
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

        //Footer with Buttons
        JPanel footer = new JPanel();
        footer.setBackground(Color.DARK_GRAY);
        
        // Container to stack buttons vertically
        JPanel buttonContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonContainer.setBackground(Color.DARK_GRAY);

        JButton sendDataButton = new JButton("SEND DATA");
        sendDataButton.setPreferredSize(new Dimension(200, 50));
        sendDataButton.setBackground(Color.BLACK);
        sendDataButton.setForeground(Color.ORANGE);
        sendDataButton.addActionListener(e -> sendDataToDatabase());

        JButton startButton = new JButton("START GAME (F5)");
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.CYAN);
        startButton.addActionListener(e -> startGame());

        buttonContainer.add(sendDataButton);
        buttonContainer.add(startButton);

        footer.add(buttonContainer);
        panel.add(footer, BorderLayout.SOUTH);

        //F5 Keybinding to Start Game
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "startGame");
        am.put("startGame", new AbstractAction() { 
            @Override public void actionPerformed(ActionEvent e) { startGame(); } 
        });

        // ip address input and send button
        this.ipField = new JTextField("127.0.0.1");
        JPanel ipPanel = new JPanel();
        ipPanel.setBackground(new Color(50,50,50));
        JLabel ipLabel = new JLabel("Target IP: ");
        ipLabel.setForeground(Color.WHITE);
        ipPanel.add(ipLabel);
        ipPanel.add(ipField);

        panel.add(ipPanel, BorderLayout.NORTH);

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
        // clear the lists of text field references since we're rebuilding
        redPlayerId.clear();
        redPlayerName.clear();
        redPlayerHwId.clear();
        greenPlayerId.clear();
        greenPlayerName.clear();
        greenPlayerHwId.clear();

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

        //Columns: Player ID (20%), Name (60%), Hardware ID (20%)
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
        JLabel h1 = new JLabel("PLAYER ID", SwingConstants.CENTER);
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
            
            //Player ID Column (Editable Text Field)
            c.gridx = 0;
            c.weightx = 0.2; //20% Width
            JTextField playerId = new JTextField();
            playerId.setBackground(boxColor);
            playerId.setForeground(Color.WHITE);
            playerId.setCaretColor(Color.WHITE);
            playerId.setFont(new Font("Arial", Font.BOLD, 16));
            playerId.setBorder(BorderFactory.createLineBorder(teamColor, 2));
            playerId.setHorizontalAlignment(JTextField.CENTER);
            list.add(playerId, c);

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

            if (teamName.contains("RED")) {
                redPlayerId.add(playerId);
                redPlayerName.add(name);
                redPlayerHwId.add(hardwareId);
            } else {
                greenPlayerId.add(playerId);
                greenPlayerName.add(name);
                greenPlayerHwId.add(hardwareId);
            }
        }
        
        teamPanel.add(list, BorderLayout.CENTER); 
        
        return teamPanel;
    }

    private void sendDataToDatabase() {
        // red team database entries
        for (int i = 0; i < redPlayerName.size(); i++) 
        {
            // get the data from the text fields
            String name = redPlayerName.get(i).getText().trim();
            String idText = redPlayerId.get(i).getText().trim();
            int hwId = Integer.parseInt(redPlayerHwId.get(i).getText().trim());
            if (!name.isEmpty() && !idText.isEmpty()) 
            {
                // add the player to the database, parse the id text to an integer
                int id = Integer.parseInt(idText);
                laser.addPlayer(id, name, hwId);
            }
        }
        // green team database entries
        for (int i = 0; i < greenPlayerName.size(); i++) 
        {
            // get the data from the text fields
            String name = greenPlayerName.get(i).getText().trim();
            String idText = greenPlayerId.get(i).getText().trim();
            int hwId = Integer.parseInt(greenPlayerHwId.get(i).getText().trim());
            if (!name.isEmpty() && !idText.isEmpty()) 
            {
                // add the player to the database, parse the id text to an integer
                int id = Integer.parseInt(idText);
                laser.addPlayer(id, name, hwId);
            }
        }
        System.out.println("Data successfully dispatched to PostgreSQL.");
    }

    private void startGame() {
        // update ip target based on input
        String ipInput = this.ipField.getText().trim();
        sender.setTargetIp(ipInput);

        cardLayout.show(mainPanel, "GAME");
        actionDisplay.setText("Game Started. Waiting for data...\n");
        sender.send("202"); //Send Start Code
    }
}