package space.itoncek.trailcompass.objects;

public enum CardType {
	TimeBonusRed(CardClass.Powerup),
	TimeBonusOrange(CardClass.Powerup),
	TimeBonusYellow(CardClass.Powerup),
	TimeBonusGreen(CardClass.Powerup),
	TimeBonusBlue(CardClass.Powerup),
	Randomize(CardClass.Powerup),
	Veto(CardClass.Powerup),
	Duplicate(CardClass.Powerup),
	Move(CardClass.Powerup),
	Discard1(CardClass.Powerup),
	Discard2(CardClass.Powerup),
	Draw1Expand(CardClass.Powerup),
	//Curses
	Curse_Zoologist(CardClass.Curse),
	Curse_UnguidedTourist(CardClass.Curse),
	Curse_EndlessTumble(CardClass.Curse),
	Curse_HiddenHangman(CardClass.Curse),
	Curse_OverflowingChalice(CardClass.Curse),
	Curse_MediocreTravelAgent(CardClass.Curse),
	Curse_LuxuryCard(CardClass.Curse),
	Curse_UTurn(CardClass.Curse),
	Curse_BridgeTroll(CardClass.Curse),
	Curse_WaterWeight(CardClass.Curse),
	Curse_JammmedDoor(CardClass.Curse),
	Curse_Cairn(CardClass.Curse),
	Curse_Urbex(CardClass.Curse),
	Curse_ImpressionableConsumer(CardClass.Curse),
	Curse_EggPartner(CardClass.Curse),
	Curse_DistantCuisine(CardClass.Curse),
	Curse_RightTurn(CardClass.Curse),
	Curse_Labyrinth(CardClass.Curse),
	Curse_BirdGuide(CardClass.Curse),
	Curse_SpottyMemory(CardClass.Curse),
	Curse_LemonPhylactery(CardClass.Curse),
	Curse_DrainedBrain(CardClass.Curse),
	Curse_RansomNote(CardClass.Curse),
	Curse_GamblersFeet(CardClass.Curse);

	public final CardClass cardClass;

	CardType(CardClass cardClass) {
		this.cardClass = cardClass;
	}
}
