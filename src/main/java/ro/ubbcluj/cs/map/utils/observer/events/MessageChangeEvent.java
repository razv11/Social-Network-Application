package ro.ubbcluj.cs.map.utils.observer.events;

import ro.ubbcluj.cs.map.domain.Message;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;

public class MessageChangeEvent implements Event{
    private ChangeEventType type;
    private Message newMessage;

    public MessageChangeEvent(ChangeEventType type, Message newMessage) {
        this.type = type;
        this.newMessage = newMessage;
    }

    public ChangeEventType getType() {
        return type;
    }

    public void setType(ChangeEventType type) {
        this.type = type;
    }

    public Message getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(Message newMessage) {
        this.newMessage = newMessage;
    }
}
