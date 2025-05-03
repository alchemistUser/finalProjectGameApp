package finalproject;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.sql.Statement;
import java.sql.SQLException;

import static finalproject.Constants.*;

public class Character {

    // Stats
    public String name;
    public String difficulty;
    public int level;
    public int health;
    public int max_health;
    public int strength;
    public int agility;
    public int vitality; // 1 vitality = 2 health
    public int coins;
    public long timer = 0; // Tracks the elapsed time in milliseconds

    public int smallPotion;
    public int mediumPotion;
    public int bigPotion;
    public int speedPotion;
    public int goldenBanana;

    public int currentLevelProgress;

    public int score;

    public HashMap<Item, Integer> inventory = new HashMap();

    // Inputs
    public boolean left, right, jump, attack, heal, interact;

    // Positional and Movement properties
    public Rectangle rect;  // Stores x, y position and w, h sizes
    public String direction = "Right";
    private boolean inAir;
    private final float playerSpeed = 14f;
    private float airSpeed = 0f;
    private final float jumpSpeed = -50f;
    private final float gravity = 5f;
    private final float bounceSpeed = -25f;
    private final float knockbackSpeed = 5f;
    private final float knockbackAirSpeed = -25f;

    // Gameplay properties
    public String state = "idle";
    public ArrayList<Projectile> projectiles = new ArrayList();
    public Enemy enemyKilled = null;
    private boolean walkAnimation = false;
    private boolean idleAnimation = false;
    private int idling = 1;
    private int walking = 1;
    public int playSoundEffect = 0;
    public Object interactedWith;
    private boolean hurt = false;

    public int golden_hearts = 0;
    public boolean fightingBoss = false;
    public boolean bossDefeated = false;
    public boolean phoneCollected = false;
    public boolean spedUp = false;
    public int lastSpedUp = -1;
    public boolean canSpeedUp = false;

    // Action delays
    public int lastAttack = -1;
    public int lastHeal = -1;

    // Character Constructor
    public Character(Rectangle rect, String name, String diff, int lvl, int str, int agi, int vit, int coins, int smallP, int Medp, int bp, int speep, int gB, int LP) {
        this.rect = rect;
        this.name = name;
        this.difficulty = diff;
        this.level = lvl;
        this.strength = str;
        this.agility = agi;
        this.vitality = vit;
        this.coins = coins;
        this.smallPotion = smallP;
        this.mediumPotion = Medp;
        this.bigPotion = bp;
        this.speedPotion = speep;
        this.goldenBanana = gB;
        this.currentLevelProgress = LP;

        // Initialize health and max health
        this.max_health = vit * 2;
        this.health = this.max_health;

        // Initialize score
        this.score = 0;
    }

    public void interactWithMerchant() {
        // open merchant menu
        System.out.println("OPEN MERCHANT MENU");
        interact = false;
    }

    public void move(ArrayList objects, Camera camera) {

        if (jump) {
            jump();
        }

        if (!inAir) {
            if (isInAir(objects)) {
                inAir = true;
            }
        }

        if (!inAir) {
            state = "idle";
        }

        if (!left && !right && !inAir) {
            state = "idle";
            return;
        }

        float xSpeed = 0;

        if (left && !hurt) {
            xSpeed -= (playerSpeed + 2 * (agility - 1)) * ((spedUp) ? 1.3 : 1);
            this.direction = "Left";
        }
        if (right && !hurt) {
            xSpeed += (playerSpeed + 2 * (agility - 1)) * ((spedUp) ? 1.3 : 1);
            this.direction = "Right";
        }

        if (hurt) {
            xSpeed += (direction.equals("Right") ? -knockbackSpeed : knockbackSpeed);
        }

        if ((left || right) && !inAir) {
            state = "walking";
        }

        if (inAir) {
            Rectangle new_rect = new Rectangle(new Rectangle(rect.x, (int) (rect.y + airSpeed), rect.width, rect.height));
            if (canMoveHere(new_rect, objects, camera)) {
                rect.y += airSpeed;
                airSpeed += gravity;
                if (airSpeed < 0) {
                    state = "jumping_2";
                } else if (airSpeed > 0) {
                    state = "inAir";
                }
                updateXPos(xSpeed, objects, camera);
            } else {
                for (Object object : objects) {
                    if (object instanceof Portal || object instanceof Merchant || object instanceof Enemy) {
                        continue;
                    }

                    Rectangle objectRect = (Rectangle) object;

                    if (!(new_rect.x + new_rect.width > objectRect.x
                            && new_rect.x < objectRect.x + objectRect.width
                            && new_rect.y + new_rect.height > objectRect.y
                            && new_rect.y < objectRect.y + objectRect.height)) {
                        continue;
                    }

                    String collision_side = collide_with(new_rect, objectRect);

                    switch (collision_side) {
                        case "Top":
                            rect.y = objectRect.y + objectRect.height;
                            break;
                        case "Bottom":
                            rect.y = objectRect.y - this.rect.height;
                            break;
                        default:
                            break;
                    }
                }

                if (airSpeed > 0) {
                    inAir = false;
                    airSpeed = 0;
                    state = "landing";
                    hurt = false;
                } else {
                    airSpeed = 5f;
                }
                updateXPos(xSpeed, objects, camera);
            }
        } else {
            updateXPos(xSpeed, objects, camera);
        }
    }

    public void checkCollision(ArrayList objects) {
        boolean is_enemy = false;

        for (Object object : objects) {
            if (!(object instanceof Enemy || object instanceof Portal || object instanceof Merchant)) {
                continue;
            }

            Rectangle objectRect = (Rectangle) object;

            if (!(this.rect.x + this.rect.width > objectRect.x
                    && this.rect.x < objectRect.x + objectRect.width
                    && this.rect.y + this.rect.height > objectRect.y
                    && this.rect.y < objectRect.y + objectRect.height)) {
                continue;
            }

            String collision_side = collide_with(this.rect, objectRect);

            if ((object instanceof Portal || object instanceof Merchant) && !collision_side.equals("None")) {
                interactedWith = object;
                continue;
            }

            if (collision_side.equals("Bottom")) {
                is_enemy = true;
                enemyKilled = (Enemy) object;
            } else if (!collision_side.equals("None") && !hurt && airSpeed >= 0) {
                if (this.golden_hearts <= 0) {
                    this.health -= 1 * ((this.difficulty.equals("Easy")) ? 1 : (this.difficulty.equals("Normal")) ? 2 : 3);
                } else {
                    this.golden_hearts--;
                }
                airSpeed = knockbackAirSpeed;
                inAir = true;
                hurt = true;
            }

            switch (collision_side) {
                case "Top":
                    rect.y = objectRect.y + objectRect.height;
                    break;
                case "Bottom":
                    rect.y = objectRect.y - this.rect.height;
                    break;
                default:
                    break;
            }
        }

        if (is_enemy) {
            inAir = true;
            airSpeed = bounceSpeed;
        }
    }

    public boolean isHit(Projectile projectile) {
        if (collide_with(this.rect, projectile) != "None") {
            if (this.golden_hearts <= 0) {
                this.health -= 1 * ((this.difficulty.equals("Easy")) ? 1 : (this.difficulty.equals("Normal")) ? 2 : 3);
            } else {
                this.golden_hearts--;
            }
            airSpeed = knockbackAirSpeed;
            hurt = true;
            inAir = true;
            return true;
        }
        return false;
    }

    private void jump() {
        if (inAir) {
            return;
        }
        state = "jumping_1";
        walking = 1;
        airSpeed = jumpSpeed / ((this.currentLevelProgress == 7) ? 1.1f : 1);
        inAir = true;
        this.playSoundEffect = JUMP;
    }

    public void update(ArrayList levels, Camera camera, int tick) {

        if (tick < lastSpedUp + 3 * 40) {
            canSpeedUp = true;
        }

        if (spedUp && tick >= lastSpedUp + 3 * 40) {
            spedUp = false;
        }

        interactedWith = null;
        move(levels, camera);
        if (tick % 4 == 0 && state.equals("walking")) {
            walkAnimation = true;
        }

        if (tick % 4 == 0 && state.equals("idle")) {
            idleAnimation = true;
        }
    }

    public void render(Graphics g, Camera camera, HashMap<String, BufferedImage> images, Game game) {
        if (state.equals("walking") && walkAnimation) {
            walking++;
            if (walking > 7) {
                walking = 1;
            }
            walkAnimation = false;
        }

        if (state.equals("idle") && idleAnimation) {
            idling++;
            if (idling > 7) {
                idling = 1;
            }
            idleAnimation = false;
        }

        BufferedImage img;
        if (state.equals("walking")) {
            img = images.get("walking_" + walking + ".png");
        } else if (state.equals("idle")) {
            img = images.get("idle.png").getSubimage(74 * (idling - 1), 0, 74, 120);
        } else {
            img = images.get(this.state + ".png");
        }
        int width = (state == "walking" || state == "idle") ? img.getWidth() : this.rect.width;

        g.drawImage(img,
                this.rect.x - camera.offset.x + ((this.direction.equals("Left")) ? width : 0),
                this.rect.y,
                ((this.direction.equals("Left")) ? -width : width),
                this.rect.height, game);

        if (showHitbox) {
            g.drawRect(this.rect.x - camera.offset.x, this.rect.y, rect.width, rect.height);
        }
    }

    // Thanks to: https://stackoverflow.com/a/56607347
    private String check_side_collision(Rectangle new_rect, Rectangle object) {
        float playerHalfW = new_rect.width / 2;
        float playerHalfH = new_rect.height / 2;
        float objectHalfW = object.width / 2;
        float objectHalfH = object.height / 2;
        float playerCenterX = new_rect.x + new_rect.width / 2;
        float playerCenterY = new_rect.y + new_rect.height / 2;
        float objectCenterX = object.x + object.width / 2;
        float objectCenterY = object.y + object.height / 2;

        // Calculate the distance between centers
        float diffX = playerCenterX - objectCenterX;
        float diffY = playerCenterY - objectCenterY;

        // Calculate the minimum distance to separate along X and Y
        float minXDist = playerHalfW + objectHalfW;
        float minYDist = playerHalfH + objectHalfH;

        // Calculate the depth of collision for both the X and Y axis
        float depthX = diffX > 0 ? minXDist - diffX : -minXDist - diffX;
        float depthY = diffY > 0 ? minYDist - diffY : -minYDist - diffY;

        String collision = "None";

        if (depthX != 0 && depthY != 0) {
            if (Math.abs(depthX) < Math.abs(depthY)) {
                collision = (depthX > 0) ? "Left" : "Right";
            } else {
                collision = (depthY > 0) ? "Top" : "Bottom";
            }
        }

        return collision;
    }

    public String collide_with(Rectangle new_rect, Rectangle object) {
        if (new_rect.x + this.rect.width > object.x
                && new_rect.x < object.x + object.width
                && new_rect.y + this.rect.height > object.y
                && new_rect.y < object.y + object.height) {
            return check_side_collision(new_rect, object);
        }
        return "None";
    }

    public boolean canMoveHere(Rectangle new_rect, ArrayList objects, Camera camera) {
        float player_x = new_rect.x - camera.offset.x;

        if (player_x < 0 || player_x + new_rect.width > GAME_WIDTH) {
            return false;
        }

        for (Object object : objects) {

            Rectangle rect = (Rectangle) object;

            String collision = collide_with(new_rect, rect);

            if (object instanceof Enemy && collision != "None") {
                return true;
            }

            if (object instanceof Tower && collision == "Bottom") {
                Tower tower = (Tower) object;
                tower.setInfected(false);
            }

            if ((object instanceof Portal || object instanceof Merchant) && collision != "None") {
                return true;
            }

            if (object instanceof Phone && collision != "None") {
                this.playSoundEffect = COIN;
                phoneCollected = true;
                return true;
            }

            if (object instanceof Coin && collision != "None") {
                Coin coin = (Coin) object;
                if (!coin.collected) {
                    this.coins += 1 * ((this.difficulty.equals("Easy")) ? 1 : (this.difficulty.equals("Normal")) ? 2 : 3);
                    this.playSoundEffect = COIN;
                }
                coin.collected = true;
                return true;
            }

            if (collision != "None") {
                return false;
            }
        }

        return true;
    }

    private boolean isInAir(ArrayList objects) {
        for (Object object : objects) {
            Rectangle objectRect = (Rectangle) object;
            if (rect.x + this.rect.width >= objectRect.x
                    && rect.x <= objectRect.x + objectRect.width
                    && rect.y + this.rect.height >= objectRect.y
                    && rect.y <= objectRect.y + objectRect.height) {

                if (check_side_collision(rect, objectRect).equals("Top")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateXPos(float xSpeed, ArrayList objects, Camera camera) {
        if (canMoveHere(new Rectangle((int) (rect.x + xSpeed), rect.y, rect.width, rect.height), objects, camera)) {
            rect.x += xSpeed;
        } else {
            // new x pos here
            if (rect.x + xSpeed - camera.offset.x < 0) {
                rect.x = 0;
            } else if (rect.x + xSpeed + rect.width - camera.offset.x > GAME_WIDTH) {
                rect.x = GAME_WIDTH - rect.width + camera.offset.x;
            } else {
                rect.x = getNewXPos(new Rectangle((int) (rect.x + xSpeed), rect.y, rect.width, rect.height), objects);
            }
        }
    }

    private int getNewXPos(Rectangle new_rect, ArrayList objects) {
        int pos = new_rect.x;
        for (Object object : objects) {
            Rectangle objectRect = (Rectangle) object;
            if (!(new_rect.x + new_rect.width > objectRect.x
                    && new_rect.x < objectRect.x + objectRect.width
                    && new_rect.y + new_rect.height > objectRect.y
                    && new_rect.y < objectRect.y + objectRect.height)) {
                continue;
            }
            String collision_side = check_side_collision(new_rect, objectRect);

            switch (collision_side) {
                case "Left":
                    pos = objectRect.x + objectRect.width;
                    break;
                case "Right":
                    pos = objectRect.x - this.rect.width;
                    break;
                default:
                    break;
            }
        }
        return pos;
    }

    public void calculateScore() {
        // Coin Score (logarithmic scaling)
        int coinScore = (int) (Math.log(this.coins + 1) * 100);

        // Level Progress Score (linear scaling)
        int levelScore = this.currentLevelProgress * 500;

        // Health Score (percentage of remaining HP)
        int healthScore = (int) ((this.health / (double) this.max_health) * 1000);

        // Difficulty Multiplier
        double difficultyMultiplier = 1.0;
        if ("Normal".equals(this.difficulty)) {
            difficultyMultiplier = 1.5;
        } else if ("Hard".equals(this.difficulty)) {
            difficultyMultiplier = 2.0;
        }

        // Time Bonus (inverse relationship with elapsed time)
        long elapsedTimeInSeconds = this.timer / 1000; // Convert milliseconds to seconds
        double timeMultiplier = 10000.0 / (elapsedTimeInSeconds + 1); // Add 1 to avoid division by zero

        // Calculate the final score
        this.score = (int) ((coinScore + levelScore + healthScore) * timeMultiplier * difficultyMultiplier);
    }

    // Save method to store character data in the database
    public void save(Statement stmt) {
        try {
            stmt.execute("INSERT INTO characters(name, difficulty, level, strength, agility, vitality, coins, smallPotion, mediumPotion, bigPotion, speedPotion, goldenBanana, level_progress, timer, score) "
                    + "VALUES('" + this.name + "', '" + this.difficulty + "', " + this.level + ", " + this.strength + ", " + this.agility + ", " + this.vitality + ", " + this.coins + ", " + this.smallPotion + ", " + this.mediumPotion + ", " + this.bigPotion + ", " + this.speedPotion + ", " + this.goldenBanana + ", " + this.currentLevelProgress + ", " + this.timer + ", " + this.score + ")");
        } catch (SQLException e) {
            System.out.println("Unable to execute statement!");
            e.printStackTrace();
        }
    }

}
