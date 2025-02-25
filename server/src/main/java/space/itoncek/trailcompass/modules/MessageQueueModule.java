package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.messages.Message;
import space.itoncek.trailcompass.messages.MessageContent;

import java.sql.SQLException;
import java.util.List;

public class MessageQueueModule {
	private final TrailServer server;

	public MessageQueueModule(TrailServer server) {
		this.server = server;
	}

	public void addMessage(int sender_id, int receiver_id, MessageContent content) throws SQLException {
		server.db.createMessage(sender_id,receiver_id,content);
	}
	public List<Message> getMessages(int receiver_id) throws SQLException {
		return server.db.getMessages(receiver_id);
	}

	public void addMessage(@NotNull Context ctx) {

	}

	public void getMessages(@NotNull Context ctx) {

	}
}
