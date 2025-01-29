package space.itoncek.trailcompass.packages;

import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.Card;
import space.itoncek.trailcompass.Package;
import space.itoncek.trailcompass.TrailServer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class PackageLoader {
	private boolean isReady = false;
	private final ArrayList<Package> pkgs = new ArrayList<>();
	private final TrailServer server;

	public PackageLoader(TrailServer server) {
		this.server = server;
	}

	public void loadPlugins(File dir) throws Exception {
		if(!dir.isDirectory()) {
			dir.delete();
			dir.mkdirs();
		}
		File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));

		if (jars == null) return;

		for (File jar : jars) {
			URLClassLoader classLoader = new URLClassLoader(
					new URL[]{jar.toURI().toURL()},
					this.getClass().getClassLoader()
			);
			PackageMetadata meta = new PackageMetadata(jar);

			// Load the pkg main class name from a pkg.yml file inside the JAR
			String mainClassName = meta.mainClass();
			Class<?> clazz = classLoader.loadClass(mainClassName);

			Object pluginInstance = clazz.getDeclaredConstructor().newInstance();
			if (pluginInstance instanceof Package pkg) {
				pkgs.add(pkg);
				pkg.onLoad(LoggerFactory.getLogger(meta.packageName()));
			}

			classLoader.close();
			isReady =true;
		}
	}

	public void loadPlugins() {
		for (Package pkg : pkgs) {
			pkg.onEnable();
		}
	}

	public ArrayList<Card> getAllCards() {
		ArrayList<Card> cards = new ArrayList<>();
		for (Package pkg : pkgs) {
			cards.addAll(pkg.getCards());
		}
		return cards;
	}

	public void unloadPlugins() {
		for (Package pkg : pkgs) {
			pkg.onDisable();
		}
		pkgs.clear();
	}

	public boolean isHealthy() {
		return isReady;
	}
}
