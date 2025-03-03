package space.itoncek.trailcompass.database;

import java.io.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.User;
import space.itoncek.trailcompass.objects.UserMeta;
import space.itoncek.trailcompass.objects.messages.Message;
import space.itoncek.trailcompass.objects.messages.MessageContent;

public class StorageDatabaseImpl implements DatabaseInterface {
	private static final Logger log = LoggerFactory.getLogger(StorageDatabaseImpl.class);
	private final File db;
	private final ReentrantLock lock = new ReentrantLock();

	public StorageDatabaseImpl(File db) {
		this.db = db;
	}

	@Override
	public void migrate() {
		log.info("This database is stored in a file!");
		if (!db.exists()) {
			try {
				db.createNewFile();
			} catch (IOException e) {
				log.error("Unable to create a new database file", e);
				throw new RuntimeException(e);
			}

			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(db))) {
				oos.writeObject(Database.generateEmptyDatabase());
			} catch (IOException e) {
				log.error("Unable to write to a new database file", e);
				throw new RuntimeException(e);
			}
		}
	}

	private Database getDatabase() throws IOException, ClassNotFoundException {
		lock.lock();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(db))) {
			Object o = ois.readObject();
			if (o instanceof Database obj) {
				return obj;
			} else throw new ClassNotFoundException("Config is corrupted, please fix or file an issue!");
		} catch (FileNotFoundException e) {
			log.error("Unable to locate database file", e);
			throw e;
		} catch (IOException e) {
			log.error("IOError while working with the database", e);
			throw e;
		} catch (ClassNotFoundException e) {
			log.error("Cannot find specified class", e);
			throw e;
		} finally {
			lock.unlock();
		}
	}

	private void saveDatabase(Database d) throws IOException {
		lock.lock();
		db.delete();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(db))) {
			oos.writeObject(d);
		} finally {
			lock.unlock();
		}
	}

	@Nullable
	@Override
	public User getUserByID(int id) {
		try {
			Database d = getDatabase();
			List<Database.User> userStream = d.users.stream().filter(x -> x.id == id).toList();
			if (userStream.size() == 1) {
				Database.User user = userStream.get(0);
				return new User(user.id, user.nickname, user.isAdmin, user.isHider);
			}
			return null;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Nullable
	@Override
	public UserMeta getUserMeta(String nickname, String passwordhash) {
		try {
			Database d = getDatabase();
			List<Database.User> userStream = d.users.stream().filter(x -> x.nickname.equals(nickname) && x.passwordHash.equals(passwordhash)).toList();
			if (userStream.size() == 1) {
				Database.User user = userStream.get(0);
				return new UserMeta(user.id, System.currentTimeMillis() + 600000);
			}
			return null;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public boolean createUser(String name, String passwordhash) {
		return createUser(name, passwordhash, false);
	}

	@Override
	public boolean createUser(String name, String passwordhash, boolean isAdmin) {
		try {
			Database d = getDatabase();
			d.users.add(new Database.User(findHighestIndexUser(d.users) + 1, name, passwordhash, isAdmin));
			saveDatabase(d);
			return true;
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean needsDefaultUser() {
		try {
			Database d = getDatabase();
			return d.users.isEmpty();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isHealthy() {
		if (!db.exists()) return false;
		try {
			getDatabase();
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean createMessage(int senderId, int receiverId, MessageContent content) {
		try {
			Database d = getDatabase();
			d.messages.add(new Database.Message(findHighestIndexMessage(d.messages), senderId, receiverId, content.serialize(), false));
			saveDatabase(d);
			return true;
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}

	@Nullable
	@Override
	public List<Message> getMessages(int receiverId) {
		try {
			Database d = getDatabase();

			return d.messages.parallelStream()
					.filter(x -> x.receiver_id == receiverId)
					.map(x -> new Message(x.id, x.sender_id, MessageContent.decode(x.content), x.read))
					.toList();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public int getCurrentHiderId() {
		try {
			Database d = getDatabase();
			return d.hiderId;
		} catch (IOException | ClassNotFoundException e) {
			log.warn("Unable to get current hider from the database", e);
			return -1;
		}
	}

	@Override
	public boolean setCurrentHider(int i) {
		try {
			Database d = getDatabase();
			if (d.users.stream().anyMatch(x -> x.id == i)) {
				d.hiderId = i;
				return true;
			} else return false;
		} catch (IOException | ClassNotFoundException e) {
			log.warn("Unable to get current hider from the database", e);
			return false;
		}
	}

	@Override
	public ZonedDateTime getStartTime() {
		try {
			Database d = getDatabase();
			return d.gamestate.game_start;
		} catch (IOException | ClassNotFoundException e) {
			log.warn("Unable to get current hider from the database", e);
			return ZonedDateTime.of(1970,1,1,0,0,1,0, ZoneId.of("UTC"));
		}
	}

	@Nullable
	@Override
	public List<User> listUsers() {
		return List.of();
	}

	@Override
	public boolean setSetupLocked(boolean lock) {
		try {
			Database d = getDatabase();
			d.gamestate.setupLocked = lock;
			saveDatabase(d);
			return true;
		} catch (IOException | ClassNotFoundException e) {
			log.warn("Unable to save setuplock to the database", e);
			return false;
		}
	}

	@Override
	public boolean getSetupLocked() {
		try {
			Database d = getDatabase();
			return d.gamestate.setupLocked;
		} catch (IOException | ClassNotFoundException e) {
			log.warn("Unable to get setuplock from the database", e);
			return false;
		}
	}

	@Override
	public Duration setHidingTime() {
		return null;
	}

	@Override
	public boolean setHidingTime(Duration hiding_time) {
		return false;
	}

	@Override
	public boolean isInRestPeriod(ZonedDateTime dateTime) {
		return false;
	}

	@Override
	public void close() {
		lock.lock();
		lock.unlock();
	}

	private int findHighestIndexUser(List<Database.User> users) {
		return users.parallelStream()
				.max(Comparator.comparingInt(a -> a.id))
				.map(user -> user.id)
				.orElse(0);
	}

	private int findHighestIndexMessage(List<Database.Message> messages) {
		return messages.parallelStream()
				.max(Comparator.comparingInt(a -> a.id))
				.map(user -> user.id)
				.orElse(0);
	}

	private static class Database implements Serializable {
		public List<Database.User> users;
		public List<Database.Message> messages;
		public GameStateTable gamestate;
		public int hiderId;

		public static Database generateEmptyDatabase() {
			Database database = new Database();
			database.users = new ArrayList<>();
			database.messages = new ArrayList<>();
			database.hiderId = -1;
			database.gamestate = new GameStateTable(ZonedDateTime.now(),Duration.ofHours(3),false);
			return database;
		}

		@SuppressWarnings("CanBeFinal")
		public static final class User implements Serializable {
			public int id;
			public String nickname;
			public String passwordHash;
			public boolean isAdmin;
			public boolean isHider;

			private User(int id, String nickname, String passwordHash, boolean isAdmin) {
				this.id = id;
				this.nickname = nickname;
				this.passwordHash = passwordHash;
				this.isAdmin = isAdmin;
				this.isHider = false;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this) return true;
				if (obj == null || obj.getClass() != this.getClass()) return false;
				var that = (User) obj;
				return this.id == that.id &&
					   Objects.equals(this.nickname, that.nickname) &&
					   Objects.equals(this.passwordHash, that.passwordHash) &&
					   this.isAdmin == that.isAdmin &&
					   this.isHider == that.isHider;
			}

			@Override
			public int hashCode() {
				return Objects.hash(id, nickname, passwordHash, isAdmin, isHider);
			}

			@Override
			public String toString() {
				return "User[" +
					   "id=" + id + ", " +
					   "nickname=" + nickname + ", " +
					   "passwordHash=" + passwordHash + ", " +
					   "isAdmin=" + isAdmin + ", " +
					   "isHider=" + isHider + ']';
			}
		}

		@SuppressWarnings("CanBeFinal")
		public static final class Message implements Serializable {
			public int id;
			public int sender_id;
			public int receiver_id;
			public String content;
			public boolean read;

			public Message(int id, int sender_id, int receiver_id, String content,
						   boolean read) {
				this.id = id;
				this.sender_id = sender_id;
				this.receiver_id = receiver_id;
				this.content = content;
				this.read = read;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this) return true;
				if (obj == null || obj.getClass() != this.getClass()) return false;
				var that = (Message) obj;
				return this.id == that.id &&
					   this.sender_id == that.sender_id &&
					   this.receiver_id == that.receiver_id &&
					   Objects.equals(this.content, that.content) &&
					   this.read == that.read;
			}

			@Override
			public int hashCode() {
				return Objects.hash(id, sender_id, receiver_id, content, read);
			}

			@Override
			public String toString() {
				return "Message[" +
					   "id=" + id + ", " +
					   "sender_id=" + sender_id + ", " +
					   "receiver_id=" + receiver_id + ", " +
					   "content=" + content + ", " +
					   "read=" + read + ']';
			}

		}

		@SuppressWarnings("CanBeFinal")
		public static final class GameStateTable implements Serializable {
			public ZonedDateTime game_start;
			public Duration hiding_time;
			public boolean setupLocked;

			public GameStateTable(ZonedDateTime game_start, Duration hiding_time, boolean setup_locked) {
				this.game_start = game_start;
				this.hiding_time = hiding_time;
				this.setupLocked = setup_locked;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this) return true;
				if (obj == null || obj.getClass() != this.getClass()) return false;
				var that = (GameStateTable) obj;
				return Objects.equals(this.game_start, that.game_start) &&
					   Objects.equals(this.setupLocked, that.setupLocked);
			}

			@Override
			public int hashCode() {
				return Objects.hash(game_start, setupLocked);
			}

			@Override
			public String toString() {
				return "GameStateTable[" +
					   "game_start=" + game_start + ", " +
					   "setupLocked=" + setupLocked + ']';
			}
		}
	}
}