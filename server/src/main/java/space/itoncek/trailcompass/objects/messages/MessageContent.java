package space.itoncek.trailcompass.objects.messages;

public interface MessageContent {
	static MessageContent decode(String content) {
		return null;
	}

	MessageContent deserialize(String o);
	String serialize();
	MessageType getMessageType();
}
