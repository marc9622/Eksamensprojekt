import processing.core.*;

public class Main extends PApplet{

    private ButtonManager buttonManager = new ButtonManager();
    private SaveManager saveManager = new SaveManager("saves.sqlite", this);

    private Background background;
    private HUD hud;
    private World world;
    private Frog player;

    public static void main(String[] passedArgs) {
        PApplet.main("Main");
    }

    public void settings() {
        size(1000, 1000);
    }

    public void setup() {
        frameRate(360);

        loadImages();
        reset();
    }

    private void loadImages() {
        Platform.loadImages(this);
        Coin.loadImages(this);
        Enemy.loadImages(this);
        Background.loadImages(this);
        Frog.loadImages(this);
        World.loadImages(this);
    }

    public void reset() {
        SaveManager.Save save = saveManager.getAliveOrNew();

        background = new Background();
        hud = new HUD(saveManager.getHighestAltitude(), this);
        player = new Frog(save);
        world = new World(save, player, width, this);

        Camera.moveToYPosition(player.position().y);
        Camera.resetDisplayables();
        Camera.displayables.add(background);
        Camera.displayables.add(hud);
        Camera.displayables.add(world);
        Camera.displayables.add(player);
    }

    public void draw() {
        if(player.isDead())
            reset();

        float deltaTime = 1/frameRate;
        if(deltaTime > 0.1f)
            deltaTime = 0.05f; //Cap deltaTime for when the FPS is too low

        Forces.update();
        world.update(player.position(), screenSize(), deltaTime);
        player.update(saveManager, buttonManager, mousePosition(), screenSize(), world, deltaTime);
        hud.update(player);

        Camera.update(player.position(), new PVector(width, height), deltaTime);
        Camera.displayAll(new PVector(width, height), this);

        saveManager.update(player, deltaTime);
    }

    public PVector screenSize() {
        return new PVector(width, height);
    }
    public PVector mousePosition() {
        return new PVector(mouseX, mouseY)
                        .sub(screenSize().div(2))
                        .add(Camera.position());
    }

    public void keyPressed() {
        buttonManager.buttonPress(keyCode);
    }
    public void keyReleased() {
        buttonManager.buttonRelease(keyCode);
    }
    public void mousePressed() {
        buttonManager.mousePress(mouseButton);
    }
    public void mouseReleased() {
        buttonManager.mouseRelease(mouseButton);
    }
}