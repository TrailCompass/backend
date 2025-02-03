package space.itoncek.trailcompass.measuring;

import org.slf4j.Logger;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.objects.LocationSupplier;
import space.itoncek.trailcompass.objects.Type;

import java.util.Optional;

public class AltitudeMeasurementRequest implements Request {
	private final LocationSupplier ls;
	private final Logger l;

	public AltitudeMeasurementRequest(LocationSupplier ls, Logger l) {
		this.ls = ls;
		this.l = l;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Type getRequestType() {
		return Type.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			return Optional.of(ls.getSeekerLocation().call().alt() > ls.getHiderLocation().call().alt());
		} catch (Exception e) {
			l.error("Unable to parse players' locations.",e);
			return Request.super.predictBool();
		}
	}
}
