package space.itoncek.trailcompass.modules;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.pkg.objects.UserMeta;
import space.itoncek.trailcompass.pkg.objects.User;
import static space.itoncek.trailcompass.utils.Randoms.generateRandomString;

import java.sql.SQLException;
import java.util.Optional;

public class LoginSystem {
	private static final Logger log = LoggerFactory.getLogger(LoginSystem.class);
	public static Algorithm algorithm;
	public static JWTGenerator<UserMeta> generator;
	public static JWTVerifier verifier;
	public static JWTProvider<UserMeta> provider;
	private final TrailServer server;

	public LoginSystem(TrailServer server) throws SQLException {
		this.server = server;

		algorithm = Algorithm.HMAC512(generateRandomString(1024, true, true));

		generator = (user, alg) -> {
			JWTCreator.Builder token = JWT.create()
					.withClaim("id", user.id())
					.withClaim("validuntil", user.validUntil());
			return token.sign(alg);
		};
		verifier = JWT.require(algorithm).build();
		provider = new JWTProvider<>(algorithm, generator, verifier);
	}

	@OpenApi(
			summary = "Acquire JWT token",
			operationId = "login",
			path = "/uac/login",
			methods = HttpMethod.POST,
			tags = {"UAC"},
			requestBody = @OpenApiRequestBody(description = "Body containing username and SHA-512'd password", content = @OpenApiContent(mimeType = "application/json", example = """
					{
					    "username": "IToncek",
					    "passwordhash": "fec799ae04ebe814db7f1d9d21dbca0e834c175e2177ac98c8ee2c2219b3687b8d931f290209a2a6c6cfd72a39b3724be768d69250cafd30fef947fe829711f2"
					}
					""")),
			responses = {@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "application/json", example = """
					{
					    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpZCI6MSwidmFsaWR1bnRpbCI6MTUzODc0MjYxN30.-So-FS-bAvizmblLtGdSQHnepCsQ0J7JnwSVTsmRN2ybYOQAyNsMjsakljgtJdc-LdwcV0zgYBVBQVJaAAaU2A"
					}
					""")}),
					@OpenApiResponse(status = "401", content = {@OpenApiContent(mimeType = "application/json", example = """
							{
							    "error": "Unable to authorize this account, this incident has been reported!"
							}
							""")}, description = "Token has been denied/wrong syntax")
			},
			security = @OpenApiSecurity(name = "no authorization")
	)
	public void login(Context ctx) {
		try {
			JSONObject body = new JSONObject(ctx.body());

			UserMeta user = server.db.getUserMeta(body.getString("username"), body.getString("passwordhash"));

			if (user == null) {
				ctx.status(401).result(new JSONObject().put("error", "Unable to authorize this account, this incident has been reported!").toString(4));
				return;
			}

			JSONObject ident = new JSONObject().put("token", generator.generate(user, algorithm));
			ctx.res().setContentType("application/json");
			ctx.result(ident.toString());

		} catch (JSONException e) {
			log.info("JSON Exception", e);
			ctx.status(401).result(new JSONObject().put("error", "Unable to authorize this account, this incident has been reported!").toString(4));
		}
	}

	@OpenApi(
			summary = "Register new user",
			description = "User authentificated with JWT token in the header must have \"ADD_USER\" permission in the database",
			operationId = "reguster",
			path = "/uac/register",
			methods = HttpMethod.POST,
			tags = {"UAC"},
			requestBody = @OpenApiRequestBody(description = "Body containing username, SHA-512'd password and permissions", content = @OpenApiContent(mimeType = "application/json", example = """
					{
						"title":"dan",
						"passwordhash": "a58e0ac7c87a679ca4bd18593f0efc4208b9ea812e7e1b27f57b969d3c527fd43f06b6ccc76baaa88f3340f563699fd6b79b5a72295076702759ac2238436844",
						"permissions": [
							"ADMIN",
							"ADD_USERS"
						]
					}
					""")),
			responses = {@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "text/plain", example = "ok")}),
					@OpenApiResponse(status = "400", content = @OpenApiContent(mimeType = "application/json", example = """
							{
							    "error": "Neplatný token!"
							}
							""")),
					@OpenApiResponse(status = "500", content = @OpenApiContent(mimeType = "application/json", example = """
							{
							    "error": "DB error"
							}
							"""))
			},
			security = @OpenApiSecurity(name = "JWT")
	)
	public void register(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		try {
			Optional<DecodedJWT> decodedJWT = provider.validateToken(ctx.header(Header.AUTHORIZATION).substring(7));
			if (decodedJWT.isEmpty()) {
				ctx.status(400).result(new JSONObject().put("error", "Neplatný token!").toString(4));
				return;
			}
			JSONObject body = new JSONObject(ctx.body());

			int requesterID = decodedJWT.get().getClaim("id").asInt();
			User requester = server.db.getUserByID(requesterID);

			if (requester == null || !requester.admin()) {
				ctx.status(400).result(new JSONObject().put("error", "Neplatný token!").toString(4));
				return;
			}

			if (server.db.createUser(body.getString("title"), body.getString("passwordhash"))) {
				ctx.status(200).result("ok");
			} else {
				ctx.status(500).result("DB error");
			}

		} catch (JSONException | AlgorithmMismatchException | SignatureVerificationException |
				 TokenExpiredException | MissingClaimException | IncorrectClaimException e) {
			ctx.status(400).result(new JSONObject().put("error", "Neplatný dotaz!").toString(4));
		}
	}

	public void amIAdmin(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		User user = getUser(ctx);
		if (user != null) {
			ctx.status(200).contentType(ContentType.TEXT_PLAIN).result("%b".formatted(user.admin()));
		} else ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.TEXT_PLAIN).result("Unable to find that user!");
	}

	public void myName(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		User user = getUser(ctx);
		if (user != null) {
			ctx.status(200).contentType(ContentType.TEXT_PLAIN).result(user.name());
		} else ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.TEXT_PLAIN).result("Unable to find that user!");
	}

	public void verifyLogin(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		try {
			Optional<DecodedJWT> decodedJWT = provider.validateToken(ctx.header("Token"));
			if (decodedJWT.isPresent()) {
				JSONObject resp = new JSONObject();
				resp.put("title", decodedJWT.get().getClaim("title").asString());
				resp.put("id", decodedJWT.get().getClaim("id").asInt());
				ctx.status(200).result(resp.toString(4));
			} else {
				ctx.status(HttpStatus.UNAUTHORIZED).result("Neplatný token!");
			}
		} catch (JSONException | AlgorithmMismatchException | SignatureVerificationException |
				 TokenExpiredException | MissingClaimException | IncorrectClaimException e) {
			ctx.status(HttpStatus.UNAUTHORIZED).result("Neplatný token!");
		}

	}

	public void checkTokenValidity(Context h) {
		if (h.path().startsWith("/uac/login")) return;
		else if (h.path().equals("/uac/verifyLogin")) return;
		else if (h.path().equals("/")) return;
		else if (h.method().equals(HandlerType.GET) && server.dev) return;
		try {
			String header = h.header(Header.AUTHORIZATION);
			if (header == null) {
				throw new MissingClaimException(h.ip());
			}

			DecodedJWT verify = verifier.verify(header.substring(7));

			if (verify.getClaim("validuntil").asLong() < System.currentTimeMillis()) {
				h.status(HttpStatus.IM_A_TEAPOT).result("expired");
			}
		} catch (JSONException | AlgorithmMismatchException | SignatureVerificationException |
				 TokenExpiredException | MissingClaimException | IncorrectClaimException e) {
			h.status(HttpStatus.UNAUTHORIZED).result("Neplatný token!");
		}
	}

	public @Nullable User getUser(Context ctx) {
		Optional<DecodedJWT> decodedJWT = provider.validateToken(ctx.header(Header.AUTHORIZATION).substring(7));
		if (decodedJWT.isEmpty()) {
			return null;
		}

		int requesterID = decodedJWT.get().getClaim("id").asInt();
		return server.db.getUserByID(requesterID);
	}

	public boolean isHealthy() {
		return provider != null && verifier != null && generator != null && algorithm != null;
	}
}
