package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.prediction.engine.PredictionEngineElytra;
import ac.boar.anticheat.prediction.engine.PredictionEngineNormal;
import ac.boar.anticheat.prediction.engine.base.PredictionEngine;
import ac.boar.anticheat.user.api.BoarPlayer;
import org.cloudburstmc.protocol.bedrock.data.Ability;

public class LivingEntityTicker extends EntityTicker {
    public LivingEntityTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();
        tickMovement();
        super.tickBlockCollision();
    }

    public void tickMovement() {
        PredictionEngine engine;
        if (player.gliding) {
            engine = new PredictionEngineElytra(player);
        } else {
            engine = new PredictionEngineNormal(player);
        }
//        if ((this.isTouchingWater() || this.isInLava()) && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState)) {
//            this.travelInFluid(movementInput);
//        } else if (this.isGliding()) {
//            this.travelGliding();
//        } else {
//            this.travelMidAir(movementInput);
//        }

        if (player.abilities.getAbilities().contains(Ability.MAY_FLY)) {
            player.clientVelocity = engine.applyEndOfTick(player.actualVelocity);
            return;
        }

        player.movementInput = player.movementInput.mul(0.98F);

        if (player.movementInput.x != 0 && player.movementInput.z != 0) {
            player.movementInput = player.movementInput.mul(0.70710677F);
        }

        engine.move();
    }
}
