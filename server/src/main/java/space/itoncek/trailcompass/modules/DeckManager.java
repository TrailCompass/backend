package space.itoncek.trailcompass.modules;

/*
 *
 * ████████╗██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗██████╗  █████╗ ███████╗███████╗
 * ╚══██╔══╝██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██╔══██╗██╔════╝██╔════╝
 *    ██║   ██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║██████╔╝███████║███████╗███████╗
 *    ██║   ██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██╔══██║╚════██║╚════██║
 *    ██║   ██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ██║  ██║███████║███████║
 *    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝
 *
 *                                    Copyright (c) 2025.
 */

import jakarta.persistence.EntityManager;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.CardCastRequirement;
import space.itoncek.trailcompass.commons.objects.CardClass;
import space.itoncek.trailcompass.commons.objects.CardType;
import space.itoncek.trailcompass.commons.utils.BackendException;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.PlayedCurse;
import space.itoncek.trailcompass.database.cards.Card;
import space.itoncek.trailcompass.database.cards.DeckCard;
import space.itoncek.trailcompass.database.cards.ShadowCard;
import space.itoncek.trailcompass.gamedata.HomeGameDeck;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
		server.ef.runInTransaction(em -> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			List<Card> cards = em.createNamedQuery("getAllCardsInDeck", Card.class).getResultList();
			List<DeckCard> deckCards = cards.stream().filter(x -> (x instanceof DeckCard)).map(x -> (DeckCard) x).toList();
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
			} else if (card instanceof ShadowCard sc) {
				sourceCard = sc.getMirroredCard();
			}

			ShadowCard sc = new ShadowCard();
			sc.setOwner(card.getOwner());
			sc.setMirroredCard(sourceCard);
			em.persist(sc);
		});

		if (failed.get()) {
			throw new IllegalArgumentException("Card duplication has failed!");
		}
	}

	public List<DeckCard> listMyRealCards(UUID playerUUID) {
		AtomicBoolean failed = new AtomicBoolean(false);
		var ref = new Object() {
			List<DeckCard> cards = null;
		};
		server.ef.runInTransaction(em -> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			if (player == null) {
				failed.set(true);
				return;
			}

			ref.cards = player.getCards();
		});
		if (failed.get()) {
			throw new IllegalArgumentException("Card duplication has failed!");
		}
		return ref.cards;
	}

	public List<ShadowCard> listMyShadowCards(UUID playerUUID) {
		AtomicBoolean failed = new AtomicBoolean(false);
		var ref = new Object() {
			List<ShadowCard> cards = null;
		};
		server.ef.runInTransaction(em -> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			if (player == null) {
				failed.set(true);
				return;
			}

			ref.cards = player.getShadowCards();
		});
		if (failed.get()) {
			throw new IllegalArgumentException("Card duplication has failed!");
		}
		return ref.cards;
	}

	public List<Card> listAllMyCards(UUID playerUUID) {
		AtomicBoolean failed = new AtomicBoolean(false);
		final List<Card> cards = new ArrayList<>();
		server.ef.runInTransaction(em -> {
			DatabasePlayer player = em.find(DatabasePlayer.class, playerUUID);
			if (player == null) {
				failed.set(true);
				return;
			}

			cards.addAll(player.getCards());
			cards.addAll(player.getShadowCards());
		});
		if (failed.get()) {
			throw new IllegalArgumentException("Card duplication has failed!");
		}
		return cards;
	}

	public CardCastRequirement getCardCastRequirement(UUID cardUUID) throws BackendException {
		AtomicBoolean failed = new AtomicBoolean(false);
		AtomicReference<CardType> cardType = new AtomicReference<>();
		server.ef.runInTransaction(em -> {
			Card card = em.find(Card.class, cardUUID);
			if (card == null) {
				failed.set(true);
				return;
			}
			CardType ct = null;
			if(card instanceof DeckCard dc) {
				ct = dc.getType();
			} else if (card instanceof ShadowCard sc) {
				ct = sc.getMirroredCard().getType();
			}

			cardType.set(ct);
		});

		if (failed.get()) throw new BackendException("Unable to fetch the card!");
		return cardType.get().requirement;
	}

	public void CastVoidCard(UUID cardId) throws BackendException {
		AtomicReference<String> exception = new AtomicReference<>(null);
		server.ef.runInTransaction(em -> {

			try {
				Card card = em.find(Card.class, cardId);
				if (card == null) {
					exception.set("Unable to find that card in the database!");
					return;
				}

				if (card.getOwner() == null) {
					exception.set("Nobody owns that card!");
					return;
				}

				boolean shadowCard = false;
				CardType cardType;
				if (card instanceof ShadowCard sc) {
					cardType = sc.getMirroredCard().getType();
					shadowCard = true;
				} else if (card instanceof DeckCard dc) {
					cardType = dc.getType();
				} else {
					cardType = null;
				}

				if (cardType == null) {
					exception.set("Unknown card type!");
					return;
				} else if (cardType.requirement != CardCastRequirement.Nothing) {
					exception.set("Card does not have a void cast requirement!");
					return;
				}

				if (cardType.cardClass.equals(CardClass.Curse)) {
					castCurse(em, cardType);

					if (shadowCard) em.remove(card);
					else card.setOwner(null);
				}

			} catch (IOException e) {
				exception.set(e.toString());
			}
		});

		if (exception.get() != null) {
			throw new BackendException(exception.get());
		}
	}

	public void CastWithOtherCard(UUID cardId, UUID otherCardId) throws BackendException {
		AtomicReference<String> exception = new AtomicReference<>(null);
		server.ef.runInTransaction(em -> {
			try {
				Card card = em.find(Card.class, cardId);
				Card otherCard = em.find(Card.class, otherCardId);
				if (card == null || otherCard == null) {
					exception.set("Unable to find that card in the database!");
					return;
				}

				if (card.getOwner() == null || otherCard.getOwner() == null) {
					exception.set("Nobody owns that card!");
					return;
				}

                CardType cardType;
				if (card instanceof ShadowCard sc) {
					cardType = sc.getMirroredCard().getType();
                } else if (card instanceof DeckCard dc) {
					cardType = dc.getType();
				} else {
					cardType = null;
				}

				if (cardType == null) {
					exception.set("Unknown card type!");
					return;
				} else if (cardType.requirement != CardCastRequirement.OtherCard) {
					exception.set("Card does not have a \"OtherCard\" cast requirement!");
					return;
				}

				switch (cardType) {
					case Discard1 -> {
						removeCard(em, otherCard);
						removeCard(em, card);

						drawCardForPlayer(card.getOwner().getId());
						drawCardForPlayer(card.getOwner().getId());
					}
					case Duplicate -> {
						duplicateCard(otherCardId);
						removeCard(em, card);
					}
					case Curse_OverflowingChalice, Curse_RightTurn -> {
						castCurse(em,cardType);
						removeCard(em, otherCard);
						removeCard(em, card);
					}
                }
			} catch (IOException e) {
				exception.set(e.toString());
			}
		});

		if (exception.get() != null) {
			throw new BackendException(exception.get());
		}
	}
	public void CastWithTwoOtherCards(UUID cardId, UUID other1, UUID other2) throws BackendException {
		AtomicReference<String> exception = new AtomicReference<>(null);
		server.ef.runInTransaction(em -> {
			try {
				Card card = em.find(Card.class, cardId);
				Card oCard1 = em.find(Card.class, other1);
				Card oCard2 = em.find(Card.class, other1);
				if (card == null || oCard1 == null || oCard2 == null) {
					exception.set("Unable to find that card in the database!");
					return;
				}

				if(oCard1.getId() == oCard2.getId()) {
					exception.set("You need to select two different cards");
					return;
				}

				if (card.getOwner() == null || oCard1.getOwner() == null || oCard2.getOwner() == null) {
					exception.set("Nobody owns that card!");
					return;
				}

                CardType cardType;
				if (card instanceof ShadowCard sc) {
					cardType = sc.getMirroredCard().getType();
                } else if (card instanceof DeckCard dc) {
					cardType = dc.getType();
				} else {
					cardType = null;
				}

				if (cardType == null) {
					exception.set("Unknown card type!");
					return;
				} else if (cardType.requirement != CardCastRequirement.TwoOtherCards) {
					exception.set("Card does not have a \"TwoOtherCards\" cast requirement!");
					return;
				}

				switch (cardType) {
					case Discard2 -> {
						removeCard(em, card);
						removeCard(em, oCard1);
						removeCard(em, oCard2);

						drawCardForPlayer(card.getOwner().getId());
						drawCardForPlayer(card.getOwner().getId());
						drawCardForPlayer(card.getOwner().getId());
					}

					case Curse_HiddenHangman, Curse_JammmedDoor, Curse_Urbex, Curse_EggPartner -> {
						castCurse(em, cardType);
						removeCard(em, card);
						removeCard(em, oCard1);
						removeCard(em, oCard2);
					}
                }
			} catch (IOException e) {
				exception.set(e.toString());
			}
		});

		if (exception.get() != null) {
			throw new BackendException(exception.get());
		}
	}

	private static void removeCard(EntityManager em, Card card) {
		if (card instanceof ShadowCard sc) {
			em.remove(sc);
		} else if (card instanceof DeckCard dc) {
			dc.setOwner(null);
		}
	}

	private void castCurse(EntityManager em, CardType cardType) throws IOException {
		PlayedCurse pc = new PlayedCurse();
		pc.setType(cardType);
		pc.setCleared(false);
		pc.setStart(ZonedDateTime.now());
		pc.setCaster(em.find(DatabasePlayer.class, server.config.getConfig().getRules().getHider()));

		em.persist(pc);
	}
}