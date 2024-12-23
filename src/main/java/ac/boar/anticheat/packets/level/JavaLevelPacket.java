package ac.boar.anticheat.packets.level;

import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.geyser.GeyserPacketListener;
import ac.boar.protocol.event.bedrock.geyser.GeyserSendEvent;
import ac.boar.protocol.event.java.PacketListener;
import ac.boar.protocol.event.java.PacketSendEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.geysermc.geyser.level.block.property.Properties;
import org.geysermc.geyser.level.block.type.BlockState;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;

public class JavaLevelPacket implements PacketListener, GeyserPacketListener {
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

        if (event.getPacket() instanceof ClientboundRespawnPacket) {
            event.getPostTasks().add(player.compensatedWorld::loadDimension);
        }
    }

    @Override
    public void onPacketSend(GeyserSendEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof UpdateBlockPacket packet) {
            final Vector3i blockPosition = packet.getBlockPosition();
            // Waterlogged block.
            if (packet.getDataLayer() == 1) {
                final boolean waterlogged = packet.getDefinition() == player.getSession().getBlockMappings().getBedrockWater();

                event.getPostTasks().add(() -> player.sendTransaction(event.isImmediate()));
                player.latencyUtil.addTransactionToQueue(player.lastSentId + 1, () -> {
                    final BlockState state = player.compensatedWorld.getBlockState(blockPosition);
                    if (state.getValue(Properties.WATERLOGGED) != null) {
                        player.compensatedWorld.updateBlock(blockPosition, state.withValue(Properties.WATERLOGGED, waterlogged).javaId());
                    }
                });
                return;
            }

            int javaId = player.getJavaBlock(packet.getDefinition());
            if (javaId == -1) {
                player.getSession().getGeyser().getWorldManager().getBlockAt(
                        player.getSession(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            }

            player.sendTransaction(event.isImmediate());
            player.latencyUtil.addTransactionToQueue(player.lastSentId, () -> player.compensatedWorld.updateBlock(blockPosition, javaId));
        }
    }
}
