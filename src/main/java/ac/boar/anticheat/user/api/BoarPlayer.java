package ac.boar.anticheat.user.api;

import ac.boar.anticheat.utils.LatencyUtil;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.utils.GeyserUtil;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.network.Session;

import java.util.Optional;

@RequiredArgsConstructor
public class BoarPlayer {
    private final GeyserConnection connection;
    public final long joinedTime = System.currentTimeMillis();

    @Getter @Setter private BedrockSession bedrockSession;
    @Getter @Setter private Session javaSession;

    public final TeleportUtil teleportUtil = new TeleportUtil(this);
    public final LatencyUtil latencyUtil = new LatencyUtil(this);

    public float lastX, x, lastY, y, lastZ, z;
    public long tick;

    public boolean onGround;
    public float fallDistance;

    public float yaw, pitch;
    public boolean sprinting, lastSprinting, sneaking, lastSneaking;

    public long lastReceivedId = -1, lastSentId = 0, lastRespondTime = System.currentTimeMillis();

    public boolean lastTickWasTeleport;

    public Optional<Vector3i> supportingBlockPos;

    // End of tick velocity.
    public Vec3d clientVelocity = Vec3d.ZERO, claimedClientVelocity = Vec3d.ZERO, actualVelocity = Vec3d.ZERO;

    public void init() {
        GeyserUtil.hookGeyserPlayer(this);
    }

    public void sendTransaction() {
        final NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
        latencyPacket.setTimestamp(++lastSentId);
        latencyPacket.setFromServer(true);

        this.bedrockSession.sendPacketImmediately(latencyPacket);

        this.latencyUtil.getSentTransactions().add(lastSentId);
    }

    public GeyserSession getSession() {
        return (GeyserSession) connection;
    }

    public float getMovementSpeed(float slipperiness) {
        if (onGround) {
            return /* this.getMovementSpeed() */ 0.7F * (0.21600002F / (slipperiness * slipperiness * slipperiness));
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

    public double getEffectiveGravity() {
        boolean bl = clientVelocity.y <= 0.0;
        return 0.08D;
//        return bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING) ? Math.min(this.getFinalGravity(), 0.01) : this.getFinalGravity();
    }
}
