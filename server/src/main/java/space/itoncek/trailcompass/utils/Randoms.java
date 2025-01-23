package space.itoncek.trailcompass.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class Randoms {

	/**
	 * Generates new random string
	 *
	 * @param char_num new string's length
	 * @param letters  should the new string contain letters
	 * @param numbers  should the new string contain numbers
	 *
	 * @return random string
	 */
	public static String generateRandomString(int char_num, boolean letters, boolean numbers) {
		return RandomStringUtils.random(char_num, letters, numbers);
	}
}
