package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.prediction.engine.base.PredictionEngine;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.math.Vec3f;
import org.bukkit.Bukkit;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.geysermc.geyser.level.block.Fluid;

public class PredictionEngineWater extends PredictionEngine {
    public PredictionEngineWater(BoarPlayer player) {
        super(player);
    }

    @Override
    public Vec3f travel(Vec3f vec3f, Vec3f movementInput) {
        Vec3f lv2 = player.waterFluidSpeed.clone();
        if (lv2.length() > 0.0) {
            Bukkit.broadcastMessage("water fluid!");
            lv2 = lv2.mul(0.014F);
            if (Math.abs(vec3f.x) < 0.003 && Math.abs(vec3f.z) < 0.003 && lv2.length() < 0.0045000000000000005) {
                Bukkit.broadcastMessage("0.003");
                lv2 = lv2.normalize().mul(0.0045000000000000005F);
            }

            vec3f = vec3f.add(lv2);
        }

        if (player.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
            vec3f = vec3f.add(0, -0.04F, 0);
        }

        float f = player.sprinting ? 0.9F : 0.8F;
        float g = 0.02F;
        float h = 0 /* (float)this.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY) */;
        if (!player.onGround) {
            h *= 0.5F;
        }

        if (h > 0.0F) {
            f += (0.54600006F - f) * h;
            g += (player.movementSpeed - g) * h;
        }

        // There is no dolphins grace in bedrock.
//        if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
//            f = 0.96F;
//        }

        return this.updateVelocity(vec3f, movementInput, g);
    }

    @Override
    public Vec3f applyEndOfTick(Vec3f vec3f) {
        float f = player.sprinting ? 0.9F : 0.8F;
        float e = player.getEffectiveGravity(vec3f);

        Vec3f lv = vec3f.clone();
//        if ((player.collideZ || player.collideX) && this.isClimbing()) {
//            lv = new Vec3d(lv.x, 0.2, lv.z);
//        }

        lv = lv.mul(f, 0.8F, f);
        lv = this.applyFluidMovingSpeed(e, lv);

        if (player.horizontalCollision && Collisions.doesNotCollide(player, lv.x, lv.y + player.getStepHeight() - player.y + player.prevY, lv.z)) {
            lv = new Vec3f(lv.x, 0.3F, lv.z);
        }

        return lv;
    }

    protected Vec3f jump(Vec3f vec3f) {
        return vec3f.add(0, 0.04F, 0);
    }

    @Override
    protected boolean canJump() {
        double g = player.fluidHeight.getOrDefault(Fluid.WATER, 0D);

        double h = player.getSwimHeight();
        boolean allowed = player.onGround && g > h || player.wasTouchingWater && g > 0;
        return player.inputData.contains(PlayerAuthInputData.WANT_UP) && allowed;
    }

    private Vec3f applyFluidMovingSpeed(float gravity, Vec3f motion) {
        if (gravity != 0.0 && !player.swimming) {
            return new Vec3f(motion.x, motion.y - gravity / 16.0f, motion.z);
        } else {
            return motion;
        }
    }
}
