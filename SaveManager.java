import de.bezier.data.sql.SQLite;
import processing.core.PApplet;

public class SaveManager {
    
    private SQLite database = null;
    private boolean isConnected = false;

    private Save currentSave = null;

    private float timeSinceLastSave = 0;
    private float saveInterval = 15; //in seconds

    public SaveManager(String databaseName, PApplet p) {
        database = new SQLite(p, databaseName);
    }

    public void update(Frog player, float deltaTime) {
        timeSinceLastSave += deltaTime;
        if (!player.isDead() && timeSinceLastSave > saveInterval) {
            Runnable save = () -> saveCurrent(player);
            new Thread(save).start();
            timeSinceLastSave = 0;
        }
    }

    private boolean connect() {
        if(isConnected || database.connect())
            return isConnected = true;
        return false;
    }

    public Save getAliveOrNew() {
        if(connect()) {
            database.query("SELECT * FROM saves WHERE isAlive = 1");

            if(database.next()) {
                int id   = database.getInt("id");
                int altitude    = database.getInt("altitude");
                int coins       = database.getInt("coins");
                boolean isAlive = database.getBoolean("isAlive");

                PApplet.println("Found alive save with id: " + id + ", altitude: " + altitude + ", coins: " + coins + ", isAlive: " + isAlive);
                return currentSave = new Save(id, altitude, coins, isAlive);
            }
            PApplet.println("No alive save found, creating new one and trying again");
            database.query("INSERT INTO saves (altitude, coins, isAlive) VALUES (0, 0, 1)");

            return getAliveOrNew();
        }
        PApplet.println("Could not connect to database");
        return new Save();
    }

    public int getHighestAltitude() {
        if(connect() && currentSave != null) {
            database.query("SELECT id, altitude FROM saves");

            int highestAltitude = 0;

            while(database.next()) {
                if(currentSave.id == database.getInt("id"))
                    continue;
                
                int altitude = database.getInt("altitude");
                if(altitude > highestAltitude)
                    highestAltitude = altitude;
            }
            PApplet.println("Highest altitude: " + highestAltitude);
            return highestAltitude;
        }
        PApplet.println("Could not get highest altitude");
        return 0;
    }

    public int getHighestCoins() {
        if(connect() && currentSave != null) {
            database.query("SELECT id, coins FROM saves");

            int highestCoins = 0;

            while(database.next()) {
                if(currentSave.id == database.getInt("id"))
                    continue;
                
                int coins = database.getInt("coins");
                if(coins > highestCoins)
                    highestCoins = coins;
            }
            PApplet.println("Highest coins: " + highestCoins);
            return highestCoins;
        }
        PApplet.println("Could not get highest coins");
        return 0;
    }

    public void saveCurrent(int altitude, int coins, boolean isAlive) {
        if(!connect() || currentSave == null || currentSave.id == -1) {
            PApplet.println("Could not save current save, no current save found");
            return;
        }
        PApplet.println("Saving current save with id: " + currentSave.id + ", altitude: " + altitude + ", coins: " + coins + ", isAlive: " + isAlive);
        database.query("UPDATE saves SET altitude = " + altitude + ", coins = " + coins + ", isAlive = " + (isAlive ? 1 : 0) + " WHERE id = " + currentSave.id);
    }

    public void saveCurrent(Frog player) {
        saveCurrent((int) player.altitude(), player.coins(), !player.isDead());
    }

    public record Save(int id, int altitude, int coins, boolean isAlive) {

        public Save() {
            this(-1, 0, 0, true);
        }

    }

}