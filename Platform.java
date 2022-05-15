import java.util.concurrent.ThreadLocalRandom;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Platform implements Displayable{

    public static enum Type {BENCH, CLOUD, CONE, LEAF, LILYPAD, TRASHCAN};

    //#region Images
    private static PImage benchImage = null;
    private static PVector benchSize = new PVector(100, 50);
    private static PImage cloud1Image = null;
    private static PVector cloud1Size = new PVector(200, 50);
    private static PImage cloud2Image = null;
    private static PVector cloud2Size = new PVector(200, 50);
    private static PImage cloud3Image = null;
    private static PVector cloud3Size = new PVector(200, 50);
    private static PImage coneImage = null;
    private static PVector coneSize = new PVector(50, 50);
    private static PImage lilypadImage = null;
    private static PVector lilypadSize = new PVector(100, 20);
    private static PImage leafImage = null;
    private static PVector leafSize = new PVector(100, 20);
    private static PImage trashcan1Image = null;
    private static PVector trashcan1Size = new PVector(50, 80);
    private static PImage trashcan2Image = null;
    private static PVector trashcan2Size = new PVector(80, 50);
    //#endregion

    private static float benchWeight = 50;
    private static float cloudWeight = 5;
    private static float coneWeight = 20;
    private static float leafWeight = 10;
    private static float lilypadWeight = 15;
    private static float trashcanWeight = 25;

    private PImage image;
    private PVector position;
    private PVector velocity = new PVector(0, 0);
    private PVector size;
    private float weight;
    private Type type;

    public static void loadImages(PApplet p) {
        benchImage = p.loadImage("assets/platforms/bench.png");
        cloud1Image = p.loadImage("assets/platforms/cloud1.png");
        cloud2Image = p.loadImage("assets/platforms/cloud2.png");
        cloud3Image = p.loadImage("assets/platforms/cloud3.png");
        coneImage = p.loadImage("assets/platforms/cone.png");
        leafImage = p.loadImage("assets/platforms/leaf.png");
        lilypadImage = p.loadImage("assets/platforms/lilypad.png");
        trashcan1Image = p.loadImage("assets/platforms/trashcan1.png");
        trashcan2Image = p.loadImage("assets/platforms/trashcan2.png");

        benchImage.resize((int)benchSize.x, (int)benchSize.y);
        cloud1Image.resize((int)cloud1Size.x, (int)cloud1Size.y);
        cloud2Image.resize((int)cloud2Size.x, (int)cloud2Size.y);
        cloud3Image.resize((int)cloud3Size.x, (int)cloud3Size.y);
        coneImage.resize((int)coneSize.x, (int)coneSize.y);
        leafImage.resize((int)leafSize.x, (int)leafSize.y);
        lilypadImage.resize((int)lilypadSize.x, (int)lilypadSize.y);
        trashcan1Image.resize((int)trashcan1Size.x, (int)trashcan1Size.y);
        trashcan2Image.resize((int)trashcan2Size.x, (int)trashcan2Size.y);
    }

    public Platform(PVector position, Type... types) {
        this(position.x, position.y, types);
    }

    public Platform(float x, float y, Type... types) {
        position = new PVector(x, y);
        type = types[ThreadLocalRandom.current().nextInt(types.length)];
        setProperties();
    }

    private void setProperties() {
        switch (type) {
            case BENCH -> {
                image = benchImage;
                size = benchSize;
                weight = benchWeight;
            }
            case CLOUD -> {
                int cloudNumber = ThreadLocalRandom.current().nextInt(1, 4);
                switch (cloudNumber) {
                    case 1 -> {
                        image = cloud1Image;
                        size = cloud1Size;
                    }
                    case 2 -> {
                        image = cloud2Image;
                        size = cloud2Size;
                    }
                    case 3 -> {
                        image = cloud3Image;
                        size = cloud3Size;
                    }
                }
                weight = cloudWeight;
            }
            case CONE -> {
                image = coneImage;
                size = coneSize;
                weight = coneWeight;
            }
            case LEAF -> {
                image = leafImage;
                size = leafSize;
                weight = leafWeight;
            }
            case LILYPAD -> {
                image = lilypadImage;
                size = lilypadSize;
                weight = lilypadWeight;
            }
            case TRASHCAN -> {
                int trashcanNumber = ThreadLocalRandom.current().nextInt(1, 3);
                switch (trashcanNumber) {
                    case 1 -> {
                        image = trashcan1Image;
                        size = trashcan1Size;
                    }
                    case 2 -> {
                        image = trashcan2Image;
                        size = trashcan2Size;
                    }
                }
                weight = trashcanWeight;
            }
        }
    }

    public void update(float screenWidth, float deltaTime) {
        addHorizontalVelocity(deltaTime);

        if(position.x < -screenWidth)
            position.x += screenWidth * 2;
        if(position.x >  screenWidth)
            position.x -= screenWidth * 2;

        position.add(velocity);

        velocity.add(Forces.getDrag(velocity));
    }

    private void addHorizontalVelocity(float deltaTime) {
        if(weight != 0)
            velocity.add(Forces.getWind(position).mult(deltaTime).div(weight));
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        p.imageMode(PApplet.CENTER);
        p.image(image, (int) (position.x - cameraPosition.x), (int) (position.y - cameraPosition.y));
    }

    public PVector position() {
        return position;
    }

    public PVector size() {
        return size;
    }

}