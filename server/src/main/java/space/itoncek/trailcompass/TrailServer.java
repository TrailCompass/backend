package space.itoncek.trailcompass;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("dev") != null && Boolean.parseBoolean(System.getenv("dev"));
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));

	public TrailServer() {

	}
	@OpenApi(
			summary = "Get server time, useful for determining ping and synchronising clocks",
			operationId = "time",
			path = "/time",
			methods = HttpMethod.GET,
			tags = {"SYSTEM"},
			responses = {
					@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "text/plain", example = "1739379173617")})
			}
	)
	private void getTime(@NotNull Context context) {
		context.status(HttpStatus.OK).result(System.currentTimeMillis() + "");
	}

	@OpenApi(
			summary = "Get server version",
			operationId = "ver",
			path = "/",
			methods = HttpMethod.GET,
			tags = {"SYSTEM"},
			responses = {
					@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "text/plain", example = "vDEVELOPMENT")})
			}
	)
	private void getVersion(@NotNull Context context) {
		context.status(HttpStatus.OK).result(TrailServer.class.getPackage().getImplementationVersion() == null? "vDEVELOPMENT":TrailServer.class.getPackage().getImplementationVersion());
	}

	public static void main(String[] args) {
		TrailServer ts = new TrailServer();

		ts.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				ts.stop();
			} catch (IOException e) {
				log.error("Unable to stop the server cleanly!", e);
			}
		}));
	}

	private void start() {

	}

	private void stop() throws IOException {

	}
}