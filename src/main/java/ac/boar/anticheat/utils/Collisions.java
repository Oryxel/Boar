package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;

import java.util.ArrayList;
import java.util.List;

public class Collisions {

    private static boolean isSpaceAroundPlayerEmpty(BoarPlayer player, float offsetX, float offsetZ, float f) {
        BoundingBox box = player.boundingBox;
        List<BoundingBox> collisions = legacyBoxCollisions(player, new BoundingBox(box.minX + offsetX,
                box.minY - f - 9.999999747378752E-6F, box.minZ + offsetZ, box.maxX + offsetX, box.minY, box.maxZ + offsetZ));

        return collisions.isEmpty();
    }

    private static boolean method_30263(BoarPlayer player, float f) {
        return player.onGround || player.fallDistance < f && !isSpaceAroundPlayerEmpty(player, 0.0F, 0.0F, f - player.fallDistance);
    }

    public static Vec3f adjustMovementForSneaking(BoarPlayer player, Vec3f movement) {
        float f = player.getStepHeight();
        if (/* !this.abilities.flying && */ !(movement.y > 0.0) && player.sneaking && method_30263(player, f)) {
            float d = movement.x;
            float e = movement.z;
            float h = Math.signum(d) * 0.05F;

            float i;
            for(i = Math.signum(e) * 0.05F; d != 0.0 && isSpaceAroundPlayerEmpty(player, d, 0.0F, f); d -= h) {
                if (Math.abs(d) <= 0.05) {
                    d = 0.0F;
                    break;
                }
            }

            while(e != 0.0 && isSpaceAroundPlayerEmpty(player,0.0F, e, f)) {
                if (Math.abs(e) <= 0.05) {
                    e = 0.0F;
                    break;
                }

                e -= i;
            }

            while(d != 0.0 && e != 0.0 && isSpaceAroundPlayerEmpty(player,d, e, f)) {
                if (Math.abs(d) <= 0.05) {
                    d = 0.0F;
                } else {
                    d -= h;
                }

                if (Math.abs(e) <= 0.05) {
                    e = 0.0F;
                } else {
                    e -= i;
                }
            }

            return new Vec3f(d, movement.y, e);
        } else {
            return movement;
        }
    }

    public static Vec3f adjustMovementForCollisions(Vec3f movement, BoundingBox entityBoundingBox, List<BoundingBox> collisions) {
        if (collisions.isEmpty()) {
            return movement;
        } else {
            float d = movement.x;
            float e = movement.y;
            float f = movement.z;
            if (e != 0.0) {
                e = calculateMaxOffset(Axis.Y, entityBoundingBox, collisions, e);
                if (e != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0F, e, 0.0F);
                }
            }

            boolean bl = Math.abs(d) < Math.abs(f);
            if (bl && f != 0.0) {
                f = calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
                if (f != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0F, 0.0F, f);
                }
            }

            if (d != 0.0) {
                d = calculateMaxOffset(Axis.X, entityBoundingBox, collisions, d);
                if (!bl && d != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(d, 0.0F, 0.0F);
                }
            }

            if (!bl && f != 0.0) {
                f = calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
            }

            return new Vec3f(d, e, f);
        }
    }

    private static float calculateMaxOffset(Axis axis, BoundingBox boundingBox, List<BoundingBox> collision, float d) {
        BoundingBox box = boundingBox.clone();

        for (BoundingBox bb : collision) {
            if (axis == Axis.X) {
                d = bb.calculateXOffset(box, d);
            } else if (axis == Axis.Y) {
                d = bb.calculateYOffset(box, d);
            } else {
                d = bb.calculateZOffset(box, d);
            }
        }

        return d;
    }

    private static List<BoundingBox> findCollisionsForMovement(BoarPlayer player, List<BoundingBox> regularCollisions, BoundingBox boundingBox) {
        regularCollisions.addAll(legacyBoxCollisions(player, boundingBox));
        return regularCollisions;
    }

    private static List<BoundingBox> legacyBoxCollisions(BoarPlayer player, BoundingBox bb) {
        List<BoundingBox> list = Lists.newArrayList();
        int i = (int) Math.floor(bb.minX);
        int j = (int) Math.floor(bb.maxX + 1.0D);
        int k = (int) Math.floor(bb.minY);
        int l = (int) Math.floor(bb.maxY + 1.0D);
        int i1 = (int) Math.floor(bb.minZ);
        int j1 = (int) Math.floor(bb.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = i1; l1 < j1; ++l1) {
                for (int i2 = k - 1; i2 < l; ++i2) {
                    addCollisionBoxesToList(player, Vector3i.from(k1, i2, l1), bb, list);
                }
            }
        }

        return list;
    }

    // TODO: compensated world
    private static void addCollisionBoxesToList(BoarPlayer player, Vector3i vector3i, BoundingBox boundingBox, List<BoundingBox> list) {
        BlockCollision collision = BlockUtils.getCollisionAt(player.getSession(), vector3i);
        if (collision == null) {
            return;
        }

        for (org.geysermc.geyser.level.physics.BoundingBox geyserBB : collision.getBoundingBoxes()) {
            BoundingBox box = new BoundingBox(geyserBB).offset(new Vec3f(vector3i));

            if (box.intersects(boundingBox)) {
                list.add(box);
            }
        }
    }

    public static Vec3f adjustMovementForCollisions(BoarPlayer player, Vec3f movement, BoundingBox box, List<BoundingBox> collisions) {
        List<BoundingBox> list = findCollisionsForMovement(player, collisions, box.stretch(movement));
        return adjustMovementForCollisions(movement, box, list);
    }

    public static Vec3f adjustMovementForCollisions(BoarPlayer player, BoundingBox box, Vec3f movement) {
        List<BoundingBox> list = /* this.getWorld().getEntityCollisions(this, box.stretch(movement)) */ new ArrayList<>();
        Vec3f vec3F = movement.lengthSquared() == 0.0 ? movement : adjustMovementForCollisions(player, movement, box, list);
        boolean bl = movement.x != vec3F.x;
        boolean bl2 = movement.y != vec3F.y;
        boolean bl3 = movement.z != vec3F.z;
        boolean bl4 = bl2 && movement.y < 0.0;
        if (player.getStepHeight() > 0.0F && (bl4 || player.onGround) && (bl || bl3)) {
            BoundingBox box2 = bl4 ? box.offset(0, vec3F.y, 0) : box;
            BoundingBox box3 = box2.stretch(movement.x, player.getStepHeight(), movement.z);
            if (!bl4) {
                box3 = box3.stretch(0, -9.999999747378752E-6F, 0);
            }

            List<BoundingBox> list2 = findCollisionsForMovement(player, list, box3);
            float f = vec3F.y;
            float[] fs = collectStepHeights(box2, list2, 0.6F, f);
            float[] var14 = fs;
            int var15 = fs.length;

            for (int var16 = 0; var16 < var15; ++var16) {
                float g = var14[var16];
                Vec3f vec3f2 = adjustMovementForCollisions(new Vec3f(movement.x, g, movement.z), box2, list2);
                if (vec3f2.horizontalLengthSquared() > vec3F.horizontalLengthSquared()) {
                    float d = box.minY - box2.minY;
                    return vec3f2.add(0.0F, -d, 0.0F);
                }
            }
        }

        return vec3F;
    }

    private static float[] collectStepHeights(BoundingBox collisionBox, List<BoundingBox> collisions, float f, float stepHeight) {
        FloatArraySet floatSet = new FloatArraySet(4);
        block0: for (BoundingBox bb : collisions) {
            FloatList floatList = bb.getPointPositions();
            FloatListIterator floatListIterator = floatList.iterator();
            while (floatListIterator.hasNext()) {
                double d = floatListIterator.next();
                float g = (float)(d - collisionBox.minY);
                if (g < 0.0f || g == stepHeight) continue;
                if (g > f) continue block0;
                floatSet.add(g);
            }
        }
        float[] fs = floatSet.toFloatArray();
        FloatArrays.unstableSort(fs);
        return fs;
    }
}
