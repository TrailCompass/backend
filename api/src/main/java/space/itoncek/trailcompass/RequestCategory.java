package space.itoncek.trailcompass;

import java.util.List;

public interface RequestCategory {
	String getName();
	String getDescription();
	List<Request> getRequests();
}
