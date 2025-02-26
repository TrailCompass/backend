package space.itoncek.trailcompass.database;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.messages.Message;
import space.itoncek.trailcompass.messages.MessageContent;
import space.itoncek.trailcompass.pkg.objects.User;
import space.itoncek.trailcompass.pkg.objects.UserMeta;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
		if(!db.exists()) {
			try {
				db.createNewFile();
			} catch (IOException e) {
				log.error("Unable to create a new database file",e);
				throw new RuntimeException(e);
			}

			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(db))){
				oos.writeObject(Database.generateEmptyDatabase());
			} catch (IOException e) {
				log.error("Unable to write to a new database file",e);
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
			}
			else throw new ClassNotFoundException("Config is corrupted, please fix or file an issue!");
		} catch (FileNotFoundException e) {
			log.error("Unable to locate database file",e);
			throw e;
		} catch (IOException e) {
			log.error("IOError while working with the database",e);
			throw e;
		} catch (ClassNotFoundException e) {
			log.error("Cannot find specified class",e);
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
				return new User(user.id,user.nickname,user.isAdmin,user.isHider);
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
				return new UserMeta(user.id,System.currentTimeMillis() + 600000);
			}
			return null;
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public boolean createUser(String name, String passwordhash) {
		return createUser(name,passwordhash,false);
	}

	@Override
	public boolean createUser(String name, String passwordhash, boolean isAdmin) {
		try {
			Database d = getDatabase();
			d.users.add(new Database.User(findHighestIndexUser(d.users) + 1, name,passwordhash,isAdmin,false));
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
		if(!db.exists()) return false;
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
			d.messages.add(new Database.Message(findHighestIndexMessage(d.messages),senderId,receiverId, content.serialize(), false));
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
					.filter(x->x.receiver_id == receiverId)
					.map(x-> {
						return new Message(x.id,x.sender_id,MessageContent.decode(x.content),x.read);
					})
					.toList();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public void close() {

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

		public static Database generateEmptyDatabase() {
			Database database = new Database();
			database.users = new ArrayList<>();
			database.messages = new ArrayList<>();
			return database;
		}

		private record User(int id, String nickname, String passwordHash, boolean isAdmin, boolean isHider) implements Serializable {

		}

		public record Message(int id, int sender_id, int receiver_id, String content, boolean read) implements Serializable {
		}
	}
}
