package finalproject;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Graphics;
import static finalproject.Constants.*;

class Obstacle extends Rectangle {
    public int speedReduction = 1;
    public Obstacle(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public Obstacle(int x, int y, int w, int h, int sr) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        speedReduction = sr;
    }
}

class Floor extends Rectangle {
    public Floor(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}

class Phone extends Rectangle {
    public Phone(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}

class Tower extends Rectangle {
    public boolean infected;
    
    public Tower(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public void setInfected(boolean SI) {
        infected = SI;
    }
}

class Enemy extends Rectangle {
    public int id;
    public Point point_1 = null;
    public Point point_2 = null;
    public float xSpeed = 0;
    public String direction = "Left";
    public boolean alive = true;
    
    // Stats and Rewards
    public float difficultyMultiplier = 1;
    
    // Attack
    public int lastAttack = -1;
    public int attackDelay = 2; 
    
    // Projectile properties
    public Projectile projectile;
    public float projectileSpeed;
    public int projectileMaxDist;
    
    // Summoned properties
    public Enemy summoned;
    public int summonSpeed;
    public int summonMaxDist;
    
    // Hostility properties
    public int hostilityRange;
    public float hostilitySpeed;
    public String hostilityDirection;
    public Point hostilityBackPosition;
    public boolean chasing = false;
    
    // Mode
    public int attackMode;
    
    // Animation
    private boolean animated;
    private boolean animate = false;
    private int animationFrames = -1;
    private int framePerTick = 8;
    private int animationOffset = 0;
    
    // Hostile
    public Enemy(int id, int x, int y, int w, int h, int hR, int hS, Point hBp, int aM) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.hostilityRange = hR;
        this.hostilitySpeed = hS;
        this.hostilityBackPosition = hBp;
        this.attackMode = aM;
    }
    
    // Ranged/Summoner Fixed
    public Enemy(int id, int x, int y, int w, int h, int pS, int pMD, int aM) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.attackMode = aM;
        if (aM == RangedFixed) {
            this.projectileSpeed = pS;
            this.projectileMaxDist = pMD;
        } else {
            this.summonSpeed = pS;
            this.summonMaxDist = pMD;
        }
    }
    
    // Moving on a fixed path
    public Enemy(int id, int x, int y, int w, int h, Point p1, Point p2, int dx, int aM) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        point_1 = p1;
        point_2 = p2;
        xSpeed = dx;
        this.attackMode = aM;
    }
    
    // BOss
    public Enemy(int id, int x, int y, int w, int h, int pS, int aM) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.projectileSpeed = pS;
        this.attackMode = aM;
        this.attackDelay = 1;
    }
    
    public void render(Graphics g, Camera camera, HashMap<Integer, BufferedImage> images, Game game) {
        BufferedImage img = images.get(id);
        if (animationFrames == -1)
            animationFrames = img.getWidth()/width;
        
        if (img.getWidth() > width) {
            if (animate)
                animationOffset++;
            
            if (animationOffset > animationFrames-1)
                animationOffset = 0;
            img = img.getSubimage(width*animationOffset, 0, width, height);
            animate = false;
        }
        
        g.drawImage(img,
            x-camera.offset.x + ((direction == "Right") ? width : 0),
            y,
            (direction == "Right") ? -width : width,
            height, game);
    }
    
    public Enemy setDirection(String dir) {
        this.direction = dir;
        return this;
    }
    
    public void setDifficulty(float newDiff) {
        difficultyMultiplier = newDiff;
        hostilitySpeed += (5 * (difficultyMultiplier-1));
        projectileSpeed *= difficultyMultiplier;
        xSpeed *= difficultyMultiplier;
    }
    
    public void update(Character player, int tick, HashMap imgs) {
        
        isHitByProjectile(player.projectiles);
        
        if (!imgs.containsKey(id) && !animate && tick % 4 == 0)
            animate = true;
        
        if (id == FINAL_BOSS) {
            float dx = player.rect.x+player.rect.width/2 - x;
            float dy = player.rect.y+player.rect.height/2 - y;
            float l = (float)Math.sqrt(dx*dx + dy*dy);
            xSpeed = (dx/l) * 10;
            if (l <= 600)
                xSpeed = 0;
            this.x += xSpeed;
        }
        
        if (imgs.containsKey(id))
            shootProjectile((BufferedImage)imgs.get(id), tick);
        
//        if (attackMode == SummonerFixed && summoned == null)
//            summoned = new Enemy(BAT, x+summonMaxDist/2, y+height/2, 64, 48, new Point(x, y+height/2), new Point(x+(direction.equals("Left") ? -summonMaxDist : summonMaxDist), y+height/2), -summonSpeed, Moving);
        
        if (projectile != null && id != FINAL_BOSS) {
            projectile.x += (projectile.direction.equals("Left")) ? -projectileSpeed : projectileSpeed;
            if ((projectile.direction.equals("Left") && projectile.x < x-projectileMaxDist) ||
                    (projectile.direction.equals("Right") && projectile.x > x+projectileMaxDist)) {
                projectile = null;
                lastAttack = tick;
            }
        } else if (projectile != null && id == FINAL_BOSS) {
            if (projectile.speed == null) {
                float cx = (player.rect.x+player.rect.width/2) - (x+width/2);
                float cy = (player.rect.y+player.rect.height/2) - (y+height);
                float cl = (float)Math.sqrt(cx*cx + cy*cy);
                projectile.setSpeed(new Point((int)(cx/cl*projectileSpeed), (int)(cy/cl*projectileSpeed)));
            }
            projectile.tick();
        }
        
        if (hostilityRange != 0)
            chasePlayer(player);
        
        if (point_1 == null && point_2 == null && xSpeed == 0)
            return;
        
        if (attackMode == Moving) {
            if (xSpeed > 0) {
                if (this.x + xSpeed > point_2.x - xSpeed) {
                    this.x = point_2.x;
                    xSpeed = -xSpeed;
                    direction = "Left";
                } else
                    this.x += xSpeed;
            } else {
                if (this.x + xSpeed < point_1.x + xSpeed) {
                    this.x = point_1.x;
                    xSpeed = -xSpeed;
                    direction = "Right";
                } else
                    this.x += xSpeed;
            }
        }
    }
    
    private void shootProjectile(BufferedImage img, int tick) {
        if (projectile != null && id != FINAL_BOSS)
            return;
        
        int tickForAttack = (int)(lastAttack + attackDelay / difficultyMultiplier * 40);
        if (!animate && tick % framePerTick == 0 && tick >= tickForAttack - animationFrames * framePerTick && tick < tickForAttack)
            animate = true;
            
        if (tick < tickForAttack)
            return;
        
        if (id == PLANT)
            projectile = new Projectile(img, x-img.getWidth()+(direction.equals("Right") ? width : 0), y, img.getWidth(), img.getHeight(), direction);
        else if (id == SKELETON_ARCHER)
            projectile = new Projectile(img, x-img.getWidth()+(direction.equals("Right") ? width : 0), y + 66, img.getWidth(), img.getHeight(), direction);
        else if (id == BATMAN)
            projectile = new Projectile(img, x-img.getWidth()+(direction.equals("Right") ? width : 0), y + height/2, img.getWidth(), img.getHeight(), direction);
        else if (id == IMP)
            projectile = new Projectile(img, x-img.getWidth()+(direction.equals("Right") ? width : 0), y + height/2, img.getWidth(), img.getHeight(), direction);
        else if (id == FINAL_BOSS)
            projectile = new Projectile(img, x+width/2, y+height, img.getWidth()/6, img.getHeight());

        lastAttack = tick;
    }
    
    private void isHitByProjectile(ArrayList<Projectile> projectiles) {
        Projectile projectileToRemove = null;
        for (Projectile projectile : projectiles) {
            if (projectile.x + projectile.width > this.x &&
                    projectile.x < this.x + this.width &&
                    projectile.y + projectile.height > this.y &&
                    projectile.y < this.y + this.height) {
                this.alive = false;
                projectileToRemove = projectile;
                break;
            }
        }
        projectiles.remove(projectileToRemove);
    }
    
    private void chasePlayer(Character player) {
        Rectangle hostilityRangeRect = new Rectangle(x-hostilityRange, y, hostilityRange, height);
        
        if (!player.collide_with(player.rect, hostilityRangeRect).equals("None")) {
            chasing = true;
            if (player.rect.x < x)
                xSpeed = -hostilitySpeed;
            if (player.rect.x > x)
                xSpeed = hostilitySpeed;
            direction = "Left";
        } else {
            if (chasing) {
                xSpeed = -xSpeed/2;
                direction = "Right";
            }
            chasing = false;
            if (xSpeed > 0) {
                if (this.x + xSpeed > hostilityBackPosition.x) {
                    xSpeed = 0;
                    direction = "Left";
                }
            } else {
                if (this.x + xSpeed < hostilityBackPosition.x) {
                    xSpeed = 0;
                    direction = "Right";
                }
            }
            this.x += xSpeed;
        }
        
        if (chasing) {
            if (xSpeed > 0) {
                if (this.x + xSpeed > hostilityBackPosition.x + hostilityRange) {
                    this.x = hostilityBackPosition.x + hostilityRange;
                    xSpeed = -xSpeed;
                    direction = "Right";
                } else
                    this.x += xSpeed;
            } else {
                if (this.x + xSpeed < hostilityBackPosition.x - hostilityRange) {
                    this.x = hostilityBackPosition.x - hostilityRange;
                    xSpeed = -xSpeed;
                    direction = "Left";
                } else
                    this.x += xSpeed;
            }
        }
    }
}

class Projectile extends Rectangle {
    public BufferedImage image;
    public String direction;
    public int frame;
    public Point speed;
    
    public Projectile(BufferedImage image, int x, int y, int w, int h, String dir) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.image = image;
        this.direction = dir;
    }
    
    public Projectile(BufferedImage image, int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.image = image;
    }
    
    public void setSpeed(Point speed) {
        this.speed = speed;
    }
    
    public void tick() {
        this.x += this.speed.x;
        this.y += this.speed.y;
    }
}

class Merchant extends Rectangle {
    
    public Merchant(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}

class Portal extends Rectangle {
    public int destination;
    Portal(int x, int y, int w, int h, int d) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.destination = d;
    }
}

class Item {
    public BufferedImage image;
    public String name;
    public int buyPrice;
    public int sellPrice;
    private int animationOffset = 0;
    public boolean animate = false;
    
    public Item(BufferedImage img, String name, int bP, int sP) {
        this.image = img;
        this.name = name;
        this.buyPrice = bP;
        this.sellPrice = sP;
    }
    
    public BufferedImage getImage() {
        if (animate)
            animationOffset++;
        animate = false;
        
        int size = name.contains("Big") ? 46 : 44;

        if (animationOffset > image.getWidth()/44-1)
            animationOffset = 0;
        
        return image.getSubimage(size*animationOffset, 0, size, size);
    }
}

class Coin extends Rectangle {
    public boolean collected = false;
    public int frame = 0;
    
    public BufferedImage getFrame(BufferedImage img, int tick) {
        if (tick % 8 == 0) {
            frame++;
            if (frame == 5)
                frame = 0;
        }
        
        return img.getSubimage(frame*64, 0, 64, 64);
    }
    
    public Coin(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}