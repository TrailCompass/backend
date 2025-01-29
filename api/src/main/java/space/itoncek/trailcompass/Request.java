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


	/**
	 * Executes request as boolean
	 * @return true if it is a hit, false otherwise, can be empty if request is not boolean
	 */
	default Optional<Boolean> executeBool() {
		return Optional.empty();
	}

	/**
	 * Executes request as image
	 * @return {@link Image} object, can be empty if request is not boolean
	 */
	default Optional<Image> executeImage() {
		return Optional.empty();
	}
	/**
	 * Executes request as note
	 * @return {@link String}, can be empty if request is not boolean
	 */
	default Optional<String> executeNote() {
		return Optional.empty();
	}

	/**
	 * Supplies "preview" information to the hider
	 * @return true if it is a hit, false otherwise, can be emtpy if request is not boolean
	 */
	default Optional<Boolean> predictBool() {
		return Optional.empty();
	}
}
