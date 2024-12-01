package ac.boar.anticheat.user.api;

import ac.boar.anticheat.check.api.holder.CheckHolder;
import ac.boar.anticheat.compensated.CompensatedEntity;
import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.utils.LatencyUtil;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.utils.GeyserUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BoarPlayer {
    private final GeyserConnection connection;
    public final long joinedTime = System.currentTimeMillis();

    @Getter @Setter private BedrockSession bedrockSession;
    @Getter @Setter private Session javaSession;

    public final TeleportUtil teleportUtil = new TeleportUtil(this);
    public final LatencyUtil latencyUtil = new LatencyUtil(this);

    public final CompensatedEntity compensatedEntity = new CompensatedEntity(this);

    public final CheckHolder checkHolder = new CheckHolder(this);

    public float lastX, x, lastY, y, lastZ, z;
    public long tick;

    public boolean onGround, lastGround;
    public float fallDistance;

    public float yaw, pitch;

    public int sinceSprinting, sinceSneaking;
    public boolean sprinting, lastSprinting, sneaking, lastSneaking, swimming, lastSwimming;

    public long lastReceivedId = -1, lastSentId = 0, lastRespondTime = System.currentTimeMillis();

    public boolean lastTickWasTeleport;

    public boolean wasInPowderSnow, inPowderSnow;
    public boolean touchingWater, submergedInWater;

    public boolean collideX, collideZ;

    public Optional<Vector3i> supportingBlockPos = Optional.empty();

    public double extraUncertainOffset = 0;

    // End of tick velocity.
    public Vec3d clientVelocity = Vec3d.ZERO, actualVelocity = Vec3d.ZERO, predictedVelocity = Vec3d.ZERO;
    public Vec3d movementInput = Vec3d.ZERO;

    public Vector closetVector = new Vector(Vec3d.ZERO, VectorType.NORMAL);

    public Map<Long, Vec3d> queuedVelocities = new ConcurrentHashMap<>();

    public Map<Effect, StatusEffect> statusMap = new ConcurrentHashMap<>();

    public Map<Fluid, Double> fluidHeight = new HashMap<>(), submergedFluidTag = new HashMap<>();

    public BoundingBox boundingBox;

    public void init() {
        GeyserUtil.hookBedrockSession(this);
        this.checkHolder.init();
    }

    public void sendTransaction() {
        sendTransaction(false);
    }

    public void sendTransaction(boolean immediate) {
        final NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
        latencyPacket.setTimestamp(++lastSentId);
        latencyPacket.setFromServer(true);

        if (immediate) {
            this.bedrockSession.sendPacketImmediately(latencyPacket);
        } else {
            this.bedrockSession.sendPacket(latencyPacket);
        }

        this.latencyUtil.getSentTransactions().add(lastSentId);
    }

    public void tick() {
        this.tick++;

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

    public float getMovementSpeed(float slipperiness) {
        if (onGround) {
            return /* this.getMovementSpeed() */ (0.1F * ((sprinting || lastSprinting) ? 1.3F : 1)) * (0.21600002F / (slipperiness * slipperiness * slipperiness));
        }

        return sprinting ? 0.025999999F : 0.02F;
    }

    public Vector3i getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.500001F);
    }

    public Vector3i getSteppingPos() {
        return this.getPosWithYOffset(1.0E-5F);
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
            int i = (int) Math.floor(this.x);
            int j = (int) Math.floor(this.y - (double)offset);
            int k = (int) Math.floor(this.z);
            return Vector3i.from(i, j, k);
        }
    }

    public float getJumpVelocity() {
        return this.getJumpVelocity(1.0F);
    }

    protected float getJumpVelocity(float strength) {
        return /*this.getAttributeValue(EntityAttributes.JUMP_STRENGTH)*/ 0.42F * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(Effect.JUMP_BOOST) ? 0.1F * (this.getStatusEffect(Effect.JUMP_BOOST).getAmplifier() + 1.0F) : 0.0F;
    }

    protected float getJumpVelocityMultiplier() {
//        float f = this.getWorld().getBlockState(this.getBlockPos()).getBlock().getJumpVelocityMultiplier();
//        float g = this.getWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
//        return (double)f == 1.0 ? g : f;

        return 1.0F;
    }

    public double getEffectiveGravity() {
        return clientVelocity.y <= 0.0 && this.hasStatusEffect(Effect.SLOW_FALLING) ? Math.min(this.getFinalGravity(), 0.01) : this.getFinalGravity();
    }

    public final double getFinalGravity() {
        return 0.08D;
    }

    public void onLanding() {
        this.fallDistance = 0.0F;
    }

}
