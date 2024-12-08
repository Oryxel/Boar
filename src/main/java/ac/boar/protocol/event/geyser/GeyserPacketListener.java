package ac.boar.protocol.event.geyser;

public interface GeyserPacketListener {
    default void onPacketSend(final GeyserSendEvent event) {
    }
}
