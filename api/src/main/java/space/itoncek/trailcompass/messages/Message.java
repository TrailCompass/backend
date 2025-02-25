package space.itoncek.trailcompass.messages;

public record Message(int id, int sender, MessageContent content, boolean read) {
}
