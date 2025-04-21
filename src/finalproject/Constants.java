package finalproject;

public final class Constants {
    private Constants() {
    }
    
    public final static boolean testing = true;
    public final static boolean showHitbox = false;
    
    public final static int GAME_WIDTH = 1280;
    public final static int GAME_HEIGHT = 720;
    
    // Enemies
    public final static int PLANT = 1;
    public final static int SLIME = 2;
    public final static int HARLEY = 3;
    public final static int SKELETON_WARRIOR = 4;
    public final static int SKELETON_ARCHER = 5;
    public final static int BATMAN = 6;
    public final static int FIRE_SLIME = 7;
    public final static int IMP = 8;
    public final static int FINAL_BOSS = 9;
    
    // Enemy Attack Mode??
    public final static int Hostile = 25; // Will run towards the player if in range
    public final static int Moving = 26; // Has a fixed path it follows
    public final static int RangedFixed = 27; // In place shooter (doesn't move)
    public final static int SummonerFixed = 28; // In place shooter (doesn't move)
    public final static int BOSS = 69;
    
    // Sound Effects
    public final static int COIN = 12;
    public final static int HURT = 13;
    public final static int JUMP = 14;
}
