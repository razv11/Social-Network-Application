package ro.ubbcluj.cs.map.domain;

public class FriendRequest extends Entity<Long> {
    private Long idSender;
    private Long idReceiver;
    private FriendshipStatusType status;

    public FriendRequest(Long idSender, Long idReceiver, FriendshipStatusType status) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.status = status;
    }

    public Long getIdSender() {
        return idSender;
    }

    public void setIdSender(Long idSender) {
        this.idSender = idSender;
    }

    public Long getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(Long idReceiver) {
        this.idReceiver = idReceiver;
    }

    public FriendshipStatusType getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatusType status) {
        this.status = status;
    }
}
