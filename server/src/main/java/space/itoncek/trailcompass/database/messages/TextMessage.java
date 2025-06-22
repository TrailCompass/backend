package space.itoncek.trailcompass.database.messages;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TextMessage extends Message {
    String title;
    String content;
}
