package ac.boar.data;

import ac.boar.utils.StringUtil;
import ac.boar.utils.math.BoundingBox;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BedrockMappingData {

    public static Logger LOGGER = Logger.getLogger("[Boar - Mapping Phase]");

    private static Gson GSON = new Gson();

    public static Map<Integer, List<BoundingBox>> collisionMap = new HashMap<>();
    public static Map<String, List<BlockMappedData>> blockCollisionMappings = new HashMap<>();

    public static void load() {
        {
            final JsonObject blockCollisions = readJson("1.21.0-block-collisions.json");

            final JsonObject shapes = blockCollisions.getAsJsonObject("shapes");
            for (String element : shapes.keySet()) {
                final int id = Integer.parseInt(element);
                final JsonArray array = shapes.getAsJsonArray(element);

                if (array.isEmpty()) {
                    collisionMap.put(id, List.of(new BoundingBox(0, 0, 0, 0, 0, 0)));
                    continue;
                }

                final List<BoundingBox> collisions = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    final JsonArray collision = array.get(i).getAsJsonArray();

                    double middleX = collision.get(0).getAsDouble(),
                            middleY = collision.get(1).getAsDouble(),
                            middleZ = collision.get(2).getAsDouble();
                    double sizeX = collision.get(3).getAsDouble(),
                            sizeY = collision.get(4).getAsDouble(),
                            sizeZ = collision.get(5).getAsDouble();

                    collisions.add(new BoundingBox(
                            middleX - sizeX / 2, middleY - sizeY / 2, middleZ - sizeZ / 2,
                            middleX + sizeX / 2, middleY + sizeY / 2, middleZ + sizeZ / 2
                    ));
                }

                collisionMap.put(id, collisions);
            }

            int count = 0;
            final JsonObject blocks = blockCollisions.getAsJsonObject("blocks");
            for (String element : blocks.keySet()) {
                final String name = StringUtil.addNamespaceIfNeeded(element);
                final JsonArray array = blocks.getAsJsonArray(element);

                if (array.isEmpty()) {
                    LOGGER.info("Failed to find " + name + "~!");
                    continue; // Shouldn't happen but just in case.
                }

                final List<BlockMappedData> list = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    final int collisionId = array.get(i).getAsInt();
                    list.add(new BlockMappedData(i, collisionMap.get(collisionId)));
                }

                blockCollisionMappings.put(name, list);
                count++;
            }

            if (count < blocks.keySet().size()) {
                LOGGER.info("Block collision list: " + blocks.keySet().size() + ", actual mapped size: " + count);
            }
        }
    }

    private static JsonObject readJson(final String file) {
        try (final InputStream inputStream = BedrockMappingData.class.getClassLoader().getResourceAsStream("data/" + file)) {
            if (inputStream == null) {
                return null;
            }

            return GSON.fromJson(new InputStreamReader(inputStream), JsonObject.class);
        } catch (IOException e) {
            return null;
        }
    }

    private static Object loadGzipNBT(String file) {
        try (InputStream inputStream = BedrockMappingData.class.getClassLoader().getResourceAsStream("data/" + file);
             NBTInputStream nbtInputStream = NbtUtils.createGZIPReader(inputStream)) {
            return nbtInputStream.readTag();
        } catch (IOException e) {
            return null;
        }
    }

    public record BlockMappedData(int stateId, List<BoundingBox> box) {
    }

}
