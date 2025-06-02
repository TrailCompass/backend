package space.itoncek.trailcompass.exchange;

import com.auth0.jwt.interfaces.DecodedJWT;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IDeckExchange;
import space.itoncek.trailcompass.commons.objects.Card;
import space.itoncek.trailcompass.commons.objects.CardCastRequirement;
import space.itoncek.trailcompass.commons.requests.deck.*;
import space.itoncek.trailcompass.commons.responses.deck.*;
import space.itoncek.trailcompass.commons.utils.BackendException;
import space.itoncek.trailcompass.gamedata.metadata.CardMetadata;
import space.itoncek.trailcompass.gamedata.metadata.CurseMetadataHandler;

import java.io.IOException;
import java.util.UUID;

public class DeckExchange implements IDeckExchange {
	private final TrailServer server;

	public DeckExchange(TrailServer server) {
		this.server = server;
	}

	@Override
	public CardListResponse listCards(ListCardsRequest req) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(req);
		if (jwt == null) {
			return null;
		}
		UUID requesterId = UUID.fromString(jwt.getClaim("id").asString());
		return new CardListResponse(server.dm.listAllMyCards(requesterId));
	}

	@Override
	public CardResponse drawCard(DrawCardRequest req) {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(req);
		if (jwt == null) {
			return null;
		}

		UUID requesterId = UUID.fromString(jwt.getClaim("id").asString());
		UUID cardUuid = server.dm.drawCardForPlayer(requesterId);
		return new CardResponse(cardUuid);
	}

	@Override
	public CardCastRequirementsResponse getCardCastRequirements(FetchCardCastRequirementsRequest req) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(req);
		if (jwt == null) {
			return null;
		}

		UUID cardUuid = req.cardUUID();
		CardCastRequirement ccr = server.dm.getCardCastRequirement(cardUuid);
		return new CardCastRequirementsResponse(ccr);
	}

	@Override
	public CardMetadataResponse getCardMetadata(FetchCardMetadataRequest req) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(req);
		if (jwt == null) {
			return null;
		}

		UUID cardUuid = req.cardUUID();
		Card ccr = server.dm.getCardMetadata(cardUuid);
		return new CardMetadataResponse(ccr);
	}

	@Override
	public CurseMetadataResponse getCurseMetadata(FetchCurseMetadataRequest req) throws BackendException {
		try {
			DecodedJWT jwt = server.tch.ex.auth().getJWTToken(req);
			if (jwt == null) {
				return null;
			}

			UUID cardUuid = req.cardUUID();
			Card ccr = server.dm.getCardMetadata(cardUuid);
			CardMetadata cardMetadata = new CurseMetadataHandler(server).parseMetadataContextDependant(ccr.type());
			return new CurseMetadataResponse(cardMetadata.cardClass(),cardMetadata.title(), cardMetadata.description(), cardMetadata.casting_cost());
		} catch (IOException e) {
			throw new BackendException(e);
		}
	}
}
