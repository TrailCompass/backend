package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public record RequestClass(int id, String name, int draw_cards, int pick_cards) {
	@NotNull
	public static RequestClass parse(JSONObject o) {
		return new RequestClass(o.getInt("id"),
				o.getString("name"),
				o.getInt("draw_cards"),
				o.getInt("pick_cards"));
	}

	public JSONObject serialize() {
		return new JSONObject()
				.put("id", id)
				.put("name", name)
				.put("draw_cards", draw_cards)
				.put("pick_cards", pick_cards);
	}
}
