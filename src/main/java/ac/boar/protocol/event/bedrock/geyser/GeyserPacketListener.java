package ac.boar.protocol.event.bedrock.geyser;

public interface GeyserPacketListener {
    default void onPacketReceived(final GeyserReceivedEvent event) {}
}
