package ac.boar.utils;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.listener.MITMBedrockListener;
import ac.boar.protocol.listener.UpstreamSessionListener;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.lang.reflect.Field;

public class GeyserUtil {
    public static long MAGIC_FORM_IMAGE_HACK_TIMESTAMP = 1234567890L;

    public static void hookBedrockSession(BoarPlayer player) {
        final GeyserConnection connection = player.getSession();

        try {
            BedrockServerSession bedrockSession = getBedrockSession(player, connection);
            hookUpstreamSession(player, bedrockSession, connection);
            player.setBedrockSession(bedrockSession);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BedrockServerSession getBedrockSession(BoarPlayer player, GeyserConnection connection) throws Exception {
        Class klass = Class.forName("org.geysermc.geyser.session.GeyserSession");
        Field upstream = klass.getDeclaredField("upstream");
        upstream.setAccessible(true);
        Object o = upstream.get(connection);
        Field field = o.getClass().getDeclaredField("session");
        field.setAccessible(true);
        BedrockServerSession session = (BedrockServerSession) field.get(o);

        BedrockPacketHandler handler = session.getPacketHandler();
        MITMBedrockListener newHandler = new MITMBedrockListener(player, handler);

        session.setPacketHandler(newHandler);
        return session;
    }

    private static void hookUpstreamSession(BoarPlayer player, BedrockServerSession session, GeyserConnection connection) throws Exception {
        Class klass = Class.forName("org.geysermc.geyser.session.GeyserSession");
        Field upstream = klass.getDeclaredField("upstream");
        upstream.setAccessible(true);
        upstream.set(connection, new UpstreamSessionListener(player, session));
    }
}
