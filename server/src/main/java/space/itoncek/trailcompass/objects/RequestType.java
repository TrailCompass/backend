package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public record RequestType(int id, int classID, String name, String description, String svg_icon_url) {
    @NotNull
    public static RequestType parse(JSONObject o) {
        return new RequestType(o.getInt("id"),
                o.getInt("classID"),
                o.getString("name"),
                o.getString("description"),
                o.getString("svg_icon_url"));
    }

    public JSONObject serialize() {
        return new JSONObject()
                .put("id", id)
                .put("classID", classID)
                .put("name", name)
                .put("description", description)
                .put("svg_icon_url", svg_icon_url);
    }
}
