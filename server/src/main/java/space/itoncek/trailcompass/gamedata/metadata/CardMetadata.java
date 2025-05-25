package space.itoncek.trailcompass.gamedata.metadata;

import space.itoncek.trailcompass.objects.CardClass;

public record CardMetadata(CardClass cardClass, String title, String description, String casting_cost
) {
}
