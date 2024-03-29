package ro.ubbcluj.cs.map.domain;

import java.time.LocalDateTime;

public class UserDTO extends User{
    private String friendsFrom;
    private FriendshipStatusType status;

    public UserDTO(String firstName, String lastName, String friendsFrom, FriendshipStatusType status) {
        super(firstName, lastName);
        this.friendsFrom = friendsFrom;
        this.status = status;
    }

    public String getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(String friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    public FriendshipStatusType getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatusType status) {
        this.status = status;
    }
}
