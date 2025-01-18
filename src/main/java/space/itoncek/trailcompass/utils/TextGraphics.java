package space.itoncek.trailcompass.utils;

public class TextGraphics {
	/**
	 * Generates login box alert.
	 * @param username username
	 * @param password password
	 *
	 * @return boxed alert
	 */
	public static String generateLoginBox(String username, String password) {
		String alert = "Admin account has been created!";
		String alert2 = "This message will not be shown again!";
		String user = "Username: " + username;
		String pw = "Password: " + password;

		int length = Math.max(alert2.length(),Math.max(user.length(),pw.length()))+4;
		return "\n" + "╔" + "═".repeat(length) + "╗" + "\n" +
			   "║" + "  " + alert + " ".repeat(length - alert.length() - 2) + "║\n" +
			   "║" + "  " + alert2 + " ".repeat(length - alert2.length() - 2) + "║\n" +
			   "║" + " ".repeat(length) + "║\n" +
			   "║" + "  " + user + " ".repeat(length - user.length() - 2) + "║\n" +
			   "║" + "  " + pw + " ".repeat(length - pw.length() - 2) + "║\n" +
			   "╚" + "═".repeat(length) + "╝" + "\n";
	}
}
