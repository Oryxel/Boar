package ac.boar.anticheat.event.spigot;

import ac.boar.anticheat.Boar;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.GeyserUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final GeyserConnection connection = GeyserApi.api().connectionByUuid(event.getPlayer().getUniqueId());
        if (connection == null) {
            return;
        }

        BoarPlayer player = Boar.getInstance().getPlayerManager().get(connection);
        if (player == null) {
            return;
        }

        GeyserUtil.hookJavaSession(player);
    }
}
