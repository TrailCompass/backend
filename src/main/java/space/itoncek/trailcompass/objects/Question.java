package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public record Question(int id, String title, String description, String casting_cost, int amount_in_deck) implements Request {
    public static @Nullable Request parse(JSONObject o) {
        if (o.getEnum(RequestType.class, "type") != RequestType.QUESTION) return null;

        return new Question(o.getInt("id"),
                o.getString("title"),
                o.getString("description"),
                o.getString("casting_cost"),
                o.getInt("amount_in_deck"));
    }

    @Override
    public JSONObject serialize() {
        return new JSONObject()
                .put("type", RequestType.QUESTION)
                .put("id", id)
                .put("title", title)
                .put("description", description)
                .put("casting_cost", casting_cost)
                .put("amount_in_deck", amount_in_deck);
    }

    @Override
    public RequestType getType() {
        return null;
    }
}
