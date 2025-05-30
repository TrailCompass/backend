package space.itoncek.trailcompass.gamedata.metadata;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.CardClass;
import space.itoncek.trailcompass.commons.objects.CardType;
import space.itoncek.trailcompass.modules.config.GameSize;

import java.io.IOException;

public class CurseMetadataHandler {
	private final TrailServer server;

	public CurseMetadataHandler(TrailServer server) {
		this.server = server;
	}

	private static CardMetadata parseMetadata(CardType type) {
		// Regex to parse different values depending on game size
		// /\[([^\]]+),([^\]]+),([^\]]+)\]/gm

		return switch (type) {
			case Curse_Zoologist ->
					new CardMetadata(CardClass.Curse, "The Zoologist", "Take a photo of a wild fish, bird, mammal, reptile, amphibian or bug. The seeker(s) must take a picture of a wild animal in the same category before asking another question.", "A photo of an animal");
			case Curse_UnguidedTourist ->
					new CardMetadata(CardClass.Curse, "The Unguided Tourist", "Send the seeker(s) an unzoomed google Street View image from a street within 150 meters of where they are now. The shot has to be parallel to the horizon and include at least one human-built structure other than a road. Without using the internet for research, they must find what you sent them in real life before they can use transportation or ask another question. They must send a picture the hiders for verification.", "Seker(s) must be outside");
			case Curse_EndlessTumble ->
					new CardMetadata(CardClass.Curse, "The Endless Tumble", "Seekers Must roll a die at least 30 meters and have it land on a 5 or a 6 before they can ask another question. The die must roll the full distance, unaided, using only the momentum from the initial throw and gravity to travel the 30 meters. If the seekers accidentally hit someone with a die you are awarded a [10 min,20 min,30 min] bonus", "Roll a die. If its 5 or 6 this card has no effect.");
			case Curse_HiddenHangman ->
					new CardMetadata(CardClass.Curse, "The Hidden Hangman", "Before asking another question or boarding another form of transportation, seeker(s) must be the hider(s) in game of hangman.", "Discard 2 cards");
			case Curse_OverflowingChalice ->
					new CardMetadata(CardClass.Curse, "The Overflowing Chalice", "For the next three questions, you may draw (not keep) an additional card when drawing from the hider deck", "Discard a card");
			case Curse_MediocreTravelAgent ->
					new CardMetadata(CardClass.Curse, "The Mediocre Travel Agent", "Choose any publicly-accessible place within [400 meters,400 meters,500 meters] of the seeker(s) current location. They cannot currently be on transit. They must go there, and spend at least [5 minutes,5 minutes,10 minutes] there, before asking another question. They must send you at least three photos of them enjoying their vacation, and procure an object to bring you as souvenir. If this souvenir is lost before they can get to you, you are awarded and extra [30 minutes,45 minutes,60 minutes].", "");
			case Curse_LuxuryCard ->
					new CardMetadata(CardClass.Curse, "The Luxury Car", "Take a photo of a car. The seekers must take a photo of a more expensive car before asking another question.", "A photo of a car");
			case Curse_UTurn ->
					new CardMetadata(CardClass.Curse, "The U-Turn", "Seeker(s) must disembark their current mode of transportation at the next station (as long as that station is served by another form of transit in the next [30 minutes,30 minutes,60 minutes]", "Seekers must be heading the wrong way. (Their next station is further from you then they are.)");
			case Curse_BridgeTroll ->
					new CardMetadata(CardClass.Curse, "The Bridge Troll", "The seekers must ask their next question from under a bridge", "The seekers must ask their next question from under a bridge");
			case Curse_WaterWeight ->
					new CardMetadata(CardClass.Curse, "Water Weight", "Seeker(s) must acquire and carry at least 2 liters of liquid per seeker for the rest of your run. They cannot ask another question until they have acquired the liquid. The water may be distributed between seeker as they see fit. If the liquid is lost or abandoned at any point the hider is awarded a [30 minutes, 30 minutes,60 minutes] bonus", "Seekers must be within 300 meters of a body of water");
			case Curse_JammmedDoor ->
					new CardMetadata(CardClass.Curse, "The Jammed Door", "For the next [30 minutes,1 hour,3 hours], whenever the seeker(s) want to pass through a doorway into a building, business, train, or other vehicle they must first roll 2 dice. If they do not roll a 7 or higher they cannot enter that space (including through other doorways) any given doorway can be reattempted after [5 minutes,10 minutes,15 minutes].", "Discard 2 cards");
			case Curse_Cairn ->
					new CardMetadata(CardClass.Curse, "The Cairn", "You have one attempt to stack as many rocks on top of each other as you can in a freestanding tower. Each rock may only touch one other rock. Once you have added a rock to the tower it may not be removed. Before adding another rock, the tower must stand for at least 5 seconds. If at any point any rock other then the base rock touches the ground, your tower has fallen. Once your tower falls tell the seekers how many rocks high your tower was when it last stood for five seconds. The seekers must then construct a rock tower of the same number of rucks, under the same parameters before asking another question. If their tower falls they must restart. The rocks must be found in nature and both teams must disperse the rocks after building.", "Build a rock tower");
			case Curse_Urbex ->
					new CardMetadata(CardClass.Curse, "The Urban Explorer", "For the rest of the run seekers cannot ask question when they are on transit or in a train station", "Discard 2 cards");
			case Curse_ImpressionableConsumer ->
					new CardMetadata(CardClass.Curse, "The Impressionable Consumer", "Seekers must enter and gain admission (if applicable) to a location or buy a product that they saw an advertisement for before asking another question. This advertisement musts be found out in the world and must be at least 30 meters from the product or location itself.", "The seekers next question is free");
			case Curse_EggPartner ->
					new CardMetadata(CardClass.Curse, "The Egg Partner", "Seeker(s) must acquire an egg before asking another question. This egg is now treated as an official team member of the seekers. If any team members are abandoned or killed (defined as crack in the eggs case) before the end of your run you are awarded an extra [30 minutes,45 minutes,60 minutes]. This course cannot be played during the endgame.", "Discard two cards");
			case Curse_DistantCuisine ->
					new CardMetadata(CardClass.Curse, "The Distant Cuisine", "Find a restaurant within your zone that explicitly serves food from a specific foreign country. The seekers must visit a restaurant serving food from a country that is equal or great distance away before asking another question", "You must be at the restaurant");
			case Curse_RightTurn ->
					new CardMetadata(CardClass.Curse, "The Right Turn", "For the next [20 minutes,40 minutes,60 minutes] the seekers can only turn right at any street intersection. If at any point they find themselves in dead end where they cannot continue forward or turn right for another 300 meters they must do a full 180. A right turn is defined as a road at any angle that veers to the right of the seekers", "Discard a card");
			case Curse_Labyrinth ->
					new CardMetadata(CardClass.Curse, "The Labyrinth", "Spend up to [10 minutes,20 minutes,30 minutes] minutes drawing a solvable maze and send a photo of it to the seekers. You cannot use the internet to research maze designs. The seekers musts solve the maze before asking another question.", "Draw a maze");
			case Curse_BirdGuide ->
					new CardMetadata(CardClass.Curse, "The Bird Guide", "You have one chance to film a bird for as long as possible. Up to [5 minutes,10 minutes,15 minutes] straight, if at any point the bird leaves the frame your timer is stopped. The seekers must then film a bird for the same amount of time or longer", "Film a bird");
			case Curse_SpottyMemory ->
					new CardMetadata(CardClass.Curse, "Spotty Memory", "For the rest of the run, one random category of questions will be disabled at all times. After this curse is played seeker(s) must roll a die to determine the category of questions to be disabled. The catergy remain disabled until the next question is asked at which point a die is rolled again to choose an e category. The same category can be disabled multiple times in a row", "Discard a time bonus card");
			case Curse_LemonPhylactery ->
					new CardMetadata(CardClass.Curse, "The Lemon Phylactery", "Before asking another question the seeker(s) must each find a lemon and affix it to their outermost layer of their clothes or skin. If at any point one of these lemons is no longer touching a seeker you are awarded [30 minutes,45 minutes,60 minutes]. This curse cannot be played during the endgame.", "Discard a powerup card");
			case Curse_DrainedBrain ->
					new CardMetadata(CardClass.Curse, "The Drained Brain", "Choose three questions in different categories. The seekers cannot ask those questions for the rest of the run.", "Discard your hand");
			case Curse_RansomNote ->
					new CardMetadata(CardClass.Curse, "The Ransom Note", "The next question that the seekers ask must be composed of words and letters cut out of any printed material. The question must be coherent and include at least 5 words.", "Spell out \"Ransom Note\" as a ransom note (without using this card)");
			case Curse_GamblersFeet ->
					new CardMetadata(CardClass.Curse, "The Gambler's Feet", "For the next [20 minutes,40 minutes,60 minutes] minutes seekers must roll a die before they take any steps in any direction, they may take that many steps before rolling again", "Roll a die if its even number this curse has no effect");
			case TimeBonusRed, TimeBonusOrange, TimeBonusYellow, TimeBonusGreen, TimeBonusBlue, Randomize, Veto,
				 Duplicate, Move, Discard1, Discard2, Draw1Expand -> null;
		};
	}

    public CardMetadata parseMetadataContextDependant(CardType cardType) throws IOException {
		GameSize size = server.config.getConfig().getRules().getSize();

		if (cardType.cardClass != CardClass.Curse) {
			throw new IllegalArgumentException("Card is not a curse!");
		}

		CardMetadata cardMetadata = parseMetadata(cardType);
		if (cardMetadata == null) return null;
		String s = cardMetadata.description().replaceAll("\\[([^]]+),([^]]+),([^]]+)]", "$" + size.value);
		return new CardMetadata(cardMetadata.cardClass(), cardMetadata.title(), s, cardMetadata.casting_cost());
	}
}