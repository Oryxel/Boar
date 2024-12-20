# Boar

A dedicated (proof of concept) ~~anti cheat~~ for GeyserMC project.

- Warning: This anti-cheat is not the best thing ever, I believed that a lot of other people
  could've done it much better (Polar developers, or any developer that have work on a prediction ac before).
- I can't guarantee I will ever finish this, or anything performance-related, falses, lag compatibility.

## Features

- A packet managing system for GeyserMC
- A broken teleport handling system
- An uncompleted prediction engine that only works on normal/elytra movement. (1e-4 accuracy)

## Problems

- Floating point errors break collision, causing false positive, or loss precision
- Player can clip into walls (they will glitch back anyway, but it will still cause false) mentioned
  in [3370](https://github.com/GeyserMC/Geyser/issues/3370) and [4269](https://github.com/GeyserMC/Geyser/issues/4269)
- Slamming your head while trying to move on the Z (and Y) axis causes 0.002 false positive.
- Sprinting while slamming your head to the walls causes a bunch of falses.
- Prediction engine accuracy is yuck and can be even worse if player movement is too fast or in other cases.
- Collision is wrong and broken (different bounding boxes, collisions is wrong on block edge, sneaking on edge is wrong)
- ~~Teleport system is broken (partially fixed)~~
- A lot of stuff is still unimplemented.

#### Things to note about (for me - or any contributors)

- Why the **HELL** position (and everything else) stored in floating point. (just switch to double this is stupid [11418](https://bugs.mojang.com/browse/MCPE-11418))
- A lot of bounding boxes is different, ~~step motion seems to be different (not sure)~~?
- Sprinting/Sneaking status client sent doesn't seem to match up with player movement
- NetworkStackLatency (ping packet), is deprecated, and might be removed soon (hope not).
- CorrectPlayerMovePrediction is weird, the velocity player actually moves seems to be different from
  the one we sent, (it's not EOT or anything, it's just that...?), this is a TODO.
- Sneaking collisions calculation currently is wrong on both Bedrock (and Java - ViaBedrock).
- Push out of block is instant, not slowly like on single-player world or on Java? (prob geyser teleport.)
- I don't trust my way of hooking and reading packet sent from/to client enough, it might not work
  sometimes (although that never happened).

### Credit:

- https://github.com/PrismarineJS/minecraft-data
- https://github.com/CloudburstMC/ProxyPass
- https://github.com/GeyserMC/Geyser