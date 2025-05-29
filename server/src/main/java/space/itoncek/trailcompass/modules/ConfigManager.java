package space.itoncek.trailcompass.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import space.itoncek.trailcompass.SystemUtils;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.modules.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;

public class ConfigManager {
	private final File file;
	private final TrailServer server;

	public ConfigManager(TrailServer server) {
		this.server = server;
		this.file = new File("./data/config.json");

        if (!file.exists()) {
            throw new RuntimeException("No config supplied, exiting!");
        }
    }

	public Config getConfig() throws IOException {
		try (FileReader fr = new FileReader(file)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().setVersion(SystemUtils.doubleVersion).create();
			return gson.fromJson(fr, Config.class);
		}
	}
}
