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

    // Define column positions
    int arankX = 420; // Rank column
    int anameX = 490; // Name column
    int alevelX = 635; // Level column
    int atimeX = 710; // Time column
    int ascoreX = 820; // Score column
    int ayOffset = 200; // Starting vertical position

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

    // Algorithm for Time Sort
    private void srtTime() {
        System.out.println("Sorting by time...");
        quickSort(characters, "Time"); // Sort by timer
        repaint(); // Refresh the UI
    }

    // Algorithm for Level Sort
    private void srtLevel() {
        System.out.println("Sorting by level...");
        quickSort(characters, "Level"); // Sort by level
        repaint(); // Refresh the UI
    }

    // Algorithm for Score Sort
    private void srtScore() {
        System.out.println("Sorting by score...");
        quickSort(characters, "Score"); // Sort by score
        repaint(); // Refresh the UI
    }

    private void quickSort(ArrayList<Character> list, String sortBy) {
        quickSortHelper(list, 0, list.size() - 1, sortBy);
    }

    private void quickSortHelper(ArrayList<Character> list, int low, int high, String sortBy) {
        if (low < high) {
            // Partition the array and get the pivot index
            int pi = partition(list, low, high, sortBy);

            // Recursively sort elements before and after partition
            quickSortHelper(list, low, pi - 1, sortBy);
            quickSortHelper(list, pi + 1, high, sortBy);
        }
    }

    private int partition(ArrayList<Character> list, int low, int high, String sortBy) {
        Character pivot = list.get(high); // Choose the last element as the pivot
        int i = low - 1; // Index of the smaller element

        for (int j = low; j < high; j++) {
            // Compare elements based on the sorting criterion
            if (compareCharacters(list.get(j), pivot, sortBy) <= 0) {
                i++;
                // Swap elements at indices i and j
                Collections.swap(list, i, j);
            }
        }

        // Swap the pivot element with the element at i+1
        Collections.swap(list, i + 1, high);

        return i + 1; // Return the partition index
    }

    private int compareCharacters(Character c1, Character c2, String sortBy) {
        switch (sortBy) {
            case "Time":
                return Long.compare(c1.timer, c2.timer); // Ascending order for time
            case "Score":
                return Integer.compare(c1.score, c2.score); // Ascending order for score
            case "Level":
                return Integer.compare(c1.level, c2.level); // Ascending order for level
            default:
                throw new IllegalArgumentException("Invalid sorting criterion: " + sortBy);
        }
    }

    private ArrayList<Character> loadEntriesFromDatabase(Statement stmt) {
        ArrayList<Character> entries = new ArrayList<>();
        try {
            if (stmt == null || stmt.getConnection() == null || stmt.getConnection().isClosed()) {
                System.out.println("Database statement or connection is invalid. Unable to load entries.");
                return entries;
            }

            // Construct the SQL query with the provided sorting criteria
            String query = "SELECT * FROM leaderboards";

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
            System.out.println("Failed to execute query for table: " + "leaderboards");
            e.printStackTrace();
        }

        return entries;
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

        // Set up a monospaced font for consistent alignment
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.setColor(Color.WHITE);

        // Define column positions
        int rankX = arankX; // Rank column
        int nameX = anameX; // Name column
        int levelX = alevelX; // Level column
        int timeX = atimeX; // Time column
        int scoreX = ascoreX; // Score column
        int yOffset = ayOffset; // Starting vertical position

        // Render leaderboard entries
        for (int i = 0; i < characters.size(); i++) {
            Character character = characters.get(i);
            String formattedTime = String.format("%02d:%02d:%02d",
                    character.timer / 3600000, // Hours
                    (character.timer % 3600000) / 60000, // Minutes
                    (character.timer % 60000) / 1000); // Seconds

            g.drawString(String.format("%d", i + 1), rankX, yOffset); // Rank
            g.drawString(character.name, nameX, yOffset); // Name
            g.drawString(String.valueOf(character.level), levelX, yOffset); // Level
            g.drawString(formattedTime, timeX, yOffset); // Time
            g.drawString(String.valueOf(character.score), scoreX, yOffset); // Score
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
                    srtTime();
                } else if (buttonName.equals("Level")) {
                    srtLevel();
                } else if (buttonName.equals("Score")) {
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
