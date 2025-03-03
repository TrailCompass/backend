package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.messages.Message;
import space.itoncek.trailcompass.objects.messages.MessageContent;
import space.itoncek.trailcompass.objects.User;

import java.sql.SQLException;
import java.util.List;

public class MessageQueueModule {
	private static final Logger log = LoggerFactory.getLogger(MessageQueueModule.class);
	private final TrailServer server;

	public MessageQueueModule(TrailServer server) {
		this.server = server;
	}

	public boolean addMessage(int sender_id, int receiver_id, MessageContent content) throws SQLException {
		return server.db.createMessage(sender_id, receiver_id, content);
	}

	public boolean sendMessageToGroup(int sender_id, boolean hiders, MessageContent content) {
		try {
			List<User> usrs = server.db.listUsers();
			if (usrs == null) return false;
			usrs = usrs.stream()
					.filter(x -> x.hider() == hiders)
					.toList();
			for (User user : usrs) {
				boolean b = addMessage(sender_id, user.id(), content);
				if (!b) return false;
			}
			return true;
		} catch (SQLException e) {
			log.warn("Database access error",e);
			return false;
		}
	}

	public boolean sendMessageToAll(int sender_id, MessageContent content) {
		try {
			List<User> usrs = server.db.listUsers();
			if (usrs == null) return false;
			for (User user : usrs) {
				boolean b = addMessage(sender_id, user.id(), content);
				if (!b) return false;
			}
			return true;
		} catch (SQLException e) {
			log.warn("Database access error",e);
			return false;
		}
	}

	public List<Message> getMessages(int receiver_id) throws SQLException {
		return server.db.getMessages(receiver_id);
	}

	// TODO)) Fill this method
	public void addMessage(@NotNull Context ctx) {
	}

	// TODO)) Fill this method
	public void getMessages(@NotNull Context ctx) {

	}
}
