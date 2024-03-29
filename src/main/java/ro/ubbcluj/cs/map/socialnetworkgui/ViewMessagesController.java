package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import ro.ubbcluj.cs.map.domain.Message;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.service.MessageService;
import ro.ubbcluj.cs.map.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ViewMessagesController {
    private UserService userService;
    private MessageService messageService;

    @FXML
    ComboBox<User> firstUserComboBox;
    @FXML
    ComboBox<User> secondUserComboBox;
    @FXML
    ListView<Message> listViewConversation;
    ObservableList<Message> conversationModel = FXCollections.observableArrayList();

    public void setAttributes(UserService userService, MessageService messageService){
        this.userService = userService;
        this.messageService = messageService;

        this.init_ComboBoxes();
    }

    @FXML
    public void initialize(){
    }

    void init_ComboBoxes(){
        Iterable<User> userList = this.userService.getUsers();

        List<User> users = StreamSupport.stream(userList.spliterator(), false)
                .collect(Collectors.toList());

        firstUserComboBox.getItems().setAll(users);
        secondUserComboBox.getItems().setAll(users);
    }

    public void onPressShowMessages(ActionEvent actionEvent) {
        if(firstUserComboBox.getSelectionModel().getSelectedItem() == null || secondUserComboBox.getSelectionModel().getSelectedItem() == null){
            MessageAlert.showErrorMessage(null, "Select users first!");
            return;
        }

        User firstUser = firstUserComboBox.getSelectionModel().getSelectedItem();
        User secondUser = secondUserComboBox.getSelectionModel().getSelectedItem();

        if(firstUser.getId() == secondUser.getId()){
            MessageAlert.showErrorMessage(null, "Users cannot be the same!");
            return;
        }

        List<Message> messages = this.messageService.getOrderedMessagesBetweenUsers(firstUser, secondUser);

        for(Message message : messages){
            String extraInfo = "";
            if(message.getFrom().getId() == firstUser.getId()) {
                if (message.getReply()  != null) {
                    extraInfo += firstUser.getFirstName() + " (" + message.getId().toString() + ") replied to " + secondUser.getFirstName() + " (" + message.getReply().getId().toString() + "): ";
                } else {
                    extraInfo += firstUser.getFirstName() + " (" + message.getId().toString() + "): ";
                }
            }
            else {
                if (message.getReply() != null) {
                    extraInfo += secondUser.getFirstName() + " (" + message.getId().toString() + ") replied to " + firstUser.getFirstName() + " (" + message.getReply().getId() + "): ";
                } else {
                    extraInfo += secondUser.getFirstName() + " (" + message.getId().toString() + "): ";
                }
            }
            message.setMessage(extraInfo + message.getMessage());
        }

        conversationModel.setAll(messages);
        listViewConversation.setItems(conversationModel);
    }
}
