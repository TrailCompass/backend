package space.itoncek.trailcompass.modules.config;

import lombok.Getter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
public class RestPeriod implements Serializable {
    ZonedDateTime start;
    ZonedDateTime end;

    public boolean isInside(ZonedDateTime dateTime) {
        return start.isBefore(dateTime) && end.isAfter(dateTime);
    }
}
