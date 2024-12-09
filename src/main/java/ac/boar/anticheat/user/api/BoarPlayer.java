package ac.boar.anticheat.user.api;

import ac.boar.anticheat.check.api.holder.CheckHolder;
import ac.boar.anticheat.compensated.CompensatedEntity;
import ac.boar.anticheat.data.PlayerAbilities;
import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.anticheat.utils.LatencyUtil;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.utils.GeyserUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.EntityDefinitions;
import org.geysermc.geyser.level.WorldManager;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BoarPlayer {
    private final GeyserConnection connection;
    public final long joinedTime = System.currentTimeMillis();

    @Getter @Setter private BedrockSession bedrockSession;

    public final TeleportUtil teleportUtil = new TeleportUtil(this);
    public final LatencyUtil latencyUtil = new LatencyUtil(this);

    public final CompensatedEntity compensatedEntity = new CompensatedEntity(this);

    public final CheckHolder checkHolder = new CheckHolder(this);

    public long runtimeEntityId;

    public float lastX, x, lastY, y, lastZ, z;
    public long tick;

    public boolean onGround, lastGround;
    public float fallDistance;

    public float yaw, pitch;

    public int sinceSprinting, sinceSneaking;
    public boolean sprinting, lastSprinting, sneaking, lastSneaking, swimming, lastSwimming;
    public boolean gliding, lastGliding;

    public long lastReceivedId = 0, lastSentId = 0, lastRespondTime = System.currentTimeMillis();

    public boolean lastTickWasTeleport, lastTeleportWasSimulation;

    public boolean wasInPowderSnow, inPowderSnow;
    public boolean touchingWater, submergedInWater;

    public boolean collideX, collideZ, collideY;

    public boolean canClimb, lastCanClimb;
    public float lastClimbingSpeed, climbingSpeed;

    public Optional<Vector3i> supportingBlockPos = Optional.empty();

    public double extraUncertainOffset = 0;

    // End of tick velocity.
    public Vec3f clientVelocity = Vec3f.ZERO, actualVelocity = Vec3f.ZERO, predictedVelocity = Vec3f.ZERO;
    public Vec3f movementInput = Vec3f.ZERO;

    public Vector3f claimedEOT;

    public Vector3f bedrockRotation = Vector3f.ZERO;

    public Vector closetVector = new Vector(Vec3f.ZERO, VectorType.NORMAL);
    public Map<Long, Vec3f> queuedVelocities = new ConcurrentHashMap<>();
    public Map<Long, Vec3f> postPredictionVelocities = new ConcurrentHashMap<>();

    public Map<Effect, StatusEffect> statusMap = new ConcurrentHashMap<>();
    public Map<Fluid, Double> fluidHeight = new HashMap<>(), submergedFluidTag = new HashMap<>();
    public BoundingBox boundingBox;

    public Set<PlayerAuthInputData> inputData = new HashSet<>();

    public PlayerAbilities abilities = new PlayerAbilities();

    public void init() {
        GeyserUtil.hookBedrockSession(this);
        this.checkHolder.init();
    }

    public void sendTransaction() {
        sendTransaction(false);
    }

    public void sendTransaction(boolean immediate) {
        lastSentId++;
        if (lastSentId == GeyserUtil.MAGIC_FORM_IMAGE_HACK_TIMESTAMP) {
            lastSentId++;
        }

        // We have to send negative values since geyser translate positive one.
        final NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
        latencyPacket.setTimestamp(-lastSentId);
        latencyPacket.setFromServer(true);

        if (immediate) {
            this.bedrockSession.sendPacketImmediately(latencyPacket);
        } else {
            this.bedrockSession.sendPacket(latencyPacket);
        }

        this.latencyUtil.getSentTransactions().add(lastSentId);
    }

    public long getMagnitude() {
        return 1000000L;
    }

    public void updateBoundingBox() {
        boundingBox = BoundingBox.getBoxAt(x, y, z, EntityDefinitions.PLAYER.width(), EntityDefinitions.PLAYER.height());
    }

    public void tick() {
        List<Effect> ranOutStatus = new ArrayList<>();
        for (Map.Entry<Effect, StatusEffect> entry : this.statusMap.entrySet()) {
            entry.getValue().tick();
            if (entry.getValue().getDuration() <= 0) {
                ranOutStatus.add(entry.getKey());
            }
        }

        ranOutStatus.forEach(e -> this.statusMap.remove(e));
    }

    public boolean hasStatusEffect(final Effect effect) {
        return this.statusMap.containsKey(effect);
    }

    public StatusEffect getStatusEffect(final Effect effect) {
        return this.statusMap.get(effect);
    }

    public GeyserSession getSession() {
        return (GeyserSession) connection;
    }

    public float getHeight() {
        if (sneaking) {
            return 1.5F;
        } else {
            return EntityDefinitions.PLAYER.height();
        }
    }

    public float getMovementSpeed(boolean sprinting, float slipperiness) {
        if (onGround) {
            return (abilities.getWalkSpeed() * (sprinting ? 1.3F : 1)) * (0.21600002F / (slipperiness * slipperiness * slipperiness));
        }

        return sprinting ? 0.025999999F : 0.02F;
    }

    public boolean isClimbing() {
        BlockState state = getBlockStateAtPos();
        BlockState lastState = getBlockStateAtPosLast();

        boolean ladder = state.is(Blocks.LADDER) || lastState.is(Blocks.LADDER);
        boolean scaffolding = state.is(Blocks.SCAFFOLDING) || lastState.is(Blocks.SCAFFOLDING);

        climbingSpeed = ladder ? 0.20000076F : 0.15000153F;
        return ladder || scaffolding;
    }

    public BlockState getBlockStateAtPosLast() {
        WorldManager manager = getSession().getGeyser().getWorldManager();
        return manager.blockAt(getSession(), Vector3i.from(lastX, lastY, lastZ));
    }

    public BlockState getBlockStateAtPos() {
        WorldManager manager = getSession().getGeyser().getWorldManager();
        return manager.blockAt(getSession(), Vector3i.from(x, y, z));
    }

    public Vector3i getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.500001F);
    }

    public Vector3i getSteppingPos() {
        return this.getPosWithYOffset(1.0E-5F);
    }

    public Vector3i getLandingPos() {
        return this.getPosWithYOffset(0.2F);
    }

    public Vector3i getPosWithYOffset(float offset) {
        if (this.supportingBlockPos.isPresent()) {
            Vector3i blockPos = this.supportingBlockPos.get();
            return blockPos;
//            if (!(offset > 1.0E-5F)) {
//                return blockPos;
//            } else {
//                BlockState blockState = this.getWorld().getBlockState(blockPos);
//                return (!((double)offset <= 0.5) || !blockState.isIn(BlockTags.FENCES)) && !blockState.isIn(BlockTags.WALLS) && !(blockState.getBlock() instanceof FenceGateBlock) ? blockPos.withY(MathHelper.floor(this.pos.y - (double)offset)) : blockPos;
//            }
        } else {
            int i = (int) Math.floor(this.lastX);
            int j = (int) Math.floor(this.lastY - (double)offset);
            int k = (int) Math.floor(this.lastZ);
            return Vector3i.from(i, j, k);
        }
    }

    public float getJumpVelocity() {
        return this.getJumpVelocity(1.0F);
    }

    protected float getJumpVelocity(float strength) {
        return 0.42F * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(Effect.JUMP_BOOST) ? 0.1F * (this.getStatusEffect(Effect.JUMP_BOOST).getAmplifier() + 1.0F) : 0.0F;
    }

    protected float getJumpVelocityMultiplier() {
        WorldManager manager = getSession().getGeyser().getWorldManager();
        float f = BlockUtil.getJumpVelocityMultiplier(manager.blockAt(getSession(), Vector3i.from(lastX, lastY, lastZ)));
        float g = BlockUtil.getJumpVelocityMultiplier(manager.blockAt(getSession(), this.getVelocityAffectingPos()));

        return (double)f == 1.0 ? g : f;
    }

    public float getEffectiveGravity() {
        return (float) (clientVelocity.y <= 0.0 && this.hasStatusEffect(Effect.SLOW_FALLING) ? Math.min(this.getFinalGravity(), 0.01) : this.getFinalGravity());
    }

    public final float getFinalGravity() {
        return 0.08F;
    }

    public void onLanding() {
        this.fallDistance = 0.0F;
    }

    public float getStepHeight() {
        return 0.6F;
    }

}