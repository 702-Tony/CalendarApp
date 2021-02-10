/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Anthony Adams
 */
public class Helpers {
    
        public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.s");
        
        public static Stage getWindowInfo(ActionEvent e) {
        // get Stage information
        Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow();
        return window;
    }
        public static void popUpBox(String string) {
            // displays a message with an ok button
            // used mostly to display errors
            Stage popUpWindow = new Stage();
            popUpWindow.initModality(Modality.APPLICATION_MODAL);
            // creates label with passed in string
            Label label= new Label(string);
            // wrap text
            label.setWrapText(true);
            // set alignment to Justified
            label.setTextAlignment(TextAlignment.JUSTIFY);
            // create ok button
            Button okButton = new Button("Ok");
            // simple vbox layout
            VBox dialogBox = new VBox(10);

            dialogBox.setPadding(new Insets(25, 25, 25, 25));
            // button action to close window on pressing ok
            okButton.setOnAction(e -> {
              popUpWindow.close();
            });

            okButton.setMinWidth(100);

            dialogBox.getChildren().addAll(label, okButton);
            dialogBox.setAlignment(Pos.CENTER);

            Scene scene= new Scene(dialogBox, 300, 150);
            // show pop up and wait for user input
            popUpWindow.setScene(scene);
            popUpWindow.showAndWait();
    }
       
        public static LocalDateTime timestampConverter(String timestamp) {
            // converts String to localDateTime
            try {
                Timestamp ts = Timestamp.valueOf(timestamp);
                return ts.toLocalDateTime();
            } catch (IllegalArgumentException e) {
                Helpers.popUpBox("Incorrect Time Value, Try Again.");
            }
            return null;
            
        }
        public static LocalDateTime utcToLocalTime(LocalDateTime utcTime) {
            // this will take utc time and convert it to local time
            // used for taking times from DB and converting to local
            ZonedDateTime zonedUTC = utcTime.atZone(ZoneId.of("UTC"));
            ZonedDateTime utcConverted = zonedUTC.withZoneSameInstant(ZoneId.of(ZoneId.systemDefault().toString())); 
            LocalDateTime localTime = utcConverted.toLocalDateTime();
            return localTime;
        }
        public static LocalDateTime localToUTCTime(LocalDateTime localTime) {
            // this will take local time and convert to UTC
            // used for sending times to DB converted to UTC
            ZonedDateTime zonedLocal = localTime.atZone(ZoneId.of(ZoneId.systemDefault().toString()));
            ZonedDateTime utcZoned = zonedLocal.withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime utc = utcZoned.toLocalDateTime();
            return utc;
        }
        public static boolean phoneNumberValidator(String phone) {
            // returns true if phone number is valid
            /*
            checks for only numbers matching formats:
            555 555 5555
            555.555.5555
            (555)555-5555
            (555) 555-5555
            5555555555
            */
            if(phone.matches("^\\(?\\d{3}[\\-\\)\\.]?\\s?\\d{3}[\\-\\s\\.]?\\d{4}$")) { return true; }
            return false;
        }
}
