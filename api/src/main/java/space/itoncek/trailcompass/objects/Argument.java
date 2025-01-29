package space.itoncek.trailcompass.objects;

public record Argument(Type type, String name, AutofillFlag autofillFlag) {
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

	public enum AutofillFlag {
		GPS_LATTITUDE,
		GPS_LONGITUDE,
		GPS_ALTITUDE,
		NONE
	}
}
