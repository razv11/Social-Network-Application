package ro.ubbcluj.cs.map.utils.observer.events;

import ro.ubbcluj.cs.map.domain.FriendRequest;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;

public class FriendRequestChangeEvent implements Event{
    private ChangeEventType type;
    private FriendRequest newFriendRequest;
    private FriendRequest olfFriendRequest;

    public FriendRequestChangeEvent(ChangeEventType type, FriendRequest newFriendRequest, FriendRequest olfFriendRequest) {
        this.type = type;
        this.newFriendRequest = newFriendRequest;
        this.olfFriendRequest = olfFriendRequest;
    }

    public ChangeEventType getType() {
        return type;
    }

    public void setType(ChangeEventType type) {
        this.type = type;
    }

    public FriendRequest getNewFriendRequest() {
        return newFriendRequest;
    }

    public void setNewFriendRequest(FriendRequest newFriendRequest) {
        this.newFriendRequest = newFriendRequest;
    }

    public FriendRequest getOlfFriendRequest() {
        return olfFriendRequest;
    }

    public void setOlfFriendRequest(FriendRequest olfFriendRequest) {
        this.olfFriendRequest = olfFriendRequest;
    }
}
