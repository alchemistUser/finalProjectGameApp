package finalproject;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.Math;

/*
This class shall act as the camera for the game:
- Follow Camera, which takes 
*/

public class Camera {
    public Character player;
    public Point offset = new Point(0, 0);
    public Point CONSTANT;  // Half of the screen
    public Dimension window_size;
    
    public Camera(Character player, Dimension window_size, Dimension character_size) {
        this.player = player;
        this.window_size = window_size;
        this.CONSTANT = new Point(-window_size.width / 2 + character_size.width/2, -window_size.height + character_size.height + 116);
    }
    
    public void follow(int width) {
        this.offset.x += this.player.rect.x - this.offset.x + this.CONSTANT.x;
        this.offset.y += this.player.rect.y - this.offset.y + this.CONSTANT.y;
        this.offset.x = Math.max(0, this.offset.x);
        this.offset.x = Math.min(this.offset.x, width - this.window_size.width);
    }
}
