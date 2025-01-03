# Boar

### ⚠️ WARNING: THIS ONLY FOR BEDROCK PLAYER NOT JAVA PLAYER! YOU WILL NEED TO PAIR THIS WITH ANOTHER JAVA ANTICHEAT!
A dedicated (proof of concept) anti cheat for GeyserMC project.
- Warning: No guarantee about performance, lag compatibility, or if I will ever finish this.

## Features
- A packet managing system for GeyserMC
- An uncompleted prediction engine that only works on normal/elytra movement.
- 1:1 recreation of player world server-sided that accounted for lag.

## Problems
- Sprinting (or other status) on Bedrock is absolutely broken (can send both START and STOP 
at the same time or send the same status 2 tick in a row)
- Floating point errors break collision, causing false positive, or loss precision. (Partially fixed)
- Player falses after teleport, I can't figure out why movement act weirdly after teleport yet.
- Step motion is different on Bedrock.
- A lot of stuff is still unimplemented.

### Differences from other Geyser anti-cheat.
#### You can take a look at a list of "other anti-cheats" [here](https://geysermc.org/wiki/geyser/anticheat-compatibility/)
- Fully packet-based that support both reading Java-Bedrock packet instead of relying on Bukkit events or java-only packet manager.
- Fully prediction-based, check player movement on a 1:1 scale, detecting any *1.0E-4* movement abnormality.
- Fully synced world (and other) that synced with player ping to account for lag.

### Credits
- https://github.com/GeyserMC/Geyser
- https://github.com/RaphiMC/ViaBedrock
- https://github.com/ViaVersion/ViaFabricPlus
- https://github.com/Mojang/bedrock-protocol-docs
- https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Bedrock_Protocol (wiki.vg)

#### Also checkout "prototype" [branch](https://github.com/Oryxel/Boar/tree/prototype)