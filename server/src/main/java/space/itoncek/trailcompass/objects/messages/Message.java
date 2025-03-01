package space.itoncek.trailcompass.objects.messages;

public record Message(int id, int sender, MessageContent content, boolean read) {
}
