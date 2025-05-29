package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.database.DatabaseCard;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.gamedata.HomeGameDeck;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
				DatabaseCard card = new DatabaseCard();
				card.setType(x.getType());
				card.setOwner(null);
				return card;
			}).forEach(em::persist);
		});
	}

	private void discardAllCards() {
		server.ef.runInTransaction(em -> {
			List<DatabaseCard> cards = em.createNamedQuery("getAllCards", DatabaseCard.class).getResultList();
			List<DatabasePlayer> players = em.createNamedQuery("findAllPlayers", DatabasePlayer.class).getResultList();

			players.forEach(x -> x.getCards().clear());
			cards.forEach(em::remove);
		});
	}

	public UUID drawCardForPlayer(UUID playerUUID) {
		final UUID[] res = {null};
		server.ef.runInTransaction(em-> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			List<DatabaseCard> cards = em.createNamedQuery("getAllCardsInDeck", DatabaseCard.class).getResultList();
			int randomId = new Random().nextInt(cards.size());

			DatabaseCard card = cards.get(randomId);
			card.setOwner(player);
			res[0] = card.getId();
		});
		return res[0];
	}
}