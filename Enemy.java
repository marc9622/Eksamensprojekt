import java.util.concurrent.ThreadLocalRandom;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Enemy implements Displayable{
    
    public static enum Type {BIRD, PLANE, UFO};

    //#region Images
    private static PImage birdLeftImage1 = null;
    private static PImage birdLeftImage2 = null;
    private static PImage birdRightImage1 = null;
    private static PImage birdRightImage2 = null;

    private static PImage planeLeftImage = null;
    private static PImage planeRightImage = null;

    private static PImage ufoImage = null;

    private static PVector birdSize = new PVector(100, 120);
    private static PVector planeSize = new PVector(200, 120);
    private static PVector ufoSize = new PVector(200, 120);
    //#endregion

    private static float birdSpeed = 150f;
    private static float planeSpeed = 200f;
    private static float ufoSpeed = 250f;

    private int currentImage = 0;
    private float timeSinceChangeImage = 0;
    private float timeToChangeImage = 0.1f;

    private PImage[] images;
    private PVector position;
    private PVector velocity;
    private boolean isFacingRight;
    private PVector size;
    private Type type;

    private float timeSinceLastCollision = 0;
    private float collisionCooldown = 0.5f;

    public static void loadImages(PApplet p) {
        birdLeftImage1 = p.loadImage("assets/enemies/birdLeftFrame1.png");
        birdLeftImage2 = p.loadImage("assets/enemies/birdLeftFrame2.png");
        birdRightImage1 = p.loadImage("assets/enemies/birdRightFrame1.png");
        birdRightImage2 = p.loadImage("assets/enemies/birdRightFrame2.png");
        planeLeftImage = p.loadImage("assets/enemies/planeLeft.png");
        planeRightImage = p.loadImage("assets/enemies/planeRight.png");
        ufoImage = p.loadImage("assets/enemies/UFO.png");

        birdLeftImage1.resize((int)birdSize.x, (int)birdSize.y);
        birdLeftImage2.resize((int)birdSize.x, (int)birdSize.y);
        birdRightImage1.resize((int)birdSize.x, (int)birdSize.y);
        birdRightImage2.resize((int)birdSize.x, (int)birdSize.y);
        planeLeftImage.resize((int)planeSize.x, (int)planeSize.y);
        planeRightImage.resize((int)planeSize.x, (int)planeSize.y);
        ufoImage.resize((int)ufoSize.x, (int)ufoSize.y);
    }

    public Enemy(PVector playerPosition, PVector screenSize, Type... types) {
        if(playerPosition.x < 0) {
            isFacingRight = false;
            position = new PVector( screenSize.x, playerPosition.y - ThreadLocalRandom.current().nextFloat(-screenSize.y/2, screenSize.y*2));
        } else {
            isFacingRight = true;
            position = new PVector(-screenSize.x, playerPosition.y - ThreadLocalRandom.current().nextFloat(-screenSize.y/2, screenSize.y*2));
        }
        type = types[ThreadLocalRandom.current().nextInt(types.length)];
        setProperties();
    }

    private void setProperties() {
        switch (type) {
            case BIRD -> {
                size = birdSize;
                if(isFacingRight) {
                    images = new PImage[]{birdRightImage1, birdRightImage2};
                    velocity = new PVector( birdSpeed, 0);
                } else {
                    images = new PImage[]{birdLeftImage1, birdLeftImage2};
                    velocity = new PVector(-birdSpeed, 0);
                }
            }
            case PLANE -> {
                size = planeSize;
                if(isFacingRight) {
                    images = new PImage[]{planeRightImage};
                    velocity = new PVector( planeSpeed, 0);
                } else {
                    images = new PImage[]{planeLeftImage};
                    velocity = new PVector(-planeSpeed, 0);
                }
            }
            case UFO -> {
                size = ufoSize;
                images = new PImage[]{ufoImage};
                if(isFacingRight) {
                    velocity = new PVector( ufoSpeed, 0);
                } else {
                    velocity = new PVector(-ufoSpeed, 0);
                }
            }
        }
    }

    public void collide(float deltaTime) {
        if(timeSinceLastCollision > collisionCooldown) {
            timeSinceLastCollision = 0;
            swithcDirection();
        }
    }

    public void swithcDirection() {
        if(isFacingRight) {
            isFacingRight = false;
            velocity.x = -birdSpeed;
        } else {
            isFacingRight = true;
            velocity.x = birdSpeed;
        }
        setProperties();
    }

    public void update(float deltaTime) {
        position.add(velocity.copy().mult(deltaTime));
        timeSinceLastCollision += deltaTime;
        timeSinceChangeImage += deltaTime;
        if (timeSinceChangeImage > timeToChangeImage) {
            timeSinceChangeImage = 0;
            currentImage = (currentImage + 1) % images.length;
        }
    }

    public boolean isOutOfBounds(float screenWidth) {
        return position.x < -screenWidth || position.x > screenWidth;
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        p.imageMode(PApplet.CENTER);
        p.image(images[currentImage], (int) (position.x - cameraPosition.x), (int) (position.y - cameraPosition.y));
    }

    public PVector position() {
        return position;
    }

    public PVector size() {
        return size;
    }
}