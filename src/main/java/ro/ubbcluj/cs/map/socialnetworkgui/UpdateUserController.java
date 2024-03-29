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

public class UpdateUserController {
    @FXML
    TextField textFieldId;
    @FXML
    TextField textFieldFirstName;
    @FXML
    TextField textFieldLastName;

    UserService userService;
    User user;
    Stage dialogStage;

    public void setUserService(UserService userService, Stage dialogStage, User user){
        this.userService = userService;
        this.dialogStage = dialogStage;
        this.user = user;
        setFields(user);
        textFieldId.setDisable(true);
    }

    private void setFields(User user){
        textFieldId.setText(user.getId().toString());
        textFieldFirstName.setText(user.getFirstName());
        textFieldLastName.setText(user.getLastName());
    }

    @FXML
    public void initialize(){
    }

    @FXML
    public void onPressUpdate(ActionEvent actionEvent) {
        String firstName = textFieldFirstName.getText();
        String lastName = textFieldLastName.getText();

        try {
            this.userService.updateUser(user.getId(), firstName, lastName);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Update", "User updated successfully!");
            dialogStage.close();

        } catch (ValidationException ve) {
            MessageAlert.showErrorMessage(null, ve.getMessage());
        }
    }

    @FXML
    public void onPressCancel(ActionEvent actionEvent) {
        dialogStage.close();
    }
}
