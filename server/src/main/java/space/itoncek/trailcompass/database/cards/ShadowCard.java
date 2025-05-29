package space.itoncek.trailcompass.database.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ShadowCard extends Card {
    @ManyToOne(targetEntity = DeckCard.class)
    DeckCard mirroredCard;
}
