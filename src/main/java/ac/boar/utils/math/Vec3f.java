package ac.boar.utils.math;

import lombok.Getter;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;

@Getter
public class Vec3f implements Cloneable {
    public static final Vec3f ZERO = Vec3f.from(0, 0, 0);

    public float x, y, z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f(Vector3f vector3f) {
        this.x = vector3f.getX();
        this.y = vector3f.getY();
        this.z = vector3f.getZ();
    }

    public Vec3f(Vector3i vector3i) {
        this.x = vector3i.getX();
        this.y = vector3i.getY();
        this.z = vector3i.getZ();
    }

    public static Vec3f from(float x, float y, float z) {
        return new Vec3f(x, y, z);
    }

    public static Vec3f from(double x, double y, double z) {
        return new Vec3f((float) x, (float) y, (float) z);
    }

    public float distanceTo(Vec3f vec) {
        return (float) Math.sqrt(squaredDistanceTo(vec));
    }

    public float squaredDistanceTo(Vec3f vec) {
        float d = vec.x - this.x;
        float e = vec.y - this.y;
        float f = vec.z - this.z;
        return d * d + e * e + f * f;
    }

    public float horizontalLength() {
        return (float) Math.sqrt(this.x * this.x + this.z * this.z);
    }

    public float horizontalLengthSquared() {
        return this.x * this.x + this.z * this.z;
    }

    public Vector3f toVector3f() {
        return Vector3f.from(this.x, this.y, this.z);
    }

    public Vector3d toVector3d() {
        return Vector3d.from(this.x, this.y, this.z);
    }

    public float dot(float x, float y, float z) {
        return this.getX() * x + this.getY() * y + this.getZ() * z;
    }

    public Vec3f add(float v) {
        return this.add(v, v, v);
    }

    public Vec3f add(Vec3f vec3f) {
        return this.add(vec3f.x, vec3f.y, vec3f.z);
    }

    public Vec3f add(float v, float v1, float v2) {
        return Vec3f.from(this.x + v, this.y + v1, this.z + v2);
    }

    public Vec3f sub(Vec3f v) {
        return this.sub(v.getX(), v.getY(), v.getZ());
    }

    public Vec3f sub(double x, double y, double z) {
        return this.sub((float) x, (float) y, (float) z);
    }

    public Vec3f sub(float v, float v1, float v2) {
        return Vec3f.from(this.x - v, this.y - v1, this.z - v2);
    }

    public Vec3f mul(double a) {
        return this.mul((float) a);
    }

    public Vec3f mul(float a) {
        return this.mul(a, a, a);
    }

    public Vec3f mul(float v, float v1, float v2) {
        return Vec3f.from(this.x * v, this.y * v1, this.z * v2);
    }

    public Vec3f mul(Vec3f v) {
        return this.mul(v.getX(), v.getY(), v.getZ());
    }

    public Vec3f div(float v, float v1, float v2) {
        return Vec3f.from(this.x * v, this.y * v1, this.z * v2);
    }

    public Vec3f project(float v, float v1, float v2) {
        float lengthSquared = x * x + y * y + z * z;
        if (Math.abs(lengthSquared) < GenericMath.FLT_EPSILON) {
            throw new ArithmeticException("Cannot project onto the zero vector");
        } else {
            float a = this.dot(x, y, z) / lengthSquared;
            return Vec3f.from(a * x, a * y, a * z);
        }
    }

    public Vec3f cross(float v, float v1, float v2) {
        return Vec3f.from(this.getY() * z - this.getZ() * y, this.getZ() * x - this.getX() * z, this.getX() * y - this.getY() * x);
    }

    public Vec3f pow(float power) {
        return Vec3f.from(Math.pow(this.x, power), Math.pow(this.y, power), Math.pow(this.z, power));
    }

    public Vec3f ceil() {
        return Vec3f.from(Math.ceil(this.getX()), Math.ceil(this.getY()), Math.ceil(this.getZ()));
    }

    public Vec3f floor() {
        return Vec3f.from((float) GenericMath.floor(this.getX()), (float) GenericMath.floor(this.getY()), (float) GenericMath.floor(this.getZ()));
    }

    public Vec3f round() {
        return Vec3f.from((float) Math.round(this.getX()), (float) Math.round(this.getY()), (float) Math.round(this.getZ()));
    }

    public Vec3f abs() {
        return Vec3f.from(Math.abs(this.getX()), Math.abs(this.getY()), Math.abs(this.getZ()));
    }

    public Vec3f negate() {
        return Vec3f.from(-this.getX(), -this.getY(), -this.getZ());
    }

    public Vec3f min(float v, float v1, float v2) {
        return Vec3f.from(Math.min(this.getX(), x), Math.min(this.getY(), y), Math.min(this.getZ(), z));
    }

    public Vec3f max(float v, float v1, float v2) {
        return Vec3f.from(Math.max(this.getX(), x), Math.max(this.getY(), y), Math.max(this.getZ(), z));
    }

    public Vec3f up(float v) {
        return Vec3f.from(this.getX(), this.getY() + v, this.getZ());
    }

    public Vec3f down(float v) {
        return Vec3f.from(this.getX(), this.getY() - v, this.getZ());
    }

    public Vec3f north(float v) {
        return Vec3f.from(this.getX(), this.getY(), this.getZ() - v);
    }

    public Vec3f south(float v) {
        return Vec3f.from(this.getX(), this.getY(), this.getZ() + v);
    }

    public Vec3f east(float v) {
        return Vec3f.from(this.getX() + v, this.getY(), this.getZ());
    }

    public Vec3f west(float v) {
        return Vec3f.from(this.getX() - v, this.getY(), this.getZ());
    }

    public Vec3f normalize() {
        float length = this.length();
        if (Math.abs(length) < GenericMath.FLT_EPSILON) {
            throw new ArithmeticException("Cannot normalize the zero vector");
        } else {
            return Vec3f.from(this.getX() / length, this.getY() / length, this.getZ() / length);
        }
    }

    public float lengthSquared() {
        return this.getX() * this.getX() + this.getY() * this.getY() + this.getZ() * this.getZ();
    }

    public float length() {
        return (float) Math.sqrt(this.lengthSquared());
    }

    public Vec3f clone() {
        return new Vec3f(this.x, this.y, this.z);
    }
}
