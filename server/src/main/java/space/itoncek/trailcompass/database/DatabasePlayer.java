package space.itoncek.trailcompass.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import space.itoncek.trailcompass.commons.objects.Player;
import space.itoncek.trailcompass.database.cards.DeckCard;
import space.itoncek.trailcompass.database.cards.ShadowCard;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NamedQuery(name = "findAllPlayers", query = "SELECT e FROM DatabasePlayer e")
@NamedQuery(name = "findPlayerByNickname", query = "SELECT e FROM DatabasePlayer e WHERE e.nickname = :nickmane")
public class DatabasePlayer {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID id;
	String nickname;
	byte[] passwordHash;
	boolean admin;
	@OneToMany(targetEntity = LocationEntry.class, mappedBy = "player")
	List<LocationEntry> tracList;
	@OneToMany(targetEntity = DeckCard.class, mappedBy = "owner")
	List<DeckCard> cards;
	@OneToMany(targetEntity = DeckCard.class, mappedBy = "owner")
	List<ShadowCard> shadowCards;

	public Player serialize() {
		return new Player(id, nickname, passwordHash,admin);
	}

	public static DatabasePlayer deserialize(Player p) {
		DatabasePlayer db = new DatabasePlayer();
		db.setId(p.id());
		db.setNickname(p.nickname());
		db.setPasswordHash(p.passwordHash());
		db.setAdmin(p.admin());
		return db;
	}
}
