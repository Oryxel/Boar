package ac.boar.protocol.event.bedrock.geyser;

public interface GeyserPacketListener {
    default void onPacketSend(final GeyserSendEvent event) {}
}
