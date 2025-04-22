package finalproject;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import finalproject.Camera;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import static finalproject.Constants.*;

/* Difficulty Ratio
Easy - 1x enemy hp and 1x rewards
Normal - 2x enemy hp and 2x rewards
Hard - 3x enemy hp and 4x rewards
 */
public class Game extends JPanel implements ActionListener, KeyListener, MouseMotionListener, MouseListener {

    // controls the delay between each tick in ms
    private final int DELAY = 25;
    private Timer timer;
    public int tick = 0;

    private HelperMethods helperMethods = new HelperMethods();
    private Font customFont;
    private Settings settings;
    private JFrame window;

    private Character player;
    private Camera camera;

    private boolean nearPortal = false; // Tracks if the player is near the portal

    // Images
    private HashMap<Integer, ArrayList<BufferedImage>> level_images;
    private HashMap<String, BufferedImage> character_images;
    private HashMap<Integer, String> enemiesStatus = new HashMap();
    private HashMap<Integer, BufferedImage> enemy_images;
    private HashMap<Integer, BufferedImage> enemy_projectile_images;
    private HashMap<String, Item> merchant_items_images;

    private BufferedImage merchant_menu_image;
    private BufferedImage heart_image;
    private BufferedImage golden_heart_image;
    private BufferedImage potion_hotkey_image;
    private BufferedImage reset_level_menu_image;
    private BufferedImage coin_image;
    private BufferedImage bullet;
    private Image tower_gif;
    private BufferedImage tower_image;
    private BufferedImage phone_image;

    // Audios
    private HashMap<Integer, File> soundEffects = new HashMap();
    private Clip clip;

    // Levels and Obstacles
    private HashMap<Integer, ArrayList<Object>> levels = new HashMap<>();
    private ArrayList<Enemy> enemy_projectiles = new ArrayList();
    private ArrayList<Coin> coins = new ArrayList();
    private int current_level;
    private int stages;

    private boolean pause = false;
    private boolean gameOver = false;
    private boolean resetLevelMenuPrompt = false;
    private boolean openMerchantMenu = false;

    private int fadeInFrame = 0;
    private int fadeOutFrame = 320;
    private int fadeMaxFrame = 720;
    private int fadeSpeed = 16;
    private boolean fadeInAnimation = false;
    private boolean fadeOutAnimation = false;
    private boolean fadeAnimation = false;

    private int bossPhase = 1;
    private Object bossObject;
    private Phone phone;
    private int lastClick = -1;

    // Saving and Loading
    private Statement stmt;
    private Point mouse_pos = new Point(0, 0);

    //timer
    private long startTime; // Tracks when the stopwatch starts
    private long elapsedTime; // Tracks the total elapsed time
    private boolean isTimerRunning = true; // Controls whether the stopwatch is active    

    public Game(JFrame window, Character player, Statement stmt, Font cf, Settings settings) {

        this.startTime = System.currentTimeMillis() - player.timer; // Initialize the stopwatch with the saved timer value

        this.stmt = stmt;
        this.player = player;
        this.customFont = cf;
        this.settings = settings;
        this.window = window;

        // set the game board size
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));

        // Load Images and Hitboxes
        level_images = helperMethods.loadLevelsAndStages();
        character_images = helperMethods.loadCharacterImages();
        levels = helperMethods.loadObstacles(player.difficulty);
        enemy_images = helperMethods.loadEnemyImages();
        enemy_projectile_images = helperMethods.loadEnemyProjectileImages();
        merchant_items_images = helperMethods.loadMerchantItemsImages();

        loadImages();

        current_level = player.currentLevelProgress;
        stages = level_images.get(current_level).size();

        camera = new Camera(this.player, new Dimension(1280, 720), new Dimension(player.rect.width, player.rect.height));

        // this timer will call the actionPerformed() method every DELAY ms
        timer = new Timer(DELAY, this);
        timer.start();
    }

    //for timer
    private String formatTime(long millis) {
        int minutes = (int) (millis / 60000); // Convert milliseconds to minutes
        int seconds = (int) ((millis % 60000) / 1000); // Convert remaining milliseconds to seconds
        return String.format("%02d:%02d", minutes, seconds); // Format as MM:SS
    }

    public void pauseTimer() {
        isTimerRunning = false;
    }

    public void resumeTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis() - elapsedTime; // Adjust start time to account for paused time
            isTimerRunning = true;
        }
    }

    private void fadeIn(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Area outter = new Area(new Rectangle(0, 0, GAME_WIDTH, GAME_HEIGHT));
        Ellipse2D.Double inner = new Ellipse2D.Double(player.rect.x - this.camera.offset.x + player.rect.width / 2 - fadeInFrame / 2, player.rect.y + player.rect.height / 2 - fadeInFrame / 2, fadeInFrame, fadeInFrame);
        outter.subtract(new Area(inner));// remove the ellipse from the original area
        g2d.setColor(Color.BLACK);
        g2d.fill(outter);
        fadeInFrame += fadeSpeed;
        if (fadeInFrame >= fadeMaxFrame) {
            fadeInFrame = 0;
            fadeInAnimation = false;
            fadeAnimation = false;
            pause = false;
            if (player.phoneCollected) {
                window.removeMouseListener(this);
                window.removeMouseMotionListener(this);
                window.removeKeyListener(this);
                StartMenu menu = new StartMenu(new Dimension(GAME_WIDTH, GAME_HEIGHT), window, stmt, false);
                window.add(menu);
                window.addMouseListener(menu);
                window.addMouseMotionListener(menu);
                window.addKeyListener(menu);
                window.remove(this);
                window.validate();
            }
        }
    }

    private void fadeOut(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Area outter = new Area(new Rectangle(0, 0, GAME_WIDTH, GAME_HEIGHT));
        Ellipse2D.Double inner = new Ellipse2D.Double(player.rect.x - this.camera.offset.x + player.rect.width / 2 - fadeOutFrame / 2, player.rect.y + player.rect.height / 2 - fadeOutFrame / 2, fadeOutFrame, fadeOutFrame);
        outter.subtract(new Area(inner));// remove the ellipse from the original area
        g2d.setColor(Color.BLACK);
        g2d.fill(outter);
        fadeOutFrame -= fadeSpeed;
        if (fadeOutFrame <= 0) {
            this.player.rect.x = 100;
            if (player.currentLevelProgress == 7) {
                player.fightingBoss = true;
                this.player.rect.x = 1920;
            }
            this.player.rect.y = 300;
            fadeOutFrame = fadeMaxFrame;
            fadeOutAnimation = false;
            fadeInAnimation = true;
        }
    }

    private void resetLevel() {
        // Reset player's position
        this.player.rect.x = 100;
        this.player.rect.y = 300;

        // Reset player's health
        this.player.health = this.player.max_health;

        // Reset player's coins to zero
        this.player.coins = 0;

        // Reset the stopwatch
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = 0;
        this.isTimerRunning = true;

        // Reset potions and other collectibles
        this.player.smallPotion = 0;
        this.player.mediumPotion = 0;
        this.player.bigPotion = 0;
        this.player.speedPotion = 0;
        this.player.goldenBanana = 0;

        // Clear all coins from the level
        coins.clear();

        // Reload obstacles and reset the level
        levels = helperMethods.loadObstacles(player.difficulty);

        // Reset camera offset
        camera.offset.x = 0;
        camera.offset.y = 0;

        // Close any open menus and unpause the game
        pause = false;
        resetLevelMenuPrompt = false;
        openMerchantMenu = false;

        // Stop any ongoing animations or effects
        fadeAnimation = false;
        fadeInAnimation = false;
        fadeOutAnimation = false;

        // Reset boss fight state if applicable
        if (current_level == 7) {
            player.fightingBoss = false;
            bossPhase = 1;
            if (bossObject != null) {
                levels.get(current_level).add(bossObject);
            }
        }
    }

    private void gameUI(Graphics g) {
        g.drawImage(potion_hotkey_image.getSubimage(0, potion_hotkey_image.getHeight() / 2 * ((player.smallPotion > 0) ? 0 : 1), potion_hotkey_image.getWidth() / 5, potion_hotkey_image.getHeight() / 2),
                25, 25, this);
        g.drawImage(potion_hotkey_image.getSubimage(67, potion_hotkey_image.getHeight() / 2 * ((player.mediumPotion > 0) ? 0 : 1), potion_hotkey_image.getWidth() / 5, potion_hotkey_image.getHeight() / 2),
                25 + 67, 25, this);
        g.drawImage(potion_hotkey_image.getSubimage(67 * 2, potion_hotkey_image.getHeight() / 2 * ((player.bigPotion > 0) ? 0 : 1), potion_hotkey_image.getWidth() / 5, potion_hotkey_image.getHeight() / 2),
                25 + 67 * 2, 25, this);
        g.drawImage(potion_hotkey_image.getSubimage(67 * 3, potion_hotkey_image.getHeight() / 2 * ((player.speedPotion > 0) ? 0 : 1), potion_hotkey_image.getWidth() / 5, potion_hotkey_image.getHeight() / 2),
                25 + 67 * 3, 25, this);
        g.drawImage(potion_hotkey_image.getSubimage(66 * 4, potion_hotkey_image.getHeight() / 2 * ((player.goldenBanana > 0) ? 0 : 1), potion_hotkey_image.getWidth() / 5, potion_hotkey_image.getHeight() / 2),
                25 + 67 * 4, 25, this);

        // Draw Hearts
        int heart_offset = 25;
        double hearts = (float) player.health / 2;
        int max_hearts = player.max_health / 2;
        int height_offset = 0;
        for (int cur_heart = 1; cur_heart <= max_hearts; cur_heart++) {
            if (hearts >= 1) {
                height_offset = 0;
            } else if (hearts > 0) {
                height_offset = 1;
            } else {
                height_offset = 2;
            }
            g.drawImage(heart_image.getSubimage(0, height_offset * heart_image.getHeight() / 3, heart_image.getWidth(), heart_image.getHeight() / 3),
                    GAME_WIDTH - (heart_image.getWidth() * (cur_heart)) - heart_offset, heart_offset, // Location of where to draw,
                    this);
            hearts--;
        }
        for (int cur_golden_heart = 1; cur_golden_heart <= player.golden_hearts; cur_golden_heart++) {
            g.drawImage(golden_heart_image, GAME_WIDTH - (golden_heart_image.getWidth() * (cur_golden_heart + max_hearts)) - heart_offset, heart_offset, // Location of where to draw,
                    this);
        }

        // Draw Coin
        g.drawImage(coin_image.getSubimage(0, 0, 64, 64).getScaledInstance(48, 48, Image.SCALE_DEFAULT), GAME_WIDTH - 60, 75, this);
        helperMethods.drawCenteredString(g, "" + player.coins, new Rectangle(GAME_WIDTH - 100, 76, 50, 50), customFont.deriveFont(16f));
    }

    private void resetLevelMenu(Graphics g) {
        g.drawImage(reset_level_menu_image,
                GAME_WIDTH / 2 - reset_level_menu_image.getWidth() / 2,
                GAME_HEIGHT / 2 - reset_level_menu_image.getHeight() / 2,
                this);
    }

    private void merchantMenu(Graphics g) {
        g.drawImage(merchant_menu_image,
                GAME_WIDTH / 2 - merchant_menu_image.getWidth() / 2,
                12,
                this);
        int heightOffset = 0;
        for (String itemName : merchant_items_images.keySet()) {
            int size = itemName.contains("Big") ? 46 : 44;
            Item item = merchant_items_images.get(itemName);
            if (tick % 4 == 0) {
                item.animate = true;
            }
            g.drawImage(item.getImage(), 420, 250 + (size + 17) * heightOffset, size, size, this);
            heightOffset++;
        }
    }

    private void bossFight() {
        if (player.phoneCollected && !fadeAnimation) {
            this.levels.get(this.current_level).remove(phone);
            phone = null;
            fadeAnimation = true;
            fadeOutAnimation = true;
            pause = true;
            return;
        }

        if (player.bossDefeated) {
            this.levels.get(this.current_level).remove(bossObject);
            return;
        }

        ArrayList<Tower> towers = new ArrayList();
        int infected = 0;

        for (Object object : this.levels.get(this.current_level)) {

            if (object instanceof Tower) {
                Tower tower = (Tower) object;
                towers.add(tower);
            }
        }

        if (player.fightingBoss) {
            for (Tower tower : towers) {
                if (tower.infected) {
                    infected++;
                }
            }
            if (infected == 0) {
                player.fightingBoss = false;
                bossPhase++;
                if (bossPhase == 4) {
                    player.bossDefeated = true;
                    phone = new Phone(1920 - phone_image.getWidth(), 673 - phone_image.getHeight(), phone_image.getWidth(), phone_image.getHeight());
                    this.levels.get(this.current_level).add(phone);
                }
            }
            return;
        }

        infected = 0;
        System.out.println(bossPhase);
        while (infected < bossPhase + 1) {
            int rand = (int) Math.random() * towers.size();
            towers.get(rand).setInfected(true);
            towers.remove(towers.get(rand));
            infected++;
        }

        player.fightingBoss = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        current_level = player.currentLevelProgress;
        enemy_projectiles.clear();

        // Player died or Fell off the map
        if ((player.health <= 0 && player.golden_hearts <= 0) || player.rect.y > GAME_HEIGHT) {
            resetLevelMenuPrompt = true;
        }

        if (player.interact) {
            System.out.println((player.interactedWith != null) ? player.interactedWith.getClass() : null);
        }

        if (player.interactedWith instanceof Merchant && player.interact) {
            openMerchantMenu = true;
        }

        if (player.interactedWith instanceof Portal && player.interact) {
            player.currentLevelProgress++;
            coins.clear();
            player.interact = false;
            fadeAnimation = true;
            fadeOutAnimation = true;
            pause = true;
        }

        if (current_level == 7) {
            bossFight();
        }

        if (resetLevelMenuPrompt || openMerchantMenu) {
            pause = true;
        }

        if (!pause) {

            player.update(this.levels.get(this.current_level), this.camera, tick);

            for (Object object : this.levels.get(this.current_level)) {
                if (object instanceof Enemy) {
                    Enemy enemy = (Enemy) object;
                    enemy.update(player, tick, enemy_projectile_images);
                    if (bossObject == null && enemy.id == FINAL_BOSS) {
                        bossObject = object;
                    }
                }
            }

            for (Projectile projectile : player.projectiles) {
                projectile.x += ((projectile.direction == "Right") ? 20 : -20);
            }

            if (player.attack && tick > player.lastAttack + 15) {
                player.projectiles.add(new Projectile(bullet, player.rect.x + player.rect.width, player.rect.y + player.rect.height / 2, bullet.getWidth(), bullet.getHeight(), player.direction));
                player.lastAttack = tick;
            }

            player.checkCollision(this.levels.get(this.current_level));

            Enemy killed_enemy = null;
            Enemy summoned_enemy = null;
            for (Object object : this.levels.get(this.current_level)) {
                if (object instanceof Enemy) {
                    Enemy enemy = (Enemy) object;
                    if (enemy == player.enemyKilled || !enemy.alive) {
                        killed_enemy = enemy;
                        enemiesStatus.put(enemy.id, "Coin");
                        coins.add(new Coin(enemy.x + enemy.width / 2 - 32, enemy.y + enemy.height - 64, 64, 64));
                    }
                    if (enemy.projectile != null) {
                        enemy_projectiles.add(enemy);
                    }
                    summoned_enemy = enemy.summoned;
                }
            }
            if (killed_enemy != null) {
                enemy_projectiles.remove(killed_enemy.projectile);
                this.levels.get(this.current_level).remove(killed_enemy);
            }
            if (summoned_enemy != null) {
                this.levels.get(this.current_level).add(summoned_enemy);
            }

            for (Enemy enemy : enemy_projectiles) {
                if (enemy.projectile != null && player.isHit(enemy.projectile)) {
                    enemy.projectile = null;
                    break;
                }
            }

            for (Object coinObj : coins) {
                Coin coin = (Coin) coinObj;
                if (!coin.collected && !this.levels.get(this.current_level).contains(coinObj)) {
                    this.levels.get(this.current_level).add(coinObj);
                }
            }

            for (Object object : this.levels.get(this.current_level)) {
                if (object instanceof Coin) {
                    Coin coin = (Coin) object;
                    if (coin.collected) {
                        this.levels.get(this.current_level).remove(object);
                        break;
                    }
                }
            }
        }

        // Check if the player is near the portal
        nearPortal = false; // Reset the flag at the start of each frame
        for (Object object : this.levels.get(this.current_level)) {
            if (object instanceof Portal) {
                Portal portal = (Portal) object;
                if (this.player.rect.intersects(portal.getBounds())) {
                    nearPortal = true;
                    break; // Exit loop once we find the portal
                }
            }
        }

        if (isTimerRunning) {
            elapsedTime = System.currentTimeMillis() - startTime; // Calculate elapsed time in milliseconds
        }

        repaint();
        playSoundEffect();
        tick++;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.camera.follow(level_images.get(current_level).size() * 3840);

        // Draw stage levels
        for (int i = 0; i < level_images.get(current_level).size(); i++) {
            g.drawImage(level_images.get(current_level).get(i), 3840 * i - this.camera.offset.x, 0, this);
        }

        // DRAW OBSTACLES AND ENEMIES
        for (Object object : this.levels.get(this.current_level)) {
            Rectangle rect = (Rectangle) object;
            if (showHitbox) {
                g.drawRect(rect.x - this.camera.offset.x, rect.y, rect.width, rect.height);
            }
            if (object instanceof Enemy) {
                Enemy enemy = (Enemy) object;

                enemy.render(g, camera, enemy_images, this);

                if (showHitbox) {
                    g.drawLine(enemy.x - this.camera.offset.x + (enemy.direction.equals("Right") ? enemy.width : 0),
                            enemy.y,
                            enemy.x - this.camera.offset.x + (enemy.direction.equals("Right") ? enemy.projectileMaxDist : -enemy.projectileMaxDist),
                            enemy.y);
                }

                if (enemy.attackMode == Hostile && showHitbox) {
                    g.drawRect(enemy.x - enemy.hostilityRange - this.camera.offset.x, enemy.y, enemy.hostilityRange, enemy.height);
                }

                if (enemy.projectile != null) {
                    if (enemy.id != FINAL_BOSS) {
                        g.drawImage(enemy.projectile.image, enemy.projectile.x - this.camera.offset.x, enemy.projectile.y, this);
                    } else {
                        if (tick % 4 == 0) {
                            enemy.projectile.frame++;
                        }
                        if (enemy.projectile.frame > enemy.projectile.image.getWidth() / 52 - 1) {
                            enemy.projectile.frame = 0;
                        }
                        g.drawImage(enemy.projectile.image.getSubimage(52 * enemy.projectile.frame, 0, 52, 72),
                                enemy.projectile.x - this.camera.offset.x, enemy.projectile.y, this);
                        if (showHitbox) {
                            g.drawRect(enemy.projectile.x - this.camera.offset.x, enemy.projectile.y, enemy.projectile.width, enemy.projectile.height);
                        }
                    }
                }

            } else if (object instanceof Coin) {
                Coin coin = (Coin) object;
                g.drawImage(coin.getFrame(coin_image, tick), coin.x - this.camera.offset.x, coin.y, this);
            } else if (object instanceof Tower) {
                Tower tower = (Tower) object;
                g.drawImage(((tower.infected) ? tower_gif : tower_image), tower.x - this.camera.offset.x, tower.y - ((tower.infected) ? 110 : 0), this);
            }
        }

        for (Projectile projectile : player.projectiles) {
            g.drawImage(projectile.image,
                    projectile.x - this.camera.offset.x + ((projectile.direction == "Left") ? projectile.width : 0),
                    projectile.y,
                    ((projectile.direction == "Right") ? projectile.width : -projectile.width),
                    projectile.height, this);

            // Hitbox
            if (showHitbox) {
                g.drawRect(projectile.x - this.camera.offset.x, projectile.y, projectile.image.getWidth(), projectile.image.getHeight());
            }
        }

        if (phone != null) {
            g.drawImage(phone_image, phone.x - this.camera.offset.x, phone.y, this);
        }

        // Draw Player
        player.render(g, this.camera, this.character_images, this);

        gameUI(g);

        // Display floating text if near the portal
        if (nearPortal) {
            g.setFont(customFont.deriveFont(14f)); // Set font size
            g.setColor(Color.WHITE); // Set text color
            String text = "Press F to enter portal";
            int textX = GAME_WIDTH / 2 - g.getFontMetrics().stringWidth(text) / 2; // Center horizontally
            int textY = GAME_HEIGHT - 50; // Position near the bottom of the screen

            g.drawString(text, textX, textY);
        }

        if (resetLevelMenuPrompt) {
            resetLevelMenu(g);
        } else if (openMerchantMenu) {
            merchantMenu(g);
        }

        if (showHitbox) {
            g.drawOval(mouse_pos.x - 2, mouse_pos.y - 2, 4, 4);
        }

        if (fadeAnimation) {
            if (fadeOutAnimation) {
                fadeOut(g);
            } else if (fadeInAnimation) {
                fadeIn(g);
            }
        }

        g.setColor(Color.red);

        Toolkit.getDefaultToolkit().sync();

        // Always display the stopwatch
        g.setFont(customFont.deriveFont(18f)); // Set font size
        g.setColor(Color.WHITE); // Set text color
        String stopwatchText = "Time: " + formatTime(elapsedTime);
        int textX = GAME_WIDTH / 2 - g.getFontMetrics().stringWidth(stopwatchText) / 2;
        int textY = 40; // Position near the top of the screen
        g.drawString(stopwatchText, textX, textY);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void playSoundEffect() {
        int SE = player.playSoundEffect;
        if (SE == 0) {
            return;
        }
        try {
            AudioInputStream aui = AudioSystem.getAudioInputStream(soundEffects.get(SE));
            try {
                clip = AudioSystem.getClip();
                clip.open(aui);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue((float) (settings.sound * 0.86 - 80));
            } catch (IOException | LineUnavailableException ex) {
            }
        } catch (IOException | UnsupportedAudioFileException exx) {
        }
        clip.start();
        player.playSoundEffect = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_N) {
            player.currentLevelProgress++;
            save();
        }

        if (key == KeyEvent.VK_G) {
            player.coins++;
        }

        if (key == KeyEvent.VK_ESCAPE) {
            resetLevelMenuPrompt = true;
        }

        if (gameOver) {
            return;
        }

        if (key == KeyEvent.VK_W) {
        } else if (key == KeyEvent.VK_D) {
            player.right = true;
        } else if (key == KeyEvent.VK_A) {
            player.left = true;
        } else if (key == KeyEvent.VK_S) {
        } else if (key == KeyEvent.VK_SPACE) {
            player.jump = true;
        } else if (key == KeyEvent.VK_T) {
            player.attack = true;
        } else if (key == KeyEvent.VK_F && nearPortal) {
            player.interact = true;
        } else if (key == KeyEvent.VK_1) {
            if (player.smallPotion > 0 && player.health < player.max_health) {
                player.health++;
                player.smallPotion--;
            }
        } else if (key == KeyEvent.VK_2) {
            if (player.smallPotion > 0 && player.health < player.max_health) {
                player.health += 2;
                player.mediumPotion--;
            }
        } else if (key == KeyEvent.VK_3) {
            if (player.bigPotion > 0 && player.health < player.max_health) {
                player.health += 4;
                player.bigPotion--;
            }
        } else if (key == KeyEvent.VK_4) {
            if (player.speedPotion > 0) {
                player.spedUp = true;
                player.lastSpedUp = tick;
                player.speedPotion--;
            }
        } else if (key == KeyEvent.VK_5) {
            if (player.goldenBanana > 0) {
                player.golden_hearts++;
                player.goldenBanana--;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameOver) {
            return;
        }

        if (key == KeyEvent.VK_W) {
        } else if (key == KeyEvent.VK_D) {
            player.right = false;
        } else if (key == KeyEvent.VK_A) {
            player.left = false;
        } else if (key == KeyEvent.VK_S) {
        } else if (key == KeyEvent.VK_SPACE) {
            player.jump = false;
        } else if (key == KeyEvent.VK_T) {
            player.attack = false;
        } else if (key == KeyEvent.VK_1) {
            player.heal = false;
        } else if (key == KeyEvent.VK_F) {
            player.interact = false;
        }
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
        if (tick < lastClick + 2) {
            return;
        }
        lastClick = tick;
        HashMap<String, Rectangle> buttons;
        if (resetLevelMenuPrompt) {
            buttons = new HashMap<>() {
                {
                    put("Back To Menu", new Rectangle(445, 441, 187, 36));
                    put("Reset", new Rectangle(650, 442, 185, 36));
                }
            };
            // hehehehehehehe
            for (String buttonName : buttons.keySet()) {
                Rectangle bp = buttons.get(buttonName);
                if (mouse_pos.x >= bp.x && mouse_pos.x <= bp.x + bp.width
                        && mouse_pos.y >= bp.y && mouse_pos.y <= bp.y + bp.height) {

                    if (buttonName.equals("Back To Menu")) {
                        save(); // Save the player's progress before returning to the menu

                        // Remove the current Game panel and switch to StartMenu
                        window.removeMouseListener(this);
                        window.removeMouseMotionListener(this);
                        window.removeKeyListener(this);

                        // Pass the `stmt` object to the new StartMenu instance
                        StartMenu menu = new StartMenu(new Dimension(GAME_WIDTH, GAME_HEIGHT), window, stmt, false);
                        window.add(menu);
                        window.addMouseListener(menu);
                        window.addMouseMotionListener(menu);
                        window.addKeyListener(menu);

                        window.remove(this);
                        window.validate();
                    } else if (buttonName.equals("Reset")) {
                        resetLevel();
                    }
                }
            }
        } else if (openMerchantMenu) {
            buttons = new HashMap<>() {
                {
                    put("Close", new Rectangle(GAME_WIDTH / 2 - 65, 12 + merchant_menu_image.getHeight() - 53, 130, 26));
                }
            };

            int heightOffset = 0;
            for (String itemName : merchant_items_images.keySet()) {
                buttons.put(itemName + "Buy", new Rectangle(766, 280 + (58) * heightOffset, 50, 15));
                buttons.put(itemName + "Sell", new Rectangle(833, 280 + (58) * heightOffset, 50, 15));
                heightOffset++;
            }
            for (String buttonName : buttons.keySet()) {
                Rectangle bp = buttons.get(buttonName);
                if (mouse_pos.x >= bp.x
                        && mouse_pos.x <= bp.x + bp.width
                        && mouse_pos.y <= bp.y + bp.height
                        && mouse_pos.y >= bp.y) {
                    if (buttonName.equals("Close")) {
                        pause = false;
                        openMerchantMenu = false;
                    } else if (buttonName.contains("Buy")) {
                        Item item = merchant_items_images.get(buttonName.substring(0, buttonName.length() - 3));
                        int coins = player.coins;

                        System.out.println(item.buyPrice);

                        if (coins < item.buyPrice) {
                            return;
                        }

                        if (buttonName.contains("Small")) {
                            player.smallPotion++;
                        } else if (buttonName.contains("Medium")) {
                            player.mediumPotion++;
                        } else if (buttonName.contains("Big")) {
                            player.bigPotion++;
                        } else if (buttonName.contains("Speed")) {
                            player.speedPotion++;
                        } else {
                            player.goldenBanana++;
                        }

                        player.coins -= item.buyPrice;

                    } else if (buttonName.contains("Sell")) {
                        Item item = merchant_items_images.get(buttonName.substring(0, buttonName.length() - 4));

                        if (buttonName.contains("Small") && player.smallPotion > 0) {
                            player.smallPotion--;
                            player.coins += item.sellPrice;
                        } else if (buttonName.contains("Medium") && player.mediumPotion > 0) {
                            player.mediumPotion--;
                            player.coins += item.sellPrice;
                        } else if (buttonName.contains("Big") && player.bigPotion > 0) {
                            player.bigPotion--;
                            player.coins += item.sellPrice;
                        } else if (buttonName.contains("Speed") && player.speedPotion > 0) {
                            player.speedPotion--;
                            player.coins += item.sellPrice;
                        } else if (buttonName.contains("Golden") && player.goldenBanana > 0) {
                            player.goldenBanana--;
                            player.coins += item.sellPrice;
                        }

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

    private void save() {
        if (stmt == null) {
            System.out.println("Database statement is null. Unable to save.");
            return;
        }

        try {
            stmt.execute("DELETE FROM characters WHERE name='" + player.name + "'");
            stmt.execute("INSERT INTO characters(name, difficulty, level, strength, agility, vitality, coins, smallPotion, mediumPotion, bigPotion, speedPotion, goldenBanana, level_progress, timer) VALUES('"
                    + player.name + "', '"
                    + player.difficulty + "', "
                    + player.level + ", "
                    + player.strength + ", "
                    + player.agility + ", "
                    + player.vitality + ", "
                    + player.coins + ", "
                    + player.smallPotion + ", "
                    + player.mediumPotion + ", "
                    + player.bigPotion + ", "
                    + player.speedPotion + ", "
                    + player.goldenBanana + ", "
                    + player.currentLevelProgress + ", "
                    + elapsedTime + ")"); // Save the elapsed time
        } catch (SQLException SQLError) {
            System.out.println("Unable to execute statement!");
            SQLError.printStackTrace();
        }
    }

    public void loadImages() {
        String filepath = "src/finalproject/img/";
        try {
            soundEffects.put(COIN, new File("src/finalproject/audio/coin.wav"));
            soundEffects.put(HURT, new File("src/finalproject/audio/hurt.wav"));
            soundEffects.put(JUMP, new File("src/finalproject/audio/jump.wav"));
            merchant_menu_image = ImageIO.read(new File(filepath + "/UIs/merchant_menu.png"));
            bullet = ImageIO.read(new File(filepath + "Spider-Shoot.png"));
            heart_image = ImageIO.read(new File(filepath + "heart.png"));
            golden_heart_image = ImageIO.read(new File(filepath + "golden_heart.png"));
            potion_hotkey_image = ImageIO.read(new File(filepath + "consumables_hotkey.png"));
            coin_image = ImageIO.read(new File(filepath + "coin.png"));
            reset_level_menu_image = ImageIO.read(new File(filepath + "/UIs/reset_level_menu.png"));
            tower_gif = new ImageIcon(filepath + "testa.gif").getImage();
            tower_image = ImageIO.read(new File(filepath + "/tower.png"));
            phone_image = ImageIO.read(new File(filepath + "/phone.png"));
        } catch (IOException ex) {
            System.out.println(System.getProperty("user.dir"));
        }
    }

}
