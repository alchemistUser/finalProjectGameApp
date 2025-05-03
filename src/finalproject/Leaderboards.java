// not fully working yet

package finalproject;

import javax.swing.*;
import java.awt.*;
import java.sql.Statement;
import java.sql.ResultSet;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class Leaderboards extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    private JFrame window;
    private BufferedImage leaderboard_ui_image; // Background image for the leaderboards screen
    private Point mouse_pos = new Point(0, 0);
    private Timer timer;
    private ArrayList<Character> characters; // List of saved characters (for leaderboard data)
    private HashMap<String, Rectangle> buttons = new HashMap<>() {
        {
            put("Back", new Rectangle(25, 25, 258, 51)); // Back button
            put("Time", new Rectangle(900, 250, 200, 40)); // Time button
            put("Level", new Rectangle(900, 300, 200, 40)); // Level button
            put("Score", new Rectangle(900, 350, 200, 40)); // Score button
        }
    };

    public Leaderboards(JFrame window, Statement stmt) {
        this.window = window;

        // Load images
        try {
            leaderboard_ui_image = ImageIO.read(new File("src/finalproject/img/UIs/leaderboards_ui_menu.png"));
        } catch (Exception e) {
            System.out.println("Unable to load leaderboard UI image.");
            e.printStackTrace();
        }

        // Initialize buttons
        buttons.put("Back", new Rectangle(25, 25, 258, 51)); // Back button

        // Load saved characters from the database
        characters = loadCharactersFromDatabase(stmt);

        // Set up the panel
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        addMouseListener(this);
        addMouseMotionListener(this);

        // Start the timer for animations or updates
        timer = new Timer(Constants.DELAY, this);
        timer.start();
    }

    private ArrayList<Character> loadCharactersFromDatabase(Statement stmt) {
        ArrayList<Character> characters = new ArrayList<>();
        if (stmt == null) {
            System.out.println("Database statement is null. Unable to load characters.");
            return characters;
        }

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM characters ORDER BY score DESC"); // Sort by score
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
                long timer = rs.getLong("timer");
                int score = rs.getInt("score");

                Character character = new Character(new Rectangle(0, 0, 74, 107), name, difficulty, level, strength, agility, vitality, coins, smallPotion, mediumPotion, bigPotion, speedPotion, goldenBanana, levelProgress);
                character.timer = timer;
                character.score = score; // Set the score
                characters.add(character);
            }
        } catch (Exception e) {
            System.out.println("Unable to load characters from the database.");
            e.printStackTrace();
        }

        return characters;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image
        g.drawImage(leaderboard_ui_image, 0, 0, this);

        if (characters.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.WHITE);
            g.drawString("No entries yet!", GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2);
            return;
        }

        // Render leaderboard entries
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        int yOffset = 150; // Starting vertical position for leaderboard entries
        for (int i = 0; i < characters.size(); i++) {
            Character character = characters.get(i);
            String entry = String.format("%d. %s - %s - %02d:%02d - %d",
                    i + 1, character.name, character.difficulty,
                    character.timer / 60000, (character.timer % 60000) / 1000,
                    character.score); // Include score
            g.drawString(entry, 300, yOffset + i * 30); // Adjust spacing between entries
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint(); // Trigger a repaint to update the UI
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (String buttonName : buttons.keySet()) {
            Rectangle bp = buttons.get(buttonName);
            if (mouse_pos.x >= bp.x && mouse_pos.x <= bp.x + bp.width
                    && mouse_pos.y >= bp.y && mouse_pos.y <= bp.y + bp.height) {

                if (buttonName.equals("Back")) {
                    // Return to the Start Menu
                    window.removeMouseListener(this);
                    window.removeMouseMotionListener(this);
                    StartMenu menu = new StartMenu(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT), window, null, false);
                    window.add(menu);
                    window.addMouseListener(menu);
                    window.addMouseMotionListener(menu);
                    window.remove(this);
                    window.validate();
                } else if (buttonName.equals("Time")) {
                    // Sort characters by timer (completion time)
                    Collections.sort(characters, Comparator.comparingLong(c -> c.timer));
                } else if (buttonName.equals("Level")) {
                    // Sort characters by level
                    Collections.sort(characters, Comparator.comparingInt(c -> c.level));
                } else if (buttonName.equals("Score")) {
                    // Sort characters by score (assuming a score attribute exists)
                    Collections.sort(characters, Comparator.comparingInt(c -> c.score));
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
