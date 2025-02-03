package space.itoncek.trailcompass.measuring;

import com.geodesk.feature.FeatureLibrary;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static space.itoncek.trailcompass.measuring.RelationMeasurementRequest.*;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class RelationMeasurementRequestTest {

	private static final Logger log = LoggerFactory.getLogger(RelationMeasurementRequestTest.class);
	private FeatureLibrary flib;
	private LocationSupplier ls;

	@BeforeEach
	void setUp() {
		flib = new FeatureLibrary(new File("./praha.gol").getPath(),"https://cdn.itoncek.space/praha");
		ls = new LocationSupplier(() -> new Location(50.0544700, 14.2905664, 0),
				() -> new Location(50.1089592, 14.5773658, 0));
		new File("./praha.gol").deleteOnExit();
	}

	@Test
	void predictBool() {
		HashMap<String, Boolean> testMap = new HashMap<>();

		//testMap.put(INTERNATIONAL_BORDER, false);
		testMap.put(CZ_KRAJE, false);
		testMap.put(CZ_OKRES, false);
		testMap.put(CZ_OBEC, false);

		log.info("--------------------------------------");
		for (Map.Entry<String, Boolean> entry : testMap.entrySet()) {
			String t = entry.getKey();
			Boolean r = entry.getValue();

			log.info("Starting test for {}", t);
			RelationMeasurementRequest pmr = new RelationMeasurementRequest(flib, ls, log, t);
			Optional<Boolean> bool = pmr.predictBool();
			assertTrue(bool.isPresent());
			assertSame(bool.get(), r);
			log.info("Test finished with result {}", bool.get());
			log.info("--------------------------------------");
		}
	}

	@AfterEach
	void tearDown() {
		flib.close();
	}
}