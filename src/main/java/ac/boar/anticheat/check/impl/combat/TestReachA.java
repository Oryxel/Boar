package ac.boar.anticheat.check.impl.combat;

import ac.boar.anticheat.check.api.CheckInfo;
import ac.boar.anticheat.check.api.impl.PacketCheck;
import ac.boar.anticheat.compensated.cache.EntityCache;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import ac.boar.utils.math.Vec3d;
import org.bukkit.Bukkit;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.geysermc.geyser.entity.EntityDefinitions;

@CheckInfo(name = "TestReach", type = "A")
public class TestReachA extends PacketCheck {
    private static double MAX_REACH = 3.1;

    public TestReachA(BoarPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (event.getPacket() instanceof InventoryTransactionPacket) {
            final InventoryTransactionPacket packet = (InventoryTransactionPacket) event.getPacket();
            if (packet.getTransactionType() != InventoryTransactionType.ITEM_USE_ON_ENTITY || packet.getActionType() != 1) {
                return;
            }

            if (check(player, packet.getRuntimeEntityId())) {
                Bukkit.broadcastMessage("fail!");
                event.setCancelled(true);
            }
        }

        // This packet doesn't matter.
        if (event.getPacket() instanceof InteractPacket) {
            final InteractPacket packet = (InteractPacket) event.getPacket();
            if (packet.getAction() == InteractPacket.Action.DAMAGE) {
                event.setCancelled(true);
            }
        }
    }

    public boolean check(BoarPlayer player, long id) {
        EntityCache cache = player.compensatedEntity.getEntityCache(id);
        if (cache == null) {
            return true;
        }

        final Vec3d vec3d = new Vec3d(player.x, player.y, player.z);
        double distance = cache.getPosition().distanceTo(vec3d);
        if (cache.getBoundingBox().contains(player.x, player.y + EntityDefinitions.PLAYER.offset(), player.z)) {
            distance = 0;
        }

        // I gave up, this has to do with bedrock RakNet not syncing properly or whatever
        // The check is stable for java player on ViaBedrock this is prob bedrock fault, or my own incompetent.
        for (Vec3d vec3d1 : cache.getOldPositions()) {
            distance = Math.min(distance, vec3d1.distanceTo(vec3d));
        }

        // Distance that we calculated is not reliable, intersects should be more reliable, not a good thing to do tho.
        boolean intersects = cache.getBoundingBox().intersects(player.boundingBox.expand(3.1));
        return !intersects && distance < 6 && distance > MAX_REACH;
    }
}
