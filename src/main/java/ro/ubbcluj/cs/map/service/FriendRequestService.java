package ro.ubbcluj.cs.map.service;

import javafx.event.ActionEvent;
import ro.ubbcluj.cs.map.domain.FriendRequest;
import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.domain.FriendshipStatusType;
import ro.ubbcluj.cs.map.repository.DBFriendRequestRepository;
import ro.ubbcluj.cs.map.socialnetworkgui.MessageAlert;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;
import ro.ubbcluj.cs.map.utils.observer.Observable;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.FriendRequestChangeEvent;
import ro.ubbcluj.cs.map.utils.observer.events.FriendshipChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendRequestService implements Observable<FriendRequestChangeEvent> {
    private DBFriendRequestRepository<Long, FriendRequest> repo;

    private List<Observer<FriendRequestChangeEvent>> observers = new ArrayList<>();

    public FriendRequestService(DBFriendRequestRepository<Long, FriendRequest> repo) {
        this.repo = repo;
    }

    public Optional<FriendRequest> getFriendRequest(Long id){
        return this.repo.findOne(id);
    }

    public Long getFriendRequestId(Long sender, Long receiver){
        ArrayList<FriendRequest> friendRequests = (ArrayList<FriendRequest>) this.repo.findAll();

        for(FriendRequest friendRequest : friendRequests){
            if(friendRequest.getIdSender() == sender && friendRequest.getIdReceiver() == receiver){
               return friendRequest.getId();
            }
        }

        return -1L;
    }

    public void addFriendRequest(Long id1, Long id2, FriendshipStatusType status){
        FriendRequest friendRequest = new FriendRequest(id1, id2, status);

        Optional<FriendRequest> optionalUser = this.repo.save(friendRequest);
        if(optionalUser.isEmpty()){
            notify(new FriendRequestChangeEvent(ChangeEventType.ADD, friendRequest, null));
        }
    }

    public Optional<FriendRequest> update(Long id, Long id1, Long id2, FriendshipStatusType status){
        Optional<FriendRequest> oldFriendRequest = this.repo.findOne(id);
        FriendRequest newFriendRequest = new FriendRequest(id1, id2, status);
        newFriendRequest.setId(id);

        Optional<FriendRequest> optionalFriendRequest = this.repo.update(newFriendRequest);
        if(optionalFriendRequest.isEmpty()){
            notify(new FriendRequestChangeEvent(ChangeEventType.UPDATE, newFriendRequest, oldFriendRequest.get()));
        }

        return optionalFriendRequest;
    }

    public void deleteRequestsBetweenSenderAndReceiver(Long sender, Long receiver){
        ArrayList<FriendRequest> friendRequests = (ArrayList<FriendRequest>) this.getFriendRequests();

        for(FriendRequest friendRequest : friendRequests){
            if(friendRequest.getIdSender() == sender && friendRequest.getIdReceiver() == receiver){
                this.repo.delete(friendRequest.getId());
                notify(new FriendRequestChangeEvent(ChangeEventType.REMOVE, null, friendRequest));
                return;
            }

            if(friendRequest.getIdReceiver() == sender && friendRequest.getIdSender() == receiver){
                this.repo.delete(friendRequest.getId());
                notify(new FriendRequestChangeEvent(ChangeEventType.REMOVE, null, friendRequest));
                return;
            }
        }
    }

    public Iterable<FriendRequest> getFriendRequests(){
        return this.repo.findAll();
    }

    @Override
    public void addObserver(Observer<FriendRequestChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<FriendRequestChangeEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(FriendRequestChangeEvent t) {
        observers.stream().forEach(o -> o.update(t));
    }
}
