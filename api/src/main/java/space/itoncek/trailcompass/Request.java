package space.itoncek.trailcompass;

import space.itoncek.trailcompass.objects.Type;

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

	Type getRequestType();
	/**
	 * Supplies "preview" information to the hider
	 * @return true if it is a hit, false otherwise, can be emtpy if request is not boolean
	 */
	default Optional<Boolean> predictBool() {
		return Optional.empty();
	}
}
