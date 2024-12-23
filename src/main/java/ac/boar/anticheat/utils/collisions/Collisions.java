package ac.boar.anticheat.utils.collisions;

import ac.boar.anticheat.compensated.cache.BoarEntity;
import ac.boar.anticheat.data.FluidState;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.MutableBlockPos;
import ac.boar.utils.math.Vec3f;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.level.physics.Axis;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;

import java.util.ArrayList;
import java.util.List;

public final class Collisions {

    private static boolean isSpaceAroundPlayerEmpty(final BoarPlayer player, float offsetX, float offsetZ, float f) {
        BoundingBox lv = player.boundingBox;
        return isSpaceEmpty(player, new BoundingBox(lv.minX + offsetX, lv.minY - f - 1.0E-5F, lv.minZ + offsetZ, lv.maxX + offsetX, lv.minY, lv.maxZ + offsetZ));
    }

    public static boolean isSpaceEmpty(final BoarPlayer player, final BoundingBox box) {
        return legacyBoxCollisions(player, box, true).isEmpty();
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
            for (i = Math.signum(e) * 0.05F; d != 0.0 && isSpaceAroundPlayerEmpty(player, d, 0.0F, f); d -= h) {
                if (Math.abs(d) <= 0.05) {
                    d = 0.0F;
                    break;
                }
            }

            while (e != 0.0 && isSpaceAroundPlayerEmpty(player, 0.0F, e, f)) {
                if (Math.abs(e) <= 0.05) {
                    e = 0.0F;
                    break;
                }

                e -= i;
            }

            while (d != 0.0 && e != 0.0 && isSpaceAroundPlayerEmpty(player, d, e, f)) {
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

            if (d != 0.0) {
                d = calculateMaxOffset(Axis.X, entityBoundingBox, collisions, d);
                if (d != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(d, 0.0F, 0.0F);
                }
            }

            if (f != 0.0) {
                f = calculateMaxOffset(Axis.Z, entityBoundingBox, collisions, f);
            }

            return new Vec3f(d, e, f);
        }
    }

    private static float calculateMaxOffset(Axis axis, BoundingBox boundingBox, List<BoundingBox> collision, float maxDist) {
        BoundingBox box = boundingBox.clone();

        for (BoundingBox bb : collision) {
            if (Math.abs(maxDist) < 1.0E-7) {
                return 0;
            }

            double oldDist = maxDist;
            maxDist = bb.calculateMaxDistance(axis, box, maxDist);

            // Normally minecraft (java) uses 1.0E-7 when it comes to calculating collision (calculateMaxDistance)
            // We however, uses 3.0E-5 since we have to account for floating point errors. This prevents collision being ignored when there is floating point errors.
            // But, sometimes this causes the anti-cheat to wrongly calculate your movement around 1.0E-5 -> 3.0E-5 offset (floating point error)
            // So we simply check for this and correct it back to 0. NOTE: This is only for cases that your movement is supposed to be 0.
            if (oldDist > 0 && maxDist >= -BoundingBox.MAX_TOLERANCE_ERROR && maxDist < -BoundingBox.EPSILON || oldDist < 0 && maxDist <= BoundingBox.MAX_TOLERANCE_ERROR && maxDist > BoundingBox.EPSILON) {
                // Bukkit.broadcastMessage("floating point error!");
                maxDist = 0;
            }
        }

        return maxDist;
    }

    private static List<BoundingBox> findCollisionsForMovement(BoarPlayer player, List<BoundingBox> regularCollisions, BoundingBox boundingBox, boolean compensated) {
        regularCollisions.addAll(legacyBoxCollisions(player, boundingBox, compensated));
        return regularCollisions;
    }

    private static List<BoundingBox> legacyBoxCollisions(BoarPlayer player, BoundingBox bb, boolean compensated) {
        final List<BoundingBox> list = Lists.newArrayList();
        int i = (int) Math.floor(bb.minX);
        int j = (int) Math.floor(bb.maxX + 1.0D);
        int k = (int) Math.floor(bb.minY);
        int l = (int) Math.floor(bb.maxY + 1.0D);
        int i1 = (int) Math.floor(bb.minZ);
        int j1 = (int) Math.floor(bb.maxZ + 1.0D);

        final MutableBlockPos mutable = new MutableBlockPos(0, 0, 0);
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = i1; l1 < j1; ++l1) {
                for (int i2 = k - 1; i2 < l; ++i2) {
                    mutable.set(k1, i2, l1);
                    addCollisionBoxesToList(player, mutable, bb, list, compensated);
                }
            }
        }

        return list;
    }

    private static void addCollisionBoxesToList(BoarPlayer player, MutableBlockPos blockPos, BoundingBox boundingBox, List<BoundingBox> list, boolean compensated) {
        GeyserSession session = player.getSession();
        BlockState state;
        if (compensated) {
            state = player.compensatedWorld.getBlockState(blockPos.x, blockPos.y, blockPos.z);
        } else {
            state = session.getGeyser().getWorldManager().blockAt(session, blockPos.x, blockPos.y, blockPos.z);
        }

        List<BoundingBox> boxes = BedrockCollision.getBoundingBox(player, blockPos, state);
        if (boxes != null) {
            for (BoundingBox box : boxes) {
                box = box.offset(blockPos.x, blockPos.y, blockPos.z);
                if (box.intersects(boundingBox)) {
                    list.add(box);
                }
            }
            return;
        }

        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        if (collision == null) {
            return;
        }

        for (org.geysermc.geyser.level.physics.BoundingBox geyserBB : collision.getBoundingBoxes()) {
            BoundingBox box = new BoundingBox(geyserBB).offset(blockPos.x, blockPos.y, blockPos.z);

            if (box.intersects(boundingBox)) {
                list.add(box);
            }
        }
    }

    public static Vec3f adjustMovementForCollisions(BoarPlayer player, Vec3f movement, BoundingBox box, List<BoundingBox> collisions, boolean compensated) {
        List<BoundingBox> list = findCollisionsForMovement(player, collisions, box.stretch(movement), compensated);
        return adjustMovementForCollisions(movement, box, list);
    }

    public static Vec3f adjustMovementForCollisions(BoarPlayer player, BoundingBox box, Vec3f movement, boolean compensated) {
        List<BoundingBox> list = getEntityCollisions(player, box.stretch(movement));
        Vec3f vec3f = movement.lengthSquared() == 0.0 ? movement : adjustMovementForCollisions(player, movement, box, list, compensated);
        boolean bl = movement.x != vec3f.x;
        boolean bl2 = movement.y != vec3f.y;
        boolean bl3 = movement.z != vec3f.z;
        boolean bl4 = bl2 && movement.y < 0.0;
        if (player.getStepHeight() > 0.0F && (bl4 || player.onGround) && (bl || bl3)) {
            BoundingBox box2 = bl4 ? box.offset(0, vec3f.y, 0) : box;
            BoundingBox box3 = box2.stretch(movement.x, player.getStepHeight(), movement.z);
            if (!bl4) {
                box3 = box3.stretch(0, -9.999999747378752E-6F, 0);
            }

            List<BoundingBox> list2 = findCollisionsForMovement(player, list, box3, compensated);
            float f = vec3f.y;
            float[] fs = collectStepHeights(box2, list2, player.getStepHeight(), f);

            for (float g : fs) {
                Vec3f vec3f2 = adjustMovementForCollisions(new Vec3f(movement.x, g, movement.z), box2, list2);
                if (vec3f2.horizontalLengthSquared() > vec3f.horizontalLengthSquared()) {
                    float d = box.minY - box2.minY;
                    return vec3f2.add(0.0F, -d, 0.0F);
                }
            }
        }

        return vec3f;
    }

    private static float[] collectStepHeights(BoundingBox collisionBox, List<BoundingBox> collisions, float f, float stepHeight) {
        FloatArraySet floatSet = new FloatArraySet(4);
        block0:
        for (BoundingBox bb : collisions) {
            FloatList floatList = bb.getPointPositions();
            for (double d : floatList) {
                float g = (float) (d - collisionBox.minY);
                if (g < 0.0f || g == stepHeight) continue;
                if (g > f) continue block0;
                floatSet.add(g);
            }
        }
        float[] fs = floatSet.toFloatArray();
        FloatArrays.unstableSort(fs);
        return fs;
    }

    public static boolean doesNotCollide(BoarPlayer player, float offsetX, float offsetY, float offsetZ) {
        return doesNotCollide(player, player.boundingBox.offset(offsetX, offsetY, offsetZ));
    }

    private static boolean doesNotCollide(BoarPlayer player, BoundingBox box) {
        return legacyBoxCollisions(player, box, true).isEmpty() && !containsFluid(player, box);
    }

    private static boolean containsFluid(BoarPlayer player, BoundingBox box) {
        int i = MathUtil.floor(box.minX);
        int j = MathUtil.ceil(box.maxX);
        int k = MathUtil.floor(box.minY);
        int l = MathUtil.ceil(box.maxY);
        int m = MathUtil.floor(box.minZ);
        int n = MathUtil.ceil(box.maxZ);

        for (int o = i; o < j; o++) {
            for (int p = k; p < l; p++) {
                for (int q = m; q < n; q++) {
                    FluidState lv2 = player.compensatedWorld.getFluidState(o, p, q);
                    if (lv2.fluid() != Fluid.EMPTY) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static List<BoundingBox> getEntityCollisions(BoarPlayer player, BoundingBox box) {
        if (box.getAverageSideLength() > BoundingBox.EPSILON) {
            List<BoarEntity> list = new ArrayList<>();
            player.compensatedEntity.getMap().forEach((_, v) -> {
                EntityType type = v.getType();
                if (v.getBoundingBox().intersects(box) && (type.name().toLowerCase().contains("boat") || type == EntityType.SHULKER)) {
                    list.add(v);
                }
            });

            List<BoundingBox> boxes = new ArrayList<>();

            for (BoarEntity lv : list) {
                boxes.add(lv.getBoundingBox());
            }

            return boxes;
        }

        return List.of();
    }
}
