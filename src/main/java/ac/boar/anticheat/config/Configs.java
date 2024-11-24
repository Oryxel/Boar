package ac.boar.anticheat.config;

import ac.boar.anticheat.config.api.ConfigTag;

public class Configs {

    @ConfigTag(name = "cancelInteractAttackPacket", description = "Bedrock doesn't seems to use interact packet " +
            "for attack but geyser still translate this, but you can active this settings to cancel it.")
    public static boolean CANCEL_INTERACT_ATTACK_PACKET = true;

}
