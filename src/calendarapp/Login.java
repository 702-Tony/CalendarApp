/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 *
 * @author Anthony Adams
 */
public class Login implements Initializable {
    
    @FXML
    private Label label;
    @FXML
    private Button login;
    @FXML
    private Insets x1;
    @FXML
    private Button cancel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label userLabel;
    @FXML
    private Label passLabel;
    
    ResourceBundle rb;
    // Input Resource Bundle
    private String errorString;
    private Logger log;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // initialize
        // create logger for tracking logins
        log = Logger.getLogger("log.txt");
        try {
            FileHandler fileHandler = new FileHandler("log.txt", true);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            log.addHandler(fileHandler);
        } catch (IOException ex) {
            System.out.print(ex);
        } catch (SecurityException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        log.setLevel(Level.INFO);
        // resource bundle actions
        this.rb = rb;
        errorString = rb.getString("error");
        // set Prompt String
        usernameField.setPromptText(rb.getString("username"));
        passwordField.setPromptText(rb.getString("password"));
        userLabel.setText(rb.getString("user"));
        passLabel.setText(rb.getString("pass"));
    }    

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        boolean validLogin;
        try {
            System.out.println("Attempting Login...");
            // check for values
            if (usernameField.getText().length() > 0 && passwordField.getText().length() > 0) {
                // pass username and password to db boolean is returned
                validLogin = DBConnection.validateLogin(usernameField.getText(), passwordField.getText());
                // if login is valid pullup main screen
                if (validLogin){
                    // store login to log
                    log.log(Level.INFO, "User Logged In: {0}", ActiveUser.getUserName());
                    // load mainScreen
                    Parent mainView = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));	
                    Scene mainScene = new Scene(mainView);
                    Stage window = Helpers.getWindowInfo(event);
                    window.setScene(mainScene);
                    
                } else {
                    Helpers.popUpBox(errorString);
                    System.out.println("Error!");
                    // store invalid login to log
                    log.log(Level.INFO, "Invalid Login Attempt Username used: {0}", usernameField.getText());
                }
            } else {
                // show prompt
                Helpers.popUpBox(rb.getString("prompt"));
                validLogin = false;
            }   
        } catch (IOException e) {
            System.out.println(e);
            Helpers.popUpBox("Something Happened, Could not load. \nTry again.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        System.out.println("Exiting Program Now...");
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }
}
