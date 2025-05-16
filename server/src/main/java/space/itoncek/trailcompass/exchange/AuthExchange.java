package space.itoncek.trailcompass.exchange;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.Authorized;
import space.itoncek.trailcompass.commons.exchange.IAuthExchange;
import space.itoncek.trailcompass.commons.requests.auth.*;
import space.itoncek.trailcompass.commons.responses.auth.LoginResponse;
import space.itoncek.trailcompass.commons.responses.auth.ProfileListResponse;
import space.itoncek.trailcompass.commons.responses.auth.ProfileResponse;
import space.itoncek.trailcompass.commons.responses.generic.OkResponse;
import static space.itoncek.trailcompass.commons.utils.RandomUtils.generateRandomString;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.objects.UserMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AuthExchange implements IAuthExchange {
	private final TrailServer server;
	private final Algorithm algorithm;
	private final JWTGenerator<UserMeta> generator;
	private final JWTVerifier verifier;
	private final JWTProvider<UserMeta> provider;

	public AuthExchange(TrailServer server) {
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

	@Override
	public LoginResponse login(LoginRequest request) {
		String username = request.username();
		byte[] passwordHash = request.passwordHash();

		AtomicReference<DatabasePlayer> player = new AtomicReference<>();

		server.ef.runInTransaction(em -> {
			List<DatabasePlayer> players = em.createNamedQuery("findPlayerByNickname", DatabasePlayer.class).setParameter("nickmane", username).getResultList();
			if (players.size() == 1) {
				player.set(players.getFirst());
			} else {
				player.set(null);
			}
		});

		if (player.get() == null) {
			return null;
		}

		boolean passwordCorrect = Arrays.equals(passwordHash, player.get().getPasswordHash());

		if (!passwordCorrect) {
			return null;
		}

		return new LoginResponse(generator.generate(new UserMeta(player.get().getId(), System.currentTimeMillis() + 600000), algorithm));
	}

	@Override
	public OkResponse register(RegisterRequest request) {
		DecodedJWT jwt = getJWTToken(request);
		if (jwt == null) {
			return null;
		}
		int requesterId = jwt.getClaim("id").asInt();
		AtomicReference<DatabasePlayer> dbp = new AtomicReference<>(null);

		server.ef.runInTransaction(em -> {
			DatabasePlayer dp = em.find(DatabasePlayer.class, requesterId);
			dbp.set(dp);
		});

		DatabasePlayer db = dbp.get();

		if (db == null || db.isAdmin()) {
			return null;
		}

		server.ef.runInTransaction(em -> {
			DatabasePlayer p = DatabasePlayer.deserialize(request.p());
			em.persist(p);
		});

		return new OkResponse();
	}

	@Override
	public ProfileResponse getProfile(ProfileRequest request) {
		DecodedJWT jwt = getJWTToken(request);
		if (jwt == null) {
			return null;
		}
		int requesterId = jwt.getClaim("id").asInt();
		AtomicReference<DatabasePlayer> dbp = new AtomicReference<>(null);

		server.ef.runInTransaction(em -> {
			DatabasePlayer dp = em.find(DatabasePlayer.class, requesterId);
			dbp.set(dp);
		});

		DatabasePlayer db = dbp.get();

		return new ProfileResponse(db.serialize());
	}

	@Override
	public ProfileResponse getOtherProfile(ProfileOtherRequest request) {
		DecodedJWT jwt = getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		AtomicReference<DatabasePlayer> dbp = new AtomicReference<>(null);

		server.ef.runInTransaction(em -> {
			DatabasePlayer dp = em.find(DatabasePlayer.class, request.id());
			dbp.set(dp);
		});

		DatabasePlayer db = dbp.get();

		return new ProfileResponse(db.serialize());
	}

	@Override
	public ProfileListResponse listPlayers(ListPlayersRequest request) {
		DecodedJWT jwt = getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		AtomicReference<List<DatabasePlayer>> players = new AtomicReference<>();
		server.ef.runInTransaction(em -> {
			players.set(em.createNamedQuery("findAllPlayers", DatabasePlayer.class).getResultList());
		});

		return new ProfileListResponse(
				players.get()
						.parallelStream()
						.map(DatabasePlayer::serialize)
						.toList()
		);
	}

	private DecodedJWT getJWTToken(Authorized obj) {
		Optional<DecodedJWT> decodedJWT = provider.validateToken(obj.token().token());
		return decodedJWT.orElse(null);
	}
}
