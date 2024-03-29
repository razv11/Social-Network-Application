package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ro.ubbcluj.cs.map.domain.*;
import ro.ubbcluj.cs.map.service.FriendRequestService;
import ro.ubbcluj.cs.map.service.FriendshipService;
import ro.ubbcluj.cs.map.service.MessageService;
import ro.ubbcluj.cs.map.service.UserService;
import ro.ubbcluj.cs.map.utils.observer.Observable;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.FriendRequestChangeEvent;
import ro.ubbcluj.cs.map.utils.observer.events.FriendshipChangeEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OpenUserWindowController implements Observer<FriendRequestChangeEvent> {
    private UserService userService;
    private FriendshipService friendshipService;
    private FriendRequestService friendRequestService;
    private MessageService messageService;
    private User user;
    private Stage stage;

    @FXML
    AnchorPane rootPane;
    @FXML
    TableView<UserDTO> tableViewFriends;
    @FXML
    ObservableList<UserDTO> modelFriends = FXCollections.observableArrayList();
    @FXML
    TableColumn<UserDTO, Long> friendIdColumn;
    @FXML
    TableColumn<UserDTO, String> friendFirstNameColumn;
    @FXML
    TableColumn<UserDTO, String> friendLastNameColumn;
    @FXML
    TableColumn<UserDTO, LocalDateTime> friendsFromColumn;
    @FXML
    TableColumn<UserDTO, FriendshipChangeEvent> friendshipStatusColumn;
    @FXML
    Label userLabel;

    @FXML
    TableView<User> tableViewFriendRequest;
    @FXML
    ObservableList<User> modelUsers = FXCollections.observableArrayList();
    @FXML
    TableColumn userIdColumn;
    @FXML
    TableColumn userFirstNameColumn;
    @FXML
    TableColumn userLastNameColumn;

    @Override
    public void update(FriendRequestChangeEvent o) {
        initModels();
    }

    void setAttributes(UserService userService, FriendshipService friendshipService, FriendRequestService friendRequestService, MessageService messageService, User user, Stage stage){
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.friendRequestService = friendRequestService;
        this.messageService = messageService;
        this.user = user;
        this.stage = stage;

        //this.friendshipService.addObserver(this);
        this.friendRequestService.addObserver(this);
        userLabel.setText("ID: " + this.user.getId() + "  |  First name: " + this.user.getFirstName() + "  |  Last name: " + this.user.getLastName());
        initModels();
    }

    @FXML
    void initialize(){
        friendIdColumn.setCellValueFactory(new PropertyValueFactory<UserDTO, Long>("id"));
        friendFirstNameColumn.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("firstName"));
        friendLastNameColumn.setCellValueFactory(new PropertyValueFactory<UserDTO, String>("lastName"));
        friendsFromColumn.setCellValueFactory(new PropertyValueFactory<UserDTO, LocalDateTime>("friendsFrom"));
        friendshipStatusColumn.setCellValueFactory(new PropertyValueFactory<UserDTO, FriendshipChangeEvent>("status"));
        tableViewFriends.setItems(modelFriends);

        userIdColumn.setCellValueFactory(new PropertyValueFactory<User, Long>("id"));
        userFirstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        userLastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        tableViewFriendRequest.setItems(modelUsers);
    }

    void initModels(){
        List<UserDTO> dtos = new ArrayList<>();

        for(Friendship friendship : this.friendshipService.getFriendships()){
            if(friendship.getId1() == this.user.getId()){
                User currentUser = this.userService.existUser(friendship.getId2()).get();
                UserDTO dto = new UserDTO(currentUser.getFirstName(), currentUser.getLastName(), friendship.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")), FriendshipStatusType.APPROVED);
                dto.setId(friendship.getId2());

                dtos.add(dto);
            }

            if(friendship.getId2() == this.user.getId()){
                User currentUser = this.userService.existUser(friendship.getId1()).get();
                UserDTO dto = new UserDTO(currentUser.getFirstName(), currentUser.getLastName(), friendship.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")), FriendshipStatusType.APPROVED);
                dto.setId(friendship.getId1());

                dtos.add(dto);
            }
        }

        List<User> users = new ArrayList<>();

        for(FriendRequest friendRequest : this.friendRequestService.getFriendRequests()){
            if(friendRequest.getIdReceiver() == this.user.getId() && friendRequest.getStatus() == FriendshipStatusType.PENDING){
                users.add(this.userService.existUser(friendRequest.getIdSender()).get());
            }

            if(friendRequest.getIdSender() == this.user.getId() && friendRequest.getStatus() != FriendshipStatusType.APPROVED){
                User currentUser = this.userService.existUser(friendRequest.getIdReceiver()).get();
                UserDTO dto = new UserDTO(currentUser.getFirstName(), currentUser.getLastName(), null, friendRequest.getStatus());
                dto.setId(friendRequest.getIdReceiver());
                dtos.add(dto);
            }
        }

        modelFriends.setAll(dtos);
        modelUsers.setAll(users);
    }

    public void onPressAdd(ActionEvent actionEvent) {

        try {
            FXMLLoader addFriendshipLoader = new FXMLLoader();
            addFriendshipLoader.setLocation(getClass().getResource("views/user/addFriendship.fxml"));

            AnchorPane root = (AnchorPane) addFriendshipLoader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Add friendship");
            newStage.setResizable(false);
            newStage.initModality(Modality.WINDOW_MODAL);

            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            AddFriendshipController addFriendshipController = addFriendshipLoader.getController();
            addFriendshipController.setAttributes(this.userService, this.friendshipService, this.friendRequestService, this.user, newStage);

            newStage.setMinWidth(400);
            newStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onPressDeleteFriendshipOrRequest(ActionEvent actionEvent) {
        UserDTO dto = tableViewFriends.getSelectionModel().getSelectedItem();
        if(dto == null){
            MessageAlert.showErrorMessage(null, "No friend selected!");
            return;
        }

        if(dto.getStatus() == FriendshipStatusType.APPROVED) {
            this.friendshipService.deleteFriendshipBetweenUsers(this.user.getId(), dto.getId());
        }
        this.friendRequestService.deleteRequestsBetweenSenderAndReceiver(this.user.getId(), dto.getId());
    }

    public void onPressAccept(ActionEvent actionEvent) {
        User selectedUser = tableViewFriendRequest.getSelectionModel().getSelectedItem();
        if(selectedUser == null){
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        this.friendshipService.addFriendship(this.user.getId(), selectedUser.getId());
        Long friendRequestId = this.friendRequestService.getFriendRequestId(selectedUser.getId(), this.user.getId());
        this.friendRequestService.update(friendRequestId, selectedUser.getId(), this.user.getId(), FriendshipStatusType.APPROVED);
    }

    public void onPressReject(ActionEvent actionEvent) {
        User selectedUser = tableViewFriendRequest.getSelectionModel().getSelectedItem();
        if(selectedUser == null){
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        Long friendRequestId = this.friendRequestService.getFriendRequestId(selectedUser.getId(), this.user.getId());
        this.friendRequestService.update(friendRequestId, selectedUser.getId(), this.user.getId(), FriendshipStatusType.REJECTED);
    }

    public void onPressExit(ActionEvent actionEvent) {
        this.stage.close();
    }

    public void onPressMessages(ActionEvent actionEvent) {
        try{
            FXMLLoader messagesLoader = new FXMLLoader();
            messagesLoader.setLocation(getClass().getResource("views/conversations-view.fxml"));

            AnchorPane root = (AnchorPane) messagesLoader.load();

            Stage newStage = new Stage();
            newStage.setTitle("Conversations");
            newStage.initModality(Modality.WINDOW_MODAL);

            Scene newScene = new Scene(root);
            newStage.setScene(newScene);

            ConversationController conversationController = messagesLoader.getController();
            conversationController.setAttributes(this.userService, this.messageService, this.user, newStage);

            newStage.setMinWidth(800);
            newStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
