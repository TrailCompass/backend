package space.itoncek.trailcompass.objects;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface Request {
    static @Nullable Request deserialize(JSONObject o) {
        return Question.parse(o);
    }
    JSONObject serialize();

    RequestType getType();
}
