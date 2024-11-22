package ac.boar.utils;

public class StringUtils {
    public static String addNamespaceIfNeeded(String name) {
        String[] split = name.split(":");
        if (split.length == 1) {
            return "minecraft:" + name;
        }

        return name;
    }
}
