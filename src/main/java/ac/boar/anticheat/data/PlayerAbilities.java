package ac.boar.anticheat.data;

import lombok.Getter;
import lombok.Setter;
import org.cloudburstmc.protocol.bedrock.data.Ability;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class PlayerAbilities {
    private final Set<Ability> abilities = new HashSet<>();
}
