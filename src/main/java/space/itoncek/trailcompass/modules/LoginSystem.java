package space.itoncek.trailcompass.modules;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import io.javalin.http.Header;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.SimpleUser;

import java.sql.SQLException;
import java.util.Optional;

public class LoginSystem {
    public static Algorithm algorithm;
    public static JWTGenerator<SimpleUser> generator;
    public static JWTVerifier verifier;
    public static JWTProvider<SimpleUser> provider;
    private final TrailServer server;

    public LoginSystem(TrailServer server) throws SQLException {
        this.server = server;

        algorithm = Algorithm.HMAC512(generateRandomString());

        generator = (user, alg) -> {
            JWTCreator.Builder token = JWT.create()
                    .withClaim("id", user.id())
                    .withClaim("validuntil", user.validUntil());
            return token.sign(alg);
        };
        verifier = JWT.require(algorithm).build();
        provider = new JWTProvider<>(algorithm, generator, verifier);
    }

    public void registerHandlers(Javalin app) {
        app.before(h -> {
            if ((h.method() == HandlerType.POST) && !(h.path().startsWith("/uac/login") || h.path().equals("/uac/verifyLogin"))) {
                try {
                    String header = h.header(Header.AUTHORIZATION);
                    if (header == null) throw new MissingClaimException(h.ip());
                    else verifier.verify(header.substring(6));
                } catch (JSONException | AlgorithmMismatchException | SignatureVerificationException |
                         TokenExpiredException | MissingClaimException | IncorrectClaimException e) {
                    h.status(401).result(new JSONObject().put("error", "Neplatný token!").toString(4));
                }
            }
        });

        app.post("/uac/login", ctx -> ctx.async(() -> {
            try {
                JSONObject body = new JSONObject(ctx.body());

                SimpleUser user = server.db.getUser(body.getString("username"), body.getString("passwordhash"));

                if(user == null) {
                    ctx.status(401).result(new JSONObject().put("error", "Unable to authorize this account, this incident has been reported!").toString(4));
                    return;
                }

                JSONObject ident = new JSONObject().put("token", generator.generate(user, algorithm));
                ctx.res().setContentType("application/json");
                ctx.result(ident.toString());

            } catch (JSONException e) {
                ctx.status(401).result(new JSONObject().put("error", "Unable to authorize this account, this incident has been reported!").toString(4));
            }
        }));

        app.post("/uac/verifyLogin", ctx -> ctx.async(() -> {
            if (ctx.status().getCode() != 401) {
                try {
                    Optional<DecodedJWT> decodedJWT = provider.validateToken(ctx.body());
                    if (decodedJWT.isPresent()) {
                        JSONObject resp = new JSONObject();
                        resp.put("name", decodedJWT.get().getClaim("name").asString());
                        resp.put("id", decodedJWT.get().getClaim("id").asInt());
                        ctx.status(200).result(resp.toString(4));
                    } else {
                        ctx.status(400).result(new JSONObject().put("error", "Neplatný token!").toString(4));
                    }
                } catch (JSONException | AlgorithmMismatchException | SignatureVerificationException |
                         TokenExpiredException | MissingClaimException | IncorrectClaimException e) {
                    ctx.status(400).result(new JSONObject().put("error", "Neplatný token!").toString(4));
                }
            }
        }));
    }

    private static String generateRandomString() {
        return RandomStringUtils.random(1024, true, true);
    }
}
