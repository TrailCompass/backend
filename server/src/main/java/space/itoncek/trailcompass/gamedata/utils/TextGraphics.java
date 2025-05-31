package space.itoncek.trailcompass.gamedata.utils;

/*
 *
 * ████████╗██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗██████╗  █████╗ ███████╗███████╗
 * ╚══██╔══╝██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██╔══██╗██╔════╝██╔════╝
 *    ██║   ██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║██████╔╝███████║███████╗███████╗
 *    ██║   ██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██╔══██║╚════██║╚════██║
 *    ██║   ██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ██║  ██║███████║███████║
 *    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝
 *
 *                                    Copyright (c) 2025.
 */

import space.itoncek.trailcompass.server.BuildMeta;

import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
			                     Version: %s, built at: %s                                            \s
		\t
		\t""".formatted(BuildMeta.APP_VERSION, ZonedDateTime.ofInstant(Instant.ofEpochMilli(BuildMeta.BUILD_TIME), ZoneId.of("Z")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_DATE_TIME));
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

	public static boolean isDebuggerPresent() {
		// Get ahold of the Java Runtime Environment (JRE) management interface
		RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();

		// Get the command line arguments that we were originally passed in
		List<String> args = runtime.getInputArguments();

		// Check if the Java Debug Wire Protocol (JDWP) agent is used.
		// One of the items might contain something like "-agentlib:jdwp=transport=dt_socket,address=9009,server=y,suspend=n"
		// We're looking for the string "jdwp".

		return args.toString().contains("jdwp");
	}
}
