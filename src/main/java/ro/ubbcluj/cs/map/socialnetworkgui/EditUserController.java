package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.domain.validators.ValidationException;
import ro.ubbcluj.cs.map.service.UserService;
import ro.ubbcluj.cs.map.socialnetworkgui.MessageAlert;

public class EditUserController {
    @FXML
    TextField textFieldFirstName;
    @FXML
    TextField textFieldLastName;

    UserService userService;
    User user;
    Stage dialogStage;

    @FXML
    public void initialize(){
    }

    public void setUserService(UserService userService, Stage dialogStage, User user){
        this.userService = userService;
        this.dialogStage = dialogStage;
        if(user != null){
            setFields(user);
        }
    }

    private void setFields(User user){
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }

    @FXML
    public void onPressSave(ActionEvent actionEvent) {
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldLastName.getText();

        try{
            this.userService.addUser(firstName, lastName);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add", "User added successfully!");
            dialogStage.close();
        }
        catch (ValidationException ve){
            MessageAlert.showErrorMessage(null, ve.getMessage());
        }
    }

    @FXML
    public void onPressCancel(ActionEvent actionEvent) {
        dialogStage.close();
    }
}
