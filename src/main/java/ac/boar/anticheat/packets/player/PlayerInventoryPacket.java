package ac.boar.anticheat.packets.player;

import ac.boar.anticheat.handler.BlockInteractHandler;
import ac.boar.anticheat.user.api.BoarPlayer;
import ac.boar.protocol.event.bedrock.BedrockPacketListener;
import ac.boar.protocol.event.bedrock.PacketReceivedEvent;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;

// "Inventory" Transaction packet, handle use item, interact with the world, ....
public class PlayerInventoryPacket implements BedrockPacketListener {
    @Override
    public void onPacketReceived(PacketReceivedEvent event) {
        final BoarPlayer player = event.getPlayer();
        if (!(event.getPacket() instanceof InventoryTransactionPacket packet)) {
            return;
        }

        if (packet.getTransactionType() == InventoryTransactionType.ITEM_USE && packet.getActionType() == 0) {
            BlockInteractHandler.handleBlockClick(player, packet.getBlockPosition());
        }
    }
}
