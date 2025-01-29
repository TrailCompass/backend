package space.itoncek.trailcompass.objects;

/**
 * Represents the condition of a card, if it can be cast right now.
 * There are a few cases, which this object can be representing:
 *
 * <h4>Can be cast, verified</h4>
 * {@link #canCast()} should return true
 * {@link #checked()} should return true
 *
 * <h4>Cannot be cast, verified</h4>
 * {@link #canCast()} should return false
 * {@link #checked()} should return true
 *
 * <h4>Unable to determine castability</h4>
 * {@link #canCast()} should return true
 * {@link #checked()} should return false
 *
 * @param canCast true if the player should be able to cast this card
 * @param checked true if the condition was successfully checked
 * @param condition Text description
 */
public record Usage(boolean canCast, boolean checked, String condition) {

}
