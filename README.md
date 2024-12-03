# Boar
A dedicated anti cheat for GeyserMC project.
- Warning: This anti-cheat is really terrible and so is the code, I'm not 
good at making anti-cheat too which sounds like an excuse, and it is lol.

## Features
- A packet managing system for GeyserMC
- A broken teleport handling system
- A uncompleted prediction engine that only works on normal movement. (1e-4 accuracy)
- A broken reach check that can only detect around 3.2 (not reliable btw).
## Problems
- Player can clip into walls (they will glitch back anyway, but it will still cause false) mentioned in [3370](https://github.com/GeyserMC/Geyser/issues/3370) and [4269](https://github.com/GeyserMC/Geyser/issues/4269)
- Prediction engine accuracy is yuck and can be even worse if player movement is too fast or in other cases.
- Teleport handling system is broken (partially fixed)
- A lot of stuff is still unimplemented.

### Credit:
- https://github.com/PrismarineJS/minecraft-data
- https://github.com/CloudburstMC/ProxyPass
- https://github.com/GeyserMC/Geyser