package ac.boar.anticheat.player;

import ac.boar.anticheat.compensated.CompensatedWorld;
import ac.boar.anticheat.data.StatusEffect;
import ac.boar.anticheat.util.BlockUtil;
import ac.boar.anticheat.util.TeleportUtil;
import ac.boar.anticheat.util.math.Box;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import ac.boar.anticheat.player.data.PlayerData;
import ac.boar.anticheat.util.ChatUtil;
import ac.boar.util.GeyserUtil;
import org.cloudburstmc.math.GenericMath;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.level.block.Blocks;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.geyser.registry.type.GeyserBedrockBlock;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.cache.TagCache;
import org.geysermc.geyser.session.cache.tags.BlockTag;
import org.geysermc.mcprotocollib.network.tcp.TcpSession;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class BoarPlayer extends PlayerData {
    @Getter
    private final GeyserSession session;
    public BedrockServerSession cloudburstSession;
    public TcpSession mcplSession;

    public final long joinedTime = System.currentTimeMillis();
    public long runtimeEntityId, javaEntityId;

    public final TeleportUtil teleportUtil = new TeleportUtil(this);

    // Lag compensation
    public final CompensatedWorld compensatedWorld = new CompensatedWorld(this);

    // Mappings
    public final Map<BlockDefinition, Integer> bedrockToJavaBlocks = new HashMap<>();

    public void loadBlockMappings() {
        final GeyserBedrockBlock[] javaToBedrockBlocks = this.session.getBlockMappings().getJavaToBedrockBlocks();
        for (int i = 0; i < javaToBedrockBlocks.length; i++) {
            this.bedrockToJavaBlocks.put(javaToBedrockBlocks[i], i);
        }
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
            this.cloudburstSession.sendPacketImmediately(latencyPacket);
        } else {
            this.cloudburstSession.sendPacket(latencyPacket);
        }

        this.latencyUtil.getSentTransactions().add(lastSentId);
    }

    public void disconnect(String reason) {
        this.session.disconnect(ChatUtil.PREFIX + " " + reason);
    }

    // Mappings related
    public int bedrockToJavaBlockId(final BlockDefinition definition) {
        return this.bedrockToJavaBlocks.getOrDefault(definition, -1);
    }

    // Prediction related method
    public void tick() {
        this.attributes.forEach((_, attribute) -> attribute.tick());
        this.movementSpeed = this.attributes.get(AttributeType.Builtin.MOVEMENT_SPEED.getId()).getValue();
        this.movementSpeed *= (this.sprinting || this.hasSprintingAttribute) ? 1.3f : 1;

        final List<Effect> shouldBeRemoved = new ArrayList<>();
        for (Map.Entry<Effect, StatusEffect> entry : this.statusEffects.entrySet()) {
            entry.getValue().tick();
            if (entry.getValue().getDuration() <= 0) {
                shouldBeRemoved.add(entry.getKey());
            }
        }

        shouldBeRemoved.forEach(this.statusEffects::remove);
    }

    public float getVelocityMultiplier() {
        final BlockState lv = this.compensatedWorld.getBlockState(Vector3i.from(this.x, this.y, this.z));
        final float f = BlockUtil.getVelocityMultiplier(lv);
        if (!lv.is(Blocks.WATER) && !lv.is(Blocks.BUBBLE_COLUMN)) {
            return f == 1.0 ? BlockUtil.getVelocityMultiplier(this.compensatedWorld.getBlockState(this.getVelocityAffectingPos())) : f;
        } else {
            return f;
        }
    }

    public float getJumpVelocity() {
        return PlayerData.JUMP_HEIGHT * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(Effect.JUMP_BOOST) ? 0.1F * (this.statusEffects.get(Effect.JUMP_BOOST).getAmplifier() + 1.0F) : 0.0F;
    }

    public float getJumpVelocityMultiplier() {
        float f = BlockUtil.getJumpVelocityMultiplier(this.compensatedWorld.getBlockState(this.prevX, this.prevY, this.prevZ));
        float g = BlockUtil.getJumpVelocityMultiplier(this.compensatedWorld.getBlockState(this.getVelocityAffectingPos()));
        return (double)f == 1.0 ? g : f;
    }

    public Vector3i getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.500001F);
    }

    public Vector3i getPosWithYOffset(final float offset) {
        if (this.supportingBlockPos != null) {
            if (!(offset > 1.0E-5F)) {
                return this.supportingBlockPos;
            } else {
                final TagCache cache = this.session.getTagCache();
                final BlockState lv2 = this.compensatedWorld.getBlockState(this.supportingBlockPos);
                return offset > 0.5 || !cache.is(BlockTag.FENCES, lv2.block())
                        && !cache.is(BlockTag.WALLS, lv2.block()) && !cache.is(BlockTag.FENCE_GATES, lv2.block())
                        ? Vector3i.from(this.supportingBlockPos.getX(), GenericMath.floor(this.prevY - offset), this.supportingBlockPos.getZ())
                        : this.supportingBlockPos;
            }
        } else {
            int i = GenericMath.floor(this.prevX);
            int j = GenericMath.floor(this.prevY - offset);
            int k = GenericMath.floor(this.prevZ);
            return Vector3i.from(i, j, k);
        }
    }

    public boolean isRegionUnloaded() {
        final Box lv = this.boundingBox.expand(1);
        int i = GenericMath.floor(lv.minX);
        int j = GenericMath.ceil(lv.maxX);
        int k = GenericMath.floor(lv.minZ);
        int l = GenericMath.ceil(lv.maxZ);
        return !this.compensatedWorld.isRegionLoaded(i, k, j, l);
    }

    // Other
    public MinecraftCodecHelper getCodecHelper() {
        return (MinecraftCodecHelper) this.mcplSession.getCodecHelper();
    }
}
