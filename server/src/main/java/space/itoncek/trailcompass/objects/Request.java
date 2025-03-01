package space.itoncek.trailcompass.objects;


import java.util.Optional;

import space.itoncek.trailcompass.objects.gamedata.Type;

/**
 * Describes a singular request
 */
public interface Request {
	/**
	 * @return ID of the request, must be unique inside your package & should be unique across packages
	 * @implNote We recommend using your class name as the ID, such as {@code space.itoncek.trailcompass.ExampleRequest}.
	 * If one card has multiple instances, you can append {@code .<variable>} to differentiate them.
	 */
	String getID();
	/**
	 * @return Name of the request
	 */
	String getName();

	/**
	 * @return Description of the request
	 */
	String getDescription();

	/**
	 * @return Type of the request
	 */
	Type getRequestType();
	/**
	 * Supplies "preview" information to the hider
	 * @return true if it is a hit, false otherwise, can be emtpy if request is not boolean
	 */
	default Optional<Boolean> predictBool() {
		return Optional.empty();
	}
}
