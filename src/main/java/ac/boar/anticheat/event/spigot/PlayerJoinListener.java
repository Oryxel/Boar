package ac.boar.anticheat.event.spigot;

import ac.boar.anticheat.Boar;
import ac.boar.plugin.BoarPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // I was planning to use geyser events for this but it normally too late or too early so we have to do this :(
        final GeyserConnection connection = GeyserApi.api().connectionByUuid(event.getPlayer().getUniqueId());
        if (connection == null) {
            return;
        }

        BoarPlugin.LOGGER.info(event.getPlayer().getDisplayName() + " joined!");
        Boar.getInstance().getPlayerManager().add(connection);
    }
}
