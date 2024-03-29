package ro.ubbcluj.cs.map.repository;

import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.domain.validators.ValidationException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class DBFriendshipRepository<ID, E extends Entity<ID>> implements Repository<Long, Friendship> {
    private static Long currentID;
    private String url;
    private String user;
    private String password;
    public DBFriendshipRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        loadCurrentIdAvailable();
    }

    @Override
    public Optional<Friendship> findOne(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships WHERE idfriendship = ?");
        ){
                statement.setLong(1, id);
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    Long id1 = r.getLong("id1");
                    Long id2 = r.getLong("id2");
                    LocalDateTime date = r.getTimestamp("friendsfrom").toLocalDateTime();

                    Friendship fs = new Friendship(id1, id2);
                    fs.setDate(date);
                    fs.setId(id);

                    return Optional.of(fs);
                }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Friendship> findAll() {
        ArrayList<Friendship> list = new ArrayList<>();

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships");
        ){
                ResultSet r = statement.executeQuery();
                while(r.next()){
                    Long id = r.getLong("idfriendship");
                    Long id1 = r.getLong("id1");
                    Long id2 = r.getLong("id2");
                    LocalDateTime friendsFrom = r.getTimestamp("friendsfrom").toLocalDateTime();

                    Friendship fs = new Friendship(id1, id2);
                    fs.setDate(friendsFrom);
                    fs.setId(id);

                    list.add(fs);
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("INSERT INTO friendships(idfriendship, id1, id2, friendsfrom) VALUES (?, ?, ?, ?)");
        ){
                entity.setId(currentID);
                currentID++;

                statement.setLong(1, entity.getId());
                statement.setLong(2, entity.getId1());
                statement.setLong(3, entity.getId2());
                statement.setObject(4, entity.getDate());

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("DELETE FROM friendships WHERE idfriendship = ?");
        ){
                Optional<Friendship> fs = findOne(id);
                statement.setLong(1, id);

                int affectedRows = statement.executeUpdate();
                return  affectedRows == 0 ? Optional.empty() : fs;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        if(entity == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("UPDATE friendships SET friendsfrom = ? WHERE idfriendship = ?");
        ){
                statement.setObject(1, entity.getDate());
                statement.setLong(2,entity.getId());

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

//    private void validatAndThrow(Friendship entity, Consumer<Friendship> validateFunction){
//        try{
//            validateFunction.accept(entity);
//        }
//        catch(ValidationException exception){
//            throw exception;
//        }
//    }

    private void loadCurrentIdAvailable(){
        currentID = 0L;
        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT idfriendship FROM friendships ORDER BY idfriendship DESC LIMIT 1");
        ){
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    currentID = r.getLong("idfriendship");
                    currentID++;
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
