package space.itoncek.trailcompass.objects;

public interface Curse extends Card {
	String getNote();
	String getCastingCost();

	@Override
	default String getDescription() {
		return getNote() + "\n\nCasting cost:" + getCastingCost();
	}

	@Override
	default void useAtGameEnd() {
		// nop
	}
}
