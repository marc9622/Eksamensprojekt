import processing.core.PVector;

public class Forces {
    
    private static float nightWind = 20;
    private static float spaceWind = 10;
    private static boolean isWindFacingRight = true;
    private static float secondsPerWindChange = 10;

    private static float gravity = 50;
    private static float dragStrengthX = 0.1f;
    private static float dragStrengthY = 0.05f;
    private static float dragMultiplier = 1.1f;

    private Forces() {}

    public static void update() {
        if(System.currentTimeMillis() % (secondsPerWindChange * 1000d) < (secondsPerWindChange * 1000d) / 2)
            isWindFacingRight = true;
        else
            isWindFacingRight = false;
    }

    public static PVector getGravity(PVector position) {
        float altitude = -position.y;
        float currentGravity = gravity;

        if(altitude > Background.nightAltitude) {
            if(altitude < Background.spaceAltitude) {
                altitude -= Background.nightAltitude;
                altitude /= (Background.spaceAltitude - Background.nightAltitude);
                currentGravity *= 1 - altitude / 2;
            } else
                currentGravity *= 1/2f;
        }

        return new PVector(0, currentGravity);
    }

    public static PVector getWind(PVector position) {
        float altitude = -position.y;
        float currentWind = 0;
        int windDirectionScaler = isWindFacingRight ? 1 : -1;

        if(altitude > Background.sunsetAltitude) {
            if(altitude < Background.nightAltitude) {
                altitude -= Background.sunsetAltitude;
                altitude /= (Background.nightAltitude - Background.sunsetAltitude);
                currentWind = windDirectionScaler * nightWind * altitude;
            } else if(altitude < Background.spaceAltitude) {
                altitude -= Background.nightAltitude;
                altitude /= (Background.spaceAltitude - Background.nightAltitude);
                currentWind = windDirectionScaler * nightWind * (1 - altitude);
                currentWind += windDirectionScaler * spaceWind * altitude;
            } else
                currentWind = windDirectionScaler * spaceWind;
        }
        return new PVector(currentWind, 0);
    }

    public static PVector getDrag(PVector velocity) {
        PVector vel = velocity.copy();
        vel.x *= dragStrengthX;
        vel.y *= dragStrengthY;

        vel.x = vel.x > 0 ?
                (float) Math.pow( vel.x, dragMultiplier) :
               -(float) Math.pow(-vel.x, dragMultiplier);

        vel.y = vel.y > 0 ?
                (float) Math.pow( vel.y, dragMultiplier) :
               -(float) Math.pow(-vel.y, dragMultiplier);

        vel.x = Math.abs(vel.x) > Math.abs(velocity.x) ? velocity.x : vel.x;
        vel.y = Math.abs(vel.y) > Math.abs(velocity.y) ? velocity.y : vel.y;

        return vel.mult(-1);
    }

}
