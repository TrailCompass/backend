package space.itoncek.trailcompass;

public record Return(Type type, String content) {
	public enum Type{
		IMAGE,
		STRING,
		BOOL
	}
}
