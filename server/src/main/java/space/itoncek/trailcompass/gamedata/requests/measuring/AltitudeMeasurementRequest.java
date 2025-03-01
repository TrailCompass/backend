package space.itoncek.trailcompass.gamedata.requests.measuring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Request;
import space.itoncek.trailcompass.objects.gamedata.Type;

import java.util.Optional;

public class AltitudeMeasurementRequest implements Request {
	private static final Logger log = LoggerFactory.getLogger(AltitudeMeasurementRequest.class);
	private final TrailServer server;

	public AltitudeMeasurementRequest(TrailServer server) {
		this.server = server;
	}

	@Override
	public String getID() {
		return "space.itoncek.trailcompass.gamedata.requests.measuring.AltitudeMeasurementRequest";
	}

	@Override
	public String getName() {
		return "Altitude";
	}

	@Override
	public String getDescription() {
		return "Is your altitude higher than mine?";
	}

	@Override
	public Type getRequestType() {
		return Type.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			return Optional.of(server.lm.getSeekerLocation().alt() < server.lm.getHiderLocation().alt());
		} catch (Exception e) {
			log.error("Unable to parse players' locations.",e);
			return Request.super.predictBool();
		}
	}
}
