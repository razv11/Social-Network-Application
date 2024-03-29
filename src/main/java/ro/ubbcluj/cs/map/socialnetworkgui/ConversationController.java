package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ro.ubbcluj.cs.map.domain.Message;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.service.MessageService;
import ro.ubbcluj.cs.map.service.UserService;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.MessageChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConversationController implements Observer<MessageChangeEvent> {
    private UserService userService;
    private MessageService messageService;
    private User user;
    private Stage stage;
    ObservableList<User> usersModel = FXCollections.observableArrayList();
    ObservableList<Message> conversationModel = FXCollections.observableArrayList();
    @FXML
    TableView<User> tableViewUsers;
    @FXML
    TableColumn<User, Long> userIdColumn;
    @FXML
    TableColumn<User, String> userFirstNameColumn;
    @FXML
    TableColumn<User, String> userLastNameColumn;
    @FXML
    TableColumn<User, Boolean> userSelectedColumn;
    @FXML
    ListView<Message> listViewConversation;
    @FXML
    TextField typeMessageField;

    public void setAttributes(UserService userService, MessageService messageService, User user, Stage stage) {
        this.userService = userService;
        this.messageService = messageService;
        this.user = user;
        this.stage = stage;

        this.messageService.addObserver(this);
        init_model_for_users();
    }

    @Override
    public void update(MessageChangeEvent o) {
        this.init_model_for_conversation();
    }

    @FXML
    void initialize() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<User, Long>("id"));
        userFirstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        userLastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        userSelectedColumn.setCellValueFactory(new PropertyValueFactory<User, Boolean>("selected"));
        userSelectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(userSelectedColumn));

        tableViewUsers.getSelectionModel().setCellSelectionEnabled(true);
        tableViewUsers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewConversation.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);

        tableViewUsers.setItems(usersModel);

        tableViewUsers.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1){
                // single click
                init_model_for_conversation();
            }
        });

        typeMessageField.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER){
                this.onPressSendMessage(new ActionEvent());
            }
        });
    }
    public void init_model_for_users() {
        Iterable<User> users = this.userService.getUsers();
        List<User> userList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        userList.remove(this.user);
        usersModel.setAll(userList);
    }

    public void init_model_for_conversation() {
        if(user == null || tableViewUsers.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        User selectedUser = tableViewUsers.getSelectionModel().getSelectedItem();
        tableViewUsers.getItems().forEach(user1 -> user1.setSelected(false));
        selectedUser.setSelected(true);

        listViewConversation.scrollTo(conversationModel.size() - 1);

        List<Message> messages = this.messageService.getOrderedMessagesBetweenUsers(this.user, selectedUser);

        for(Message message : messages){
            String extraInfo = "";
            if(message.getFrom().getId() == this.user.getId()) {
                if (message.getReply()  != null) {
                    extraInfo += "You (" + message.getId().toString() + ") replying to (" + message.getReply().getId().toString() + "): ";
                } else {
                    extraInfo += "You (" + message.getId().toString() + "): ";
                }
            }
            else {
                if (message.getReply() != null) {
                    extraInfo += selectedUser.getFirstName() + " (" + message.getId().toString() + ") replying to (" + message.getReply().getId() + "): ";
                } else {
                    extraInfo += selectedUser.getFirstName() + " (" + message.getId().toString() + "): ";
                }
            }
            message.setMessage(extraInfo + message.getMessage());
        }

        conversationModel.setAll(messages);
        listViewConversation.setItems(conversationModel);

    }

    public void onPressSendMessage(ActionEvent actionEvent) {
        if(tableViewUsers.getSelectionModel().getSelectedItem() == null){
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        User selectedUser = tableViewUsers.getSelectionModel().getSelectedItem();
        List<User> to = new ArrayList<>();
        for(User user : tableViewUsers.getItems()){
            if(user.isSelected()){
                to.add(user);
            }
        }

        String message = typeMessageField.getText();
        if(message == ""){
            return;
        }

        this.messageService.addMessage(this.user, to, message);
        tableViewUsers.getItems().forEach(user1 -> user1.setSelected(false));
        selectedUser.setSelected(true);
        typeMessageField.setText("");
        typeMessageField.setPromptText("Type message or reply");
    }

    public void onPressReply(ActionEvent actionEvent) {
        if(listViewConversation.getSelectionModel().getSelectedItem() == null){
            return;
        }

        Message selectedMessage = listViewConversation.getSelectionModel().getSelectedItem();
        String replyText = typeMessageField.getText().toString();

        List<User> receiver = new ArrayList<>();
        receiver.add(selectedMessage.getFrom());
        this.messageService.addReply(this.user, receiver, replyText, selectedMessage);
        typeMessageField.setText("");
        typeMessageField.setPromptText("Type message or reply");
    }

    public void onPressExit(ActionEvent actionEvent) {
        this.stage.close();
    }
}
