package ac.boar.utils;

import org.bukkit.Bukkit;

public class ChatUtil {
    public static final String PREFIX = "§3Boar §7>§r ";

    public static void alert(Object message) {
        Bukkit.broadcast(ChatUtil.PREFIX + "§3" + message, "boar.alert");
    }
}
