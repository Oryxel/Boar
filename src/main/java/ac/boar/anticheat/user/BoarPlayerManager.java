package ac.boar.anticheat.user;

import ac.boar.anticheat.user.api.BoarPlayer;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.HashMap;

public class BoarPlayerManager extends HashMap<GeyserConnection, BoarPlayer> {
    public void add(GeyserConnection connection) {
        final BoarPlayer player = new BoarPlayer(connection);
        player.init();
        this.put(connection, player);
    }
}
