package ac.boar.protocol.event;

public interface BedrockPacketListener {
    default void onPacketReceived(final PacketReceivedEvent event) {
    }
}
