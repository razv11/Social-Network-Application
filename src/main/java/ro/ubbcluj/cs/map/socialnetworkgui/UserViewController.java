package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import ro.ubbcluj.cs.map.repository.PagingRepo.Page;
import ro.ubbcluj.cs.map.repository.PagingRepo.Pageable;
import ro.ubbcluj.cs.map.service.*;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.utils.observer.Observer;
import ro.ubbcluj.cs.map.utils.observer.events.UserChangeEvent;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UserViewController implements Observer<UserChangeEvent> {
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalNumberOfElements = 0;
    private LoginService loginService;
    private UserService userService;
    private FriendshipService friendshipService;
    private FriendRequestService friendRequestService;
    private MessageService messageService;
    ObservableList<User> model = FXCollections.observableArrayList();
    @FXML
    Button previousButton;
    @FXML
    Button nextButton;
    @FXML
    TextField pageSizeField;

    @FXML
    TableView<User> userTableView;
    @FXML
    TableColumn<User, Long> userIdColumn;
    @FXML
    TableColumn<User, String> userFirstNameColumn;
    @FXML
    TableColumn<User, String> userLastNameColumn;
    @FXML
    TextField textFieldSearch;
    Stage mainStage;

    @Override
    public void update(UserChangeEvent o) {
        initModel();
    }

    public void setAttributes(LoginService loginService, UserService userService, FriendshipService friendshipService, FriendRequestService friendRequestService, MessageService messageService, Stage mainStage) {
        this.loginService = loginService;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.friendRequestService = friendRequestService;
        this.messageService = messageService;
        this.mainStage = mainStage;

        this.userService.addObserver(this);
        initModel();
    }

    @FXML
    public void initialize() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<User, Long>("id"));
        userFirstNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("firstName"));
        userLastNameColumn.setCellValueFactory(new PropertyValueFactory<User, String>("lastName"));
        userTableView.setItems(model);

        userTableView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2){
                this.onPressOpen(new ActionEvent());
            }
        });

        pageSizeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int newPageSize = 0;
                try{
                    newPageSize = Integer.parseInt(newValue);
                    if(newPageSize > 0) {
                        pageSize = newPageSize;
                        initModel();
                    }
                }
                catch (NumberFormatException ne){
                    return;
                }
            }
        });
    }

    private void initModel(){
        Page<User> userPerPage = this.userService.findAll(new Pageable(currentPage, pageSize));

        int maxPage = (int) Math.ceil((double) userPerPage.getTotalElementsCount() / pageSize) - 1;
        if(currentPage > maxPage){
            currentPage = maxPage;
            userPerPage = this.userService.findAll(new Pageable(currentPage, pageSize));
        }

        this.model.setAll(StreamSupport.stream(userPerPage.getElementsOnPage().spliterator(), false).collect(Collectors.toList()));
        this.totalNumberOfElements = userPerPage.getTotalElementsCount();

        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable( (currentPage + 1) * pageSize >= totalNumberOfElements);
        pageSizeField.setText(Integer.toString(this.pageSize));
    }

    @FXML
    public void onPressDeleteUser(ActionEvent actionEvent) {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            this.loginService.deleteLogin(selectedUser.getId());
            this.messageService.deleteMessageForUser(selectedUser.getId());
            this.friendshipService.deleteFriendshipsByUserId(selectedUser.getId());

            Optional<User> user = this.userService.deleteUser(selectedUser.getId());
            if (user.isPresent()) {
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Delete", "User removed successfully");
            }
        } else {
            MessageAlert.showErrorMessage(null, "No user selected!");
        }
    }

    @FXML
    public void onPressAddUser(ActionEvent actionEvent) {
        showUserEditDialog(null);
    }

    public void showUserEditDialog(User user) {
        try {
            // create a new stage for the popup dialog
            FXMLLoader editLoader = new FXMLLoader();
            editLoader.setLocation(getClass().getResource("views/user/edituser-view.fxml"));

            AnchorPane root = (AnchorPane) editLoader.load();

            // create the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit user");
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            EditUserController editUserController = editLoader.getController();
            editUserController.setUserService(this.userService, dialogStage, user);

            dialogStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onPressUpdateUser(ActionEvent actionEvent) {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if(selectedUser == null){
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;

        }

        try{
            FXMLLoader updateLoader = new FXMLLoader();
            updateLoader.setLocation(getClass().getResource("views/user/updateuser-view.fxml"));

            AnchorPane root = (AnchorPane) updateLoader.load();

            // create the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit user");
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            UpdateUserController updateUserController = updateLoader.getController();
            updateUserController.setUserService(this.userService, dialogStage, selectedUser);

            dialogStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onPressSearch(ActionEvent actionEvent) {
        String name = textFieldSearch.getText();
        if(name == ""){
            MessageAlert.showErrorMessage(null, "No name added!");
            return;
        }

        Iterable<User> users = this.userService.getUsersWithName(name);
        List<User> userList = StreamSupport.stream(users.spliterator(), false)
                        .collect(Collectors.toList());
        model.setAll(userList);
    }

    @FXML
    public void onPressOpen(ActionEvent actionEvent) {
        User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if(selectedUser == null){
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        try{
            FXMLLoader userOpenWindow = new FXMLLoader();
            userOpenWindow.setLocation(getClass().getResource("views/openUserWindow.fxml"));

            AnchorPane root = (AnchorPane) userOpenWindow.load();

            // create new stage
            Stage newStage = new Stage();
            newStage.setTitle(selectedUser.getFirstName() + " " + selectedUser.getLastName());
            newStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            newStage.setScene(scene);

            OpenUserWindowController openUserWindow = userOpenWindow.getController();
            openUserWindow.setAttributes(this.userService, this.friendshipService,  this.friendRequestService, this.messageService,  selectedUser, newStage);

            newStage.setMinWidth(1000);
            newStage.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onPressViewMessages(ActionEvent actionEvent) {

        try {
            FXMLLoader viewMessagesLoader = new FXMLLoader();
            viewMessagesLoader.setLocation(getClass().getResource("views/user/showmessages-view.fxml"));

            AnchorPane root = (AnchorPane) viewMessagesLoader.load();

            Stage newStage = new Stage();
            newStage.setTitle("View conversation");
            newStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            newStage.setScene(scene);

            ViewMessagesController viewMessagesController = viewMessagesLoader.getController();
            viewMessagesController.setAttributes(this.userService, this.messageService);

            newStage.setMinWidth(800);
            newStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    public void onButtonPressNext(ActionEvent actionEvent) {
        currentPage++;
        initModel();
    }

    @FXML
    public void onButtonPressPrevious(ActionEvent actionEvent) {
        currentPage--;
        initModel();
    }

    @FXML
    public void onPressRefreshUser(ActionEvent actionEvent) {
        initModel();
    }

    @FXML
    public void onPressExit(ActionEvent actionEvent) {
        mainStage.close();
    }
}
