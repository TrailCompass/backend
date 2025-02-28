package space.itoncek.trailcompass.modules;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZonedDateTime;

public class MapServer {
	private static final Logger log = LoggerFactory.getLogger(MapServer.class);
	private final TrailServer server;
	private String sha256 = null;
	private ZonedDateTime timeout = ZonedDateTime.now().minusYears(100);

	public MapServer(TrailServer server) {
		this.server = server;
		new File("./data/servermap.map").getParentFile().mkdirs();
	}

	public void getServerMapHash(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		try {
			if(sha256 == null ||ZonedDateTime.now().isAfter(timeout)) {
				sha256 = DigestUtils.sha256Hex(new FileInputStream("./data/servermap.map"));
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
			ctx.status(HttpStatus.OK).result(new FileInputStream("./data/servermap.map"));
		} catch (IOException e) {
			log.error("Unable to read mapfile",e);
		}finally {
			System.gc();
		}
	}

	public boolean isHealthy() {
		return new File("./data/servermap.map").exists();
	}
}
