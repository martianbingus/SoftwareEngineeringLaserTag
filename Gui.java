import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;

public class Gui extends JFrame {

    //Layout & UI Components
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel centerPanel;
    private JTextField ipField;

    //Player Entry Fields (Red Team)
    private ArrayList<JTextField> redPlayerId = new ArrayList<>();
    private ArrayList<JTextField> redPlayerName = new ArrayList<>();
    private ArrayList<JTextField> redPlayerHwId = new ArrayList<>();
    private ArrayList<Integer> redPlayerScores = new ArrayList<>();
    private ArrayList<JLabel> redPlayerLabels = new ArrayList<>(); //to display names/icons
    private int totalRedScore = 0;
    
    //Player Entry Fields (Green Team)
    private ArrayList<JTextField> greenPlayerId = new ArrayList<>();
    private ArrayList<JTextField> greenPlayerName = new ArrayList<>();
    private ArrayList<JTextField> greenPlayerHwId = new ArrayList<>();
    private ArrayList<Integer> greenPlayerScores = new ArrayList<>();
    private ArrayList<JLabel> greenPlayerLabels = new ArrayList<>(); //to display names/icons
    private int totalGreenScore = 0;

    //Game Stats & Event Logging
    private JPanel redStats;    //changed from jtextarea to jpanel
    private JPanel greenStats;  // ""
    private JTextArea eventLog; 
    private JTextArea actionDisplay; 
    private int eventCount;

    //Networking & Logic
    private final udpSend sender;
    private final udpReceive receiver;
    private Laser laser;
    private java.util.HashSet<PlayerSync> synchronizedPlayers = new java.util.HashSet<>();
    private boolean baseHit = false;

    //Timers, Music, and Assets
    private JLabel countdownLabel;
    private Timer iLikeKissingBoysUwU;
    private Timer gameCountdownTimer; // The 30s lead-in
    private Timer mainGameTimer;      // The 6m game
    private int secondsRemaining;
    private Clip musicClip;
    private ImageIcon baseIcon = new ImageIcon(new ImageIcon("baseicon.jpg").getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));

    // hashmap to store player id and score (will more than likely be needed for sprint 4)
    // private java.util.Hashmap<Integer, Integer> playerScores;

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

        // start Receiver Thread (Listener)
        Thread recThread = new Thread(receiver);
        recThread.start();

        // initialize UI
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createSplashScreen(), "SPLASH");
        mainPanel.add(createPlayerEntryScreen(), "ENTRY");
        mainPanel.add(createCountdownScreen(), "COUNTDOWN");
        mainPanel.add(createGameActionScreen(), "GAME");

        add(mainPanel);

        // start Flow
        cardLayout.show(mainPanel, "SPLASH");
        
        // --- TIMER ENABLED ---
        // Auto transition from Splash to Entry after 3 seconds
        Timer splashTimer = new Timer(3000, e -> cardLayout.show(mainPanel, "ENTRY"));
        splashTimer.setRepeats(false);
        splashTimer.start();
    }
    
    // class to allow for players to be synched with the database and photon system to ensure hw ids are transmitted to the system only when they have not been sent yet
    // overriding equals and hashcode so the hashset can compare them accurately
    private static class PlayerSync
    {
		int playerId;
		String hwId;
		
		PlayerSync(int playerId, String hwId)
		{
			this.playerId = playerId;
			this.hwId = hwId;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}
			PlayerSync that = (PlayerSync) o;
			return playerId == that.playerId && hwId.equals(that.hwId);
		}
		
		@Override
		public int hashCode()
		{
			return java.util.Objects.hash(playerId, hwId);
		}
	} 

    /*
    called from udpReceive when a message is received from the system
    parses the message for attacker and victim hw ids
    passes that to processScore
    processScore checks if it's a base or player hit, checks for friendly fire, updates scores accordingly, and logs the event to the event feed
    refreshStats is then called to update the score boxes on the GUI with the new scores
    */
    public void consoleLog(String message) 
    {
        SwingUtilities.invokeLater(() -> {
            if (eventLog != null) 
            {
                String[] parts = message.split(":");
                // parse message for attacker and victim ids, get their codenames from the database, and log "Attacker hit Victim" to the event log
                String attackerHw = parts[0].trim();
                String victimHw = parts[1].trim();
                processScore(attackerHw, victimHw);
                refreshStats();
            }
        });
    }

    //UPDATED to adjust for baseicon
    private void processScore(String attackerHw, String victimHw)
    {
        int attackerIdx = -1;
	int defenderIdx = -1;
        boolean isRedAttacker = false;
	boolean isRedDefender = false;
        String attackerName = "Unknown";
        String victimName = "Unknown";
	boolean isFriendlyFire = false;

        //1. Identify the Attacker and their Team
        for (int i = 0; i < redPlayerHwId.size(); i++)
        {
            if (redPlayerHwId.get(i).getText().equals(attackerHw))
            {
                attackerIdx = i;
                isRedAttacker = true;
                attackerName = redPlayerName.get(i).getText();
                break;
            }
        }
        
        if (attackerIdx == -1)
        {
            for (int i = 0; i < greenPlayerHwId.size(); i++)
            {
                if (greenPlayerHwId.get(i).getText().equals(attackerHw))
                {
                    attackerIdx = i;
                    isRedAttacker = false;
                    attackerName = greenPlayerName.get(i).getText();
                    break;
                }
            }
        }

        if (attackerIdx == -1) return; //stop if attacker isnt in current game

        //2. identify the victim
        victimHw = victimHw.replaceAll("[^0-9]", ""); 
        boolean isBaseHit = false;

        if (victimHw.equals("43"))
        {
            victimName = "Green Base";
            isBaseHit = true;
        } else if (victimHw.equals("53"))
        {
            victimName = "Red Base";
            isBaseHit = true;
        } else {
            //find if victim is a player
            for (int i = 0; i < redPlayerHwId.size(); i++)
            {
                if (redPlayerHwId.get(i).getText().equals(victimHw))
                {
                    victimName = redPlayerName.get(i).getText();
		    defenderIdx = i;
		    isRedDefender = true;
                    break;
                }
            }
            if (victimName.equals("Unknown"))
            {
                for (int i = 0; i < greenPlayerHwId.size(); i++)
                {
                    if (greenPlayerHwId.get(i).getText().equals(victimHw))
                    {
                        victimName = greenPlayerName.get(i).getText();
			defenderIdx = i;
			isRedDefender = false;
                        break;
                    }
                }
            }
        }

        //3. scoring logic & base icon assignment
        int points = 0;

        if (isBaseHit) {
            // Check if they hit the OPPOSING base
            if ((isRedAttacker && victimHw.equals("43")) || (!isRedAttacker && victimHw.equals("53"))) {
                points = 100;
                // Add the physical icon to the player's label
                if (isRedAttacker) {
                    redPlayerLabels.get(attackerIdx).setIcon(baseIcon);
                } else {
                    greenPlayerLabels.get(attackerIdx).setIcon(baseIcon);
                }
            }
        } else {
            //Player hit - check for Friendly Fire
            if (isRedAttacker) {
                isFriendlyFire = isHwIdInList(victimHw, redPlayerHwId);
            } else {
                isFriendlyFire = isHwIdInList(victimHw, greenPlayerHwId);
            }

            if (isFriendlyFire) {
                points = -10;
                // Optionally broadcast both IDs to the system for friendly fire handling
                sender.send(attackerHw);
                sender.send(victimHw);
            } else {
                points = 10;
            }
        }

        //4. Update Score Data
        if (isRedAttacker) {
            redPlayerScores.set(attackerIdx, redPlayerScores.get(attackerIdx) + points);
            totalRedScore += points;
        } else {
            greenPlayerScores.set(attackerIdx, greenPlayerScores.get(attackerIdx) + points);
            totalGreenScore += points;
        }
	// update scores for friendly fire
	if (isFriendlyFire)
	{
		if (isRedDefender)
		{
			redPlayerScores.set(defenderIdx, redPlayerScores.get(defenderIdx) + points);
			totalRedScore += points;
		}
		else
		{
			greenPlayerScores.set(defenderIdx, greenPlayerScores.get(defenderIdx) + points);
			totalGreenScore += points;
		}
	}

        //5. Update Event Log and Trigger UI Refresh
        eventLog.append(attackerName + " hit " + victimName + "!\n");
        eventCount++;
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
        
        //call the method to update the label text with new scores
        refreshStats();
    }

    // helper to check team
    private boolean isHwIdInList(String hwid, ArrayList<JTextField> list) {
        for (JTextField tf : list) {
            if (tf.getText().equals(hwid)) return true;
        }
        return false;
    }

    //UPDATED to update the text of the labels
    private void refreshStats() 
    {
        //Update Red Team individual labels
        for (int i = 0; i < redPlayerLabels.size(); i++) 
        {
            redPlayerLabels.get(i).setText(redPlayerName.get(i).getText() + "    " + redPlayerScores.get(i));
        }

        //Update Green Team individual labels
        for (int i = 0; i < greenPlayerLabels.size(); i++) 
        {
            greenPlayerLabels.get(i).setText(greenPlayerName.get(i).getText() + "    " + greenPlayerScores.get(i));
        }

        //Update the Border Titles to show the cumulative team scores
        redStats.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED, 1), 
            "RED TEAM TOTAL: " + totalRedScore, 
            2, 2, new Font("Monospaced", Font.BOLD, 18), Color.RED));

        greenStats.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN, 1), 
            "GREEN TEAM TOTAL: " + totalGreenScore, 
            2, 2, new Font("Monospaced", Font.BOLD, 18), Color.GREEN));

        //Force the UI to refresh the new text/icons
        redStats.revalidate();
        greenStats.revalidate();
        redStats.repaint();
        greenStats.repaint();
    }

    //Screen builder methods
    private JPanel createSplashScreen() {
        // 1. Load the Image (Ensure "logo.png" is in your project folder)
        ImageIcon originalIcon = new ImageIcon("logo.png");
        Image splashImage = originalIcon.getImage();

        // 2. Create a custom JPanel that paints the image to fill the entire area
        // overrides the paintComponent method to draw the image at the panel's current width/height
        JPanel panel = new JPanel() 
        {
            @Override
            protected void paintComponent(Graphics g) 
            {
                super.paintComponent(g);
                // Draw the image starting at (0,0) and stretching to current width/height
                if (splashImage != null) 
                {
                    g.drawImage(splashImage, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };
        
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BorderLayout());
        
        return panel;
    }

    private JPanel createPlayerEntryScreen() 
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        // header
        JPanel headerContainer = new JPanel(new BorderLayout());
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.BLACK);
        JLabel title = new JLabel("EDIT GAME CONFIGURATION");
        title.setForeground(Color.GREEN);
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        titlePanel.add(title);
        
        // add components to header
        headerContainer.add(titlePanel, BorderLayout.NORTH);
        headerContainer.add(createPlayerCountSelector(), BorderLayout.SOUTH);
        
        panel.add(headerContainer, BorderLayout.NORTH);

        // center Panel
        centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // initialize with default of 20 players (10 per team)
        updateTeamPanels(20);

        panel.add(centerPanel, BorderLayout.CENTER);

        // footer with Buttons
        JPanel footer = new JPanel();
        footer.setBackground(Color.DARK_GRAY);
        
        // Container to stack buttons vertically
        JPanel buttonContainer = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonContainer.setBackground(Color.DARK_GRAY);

        JButton clearButton = new JButton("CLEAR ENTRIES (F9)");
        clearButton.setPreferredSize(new Dimension(200, 50));
        clearButton.setBackground(Color.BLACK);
        clearButton.setForeground(Color.RED);
        clearButton.addActionListener(e -> clearPlayerEntries());

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

        buttonContainer.add(clearButton);
        buttonContainer.add(sendDataButton);
        buttonContainer.add(startButton);

        footer.add(buttonContainer);
        panel.add(footer, BorderLayout.SOUTH);

        // F5 Keybinding to Start Game
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "startGame");
        am.put("startGame", new AbstractAction() 
        { 
            @Override public void actionPerformed(ActionEvent e) { startGame(); } 
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "clearEntries");
        am.put("clearEntries", new AbstractAction() 
        { 
            @Override public void actionPerformed(ActionEvent e) { clearPlayerEntries(); } 
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

    private JPanel createPlayerCountSelector() 
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(Color.DARK_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel("TOTAL PLAYERS:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);

        // 10 buttons for counts: 2, 4, 6 ... 20
        for (int i = 1; i <= 10; i++) 
        {
            int count = i * 2;
            JButton box = new JButton(String.valueOf(count));
            box.setPreferredSize(new Dimension(50, 30));
            box.setBackground(Color.LIGHT_GRAY);
            box.setFocusPainted(false);
            
            // when clicked, update the lists
            box.addActionListener(e -> updateTeamPanels(count));
            
            panel.add(box);
        }
        return panel;
    }


    private JPanel createCountdownScreen()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);

        JLabel warningLabel = new JLabel("Game Starting");
        warningLabel.setForeground(Color.CYAN);
        warningLabel.setFont(new Font("Monospaced", Font.BOLD, 100));

        countdownLabel = new JLabel("");
        countdownLabel.setForeground(Color.RED);
        countdownLabel.setFont(new Font("Monospaced", Font.BOLD, 100));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        panel.add(warningLabel, c);

        c.gridy = 1;
        panel.add(countdownLabel, c);

        return panel;
    }

    // creates the screen for displaying teams and player scores, as well as the event log that displays the events that have occurred
    private JPanel createGameActionScreen() 
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(Color.BLACK);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statsPanel.add(createStatBox("RED TEAM", Color.RED));
        statsPanel.add(createStatBox("GAME EVENTS", Color.CYAN));
        statsPanel.add(createStatBox("GREEN TEAM", Color.GREEN));

        panel.add(statsPanel, BorderLayout.CENTER);

        JButton stopButton = new JButton("Stop Game");
        stopButton.addActionListener(e -> {
            sender.send("221");
            cardLayout.show(mainPanel, "ENTRY");
            stopMusic();
        });
        panel.add(stopButton, BorderLayout.SOUTH);

        return panel;
    }

    // used thrice to build the Game Action Screen
    private JPanel createStatBox(String title, Color borderColor)
    {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.BLACK);
        box.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor, 2), title, 2, 2, 
            new Font("Monospaced", Font.BOLD, 16), borderColor));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.BLACK);

        if (title.equals("RED TEAM"))
        {
            redStats = listPanel;
        }

        else if (title.equals("GREEN TEAM"))
        {
            greenStats = listPanel;
        }

        else
        {
            eventLog = new JTextArea();
            eventLog.setEditable(false);
            eventLog.setBackground(Color.BLACK);
            eventLog.setForeground(borderColor);
            box.add(new JScrollPane(eventLog), BorderLayout.CENTER);
            return box;
        }

        box.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        return box;
    }

    private void updateTeamPanels(int totalPlayers) 
    {
        // clear the lists of text field references since we're rebuilding
        redPlayerId.clear();
        redPlayerName.clear();
        redPlayerHwId.clear();
        greenPlayerId.clear();
        greenPlayerName.clear();
        greenPlayerHwId.clear();

        // calculate players per team
        int playersPerTeam = totalPlayers / 2;

        // clear existing components
        centerPanel.removeAll();

        // redraw with new player counts
        centerPanel.add(createDynamicTeamPanel("RED TEAM", Color.RED, playersPerTeam));
        centerPanel.add(createDynamicTeamPanel("GREEN TEAM", Color.GREEN, playersPerTeam));

        // reload UI
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // helper to create a panel with specific number of rows
    private JPanel createDynamicTeamPanel(String teamName, Color teamColor, int playerCount) 
    {
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBackground(Color.DARK_GRAY); 
        
        // border with team name
        teamPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(teamColor, 3), 
            teamName,
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Dialog", Font.BOLD, 20),
            teamColor
        ));

        // columns: Player ID (20%), Name (60%), Hardware ID (20%)
        JPanel list = new JPanel(new GridBagLayout()); 
        list.setBackground(Color.DARK_GRAY);
        
        // setup Constraints for the layout
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 1.0;
        c.gridy = 0;

        // ID Header
        c.gridx = 0;
        c.weightx = 0.2; 
        JLabel h1 = new JLabel("PLAYER ID", SwingConstants.CENTER);
        h1.setForeground(teamColor); 
        h1.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h1, c);
        
        // Name Header
        c.gridx = 1;
        c.weightx = 0.6;
        JLabel h2 = new JLabel("NAME", SwingConstants.CENTER);
        h2.setForeground(teamColor); 
        h2.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h2, c);

        // hardware ID Header
        c.gridx = 2;
        c.weightx = 0.2;
        JLabel h3 = new JLabel("HW ID", SwingConstants.CENTER);
        h3.setForeground(teamColor); 
        h3.setFont(new Font("Arial", Font.BOLD, 16));
        list.add(h3, c);

        // dark Team Color for the name box
        Color boxColor;
        if (teamColor.equals(Color.RED)) {
            boxColor = new Color(100, 0, 0);
        } else {
            boxColor = new Color(0, 100, 0);
        }

        // add Rows
        for(int i = 0; i < playerCount; i++) 
        {
            c.gridy = i + 1; // move to next row
            
            // player ID Column (Editable Text Field)
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
            
            // Name Column (Editable Text Field)
            // moved so as to allow for 'name' JTextField access so the focus listener can allow for autofilling of codename
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
            
            // hardware ID Column (Editable Text Field)
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

            if (teamName.contains("RED")) 
            {
                redPlayerId.add(playerId);
                redPlayerName.add(name);
                redPlayerHwId.add(hardwareId);
            } 
            else
            {
                greenPlayerId.add(playerId);
                greenPlayerName.add(name);
                greenPlayerHwId.add(hardwareId);
            }
            
            playerId.addFocusListener(new java.awt.event.FocusAdapter() 
            {
				@Override
				public void focusLost(java.awt.event.FocusEvent e) 
				{
					String idText = playerId.getText().trim();
					if (idText.isEmpty()) return;
					
					try 
					{
						int id = Integer.parseInt(idText);
						
						// if id has been used already somewhere else, dont allow it
						int occurrences = 0;
						occurrences += countIdOccurrences(redPlayerId, idText);
						occurrences += countIdOccurrences(greenPlayerId, idText);
						if (occurrences > 1)
						{
							JOptionPane.showMessageDialog(Gui.this, "Player ID " + id + " has already been entered on the entry screen.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
							playerId.setText("");
							name.setText("");
							return;
						}
						
						// query the database via the laser reference
						String existingName = laser.getCodename(id);
						
						if (existingName != null && !existingName.isEmpty()) 
						{
							// fill the corresponding name field
							name.setText(existingName);
						}
					} 
					catch (NumberFormatException ex) 
					{
						// ignore if id isnt valid
					}
				}
			});
			
			hardwareId.addFocusListener(new java.awt.event.FocusAdapter()
			{
				@Override
				public void focusLost(java.awt.event.FocusEvent e)
				{
					String hwText = hardwareId.getText().trim();
					if (hwText.isEmpty()) return;
					
					int occurrences = 0;
					occurrences += countHwOccurrences(redPlayerHwId, hwText);
					occurrences += countHwOccurrences(greenPlayerHwId, hwText);
					
					if (occurrences > 1)
					{
						JOptionPane.showMessageDialog(Gui.this, "Hardware ID " + hwText + " is already assigned to another player!", "Hardware Conflict", JOptionPane.ERROR_MESSAGE);
						hardwareId.setText("");
					}
				}
			});
        }
        
        teamPanel.add(list, BorderLayout.CENTER); 
        
        return teamPanel;
    }

	private int countIdOccurrences(ArrayList<JTextField> idList, String idText)
	{
		int count = 0;
		for (JTextField field : idList)
		{
			if (field.getText().trim().equals(idText))
			{
				count++;
			}
		}
		return count;
	}
	
	private int countHwOccurrences(ArrayList<JTextField> hwList, String hwText) 
	{
		int count = 0;
		for (JTextField field : hwList) 
		{
			if (field.getText().trim().equals(hwText)) 
			{
				count++;
			}
		}
		return count;
	}

    private void sendDataToDatabase() 
    {
        // Process Red Team
		processTeamTransmission(redPlayerId, redPlayerName, redPlayerHwId);
		
		// Process Green Team
		processTeamTransmission(greenPlayerId, greenPlayerName, greenPlayerHwId);
    }
    
    // helper function made to stop writing the same code twice
    private void processTeamTransmission(ArrayList<JTextField> idFields, ArrayList<JTextField> nameFields, ArrayList<JTextField> hwFields)
    {
		for (int i = 0; i < idFields.size(); i++) 
		{
			String name = nameFields.get(i).getText().trim();
			String idText = idFields.get(i).getText().trim();
			String hwId = hwFields.get(i).getText().trim();

			if (!name.isEmpty() && !idText.isEmpty() && !hwId.isEmpty()) 
			{
				try 
				{
					int id = Integer.parseInt(idText);
					PlayerSync currentPair = new PlayerSync(id, hwId);

					// check if this specific player, hwid combo has been sent yet
					if (!synchronizedPlayers.contains(currentPair)) 
					{
						// check if player id already in database. if it isnt, add them
						if (laser.getCodename(id) == null)
						{
							laser.addPlayer(id, name, hwId);
						}
						
						// hardware transmission
						sender.send(hwId);
						
						// mark this combination as "Sent"
						synchronizedPlayers.add(currentPair);
						
						System.out.println("Syncing: " + name + " (ID: " + id + ") to Hardware: " + hwId + "\nHardware ID: " + hwId + " transmitted to system for registration.");
					}
				}
				catch (NumberFormatException e) 
				{
					// Handle non-integer ID input
				}
			}
		}
	}

    private void startCountdownTimer(int duration)
    {
        secondsRemaining = duration;
        countdownLabel.setText(String.valueOf(secondsRemaining));

        if (gameCountdownTimer != null) gameCountdownTimer.stop();

        int INTERVAL = 1000;

        gameCountdownTimer = new Timer(INTERVAL, e ->
        {
            secondsRemaining--;
            countdownLabel.setText(String.valueOf(secondsRemaining));

            if (secondsRemaining <= 0)
            {
                gameCountdownTimer.stop();
                transitionToGame();
            }
        });

        iLikeKissingBoysUwU = new Timer(INTERVAL * duration - 17225, e ->
        {
            iLikeKissingBoysUwU.stop();
            playRandomTrack();
        });

        iLikeKissingBoysUwU.start();
        gameCountdownTimer.start();
    }

    private void clearPlayerEntries() {
        for (JTextField tf : redPlayerId) tf.setText("");
        for (JTextField tf : redPlayerName) tf.setText("");
        for (JTextField tf : redPlayerHwId) tf.setText("");
        for (JTextField tf : greenPlayerId) tf.setText("");
        for (JTextField tf : greenPlayerName) tf.setText("");
        for (JTextField tf : greenPlayerHwId) tf.setText("");
    }



    // NEW MUSIC LOGIC - MATT
    private void playRandomTrack()
    {
        try
        {
            File folder = new File("photon_tracks");
            File[] tracks = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            
            if (tracks != null && tracks.length > 0)
            {
                File selected = tracks[new java.util.Random().nextInt(tracks.length)];
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(selected);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioStream);
                musicClip.start();
                musicClip.loop(Clip.LOOP_CONTINUOUSLY); //play music till game end
                System.out.println("Playing music: " + selected.getName());
            }
        }
        catch (Exception e)
        {
            System.err.println("Audio Error: " + e.getMessage());
        }
    }

    // no mo music :(
    private void stopMusic()
    {
        if (musicClip != null && musicClip.isRunning())
        {
            musicClip.stop();
            musicClip.close();
        }
    }

    private void startGame() {
        // update ip target based on input
        String ipInput = this.ipField.getText().trim();
        sender.setTargetIp(ipInput);

        cardLayout.show(mainPanel, "COUNTDOWN");

        startCountdownTimer(30);
    }



    // Adjusted logic here to account for game timer and baseicon -Matt
    private void transitionToGame()
    {
        cardLayout.show(mainPanel, "GAME");
        sender.send("202");
        //playRandomTrack();

        redStats.removeAll();
        greenStats.removeAll();
        redPlayerLabels.clear();
        greenPlayerLabels.clear();
        redPlayerScores.clear();
        greenPlayerScores.clear();

        // Setup Red Team Labels
        for (JTextField f : redPlayerName)
        {
            if (!f.getText().isEmpty())
            {
                JLabel label = new JLabel(f.getText() + "    0");
                label.setForeground(Color.RED);
                label.setFont(new Font("Monospaced", Font.BOLD, 14));
                redStats.add(label);
                redPlayerLabels.add(label);
                redPlayerScores.add(0);
            }
        }

        // Setup Green Team Labels
        for (JTextField f : greenPlayerName)
        {
            if (!f.getText().isEmpty())
            {
                JLabel label = new JLabel(f.getText() + "    0");
                label.setForeground(Color.GREEN);
                label.setFont(new Font("Monospaced", Font.BOLD, 14));
                greenStats.add(label);
                greenPlayerLabels.add(label);
                greenPlayerScores.add(0);
            }
        }

        redStats.revalidate();
        greenStats.revalidate();
        totalRedScore = 0;
        totalGreenScore = 0;
        refreshStats(); 
    
        startMainGameTimer(360);
    }


    private void startMainGameTimer(int duration)
    {
        secondsRemaining = duration;
        
        mainGameTimer = new Timer(1000, e ->
        {
            secondsRemaining--;
            int mins = secondsRemaining / 60;
            int secs = secondsRemaining % 60;
            
            if (!eventLog.getText().isEmpty())
            {
                ArrayList<String> a = new ArrayList<>(Arrays.asList(eventLog.getText().split("!\n")));
                a.set(0, "TIME REMAINING: " + String.format("%02d:%02d", mins, secs));
                if (eventCount > 5 && a.size() > 1)
                {
                    a.remove(1);
                    eventCount--;
                }

                String updatedText = String.join("!\n", a) + "!\n";

                //String updatedText = "TIME REMAINING: " + String.format("%02d:%02d", mins, secs) + "!"+a[1];
                eventLog.setText(updatedText);
            }
            else
            {
                eventLog.setText("TIME REMAINING: " + String.format("%02d:%02d", mins, secs) + "!\n");
            }

            if (secondsRemaining <= 0)
            {
                mainGameTimer.stop();
                stopMusic();      //music stops at 0
                sender.send("221"); //stop code
                eventLog.append(">>> GAME OVER <<<");
            }
        });
        mainGameTimer.start();
    }


    // Call this from udpReceive.java for base hits!
    public void handleBaseHit(String attackerId)
    {
        SwingUtilities.invokeLater(() ->
        {
            eventLog.append("Base Capture by: " + attackerId + "\n");
            eventCount++;
        });
    }
}
