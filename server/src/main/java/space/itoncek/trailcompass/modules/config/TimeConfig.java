package space.itoncek.trailcompass.modules.config;

import lombok.Getter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class TimeConfig implements Serializable {
    ZonedDateTime startTime;
    List<RestPeriod> restPeriods;
    ZonedDateTime endTime;
}
