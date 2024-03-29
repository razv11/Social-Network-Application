package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.cs.map.domain.FriendRequest;
import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.domain.FriendshipStatusType;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.service.FriendRequestService;
import ro.ubbcluj.cs.map.service.FriendshipService;
import ro.ubbcluj.cs.map.service.UserService;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.UserChangeEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AddFriendshipController implements Observer<UserChangeEvent> {
    private UserService userService;
    private FriendshipService friendshipService;
    private FriendRequestService friendRequestService;
    private User user;
    private Stage stage;

    @FXML
    TextField userIdTextField;
    @FXML
    ComboBox comboBoxUsers;

    @Override
    public void update(UserChangeEvent o) {
        init_comboBox();
    }

    void setAttributes(UserService userService, FriendshipService friendshipService, FriendRequestService friendRequestService, User user, Stage stage){
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.friendRequestService = friendRequestService;
        this.user = user;
        this.stage = stage;

        this.userService.addObserver(this);
        userIdTextField.setText(this.user.getId().toString());
        init_comboBox();
    }

    void init_comboBox(){
        Iterable<User> userList = this.userService.getUsers();

        List<User> users = StreamSupport.stream(userList.spliterator(), false)
                .collect(Collectors.toList());
        users.remove(this.user);

        Iterable<Friendship> friendships = this.friendshipService.getFriendships();
        for(Friendship friendship : friendships){
            if(friendship.getId1() == this.user.getId()){
                users.remove(this.userService.existUser(friendship.getId2()).get());
            }
            if(friendship.getId2() == this.user.getId()){
                users.remove(this.userService.existUser(friendship.getId1()).get());
            }
        }

        Iterable<FriendRequest> friendRequests = this.friendRequestService.getFriendRequests();
        for(FriendRequest friendRequest : friendRequests){
            if(friendRequest.getIdSender() == this.user.getId()){
                users.remove(this.userService.existUser(friendRequest.getIdReceiver()).get());
            }
            if(friendRequest.getIdReceiver() == this.user.getId()){
                users.remove(this.userService.existUser(friendRequest.getIdSender()).get());
            }
        }

        if(users.size() == 0){
            comboBoxUsers.setPromptText("You have already sent request to all users");
            return;
        }

        comboBoxUsers.setPromptText("Select user");
        comboBoxUsers.getItems().setAll(users);
    }

    public void onPressAddFriendship(ActionEvent actionEvent) {
        if(comboBoxUsers.getSelectionModel().getSelectedItem() == null){
            MessageAlert.showErrorMessage(null,"No user selected!");
            return;
        }

        String selectedUser = comboBoxUsers.getSelectionModel().getSelectedItem().toString();

        int startIndex = selectedUser.lastIndexOf("ID: ");
        Long id = Long.parseLong(selectedUser.substring(startIndex + 4).trim());

        User selUser = this.userService.existUser(id).get();
        this.friendRequestService.addFriendRequest(this.user.getId(), selUser.getId(), FriendshipStatusType.PENDING);

        init_comboBox();
        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add", "Request sent successfully!");
    }

    public void onPressCancel(ActionEvent actionEvent) {
        this.stage.close();
    }
}
