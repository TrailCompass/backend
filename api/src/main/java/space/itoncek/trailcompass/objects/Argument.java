package space.itoncek.trailcompass.objects;

public record Argument(Type type, String name) {
	public enum Type {
		STRING,
		BOOL,
		BIGINT,
		BIGDECIMAL,
		DOUBLE,
		FLOAT,
		NUMBER,
		INT,
		JSONOBJECT,
		JSONARRAY,
		LONG
	}
}
