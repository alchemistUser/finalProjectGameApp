package finalproject;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import static finalproject.Constants.*;

/*
Level_1 Slime: https://rvros.itch.io/pixel-art-animated-slime

Boss Gameplay:

> Phase 1
- Floating
- You let the boss attack "Pillars" that makes the boss float, destroy 4 of these and 

 */
public class HelperMethods {

    public HashMap loadLevelsAndStages() {
        HashMap<Integer, ArrayList<BufferedImage>> level_images = new HashMap();

        File levels_dir = new File("src/finalproject/img/levels");

        if (levels_dir.isDirectory()) {
            String levels[] = levels_dir.list();

            if (levels.length > 0) {
                for (int i = 0; i < levels.length; i++) {
                    ArrayList<BufferedImage> stage_images = new ArrayList();

                    if (levels[i].contains("DS")) {
                        continue;
                    }
                    if (new File("src/finalproject/img/levels/" + levels[i]).isDirectory()) { // Level and Stages
                        String stagesArr[] = new File("src/finalproject/img/levels/" + levels[i]).list();
                        if (stagesArr.length > 0) {
                            for (int j = 0; j < stagesArr.length; j++) {
                                try {
                                    BufferedImage im = ImageIO.read(new File("src/finalproject/img/levels/" + levels[i] + "/stage_" + (j + 1) + ".png"));
                                    stage_images.add(im);
                                } catch (IOException ex) {
                                    System.out.println("Levels and Stages: " + stagesArr[i]);
                                }
                            }
                            level_images.put(Integer.parseInt("" + levels[i].charAt(levels[i].length() - 1)), stage_images);
                        }
                    } else { // Merchant
                        try {
                            BufferedImage im = ImageIO.read(new File("src/finalproject/img/levels/" + levels[i]));
                            stage_images.add(im);
                        } catch (IOException ex) {
                            System.out.println("Merchant: " + levels[i]);
                        }
                        level_images.put(Integer.parseInt("" + levels[i].charAt(levels[i].length() - 5)), stage_images);
                    }
                }
            }
        }
        return level_images;
    }

    public HashMap loadCharacterImages() {
        HashMap<String, BufferedImage> char_ims = new HashMap();

        File char_dir = new File("src/finalproject/img/character");

        String charsArr[] = char_dir.list();

        for (int i = 0; i < charsArr.length; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/finalproject/img/character/" + charsArr[i]));
                char_ims.put(charsArr[i], img);
            } catch (IOException ex) {
                System.out.println("Character Images: " + charsArr[i]);
            }
        }
        return char_ims;
    }

    public HashMap loadEnemyImages() {
        HashMap<Integer, BufferedImage> imgs = new HashMap();

        File dir = new File("src/finalproject/img/enemies");
        String arr[] = dir.list();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].contains("DS")) {
                continue;
            }

            if (new File("src/finalproject/img/enemies/" + arr[i]).isDirectory()) {
                continue;
            }

            try {
                BufferedImage img = ImageIO.read(new File("src/finalproject/img/enemies/" + arr[i]));
                imgs.put(Integer.parseInt("" + arr[i].charAt(arr[i].length() - 5)), img);
            } catch (IOException ex) {
                System.out.println("Enemy Images: " + arr[i]);
            }
        }

        return imgs;
    }

    public HashMap loadEnemyProjectileImages() {
        HashMap<Integer, BufferedImage> imgs = new HashMap();

        File dir = new File("src/finalproject/img/projectiles");
        String arr[] = dir.list();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].contains("DS")) {
                continue;
            }
            try {
                BufferedImage img = ImageIO.read(new File("src/finalproject/img/projectiles/" + arr[i]));
                imgs.put(Integer.parseInt("" + arr[i].charAt(arr[i].length() - 5)), img);
            } catch (IOException ex) {
                System.out.println("Enemy Projectile Images: " + arr[i]);
            }
        }

        return imgs;
    }

    public HashMap loadMerchantItemsImages() {
        HashMap<String, Item> imgs = new HashMap();

        File dir = new File("src/finalproject/img/merchant_items");
        String arr[] = dir.list();

        int bP;
        int sP;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].contains("DS")) {
                continue;
            }
            try {
                BufferedImage img = ImageIO.read(new File("src/finalproject/img/merchant_items/" + arr[i]));
                String itemName = arr[i].substring(0, arr[i].length() - 4);
                if (itemName.contains("Small")) {
                    bP = 3;
                    sP = 1;
                } else if (itemName.contains("Medium")) {
                    bP = 5;
                    sP = 2;
                } else if (itemName.contains("Big")) {
                    bP = 9;
                    sP = 3;
                } else if (itemName.contains("Speed")) {
                    bP = 6;
                    sP = 2;
                } else {
                    bP = 10;
                    sP = 4;
                }
                Item item = new Item(img, itemName, bP, sP);
                imgs.put(itemName, item);
            } catch (IOException ex) {
                System.out.println("Enemy Projectile Images: " + arr[i]);
            }
        }

        return imgs;
    }

    public void updateSave(Statement stmt, int cID, String charName, HashMap<Integer, String> enemies) {
        try {
            stmt.execute("UPDATE characters SET checkpoint=" + cID + "WHERE name='" + charName + "';");
            for (Integer enemyID : enemies.keySet()) {
                stmt.execute("INSERT INTO enemies_killed(characterName, checkpointID, enemyID, enemyStatus) VALUES('" + charName + "'," + cID + "," + enemyID + ",'" + enemies.get(enemyID) + "');");
            }
        } catch (SQLException e) {
            System.out.println("Checkpoint saving error!");
        }
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public HashMap loadObstacles(String difficulty) {
        float diffMultiplier = (difficulty.equals("Easy") ? 1f : difficulty.equals("Normal") ? 1.5f : 2f);
        
        HashMap<Integer, ArrayList<Object>> levels = new HashMap<>();
        levels.put(1, new ArrayList<Object>() {
            {
                // Stage 1
                // Floor
                add(new Floor(0, 604, 3840, 116));

                // Obstacles
                add(new Obstacle(1444, 361, 142, 244));
                add(new Obstacle(695, 227, 182, 60, 5));
                add(new Obstacle(1061, 284, 182, 60));
                add(new Obstacle(1729, 207, 182, 60));
                add(new Obstacle(2064, 417, 86, 187));
                add(new Obstacle(2635, 416, 86, 188));

                // Enemies
                add(new Enemy(PLANT, 966, 495, 77, 109, 8, 400, RangedFixed));
                add(new Enemy(SLIME, 1615, 553, 113, 51, new Point(1615, 553), new Point(1920, 553), -8, Moving));
                add(new Enemy(PLANT, 2070, 309, 77, 109, 8, 400, RangedFixed));
                add(new Enemy(SLIME, 3054, 553, 113, 51, new Point(3054, 553), new Point(3540, 553), -8, Moving));

                // Stage 2
                // Floor
                add(new Floor(3840, 604, 1266, 116));
                add(new Floor(3840 + 3530, 604, 310, 116));

                // Obstacles
                add(new Obstacle(3840 + 394, 414, 85, 190));
                add(new Obstacle(3840 + 906, 142, 247, 81));
                add(new Obstacle(3840 + 1079, 417, 86, 186));
                add(new Obstacle(3840 + 1443, 488, 183, 60));
                add(new Obstacle(3840 + 1842, 565, 142, 162));
                add(new Obstacle(3840 + 2203, 489, 196, 64));
                add(new Obstacle(3840 + 2532, 491, 248, 81));
                add(new Obstacle(3840 + 2898, 538, 142, 182));
                add(new Obstacle(3840 + 3161, 449, 246, 81));

                // Coins
                add(new Coin(3840 + 999, 72, 64, 64));
                add(new Coin(3840 + 1090, 349, 64, 64));
                add(new Coin(3840 + 3256, 381, 64, 64));

                // Enemies
                add(new Enemy(PLANT, 3840 + 1880, 444, 77, 120, 8, 400, RangedFixed));
//            add(new Enemy(SLIME, 3840+2540, 444, 113, 51, new Point(3840+2540, 444), new Point(3840+2662, 444), -8, Moving));
                add(new Enemy(PLANT, 3840 + 2930, 418, 77, 120, 10, 400, RangedFixed));

                // Stage 3
                // Floor
                add(new Floor(3840 * 2, 604, 615, 116));
                add(new Floor(3840 * 2 + 3530, 604, 310, 116));

                // Obstacles
                add(new Obstacle(3840 * 2 + 496, 395, 183, 60));
                add(new Obstacle(3840 * 2 + 821, 275, 183, 60));
                add(new Obstacle(3840 * 2 + 1062, 474, 659, 59));
                add(new Obstacle(3840 * 2 + 1854, 610, 182, 60));
                add(new Obstacle(3840 * 2 + 1932, 289, 659, 59));
                add(new Obstacle(3840 * 2 + 2218, 576, 183, 60));
                add(new Obstacle(3840 * 2 + 2527, 502, 142, 218));
                add(new Obstacle(3840 * 2 + 2745, 545, 660, 59));

                // Enemies
                add(new Enemy(PLANT, 3840 * 2 + 2560, 385, 77, 120, 8, 400, RangedFixed));
                add(new Enemy(HARLEY, 3840 * 2 + 1540, 315, 69, 160, 400, 10, new Point(3840 * 2 + 1540, 315), Hostile).setDirection("Left"));
                add(new Enemy(HARLEY, 3840 * 2 + 2450, 127, 69, 160, 425, 10, new Point(3840 * 2 + 2450, 127), Hostile).setDirection("Left"));
                add(new Enemy(HARLEY, 3840 * 2 + 3275, 383, 69, 160, 425, 10, new Point(3840 * 2 + 3275, 383), Hostile).setDirection("Left"));

                // Portal
                add(new Portal(3840 * 2 + 3667, 368, 152, 217, 2));
            }
        });
        levels.put(3, new ArrayList<Object>() {
            {
                // Stage 1
                // Floor
                add(new Floor(0, 604, 3840, 116));

                // Obstacles
                add(new Obstacle(683, 313, 353, 20));
                add(new Obstacle(449, 264, 142, 20));
                add(new Obstacle(18, 225, 352, 21));
                add(new Obstacle(1232, 371, 143, 21));
                add(new Obstacle(1343, 265, 143, 21));
                add(new Obstacle(1531, 161, 361, 21));

                // Big Ass Rock
                add(new Obstacle(1478, 577, 654, 27));
                add(new Obstacle(1493, 547, 622, 29));
                add(new Obstacle(1509, 528, 606, 25));
                add(new Obstacle(1525, 490, 579, 35));
                add(new Obstacle(1549, 444, 526, 45));
                add(new Obstacle(1579, 415, 481, 29));
                add(new Obstacle(1596, 387, 434, 29));
                add(new Obstacle(1612, 354, 372, 33));
                add(new Obstacle(1657, 340, 312, 16));
                add(new Obstacle(1672, 324, 282, 17));
                add(new Obstacle(1687, 309, 252, 17));
                add(new Obstacle(1702, 294, 222, 17));
                add(new Obstacle(1717, 279, 176, 17));
                add(new Obstacle(1732, 266, 122, 14));
                add(new Obstacle(1763, 251, 61, 15));

                // Enemies
                add(new Enemy(SKELETON_ARCHER, 228, 45, 123, 180, 12, 1100, RangedFixed).setDirection("Right"));
                add(new Enemy(SKELETON_WARRIOR, 1070, 475, 137, 129, new Point(570, 475), new Point(1281, 475), -12, Moving));
                add(new Enemy(SKELETON_WARRIOR, 2900, 475, 137, 129, new Point(2364, 475), new Point(3025, 475), -12, Moving));
                add(new Enemy(SKELETON_ARCHER, 3222, 424, 123, 180, 12, 800, RangedFixed));

                // Stage 2
                // Floor
                add(new Floor(3840, 604, 1232, 116));
                add(new Floor(3840 + 1901, 604, 1939, 116));

                // Obstacles
                add(new Obstacle(3840 + 22, 245, 352, 21));
                add(new Obstacle(3840 + 442, 336, 143, 21));
                add(new Obstacle(3840 + 717, 406, 143, 21));
                add(new Obstacle(3840 + 1361, 492, 143, 21));
                add(new Obstacle(3840 + 1732, 471, 143, 21));

                // Enemies
                add(new Enemy(SKELETON_ARCHER, 3840 + 22, 65, 123, 180, 12, 350, RangedFixed).setDirection("Right"));
                add(new Enemy(SKELETON_ARCHER, 3840 + 1740, 291, 123, 180, 12, 250, RangedFixed));
                add(new Enemy(SKELETON_WARRIOR, 3840 + 2900, 475, 137, 129, new Point(3840 + 2371, 475), new Point(3840 + 3037, 475), -12, Moving));
                add(new Enemy(SKELETON_ARCHER, 3840 + 3250, 424, 123, 180, 12, 750, RangedFixed));
                add(new Enemy(SKELETON_ARCHER, 3840 + 3375, 424, 123, 180, 12, 850, RangedFixed));

                // Stage 3
                // Floor
                add(new Floor(3840 * 2, 604, 3840, 116));

                // Enemy
                add(new Enemy(BATMAN, 3840 * 2 + 3213, 463, 117, 141, 16, 2100, RangedFixed));

                // Portal
                add(new Portal(3840 * 2 + 3667, 388, 152, 217, 4));
            }
        });
        levels.put(5, new ArrayList<Object>() {
            {
                // Stage 1
                // Floor
                add(new Floor(0, 628, 1154, 92));
                add(new Floor(2980, 628, 860, 92));

                // Obstacles
                add(new Obstacle(559, 463, 209, 61));
                add(new Obstacle(909, 266, 264, 79));
                add(new Obstacle(1265, 362, 306, 89));
                add(new Obstacle(1788, 593, 450, 30));
                add(new Obstacle(1670, 213, 721, 85));
                add(new Obstacle(2549, 370, 262, 73));
                add(new Obstacle(2925, 456, 261, 76));

                // Enemies
                add(new Enemy(IMP, 1500, 272, 76, 88, 12, 250, RangedFixed));
                add(new Enemy(FIRE_SLIME, 700, 552, 60, 76, new Point(415, 552), new Point(1043, 552), -12, Moving));
                add(new Enemy(IMP, 2305, 127, 76, 88, 12, 600, RangedFixed));
                add(new Enemy(IMP, 1800, 504, 76, 88, 12, 300, RangedFixed).setDirection("Right"));
                add(new Enemy(IMP, 2160, 504, 76, 88, 12, 300, RangedFixed));
                add(new Enemy(FIRE_SLIME, 3500, 552, 60, 76, new Point(3200, 552), new Point(3690, 552), -12, Moving));

                // Coins
                // Stage 2
                // Floor
                add(new Floor(3840, 628, 3840, 92));

                // Obstacles
                add(new Obstacle(3840 + 884, 400, 206, 63));
                add(new Obstacle(3840 + 1209, 308, 568, 89));
                add(new Obstacle(3840 + 1792, 209, 414, 47));
                add(new Obstacle(3840 + 2257, 128, 561, 86));

                // Enemies
                add(new Enemy(IMP, 3840 + 1680, 225, 76, 88, 12, 400, RangedFixed));
                add(new Enemy(FIRE_SLIME, 3840 + 1500, 552, 60, 76, new Point(3840 + 1163, 552), new Point(3840 + 1880, 552), -12, Moving));
                add(new Enemy(IMP, 3840 + 2722, 32, 76, 88, 12, 400, RangedFixed));
                add(new Enemy(FIRE_SLIME, 3840 + 1495, 233, 60, 76, new Point(3840 + 1259, 233), new Point(3840 + 1595, 233), -12, Moving));
                add(new Enemy(FIRE_SLIME, 3840 + 2035, 132, 60, 76, new Point(3840 + 1822, 132), new Point(3840 + 2135, 132), -12, Moving));
                add(new Enemy(FIRE_SLIME, 3840 + 2425, 51, 60, 76, new Point(3840 + 2292, 51), new Point(3840 + 2625, 51), -12, Moving));
                add(new Enemy(FIRE_SLIME, 3840 + 2535, 515, 60, 76, new Point(3840 + 2255, 515), new Point(3840 + 2922, 515), -12, Moving));
                add(new Enemy(FIRE_SLIME, 3840 + 2135, 132, 60, 76, new Point(3840 + 1822, 132), new Point(3840 + 2135, 132), -12, Moving));

                // Coins
                // Stage 3
                // Floor
                add(new Floor(3840 * 2, 628, 512, 92));

                // Obstacles
                add(new Obstacle(3840 * 2 + 586, 512, 211, 63));
                add(new Obstacle(3840 * 2 + 921, 344, 212, 64));
                add(new Obstacle(3840 * 2 + 1116, 200, 724, 84));
                add(new Obstacle(3840 * 2 + 1805, 323, 212, 64));
                add(new Obstacle(3840 * 2 + 2005, 445, 566, 92));
                add(new Obstacle(3840 * 2 + 2705, 406, 209, 60));
                add(new Obstacle(3840 * 2 + 3128, 427, 211, 62));
                add(new Obstacle(3840 * 2 + 3437, 312, 403, 94));

                // Enemies
                add(new Enemy(FIRE_SLIME, 3840 * 2 + 200, 553, 60, 76, new Point(3840 * 2 + 62, 553), new Point(3840 * 2 + 411, 553), -12, Moving));
                add(new Enemy(IMP, 3840 * 2 + 1063, 254, 76, 88, 12, 150, RangedFixed));
                add(new Enemy(FIRE_SLIME, 3840 * 2 + 1800, 125, 60, 76, new Point(3840 * 2 + 1160, 125), new Point(3840 * 2 + 1562, 125), -12, Moving));
                add(new Enemy(IMP, 3840 * 2 + 1751, 109, 76, 88, 12, 550, RangedFixed));
                add(new Enemy(FIRE_SLIME, 3840 * 2 + 2200, 363, 60, 76, new Point(3840 * 2 + 2043, 363), new Point(3840 * 2 + 2485, 363), -12, Moving));
                add(new Enemy(IMP, 3840 * 2 + 2818, 322, 76, 88, 12, 300, RangedFixed));
                add(new Enemy(IMP, 3840 * 2 + 3262, 342, 76, 88, 12, 400, RangedFixed));

                // Portal
                add(new Portal(3840 * 2 + 3667, 102, 152, 217, 6));
            }
        });
        levels.put(2, new ArrayList<Object>() {
            {
                // Floor
                add(new Floor(0, 610, 3840, 110));

                // Merchant
                add(new Merchant(1871, 458, 99, 152));

                // Portal
                add(new Portal(2800, 368, 152, 217, 3));
            }
        });
        levels.put(4, new ArrayList<Object>() {
            {
                // Floor
                add(new Floor(0, 610, 3840, 110));

                // Merchant
                add(new Merchant(1871, 458, 99, 152));

                // Portal
                add(new Portal(2800, 368, 152, 217, 5));
            }
        });
        levels.put(6, new ArrayList<Object>() {
            {
                add(new Floor(0, 610, 3840, 110));

                add(new Portal(1887, 458, 83, 151, 7));
            }
        });
        levels.put(7, new ArrayList<Object>() {
            {
                add(new Floor(0, 673, 3840, 110));

                add(new Tower(925, 465, 148, 209));
                add(new Tower(1609, 465, 148, 209));
                add(new Tower(2375, 465, 148, 209));
                add(new Tower(3086, 465, 148, 209));

                add(new Enemy(FINAL_BOSS, 2400, 0, 250, 250, 16, BOSS));
            }
        });

        for (int lvl : levels.keySet()) {
            for (Object obj : levels.get(lvl)) {
                if (!(obj instanceof Enemy)) {
                    continue;
                }

                Enemy enemy = (Enemy) obj;
                enemy.setDifficulty(diffMultiplier);
            }
        }

        return levels;
    }

    public static BufferedImage loadLeaderboardBackgroundImage() {
        try {
            return ImageIO.read(new File("src/finalproject/img/UIs/leaderboards_ui_menu.png"));
        } catch (Exception e) {
            System.out.println("Unable to load leaderboard UI image.");
            e.printStackTrace();
            return null;
        }
    }
}
