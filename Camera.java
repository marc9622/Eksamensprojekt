import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class Camera {

    public static List<Displayable> displayables = new ArrayList<Displayable>();

    private static PVector followOffset = new PVector(0, 0);
    private static PVector defaultFollowOffset = new PVector(0, -50);
    private static float maxFollowOffset = 400;
    private static float followSpeed = 2f;
    private static PVector deadzone = new PVector(0, 0.15f); //How far the target can move before the camera tries to follow.
                                                          //In percent of the whole screen.
                                                          //0,0 is top left; 1,1 is bottom right
    
    private static PVector position = new PVector();

    private Camera() {}

    public static void update(PVector targetPosition, PVector screenSize, float deltaTime) {
        follow(targetPosition, screenSize, deltaTime);
        followOffset = defaultFollowOffset.copy();
    }

    private static void follow(PVector target, PVector screen, float deltaTime) {
        PVector targetPosition = PVector.add(target, followOffset);

        if(deadzone.x != 0 && Math.abs(targetPosition.x - position.x) > deadzone.x * screen.x)
            if(targetPosition.x > position.x)
                position.x = (position.x - (targetPosition.x - (deadzone.x * screen.x))) * deltaTime * followSpeed;
            else
                position.x = (position.x - (targetPosition.x + (deadzone.x * screen.x))) * deltaTime * followSpeed;

        if(deadzone.y != 0 && Math.abs(targetPosition.y - position.y) > deadzone.y * screen.y)
            if(targetPosition.y > position.y)
                position.y -= (position.y - (targetPosition.y - (deadzone.y * screen.y))) * deltaTime * followSpeed;
            else
                position.y -= (position.y - (targetPosition.y + (deadzone.y * screen.y))) * deltaTime * followSpeed;
    }

    public static void moveOffsetUp(float percent) {
        followOffset.y -= maxFollowOffset * percent;
    }

    public static void moveOffsetDown(float percent) {
        followOffset.y += maxFollowOffset * percent;
    }

    public static void displayAll(PVector screen, PApplet p) {
        displayables.forEach(d -> d.display(subtractHalfScreen(position, screen), screen, p));
    }

    public static void resetDisplayables() {
        displayables.clear();
    }

    //Auxiliary method
    private static PVector subtractHalfScreen(PVector vector, PVector screen) {
        return PVector.sub(vector, PVector.div(screen, 2));
    }

    public static void moveToYPosition(float y) {
        position.y = y;
    }

    public static PVector position() {
        return position.copy();
    }

    public static PVector windowPosToWorldPos(PVector position, PVector screen) {
        return position.copy()
                    .sub(PVector.div(screen, 2))
                    .add(position);
    }

    //Checks if the position is inside the screen.
    public static boolean isInside(PVector position, PVector screen) {
        PVector vector = PVector.sub(position, position);
        float w = screen.x/2;
        float h = screen.y/2;
        return vector.x > -w && vector.x < w && vector.y > -h && vector.y < h;
    }

}