package space.itoncek.trailcompass.measuring;

import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.RequestCategory;

import java.util.List;

public class MeasuringCategory implements RequestCategory {
	@Override
	public String getName() {
		return "Measuring";
	}

	@Override
	public String getDescription() {
		return "Compared to me, are you closer to _____?";
	}

	@Override
	public List<Request> getRequests() {
		return List.of();
	}
}
