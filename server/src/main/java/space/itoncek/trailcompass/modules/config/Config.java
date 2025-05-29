package space.itoncek.trailcompass.modules.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Config implements Serializable {
	RuleConfig rules;
	TimeConfig time;
}
