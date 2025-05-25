package space.itoncek.trailcompass.gamedata.metadata;

import space.itoncek.trailcompass.objects.CardClass;
import space.itoncek.trailcompass.objects.CardType;

public class PowerupMetadataHandler {
	protected static CardMetadata parseMetadata(CardType type) {
		return switch (type) {
			case TimeBonusRed ->
					new CardMetadata(CardClass.Powerup, "Time Bonus", "timebonus.red", "[2 minutes,3 minutes,5 minutes]");
			case TimeBonusOrange ->
					new CardMetadata(CardClass.Powerup, "Time Bonus", "timebonus.orange", "[4 minutes,6 minutes,10 minutes]");
			case TimeBonusYellow ->
					new CardMetadata(CardClass.Powerup, "Time Bonus", "timebonus.yellow", "[6 minutes,9 minutes,15 minutes]");
			case TimeBonusGreen ->
					new CardMetadata(CardClass.Powerup, "Time Bonus", "timebonus.green", "[8 minutes,12 minutes,20 minutes]");
			case TimeBonusBlue ->
					new CardMetadata(CardClass.Powerup, "Time Bonus", "timebonus.blue", "[12 minutes,18 minutes,30 minutes]");
			case Randomize -> new CardMetadata(CardClass.Powerup, "Randomize question", "randomize", "todo))");
			case Veto -> new CardMetadata(CardClass.Powerup, "Veto question", "veto", "todo))");
			case Duplicate -> new CardMetadata(CardClass.Powerup, "Duplicate card", "duplicate", "todo))");
			case Move -> new CardMetadata(CardClass.Powerup, "Move", "move", "todo))");
			case Discard1 -> new CardMetadata(CardClass.Powerup, "Discard 1 Draw 2", "discard1", "todo))");
			case Discard2 -> new CardMetadata(CardClass.Powerup, "Discard 2 Draw 3", "discard2", "todo))");
			case Draw1Expand -> new CardMetadata(CardClass.Powerup, "Draw 1 Expand", "draw1expand", "todo))");
			case Curse_Zoologist, Curse_UnguidedTourist, Curse_EndlessTumble, Curse_HiddenHangman,
				 Curse_OverflowingChalice, Curse_MediocreTravelAgent, Curse_LuxuryCard, Curse_UTurn, Curse_BridgeTroll,
				 Curse_WaterWeight, Curse_JammmedDoor, Curse_Cairn, Curse_Urbex, Curse_ImpressionableConsumer,
				 Curse_EggPartner, Curse_DistantCuisine, Curse_RightTurn, Curse_Labyrinth, Curse_BirdGuide,
				 Curse_SpottyMemory, Curse_LemonPhylactery, Curse_DrainedBrain, Curse_RansomNote, Curse_GamblersFeet ->
					CurseMetadataHandler.parseMetadata(type);
		};
	}
}