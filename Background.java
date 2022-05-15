import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Background implements Displayable {

    private static PImage mountainsLayer1Image = null;
    private static PImage mountainsLayer2Image = null;

    private static PImage cityLayer1Image = null;
    private static PImage cityLayer2Image = null;
    private static PImage cityLayer3Image = null;
    private static PImage cityLayer4Image = null;
    private static PImage cityLayer5Image = null;

    private static PImage valleyLayer1Image = null;
    private static PImage valleyLayer2Image = null;

    private static PVector mountainsLayer1Size = new PVector(1000, 800);
    private static PVector mountainsLayer2Size = new PVector(1000, 800);

    private static PVector cityLayer1Size = new PVector(1050, 600);
    private static PVector cityLayer2Size = new PVector(400,  500);
    private static PVector cityLayer3Size = new PVector(800,  500);
    private static PVector cityLayer4Size = new PVector(800,  450);
    private static PVector cityLayer5Size = new PVector(1000, 400);

    private static PVector valleyLayer1Size = new PVector(1000, 1400);
    private static PVector valleyLayer2Size = new PVector(1200, 1600);

    private static PVector mountainsLayer1Position = new PVector(0, -200);
    private static PVector mountainsLayer2Position = new PVector(0, -500);

    private static PVector cityLayer1Position = new PVector(0,    -0);
    private static PVector cityLayer2Position = new PVector(-200, -25);
    private static PVector cityLayer3Position = new PVector(0,    -50);
    private static PVector cityLayer4Position = new PVector(100,  -75);
    private static PVector cityLayer5Position = new PVector(-50,  -100);

    private static PVector valleyLayer1Position = new PVector(0, -250);
    private static PVector valleyLayer2Position = new PVector(0, -500);

    private static float mountainsLayer1Speed = 0.060f;
    private static float mountainsLayer2Speed = 0.080f;

    private static float cityLayer1Speed = 0.150f;
    private static float cityLayer2Speed = 0.175f;
    private static float cityLayer3Speed = 0.200f;
    private static float cityLayer4Speed = 0.225f;
    private static float cityLayer5Speed = 0.250f;

    private static float valleyLayer1Speed = 0.500f;
    private static float valleyLayer2Speed = 0.750f;

    private static Color skyColor =     new Color(100, 200, 255);
    private static Color sunsetColor =  new Color(255, 200, 100); //This color was chosen by copilot and it fits wtf.
    private static Color nightColor =   new Color(30, 15, 90);
    private static Color spaceColor =   new Color(0, 0, 0);

    public static float sunsetAltitude = 10000; //The altitude where the sunset is the strongest.
    public static float nightAltitude  = 20000; //The altitude where the night is the strongest.
    public static float spaceAltitude  = 30000; //The altitude where the space is the strongest.

    public Background() {
    }

    public static void loadImages(PApplet p) {
        mountainsLayer1Image = p.loadImage("assets/background/mountainsLayer1.png");
        mountainsLayer2Image = p.loadImage("assets/background/mountainsLayer2.png");
        cityLayer1Image = p.loadImage("assets/background/cityLayer1.png");
        cityLayer2Image = p.loadImage("assets/background/cityLayer2.png");
        cityLayer3Image = p.loadImage("assets/background/cityLayer3.png");
        cityLayer4Image = p.loadImage("assets/background/cityLayer4.png");
        cityLayer5Image = p.loadImage("assets/background/cityLayer5.png");
        valleyLayer1Image = p.loadImage("assets/background/valleyLayer1.png");
        valleyLayer2Image = p.loadImage("assets/background/valleyLayer2.png");

        mountainsLayer1Image.resize((int) mountainsLayer1Size.x, (int) mountainsLayer1Size.y);
        mountainsLayer2Image.resize((int) mountainsLayer2Size.x, (int) mountainsLayer2Size.y);
        cityLayer1Image.resize((int) cityLayer1Size.x, (int) cityLayer1Size.y);
        cityLayer2Image.resize((int) cityLayer2Size.x, (int) cityLayer2Size.y);
        cityLayer3Image.resize((int) cityLayer3Size.x, (int) cityLayer3Size.y);
        cityLayer4Image.resize((int) cityLayer4Size.x, (int) cityLayer4Size.y);
        cityLayer5Image.resize((int) cityLayer5Size.x, (int) cityLayer5Size.y);
        valleyLayer1Image.resize((int) valleyLayer1Size.x, (int) valleyLayer1Size.y);
        valleyLayer2Image.resize((int) valleyLayer2Size.x, (int) valleyLayer2Size.y);
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        colorBackground(-cameraPosition.y, screen, p);

        p.imageMode(PApplet.CENTER);
        p.image(mountainsLayer1Image, (int) (mountainsLayer1Position.x - cameraPosition.x), (int) (-mountainsLayer1Position.y - cameraPosition.y * mountainsLayer1Speed));
        p.image(mountainsLayer2Image, (int) (mountainsLayer2Position.x - cameraPosition.x), (int) (-mountainsLayer2Position.y - cameraPosition.y * mountainsLayer2Speed));
        p.image(cityLayer1Image, (int) (cityLayer1Position.x - cameraPosition.x), (int) (-cityLayer1Position.y - cameraPosition.y * cityLayer1Speed));
        p.image(cityLayer2Image, (int) (cityLayer2Position.x - cameraPosition.x), (int) (-cityLayer2Position.y - cameraPosition.y * cityLayer2Speed));
        p.image(cityLayer3Image, (int) (cityLayer3Position.x - cameraPosition.x), (int) (-cityLayer3Position.y - cameraPosition.y * cityLayer3Speed));
        p.image(cityLayer4Image, (int) (cityLayer4Position.x - cameraPosition.x), (int) (-cityLayer4Position.y - cameraPosition.y * cityLayer4Speed));
        p.image(cityLayer5Image, (int) (cityLayer5Position.x - cameraPosition.x), (int) (-cityLayer5Position.y - cameraPosition.y * cityLayer5Speed));
        p.image(valleyLayer1Image, (int) (valleyLayer1Position.x - cameraPosition.x), (int) (-valleyLayer1Position.y - cameraPosition.y * valleyLayer1Speed));
        p.image(valleyLayer2Image, (int) (valleyLayer2Position.x - cameraPosition.x), (int) (-valleyLayer2Position.y - cameraPosition.y * valleyLayer2Speed));
        p.noStroke();
        p.fill(150, 50);
        p.rect(0, 0, screen.x, screen.y);
    }

    private static void colorBackground(float altitude, PVector screen, PApplet p) {
        int skyColor = Background.skyColor.toPColor(p);
        int sunsetColor = Background.sunsetColor.toPColor(p);
        int nightColor = Background.nightColor.toPColor(p);
        int spaceColor = Background.spaceColor.toPColor(p);

        int color1;
        if(altitude < sunsetAltitude)
            color1 = lerpColorByAltitude(skyColor, sunsetColor, altitude, 0, sunsetAltitude, p);
        else if(altitude < nightAltitude)
            color1 = lerpColorByAltitude(sunsetColor, nightColor, altitude, sunsetAltitude, nightAltitude, p);
        else
            color1 = lerpColorByAltitude(nightColor, spaceColor, altitude, nightAltitude, spaceAltitude, p);

        int color2 = lerpColorByAltitude(skyColor, spaceColor, altitude, 0, spaceAltitude, p);

        paintGradient(color1, color2, screen, p);
    }

    private static int lerpColorByAltitude(int colorLow, int colorHigh, float altitudeCurrent, float altitudeLow, float altitudeHigh, PApplet p) {
        altitudeCurrent -= altitudeLow;
        altitudeHigh -= altitudeLow;

        float percentage = altitudeCurrent / altitudeHigh;
        return p.lerpColor(colorLow, colorHigh, percentage);
    }

    private static void paintGradient(int color1, int color2, PVector screen, PApplet p) {
        for(int i = 0; i < screen.y; i++) {
            int color = p.lerpColor(color2, color1, i / screen.y);
            p.stroke(color);
            p.line(0, i, screen.x, i);
        }
    }

    private record Color(int r, int g, int b) {
        public int toPColor(PApplet p) {
            return p.color(r, g, b);
        }
    }
}