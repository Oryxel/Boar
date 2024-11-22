package ac.boar.plugin;

import ac.boar.anticheat.Boar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class BoarPlugin extends JavaPlugin {

    public static Logger LOGGER;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        try {
            Boar.getInstance().init();
        } catch (Exception e) {
            LOGGER.warning("Please report this to the developer.");
            this.getServer().getPluginManager().disablePlugin(this);

            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        Boar.getInstance().shutdown();
    }

}
