package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface Card {
	static @Nullable Card deserialize(JSONObject o) {
		return switch (o.getEnum(CardType.class, "type")) {
			case CURSE -> Curse.parse(o);
			case POWERUP -> Powerup.parse(o);
			case TIME_BONUS -> TimeBonus.parse(o);
		};
	}

	JSONObject serialize();

	CardType getType();
}
