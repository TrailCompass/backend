package space.itoncek.trailcompass.database.curses;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.commons.objects.CardType;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class PlayedCurse {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID id;
	@Enumerated(value = EnumType.STRING)
	CardType type;
	ZonedDateTime start;
	boolean cleared;
}
