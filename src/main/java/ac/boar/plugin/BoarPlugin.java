package ac.boar.plugin;

import ac.boar.anticheat.Boar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class BoarPlugin extends JavaPlugin {

    public static Logger LOGGER;

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        Boar.getInstance().init();
    }

    @Override
    public void onDisable() {
        Boar.getInstance().shutdown();
    }

}
