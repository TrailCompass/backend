package space.itoncek.trailcompass.modules.config;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class RuleConfig implements Serializable {
    UUID hider;
    GameSize size;
    long movePeriodSeconds;
    boolean includeMoveCard;
    boolean playingWithPhysicalCards;
}
