import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class HUD implements Displayable {
    
    private int altitude = 0;
    private int altitudeScaling = 50; //How much the altitude changes per meter
    private int coins = 0;
    private int highestAltitude = 0; //The previous highest altitude reached

    public static PVector altitudePosition  = new PVector(0.10f, 0.10f); //Percentage of screen. 0,0 is top left; 1,1 is bottom right
    public static PVector coinsPosition     = new PVector(0.90f, 0.10f);

    public static int textColorGrayScale = 255; //0 = black, 255 = white

    public static PImage controlsInfoImage = null;
    public static PImage enemyInfoImage = null;
    public static PImage windInfoImage = null;

    public static PVector controlsInfoSize  = new PVector(500, 300);
    public static PVector enemyInfoSize     = new PVector(400, 150);
    public static PVector windInfoSize      = new PVector(400, 150);

    public static PVector controlsInfoPosition  = new PVector(0.50f, -300); //x is percentage of screen, y is world position
    public static PVector enemyInfoPosition     = new PVector(0.50f, 0);
    public static PVector windInfoPosition      = new PVector(0.50f, 0);

    public HUD(int highestAltitude, PApplet p) {
        this.highestAltitude = highestAltitude;
        loadImages(p);
    }

    public static void loadImages(PApplet p) {
        controlsInfoImage = p.loadImage("assets/info/controls.png");
        //enemyInfoImage = p.loadImage("assets/info/enemy.png"); TODO: Add enemy info
        //windInfoImage = p.loadImage("assets/info/wind.png"); TODO: Add wind info

        controlsInfoImage.resize((int) controlsInfoSize.x, (int) controlsInfoSize.y);
        //enemyInfoImage.resize((int) enemyInfoSize.x, (int) enemyInfoSize.y);
        //windInfoImage.resize((int) windInfoSize.x, (int) windInfoSize.y);
    }

    public void update(Frog player) {
        altitude = (int) player.altitude();
        coins = player.coins();
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        p.fill(textColorGrayScale);
        displayHighscore(cameraPosition, screen, p);

        p.textSize(20);
        p.textAlign(PApplet.LEFT);
        p.text("Altitude: " + altitude / altitudeScaling + "m", (int) (altitudePosition.x * screen.x), (int) (altitudePosition.y * screen.y));
        p.textAlign(PApplet.RIGHT);
        p.text("Coins: " + coins, coinsPosition.x * screen.x, coinsPosition.y * screen.y);

        p.imageMode(PApplet.CENTER);
        p.image(controlsInfoImage, (int) (controlsInfoPosition.x * screen.x), (int) (-controlsInfoPosition.y - cameraPosition.y));
        //p.image(enemyInfoImage, (int) (enemyInfoPosition.x * screen.x), (int) (-enemyInfoPosition.y - cameraPosition.y));
        //p.image(windInfoImage, (int) (windInfoPosition.x * screen.x), (int) (-windInfoPosition.y - cameraPosition.y));
    }

    private void displayHighscore(PVector cameraPosition, PVector screen, PApplet p) {
        if(highestAltitude == 0)
            return;

        int yPosition = (int) (-highestAltitude - cameraPosition.y);
        p.stroke(textColorGrayScale);
        p.strokeWeight(5);
        p.line(0, yPosition, screen.x, yPosition);
        p.textAlign(PApplet.LEFT);
        p.text("Altitude highscore: " + highestAltitude, 10, yPosition + 10);
  }

}

