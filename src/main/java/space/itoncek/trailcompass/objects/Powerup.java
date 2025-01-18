package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public record Powerup(int id, String name, String icon, int amount_in_deck) implements Card {
	public static @Nullable Powerup parse(JSONObject o) {
		if (o.getEnum(CardType.class, "type") != CardType.POWERUP) return null;

		return new Powerup(o.getInt("id"),
				o.getString("name"),
				o.getString("icon"),
				o.getInt("amount_in_deck"));
	}

	@Override
	public JSONObject serialize() {
		return new JSONObject()
				.put("type", CardType.CURSE)
				.put("id", id)
				.put("name", name)
				.put("icon", icon)
				.put("amount_in_deck", amount_in_deck);
	}

	@Override
	public CardType getType() {
		return CardType.POWERUP;
	}
}
