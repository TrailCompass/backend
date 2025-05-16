package space.itoncek.trailcompass.gamedata.utils;

public class TextGraphics {
	/**
	 * Generates login box alert.
	 *
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

		int length = Math.max(alert2.length(), Math.max(user.length(), pw.length())) + 4;
		return "\n" + "╔" + "═".repeat(length) + "╗" + "\n" +
			   "║" + "  " + alert + " ".repeat(length - alert.length() - 2) + "║\n" +
			   "║" + "  " + alert2 + " ".repeat(length - alert2.length() - 2) + "║\n" +
			   "║" + " ".repeat(length) + "║\n" +
			   "║" + "  " + user + " ".repeat(length - user.length() - 2) + "║\n" +
			   "║" + "  " + pw + " ".repeat(length - pw.length() - 2) + "║\n" +
			   "╚" + "═".repeat(length) + "╝" + "\n";
	}

	public static String generateIntroMural() {
		return """
		\t
		\t
			████████╗██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗██████╗  █████╗ ███████╗███████╗
			╚══██╔══╝██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██╔══██╗██╔════╝██╔════╝
			   ██║   ██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║██████╔╝███████║███████╗███████╗
			   ██║   ██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██╔══██║╚════██║╚════██║
			   ██║   ██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ██║  ██║███████║███████║
			   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝
			                         Version: %s                                                          \s
		\t
		\t""".formatted(TextGraphics.class.getPackage().getImplementationVersion() == null? "vDEVELOPMENT":TextGraphics.class.getPackage().getImplementationVersion());
	}

	public static String generateDevWarningBox() {
		return """
			
			╔════════════════════════════════════════════════════════════════╗
			║ This server is running in DEVELOPMENT mode! This can cause     ║
			║ unexpected behaviour, exposes information about this instance  ║
			║ that shouldn't be public and is generally a bad practice, if   ║
			║ you do not know what you are doing! If you want to run in      ║
			║ production mode, just remove "dev=true" from the startup args. ║
			╚════════════════════════════════════════════════════════════════╝
			""";
	}
}
