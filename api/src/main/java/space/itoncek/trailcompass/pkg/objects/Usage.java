package space.itoncek.trailcompass.pkg.objects;

/**
 * Represents the condition of a card, if it can be cast right now.
 * There are a few cases, which this object can be representing:
 * <p>
 * <b>Can be cast, verified</b>
 * <p>
 * {@link #canCast()} should return true
 * <p>
 * {@link #checked()} should return true
 * <p>
 * <b>Cannot be cast, verified</b>
 * <p>
 *  {@link #canCast()} should return false
 * <p>
 * {@link #checked()} should return true
 * <p>
 * <b>Unable to determine castability</b>
 * <p>
 * {@link #canCast()} should return true
 * <p>
 * {@link #checked()} should return false
 *
 * @param canCast true if the player should be able to cast this card
 * @param checked true if the condition was successfully checked
 * @param condition Text description
 */
public record Usage(boolean canCast, boolean checked, String condition) {

}
