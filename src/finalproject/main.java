package finalproject;

import java.awt.Dimension;
import javax.swing.*;
import java.sql.*;
import java.util.HashMap;
import java.io.Serializable;

import static finalproject.Constants.*;

public class main {

    public JFrame window;
    private static final Dimension window_size = new Dimension(1280, 720);
    private static Connection conn = null;
    private static Statement stmt = null;

    private static void loadDatabase() {
        final String DB_URL = "jdbc:sqlite:src/saves.db"; // Path to the SQLite database file

        try {
            conn = DriverManager.getConnection(DB_URL);
            stmt = conn.createStatement();
            System.out.println("Successfully connected to SQLite database!");
        } catch (SQLException e) {
            System.out.println("Unable to connect to SQLite database");
            e.printStackTrace();
        }

        try {
            checkCharactersTable(conn, "characters");
        } catch (SQLException e) {
            System.out.println("Unable to execute statement!");
            e.printStackTrace();
        }
    }

    private static void checkCharactersTable(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (var resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (!resultSet.next()) {
                System.out.println("No table! Creating 'characters' table...");
                stmt.execute("""
                CREATE TABLE characters (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    difficulty TEXT NOT NULL,
                    level INTEGER,
                    strength INTEGER NOT NULL,
                    agility INTEGER NOT NULL,
                    vitality INTEGER NOT NULL,
                    coins INTEGER NOT NULL,
                    smallPotion INTEGER NOT NULL,
                    mediumPotion INTEGER NOT NULL,
                    bigPotion INTEGER NOT NULL,
                    speedPotion INTEGER,
                    goldenBanana INTEGER NOT NULL,
                    level_progress INTEGER NOT NULL
                )
            """);
            } else {
                System.out.println("Yes table!");
            }
        }
    }

    private static void initWindow() {
        // create a window frame and set the title in the toolbar
        JFrame window = new JFrame("Can't Stop, Won't Stop, GameStop");
        // when we close the window, stop the app
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (!testing) {
            loadDatabase();
        }

        StartMenu menu = new StartMenu(window_size, window, stmt, true);
        window.add(menu);
        window.addMouseListener(menu);
        window.addMouseMotionListener(menu);
        window.addKeyListener(menu);

        // don't allow the user to resize the window
        window.setResizable(false);
        // fit the window size around the components (just our jpanel).
        // pack() should be called after setResizable() to avoid issues on some platforms
        window.pack();
        // open window in the center of the screen
        window.setLocationRelativeTo(null);
        // display the window
        window.setVisible(true);
    }

    public static void main(String[] args) {
        // invokeLater() is used here to prevent our graphics processing from
        // blocking the GUI. https://stackoverflow.com/a/22534931/4655368
        // this is a lot of boilerplate code that you shouldn't be too concerned about.
        // just know that when main runs it will call initWindow() once.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initWindow();
            }
        });
    }

}

class Settings implements Serializable {

    public int music = 100;
    public int sound = 100;
    public int ambience = 100;
    public HashMap<String, String> controls = new HashMap();

    Settings(int music, int sound, int ambience, HashMap controls) {
        this.music = music;
        this.sound = sound;
        this.ambience = ambience;
        this.controls = controls;
    }
}
