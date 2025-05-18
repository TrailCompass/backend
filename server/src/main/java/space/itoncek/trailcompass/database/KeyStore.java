package space.itoncek.trailcompass.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import space.itoncek.trailcompass.commons.objects.GameState;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Entity
public class KeyStore {
	@Id
	@Enumerated(EnumType.STRING)
	@Basic(fetch = FetchType.LAZY)
	KeystoreKeys kkey;
	@Basic(fetch = FetchType.LAZY)
	String kvalue;

	public static KeyStore generateKeystore(KeystoreKeys key) {
		KeyStore ks = new KeyStore();
		ks.kkey = key;
		ks.kvalue = key.defaults;
		return ks;
	}

	public JSONObject toJSON() {
		return new JSONObject()
				.put("key", kkey)
				.put("value", kvalue);
	}

	public enum KeystoreKeys {
		GAME_STATE(GameState.SETUP.name()),
		DECK_DEALT("false"),
		HIDER(""),
		START_TIME(ZonedDateTime.now().plusMinutes(10).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

		public final String defaults;

		KeystoreKeys(String defaults) {
			this.defaults = defaults;
		}
	}
}
