package ac.boar.anticheat.prediction.ticker;

import ac.boar.anticheat.user.api.BoarPlayer;

public class PlayerTicker extends LivingEntityTicker {
    public PlayerTicker(BoarPlayer player) {
        super(player);
    }

    @Override
    public void tick() {
        super.tick();

        player.updateBoundingBox();
        player.postPredictionVelocities.clear();
    }

    @Override
    public void baseTick() {
        player.lastClimbingSpeed = player.climbingSpeed;
        player.lastCanClimb = player.canClimb;
        player.canClimb = (player.isClimbing() /* || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this) */);

        super.baseTick();
    }

    @Override
    public void tickMovement() {
        // This only applies if the player is not in fluid.
        if ((player.lastSneaking || player.sneaking) && !player.gliding && !player.isInLava() && !player.touchingWater) {
            player.movementInput = player.movementInput.mul(0.3);
        }

        super.tickMovement();
    }
}
