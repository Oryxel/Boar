package ac.boar.anticheat.user.api;

import ac.boar.anticheat.check.api.holder.CheckHolder;
import ac.boar.anticheat.compensated.CompensatedEntity;
import ac.boar.anticheat.compensated.CompensatedWorld;
import ac.boar.anticheat.data.*;
import ac.boar.anticheat.prediction.engine.data.Vector;
import ac.boar.anticheat.prediction.engine.data.VectorType;
import ac.boar.anticheat.utils.BlockUtil;
import ac.boar.anticheat.utils.LatencyUtil;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.utils.ChatUtil;
import ac.boar.utils.GeyserUtil;
import ac.boar.utils.MathUtil;
import ac.boar.utils.math.BoundingBox;
import ac.boar.utils.math.Vec3f;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.entity.attribute.GeyserAttributeType;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.Fluid;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.type.GeyserBedrockBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.TagCache;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.mcprotocollib.network.tcp.TcpSession;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class BoarPlayer {
    private final GeyserSession connection;
    public final long joinedTime = System.currentTimeMillis();

    @Getter @Setter private BedrockSession bedrockSession;
    @Getter @Setter private TcpSession tcpSession;

    public final TeleportUtil teleportUtil = new TeleportUtil(this);
    public final LatencyUtil latencyUtil = new LatencyUtil(this);

    public final CompensatedEntity compensatedEntity = new CompensatedEntity(this);
    public final CompensatedWorld compensatedWorld = new CompensatedWorld(this);

    public final CheckHolder checkHolder = new CheckHolder(this);

    public static final Map<EntityPose, EntityDimensions> POSE_DIMENSIONS = ImmutableMap.<EntityPose, EntityDimensions>builder()
            .put(EntityPose.STANDING, EntityDimensions.changing(0.6F, 1.8F).withEyeHeight(1.62F))
            .put(EntityPose.SLEEPING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(0.2F))
            .put(EntityPose.GLIDING, EntityDimensions.changing(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(EntityPose.SWIMMING, EntityDimensions.changing(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(EntityPose.SPIN_ATTACK, EntityDimensions.changing(0.6F, 0.6F).withEyeHeight(0.4F))
            .put(EntityPose.CROUCHING, EntityDimensions.changing(0.6F, 1.5F).withEyeHeight(1.27F))
            .put(EntityPose.DYING, EntityDimensions.fixed(0.2F, 0.2F).withEyeHeight(1.62F)).build();

    public long runtimeEntityId, javaEntityId;

    public float prevX, x, prevY, y, prevZ, z;
    public long tick;

    public boolean onGround, wasGround;
    public float fallDistance;

    public float yaw, pitch;

    public int sinceSprinting, sinceSneaking;
    public boolean sprinting, wasSprinting, sneaking, wasSneaking, swimming, wasSwimming;
    public boolean gliding, wasGliding;

    public boolean hasSprintingAttribute, uncertainSprinting;

    public long lastReceivedId = 0, lastSentId = 0, lastRespondTime = System.currentTimeMillis();

    public boolean lastTickWasTeleport;
    public int sinceTeleport;

    public boolean wasInPowderSnow, inPowderSnow;
    public boolean wasTouchingWater, touchingWater, submergedInWater;

    public boolean verticalCollision, horizontalCollision;

    public boolean climbing, fastClimbing;
    public float climbingSpeed;

    public Vector3i supportingBlockPos = null;

    public Vec3f prevEOT = Vec3f.ZERO, eotVelocity = Vec3f.ZERO, actualVelocity = Vec3f.ZERO, predictedVelocity = Vec3f.ZERO;
    public Vec3f movementInput = Vec3f.ZERO;

    public Vec3f movementMultiplier = Vec3f.ZERO;

    public Vec3f waterFluidSpeed = Vec3f.ZERO, lavaFluidSpeed = Vec3f.ZERO;

    public Vector3f claimedEOT = Vector3f.ZERO, lastClaimedEOT = Vector3f.ZERO;

    public Vector3f bedrockRotation = Vector3f.ZERO;

    public Vector closetVector = new Vector(Vec3f.ZERO, VectorType.NORMAL);
    public final Map<Long, Vec3f> queuedVelocities = new ConcurrentHashMap<>();
    public final Map<Long, Vec3f> postPredictionVelocities = new ConcurrentHashMap<>();

    public final Map<Effect, StatusEffect> statusMap = new ConcurrentHashMap<>();
    public final Map<Fluid, Double> fluidHeight = new HashMap<>();
    public final List<Fluid> submergedFluidTag = new CopyOnWriteArrayList<>();
    public BoundingBox boundingBox;

    public final Set<PlayerAuthInputData> inputData = new HashSet<>();

    public final PlayerAbilities abilities = new PlayerAbilities();
    public final Map<GeyserAttributeType, AttributeData> attributes = new HashMap<>();

    public float movementSpeed = 0.1F;

    public EntityPose pose = EntityPose.STANDING;

    // Mappings
    public final Map<BlockDefinition, Integer> bedrockToJavaBlocks = new HashMap<>();

    public void init() {
        GeyserUtil.hookBedrockSession(this);
        this.attributes.put(GeyserAttributeType.MOVEMENT_SPEED, new AttributeData(0.1F));

        this.checkHolder.init();
    }

    public void loadBlockMappings() {
        GeyserBedrockBlock[] javaToBedrockBlocks = getSession().getBlockMappings().getJavaToBedrockBlocks();
        for (int i = 0; i < javaToBedrockBlocks.length; i++) {
            this.bedrockToJavaBlocks.put(javaToBedrockBlocks[i], i);
        }
    }

    public double getSwimHeight() {
        return (double)this.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    public float getStandingEyeHeight() {
        return 1.62F;
    }

    public int getJavaBlock(BlockDefinition definition) {
        return this.bedrockToJavaBlocks.getOrDefault(definition, -1);
    }

    public void sendTransaction() {
        sendTransaction(false);
    }

    public void sendTransaction(boolean immediate) {
        lastSentId++;
        if (lastSentId == GeyserUtil.MAGIC_FORM_IMAGE_HACK_TIMESTAMP) {
            lastSentId++;
        }

//        if (System.currentTimeMillis() - lastRespondTime > 5000L) {
//            disconnect("Timed out!");
//            return;
//        }

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

    public void disconnect(String reason) {
        getSession().disconnect(ChatUtil.PREFIX + " " + reason);
    }

    public MinecraftCodecHelper getCodecHelper() {
        return (MinecraftCodecHelper) getTcpSession().getCodecHelper();
    }

    public boolean isInLava() {
        if (!this.fluidHeight.containsKey(Fluid.LAVA)) {
            return false;
        }

        return tick != 1 && this.fluidHeight.get(Fluid.LAVA) > 0.0;
    }

    public long getMagnitude() {
        return 1000000L;
    }

    public float getEyeY() {
        return y + POSE_DIMENSIONS.get(pose).eyeHeight();
    }

    public boolean isSubmergedIn(Fluid fluidTag) {
        return this.submergedFluidTag.contains(fluidTag);
    }

    public boolean isRegionUnloaded() {
        BoundingBox lv = boundingBox.expand(1.0F);
        int i = MathUtil.floor(lv.minX);
        int j = MathUtil.ceil(lv.maxX);
        int k = MathUtil.floor(lv.minZ);
        int l = MathUtil.ceil(lv.maxZ);
        return !compensatedWorld.isRegionLoaded(i, k, j, l);
    }

    public void updateBoundingBox() {
        this.boundingBox = calculateBoundingBox(x, y, z);
    }

    public BoundingBox calculateBoundingBox(Vec3f vec3f) {
        return calculateBoundingBox(vec3f.x, vec3f.y, vec3f.z);
    }

    public BoundingBox calculateBoundingBox(float x, float y, float z) {
        return POSE_DIMENSIONS.get(pose).getBoxAt(x, y, z);
    }

    public void tick() {
        this.attributes.forEach((_, a) -> a.tick());
        this.movementSpeed = this.attributes.get(GeyserAttributeType.MOVEMENT_SPEED).getValue();
        this.movementSpeed *= (this.hasSprintingAttribute || this.sprinting) ? 1.3F : 1;

        List<Effect> ranOutStatus = new ArrayList<>();
        for (Map.Entry<Effect, StatusEffect> entry : this.statusMap.entrySet()) {
            entry.getValue().tick();
            if (entry.getValue().getDuration() <= 0) {
                ranOutStatus.add(entry.getKey());
            }
        }

        ranOutStatus.forEach(this.statusMap::remove);
    }

    public boolean hasStatusEffect(final Effect effect) {
        return this.statusMap.containsKey(effect);
    }

    public StatusEffect getStatusEffect(final Effect effect) {
        return this.statusMap.get(effect);
    }

    public GeyserSession getSession() {
        return this.connection;
    }

    public float getMovementSpeed(float slipperiness) {
        if (onGround) {
            return (this.movementSpeed * (0.21600002F / (slipperiness * slipperiness * slipperiness)));
        }

        return sprinting ? 0.025999999F : 0.02F;
    }

    public boolean wantToClimb() {
        return this.horizontalCollision || this.inputData.contains(PlayerAuthInputData.JUMPING) && this.climbing;
    }

    public boolean isClimbing() {
        final TagCache cache = getSession().getTagCache();
        BlockState state = getBlockStateAtPos();

        this.fastClimbing = state.is(Blocks.LADDER) || state.is(Blocks.VINE);
        this.climbingSpeed = state.is(Blocks.SCAFFOLDING) ? 0.15F : 0.2F;
        return cache.is(BlockTag.CLIMBABLE, state.block());
    }

    public BlockState getBlockStateAtPosLast() {
        return compensatedWorld.getBlockState(Vector3i.from(prevX, prevY, prevZ));
    }

    public BlockState getBlockStateAtPos() {
        return compensatedWorld.getBlockState(Vector3i.from(x, y, z));
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
        if (this.supportingBlockPos != null) {
            return this.supportingBlockPos;
//            if (!(offset > 1.0E-5F)) {
//                return blockPos;
//            } else {
//                BlockState blockState = this.getWorld().getBlockState(blockPos);
//                return (!((double)offset <= 0.5) || !blockState.isIn(BlockTags.FENCES)) && !blockState.isIn(BlockTags.WALLS) && !(blockState.getBlock() instanceof FenceGateBlock) ? blockPos.withY(MathHelper.floor(this.pos.y - (double)offset)) : blockPos;
//            }
        } else {
            int i = (int) Math.floor(this.prevX);
            int j = (int) Math.floor(this.prevY - (double)offset);
            int k = (int) Math.floor(this.prevZ);
            return Vector3i.from(i, j, k);
        }
    }

    public float getVelocityMultiplier() {
        final BlockState lv = this.compensatedWorld.getBlockState(MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z));
        float f = BlockUtil.getVelocityMultiplier(lv);
        if (!lv.is(Blocks.WATER) && !lv.is(Blocks.BUBBLE_COLUMN)) {
            return (double)f == 1.0 ? BlockUtil.getVelocityMultiplier(compensatedWorld.getBlockState(this.getVelocityAffectingPos())) : f;
        } else {
            return f;
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
        float f = BlockUtil.getJumpVelocityMultiplier(compensatedWorld.getBlockState(Vector3i.from(prevX, prevY, prevZ)));
        float g = BlockUtil.getJumpVelocityMultiplier(compensatedWorld.getBlockState(this.getVelocityAffectingPos()));

        return f == 1.0 ? g : f;
    }

    public float getEffectiveGravity(final Vec3f vec3f) {
        return vec3f.y < 0.0 && this.hasStatusEffect(Effect.SLOW_FALLING) ? Math.min(this.getFinalGravity(), 0.01F) : this.getFinalGravity();
    }

    public final float getFinalGravity() {
        return 0.08F;
    }

    public float getStepHeight() {
        return 0.6F;
    }

}