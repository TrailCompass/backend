package space.itoncek.trailcompass.pkg.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public record Curse(int id, String title, String description, String casting_cost, int amount_in_deck) implements Card {
	public static @Nullable Curse parse(JSONObject o) {
		if (o.getEnum(CardType.class, "type") != CardType.CURSE) return null;

		return new Curse(o.getInt("id"),
				o.getString("title"),
				o.getString("description"),
				o.getString("casting_cost"),
				o.getInt("amount_in_deck"));
	}

	@Override
	public JSONObject serialize() {
		return new JSONObject()
				.put("type", CardType.CURSE)
				.put("id", id)
				.put("title", title)
				.put("description", description)
				.put("casting_cost", casting_cost)
				.put("amount_in_deck", amount_in_deck);
	}

	@Override
	public CardType getType() {
		return CardType.CURSE;
	}
}
