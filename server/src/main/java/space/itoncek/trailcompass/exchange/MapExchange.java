package space.itoncek.trailcompass.exchange;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.geodesk.feature.FeatureLibrary;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IMapExchange;
import space.itoncek.trailcompass.commons.requests.map.MapHashRequest;
import space.itoncek.trailcompass.commons.requests.map.MapRequest;
import space.itoncek.trailcompass.commons.responses.map.MapHashResponse;
import space.itoncek.trailcompass.commons.responses.map.MapResponse;
import space.itoncek.trailcompass.commons.utils.BackendException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;

public class MapExchange implements IMapExchange {
	private final TrailServer server;
	private final JSONObject config;
	private final FeatureLibrary flib;
	private String sha256 = null;
	private ZonedDateTime timeout = ZonedDateTime.now().minusYears(100);

	public MapExchange(TrailServer server) {
		this.server = server;
		new File("./data").mkdirs();
		try {
			if (!new File("./data/mapconfig.json").exists()) {
				Files.writeString(new File("./data/mapconfig.json").toPath(),
						new JSONObject()
								.put("feature-library", "flib.gol")
								.put("android-map", "prague.osm.map")
								.toString(4),
						StandardOpenOption.CREATE);
			}
			this.config = new JSONObject(Files.readString(new File("./data/mapconfig.json").toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		flib = new FeatureLibrary("./data/" + config.getString("feature-library"));
	}

	@Override
	public MapHashResponse getMapHash(MapHashRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		try {
			if (sha256 == null || ZonedDateTime.now().isAfter(timeout)) {
				sha256 = DigestUtils.sha256Hex(new FileInputStream("./data/" + config.getString("android-map")));
				timeout = ZonedDateTime.now().plusMinutes(10);
			}
			return new MapHashResponse(sha256);
		} catch (Exception e) {
			throw new BackendException(e);
		}
	}

	@Override
	public MapResponse getMap(MapRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		try (FileInputStream fis = new FileInputStream("./data/" + config.getString("android-map"))){
			byte[] bytes = IOUtils.toByteArray(fis);
			return new MapResponse(bytes);
		} catch (Exception e) {
			throw new BackendException(e);
		} finally {
			System.gc();
		}
	}
}
