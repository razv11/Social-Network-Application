package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ro.ubbcluj.cs.map.domain.*;
import ro.ubbcluj.cs.map.domain.validators.UserValidator;
import ro.ubbcluj.cs.map.repository.*;
import ro.ubbcluj.cs.map.service.*;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private final String url = "jdbc:postgresql://localhost:5432/socialnetwork";
    private final String username = "postgres";
    private final String password = "PostgresSQL11";
    private DBLoginRepository<Long, Login> loginRepo;
    private LoginService loginService;
    private DBUserRepository<Long, User> userRepo;
    private UserService userService;
    private DBFriendshipRepository<Long, Friendship> friendshipRepo;
    private FriendshipService friendshipService;
    private DBFriendRequestRepository<Long, FriendRequest> friendRequestRepository;
    private FriendRequestService friendRequestService;
    private DBMessageRepository<Long, Message> messageRepo;
    private MessageService messageService;
    @FXML
    private Stage mainStage;
    @FXML
    private Scene scene;
    @FXML
    private Parent root;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailReigsterField;
    @FXML
    private PasswordField passwordRegisterField;
    @FXML
    private PasswordField passwordReTypeField;
    @FXML
    Button loginButton;


    @FXML
    public void initialize(){
        this.loginRepo = new DBLoginRepository<>(url, username, password);
        this.loginService = new LoginService(loginRepo);

        this.userRepo = new DBUserRepository<>(url, username, password, new UserValidator());
        this.userService = new UserService(userRepo);

        this.friendshipRepo = new DBFriendshipRepository<>(url, username, password);
        this.friendshipService = new FriendshipService(friendshipRepo);

        this.friendRequestRepository =  new DBFriendRequestRepository<>(url, username, password);
        this.friendRequestService = new FriendRequestService(friendRequestRepository);

        this.messageRepo = new DBMessageRepository<>(url, username, password, this.userRepo);
        this.messageService = new MessageService(messageRepo);

        if(loginService.findOne("admin").isEmpty()) {
            Optional<User> user = userService.addUser("admin", "admin");
            user.ifPresent(value -> loginService.addLogin(value.getId(), "admin", "admin"));
        }
    }

    public void onPressRegister(ActionEvent event) throws IOException {
        this.root = FXMLLoader.load(getClass().getResource("views/registration-view.fxml"));
        this.mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        this.scene = new Scene(root);
        this.mainStage.setScene(scene);
        this.mainStage.show();
    }

    public void onPressBack(ActionEvent event) throws IOException {
        this.root = FXMLLoader.load(getClass().getResource("views/login-view.fxml"));
        this.mainStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        this.scene = new Scene(root);
        this.mainStage.setScene(scene);
        this.mainStage.show();
    }

    public void onPressLogin(ActionEvent event) throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        if(email == "" || password == ""){
            return;
        }

        Optional<Login> login = this.loginService.tryLogin(email, password);
        if(login.isEmpty()){
            MessageAlert.showErrorMessage(null, "Invalid e-mail or password!");
            return;
        }

        if(email.equals("admin")){
            FXMLLoader adminLoader = new FXMLLoader();
            adminLoader.setLocation(getClass().getResource("views/user-view.fxml"));

            AnchorPane root = adminLoader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            UserViewController userViewController = adminLoader.getController();
            userViewController.setAttributes(loginService, userService, friendshipService, friendRequestService, messageService, newStage);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
            newStage.show();
            return;
        }


        User currentUser = userService.existUser(login.get().getId()).get();

        FXMLLoader userLoader = new FXMLLoader();
        userLoader.setLocation(getClass().getResource("views/openUserWindow.fxml"));

        AnchorPane userLayout = userLoader.load();
        Stage newStage = new Stage();
        newStage.setTitle(currentUser.getFirstName() + " " + currentUser.getLastName());
        newStage.setScene(new Scene(userLayout));

        OpenUserWindowController openUserWindow = userLoader.getController();
        openUserWindow.setAttributes(this.userService, this.friendshipService,  this.friendRequestService, this.messageService, currentUser, newStage);

        newStage.setMinWidth(1000);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
        newStage.show();
    }

    public void onPressSignUp(ActionEvent event) {
        String firstName = firstNameField.getText();
        if(firstName.equals("")){
            MessageAlert.showErrorMessage(null, "First name must not be null!");
            return;
        }

        String lastName = lastNameField.getText();
        if(lastName.equals("")){
            MessageAlert.showErrorMessage(null, "Last name must not be null!");
            return;
        }

        String email = emailReigsterField.getText();
        if(email.equals("")){
            MessageAlert.showErrorMessage(null, "E-mail must not be null!");
            return;
        }

        if(this.loginService.findOne(email).isPresent() || email.equals("admin")){
            MessageAlert.showErrorMessage(null, "This e-mail or username have already an account associated!");
            return;
        }

        if(email.length() < 3){
            MessageAlert.showErrorMessage(null, "This e-mail or username is too short!");
            return;
        }

        String password = passwordRegisterField.getText();
        String retypedPassword = passwordReTypeField.getText();

        if(!password.equals(retypedPassword)){
            MessageAlert.showErrorMessage(null, "The password doesn't correspond!");
            return;
        }

        Optional<User> newUser = this.userService.addUser(firstName, lastName);
        this.loginService.addLogin(newUser.get().getId(), email, password);

        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Info","Account created successfully!");

        firstNameField.deleteText(0, firstName.length());
        lastNameField.deleteText(0, lastName.length());
        emailReigsterField.deleteText(0, email.length());
        passwordRegisterField.deleteText(0, password.length());
        passwordReTypeField.deleteText(0, retypedPassword.length());
    }

    public void onPressCancel(ActionEvent event) {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}
