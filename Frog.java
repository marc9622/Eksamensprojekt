import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class Frog implements Displayable {

    //#region Images
    private static PImage frogSit = null;
    private static PImage frogSitClosed = null;
    private static PImage frogSitRight = null;
    private static PImage frogSitLeft = null;
    private static PImage frogAirUp = null;
    private static PImage frogAirDown = null;
    private static PImage frogAirRight = null;
    private static PImage frogAirLeft = null;
    private static PImage frogDead = null;
    //#endregion

    private static float imageYOffset = 20;

    private boolean isFacingRight = false;
    private boolean isFacingLeft = false;
    private boolean isOnPlatform = false; //Call collide each frame to check if on a platform
    private float timeUntilIdle = 0.25f;
    private float timeUntilSleep = 2f;
    private float timeStandingStill = 0;
    
    private static PVector size = new PVector(80, 80);
    private PVector position = new PVector();
    private PVector velocity = new PVector(0,0);
    private float flyAcceleration = 0.75f;
    private float walkSpeed = 2f;

    private float jumpStrength = 500f;
    private float jumpTime = 0;
    private float maxJumpTime = 0.50f; //in seconds

    private Tongue tongue = new Tongue(1, 2000, 250);

    private int coins = 0;

    private boolean isDead = false;

    public Frog() {
    }

    public Frog(SaveManager.Save save) {
        this.position.y = -save.altitude();
        this.coins = save.coins();
    }

    public static void loadImages(PApplet p) {
        frogSit =       p.loadImage("assets/frog/frogSit.png");
        frogSitClosed = p.loadImage("assets/frog/frogSitBlink.png");
        frogSitRight =  p.loadImage("assets/frog/frogSitRight.png");
        frogSitLeft =   p.loadImage("assets/frog/frogSitLeft.png");
        frogAirUp =     p.loadImage("assets/frog/frogAirUp.png");
        frogAirDown =   p.loadImage("assets/frog/frogAirDown.png");
        frogAirRight =  p.loadImage("assets/frog/frogAirRight.png");
        frogAirLeft =   p.loadImage("assets/frog/frogAirLeft.png");
        frogDead =      p.loadImage("assets/frog/frogDead.png");

        frogSit         .resize((int)size.x, (int)size.y);
        frogSitClosed   .resize((int)size.x, (int)size.y);
        frogSitRight    .resize((int)size.x, (int)size.y);
        frogSitLeft     .resize((int)size.x, (int)size.y);
        frogAirUp       .resize((int)size.x, (int)size.y);
        frogAirDown     .resize((int)size.x, (int)size.y);
        frogAirRight    .resize((int)size.x, (int)size.y);
        frogAirLeft     .resize((int)size.x, (int)size.y);
        frogDead        .resize((int)size.x, (int)size.y);
    }

    public void update(SaveManager saveManager, ButtonManager buttonManager, PVector mousePosition, PVector screen, World world, float deltaTime) {
        if(isBelowLowestChunk(world))
            die(saveManager);
        if(isDead)
            return;

        List<Platform> closePlatforms = world.getPlatformsAroundPlayer(-position.y);
        List<Coin> closeCoins = world.getCoinsAroundPlayer(-position.y);
        List<Enemy> enemies = world.getEnemies();

        move(buttonManager, deltaTime);
        manageCollision(saveManager, buttonManager, closePlatforms, closeCoins, enemies, world);
        shoot(buttonManager, mousePosition, world, deltaTime);
        tongue.update(closeCoins, enemies, world, screen, deltaTime);
        moveCamera(buttonManager);
    }

    private boolean isBelowLowestChunk(World world) {
        return altitude() < world.getAltitudeFromLowestChunk();
    }

    private void moveCamera(ButtonManager buttonManager) {
        if (buttonManager.isBindingPressed(ButtonManager.Binding.CAM_UP))
            Camera.moveOffsetUp(1f);
        else if(buttonManager.isBindingPressed(ButtonManager.Binding.JUMP))
            Camera.moveOffsetUp(0.5f);
        if (buttonManager.isBindingPressed(ButtonManager.Binding.CAM_DOWN))
            Camera.moveOffsetDown(1f);
        else if(buttonManager.isBindingPressed(ButtonManager.Binding.FALL))
            Camera.moveOffsetDown(0.5f);
    }

    //#region Collision
    private void manageCollision(SaveManager saveManager, ButtonManager buttonManager, List<Platform> closePlatforms, List<Coin> coins, List<Enemy> enemies, World world) {
        managePlatformCollision(buttonManager, closePlatforms);
        manageCoinCollision(coins, world);
        manageEnemyCollision(saveManager, enemies);
    }

    //Detect collision and move frog out of overlap
    private boolean managePlatformCollision(ButtonManager buttonManager, List<Platform> closePlatforms) {
        isOnPlatform = false;
        PlatformAndDistance closestPlatform = null;

        //Check if frog is moving down
        if(velocity.y >= 0) {
            //If frog is below altitude 0, then move it up to 0.
            if(altitude() < 0) {
                position.y = 0;
                velocity.y = 0;
                isOnPlatform = true;
                return false;
            }
            //If the player is pressing FALL, then ignore platforms
            if(buttonManager.isBindingPressed(ButtonManager.Binding.FALL))
                return false;
            //Check if the frog is on a platform
            closestPlatform = getClosestCollidingPlatform(closePlatforms);
            isOnPlatform = closestPlatform != null;
            //If the frog is on a platform, move it up onto the platform to avoid overlap
            if(isOnPlatform) {
                position.y += closestPlatform.distance - closestPlatform.platform.size().y / 2 - size.y / 2;
                velocity.y = 0;
            }
        }
        return isOnPlatform;
    }

    private PlatformAndDistance getClosestCollidingPlatform(List<Platform> closePlatforms) {
        return closePlatforms.stream()
                //Filter the platforms that share x-coordinate with the frog
                .filter(p -> Math.abs(p.position().x - position.x) < p.size().x / 2)
                //Map the platforms to a PlatformAndDistance object to keep track of the distance and object
                .map(p -> new PlatformAndDistance(p, p.position().y - position.y))
                //Filter the platforms whose distance is less than the frog's height plus its y-velocity (interpolation)
                .filter(pd -> pd.distance >= 0 && pd.distance <= velocity.y + size.y)
                //Get the closest platform
                .min((a,b) -> Float.compare(a.distance, b.distance))
                //If none is found, return null
                .orElse(null);
    }

    private record PlatformAndDistance(Platform platform, float distance) {}

    private boolean manageCoinCollision(List<Coin> closeCoins, World world) {
        Coin[] coinsInRange = null;
        coinsInRange = closeCoins.stream()
                .filter(c -> Math.abs(c.position().x - position.x) < Coin.size().x/2)
                .filter(c -> Math.abs(c.position().y - position.y + imageYOffset) < Coin.size().y/2)
                .toArray(Coin[]::new);
        
        for(Coin c : coinsInRange) {
            gainCoin();
            world.removeCoin(c);
        }
        return coinsInRange.length > 0;
    }

    private boolean manageEnemyCollision(SaveManager saveManager, List<Enemy> closeEnemies) {
        Enemy[] enemiesInRange = null;
        enemiesInRange = closeEnemies.stream()
                .filter(e -> Math.abs(e.position().x - position.x) < e.size().x/3)
                .filter(e -> Math.abs(e.position().y - position.y + imageYOffset) < e.size().y/3)
                .toArray(Enemy[]::new);

        if(enemiesInRange.length > 0)
            die(saveManager);

        return enemiesInRange.length > 0;
    }
    //#endregion

    //#region Movement
    private void move(ButtonManager buttonManager, float deltaTime) {
        fly(buttonManager, deltaTime); //TODO remove this when game is finished

        //Modify velocity based on buttons
        addHorizontalVelocity(buttonManager, isOnPlatform, deltaTime);
        addVerticalVelocity(buttonManager, isOnPlatform, deltaTime);
        //Update frog's facing direction
        setFacingDirection(deltaTime);
        //Add velocity to position
        position.add(velocity);
        //Add drag to velocity
        addDrag(isOnPlatform);
    }

    //For testing purposes
    private void fly(ButtonManager buttonManager, float deltaTime) {
        if(buttonManager.isButtonPressed('F'))
            velocity.y = -jumpStrength * deltaTime;
    }

    private void addHorizontalVelocity(ButtonManager buttonManager, boolean isOnGround, float deltaTime) {
        float x = 0;
        
        x += buttonManager.isBindingPressed(ButtonManager.Binding.MOVE_RIGHT) ? + 1 : 0;
        x += buttonManager.isBindingPressed(ButtonManager.Binding.MOVE_LEFT)  ? - 1 : 0;
        
        if(isOnGround)
            velocity.x = x * walkSpeed; //if on ground, set horizontal velocity to walk speed
        else          
            velocity.x += x * flyAcceleration; //if in air, add fly acceleration to horizontal velocity
        
        velocity.add(Forces.getWind(position).mult(deltaTime));
    }

    private void setFacingDirection(float deltaTime) {
        if(velocity.x != 0) {
            if(velocity.x > 0) {
                isFacingRight = true; isFacingLeft = false;
            }
            else
            if(velocity.x < 0) {
                isFacingRight = false; isFacingLeft = true;
            }
            timeStandingStill = 0;
        }
        else
            timeStandingStill += deltaTime;

        if(timeStandingStill > timeUntilIdle) {
            isFacingRight = false; isFacingLeft = false;
        }
    }

    private void addVerticalVelocity(ButtonManager buttonManager, boolean isOnGround, float deltaTime) {
        velocity.add(Forces.getGravity(position).mult(deltaTime));

        boolean isMoving = velocity.y == 0;

        //if the player is standing and not jumping, the player can start jumping
        if(isOnGround && !isMoving) {
            //if the player is holding down the jump button, then jump
            if(jumpTime <= maxJumpTime && buttonManager.isBindingPressed(ButtonManager.Binding.JUMP)) {
                jumpTime += deltaTime;
                velocity.y = -jumpStrength * deltaTime;
            //if the player is not holding down the jump button, then stop jumping
            } else {
                jumpTime = 0;
                buttonManager.bindRelease(ButtonManager.Binding.JUMP);
            }
        } else {
            //if the player is jumping, and...
            if(jumpTime > 0) {
                //if the player is holding down the jump button, and...
                if(buttonManager.isBindingPressed(ButtonManager.Binding.JUMP)) {
                    //if the jump time still hasn't reached the max jump time, then continue to jump
                    if(jumpTime < maxJumpTime) {
                        jumpTime += deltaTime;
                        velocity.y = -jumpStrength * deltaTime;
                    //if the jump time has reached the max jump time, then stop jumping and release the jump button
                    } else {
                        jumpTime = 0;
                        buttonManager.bindRelease(ButtonManager.Binding.JUMP);
                    }
                //if the player is not holding down the jump button, then stop jumping
                } else
                    jumpTime = 0;
            }
        }
    }

    private void addDrag(boolean isOnGround) {
        PVector drag = Forces.getDrag(velocity);

        velocity.x += drag.x;
        
        if(velocity.y > 0)
            velocity.y += drag.y;
    }
    //#endregion

    private void shoot(ButtonManager buttonManager, PVector mousePosition, World world, float deltaTime) {
        if(buttonManager.isBindingPressed(ButtonManager.Binding.SHOOT)) {
            tongue.shoot(position, mousePosition);
            buttonManager.bindRelease(ButtonManager.Binding.SHOOT);
        }
    }

    private void gainCoin() {
        coins++;
    }

    private void die(SaveManager saveManager) {
        if(isDead)
            return;
        isDead = true;
        saveManager.saveCurrent(this);
    }

    //#region Display
    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        tongue.display(cameraPosition, screen, p);

        PImage image = getCurrentImage();
        if(image == null)
            loadImages(p);
        p.imageMode(PApplet.CENTER);
        p.image(image, (int) (position.x - cameraPosition.x), (int) (position.y - cameraPosition.y + imageYOffset));
    }

    private PImage getCurrentImage() {
        if(isDead)
            return frogDead;

        if(isOnPlatform) {
            if(isFacingRight) return frogSitRight;
            if(isFacingLeft) return frogSitLeft;
            
            if(timeStandingStill > timeUntilSleep)
                if(System.currentTimeMillis() % 5000 < 500)
                    return frogSitClosed;
            return frogSit;
        }
        if(velocity.y > 0) {
            if(velocity.x >  velocity.y) return frogAirRight;
            if(velocity.x < -velocity.y) return frogAirLeft;
            return frogAirDown;
        }
        if(velocity.y < 0) {
            if(velocity.x > -velocity.y) return frogAirRight;
            if(velocity.x <  velocity.y) return frogAirLeft;
            return frogAirUp;
        }
        return frogSit;
    }
    //#endregion

    //#region Getters
    public PVector position() {
        return position;
    }

    public float altitude() {
        return -position.y;
    }
    
    public int coins() {
        return coins;
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    public PVector size() {
        return size;
    }
    //#endregion

    public class Tongue extends Shooter {

        private PVector relativePosition = null;
        private PVector velocity = null;
        private float maxLength;

        private boolean isShooting = false;
        private boolean goingOut = false;

        private float size = 5;

        public Tongue(float damage, float speed, float maxLength) {
            super(damage, speed);
            this.maxLength = maxLength;
        }

        public void shoot(PVector position, PVector target) {
            if(!isShooting) {
                this.relativePosition = new PVector(0, 0);

                this.velocity = PVector.sub(target, position);
                this.velocity.normalize();
                this.velocity.mult(speed);

                isShooting = true;
                goingOut = true;
            }
        }

        public void update(List<Coin> closeCoins, List<Enemy> enemies, World world, PVector screen, float deltaTime) {
            if(isShooting) {
                manageCollision(closeCoins, enemies, world, deltaTime);

                if(goingOut) {
                    relativePosition.add(PVector.mult(velocity, deltaTime));
                    if(relativePosition.magSq() > Math.pow(maxLength, 2)) {
                        goingOut = false;
                    }
                }
                else {
                    relativePosition.sub(PVector.mult(velocity, deltaTime));
                    if(!isEqualSign(relativePosition, velocity)) {
                        isShooting = false;
                    }
                }
                timeStandingStill = 0;
                if(velocity.x > 0) {
                    isFacingRight = true; isFacingLeft = false;
                }
                else
                if(velocity.x < 0) {
                    isFacingRight = false; isFacingLeft = true;
                }
            }
        }

        private boolean isEqualSign(PVector a, PVector b) {
            return a.x * b.x > 0 && a.y * b.y > 0;
        }

        private void manageCollision(List<Coin> coins, List<Enemy> enemies, World world, float deltaTime) {
            manageCoinCollision(coins, world);
            manageEnemyCollision(enemies, world, deltaTime);
        }
        
        private boolean manageCoinCollision(List<Coin> coins, World world) {
            PVector absolutePosition = PVector.add(relativePosition, Frog.this.position);
            Coin[] coinsInRange = null;
            coinsInRange = coins.stream()
                    .filter(c -> Math.abs(c.position().x - absolutePosition.x) < size + Coin.size().x / 2)
                    .filter(c -> Math.abs(c.position().y - absolutePosition.y + imageYOffset) < size + Coin.size().y / 2)
                    .toArray(Coin[]::new);
            
            for(Coin c : coinsInRange) {
                gainCoin();
                world.removeCoin(c);
            }

            return coinsInRange.length > 0;
        }

        private boolean manageEnemyCollision(List<Enemy> enemies, World world, float deltaTime) {
            PVector absolutePosition = PVector.add(relativePosition, Frog.this.position);
            Enemy[] enemiesInRange = null;
            enemiesInRange = enemies.stream()
                    .filter(e -> Math.abs(e.position().x - absolutePosition.x) < size + e.size().x / 2)
                    .filter(e -> Math.abs(e.position().y - absolutePosition.y + imageYOffset) < size + e.size().y / 2)
                    .toArray(Enemy[]::new);
            
            for(Enemy e : enemiesInRange)
                e.collide(deltaTime);

            return enemiesInRange.length > 0;
        }

        public void display(PVector cameraPosition, PVector screen, PApplet p) {
            if(isShooting) {
                p.stroke(255, 0, 0);
                p.strokeWeight(size);
                PVector absolutePosition = PVector.add(relativePosition, Frog.this.position).sub(cameraPosition);
                PVector frogPosition = PVector.sub(Frog.this.position, cameraPosition);
                p.line(frogPosition.x, frogPosition.y, absolutePosition.x, absolutePosition.y);
            }
        }
        
    }

}