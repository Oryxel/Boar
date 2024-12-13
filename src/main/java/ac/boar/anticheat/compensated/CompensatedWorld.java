package ac.boar.anticheat.compensated;

import ac.boar.anticheat.compensated.cache.BoarChunk;
import ac.boar.anticheat.user.api.BoarPlayer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geysermc.geyser.level.block.type.Block;
import org.geysermc.geyser.util.MathUtils;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;

// https://github.com/GeyserMC/Geyser/blob/master/core/src/main/java/org/geysermc/geyser/session/cache/ChunkCache.java
@RequiredArgsConstructor
public class CompensatedWorld {
    private final BoarPlayer player;
    private final Long2ObjectMap<BoarChunk> chunks = new Long2ObjectOpenHashMap<>();

    @Setter private int minY;
    @Setter private int heightY;

    public void addToCache(int x, int z, DataPalette[] chunks, long id) {
        long chunkPosition = MathUtils.chunkPositionToLong(x, z);
        BoarChunk geyserChunk = new BoarChunk(chunks, id);
        this.chunks.put(chunkPosition, geyserChunk);
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        final BoarChunk chunk = getChunk(chunkX, chunkZ);
        return chunk == null || chunk.id() > player.lastReceivedId;
    }

    private BoarChunk getChunk(int chunkX, int chunkZ) {
        long chunkPosition = MathUtils.chunkPositionToLong(chunkX, chunkZ);
        return chunks.getOrDefault(chunkPosition, null);
    }

    public void updateBlock(int x, int y, int z, int block) {
        BoarChunk chunk = this.getChunk(x >> 4, z >> 4);
        if (chunk == null) {
            return;
        }

        if (y < minY || ((y - minY) >> 4) > chunk.sections().length - 1) {
            // Y likely goes above or below the height limit of this world
            return;
        }

        DataPalette palette = chunk.sections()[(y - minY) >> 4];
        if (palette == null) {
            if (block != Block.JAVA_AIR_ID) {
                // A previously empty chunk, which is no longer empty as a block has been added to it
                palette = DataPalette.createForChunk();
                // Fixes the chunk assuming that all blocks is the `block` variable we are updating. /shrug
                palette.getPalette().stateToId(Block.JAVA_AIR_ID);
                chunk.sections()[(y - minY) >> 4] = palette;
            } else {
                // Nothing to update
                return;
            }
        }

        palette.set(x & 0xF, y & 0xF, z & 0xF, block);
    }

    public int getBlockAt(int x, int y, int z) {
        BoarChunk column = this.getChunk(x >> 4, z >> 4);
        if (column == null) {
            return Block.JAVA_AIR_ID;
        }

        if (y < minY || ((y - minY) >> 4) > column.sections().length - 1) {
            // Y likely goes above or below the height limit of this world
            return Block.JAVA_AIR_ID;
        }

        DataPalette chunk = column.sections()[(y - minY) >> 4];
        if (chunk != null) {
            return chunk.get(x & 0xF, y & 0xF, z & 0xF);
        }

        return Block.JAVA_AIR_ID;
    }

    public void removeChunk(int chunkX, int chunkZ) {
        long chunkPosition = MathUtils.chunkPositionToLong(chunkX, chunkZ);
        chunks.remove(chunkPosition);
    }

    /**
     * Manually clears all entries in the chunk cache.
     * The server is responsible for clearing chunk entries if out of render distance (for example) or switching dimensions,
     * but it is the client that must clear sections in the event of proxy switches.
     */
    public void clear() {
        chunks.clear();
    }

    public int getChunkMinY() {
        return minY >> 4;
    }

    public int getChunkHeightY() {
        return heightY >> 4;
    }
}
