package space.itoncek.trailcompass.database.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.objects.CardType;

@Getter
@Setter
@Entity
public class DeckCard extends Card {
	@Enumerated(value = EnumType.STRING)
	CardType type;
}
