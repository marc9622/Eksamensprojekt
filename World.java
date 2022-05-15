import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import processing.core.PApplet;
import processing.core.PVector;

public class World implements Displayable{

    private int chunkHeight = 350 / 2; //The height of each chunk of platforms. 350 is currently the max height the player can jump.
    private int chunksLoaded = 0;      //The number of chunks currently loaded.
    private int chunksToLoad = 15;     //The number of chunks that should be loaded.
    private int currentTopChunk = 0;   //The index of the chunk that is currently at the top of the world.
    private float minPlatformDistanceSquared = 100; //The minimum squared distance between platforms.

    private List<Chunk> chunks = new ArrayList<>(chunksToLoad);
    private Chunk[] currentAndAdjacentChunks = new Chunk[5]; //The current chunk, and the 4 adjacent chunks.
                                                             //Used to calculate collisions, so distant chunks are ignored.

    private float coinSpawnChance = 0.1f; //The chance of a coin spawning on a platform.

    private List<Enemy> enemies = new ArrayList<>();
    private float enemySpawnChance = 0.5f; //The chance of an enemy spawning.
    private float enemySpawnCooldown = 1f; //The minimum time between enemy spawns.
    private float timeSinceLastEnemySpawn = -5f; //The time since the last enemy spawn.
                                                 //Negative value is used to delay the first spawn.

    public static void loadImages(PApplet p) {
        Platform.loadImages(p);
        Coin.loadImages(p);
        Enemy.loadImages(p);
    }

    public World(Frog player, float screenWidth, PApplet p) {
        manageChunks(player.altitude(), screenWidth);
    }

    public World(SaveManager.Save save, Frog player, float screenWidth, PApplet p) {
        this(player, screenWidth, p);
        float saveAltitude = save.altitude();
        spawnSinglePlatform(screenWidth / 2, saveAltitude - player.size().y, getPlatformTypes(saveAltitude));
    }

    private void spawnSinglePlatform(float x, float altitude, Platform.Type[] types) {
        chunks.get(getListIndexFromAltitude(altitude)).platforms.add(new Platform(x, -altitude, types));
    }

    //Loads and unloads chunks
    private void manageChunks(float playerAltitude, float screenWidth) {
        int playerCurrentChunk = getIndexFromAltitude(playerAltitude);
        while(playerCurrentChunk + chunksToLoad / 2 > currentTopChunk) {
            if(chunksLoaded >= chunksToLoad)
                removeChunk();
            newChunk(currentTopChunk, screenWidth);
        }
        updateCurrentAndAdjacentChunks(playerAltitude);
    }

    private void newChunk(int index, float screenWidth) {
        chunks.add(new Chunk(index, screenWidth));
        chunksLoaded++;
        currentTopChunk++;
    }

    private void removeChunk() {
        chunks.remove(0);
        chunksLoaded--;
    }

    private void updateCurrentAndAdjacentChunks(float playerAltitude) {
        int playerCurrentChunkAsListIndex = getListIndexFromAltitude(playerAltitude);
        for(int i = playerCurrentChunkAsListIndex - 2, j = 0; i <= playerCurrentChunkAsListIndex + 2; i++ ,j++) {
            if(i < 0 || i >= chunks.size())
                continue;
            currentAndAdjacentChunks[j] = chunks.get(i);
        }
    }

    private Platform.Type[] getPlatformTypes(float altitude) {
        if(altitude < Background.sunsetAltitude)
            return new Platform.Type[]{Platform.Type.BENCH, Platform.Type.CONE, Platform.Type.LEAF, Platform.Type.LILYPAD, Platform.Type.TRASHCAN};
        if(altitude < Background.nightAltitude)
            return new Platform.Type[]{Platform.Type.CONE, Platform.Type.CLOUD, Platform.Type.LEAF, Platform.Type.TRASHCAN};
        if(altitude < Background.spaceAltitude)
            return new Platform.Type[]{Platform.Type.LEAF, Platform.Type.CLOUD};
        return new Platform.Type[]{Platform.Type.CLOUD};
    }

    private int getPlatformPerChunkAmount(int chunkIndex) {
        if(chunkIndex < Background.sunsetAltitude / chunkHeight)
            return 4;
        if(chunkIndex < Background.nightAltitude / chunkHeight)
            return 3;
        if(chunkIndex < Background.spaceAltitude / chunkHeight)
            return 2;
        return 1;
    }

    private int getIndexFromAltitude(float altitude) {
        return (int) (altitude / chunkHeight);
    }

    private int getListIndexFromAltitude(float altitude) {
        return getIndexFromAltitude(altitude) - (currentTopChunk - chunksLoaded);
    }

    private float getAltitudeFromIndex(int index) {
        return index * chunkHeight;
    }

    private float getAltitudeFromListIndex(int index) {
        return getAltitudeFromIndex(index + currentTopChunk - chunksLoaded);
    }

    public float getAltitudeFromLowestChunk() {
        return getAltitudeFromListIndex(0);
    }

    public void update(PVector playerPosition, PVector screenSize, float deltaTime) {
        manageChunks(-playerPosition.y, screenSize.x);
        manageEnemies(playerPosition, screenSize, deltaTime);
        chunks.forEach(c -> c.update(screenSize.x, deltaTime));
        enemies.forEach(e -> e.update(deltaTime));
    }

    private void manageEnemies(PVector playerPosition, PVector screenSize, float deltaTime) {
        enemies.removeIf(e -> e.isOutOfBounds(screenSize.x));
        spawnEnemies(playerPosition, screenSize, deltaTime);
    }

    private void spawnEnemies(PVector playerPosition, PVector screenSize, float deltaTime) {
        if(timeSinceLastEnemySpawn > enemySpawnCooldown) {
            if(ThreadLocalRandom.current().nextFloat() < enemySpawnChance * deltaTime) {
                Enemy.Type[] enemyTypes = getEnemyTypes(-playerPosition.y);
                if(enemyTypes.length == 0)
                    return;
                enemies.add(new Enemy(playerPosition, screenSize, enemyTypes));
                timeSinceLastEnemySpawn = 0;
            }
        } else
            timeSinceLastEnemySpawn += deltaTime;
    }

    private Enemy.Type[] getEnemyTypes(float altitude) {
        if(altitude < Background.sunsetAltitude)
            return new Enemy.Type[]{};
        if(altitude < Background.nightAltitude)
            return new Enemy.Type[]{Enemy.Type.BIRD};
        if(altitude < Background.spaceAltitude)
            return new Enemy.Type[]{Enemy.Type.PLANE};
        return new Enemy.Type[]{Enemy.Type.UFO};
    }

    public void display(PVector cameraPosition, PVector screen, PApplet p) {
        chunks.forEach(d -> d.display(cameraPosition, screen, p));
        enemies.forEach(e -> e.display(cameraPosition, screen, p));
    }

    public List<Platform> getPlatformsAroundPlayer(float playerAltitude) {
        List<Platform> platforms = new ArrayList<>();
        for(Chunk c : currentAndAdjacentChunks)
            if(c != null)
                platforms.addAll(c.platforms);
        return platforms;
    }

    public List<Coin> getCoinsAroundPlayer(float playerAltitude) {
        List<Coin> coins = new ArrayList<>();
        for(Chunk c : currentAndAdjacentChunks)
            if(c != null)
                coins.addAll(c.coins);
        return coins;
    }

    public void removeCoin(Coin coin) {
        for(Chunk c : currentAndAdjacentChunks)
            if(c != null)
                c.removeCoin(coin);
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<Enemy>(enemies);
    }

    private class Chunk implements Displayable {
        public final int index;
        public List<Platform> platforms = null;
        public List<Coin> coins;

        public Chunk(int index, float screenWidth) {
            this.index = index;
            loadChunk(screenWidth * 0.75f); //multiplied by this amount to make the spawn width smaller than the screen width
        }

        //Loads this chunk of platforms.
        private void loadChunk(float spawnWidth) {
            int platformAmount = getPlatformPerChunkAmount(index);
            platforms = new ArrayList<>(platformAmount);
            coins = new ArrayList<>();

            for (int i = 0; i < platformAmount; i++) {
                //Choose coordinates
                PVector position = new PVector();
                //Do-while loop to make sure the platform is not too close to another platform. If it is, choose new coordinates. (This is not the most efficient way to do this, but it's the easiest.)
                do{
                    position.x =  (float) (ThreadLocalRandom.current().nextFloat(-spawnWidth / 2, spawnWidth / 2));
                    position.y = -(float) (ThreadLocalRandom.current().nextFloat(chunkHeight * index, chunkHeight * (index + 1)));
                }
                while(platforms.stream().map(p -> PVector.sub(p.position(), position).magSq()).anyMatch(dist -> dist < minPlatformDistanceSquared));
                //Choose which type of platform to spawn
                Platform.Type[] types = getPlatformTypes(-position.y);
                //Spawn platform
                Platform platform = new Platform(position, types);
                platforms.add(platform);
                addCoin(platform);
            }
        }

        private void addCoin(Platform platform) {
            float randomNumber = ThreadLocalRandom.current().nextFloat();
            if(randomNumber < coinSpawnChance) {
                float x = ThreadLocalRandom.current().nextFloat(platform.position().x - platform.size().x/2, platform.position().x + platform.size().x/2);
                float y = platform.position().y - platform.size().y/2 - Coin.size().y/2 - 10;
                coins.add(new Coin(x, y));
            }
        }
        
        private void removeCoin(Coin coin) {
            coins.remove(coin);
        }

        public void update(float screenWidth, float deltaTime) {
            platforms.forEach(p -> p.update(screenWidth, deltaTime));
            coins.forEach(c -> c.update(deltaTime));
        }

        public void display(PVector cameraPosition, PVector screen, PApplet p) {
            platforms.forEach(d -> d.display(cameraPosition, screen, p));
            coins.forEach(d -> d.display(cameraPosition, screen, p));
        }
    }
}