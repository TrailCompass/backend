package space.itoncek.trailcompass.database.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.database.DatabasePlayer;

import java.util.UUID;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQuery(name = "getAllCards", query = "SELECT c FROM Card c")
@NamedQuery(name = "getAllCardsInDeck", query = "SELECT c FROM Card c WHERE c.owner IS NULL")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @ManyToOne(targetEntity = DatabasePlayer.class,fetch = FetchType.LAZY)
    DatabasePlayer owner;
}
