package ac.boar.utils.math;

import it.unimi.dsi.fastutil.floats.AbstractFloatList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.geysermc.geyser.level.physics.Axis;

import java.util.Optional;

public class BoundingBox implements Cloneable {
    private static final float EPSILON = 1.0E-7F;
    private static final float MAX_TOLERANCE_ERROR = 2.0E-5F;
    private static final float TOLERANCE_DISTANCE = 1.0E-5F;
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    public BoundingBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public BoundingBox(org.geysermc.geyser.level.physics.BoundingBox boundingBox) {
        this.minX = (float) boundingBox.getMin(Axis.X);
        this.minY = (float) boundingBox.getMin(Axis.Y);
        this.minZ = (float) boundingBox.getMin(Axis.Z);
        this.maxX = (float) boundingBox.getMax(Axis.X);
        this.maxY = (float) boundingBox.getMax(Axis.Y);
        this.maxZ = (float) boundingBox.getMax(Axis.Z);
    }

    public static BoundingBox getBoxAt(float x, float y, float z, float width, float height) {
        float f = width / 2.0f;
        return new BoundingBox(x - f, y, z - f, x + f, y + height, z + f);
    }

    public static FloatList gather(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (maxX - minX < 1.0E-7 || maxY - minY < 1.0E-7 || maxZ - minZ < 1.0E-7) {
            return FloatList.of();
        }
        int i = findRequiredBitResolution(minX, maxX);
        int j = findRequiredBitResolution(minY, maxY);
        int k = findRequiredBitResolution(minZ, maxZ);
        if (i < 0 || j < 0 || k < 0) {
            return FloatArrayList.wrap(new float[]{minY, maxY});
        }
        if (i == 0 && j == 0 && k == 0) {
            return FloatArrayList.wrap(new float[]{0, 1});
        }

        int m = 1 << j;
        return new AbstractFloatList() {
            @Override
            public float getFloat(int i) {
                return i / (float) m;
            }

            @Override
            public int size() {
                return m + 1;
            }
        };
    }

    public static Optional<Vec3f> raycast(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Vec3f from, Vec3f to) {
        float[] ds = new float[]{1.0F};
        float d = to.x - from.x;
        float e = to.y - from.y;
        float f = to.z - from.z;
        Direction direction = traceCollisionSide(minX, minY, minZ, maxX, maxY, maxZ, from, ds, null, d, e, f);
        if (direction == null) {
            return Optional.empty();
        } else {
            float g = ds[0];
            return Optional.of(from.add(g * d, g * e, g * f));
        }
    }

    private static Direction traceCollisionSide(BoundingBox BoundingBox, Vec3f intersectingVector, float[] traceDistanceResult, Direction approachDirection, float deltaX, float deltaY, float deltaZ) {
        return traceCollisionSide(BoundingBox.minX, BoundingBox.minY, BoundingBox.minZ, BoundingBox.maxX, BoundingBox.maxY, BoundingBox.maxZ, intersectingVector, traceDistanceResult, approachDirection, deltaX, deltaY, deltaZ);
    }

    private static Direction traceCollisionSide(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Vec3f intersectingVector, float[] traceDistanceResult, Direction approachDirection, float deltaX, float deltaY, float deltaZ) {
        if (deltaX > 1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaX, deltaY, deltaZ, minX, minY, maxY, minZ, maxZ, Direction.WEST, intersectingVector.x, intersectingVector.y, intersectingVector.z);
        } else if (deltaX < -1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaX, deltaY, deltaZ, maxX, minY, maxY, minZ, maxZ, Direction.EAST, intersectingVector.x, intersectingVector.y, intersectingVector.z);
        }

        if (deltaY > 1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaY, deltaZ, deltaX, minY, minZ, maxZ, minX, maxX, Direction.DOWN, intersectingVector.y, intersectingVector.z, intersectingVector.x);
        } else if (deltaY < -1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaY, deltaZ, deltaX, maxY, minZ, maxZ, minX, maxX, Direction.UP, intersectingVector.y, intersectingVector.z, intersectingVector.x);
        }

        if (deltaZ > 1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaZ, deltaX, deltaY, minZ, minX, maxX, minY, maxY, Direction.NORTH, intersectingVector.z, intersectingVector.x, intersectingVector.y);
        } else if (deltaZ < -1.0E-7) {
            approachDirection = traceCollisionSide(traceDistanceResult, approachDirection, deltaZ, deltaX, deltaY, maxZ, minX, maxX, minY, maxY, Direction.SOUTH, intersectingVector.z, intersectingVector.x, intersectingVector.y);
        }

        return approachDirection;
    }

    private static Direction traceCollisionSide(float[] traceDistanceResult, Direction approachDirection, float deltaX, float deltaY, float deltaZ, float begin, float minX, float maxX, float minZ, float maxZ, Direction resultDirection, float startX, float startY, float startZ) {
        float d = (begin - startX) / deltaX;
        float e = startY + d * deltaY;
        float f = startZ + d * deltaZ;
        if (0.0 < d && d < traceDistanceResult[0] && minX - 1.0E-7 < e && e < maxX + 1.0E-7 && minZ - 1.0E-7 < f && f < maxZ + 1.0E-7) {
            traceDistanceResult[0] = d;
            return resultDirection;
        } else {
            return approachDirection;
        }
    }

    public static BoundingBox of(Vec3f center, float dx, float dy, float dz) {
        return new BoundingBox(center.x - dx / 2.0F, center.y - dy / 2.0F, center.z - dz / 2.0F, center.x + dx / 2.0F, center.y + dy / 2.0F, center.z + dz / 2.0F);
    }

    protected static int findRequiredBitResolution(float min, float max) {
        if (min < -1.0E-7 || max > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            int j = 1 << i;
            float d = min * (float) j;
            float e = max * (float) j;
            boolean bl = Math.abs(d - (float) Math.round(d)) < 1.0E-7 * (float) j;
            boolean bl2 = Math.abs(e - (float) Math.round(e)) < 1.0E-7 * (float) j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    public Vec3f toVec3f(float width) {
        return new Vec3f(this.minX + (width / 2F), this.minY, this.maxZ - (width / 2F));
    }

    public FloatList getPointPositions() {
        return gather(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public float chooseMin(Axis axis) {
        return switch (axis) {
            case X -> this.minX;
            case Y -> this.minY;
            default -> this.minZ;
        };
    }

    public float chooseMax(Axis axis) {
        return switch (axis) {
            case X -> this.maxX;
            case Y -> this.maxY;
            default -> this.maxZ;
        };
    }

    public boolean isOverlapped(Axis axis, BoundingBox other) {
        return switch (axis) {
            case X -> other.maxY - this.minY > TOLERANCE_DISTANCE && this.maxY - other.minY > TOLERANCE_DISTANCE && other.maxZ - this.minZ > TOLERANCE_DISTANCE && this.maxZ - other.minZ > TOLERANCE_DISTANCE;
            case Y -> other.maxX - this.minX > TOLERANCE_DISTANCE && this.maxX - other.minX > TOLERANCE_DISTANCE && other.maxZ - this.minZ > TOLERANCE_DISTANCE && this.maxZ - other.minZ > TOLERANCE_DISTANCE;
            default -> other.maxX - this.minX > TOLERANCE_DISTANCE && this.maxX - other.minX > TOLERANCE_DISTANCE && other.maxY - this.minY > TOLERANCE_DISTANCE && this.maxY - other.minY >TOLERANCE_DISTANCE;
        };
    }

    public float calculateMaxDistance(Axis axis, BoundingBox other, float maxDist) {
        if (!isOverlapped(axis, other) || maxDist == 0) {
            return maxDist;
        }

        if (maxDist > 0) {
            float d1 = chooseMin(axis) - other.chooseMax(axis);

            if (d1 >= -MAX_TOLERANCE_ERROR) {
                maxDist = Math.min(maxDist, d1);
            }
        } else {
            float d0 = chooseMax(axis) - other.chooseMin(axis);

            if (d0 <= MAX_TOLERANCE_ERROR) {
                maxDist = Math.max(maxDist, d0);
            }
        }
        return maxDist;
    }

    public BoundingBox withMinX(float minX) {
        return new BoundingBox(minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMinY(float minY) {
        return new BoundingBox(this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMinZ(float minZ) {
        return new BoundingBox(this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMaxX(float maxX) {
        return new BoundingBox(this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMaxY(float maxY) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public BoundingBox withMaxZ(float maxZ) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BoundingBox BoundingBox)) {
            return false;
        } else {
            if (Double.compare(BoundingBox.minX, this.minX) != 0) {
                return false;
            } else if (Double.compare(BoundingBox.minY, this.minY) != 0) {
                return false;
            } else if (Double.compare(BoundingBox.minZ, this.minZ) != 0) {
                return false;
            } else if (Double.compare(BoundingBox.maxX, this.maxX) != 0) {
                return false;
            } else if (Double.compare(BoundingBox.maxY, this.maxY) != 0) {
                return false;
            } else {
                return Double.compare(BoundingBox.maxZ, this.maxZ) == 0;
            }
        }
    }

    public BoundingBox shrink(float x, float y, float z) {
        float d = this.minX;
        float e = this.minY;
        float f = this.minZ;
        float g = this.maxX;
        float h = this.maxY;
        float i = this.maxZ;
        if (x < 0.0) {
            d -= x;
        } else if (x > 0.0) {
            g -= x;
        }

        if (y < 0.0) {
            e -= y;
        } else if (y > 0.0) {
            h -= y;
        }

        if (z < 0.0) {
            f -= z;
        } else if (z > 0.0) {
            i -= z;
        }

        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox stretch(Vec3f scale) {
        return this.stretch(scale.x, scale.y, scale.z);
    }

    public BoundingBox stretch(float x, float y, float z) {
        float d = this.minX;
        float e = this.minY;
        float f = this.minZ;
        float g = this.maxX;
        float h = this.maxY;
        float i = this.maxZ;
        if (x < 0.0) {
            d += x;
        } else if (x > 0.0) {
            g += x;
        }

        if (y < 0.0) {
            e += y;
        } else if (y > 0.0) {
            h += y;
        }

        if (z < 0.0) {
            f += z;
        } else if (z > 0.0) {
            i += z;
        }

        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox expand(float x, float y, float z) {
        float d = this.minX - x;
        float e = this.minY - y;
        float f = this.minZ - z;
        float g = this.maxX + x;
        float h = this.maxY + y;
        float i = this.maxZ + z;
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox expand(float value) {
        return this.expand(value, value, value);
    }

    public BoundingBox intersection(BoundingBox BoundingBox) {
        float d = Math.max(this.minX, BoundingBox.minX);
        float e = Math.max(this.minY, BoundingBox.minY);
        float f = Math.max(this.minZ, BoundingBox.minZ);
        float g = Math.min(this.maxX, BoundingBox.maxX);
        float h = Math.min(this.maxY, BoundingBox.maxY);
        float i = Math.min(this.maxZ, BoundingBox.maxZ);
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox union(BoundingBox BoundingBox) {
        float d = Math.min(this.minX, BoundingBox.minX);
        float e = Math.min(this.minY, BoundingBox.minY);
        float f = Math.min(this.minZ, BoundingBox.minZ);
        float g = Math.max(this.maxX, BoundingBox.maxX);
        float h = Math.max(this.maxY, BoundingBox.maxY);
        float i = Math.max(this.maxZ, BoundingBox.maxZ);
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox offset(float x, float y, float z) {
        return new BoundingBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public BoundingBox offset(Vec3f vec) {
        return this.offset(vec.x, vec.y, vec.z);
    }

    public boolean intersects(BoundingBox BoundingBox) {
        return this.intersects(BoundingBox.minX, BoundingBox.minY, BoundingBox.minZ, BoundingBox.maxX, BoundingBox.maxY, BoundingBox.maxZ);
    }

    public boolean intersects(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public boolean intersects(Vec3f pos1, Vec3f pos2) {
        return this.intersects(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z), Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
    }

    public boolean contains(Vec3f pos) {
        return this.contains(pos.x, pos.y, pos.z);
    }

    public boolean contains(float x, float y, float z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public float getAverageSideLength() {
        float d = this.getLengthX();
        float e = this.getLengthY();
        float f = this.getLengthZ();
        return (d + e + f) / 3.0F;
    }

    public float getLengthX() {
        return this.maxX - this.minX;
    }

    public float getLengthY() {
        return this.maxY - this.minY;
    }

    public float getLengthZ() {
        return this.maxZ - this.minZ;
    }

    public BoundingBox contract(float x, float y, float z) {
        return this.expand(-x, -y, -z);
    }

    public BoundingBox contract(float value) {
        return this.expand(-value);
    }

    public Optional<Vec3f> raycast(Vec3f from, Vec3f to) {
        return raycast(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, from, to);
    }

    public String toString() {
        return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public boolean isNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }

    public Vec3f getMinPos() {
        return new Vec3f(this.minX, this.minY, this.minZ);
    }

    public Vec3f getMaxPos() {
        return new Vec3f(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
