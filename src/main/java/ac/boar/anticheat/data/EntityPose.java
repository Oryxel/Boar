package ac.boar.anticheat.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EntityPose {
	STANDING(0),
	GLIDING(1),
	SLEEPING(2),
	SWIMMING(3),
	SPIN_ATTACK(4),
	CROUCHING(5),
	LONG_JUMPING(6),
	DYING(7),
	CROAKING(8),
	USING_TONGUE(9),
	SITTING(10),
	ROARING(11),
	SNIFFING(12),
	EMERGING(13),
	DIGGING(14),
	SLIDING(15),
	SHOOTING(16),
	INHALING(17);

	private final int index;
}