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
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof InteractPacket) {
            final InteractPacket packet = (InteractPacket) event.getPacket();
            if (packet.getAction() != InteractPacket.Action.DAMAGE) {
                return;
            }

            if (check(player, packet.getRuntimeEntityId())) {
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

        double distance = Math.min(cache.getBoundingBox().getMinPos().distanceTo(vec3d), cache.getBoundingBox().getMaxPos().distanceTo(vec3d));
        if (cache.getBoundingBox().contains(player.x, player.y + EntityDefinitions.PLAYER.offset(), player.z)) {
            distance = 0;
        }

        // I don't think I'm a good anti cheat dev lol, something 100% wrong with how I handle relative.
        // This also false if move at a rapid speed or teleport, it's 100% going to false.
        if (distance > 3) {
            Bukkit.broadcastMessage("Distance: " + distance);
        }

        Bukkit.broadcastMessage("Intersects: " + cache.getBoundingBox().intersects(player.boundingBox.expand(3.0)));
        return false;
    }
}
