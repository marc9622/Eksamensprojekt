import java.util.concurrent.ThreadLocalRandom;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Coin implements Displayable {

    private static PImage[] coinImages = null;
    private static PVector size = new PVector(50, 60);

    private int currentImage = 0;
    private float time = 0;
    private float timeToChangeImage = 0.1f;
    
    private PVector startPosition;
    private PVector position;

    private float maxSpeed = 150;
    private float minSpeed = 75;
    private float speed;

    private float maxFlyingDistance = 300;
    private float minFlyingDistance = 50;
    private float flyingDistance;

    private boolean isFacingRight = true;

    public static void loadImages(PApplet p) {
        coinImages = new PImage[2];
        coinImages[0] = p.loadImage("assets/coins/coin1.png");
        coinImages[1] = p.loadImage("assets/coins/coin2.png");

        coinImages[0].resize((int) size.x, (int) size.y);
        coinImages[1].resize((int) size.x, (int) size.y);
    }

    public Coin(float x, float y) {
        startPosition = new PVector(x, y);
        position = new PVector(x, y);
        speed = ThreadLocalRandom.current().nextFloat(minSpeed, maxSpeed);
        flyingDistance = ThreadLocalRandom.current().nextFloat(minFlyingDistance, maxFlyingDistance);
    }

    public void update(float deltaTime) {
        move(deltaTime);
        time += deltaTime;
        if (time > timeToChangeImage) {
            time = 0;
            currentImage = (currentImage + 1) % coinImages.length;
        }
    }

    public void move(float deltaTime) {
        if (isFacingRight)
            position.x += speed * deltaTime;
        else
            position.x -= speed * deltaTime;
        if (position.x > startPosition.x + flyingDistance)
            isFacingRight = false;
        else if (position.x < startPosition.x - flyingDistance)
            isFacingRight = true;
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        p.imageMode(PApplet.CENTER);
        p.image(coinImages[currentImage], position.x - cameraPosition.x, position.y - cameraPosition.y);
    }

    public PVector position() {
        return position.copy();
    }

    public static PVector size() {
        return size.copy();
    }

}