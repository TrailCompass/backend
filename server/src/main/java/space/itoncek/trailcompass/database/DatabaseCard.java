package space.itoncek.trailcompass.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.objects.CardType;

import java.util.UUID;

@Getter
@Setter
@Entity
@NamedQuery(name = "getAllCards", query = "SELECT c FROM DatabaseCard c")
@NamedQuery(name = "getAllCardsInDeck", query = "SELECT c FROM DatabaseCard c WHERE c.owner IS NULL")
public class DatabaseCard {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID id;
	@Enumerated(value = EnumType.STRING)
	CardType type;
	@ManyToOne(targetEntity = DatabasePlayer.class,fetch = FetchType.LAZY)
	DatabasePlayer owner;
}
