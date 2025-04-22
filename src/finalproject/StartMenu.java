package finalproject;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import static finalproject.Constants.*;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class StartMenu extends JPanel implements ActionListener, MouseMotionListener, MouseListener, KeyListener {

    private JFrame window;
    private Statement stmt;

    private BufferedImage start_screen_image;
    private BufferedImage new_game_image;
    private BufferedImage load_game_image;
    private BufferedImage settings_audio_image;
    private BufferedImage settings_controls_image;
    private BufferedImage character_image;
    private BufferedImage load_game_buttons_image;
    private BufferedImage load_game_character_data_image;

    private HelperMethods helperMethods = new HelperMethods();

    private final int DELAY = 25;
    private Timer timer;
    private Vector button_names = new Vector(Arrays.asList("New Game", "Load Game", "Settings"));
    private Font customFont;

    private HashMap<String, Point> button_pos = new HashMap();
    private Point mouse_pos = new Point(0, 0);

    private String currentScreen = "";

    private String typedCharacterName = "";
    private int frame = 0;
    private String difficultyChosen = "Easy";

    private int animationOffset = 0;
    private boolean animate = false;

    private static Settings settings;
    private File music;
    private Clip musicClip;

    private ArrayList<Character> characters = new ArrayList();

    StartMenu(Dimension window_size, JFrame window, Statement stmt, boolean playM) {
        setPreferredSize(window_size);

        this.window = window;
        this.stmt = stmt;

        loadSettings();

        try {
            music = new File("src/finalproject/audio/bgMusic.wav");
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/finalproject/ARCADE_N.TTF")).deriveFont(28f);
            start_screen_image = ImageIO.read(new File("src/finalproject/img/UIs/start_screen.png"));
            load_game_image = ImageIO.read(new File("src/finalproject/img/UIs/load_game_screen.png"));
            load_game_buttons_image = ImageIO.read(new File("src/finalproject/img/UIs/load_game_buttons.png"));
            load_game_character_data_image = ImageIO.read(new File("src/finalproject/img/UIs/load_game_character_data.png"));
            settings_audio_image = ImageIO.read(new File("src/finalproject/img/UIs/settings_audio_screen.png"));
            settings_controls_image = ImageIO.read(new File("src/finalproject/img/UIs/settings_controls_screen.png"));
            character_image = ImageIO.read(new File("src/finalproject/img/character/idle.png"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException ex) {
            System.out.println(System.getProperty("user.dir"));
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

        this.currentScreen = "Start";
        if (playM) {
            playMusic();
        }

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        this.frame++;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.currentScreen.equals("Start")) {
            startMenuScreen(g);
        } else if (this.currentScreen.equals("NEW GAME")) {
            newGameScreen(g);
        } else if (this.currentScreen.equals("LOAD GAME")) {
            loadGameScreen(g);
        } else {
            settingsScreen(g);
        }
        g.drawOval(mouse_pos.x - 5, mouse_pos.y - 5, 10, 10);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouse_pos = new Point(e.getX(), e.getY() - 32);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        System.out.println(mouse_pos);
        if (this.currentScreen.equals("Start")) {
            for (String buttonName : button_pos.keySet()) {
                Point bp = button_pos.get(buttonName);
                if (mouse_pos.x >= bp.x
                        && mouse_pos.x <= bp.x + 295
                        && mouse_pos.y <= bp.y + 69
                        && mouse_pos.y >= bp.y) {
                    if (buttonName.equals("New Game")) {
                        this.currentScreen = "NEW GAME";
                        changeDifficulty();
                    } else if (buttonName.equals("Load Game")) {
                        loadCharacters();
                        this.currentScreen = "LOAD GAME";
                    } else {
                        this.currentScreen = "Settings-audio";
                    }
                }
            }
        } else if (this.currentScreen.equals("NEW GAME")) {
            HashMap<String, Rectangle> buttons = new HashMap<>() {
                {
                    put("Create", new Rectangle(660, 505, 252, 49));
                    put("Back", new Rectangle(380, 503, 258, 51));
                    put("Easy", new Rectangle(380, 330, 192, 44));
                    put("Normal", new Rectangle(380, 382, 192, 44));
                    put("Hard", new Rectangle(380, 436, 192, 44));
                }
            };
            for (String buttonName : buttons.keySet()) {
                Rectangle bp = buttons.get(buttonName);
                if (mouse_pos.x >= bp.x
                        && mouse_pos.x <= bp.x + bp.width
                        && mouse_pos.y <= bp.y + bp.height
                        && mouse_pos.y >= bp.y) {

                    if (buttonName.equals("Easy") || buttonName.equals("Normal") || buttonName.equals("Hard")) {
                        this.difficultyChosen = buttonName;
                        changeDifficulty();
                    } else if (buttonName.equals("Create")) {
                        if (testing) {
                            Rectangle rect = new Rectangle(100, 300, 74, 107);
                            Character character = new Character(rect, "Hornley", this.difficultyChosen, 1, 2, 1, 3, 0, 0, 0, 0, 0, 0, 1);
                            window.removeMouseListener(this);
                            window.removeMouseMotionListener(this);
                            window.removeKeyListener(this);
                            Game game = new Game(window, character, stmt, customFont, settings);
                            window.add(game);
                            window.addKeyListener(game);
                            window.addMouseListener(game);
                            window.addMouseMotionListener(game);
                            window.remove(this);
                            window.validate();
                            return;
                        }
                        if (typedCharacterName.isEmpty()) {
                            return;
                        }
                        try {
                            ResultSet rs = stmt.executeQuery("SELECT * FROM characters");
                            boolean exists = false;
                            while (rs.next()) {
                                if (rs.getString("name").equals(typedCharacterName)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (exists) {
                                JOptionPane.showMessageDialog(null, "Character already exists!");
                            } else {
                                stmt.execute("INSERT INTO characters(name, difficulty, level, strength, agility, vitality, coins, smallPotion, mediumPotion, bigPotion, speedPotion, goldenBanana, level_progress) VALUES('" + typedCharacterName + "', '" + this.difficultyChosen + "', 1, 2, 1, 3, 0, 0, 0, 0, 0, 0, 1)");
                                loadCharacters();
                                this.currentScreen = "LOAD GAME";
                            }
                        } catch (SQLException SQLError) {
                            System.out.println("Unable to execute statement!");
                            SQLError.printStackTrace();
                        }
                    } else {
                        this.currentScreen = "Start";
                    }
                }
            }
        } else if (this.currentScreen.equals("LOAD GAME")) {
            HashMap<String, Rectangle> buttons = new HashMap<>() {
                {
                    put("Back", new Rectangle(25, 25, 258, 51));
                }
            };
            for (int i = 0; i < characters.size(); i++) {
                buttons.put("Delete(" + i + ")", new Rectangle(364 + 545 - load_game_buttons_image.getWidth() - 20,
                        50 + 126 / 2 - load_game_buttons_image.getHeight() / 2 + (i * (126 + 2)), 30, 33));
                buttons.put("Play(" + i + ")", new Rectangle(364 + 545 - load_game_buttons_image.getWidth() - 20,
                        50 + 126 / 2 - load_game_buttons_image.getHeight() / 2 + (i * (126 + 2)) + 53, 31, 31));
            }
            for (String buttonName : buttons.keySet()) {
                Rectangle bp = buttons.get(buttonName);
                if (mouse_pos.x >= bp.x
                        && mouse_pos.x <= bp.x + bp.width
                        && mouse_pos.y <= bp.y + bp.height
                        && mouse_pos.y >= bp.y) {

                    if (buttonName.equals("Back")) {
                        this.currentScreen = "Start";
                    } else if (buttonName.startsWith("Delete")) {
                        try {
                            Character character = characters.get(Integer.parseInt(buttonName.substring(7, 8)));
                            stmt.execute("DELETE FROM characters WHERE name='" + character.name + "'");
                            characters.remove(character);
                        } catch (SQLException exex) {
                            exex.printStackTrace();
                        }
                    } else if (buttonName.startsWith("Play")) {
                        Character character = characters.get(Integer.parseInt(buttonName.substring(5, 6)));
                        window.removeMouseListener(this);
                        window.removeMouseMotionListener(this);
                        window.removeKeyListener(this);

                        // Pass the `stmt` object to the Game constructor
                        Game game = new Game(window, character, stmt, customFont, settings);
                        window.add(game);
                        window.addKeyListener(game);
                        window.addMouseListener(game);
                        window.addMouseMotionListener(game);

                        window.remove(this);
                        window.validate();
                    }
                }
            }
        } else {  // Settings
            HashMap<String, Rectangle> buttons = new HashMap<>() {
                {
                    put("Back", new Rectangle(25, 25, 258, 51));
                    put("Audio", new Rectangle(389, 127, 244, 47));
                    put("Controls", new Rectangle(389, 178, 244, 47));
                    put("Music", new Rectangle(775, 175, 100, 16));
                    put("Sound", new Rectangle(775, 212, 100, 16));
                    put("Ambience", new Rectangle(775, 248, 100, 16));
                }
            };
            for (String buttonName : buttons.keySet()) {
                Rectangle bp = buttons.get(buttonName);
                if (mouse_pos.x >= bp.x
                        && mouse_pos.x <= bp.x + bp.width
                        && mouse_pos.y <= bp.y + bp.height
                        && mouse_pos.y >= bp.y) {
                    if (buttonName.equals("Back")) {
                        this.currentScreen = "Start";
                        saveSettings();
                    } else if (buttonName.equals("Audio")) {
                        this.currentScreen = "Settings-audio";
                    } else if (buttonName.equals("Controls")) {
                        this.currentScreen = "Settings-controls";
                    } else if (this.currentScreen.contains("audio")) {
                        if (buttonName.equals("Music")) {
                            settings.music = mouse_pos.x - bp.x - 5;
                        } else if (buttonName.equals("Sound")) {
                            settings.sound = mouse_pos.x - bp.x - 5;
                        } else if (buttonName.equals("Ambience")) {
                            settings.ambience = mouse_pos.x - bp.x - 5;
                        }
                    } else if (this.currentScreen.contains("controls")) {

                    }
                }
            }
        }
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

    private void startMenuScreen(Graphics g) {
        g.drawImage(start_screen_image, 0, 0, this);

        for (Object button_name : this.button_names) {
            Point pos = new Point(500, 290 + this.button_names.indexOf(button_name) * 70);
            button_pos.put(button_name.toString(), pos);
        }
    }

    private void newGameScreen(Graphics g) {
        g.drawImage(new_game_image, 0, 0, this);
        // 537, 282
        g.setFont(customFont);
        g.setColor(new Color(228, 229, 231));
        String typing = this.typedCharacterName + ((this.frame % 48 == 0) ? "_" : "");
        g.drawString(typing, 530, 260);
    }

    private void loadGameScreen(Graphics g) {
        g.drawImage(load_game_image, 0, 0, this);

        if (frame % 4 == 0) {
            animate = true;
        }

        if (animate) {
            animationOffset++;
            animate = false;
            if (animationOffset > 6) {
                animationOffset = 0;
            }
        }

        // Load character datas
        int scaledWidth = (int) (character_image.getWidth() * 0.7 / 7);
        int scaledHeight = (int) (character_image.getHeight() * 0.7);
        for (int i = 0; i < characters.size(); i++) {
            int yOffset = i * 128;
            g.setColor(new Color(57, 65, 108));
            g.fillRect(523, 76 + yOffset, 227, 75);

            g.drawImage(character_image.getSubimage(animationOffset * 74, 0, 74, 120), 378,
                    50 + scaledHeight / 2 + yOffset,
                    scaledWidth, scaledHeight, this);
            g.setFont(customFont.deriveFont(10f));
            g.setColor(new Color(228, 229, 231));
            g.drawString(characters.get(i).name, 450, 70 + yOffset);

            // Character Summary Data
            g.drawImage(load_game_character_data_image, 480, 68 + yOffset, this);
            helperMethods.drawCenteredString(g, "Level: " + characters.get(i).level, new Rectangle(480, 80 + yOffset, 138, 73), customFont.deriveFont(10f));
            if (characters.get(i).difficulty.equals("Easy")) {
                g.setColor(Color.green);
            } else if (characters.get(i).difficulty.equals("Normal")) {
                g.setColor(Color.yellow);
            } else {
                g.setColor(Color.red);
            }
            helperMethods.drawCenteredString(g, characters.get(i).difficulty, new Rectangle(480, 100 + yOffset, 138, 73), customFont.deriveFont(10f));
            g.setColor(new Color(82, 113, 255));
            helperMethods.drawCenteredString(g, "Level: " + characters.get(i).currentLevelProgress,
                    new Rectangle(645, 80 + yOffset, 174, 92), customFont.deriveFont(10f));

            // Delete and Play Buttons
            g.drawImage(load_game_buttons_image, 364 + 545 - load_game_buttons_image.getWidth() - 20,
                    50 + 126 / 2 - load_game_buttons_image.getHeight() / 2 + i * 128, this);
        }
    }

    private void settingsScreen(Graphics g) {
        g.drawImage((this.currentScreen.contains("controls")) ? settings_controls_image : settings_audio_image, 0, 0, this);

        if (!this.currentScreen.contains("controls")) {
            // Audio sliders
            int radius = 10;
            g.setColor(Color.white);
            // Music
            g.drawLine(775, 183, 875, 183);
            g.fillOval(875 - radius / 2 - (100 - settings.music), 183 - radius / 2, radius, radius);
            // Sound
            g.drawLine(775, 220, 875, 220);
            g.fillOval(875 - radius / 2 - (100 - settings.sound), 220 - radius / 2, radius, radius);
            // Ambience
            g.drawLine(775, 256, 875, 256);
            g.fillOval(875 - radius / 2 - (100 - settings.ambience), 256 - radius / 2, radius, radius);

            g.drawRect(775, 175, 100, 16);
            g.drawRect(775, 212, 100, 16);
            g.drawRect(775, 248, 100, 16);
        } else {

        }
    }

    private void loadCharacters() {
        if (testing) {
            return;
        }

        try {
            characters.clear();
            ResultSet rs = stmt.executeQuery("SELECT * FROM characters");
            while (rs.next()) {
                int x = 100;
                int y = 300;
                Rectangle rect = new Rectangle(x, y, 74, 107);
                String name = rs.getString("name");
                String d = rs.getString("difficulty");
                int lvl = rs.getInt("level");
                int s = rs.getInt("strength");
                int a = rs.getInt("agility");
                int v = rs.getInt("vitality");
                int c = rs.getInt("coins");
                int smallp = rs.getInt("smallPotion");
                int mediump = rs.getInt("mediumPotion");
                int bigp = rs.getInt("bigPotion");
                int speedp = rs.getInt("speedPotion");
                int goldenbanana = rs.getInt("goldenBanana");
                int lp = rs.getInt("level_progress");

                Character character = new Character(rect, name, d, lvl, s, a, v, c, smallp, mediump, bigp, speedp, goldenbanana, lp);
                characters.add(character);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    private void changeDifficulty() {
        try {
            String filename = "src/finalproject/img/UIs/new_game_screen_" + this.difficultyChosen + ".png";
            new_game_image = ImageIO.read(new File(filename));
        } catch (IOException ex) {
            System.out.println(System.getProperty("user.dir"));
        }
    }

    private void playMusic() {
        try {
            AudioInputStream aui = AudioSystem.getAudioInputStream(music);
            try {
                musicClip = AudioSystem.getClip();
                musicClip.open(aui);
                FloatControl gainControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue((float) (settings.music * 0.86 - 80));
            } catch (IOException | LineUnavailableException ex) {
            }
        } catch (IOException | UnsupportedAudioFileException exx) {
        }
        musicClip.start();
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_BACK_SPACE && !this.typedCharacterName.isEmpty()) {
            this.typedCharacterName = this.typedCharacterName.substring(0, this.typedCharacterName.length() - 1);
        }

        if ((keyCode >= 48 && keyCode <= 57)
                || (keyCode >= 65 && keyCode <= 90)
                || (keyCode >= 97 && keyCode <= 122)
                || (keyCode == KeyEvent.VK_SPACE && !this.typedCharacterName.isEmpty())) {

            if (this.currentScreen.equals("NEW GAME")) {
                this.typedCharacterName += e.getKeyChar();
            }
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private static void loadSettings() {
        String filename = "settings.ser";
        settings = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            settings = (Settings) in.readObject();
            in.close();
        } catch (IOException ex) {
            settings = new Settings(100, 100, 100, new HashMap() {
                {
                    // default controls
                }
            });
            System.out.println("Creating a default settings!");
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException!");
            ex.printStackTrace();
        }

        saveSettings();
    }

    private static void saveSettings() {
        String filename = "settings.ser";
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(settings);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
