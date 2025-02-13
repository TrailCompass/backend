package space.itoncek.trailcompass.measuring;

import com.geodesk.feature.FeatureLibrary;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static space.itoncek.trailcompass.measuring.PoiMeasurementRequest.*;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

class PoiMeasurementRequestTest {

	private static final Logger log = LoggerFactory.getLogger(PoiMeasurementRequestTest.class);
	private FeatureLibrary flib;
	private LocationSupplier ls;

	@BeforeEach
	void setUp() {
		new File("./praha.gol").deleteOnExit();
		flib = new FeatureLibrary(new File("./praha.gol").getPath(),"https://cdn.itoncek.space/praha");
		ls = new LocationSupplier(() -> new Location(50.0544700, 14.2905664, 0),
				() -> new Location(50.1089592, 14.5773658, 0));
	}

	@Test
	void predictBool() {
		HashMap<String, Boolean> testMap = new HashMap<>();

		testMap.put(MOUNTAIN, false);
		testMap.put(RAILWAY_STATION, true);
		testMap.put(AMUSEMENT_PARK, false);
		testMap.put(ZOO, true);
		testMap.put(AQUARIUM, false);
		testMap.put(MUSEUM, true);
		testMap.put(CINEMA, false);
		testMap.put(HOSPITAL, false);
		testMap.put(LIBRARY, true);
		testMap.put(EMBASSY, false);

		log.info("--------------------------------------");
		testMap.forEach((t,r)-> {
			log.info("Starting test for {}", t);
			PoiMeasurementRequest pmr = new PoiMeasurementRequest(flib,ls,log,t);
			Optional<Boolean> bool = pmr.predictBool();
			assertTrue(bool.isPresent());
			assertSame(bool.get(), r);
			log.info("Test finished with result {}",bool.get());
			log.info("--------------------------------------");
		});
	}

	@AfterEach
	void tearDown() {
		flib.close();
	}
}