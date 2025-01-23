package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public record TimeBonus(int id, String title, int bonus_time, int amount_in_deck) implements Card {
	public static @Nullable TimeBonus parse(JSONObject o) {
		if (o.getEnum(CardType.class, "type") != CardType.TIME_BONUS) return null;

		return new TimeBonus(o.getInt("id"),
				o.getString("title"),
				o.getInt("bonus_time"),
				o.getInt("amount_in_deck"));
	}

	@Override
	public JSONObject serialize() {
		return new JSONObject()
				.put("type", CardType.CURSE)
				.put("id", id)
				.put("title", title)
				.put("bonus_time", bonus_time)
				.put("amount_in_deck", amount_in_deck);
	}

	@Override
	public CardType getType() {
		return CardType.TIME_BONUS;
	}
}
