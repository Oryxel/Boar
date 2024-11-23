package ac.boar.plugin;

import ac.boar.anticheat.Boar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class BoarPlugin extends JavaPlugin {

    public static BoarPlugin PLUGIN;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        PLUGIN = this;
        LOGGER = getLogger();
        Boar.getInstance().init();
    }

    @Override
    public void onDisable() {
        Boar.getInstance().shutdown();
    }

}
