package space.itoncek.trailcompass.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Entity
@NamedQuery(name = "findOldestEntry", query = "SELECT e FROM LocationEntry e WHERE timestamp IN (SELECT MAX(timestamp) t FROM LocationEntry) AND e.player.id = :id")
public class LocationEntry {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID id;
	@ManyToOne(targetEntity = DatabasePlayer.class, fetch = FetchType.LAZY)
	DatabasePlayer player;
	long timestamp;
	float lat;
	float lon;
	float alt;
}