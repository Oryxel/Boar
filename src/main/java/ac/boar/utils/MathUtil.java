package ac.boar.utils;

public class MathUtil {
    public static int floor(float value) {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static int ceil(float value) {
        int i = (int)value;
        return value > (float)i ? i + 1 : i;
    }

    public static float toValue(float value, float target) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0;
        }

        return value == 0 ? 0 : value > 0 ? target : -target;
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }
}
