package space.itoncek.trailcompass.modules.config;

public enum GameSize {
    Small(1),
    Medium(2),
    Large(3);
	public final int value;

	GameSize(int value) {
		this.value = value;
	}
}
