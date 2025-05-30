package space.itoncek.trailcompass.database.curses;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
public class TimeLimitedPlayedCurse extends PlayedCurse {
	long durationSeconds;

	public ZonedDateTime getEnd() {
		return getStart().plusSeconds(durationSeconds);
	}
}
