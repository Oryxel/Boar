package ac.boar.anticheat.packets.level;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;

public class JavaLevelChunkPacket implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof ClientboundLevelChunkWithLightPacket packet) {
            int chunkSize = player.compensatedWorld.getChunkHeightY();
            final DataPalette[] palette = new DataPalette[chunkSize];

            ByteBuf in = Unpooled.wrappedBuffer(packet.getChunkData());
            for (int sectionY = 0; sectionY < chunkSize; sectionY++) {
                ChunkSection javaSection = player.getCodecHelper().readChunkSection(in);
                palette[sectionY] = javaSection.getChunkData();
            }

            // Send a transaction if player is inside (or near) that chunk.
            int chunkX = packet.getX() << 4, chunkZ = packet.getZ() << 4;
            boolean sendTrans = Math.abs(player.x - chunkX) <= 16 || Math.abs(player.z - chunkZ) <= 16;
            if (sendTrans) {
                event.getPostTasks().add(player::sendTransaction);
            }

            player.compensatedWorld.addToCache(packet.getX(), packet.getZ(), palette, player.lastSentId + (sendTrans ? 1 : 0));
        }

        if (event.getPacket() instanceof ClientboundForgetLevelChunkPacket packet) {
            // Send a transaction if player is inside (or near) that chunk.
            int chunkX = packet.getX() << 4, chunkZ = packet.getZ() << 4;
            boolean sendTrans = Math.abs(player.x - chunkX) <= 16 || Math.abs(player.z - chunkZ) <= 16;
            if (sendTrans) {
                event.getPostTasks().add(player::sendTransaction);
            }

            player.latencyUtil.addTransactionToQueue(player.lastSentId + (sendTrans ? 1 : 0),
                    () -> player.compensatedWorld.removeChunk(packet.getX(), packet.getZ()));
        }
    }
}
