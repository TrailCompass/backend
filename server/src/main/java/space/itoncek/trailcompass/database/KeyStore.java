package space.itoncek.trailcompass.database;

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

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

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
		DECK_DEALT("false");

		public final String defaults;

		KeystoreKeys(String defaults) {
			this.defaults = defaults;
		}
	}
}
