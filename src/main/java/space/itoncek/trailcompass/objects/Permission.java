package space.itoncek.trailcompass.objects;

import java.util.ArrayList;

public enum Permission {
	ADMIN(0b100000),
	ADD_USERS(0b10000),
	MANAGE_GAMES(0b1000),
	CREATE_GAMES(0b100),
	VIEW_ALL_GAMES(0B10),
	PLAY(0B1);

	public final int bitmask;

	Permission(int bytemask) {
		this.bitmask = bytemask;
	}

	public static int convertToInt(ArrayList<Permission> permissions) {
		int out = 0;
		for (Permission permission : permissions) {
			out |= permission.bitmask;
		}
		return out;
	}
}
