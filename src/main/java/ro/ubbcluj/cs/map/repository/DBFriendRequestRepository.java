package ro.ubbcluj.cs.map.repository;

import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.domain.FriendRequest;
import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.domain.FriendshipStatusType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.PropertyResourceBundle;

public class DBFriendRequestRepository<ID, E extends Entity<ID>> implements Repository<Long, FriendRequest> {
    private static Long currentID;
    private String url;
    private String username;
    private String password;

    public DBFriendRequestRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        loadCurrentIdAvailable();
    }

    @Override
    public Optional<FriendRequest> findOne(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendrequests WHERE idrequest = ?");
        ){
                statement.setLong(1, id);
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    Long id1 = r.getLong("id1");
                    Long id2 = r.getLong("id2");
                    String status = r.getString("status");

                    FriendshipStatusType statusType = FriendshipStatusType.APPROVED;
                    if(status.equalsIgnoreCase("PENDING")){
                        statusType = FriendshipStatusType.PENDING;
                    }
                    if(status.equalsIgnoreCase("REJECTED")){
                        statusType = FriendshipStatusType.REJECTED;
                    }

                    FriendRequest friendRequest = new FriendRequest(id1, id2, statusType);
                    friendRequest.setId(id);

                    return Optional.of(friendRequest);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        ArrayList<FriendRequest> friendRequests = new ArrayList<>();
        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendrequests");
        ){
                ResultSet r = statement.executeQuery();
                while (r.next()){
                    Long id = r.getLong("idrequest");
                    Long id1 = r.getLong("id1");
                    Long id2 = r.getLong("id2");
                    String status = r.getString("status");

                    FriendshipStatusType statusType = FriendshipStatusType.APPROVED;
                    if(status.equalsIgnoreCase("PENDING")) {
                        statusType = FriendshipStatusType.PENDING;
                    }
                    if(status.equalsIgnoreCase("REJECTED")){
                        statusType = FriendshipStatusType.REJECTED;
                    }

                    FriendRequest friendRequest = new FriendRequest(id1, id2, statusType);
                    friendRequest.setId(id);

                    friendRequests.add(friendRequest);
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return friendRequests;
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("INSERT INTO friendrequests(idrequest, id1, id2, status) VALUES (?, ?, ?, ?)");
        ){
                entity.setId(currentID);
                currentID++;

                statement.setLong(1, entity.getId());
                statement.setLong(2, entity.getIdSender());
                statement.setLong(3, entity.getIdReceiver());
                statement.setString(4, entity.getStatus().toString());

                int affectedRows = statement.executeUpdate();

                return  affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("DELETE FROM friendrequests WHERE idrequest = ?");
        ){
                Optional<FriendRequest> friendRequest = this.findOne(id);

                statement.setLong(1, id);
                int affectedRows = statement.executeUpdate();

                return affectedRows == 0 ? Optional.empty() : friendRequest;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        if (entity == null){
            throw new IllegalArgumentException();
        }

        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("UPDATE friendrequests SET status = ? WHERE idrequest = ?")
        ){
                statement.setString(1, entity.getStatus().toString());
                statement.setLong(2, entity.getId());

                int affectedRows = statement.executeUpdate();

                return affectedRows == 0 ? Optional.of(entity) : Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCurrentIdAvailable(){
        currentID = 0L;

        try(
                Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement("SELECT idrequest FROM friendrequests ORDER BY idrequest DESC LIMIT 1");
        ){
                ResultSet r = statement.executeQuery();
                if(r.next()){
                    currentID = r.getLong("idrequest");
                    currentID++;
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
