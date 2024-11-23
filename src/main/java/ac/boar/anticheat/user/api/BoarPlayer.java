package ac.boar.anticheat.user.api;

import ac.boar.anticheat.utils.LatencyUtil;
import ac.boar.anticheat.utils.TeleportUtil;
import ac.boar.utils.GeyserUtil;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.network.Session;

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
    public boolean sprinting, lastSprinting;

    public long lastReceivedId = -1, lastSentId = 0, lastRespondTime = System.currentTimeMillis();

    public boolean lastTickWasTeleport;

    // End of tick velocity.
    public Vec3d clientVelocity = Vec3d.ZERO, actualVelocity = Vec3d.ZERO;

    public void init() {
        GeyserUtil.hookGeyserPlayer(this);
    }

    public void sendTransaction() {
        final NetworkStackLatencyPacket latencyPacket = new NetworkStackLatencyPacket();
        latencyPacket.setTimestamp(++lastSentId);
        latencyPacket.setFromServer(true);

        this.bedrockSession.sendPacketImmediately(latencyPacket);
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
}
