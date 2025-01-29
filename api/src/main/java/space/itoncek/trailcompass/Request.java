package space.itoncek.trailcompass;

import space.itoncek.trailcompass.objects.Image;

import java.util.Optional;

/**
 * Describes a singular request
 */
public interface Request {
	/**
	 * @return Name of the request
	 */
	String getName();

	/**
	 * @return Description of the request
	 */
	String getDescription();


	default Optional<Boolean> executeBool() {
		return Optional.empty();
	}
	default Optional<Image> executeImage() {
		return Optional.empty();
	}
	default Optional<String> executeNote() {
		return Optional.empty();
	}

	default Optional<Boolean> predictBool() {
		return Optional.empty();
	}
	default Optional<String> predictString() {
		return Optional.empty();
	}
}
