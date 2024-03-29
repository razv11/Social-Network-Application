package ro.ubbcluj.cs.map.service;

import ro.ubbcluj.cs.map.domain.Message;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.repository.DBMessageRepository;
import ro.ubbcluj.cs.map.utils.observer.ChangeEventType;
import ro.ubbcluj.cs.map.utils.observer.Observable;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.MessageChangeEvent;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageService implements Observable<MessageChangeEvent> {
    private DBMessageRepository<Long, Message> repo;
    private List<Observer<MessageChangeEvent>> observers = new ArrayList<>();

    public MessageService(DBMessageRepository<Long, Message> repo) {
        this.repo = repo;
    }

    public void addMessage(User sender, List<User> receivers, String message){
        Message newMessage = new Message(sender, receivers, message, null);
        if(this.repo.save(newMessage).isEmpty()){
            this.notify(new MessageChangeEvent(ChangeEventType.ADD, newMessage));
        }
    }

    public void addReply(User sender, List<User> receivers, String message, Message originalMessage) {
        Message reply = new Message(sender, receivers, message, originalMessage);

        if(this.repo.save(reply).isEmpty()){
            this.notify(new MessageChangeEvent(ChangeEventType.ADD, originalMessage));
        }
    }

    public void deleteConversationBetweenUsers(User currentUser, User selectedUser){
        List<Long> recvMessagesIdForCurrentUser = this.repo.getMessagesIdListForUser(currentUser.getId());
        List<Long> recvMessagesIdForSelectedser = this.repo.getMessagesIdListForUser(selectedUser.getId());

        for(Message message : this.repo.findAll()) {
            if(message.getFrom().getId() == currentUser.getId() && recvMessagesIdForSelectedser.contains(message.getId())){
                this.repo.delete(message.getId());
            }

            if(message.getFrom().getId() == selectedUser.getId() && recvMessagesIdForCurrentUser.contains(message.getId())){
                this.repo.delete(message.getId());
            }
        }

        this.notify(new MessageChangeEvent(ChangeEventType.REMOVE, null));
    }

    public void deleteMessageForUser(Long id){
        for(Message message : repo.findAll()){
            if(message.getFrom().getId() == id){
                repo.delete(message.getId());
                for(User user : message.getTo()){
                    if(user.getId() == id){
                        repo.delete(message.getId());
                    }
                }
            }
        }
    }

    Iterable<Message> getMessages(){
        return this.repo.findAll();
    }

    public List<Message> getOrderedMessagesBetweenUsers(User currentUser, User selectedUser){
        List<Long> recvMessagesIdForCurrentUser = this.repo.getMessagesIdListForUser(currentUser.getId());
        List<Long> recvMessagesIdForSelectedser = this.repo.getMessagesIdListForUser(selectedUser.getId());

        List<Message> usersMessages = StreamSupport.stream(this.repo.findAll().spliterator(), false)
                .filter(message -> ( message.getFrom().getId() == currentUser.getId() && recvMessagesIdForSelectedser.contains(message.getId()) ) || ( message.getFrom().getId() == selectedUser.getId() && recvMessagesIdForCurrentUser.contains(message.getId()) ) )
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toList());

        return usersMessages;
    }

    @Override
    public void addObserver(Observer<MessageChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<MessageChangeEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(MessageChangeEvent t) {
        observers.stream().forEach(o -> o.update(t));
    }
}
