package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.cards.Card;
import space.itoncek.trailcompass.database.cards.DeckCard;
import space.itoncek.trailcompass.database.cards.ShadowCard;
import space.itoncek.trailcompass.gamedata.HomeGameDeck;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeckManager {
	private final TrailServer server;

	public DeckManager(TrailServer server) {
		this.server = server;
	}

	public void resetDeck() {
		discardAllCards();
		initNewDeck();
	}

	private void initNewDeck() {
		HomeGameDeck deck = new HomeGameDeck(server);
		server.ef.runInTransaction(em -> {
			deck.cards.stream().map(x -> {
				DeckCard card = new DeckCard();
				card.setType(x.getType());
				card.setOwner(null);
				return card;
			}).forEach(em::persist);
		});
	}

	private void discardAllCards() {
		server.ef.runInTransaction(em -> {
			List<Card> cards = em.createNamedQuery("getAllCards", Card.class).getResultList();
			List<DatabasePlayer> players = em.createNamedQuery("findAllPlayers", DatabasePlayer.class).getResultList();

			players.forEach(x -> x.getCards().clear());
			cards.forEach(em::remove);
		});
	}

	public UUID drawCardForPlayer(UUID playerUUID) {
		final UUID[] res = {null};
		server.ef.runInTransaction(em-> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			List<Card> cards = em.createNamedQuery("getAllCardsInDeck", Card.class).getResultList();
			List<DeckCard> deckCards = cards.stream().filter(x->(x instanceof DeckCard)).map(x->(DeckCard)x).toList();
			int randomId = new Random().nextInt(deckCards.size());

			DeckCard card = deckCards.get(randomId);
			card.setOwner(player);
			res[0] = card.getId();
		});
		return res[0];
	}

	public void duplicateCard(UUID cardID) {
		AtomicBoolean failed = new AtomicBoolean(false);
		server.ef.runInTransaction(em -> {
			Card card = em.find(Card.class, cardID);
			if (card == null) {
				failed.set(true);
				return;
			}

			DeckCard sourceCard = null;

			if (card instanceof DeckCard dc) {
				sourceCard = dc;
			} else if (card instanceof ShadowCard sc){
				sourceCard = sc.getMirroredCard();
			}

			ShadowCard sc = new ShadowCard();
			sc.setOwner(card.getOwner());
			sc.setMirroredCard(sourceCard);
			em.persist(sc);
		});

		if(failed.get()) {
			throw new IllegalArgumentException("Card duplication has failed!");
		}
	}
}