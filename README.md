# Boar

A dedicated (proof of concept) ~~anti cheat~~ for GeyserMC project.

- Warning: This anti-cheat is really terrible and so is the code, I'm not
  good at making anti-cheat too which sounds like an excuse, and it is lol.
- I also don't guarantee anything about performance or false lol. I don't even think I will ever
  finish this.

## Features

- A packet managing system for GeyserMC
- A broken teleport handling system
- An uncompleted prediction engine that only works on normal movement. (1e-4 accuracy)

## Problems

- Player can clip into walls (they will glitch back anyway, but it will still cause false) mentioned
  in [3370](https://github.com/GeyserMC/Geyser/issues/3370) and [4269](https://github.com/GeyserMC/Geyser/issues/4269)
- ~~Floating point errors BREAK A LOT OF THINGS, this is stupid and break my collision system completely.  (Normally
  1e-5 -> 0.1) (partially fixed)~~
- Prediction engine accuracy is yuck and can be even worse if player movement is too fast or in other cases.
- Collision is wrong and broken (different bounding boxes, collisions is wrong on block edge, sneaking on edge is wrong)
- ~~Teleport system is broken (partially fixed)~~
- A lot of stuff is still unimplemented.

### Credit:

- https://github.com/PrismarineJS/minecraft-data
- https://github.com/CloudburstMC/ProxyPass
- https://github.com/GeyserMC/Geyser