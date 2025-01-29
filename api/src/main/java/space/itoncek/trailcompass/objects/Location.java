package space.itoncek.trailcompass.objects;

/**
 * Represents GPS coordinates
 * @param lat latitude
 * @param lon longitude
 * @param alt altitude
 */
public record Location(double lat, double lon, float alt) {
}
