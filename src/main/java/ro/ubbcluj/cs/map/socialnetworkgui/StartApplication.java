package ro.ubbcluj.cs.map.socialnetworkgui;

import javafx.scene.layout.AnchorPane;
import ro.ubbcluj.cs.map.domain.*;
import ro.ubbcluj.cs.map.domain.validators.UserValidator;
import ro.ubbcluj.cs.map.repository.*;
import ro.ubbcluj.cs.map.service.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        initView(stage);
        stage.setTitle("Social Network");
        stage.setWidth(650);
        stage.setHeight(450);
        stage.setResizable(false);
        stage.show();
    }

    public void initView(Stage stage) throws IOException {
        FXMLLoader loginLoader = new FXMLLoader();
        loginLoader.setLocation(getClass().getResource("views/login-view.fxml"));
        AnchorPane loginPane = loginLoader.load();
        stage.setScene(new Scene(loginPane));
    }

    public static void main(String[] args) {
        launch(args);
    }
}