package space.itoncek.trailcompass.radar;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;

import java.util.Optional;

class GenericRadarTest {
	private static final Logger log = LoggerFactory.getLogger(GenericRadarTest.class);
	private LocationSupplier ls;

	@BeforeEach
	void setUp() {
		ls = new LocationSupplier(() -> new Location(50.0544700, 14.2905664, 0),
				() -> new Location(50.1089592, 14.5773658, 0));
	}

	@Test
	void testRadar() {
		RadarCategory radarCategory = new RadarCategory(ls, log);

		for (Request request : radarCategory.getRequests()) {
			if(request instanceof GenericRadarRequest gr) {
				Optional<Boolean> b = gr.predictBool();
				assertTrue(b.isPresent());
				assertSame(b.get(), gr.getRadius() >= 21.34214140643563);
			}
		}
	}
}