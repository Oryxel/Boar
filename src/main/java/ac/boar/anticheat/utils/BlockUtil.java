package ac.boar.anticheat.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BlockState;

public class BlockUtil {
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
