package ac.boar.utils;

public class MathUtil {
    public static int floor(float value) {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }

    public static long lfloor(double value) {
        long l = (long)value;
        return value < (double)l ? l - 1L : l;
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static int abs(int value) {
        return Math.abs(value);
    }

    public static int ceil(float value) {
        int i = (int)value;
        return value > (float)i ? i + 1 : i;
    }

    public static int ceil(double value) {
        int i = (int)value;
        return value > (double)i ? i + 1 : i;
    }

    public static double toValue(double value, double target) {
        if (Double.isNaN(value) || Double.isInfinite(value) && value > 0) {
            return target;
        } else if (Double.isInfinite(value) && value < 0) {
            return -target;
        }

        return value == 0 ? 0 : value > 0 ? target : -target;
    }
}
