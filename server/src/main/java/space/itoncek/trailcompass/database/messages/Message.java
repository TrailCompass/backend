package space.itoncek.trailcompass.database.messages;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.database.DatabasePlayer;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Message {
    @Id
    UUID id;
    @ManyToOne(targetEntity = DatabasePlayer.class,fetch = FetchType.LAZY)
    DatabasePlayer sender;
    @ManyToOne(targetEntity = DatabasePlayer.class,fetch = FetchType.LAZY)
    DatabasePlayer receiver;
    /* Null if not read */
    ZonedDateTime read;
}
