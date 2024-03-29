package ro.ubbcluj.cs.map.repository;

import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.domain.Login;

import java.sql.*;
import java.util.Collections;
import java.util.Optional;

public class DBLoginRepository<ID, E extends Entity<ID>> {
    private String url;
    private String username;
    private String password;

    public DBLoginRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Optional<Login> findOne(String email) {
        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM logins WHERE email = ?")
        ){
                statement.setString(1, email);
                ResultSet r = statement.executeQuery();

                if(r.next()){
                    Long idUser = r.getLong("iduser");
                    String encryptedPassword = r.getString("encryptedpassword");

                    Login login = new Login(email, encryptedPassword);
                    login.setId(idUser);

                    return Optional.of(login);
                }

                return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Login> findOne(Long id) {
        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM logins WHERE iduser = ?")
        ){
            statement.setLong(1, id);
            ResultSet r = statement.executeQuery();

            if(r.next()){
                String email = r.getString("email");
                String encryptedPassword = r.getString("encryptedpassword");

                Login login = new Login(email, encryptedPassword);
                login.setId(id);

                return Optional.of(login);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Login> save(Login entity){
        if(entity == null){
            throw new IllegalArgumentException("The entity must not be null");
        }

        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("INSERT INTO logins(iduser, email, encryptedpassword) VALUES (?, ?, ?)");
        ) {
                statement.setLong(1, entity.getId());
                statement.setString(2, entity.getEmail());
                statement.setString(3, entity.getEncryptedPassword());

                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Login> delete(Long id){
        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("DELETE FROM logins WHERE iduser = ?");
        ){
                statement.setLong(1, id);
                int affectedRows = statement.executeUpdate();
                Optional<Login> login = this.findOne(id);

                return affectedRows == 0 ? Optional.empty() : login;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
