package ac.boar.protocol.event.java;

public interface PacketListener {
    default void onPacketSend(final PacketSendEvent event) {
    }
}