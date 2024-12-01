package ac.boar.utils.math;

import ac.boar.utils.MathUtil;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.Optional;

public class BoundingBox implements Cloneable {
    private static final double EPSILON = 1.0E-7;
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public Vec3d toVec3d(double width) {
        return new Vec3d(this.minX + (width / 2D), this.minY, this.maxZ - (width / 2D));
    }

    public static BoundingBox getBoxAt(float x, float y, float z, float width, float height) {
        float f = width / 2.0f;
        return new BoundingBox(MathUtil.fixFTD(x - f), MathUtil.fixFTD(y), MathUtil.fixFTD(z - f),
                MathUtil.fixFTD(x + f), MathUtil.fixFTD(y + height), MathUtil.fixFTD(z + f));
    }

    public static BoundingBox getBoxAt(double x, double y, double z, double width, double height) {
        float f = (float) (width / 2.0f);
        float g = (float) height;
        return new BoundingBox(x - f, y, z - f, x + f, y + g, z + f);
    }

    public double calculateXOffset(BoundingBox other, double offsetX) {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetX > 0.0D && other.maxX <= this.minX) {
                double d1 = this.minX - other.maxX;

                if (d1 < offsetX) {
                    offsetX = d1;
                }
            } else if (offsetX < 0.0D && other.minX >= this.maxX) {
                double d0 = this.maxX - other.minX;

                if (d0 > offsetX) {
                    offsetX = d0;
                }
            }
        }
        return offsetX;
    }

    public double calculateYOffset(BoundingBox other, double offsetY) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0D && other.maxY <= this.minY) {
                double d1 = this.minY - other.maxY;

                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.minY >= this.maxY) {
                double d0 = this.maxY - other.minY;

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }
        }
        return offsetY;
    }

    public double calculateZOffset(BoundingBox other, double offsetZ) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
                double d1 = this.minZ - other.maxZ;

                if (d1 < offsetZ) {
                    offsetZ = d1;
                }
            } else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
                double d0 = this.maxZ - other.minZ;

                if (d0 > offsetZ) {
                    offsetZ = d0;
                }
            }

        }
        return offsetZ;
    }

    public BoundingBox withMinX(double minX) {
        return new BoundingBox(minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMinY(double minY) {
        return new BoundingBox(this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMinZ(double minZ) {
        return new BoundingBox(this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMaxX(double maxX) {
        return new BoundingBox(this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    public BoundingBox withMaxY(double maxY) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    public BoundingBox withMaxZ(double maxZ) {
        return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BoundingBox)) {
            return false;
        } else {
            BoundingBox BoundingBox = (BoundingBox)o;
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

    public int hashCode() {
        long l = Double.doubleToLongBits(this.minX);
        int i = (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.minY);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.minZ);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxX);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxY);
        i = 31 * i + (int)(l ^ l >>> 32);
        l = Double.doubleToLongBits(this.maxZ);
        i = 31 * i + (int)(l ^ l >>> 32);
        return i;
    }

    public BoundingBox shrink(double x, double y, double z) {
        double d = this.minX;
        double e = this.minY;
        double f = this.minZ;
        double g = this.maxX;
        double h = this.maxY;
        double i = this.maxZ;
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

    public BoundingBox stretch(Vec3d scale) {
        return this.stretch(scale.x, scale.y, scale.z);
    }

    public BoundingBox stretch(double x, double y, double z) {
        double d = this.minX;
        double e = this.minY;
        double f = this.minZ;
        double g = this.maxX;
        double h = this.maxY;
        double i = this.maxZ;
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

    public BoundingBox expand(double x, double y, double z) {
        double d = this.minX - x;
        double e = this.minY - y;
        double f = this.minZ - z;
        double g = this.maxX + x;
        double h = this.maxY + y;
        double i = this.maxZ + z;
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox expand(double value) {
        return this.expand(value, value, value);
    }

    public BoundingBox intersection(BoundingBox BoundingBox) {
        double d = Math.max(this.minX, BoundingBox.minX);
        double e = Math.max(this.minY, BoundingBox.minY);
        double f = Math.max(this.minZ, BoundingBox.minZ);
        double g = Math.min(this.maxX, BoundingBox.maxX);
        double h = Math.min(this.maxY, BoundingBox.maxY);
        double i = Math.min(this.maxZ, BoundingBox.maxZ);
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox union(BoundingBox BoundingBox) {
        double d = Math.min(this.minX, BoundingBox.minX);
        double e = Math.min(this.minY, BoundingBox.minY);
        double f = Math.min(this.minZ, BoundingBox.minZ);
        double g = Math.max(this.maxX, BoundingBox.maxX);
        double h = Math.max(this.maxY, BoundingBox.maxY);
        double i = Math.max(this.maxZ, BoundingBox.maxZ);
        return new BoundingBox(d, e, f, g, h, i);
    }

    public BoundingBox offset(double x, double y, double z) {
        return new BoundingBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public BoundingBox offset(Vec3d vec) {
        return this.offset(vec.x, vec.y, vec.z);
    }

    public boolean intersects(BoundingBox BoundingBox) {
        return this.intersects(BoundingBox.minX, BoundingBox.minY, BoundingBox.minZ, BoundingBox.maxX, BoundingBox.maxY, BoundingBox.maxZ);
    }

    public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public boolean intersects(Vec3d pos1, Vec3d pos2) {
        return this.intersects(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z), Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
    }

    public boolean contains(Vec3d pos) {
        return this.contains(pos.x, pos.y, pos.z);
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY && z >= this.minZ && z < this.maxZ;
    }

    public double getAverageSideLength() {
        double d = this.getLengthX();
        double e = this.getLengthY();
        double f = this.getLengthZ();
        return (d + e + f) / 3.0;
    }

    public double getLengthX() {
        return this.maxX - this.minX;
    }

    public double getLengthY() {
        return this.maxY - this.minY;
    }

    public double getLengthZ() {
        return this.maxZ - this.minZ;
    }

    public BoundingBox contract(double x, double y, double z) {
        return this.expand(-x, -y, -z);
    }

    public BoundingBox contract(double value) {
        return this.expand(-value);
    }

    public Optional<Vec3d> raycast(Vec3d from, Vec3d to) {
        return raycast(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, from, to);
    }

    public static Optional<Vec3d> raycast(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vec3d from, Vec3d to) {
        double[] ds = new double[]{1.0};
        double d = to.x - from.x;
        double e = to.y - from.y;
        double f = to.z - from.z;
        Direction direction = traceCollisionSide(minX, minY, minZ, maxX, maxY, maxZ, from, ds, null, d, e, f);
        if (direction == null) {
            return Optional.empty();
        } else {
            double g = ds[0];
            return Optional.of(from.add(g * d, g * e, g * f));
        }
    }

    private static Direction traceCollisionSide(BoundingBox BoundingBox, Vec3d intersectingVector, double[] traceDistanceResult, Direction approachDirection, double deltaX, double deltaY, double deltaZ) {
        return traceCollisionSide(BoundingBox.minX, BoundingBox.minY, BoundingBox.minZ, BoundingBox.maxX, BoundingBox.maxY, BoundingBox.maxZ, intersectingVector, traceDistanceResult, approachDirection, deltaX, deltaY, deltaZ);
    }

    private static Direction traceCollisionSide(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Vec3d intersectingVector, double[] traceDistanceResult, Direction approachDirection, double deltaX, double deltaY, double deltaZ) {
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

    private static Direction traceCollisionSide(double[] traceDistanceResult, Direction approachDirection, double deltaX, double deltaY, double deltaZ, double begin, double minX, double maxX, double minZ, double maxZ, Direction resultDirection, double startX, double startY, double startZ) {
        double d = (begin - startX) / deltaX;
        double e = startY + d * deltaY;
        double f = startZ + d * deltaZ;
        if (0.0 < d && d < traceDistanceResult[0] && minX - 1.0E-7 < e && e < maxX + 1.0E-7 && minZ - 1.0E-7 < f && f < maxZ + 1.0E-7) {
            traceDistanceResult[0] = d;
            return resultDirection;
        } else {
            return approachDirection;
        }
    }

    public String toString() {
        return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public boolean isNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }

    public Vec3d getMinPos() {
        return new Vec3d(this.minX, this.minY, this.minZ);
    }

    public Vec3d getMaxPos() {
        return new Vec3d(this.maxX, this.maxY, this.maxZ);
    }

    public static BoundingBox of(Vec3d center, double dx, double dy, double dz) {
        return new BoundingBox(center.x - dx / 2.0, center.y - dy / 2.0, center.z - dz / 2.0, center.x + dx / 2.0, center.y + dy / 2.0, center.z + dz / 2.0);
    }

    public DoubleList getPointPositions() {
        return gather(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static DoubleList gather(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (maxX - minX < 1.0E-7 || maxY - minY < 1.0E-7 || maxZ - minZ < 1.0E-7) {
            return DoubleList.of();
        }
        int i = findRequiredBitResolution(minX, maxX);
        int j = findRequiredBitResolution(minY, maxY);
        int k = findRequiredBitResolution(minZ, maxZ);
        if (i < 0 || j < 0 || k < 0) {
            return DoubleArrayList.wrap(new double[]{minY, maxY});
        }
        if (i == 0 && j == 0 && k == 0) {
            return DoubleArrayList.wrap(new double[]{0, 1});
        }

        int m = 1 << j;
        return new AbstractDoubleList() {
            @Override
            public double getDouble(int i) {
                return i / m;
            }

            @Override
            public int size() {
                return m + 1;
            }
        };
    }

    protected static int findRequiredBitResolution(double min, double max) {
        if (min < -1.0E-7 || max > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            int j = 1 << i;
            double d = min * (double)j;
            double e = max * (double)j;
            boolean bl = Math.abs(d - (double)Math.round(d)) < 1.0E-7 * (double)j;
            boolean bl2 = Math.abs(e - (double)Math.round(e)) < 1.0E-7 * (double)j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    @Override
    public BoundingBox clone() {
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
