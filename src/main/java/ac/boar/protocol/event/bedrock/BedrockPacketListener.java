package ac.boar.protocol.event.bedrock;

public interface BedrockPacketListener {
    default void onPacketReceived(final PacketReceivedEvent event) {}
}
