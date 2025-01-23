package space.itoncek.trailcompass.packages;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageMetadata {
	private final JSONObject data;

	public PackageMetadata(File jarFile) {
		try (JarFile jar = new JarFile(jarFile)) {
			JarEntry entry = jar.getJarEntry("package.json");
			if (entry == null) throw new IllegalArgumentException("package.json not found in " + jarFile.getName());

			try (Scanner sc = new Scanner(jar.getInputStream(entry))) {
				StringJoiner sb = new StringJoiner("\n");
				while (sc.hasNextLine()) sb.add(sc.nextLine());
				data = new JSONObject(sb.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String mainClass() {
		return data.getString("main");
	}

	public String packageName() {
		return data.getString("name");
	}
}
