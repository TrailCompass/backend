package space.itoncek.trailcompass.gamedata;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.database.DatabaseCard;
import space.itoncek.trailcompass.objects.CardClass;
import space.itoncek.trailcompass.objects.CardType;

import java.io.IOException;
import java.util.*;

public class HomeGameDeck {
	public final List<DatabaseCard> cards = new ArrayList<>();

	public HomeGameDeck(TrailServer server) {
		cards.addAll(generateCurses());
		cards.addAll(generateCards(25, CardType.TimeBonusRed));
		cards.addAll(generateCards(15, CardType.TimeBonusOrange));
		cards.addAll(generateCards(10, CardType.TimeBonusYellow));
		cards.addAll(generateCards(3, CardType.TimeBonusGreen));
		cards.addAll(generateCards(2, CardType.TimeBonusBlue));
		cards.addAll(generateCards(4, CardType.Randomize));
		cards.addAll(generateCards(4, CardType.Veto));
		cards.addAll(generateCards(2, CardType.Duplicate));
		cards.addAll(generateCards(4, CardType.Discard1));
		cards.addAll(generateCards(4, CardType.Discard2));
		cards.addAll(generateCards(2, CardType.Draw1Expand));

        try {
            if(server.config.getConfig().getRules().isIncludeMoveCard()) {
                cards.addAll(generateCards(1, CardType.Move));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config!",e);
        }

        Collections.shuffle(cards);
	}

	private List<DatabaseCard> generateCurses() {
        return Arrays.stream(CardType.values())
                .filter(x->x.cardClass.equals(CardClass.Curse))
                .map(this::generateCard)
                .toList();
	}

	private DatabaseCard generateCard(CardType type) {
		DatabaseCard card = new DatabaseCard();
		card.setId(UUID.randomUUID());
		card.setType(type);
		return card;
	}

	private List<DatabaseCard> generateCards(int count, CardType type) {
		List<DatabaseCard> tempCards = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			tempCards.add(generateCard(type));
		}
		return tempCards;
	}
}
