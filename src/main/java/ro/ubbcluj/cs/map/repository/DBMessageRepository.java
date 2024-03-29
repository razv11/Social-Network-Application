package ro.ubbcluj.cs.map.repository;

import com.zaxxer.hikari.HikariConfig;
import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.domain.Message;
import ro.ubbcluj.cs.map.domain.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.zaxxer.hikari.HikariDataSource;


public class DBMessageRepository<ID, E extends Entity<ID>> implements Repository<Long, Message> {
    private HikariDataSource dataSource;
    private String url;
    private String username;
    private String password;

    private DBUserRepository<Long, User> repoUsers;

    public DBMessageRepository(String url, String username, String password, DBUserRepository<Long, User> repo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.repoUsers = repo;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        // Set other HikariCP configuration options as needed
        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Optional<Message> findOne(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id_message = ?");
                PreparedStatement statement_toUsers = connection.prepareStatement("SELECT * FROM userMessages WHERE id_message = ?");
        ){
                statement.setLong(1, id);
                statement_toUsers.setLong(1, id);


                ResultSet messages = statement.executeQuery();
                ResultSet toUsers = statement_toUsers.executeQuery();

                return messages.next() ? Optional.of(mapResultSetToMessage(messages, toUsers)) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Iterable<Message> findAll() {
        ArrayList<Message> messages = new ArrayList<>();

        try(
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages");
                PreparedStatement statement_toUsers = connection.prepareStatement("SELECT * FROM userMessages")
        ){
                ResultSet r = statement.executeQuery();
                ResultSet toUsers = statement_toUsers.executeQuery();
                while(r.next()){
                    messages.add(mapResultSetToMessage(r, toUsers));
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) {
        if(entity == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO messages(from_user_id, message, dateMessage, reply_message_id) VALUES (?, ?, ?, ?) RETURNING id_message");
        ){
                statement.setLong(1, entity.getFrom().getId());
                statement.setString(2, entity.getMessage());
                statement.setObject(3, entity.getDate());
                if(entity.getReply() != null){
                    statement.setLong(4, entity.getReply().getId());
                }
                else{
                    statement.setObject(4, null);
                }

                ResultSet result = statement.executeQuery();
                if(result.next()){
                    // insert into userMessages table

                    PreparedStatement statement_userMessages = connection.prepareStatement("INSERT INTO userMessages(id_user, id_message) VALUES (?, ?)");
                    Long messageId = result.getLong("id_message");

                    for(User toUser : entity.getTo()){
                        statement_userMessages.setLong(1, toUser.getId());
                        statement_userMessages.setLong(2, messageId);
                        statement_userMessages.executeUpdate();
                    }

                    return Optional.empty();
                }

                return Optional.of(entity);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE id_message = ?");
                PreparedStatement statement_userMessages = connection.prepareStatement("DELETE FROM userMessages WHERE id_message = ?")
        ){
                statement_userMessages.setLong(1, id);
                statement_userMessages.executeUpdate();

                Optional<Message> message = this.findOne(id);
                statement.setLong(1, id);
                int affectedRows = statement.executeUpdate();

                return affectedRows == 0 ? Optional.empty() : message;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> update(Message entity) {
        // update only message and date for a message

        if(entity == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE messages SET message = ?, dateMessage = ? WHERE id_message = ?");
        ){
                statement.setString(1, entity.getMessage());
                statement.setObject(2, entity.getDate());
                statement.setLong(3, entity.getId());

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> getMessagesIdListForUser(Long idUser){
        if(idUser == null){
            throw new IllegalArgumentException();
        }

        List<Long> messagesId = new ArrayList<>();

        try(
                Connection connection = this.dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM userMessages WHERE id_user = ?");
        ){
                statement.setLong(1, idUser);
                ResultSet r = statement.executeQuery();

                while(r.next()){
                    Long id = r.getLong("id_message");
                    messagesId.add(id);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return messagesId;
    }


    private Message mapResultSetToMessage(ResultSet r, ResultSet toUsers){
        try {
            Long id_message = r.getLong("id_message");

            Long from_userId = r.getLong("from_user_id");
            User fromUser = this.repoUsers.findOne(from_userId).get();

            List<User> to = new ArrayList<>();
            while(toUsers.next()){
                Long to_userId = toUsers.getLong("id_user");
                Long id_Message = toUsers.getLong("id_message");
                if(id_Message == id_message){
                    User toUser = this.repoUsers.findOne(to_userId).get();
                    to.add(toUser);
                }
            }

            String message = r.getString("message");
            LocalDateTime date = r.getTimestamp("dateMessage").toLocalDateTime();

            Message currentMessage = new Message(fromUser, to, message, null);
            currentMessage.setDate(date);
            currentMessage.setId(id_message);

            if(r.getObject("reply_message_id") != null){
                Long reply_MessageId = r.getLong("reply_message_id");
                currentMessage.setReply(this.findOne(reply_MessageId).get());
            }

            return currentMessage;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
