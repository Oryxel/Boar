package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.BedrockCollision;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.MutableBlockPos;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.BlockStateValues;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.translator.collision.BlockCollision;
import org.geysermc.geyser.util.BlockUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil {
    public static boolean blocksMovement(BoarPlayer player, MutableBlockPos vector3i, Fluid fluid, BlockState state) {
        if (state.is(Blocks.ICE)) {
            return false;
        }

        if (BlockStateValues.getFluid(state.javaId()) == fluid) {
            return false;
        }

        return !state.is(Blocks.COBWEB) && !state.is(Blocks.BAMBOO_SAPLING) && isSolid(player, state, vector3i);
    }

    public static boolean isSolid(BoarPlayer player, BlockState state, MutableBlockPos vector3i) {
        List<BoundingBox> boxes = getBlockBoundingBoxes(player, state, vector3i);
        if (boxes.isEmpty()) {
            return false;
        } else {
            BoundingBox box = new BoundingBox(0, 0, 0, 0, 0, 0);
            for (BoundingBox box1 : boxes) {
                box = box1.union(box);
            }

            return box.getAverageSideLength() >= 0.7291666666666666 || box.getLengthY() >= 1.0;
        }
    }

    public static List<BoundingBox> getBlockBoundingBoxes(BoarPlayer player, BlockState state, MutableBlockPos vector3i) {
        List<BoundingBox> boxes = BedrockCollision.getBoundingBox(player, vector3i, state);
        if (boxes != null) {
            return boxes;
        }

        List<BoundingBox> boxes1 = new ArrayList<>();
        BlockCollision collision = BlockUtils.getCollision(state.javaId());
        if (collision == null) {
            return List.of();
        }

        for (org.geysermc.geyser.level.physics.BoundingBox geyserBB : collision.getBoundingBoxes()) {
            BoundingBox box = new BoundingBox(geyserBB);
            boxes1.add(box);
        }

        return boxes1;
    }

    public static float getVelocityMultiplier(BlockState state) {
        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.HONEY_BLOCK)) {
            return 0.4F;
        }

        return 1F;
    }

    public static float getJumpVelocityMultiplier(BlockState state) {
        if (state.is(Blocks.HONEY_BLOCK)) {
            return 0.5F;
        }

        return 1F;
    }

    public static float getBlockSlipperiness(BlockState state) {
        if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.FROSTED_ICE)) {
            return 0.98F;
        } else if (state.is(Blocks.SLIME_BLOCK)) {
            return 0.8F;
        } else if (state.is(Blocks.BLUE_ICE)) {
            return 0.989F;
        }

        return 0.6F;
    }

    public static void onSteppedOn(BoarPlayer player, Vector3i pos, BlockState state) {
        if (state.is(Blocks.SLIME_BLOCK)) {
            float d = Math.abs(player.clientVelocity.y);
            if (d < 0.1 && !player.sneaking) {
                float e = 0.391F + d * 0.2F;
                player.clientVelocity = player.clientVelocity.mul(e, 1, e);
            }
        }
    }
}
