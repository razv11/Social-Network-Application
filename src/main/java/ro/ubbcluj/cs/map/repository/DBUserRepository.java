package ro.ubbcluj.cs.map.repository;

import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.domain.validators.UserValidator;
import ro.ubbcluj.cs.map.domain.validators.ValidationException;
import ro.ubbcluj.cs.map.domain.validators.Validator;
import ro.ubbcluj.cs.map.repository.PagingRepo.Page;
import ro.ubbcluj.cs.map.repository.PagingRepo.Pageable;
import ro.ubbcluj.cs.map.repository.PagingRepo.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

public class DBUserRepository<ID, E extends Entity<ID>> implements PagingRepository<Long, User> {
    private static Long currentID;
    private Validator<User> validator;
    private String url;
    private String user;
    private String password;
    public DBUserRepository(String url, String user, String password, Validator validator) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.validator = validator;
        loadCurrentIdAvailable();
    }

    @Override
    public Optional<User> findOne(Long id) {
        if(id == null) {
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id=?");
        ){
                statement.setLong(1, id);
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    String first_name = r.getString("First_Name");
                    String last_name = r.getString("Last_Name");

                    User user = new User(first_name, last_name);
                    user.setId(id);

                    return Optional.of(user);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        ArrayList<User> list = new ArrayList<>();

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id > 0");
        ){
                ResultSet r = statement.executeQuery();
                while(r.next()){
                    Long id = r.getLong("id");
                    String first_name = r.getString("First_Name");
                    String last_name = r.getString("Last_Name");

                    User user = new User(first_name, last_name);
                    user.setId(id);

                    list.add(user);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        ArrayList<User> users = new ArrayList<>();

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement pagePreparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id > 0 LIMIT ? OFFSET ?");
                PreparedStatement countPreparedStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM users WHERE id > 0");
        ){

                pagePreparedStatement.setLong(1, pageable.getPageSize());
                pagePreparedStatement.setLong(2, pageable.getPageSize() * pageable.getPageNumber());
                try(
                        ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                        ResultSet countResultSet = countPreparedStatement.executeQuery();
                ){
                         while(pageResultSet.next()){
                            Long id = pageResultSet.getLong("id");
                            String first_name = pageResultSet.getString("First_Name");
                            String last_name = pageResultSet.getString("Last_Name");

                            User user = new User(first_name, last_name);
                            user.setId(id);

                            users.add(user);
                        }
                        int totalCount = 0;
                         if(countResultSet.next()){
                             totalCount = countResultSet.getInt("count");
                         }

                         return new Page<>(users, totalCount);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> save(User entity) {
        validateAndThrow(entity, validator::validate);
        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement= connection.prepareStatement("INSERT INTO users(id, first_name, last_name) VALUES (?,?,?)")
        ){
                entity.setId(currentID);
                currentID++;
                statement.setLong(1, entity.getId());
                statement.setString(2, entity.getFirstName());
                statement.setString(3, entity.getLastName());

                int affectedRows = statement.executeUpdate();
                // ?!
                return affectedRows != 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?");
        ){
                statement.setLong(1, id);
                Optional<User> user = findOne(id);

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.empty() : user;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        if(entity == null){
            throw new IllegalArgumentException();
        }

        validateAndThrow(entity, validator::validate);
        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("UPDATE users SET first_name = ?, last_name = ? WHERE id = ?");
        ){
                statement.setString(1, entity.getFirstName());
                statement.setString(2, entity.getLastName());
                statement.setLong(3, entity.getId());

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void validateAndThrow(User entity, Consumer<User> validationFunction){
        try{
            validationFunction.accept(entity);
        }
        catch(ValidationException exception){
            throw exception;
        }
    }

    private void loadCurrentIdAvailable(){
        currentID = 0L;
        try(
                Connection connection = DriverManager.getConnection(url, user, password);
                PreparedStatement statement = connection.prepareStatement("SELECT id FROM users ORDER BY id DESC LIMIT 1");
        ){
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    currentID = r.getLong("id");
                    currentID++;
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
