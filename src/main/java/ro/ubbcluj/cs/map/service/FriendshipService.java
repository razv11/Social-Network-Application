package ro.ubbcluj.cs.map.service;

import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.repository.DBFriendshipRepository;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;
import ro.ubbcluj.cs.map.utils.observer.Observable;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.FriendshipChangeEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FriendshipService implements Observable<FriendshipChangeEvent> {
    private DBFriendshipRepository<Long, Friendship> repo;
    private List<Observer<FriendshipChangeEvent>> observers = new ArrayList<>();

    public FriendshipService(DBFriendshipRepository<Long, Friendship> repo) {
        this.repo = repo;
    }

    public Optional<Friendship> getFriendshipForUsers(Long id1, Long id2){
        ArrayList<Friendship> friendships = (ArrayList<Friendship>) this.repo.findAll();
        for(Friendship friendship : friendships){
            if(friendship.getId1() == id1 && friendship.getId2() == id2){
                return Optional.of(friendship);
            }
        }

        return Optional.empty();
    }

    public Optional<Friendship> findFriendshipById(Long id){
        return this.repo.findOne(id);
    }

    public void addFriendship(Long id1, Long id2){
        Friendship fs = new Friendship(id1, id2);
        Optional<Friendship> optionalFriendship = this.repo.save(fs);

        if(optionalFriendship.isEmpty()){
            notify(new FriendshipChangeEvent(ChangeEventType.ADD, fs, null));
        }
    }

    public Optional<Friendship> updateFriendship(Long id, long numberOfMonths){
        Optional<Friendship> friendship = this.repo.findOne(id);
        Friendship fs = friendship.get();
        LocalDateTime date = fs.getDate().minusMonths(numberOfMonths);
        fs.setDate(date);
        return this.repo.update(fs);
    }

    public Optional<Friendship> deleteFriendship(Long id){
        Optional<Friendship> friendship = this.repo.delete(id);
        if(friendship.isPresent()){
            notify(new FriendshipChangeEvent(ChangeEventType.REMOVE, null, friendship.get()));
        }

        return friendship;
    }

    public void deleteFriendshipBetweenUsers(Long id1, Long id2){
        ArrayList<Friendship> friendships = (ArrayList<Friendship>) this.repo.findAll();

        Friendship oldFriendship = null;
        for(Friendship friendship : friendships){
            if(friendship.getId1() == id1 && friendship.getId2() == id2){
               oldFriendship = friendship;
               this.repo.delete(friendship.getId());
               break;
            }

            if(friendship.getId2() == id1 && friendship.getId1() == id2){
                oldFriendship = friendship;
                this.repo.delete(friendship.getId());
                break;
            }
        }

        notify(new FriendshipChangeEvent(ChangeEventType.REMOVE, null, oldFriendship));
    }

    public void deleteFriendshipsByUserId(Long userId){
        ArrayList<Friendship> friendships = (ArrayList<Friendship>) this.repo.findAll();
        for(Friendship fs : friendships){
            if(fs.getId1() == userId || fs.getId2() == userId){
                this.repo.delete(fs.getId());
            }
        }
    }

    public int countComunities(Set<Long> usersID){
        Set<Long> visited = new HashSet<>();
        int count = 0;

        for(Long id : usersID){
            if( !visited.contains(id) ){
                explore(id, visited);
                count++;
            }
        }

        return count;
    }

    private void explore(Long id, Set<Long> visited){
        visited.add(id);
        this.repo.findAll().forEach(friendship -> {
            if( friendship.getId1() == id && !visited.contains(friendship.getId2()) ){
                explore(friendship.getId2(), visited);
            }
            if( friendship.getId2() == id && !visited.contains(friendship.getId1()) ){
                explore(friendship.getId1(), visited);
            }
        });
    }

    public Set<Long> mostSociableCommunnity(Set<Long> usersId){
        Set<Long> mostSociable = new HashSet<>();
        Set<Long> currentCommunity = new HashSet<>();

        for(Long id : usersId){
            currentCommunity.clear();
            exploreForCommunity(id, currentCommunity);
            if(currentCommunity.size() > mostSociable.size()) {
                mostSociable = new HashSet<>(currentCommunity);
            }
        }
        return mostSociable;
    }

    private void exploreForCommunity(Long id, Set<Long> currentCommunity){
        if( !currentCommunity.contains(id) ) {
            currentCommunity.add(id);
            this.repo.findAll().forEach(friendship -> {
                if(friendship.getId1() == id){
                    exploreForCommunity(friendship.getId2(), currentCommunity);
                }
                if(friendship.getId2() == id){
                    exploreForCommunity(friendship.getId1(), currentCommunity);
                }
            });
        }
    }

    public List<Friendship> getUserFriendshipsFromOneMonth(Long id, Long month){
        List<Friendship> friendships = (List<Friendship>) this.repo.findAll();

        return friendships.stream()
                .filter(friendship -> friendship.getId1() == id || friendship.getId2() == id)
                .filter(friendship -> friendship.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public Iterable<Friendship> getFriendships(){
        return this.repo.findAll();
    }

    @Override
    public void addObserver(Observer<FriendshipChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<FriendshipChangeEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(FriendshipChangeEvent t) {
        observers.stream().forEach(o -> o.update(t));
    }
}
