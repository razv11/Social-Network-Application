package ro.ubbcluj.cs.map.utils.observer.events;

import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.service.FriendshipService;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;

public class FriendshipChangeEvent implements Event{
    private ChangeEventType type;
    private Friendship newFriendship;
    private Friendship oldFriendship;

    public FriendshipChangeEvent(ChangeEventType type, Friendship newFriendship, Friendship oldFriendship) {
        this.type = type;
        this.newFriendship = newFriendship;
        this.oldFriendship = oldFriendship;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Friendship getNewFriendship() {
        return newFriendship;
    }

    public Friendship getOldFriendship() {
        return oldFriendship;
    }
}
