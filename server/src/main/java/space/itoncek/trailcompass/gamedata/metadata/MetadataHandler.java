package space.itoncek.trailcompass.gamedata.metadata;

import space.itoncek.trailcompass.objects.CardType;

public class MetadataHandler {
	public CardMetadata parseMetadata(CardType type) {
		return switch (type.cardClass) {
			case Curse -> CurseMetadataHandler.parseMetadata(type);
			case Powerup -> PowerupMetadataHandler.parseMetadata(type);
		};
	}
}
