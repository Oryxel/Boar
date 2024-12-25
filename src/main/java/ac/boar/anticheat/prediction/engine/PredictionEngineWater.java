package ac.boar.anticheat.prediction.engine;

import ac.boar.anticheat.prediction.engine.base.PredictionEngine;

import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.anticheat.utils.collisions.Collisions;
import ac.boar.utils.math.Vec3f;
import org.bukkit.Bukkit;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.geysermc.geyser.level.block.Fluid;

import java.util.List;

public class PredictionEngineWater extends PredictionEngine {
    public PredictionEngineWater(BoarPlayer player) {
        super(player);
    }

    @Override
    protected void addTravelToPossibilities(List<Vector> vectors) {
        for (Vector vector : vectors) {
            vector.setVelocity(travel(player.sprinting, vector.getVelocity(), player.movementInput));
            vector.setSprinting(player.sprinting);
        }
    }

    @Override
    public Vec3f travel(boolean sprinting, Vec3f vec3f, Vec3f movementInput) {
        Vec3f lv2 = player.waterFluidSpeed.clone();
        if (lv2.length() > 0.0) {
            Bukkit.broadcastMessage("water fluid!");
            lv2 = lv2.mul(0.014F);
            vec3f = vec3f.add(lv2);
        }

        if (player.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
            vec3f = vec3f.add(0, -0.04F, 0);
        }

        return this.updateVelocity(vec3f, movementInput, 0.02F);
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

    protected Vec3f jump(boolean sprinting, Vec3f vec3f) {
        return vec3f.add(0, 0.04F, 0);
    }

    @Override
    protected boolean canJump() {
        double g = player.fluidHeight.getOrDefault(Fluid.WATER, 0D);

        double h = player.getSwimHeight();
        boolean allowed = player.onGround && player.touchingWater && g > 0 || g > h;
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
