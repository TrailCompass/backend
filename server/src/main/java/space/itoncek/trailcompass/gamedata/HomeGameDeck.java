package space.itoncek.trailcompass.gamedata;

import space.itoncek.trailcompass.database.DatabaseCard;
import space.itoncek.trailcompass.objects.CardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class HomeGameDeck {
	public List<DatabaseCard> cards = new ArrayList<>();

	public HomeGameDeck() {
		cards.addAll(generateCurses());
		cards.addAll(generateCards(25, CardType.TimeBonusRed));
		cards.addAll(generateCards(15, CardType.TimeBonusOrange));
		cards.addAll(generateCards(10, CardType.TimeBonusYellow));
		cards.addAll(generateCards(3, CardType.TimeBonusGreen));
		cards.addAll(generateCards(2, CardType.TimeBonusBlue));
		cards.addAll(generateCards(4, CardType.Randomize));
		cards.addAll(generateCards(4, CardType.Veto));
		cards.addAll(generateCards(2, CardType.Duplicate));
		cards.addAll(generateCards(1, CardType.Move));
		cards.addAll(generateCards(4, CardType.Discard1));
		cards.addAll(generateCards(4, CardType.Discard2));
		cards.addAll(generateCards(2, CardType.Draw1Expand));

		Collections.shuffle(cards);
	}

	private List<DatabaseCard> generateCurses() {
		List<DatabaseCard> tempCards = new ArrayList<>();
		tempCards.add(generateCard(CardType.Curse_Zoologist));
		tempCards.add(generateCard(CardType.Curse_UnguidedTourist));
		tempCards.add(generateCard(CardType.Curse_EndlessTumble));
		tempCards.add(generateCard(CardType.Curse_HiddenHangman));
		tempCards.add(generateCard(CardType.Curse_OverflowingChalice));
		tempCards.add(generateCard(CardType.Curse_MediocreTravelAgent));
		tempCards.add(generateCard(CardType.Curse_LuxuryCard));
		tempCards.add(generateCard(CardType.Curse_UTurn));
		tempCards.add(generateCard(CardType.Curse_BridgeTroll));
		tempCards.add(generateCard(CardType.Curse_WaterWeight));
		tempCards.add(generateCard(CardType.Curse_JammmedDoor));
		tempCards.add(generateCard(CardType.Curse_Cairn));
		tempCards.add(generateCard(CardType.Curse_Urbex));
		tempCards.add(generateCard(CardType.Curse_ImpressionableConsumer));
		tempCards.add(generateCard(CardType.Curse_EggPartner));
		tempCards.add(generateCard(CardType.Curse_DistantCuisine));
		tempCards.add(generateCard(CardType.Curse_RightTurn));
		tempCards.add(generateCard(CardType.Curse_Labyrinth));
		tempCards.add(generateCard(CardType.Curse_BirdGuide));
		tempCards.add(generateCard(CardType.Curse_SpottyMemory));
		tempCards.add(generateCard(CardType.Curse_LemonPhylactery));
		tempCards.add(generateCard(CardType.Curse_DrainedBrain));
		tempCards.add(generateCard(CardType.Curse_RansomNote));
		tempCards.add(generateCard(CardType.Curse_GamblersFeet));
		return tempCards;
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
