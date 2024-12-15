package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.data.FluidState;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;

@RequiredArgsConstructor
public class EntityTicker {
    protected final BoarPlayer player;

    public void tick() {
        baseTick();
    }

    public void baseTick() {
        player.wasInPowderSnow = player.inPowderSnow;
        player.inPowderSnow = false;

        player.wasTouchingWater = player.touchingWater;
        updateWaterState();
        updateSubmergedInWaterState();
        updateSwimming();
    }

    private void updateSwimming() {
        player.swimming = player.swimming & player.sinceSprinting < 6 && player.touchingWater; /* && !this.hasVehicle() */
    }

    private void updateWaterState() {
        player.fluidHeight.clear();
        checkWaterState();
        double d = player.getSession().getDimensionType().ultrawarm() ? 0.007 : 0.0023333333333333335;
        this.updateMovementInFluid(Fluid.LAVA, d);
    }

    private void updateSubmergedInWaterState() {
        player.submergedInWater = player.isSubmergedIn(Fluid.WATER);
        player.submergedFluidTag.clear();
        double d = player.getEyeY();
//        if (this.getVehicle() instanceof AbstractBoatEntity lv2 && !lv2.isSubmergedInWater() && lv2.getBoundingBox().maxY >= d && lv2.getBoundingBox().minY <= d) {
//            return;
//        }

        Vector3i lv3 = Vector3i.from(player.x, d, player.z);
        FluidState lv4 = player.compensatedWorld.getFluidState(lv3);
        float e = lv3.getY() + lv4.getHeight(player, lv3);
        if (e > d) {
            player.submergedFluidTag.add(lv4.fluid());
        }
    }


    protected void checkWaterState() {
//        if (this.getVehicle() instanceof AbstractBoatEntity lv && !lv.isSubmergedInWater()) {
//            this.touchingWater = false;
//            return;
//        }

        if (this.updateMovementInFluid(Fluid.WATER, 0.014)) {
            player.fallDistance = 0;
            player.touchingWater = true;
        } else {
            player.touchingWater = false;
        }
    }

    public boolean updateMovementInFluid(Fluid tag, double speed) {
        if (player.isRegionUnloaded()) {
            return false;
        } else {
            BoundingBox lv = player.boundingBox.contract(0.001F);
            int i = MathUtil.floor(lv.minX);
            int j = MathUtil.ceil(lv.maxX);
            int k = MathUtil.floor(lv.minY);
            int l = MathUtil.ceil(lv.maxY);
            int m = MathUtil.floor(lv.minZ);
            int n = MathUtil.ceil(lv.maxZ);
            double e = 0.0;
            boolean bl = /* this.isPushedByFluids(); */ true;
            boolean bl2 = false;
            Vec3f lv2 = Vec3f.ZERO;
            int o = 0;

            for (int p = i; p < j; p++) {
                for (int q = k; q < l; q++) {
                    for (int r = m; r < n; r++) {
                        Vector3i pos = Vector3i.from(p, q, r);
                        FluidState lv4 = player.compensatedWorld.getFluidState(p, q, r);

                        if (lv4.fluid() == tag) {
                            double f = q + lv4.getHeight(player, pos);
                            if (f >= lv.minY) {
                                bl2 = true;
                                e = Math.max(f - lv.minY, e);
                                if (bl) {
                                    Vec3f lv5 = lv4.getVelocity(player, pos, lv4);
                                    if (e < 0.4) {
                                        lv5 = lv5.mul(e);
                                    }

                                    lv2 = lv2.add(lv5);
                                    o++;
                                }
                            }
                        }
                    }
                }
            }

//            if (lv2.length() > 0.0) {
//                if (o > 0) {
//                    lv2 = lv2.mul(1.0 / (double)o);
//                }
//
//                Vec3f lv6 = this.getVelocity();
//                lv2 = lv2.multiply(speed);
//                double g = 0.003;
//                if (Math.abs(lv6.x) < 0.003 && Math.abs(lv6.z) < 0.003 && lv2.length() < 0.0045000000000000005) {
//                    lv2 = lv2.normalize().multiply(0.0045000000000000005);
//                }
//
//                this.setVelocity(this.getVelocity().add(lv2));
//            }

            player.fluidHeight.put(tag, e);
            return bl2;
        }
    }

    protected final void tickBlockCollision() {
        if (player.onGround) {
            Vector3i lv = player.getLandingPos();
            BlockState lv2 = player.compensatedWorld.getBlockState(lv);

            BlockUtil.onSteppedOn(player, lv, lv2);
        }
    }
}
