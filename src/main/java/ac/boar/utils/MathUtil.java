package ac.boar.utils;

import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.TrigMath;
import org.cloudburstmc.math.vector.Vector3i;

public final class MathUtil {
    public static int pack(Vector3i vector3i) {
        return (vector3i.getY() + vector3i.getZ() * 31) * 31 + vector3i.getX();
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
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

    public static float square(float n) {
        return n * n;
    }

    public static Vec3f getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = TrigMath.cos(g);
        float i = TrigMath.sin(g);
        float j = TrigMath.cos(f);
        float k = TrigMath.sin(f);
        return new Vec3f(i * j, -k, h * j);
    }
}
