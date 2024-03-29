package ro.ubbcluj.cs.map.domain;

import ro.ubbcluj.cs.map.service.FriendshipService;

import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Entity<Long> {
    private Long id1;
    private Long id2;
    private LocalDateTime friendsFrom;
    private FriendshipStatusType status;

    public Friendship(Long id1, Long id2) {
        this.id1 = id1;
        this.id2 = id2;
        friendsFrom = LocalDateTime.now();
    }

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public LocalDateTime getDate() {
        return friendsFrom;
    }

    public FriendshipStatusType getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatusType status) {
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.friendsFrom = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(id1, that.id1) && Objects.equals(id2, that.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }

    @Override
    public String toString() {
        return  "ID Friendship: " + getId() + " | " +
                "First user ID: " + id1 + " | " +
                "Second user ID: " + id2 + " | " +
                "Friends from: " + friendsFrom;
    }
}
