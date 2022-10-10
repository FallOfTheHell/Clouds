package com.example.geekcloudclient;

import SQL.DataBase;
import animation.Shake;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SingUpController {
    public TextField login;
    public PasswordField pass;

    public TextField name;
    public TextField lastname;
    public TextField username;
    public TextField password;


    public void loginClient(ActionEvent actionEvent) throws SQLException {
        String loginText = login.getText().trim();
        String passText = pass.getText();
        if (!loginText.equals("") && !passText.equals("")){
            loginUser(loginText, passText, actionEvent);
        } else {
            System.out.println("Login and password is empty");
        }
    }

    private void loginUser(String loginText, String passText, ActionEvent ae) throws SQLException {
        DataBase dataBase = new DataBase();
        dataBase.getUser(loginText, passText);
        ResultSet result = dataBase.getUser(loginText, passText);
        int counter = 0;
        while (result.next()){
            counter++;
        }
        if (counter >= 1){
            System.out.println("Success!");
            ((Stage)(((Button)ae.getSource()).getScene().getWindow())).close();
            openNewScene("geek-cloud-client.fxml");
        } else {
            System.out.println("User not registered");
            Shake userLoginAnim = new Shake(login);
            Shake userPassAnim = new Shake(pass);
            userLoginAnim.playAnim();
            userPassAnim.playAnim();
        }
    }

    public void registerClient(ActionEvent actionEvent) throws SQLException {
        openNewScene("registration.fxml");
    }
    public void register(ActionEvent actionEvent) {
        DataBase dataBase = new DataBase();
        dataBase.singUpUser(username.getText(),password.getText(), name.getText(), lastname.getText());
        ((Stage)(((Button)actionEvent.getSource()).getScene().getWindow())).close();
    }

    private void openNewScene(String window) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(window));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.showAndWait();
        stage.setTitle("Cloud client!");
    }

}

