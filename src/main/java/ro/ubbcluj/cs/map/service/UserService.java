package ro.ubbcluj.cs.map.service;

import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.repository.DBUserRepository;
import ro.ubbcluj.cs.map.repository.PagingRepo.Page;
import ro.ubbcluj.cs.map.repository.PagingRepo.Pageable;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;
import ro.ubbcluj.cs.map.utils.observer.Observable;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.UserChangeEvent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserService implements Observable<UserChangeEvent> {
    private DBUserRepository<Long, User> repo;

    private List<Observer<UserChangeEvent>> observers = new ArrayList<>();

    public UserService(DBUserRepository<Long, User> repo) {
        this.repo = repo;
    }

    public Optional<User> existUser(Long id){
        return this.repo.findOne(id);
    }
    public Optional<User> addUser(String first_name, String last_name){
        User user = new User(first_name, last_name);
        Optional<User> userOptional = this.repo.save(user);
        if(userOptional.isPresent()){
            notify(new UserChangeEvent(ChangeEventType.ADD, user));
        }
        return userOptional;
    }

    public Optional<User> updateUser(Long id, String first_name, String last_name){
        User oldUser = this.repo.findOne(id).get();

        User newUser = new User(first_name, last_name);
        newUser.setId(id);

        Optional<User> userOptional = this.repo.update(newUser);
        if(userOptional.isEmpty()){
            notify(new UserChangeEvent(ChangeEventType.UPDATE, newUser, oldUser));
        }

        return userOptional;
    }

    public Optional<User> deleteUser(Long id){
        Optional<User> user = this.repo.delete(id);
        if(user.isPresent()){
            notify(new UserChangeEvent(ChangeEventType.REMOVE, user.get()));
        }

        return user;
    }

    public Set<Long> getUsersID(){
        Set<Long> usersId = new HashSet<>();
        for(User user : this.repo.findAll()){
            usersId.add(user.getId());
        }
        return usersId;
    }

    public Iterable<User> getUsers(){
        return this.repo.findAll();
    }

    public Page<User> findAll(Pageable pageable){
        return this.repo.findAll(pageable);
    }

    public Iterable<User> getUsersWithName(String name){
        List<User> users = StreamSupport.stream(this.repo.findAll().spliterator(), false)
                .filter(user -> user.getFirstName().equalsIgnoreCase(name) || user.getLastName().equalsIgnoreCase(name))
                .collect(Collectors.toList());

        return users;
    }

    @Override
    public void addObserver(Observer<UserChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<UserChangeEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(UserChangeEvent t) {
        observers.stream().forEach(o -> o.update(t));
    }
}
