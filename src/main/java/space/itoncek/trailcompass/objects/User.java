package space.itoncek.trailcompass.objects;

public record User(int id, String name, int permissions) {
	public boolean hasPermission(Permission permission) {
		int result = permissions & permission.bitmask;
		return result != 0;
	}
}
