package finalproject;

import javax.swing.*;
import java.awt.*;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Leaderboards extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    private JFrame window;
    private BufferedImage leaderboard_ui_image; // Background image for the leaderboards screen
    private Point mouse_pos = new Point(0, 0);
    private Timer timer;
    private ArrayList<Character> characters; // List of saved characters (for leaderboard data)
    private HashMap<String, Rectangle> buttons = new HashMap<>();
    private Statement stmt; // Class-level declaration of stmt

    public Leaderboards(JFrame window, Statement stmt) {
        this.window = window;
        this.stmt = stmt; // Initialize stmt at the class level

        // Load images
        leaderboard_ui_image = HelperMethods.loadLeaderboardBackgroundImage();

        // Initialize buttons with improved alignment
        buttons.put("Back", new Rectangle(25, 25, 200, 50)); // Back button at the top-left
        buttons.put("Time", new Rectangle(930, 230, 320, 75)); // Time button on the right
        buttons.put("Level", new Rectangle(930, 323, 320, 75)); // Level button below "Time"
        buttons.put("Score", new Rectangle(930, 416, 320, 75)); // Score button below "Level"

        // Load saved characters from the database
        characters = loadEntriesFromDatabase(stmt);

        // Set up the panel
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        addMouseListener(this);
        addMouseMotionListener(this);

        // Start the timer for animations or updates
        timer = new Timer(Constants.DELAY, this);
        timer.start();
    }

    private ArrayList<Character> loadEntriesFromDatabase(Statement stmt, String tableName, String sort) {
        ArrayList<Character> entries = new ArrayList<>();
        try {
            if (stmt == null || stmt.getConnection() == null || stmt.getConnection().isClosed()) {
                System.out.println("Database statement or connection is invalid. Unable to load entries.");
                return entries;
            }

            // Construct the SQL query with the provided sorting criteria
            String query = "SELECT * FROM " + tableName;
            if (sort != null && !sort.isEmpty()) {
                query += " ORDER BY " + sort+" DESC";
            }
            if (tableName.equals("leaderboards")) {
                query += " LIMIT 10"; // Limit results for leaderboards table
            }

            // Execute the query
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String name = rs.getString("name");
                String difficulty = rs.getString("difficulty");
                int level = rs.getInt("level");
                int strength = rs.getInt("strength");
                int agility = rs.getInt("agility");
                int vitality = rs.getInt("vitality");
                int coins = rs.getInt("coins");
                int smallPotion = rs.getInt("smallPotion");
                int mediumPotion = rs.getInt("mediumPotion");
                int bigPotion = rs.getInt("bigPotion");
                int speedPotion = rs.getInt("speedPotion");
                int goldenBanana = rs.getInt("goldenBanana");
                int levelProgress = rs.getInt("level_progress");
                long timer = rs.getLong("timer"); // Load the timer value
                int score = rs.getInt("score");

                // Create a Character object for leaderboard display
                Character entry = new Character(
                        new Rectangle(0, 0, 74, 107), name, difficulty, level, strength, agility, vitality,
                        coins, smallPotion, mediumPotion, bigPotion, speedPotion, goldenBanana, levelProgress
                );
                entry.timer = timer; // Set the timer value
                entry.score = score;
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            System.out.println("Failed to execute query for table: " + tableName);
            e.printStackTrace();
        }

        return entries;
    }

    private ArrayList<Character> loadEntriesFromDatabase(Statement stmt, String sort) {
        return loadEntriesFromDatabase(stmt, "characters", sort);
    }

    private ArrayList<Character> loadEntriesFromDatabase(Statement stmt) {
        return loadEntriesFromDatabase(stmt, "characters", "score");
    }

    // Algorithm for Time Sort
    private void srtTime() {
        System.out.println("please implement time sort here");
        characters = loadEntriesFromDatabase(stmt, "timer");
    }

    // Algorithm for Level Sort
    private void srtLevel() {
        System.out.println("please implement level sort here");
        characters = loadEntriesFromDatabase(stmt, "level");
    }

    // Algorithm for Score Sort
    private void srtScore() {
        System.out.println("please implement score sort here");
        characters = loadEntriesFromDatabase(stmt, "score");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image
        g.drawImage(leaderboard_ui_image, 0, 0, this);

        if (characters.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.WHITE);
            g.drawString("No entries yet!", Constants.GAME_WIDTH / 2 - 100, Constants.GAME_HEIGHT / 2);
            return;
        }

        // Render leaderboard entries
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        int yOffset = 150; // Starting vertical position for leaderboard entries
        for (int i = 0; i < characters.size(); i++) {
            Character character = characters.get(i);
            String formattedTime = String.format("%02d:%02d:%02d",
                    character.timer / 3600000, // Hours
                    (character.timer % 3600000) / 60000, // Minutes
                    (character.timer % 60000) / 1000); // Seconds

            String entry = String.format("%d. %s - Level %d - %s - %d Coins - Score: %d - Time: %s",
                    i + 1, character.name, character.level, character.difficulty, character.coins, character.score, formattedTime);
            g.drawString(entry, 300, yOffset);
            yOffset += 30; // Adjust spacing between entries
        }

    }
    HashMap<String, Comparator<Character>> sortCriteria = new HashMap<>() {
        {
            put("Time", Comparator.comparingLong(c -> c.timer));
            put("Level", Comparator.comparingInt(c -> c.level));
            put("Score", Comparator.comparingInt(c -> c.score));
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint(); // Trigger a repaint to update the UI
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (String buttonName : buttons.keySet()) {
            Rectangle bp = buttons.get(buttonName);
            if (e.getX() >= bp.x && e.getX() <= bp.x + bp.width
                    && e.getY() >= bp.y && e.getY() <= bp.y + bp.height) {

                if (buttonName.equals("Back")) {
                    // Stop the timer in Leaderboards
                    if (timer != null && timer.isRunning()) {
                        timer.stop();
                    }

                    // Return to the Start Menu
                    window.removeMouseListener(this);
                    window.removeMouseMotionListener(this);

                    StartMenu menu = new StartMenu(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT), window, stmt, false);
                    window.add(menu);
                    window.addMouseListener(menu);
                    window.addMouseMotionListener(menu);
                    window.addKeyListener(menu);

                    window.remove(this);
                    window.validate();
                    window.requestFocusInWindow(); // Request focus for the StartMenu
                    StartMenu.currentScreen = "Start"; // Reset the screen state
                } else if (buttonName.equals("Time")) {
                    System.out.println("Time Sort");
                    srtTime();
                } else if (buttonName.equals("Level")) {
                    System.out.println("Level Sort");
                    srtLevel();
                } else if (buttonName.equals("Score")) {
                    System.out.println("Score Sort");
                    srtScore();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouse_pos = new Point(e.getX(), e.getY());
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
}
