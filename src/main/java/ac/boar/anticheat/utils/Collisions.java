package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.data.BedrockMappingData;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import org.bukkit.Bukkit;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.registry.type.GeyserBedrockBlock;

import java.util.ArrayList;
import java.util.List;

public class Collisions {

    private static boolean isSpaceAroundPlayerEmpty(BoarPlayer player, double offsetX, double offsetZ, float f) {
        BoundingBox box = player.boundingBox;
        List<BoundingBox> collisions = legacyBoxCollisions(player, new BoundingBox(box.minX + offsetX,
                box.minY - f - 9.999999747378752E-6, box.minZ + offsetZ, box.maxX + offsetX, box.minY, box.maxZ + offsetZ));

        return collisions.isEmpty();
    }

    private static boolean method_30263(BoarPlayer player, float f) {
        return player.onGround || player.fallDistance < f && !isSpaceAroundPlayerEmpty(player, 0.0, 0.0, f - player.fallDistance);
    }

    public static Vec3d adjustMovementForSneaking(BoarPlayer player, Vec3d movement) {
        float f = /* this.getStepHeight() */ 0.6F;
        if (/* !this.abilities.flying && */ !(movement.y > 0.0) && player.sneaking && method_30263(player, f)) {
            double d = movement.x;
            double e = movement.z;
            double h = Math.signum(d) * 0.05;

            double i;
            for(i = Math.signum(e) * 0.05; d != 0.0 && isSpaceAroundPlayerEmpty(player, d, 0.0, f); d -= h) {
                if (Math.abs(d) <= 0.05) {
                    d = 0.0;
                    break;
                }
            }

            while(e != 0.0 && isSpaceAroundPlayerEmpty(player,0.0, e, f)) {
                if (Math.abs(e) <= 0.05) {
                    e = 0.0;
                    break;
                }

                e -= i;
            }

            while(d != 0.0 && e != 0.0 && isSpaceAroundPlayerEmpty(player,d, e, f)) {
                if (Math.abs(d) <= 0.05) {
                    d = 0.0;
                } else {
                    d -= h;
                }

                if (Math.abs(e) <= 0.05) {
                    e = 0.0;
                } else {
                    e -= i;
                }
            }

            return new Vec3d(d, movement.y, e);
        } else {
            return movement;
        }
    }

    public static Vec3d adjustMovementForCollisions(Vec3d movement, BoundingBox entityBoundingBox, List<BoundingBox> collisions) {
        if (collisions.isEmpty()) {
            return movement;
        } else {
            double d = movement.x;
            double e = movement.y;
            double f = movement.z;
            if (e != 0.0) {
                e = calculateMaxOffset(Axis.Y, entityBoundingBox, collisions, e);
                if (e != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, e, 0.0);
                }
            }

            boolean bl = Math.abs(d) < Math.abs(f);
            if (bl && f != 0.0) {
                f = calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
                if (f != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, f);
                }
            }

            if (d != 0.0) {
                d = calculateMaxOffset(Axis.X, entityBoundingBox, collisions, d);
                if (!bl && d != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(d, 0.0, 0.0);
                }
            }

            if (!bl && f != 0.0) {
                f = calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
            }

            return new Vec3d(d, e, f);
        }
    }

    private static double calculateMaxOffset(Axis axis, BoundingBox boundingBox, List<BoundingBox> collision, double d) {
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
        WorldManager worldManager = player.getSession().getGeyser().getWorldManager();
        BlockState state = worldManager.blockAt(player.getSession(), vector3i);

        GeyserBedrockBlock definition = player.getSession().getBlockMappings().getBedrockBlock(state);
        String name = definition.getState().getString("name");

        // Yes I know BlockUtils.getCollision exist, but I don't trust it enough, like will it account for block state?
        // I will check it later, if it does then I will use it.
        List<BedrockMappingData.BlockMappedData> mappedData = BedrockMappingData.blockCollisionMappings.get(name);
        if (mappedData == null || mappedData.isEmpty()) {
            return;
        }

        // TODO: block state.
        for (BoundingBox box : mappedData.get(0).box()) {
            // Empty.
            if (box.minX + box.minY + box.minZ + box.maxX + box.maxY + box.maxZ == 0) {
                continue;
            }

            BoundingBox bb = new BoundingBox(
                    vector3i.getX() + box.minX,
                    vector3i.getY() + box.minY,
                    vector3i.getZ() + box.minZ, vector3i.getX() + box.maxX, vector3i.getY() + box.maxY,
                    vector3i.getZ() + box.maxZ);

            if (bb.intersects(boundingBox)) {
                list.add(bb);
            }
        }
    }

    public static Vec3d adjustMovementForCollisions(BoarPlayer player, Vec3d movement, BoundingBox box, List<BoundingBox> collisions) {
        List<BoundingBox> list = findCollisionsForMovement(player, collisions, box.stretch(movement));
        return adjustMovementForCollisions(movement, box, list);
    }

    public static Vec3d adjustMovementForCollisions(BoarPlayer player, BoundingBox box, Vec3d movement) {
        List<BoundingBox> list = /* this.getWorld().getEntityCollisions(this, box.stretch(movement)) */ new ArrayList<>();
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : adjustMovementForCollisions(player, movement, box, list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = bl2 && movement.y < 0.0;
        if (/*this.getStepHeight() > 0.0F && */ (bl4 || player.onGround) && (bl || bl3)) {
            Bukkit.broadcastMessage("get!");
            BoundingBox box2 = bl4 ? box.offset(0.0, vec3d.y, 0.0) : box;
            BoundingBox box3 = box2.stretch(movement.x, /*(double) this.getStepHeight()*/ 0.6D, movement.z);
            if (!bl4) {
                box3 = box3.stretch(0.0, -9.999999747378752E-6, 0.0);
            }

            List<BoundingBox> list2 = findCollisionsForMovement(player, list, box3);
            float f = (float) vec3d.y;
            float[] fs = collectStepHeights(box2, list2, 0.6F, f);
            float[] var14 = fs;
            int var15 = fs.length;

            for (int var16 = 0; var16 < var15; ++var16) {
                float g = var14[var16];
                Vec3d vec3d2 = adjustMovementForCollisions(new Vec3d(movement.x, g, movement.z), box2, list2);
                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                    double d = box.minY - box2.minY;
                    return vec3d2.add(0.0, -d, 0.0);
                }
            }
        }

        return vec3d;
    }

    private static float[] collectStepHeights(BoundingBox collisionBox, List<BoundingBox> collisions, float f, float stepHeight) {
        FloatArraySet floatSet = new FloatArraySet(4);
        block0: for (BoundingBox bb : collisions) {
            DoubleList doubleList = bb.getPointPositions();
            DoubleListIterator doubleListIterator = doubleList.iterator();
            while (doubleListIterator.hasNext()) {
                double d = doubleListIterator.next();
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
