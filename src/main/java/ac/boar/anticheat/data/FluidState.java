package ac.boar.anticheat.data;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.utils.math.Vec3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.physics.Direction;

public record FluidState(Fluid fluid, float height) {
    public float getHeight(BoarPlayer player, Vector3i pos) {
        return isFluidAboveEqual(player, pos) ? 1.0F : this.height();
    }

    private boolean isFluidAboveEqual(BoarPlayer player, Vector3i pos) {
        return fluid == player.compensatedWorld.getFluidState(pos.up()).fluid();
    }

    private boolean isEmptyOrThis(FluidState state) {
        return state.fluid == Fluid.EMPTY || state.fluid.equals(this.fluid);
    }

    public Vec3f getVelocity(BoarPlayer player, Vector3i vector3i, FluidState state) {
        float d = 0;
        float e = 0;

        for (Direction lv2 : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            Vector3i lv = vector3i.add(lv2.getUnitVector());
            FluidState lv3 = player.compensatedWorld.getFluidState(lv);
            if (this.isEmptyOrThis(lv3)) {
                float f = lv3.height();
                float g = 0.0F;
                if (f == 0.0F) {
                    if (!BlockUtil.blocksMovement(player, lv, fluid, player.compensatedWorld.getBlockState(lv))) {
                        FluidState lv5 = player.compensatedWorld.getFluidState(lv.down());
                        if (this.isEmptyOrThis(lv5)) {
                            f = lv5.height();
                            if (f > 0.0F) {
                                g = state.height() - (f - 0.8888889F);
                            }
                        }
                    }
                } else if (f > 0.0F) {
                    g = state.height() - f;
                }

                if (g != 0.0F) {
                    d += (lv2.getUnitVector().getX() * g);
                    e += (lv2.getUnitVector().getZ() * g);
                }
            }
        }

        Vec3f lv6 = new Vec3f(d, 0, e);
//        if (state.get(FALLING)) {
//            for (Direction lv7 : Type.HORIZONTAL) {
//                lv.set(pos, lv7);
//                if (this.isFlowBlocked(world, lv, lv7) || this.isFlowBlocked(world, lv.up(), lv7)) {
//                    lv6 = lv6.normalize().add(0.0, -6.0, 0.0);
//                    break;
//                }
//            }
//        }

        return lv6.length() > 0 ? lv6.normalize() : lv6;
    }
}
