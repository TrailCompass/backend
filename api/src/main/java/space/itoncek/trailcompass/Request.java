package space.itoncek.trailcompass;

import org.json.JSONObject;
import space.itoncek.trailcompass.objects.Argument;

import java.util.List;

public interface Request {
	String getName();
	String getDescription();
	List<Argument> getArgs();

	Return execute(JSONObject args);

}
