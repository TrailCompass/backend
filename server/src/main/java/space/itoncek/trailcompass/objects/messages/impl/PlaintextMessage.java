package space.itoncek.trailcompass.objects.messages.impl;

import space.itoncek.trailcompass.objects.messages.MessageContent;
import space.itoncek.trailcompass.objects.messages.MessageType;

public class PlaintextMessage implements MessageContent {
	private final String text;

	public PlaintextMessage(String text) {
		this.text = text;
	}

	@Override
	public MessageContent deserialize(String text) {
		return new PlaintextMessage(text);
	}

	@Override
	public String serialize() {
		return text;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.PLAINTEXT;
	}
}
