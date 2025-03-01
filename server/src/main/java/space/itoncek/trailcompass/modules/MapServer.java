package space.itoncek.trailcompass.modules;

import com.geodesk.feature.FeatureLibrary;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;

public class MapServer {
	private static final Logger log = LoggerFactory.getLogger(MapServer.class);
	private final TrailServer server;
	private final JSONObject config;
	public FeatureLibrary flib;
	private String sha256 = null;
	private ZonedDateTime timeout = ZonedDateTime.now().minusYears(100);

	public MapServer(TrailServer server) {
		this.server = server;
		new File("./data").mkdirs();
		try {
			this.config = new JSONObject(Files.readString(new File("./data/mapconfig.json").toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setup() {
		flib = new FeatureLibrary("./data/"+config.getString("feature-library"));
	}

	public void getServerMapHash(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		try {
			if(sha256 == null ||ZonedDateTime.now().isAfter(timeout)) {
				sha256 = DigestUtils.sha256Hex(new FileInputStream("./data/"+config.getString("android-map")));
				timeout = ZonedDateTime.now().plusMinutes(10);
			}
			ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result(sha256);
		} catch (IOException e) {
			log.error("Unable to hash mapfile",e);
		}
	}

	public void getServerMap(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		try {
			FileInputStream fis = new FileInputStream("./data/" + config.getString("android-map"));
			ctx.status(HttpStatus.OK).header(Header.CONTENT_LENGTH,fis.available() + "").result(fis);
		} catch (IOException e) {
			log.error("Unable to read mapfile",e);
		}finally {
			System.gc();
		}
	}

	public boolean isHealthy() {
		return new File("./data/"+config.getString("android-map")).exists();
	}
}
