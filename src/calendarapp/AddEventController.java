/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import static calendarapp.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony Adams
 */
public class AddEventController implements Initializable {

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField customerField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;
    @FXML
    private TextArea descriptionBox;
    @FXML
    private TextField contactField;
    @FXML
    private TextField titleField;
    @FXML
    private GridPane myGrid;
    @FXML
    private Button selectCustomer;
    @FXML
    private HBox customerHBox;
    // Date and Time Objects
    DatePicker startDatePicker;
    DatePicker endDatePicker;
    HBox startBox;
    HBox endBox;
    ComboBox startComboHour;
    ComboBox startComboMinute;
    ComboBox endComboHour;
    ComboBox endComboMinute;
    Label hLabel;
    Label mLabel;
    Label hLabel2;
    Label mLabel2;
    Insets myInsets;
    Customer customer;
    Event editingEvent;
    boolean isEdit = false;
    ObservableList<Event> appointmentTimeList;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // update Appointment Time List with vals from DB
        updateAppointmentTimeList();
        
        myInsets = new Insets(15,15,15,15);
        // create Date and Time Selectors
        // HBoxes
        startBox = new HBox();
        endBox = new HBox();
        // datePickers for month 
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        // ComboBoxes for Hour and Minutes
        startComboHour = new ComboBox();
        startComboMinute = new ComboBox();
        endComboHour = new ComboBox();
        endComboMinute = new ComboBox();
        // Hour ComboBox populate Items
        for (int i = 9; i < 18; i++) {
            startComboHour.getItems().add(i);
            endComboHour.getItems().add(i);
        }
        // Minute ComboBox populate Items
        startComboMinute.getItems().addAll(0,15,30,45);
        endComboMinute.getItems().addAll(0,15,30,45);
        //Labels for H and M
        hLabel = new Label("H");
        mLabel = new Label("M");
        hLabel2 = new Label("H");
        mLabel2 = new Label("M");
        // add all to HBoxes 
        startBox.getChildren().addAll(startDatePicker, hLabel, startComboHour, mLabel, startComboMinute);
        endBox.getChildren().addAll(endDatePicker, hLabel2, endComboHour, mLabel2, endComboMinute);
        startBox.setAlignment(Pos.CENTER_LEFT);
        endBox.setAlignment (Pos.CENTER_LEFT);
        myGrid.add(startBox, 1, 1);// column = 1 row = 1
        myGrid.add(endBox, 1, 2); // column = 1 row = 2
        if (isEdit == false) {
            // Set Date Picker Value to Today's Date
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now());
        }
        
    }    
    private void updateAppointmentTimeList() {
        // create Observable List
        appointmentTimeList = FXCollections.observableArrayList();
        String query = "SELECT start, end, appointmentId from appointment";
        // connect and populate observable list with appointment Objects
        try {
            
            DBConnection.makeConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                LocalDateTime start = Helpers.utcToLocalTime(LocalDateTime.parse(rs.getString(1), Helpers.dtf));
                LocalDateTime end = Helpers.utcToLocalTime(LocalDateTime.parse(rs.getString(2), Helpers.dtf));
                int appointmentId = rs.getInt(3);
                appointmentTimeList.add(new Event(start, end, appointmentId));
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddEventController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBConnection.closeConnection();
        }
    }
    private boolean allFieldsNotFilled() {
        // checks for all fields to make sure they are filled will return a false if any are empty
        // checks date pickers first
        if (startDatePicker.getValue() == null){ return true; }
        if (endDatePicker.getValue() == null) { return true; }
        // then fields and combo boxes
        if (customerField.getText().length() == 0){ return true; }  // t if empty
        if (startComboHour.getSelectionModel().isEmpty()) { return true; } // t if empty
        if (startComboMinute.getSelectionModel().isEmpty()) { return true; } // t if empty
        if (endComboHour.getSelectionModel().isEmpty()) { return true; } // t if empty
        if (endComboMinute.getSelectionModel().isEmpty()) {return true; } // t if empty
        if (locationField.getText().length() == 0) { return true; } // t if empty
        if (contactField.getText().length() == 0) { return true; } // t if empty
        if (typeField.getText().length() == 0) { return true; } // t if empty
        
        return false;
    }
    private boolean validTimeChecker(LocalDateTime start, LocalDateTime end) {
        // Check to make sure combo boxes selection is not past 5:00
        // returns true if two times are not between 9am and 5pm or if times overlap
        int hourInt = (int) startComboHour.getSelectionModel().getSelectedItem();
        int minuteInt = (int) startComboMinute.getSelectionModel().getSelectedItem();
        if (hourInt == 17 && minuteInt != 0) { 
            Helpers.popUpBox("Cannot Schedule Appointment Outside of Business Hours");
            return true; }
        // now end times
        hourInt = (int) endComboHour.getSelectionModel().getSelectedItem();
        minuteInt = (int) endComboMinute.getSelectionModel().getSelectedItem();
        if (hourInt == 17 && minuteInt != 0) { 
            Helpers.popUpBox("Cannot Schedule Appointment Outside of Business Hours");
            return true; }
        // check to make sure appointment does not span multiple days
        if (start.toLocalDate().toEpochDay() != end.toLocalDate().toEpochDay()) {
            // if spans multiple days
            Helpers.popUpBox("Cannot Schedule Appointment Outside of Business Hours");
            return true;
        }
        // check to make sure appointment is not 0
        if (start.equals(end)) {
            Helpers.popUpBox("Appointment must be 15 minutes or longer.");
            return true;
        }

        // Check for overlapping appointments
        for (int i=0; i < appointmentTimeList.size(); i++) {
            // vars from appointmentTImeList
            LocalDateTime dbStartTime = appointmentTimeList.get(i).getStartDate();
            LocalDateTime dbEndTime = appointmentTimeList.get(i).getEndDate();
            // for when overlaps form from the left on the timeline
            if (dbStartTime.isBefore(start) && dbEndTime.isAfter(start)) {
                System.out.println("overlap");
                // makesure to check for editing
                if (isEdit && appointmentTimeList.get(i).getAppointmentId() != editingEvent.getAppointmentId()){
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                } else if(!isEdit) {
                    // everything else is okay to be checked
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                }
            } else if (dbStartTime.equals(start)) {
                // for when start times are equal
                System.out.println("overlap"); 
                if (isEdit && appointmentTimeList.get(i).getAppointmentId() != editingEvent.getAppointmentId()){
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                } else if(!isEdit) {
                    // everything else is okay to be checked
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                }
            } else if (dbStartTime.isAfter(start) && dbStartTime.isBefore(end)) {
                // finally if new appointment has an existing overlap internally
                System.out.println("overlap");
                if (isEdit && appointmentTimeList.get(i).getAppointmentId() != editingEvent.getAppointmentId()){
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                } else if(!isEdit) {
                    // everything else is okay to be checked
                    Helpers.popUpBox("Cannot Schedule Overlapping Appointments");
                    return true;
                }
            }
        }
        // otherwise return false
        return false;
        
    }
    
    @FXML
    private void handleSave(ActionEvent event) throws IOException, SQLException {
        System.out.println("Checking Fields for Completeness : ");
        System.out.println(!allFieldsNotFilled());
        if (!allFieldsNotFilled()) {
            // if editing an exisiting event
            PreparedStatement prepStatement;
            boolean success = false;
            String dbQuery;
            // Local Start Date from DatePicker
            LocalDate startDate = startDatePicker.getValue();
            // LocalTime from ComboBoxes

            LocalTime startTime = getLocalComboTime(startComboHour, startComboMinute);
            // LocalDateTime from both date and time
            LocalDateTime startLDT = LocalDateTime.of(startDate, startTime);
            // startLDT Converted to UTC
            LocalDateTime startToUTC = Helpers.localToUTCTime(startLDT);

            // Same as above but for end date
            LocalDate endDate = endDatePicker.getValue();
            LocalTime endTime = getLocalComboTime(endComboHour, endComboMinute);
            LocalDateTime endLDT = LocalDateTime.of(endDate, endTime);
            LocalDateTime endToUTC = Helpers.localToUTCTime(endLDT);

            if (startLDT.isAfter(endLDT)) {
                Helpers.popUpBox("Invalid times picked.\nTry again.");
                // check if times are within business hours
            } else if (validTimeChecker(startLDT, endLDT)) {
//                Helpers.popUpBox("Invalid times picked.\nTry again.");
                    System.out.println("Invalid Times Picked, Cannot Proceed.");
            } else if(false) {

            } else {
                if (isEdit) {
                    dbQuery = "UPDATE appointment SET start= ?, title= ?, end =?, customerId =?, location =?, type = ?, description =?,lastUpdate=?, lastUpdateBy = ? WHERE appointmentId = ?;";
                    try {
                        DBConnection.makeConnection();
                        prepStatement = conn.prepareStatement(dbQuery);

                        prepStatement.setString(1, startToUTC.toString());
                        prepStatement.setString(2, titleField.getText());
                        prepStatement.setString(3, endToUTC.toString());
                        prepStatement.setInt(4, customer.getCustomerId());
                        prepStatement.setString(5, locationField.getText());
                        prepStatement.setString(6, typeField.getText());
                        prepStatement.setString(7, descriptionBox.getText());
                        prepStatement.setString(8, Helpers.localToUTCTime(LocalDateTime.now()).toString());
                        prepStatement.setString(9, ActiveUser.getUserName());
                        prepStatement.setInt(10, editingEvent.getAppointmentId());
                        // process update
                        prepStatement.executeUpdate();
                        success = true;
                    } catch (SQLException e) {
                        System.out.println(e);
                        Helpers.popUpBox("Error in SQL Update \n" + e);
                    } finally {
                        DBConnection.closeConnection();
                    }

                } else {
                    // DB Query String
                    dbQuery = "INSERT INTO appointment " + "(customerId, userId, title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

                    try {
                        // Connect to DB
                        DBConnection.makeConnection();
                        // create PreparedStatement and pass in Query String to prep
                        prepStatement = conn.prepareStatement(dbQuery);
                        // Set all Values in Query String 1-14
                        prepStatement.setInt(1, customer.getCustomerId()); // customerId
                        prepStatement.setInt(2, ActiveUser.getUserId()); // userId
                        prepStatement.setString(3, titleField.getText()); // title
                        prepStatement.setString(4, descriptionBox.getText()); // description
                        prepStatement.setString(5, locationField.getText()); // location
                        prepStatement.setString(6, contactField.getText()); // contact
                        prepStatement.setString(7, typeField.getText()); // type
                        prepStatement.setString(8, ""); // url no Input from Screen 
                        prepStatement.setString(9, startToUTC.toString());
                        prepStatement.setString(10, endToUTC.toString());
                        prepStatement.setString(11, Helpers.localToUTCTime(LocalDateTime.now()).toString()); // createDate
                        prepStatement.setString(12, ActiveUser.getUserName()); // createdBy
                        prepStatement.setString(13, Helpers.localToUTCTime(LocalDateTime.now()).toString()); // lastUpdate
                        prepStatement.setString(14, ActiveUser.getUserName()); // lastUpdateBy
                        // Print to console
                        System.out.println("Saving to DB.");

                        prepStatement.executeUpdate();
                        success = true;
                    } catch(SQLException e) {
                        System.out.println(e);
                        Helpers.popUpBox("Error in SQL Update \n" + e);

                    } catch (RuntimeException re) {
                        System.out.println(re);
                        Helpers.popUpBox("Runtime Exception \n" + re);
                    }finally {
                        DBConnection.closeConnection();
                    }
                }
                // if updating db was successful, exit to main screen here...
                if (success) {
                    System.out.println("Success!");
                    Parent mainView = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
                    Scene mainScene = new Scene(mainView);
                    Stage window = Helpers.getWindowInfo(event);
                    window.setScene(mainScene);   
                } else {
                    Helpers.popUpBox("Something went wrong, try again...");
                }
            }
        } else {
            Helpers.popUpBox("Something is wrong, please fill out all required values.");
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        System.out.println("Canceling. Returning to main screen...");
        Parent mainView = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
    }
    private LocalTime getLocalComboTime(ComboBox hour, ComboBox minute) {
        try {
            // pulls ints from comboboxes and returns local time obj
            int hourInt = (int) hour.getSelectionModel().getSelectedItem();
            int minuteInt = (int) minute.getSelectionModel().getSelectedItem();
            LocalTime myLT = LocalTime.of(hourInt, minuteInt,0,0);
            return myLT;
        } catch (Exception ex) {
            Helpers.popUpBox("Please Select Valid Times");
            System.out.println(ex);
        }
        return null;
    }

    @FXML
    private void handleSelectCustomer(ActionEvent event) throws SQLException {
        // Run SQLQuery to pull Customer names and Id's
        Stage popUp = new Stage();
        Customer selectedCust = new Customer();
        String query = "SELECT customerName, customerId FROM customer;";
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        ResultSet queryResults = DBConnection.processQuery(query);
        while(queryResults.next()) {
            // create and add newCustomer Objects to list
            Customer newCust = new Customer(queryResults.getString(1), queryResults.getInt(2));
            customerList.add(newCust); // customerName
        }
        // Create PopUp Window to Select a Customer
        VBox customerSelectVBox = new VBox();
        TableView customerSelectTable = new TableView();
        TableColumn customerIdCol = new TableColumn("Customer Id");
        TableColumn customerNameCol = new TableColumn("Customer Name");
        // add list to table
        customerSelectTable.setItems(customerList);
        // Set Columns
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerSelectTable.getColumns().addAll(customerIdCol, customerNameCol);
        // create select Button
        Button selectButton = new Button("Select Customer");
        // select action
        selectButton.setOnAction((ActionEvent e) -> {
            Customer oCust = (Customer) customerSelectTable.getSelectionModel().getSelectedItem();
            selectedCust.setCustomerName(oCust.getCustomerName());
            selectedCust.setCustomerId(oCust.getCustomerId());
            popUp.close();
        });
        customerSelectVBox.getChildren().addAll(customerSelectTable, selectButton);
        customerSelectVBox.setAlignment(Pos.CENTER);
        // create scene and show
        Scene custSelectScene = new Scene(customerSelectVBox, 300, 300);
        popUp.setScene(custSelectScene);
        popUp.showAndWait();
        customer = selectedCust;
        customerField.setText(selectedCust.getIdandName());
    }
    public void setEventDetails(int id) throws SQLException {
        // sets Controller to tell whether its an edit or not
        isEdit = true;
        // will take Event Details and fill in boxes for Editing
        DBConnection.makeConnection();
        String queryString = "select appointment.start, appointment.title, appointment.description, appointment.customerID, appointment.type, appointment.appointmentId, appointment.end, appointment.location, appointment.userId, appointment.contact, customer.customerName from appointment INNER JOIN customer ON appointment.customerId = customer.customerId WHERE appointmentId = " + id;
        Statement queryStatement = conn.createStatement();
        ResultSet queryResult = queryStatement.executeQuery(queryString);
        // next row in result set
        queryResult.next();
        // Assign result values to new event
        LocalDateTime startLDTUTC = LocalDateTime.parse(queryResult.getString(1), Helpers.dtf);
        LocalDateTime endLDTUTC = LocalDateTime.parse(queryResult.getString(7), Helpers.dtf);
        editingEvent = new Event(
                Helpers.utcToLocalTime(startLDTUTC),// start
                queryResult.getString(2),// title
                queryResult.getString(3),// description
                queryResult.getInt(4), // customerID
                queryResult.getString(5), //type
                queryResult.getInt(6), // appointmentId
                Helpers.utcToLocalTime(endLDTUTC), // end
                queryResult.getString(8), //location
                queryResult.getInt(9), // userId
                queryResult.getString(10) // contact
        );
        // create simple Customer Object
        customer = new Customer(queryResult.getString(11), queryResult.getInt(4));
        System.out.println(editingEvent.getStartDate());
        
        // populate fxml Text Fields with event values        
        titleField.setText(editingEvent.getTitle());
        customerField.setText(customer.getIdandName());
        locationField.setText(editingEvent.getLocation());
        contactField.setText(editingEvent.getContact());
        typeField.setText(editingEvent.getType());
        descriptionBox.setText(editingEvent.getDescription());//TODO Set Maximum character value to ERD
        
        // Set DatePickers
        startDatePicker.setValue(LocalDate.parse(editingEvent.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        endDatePicker.setValue(LocalDate.parse(editingEvent.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE)));
        
        // Set Time ComboBoxes -start
        startComboHour.setValue(editingEvent.getStartDate().getHour());
        startComboMinute.setValue(editingEvent.getStartDate().getMinute());
        // and end
        endComboHour.setValue(editingEvent.getEndDate().getHour());
        endComboMinute.setValue(editingEvent.getEndDate().getMinute());

        DBConnection.closeConnection();
    }

    
}
