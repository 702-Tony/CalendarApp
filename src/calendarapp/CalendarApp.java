/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Anthony Adams
 */
public class CalendarApp extends Application {
    public static boolean startUp = true;
    public static ActiveUser activeUser = new ActiveUser();
    
    @Override
    public void start(Stage stage) throws Exception {
        
        // Secondary Locale Setting
        // Locale.setDefault(new Locale("es", "ES"));
        ResourceBundle rb = ResourceBundle.getBundle("rb_language/rb");
        // create new active user object for storing user stuff
        // Set First Screen on Startup
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        loader.setResources(rb);
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
