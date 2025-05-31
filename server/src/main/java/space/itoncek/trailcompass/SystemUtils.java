package space.itoncek.trailcompass;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemUtils {
	public static final String stringVersion = BuildMeta.APP_VERSION;
	public static final double doubleVersion = stringVersionDouble();

	private static double stringVersionDouble() {
		final String regex = "v([0-9]+)\\.([0-9]+)\\.([0-9]+)";

		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(stringVersion);

		if (!matcher.find()) {
			return -1;
		}

		int major = Integer.parseInt(matcher.group(1));
		int minor = Integer.parseInt(matcher.group(2));
		int patch = Integer.parseInt(matcher.group(3));

		return (major * 1000) + minor + (patch / 1000d);
	}

}