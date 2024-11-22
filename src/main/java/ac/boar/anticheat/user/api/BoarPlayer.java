package ac.boar.anticheat.user.api;

import ac.boar.utils.GeyserUtil;
import ac.boar.utils.math.Vec3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.BedrockSession;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.mcprotocollib.network.Session;

@RequiredArgsConstructor
@Getter
@Setter
public class BoarPlayer {
    private final GeyserConnection connection;
    private final long joinedTime = System.currentTimeMillis();

    private BedrockSession bedrockSession;
    private Session javaSession;

    public float lastX, x, lastY, y, lastZ, z;
    public long tick;

    public boolean onGround;
    public float fallDistance;

    public float yaw, pitch;
    public boolean sprinting, lastSprinting;

    // End of tick velocity.
    public Vec3d clientVelocity = Vec3d.ZERO, actualVelocity = Vec3d.ZERO;

    public void init() {
        GeyserUtil.hookGeyserPlayer(this);
    }

    public float getMovementSpeed(float slipperiness) {
        if (onGround) {
            return /* this.getMovementSpeed() */ 0.7F * (0.21600002F / (slipperiness * slipperiness * slipperiness));
        }

        return sprinting ? 0.025999999F : 0.02F;
    }
}
