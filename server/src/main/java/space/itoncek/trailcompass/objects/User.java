package space.itoncek.trailcompass.objects;

import org.json.JSONObject;

public record User(int id, String name, boolean admin, boolean hider) {
    public JSONObject toJSON() {
        return new JSONObject()
                .put("id",id)
                .put("name",name)
                .put("admin",admin)
                .put("hider",hider);
    }
}
