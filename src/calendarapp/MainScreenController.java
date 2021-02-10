/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import static calendarapp.DBConnection.conn;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony Adams
 */
public class MainScreenController implements Initializable {

    @FXML
    private Insets x1;
    @FXML
    private ToggleGroup eventGrouping;
    @FXML
    private Insets x2;
    @FXML
    private TableView<Event> eventTable;
    @FXML
    private TableColumn<Event, LocalDateTime> dateCol;
    @FXML
    private TableColumn<Event, String> titleCol;
    @FXML
    private TableColumn<Event, String> descriptionCol;
    @FXML
    private TableColumn<Event, Integer> customerCol;
    @FXML
    private TableColumn<Event, String> typeCol;
    @FXML
    private Button addEventButton;
    @FXML
    private Button editEventButton;
    @FXML
    private Button deleteEventButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button customerButton;
    @FXML
    private RadioButton allRadio;
    @FXML
    private RadioButton monthRadio;
    @FXML
    private RadioButton weekRadio;
    @FXML
    private Button reportBtn;
    // Non FXML objects
    private ObservableList<Event> mainEventList;
    private ObservableList<String> alertList;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initializing...");
        try {
            populateTables();
        } catch (SQLException ex) {
            System.out.println("MainScreenController.initialize | " + ex);
        } finally {
            // if not done here, result set is terminated before it can be populated
            DBConnection.closeConnection();
            if (CalendarApp.startUp) {
                // runs check for upcoming appointments within the next 15 minutes
                upcomingApptCheck();
                // To keep this from running everytime the main screen is loaded
                CalendarApp.startUp = false;
            }
        }
    }    
    private void populateTables() throws SQLException {
        // 1. start, 2. title, 3. description, 4. customerID, 5. type 6. appointmentId
        // 7. Date endDate, 7. String location, 9. int userId, 10. String contact
        String populateString = "select start, title, description, customerID, type, appointmentId, end, location, userId, contact from appointment";// Add to String if you want only upcoming events " WHERE appointment.start >= CURDATE();"
        ResultSet tableData = DBConnection.processQuery(populateString);
        mainEventList = FXCollections.observableArrayList();
        while (tableData.next()) {
            // create ObservableList of events that will be loaded from this  
            LocalDateTime date = Helpers.utcToLocalTime(LocalDateTime.parse(tableData.getString(1), Helpers.dtf));
            String title = tableData.getString(2);
            String description = tableData.getString(3);
            Integer customerId = tableData.getInt(4);
            String type = tableData.getString(5);
            Integer appointmentId = tableData.getInt(6);
            LocalDateTime endDate = Helpers.utcToLocalTime(LocalDateTime.parse(tableData.getString(7), Helpers.dtf));
            String location = tableData.getString(8);
            Integer userId = tableData.getInt(9);
            String contact = tableData.getString(10);
            Event newEvent;
            newEvent = new Event(date, title, description, customerId, type, appointmentId, endDate, location, userId, contact);
            mainEventList.add(newEvent);
        }
        eventTable.setItems(mainEventList);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("tableDate"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        eventTable.getSortOrder().add(dateCol);
        
        if (allRadio.isSelected()) {
            allFilter(new ActionEvent());
        } else if (monthRadio.isSelected()) {
            monthFilter(new ActionEvent());
        } else if (weekRadio.isSelected()) {
            weekFilter(new ActionEvent());
        }
    }
    

    private void upcomingApptCheck() {
        // check event List for appointments and HelperPopUpBox an Alert with the Upcoming Event
        LocalDateTime currentTime = LocalDateTime.now();
        // current time plus 15 minutes
        LocalDateTime currentTimePlus = currentTime.plusMinutes(15);
        // list for all upcoming appointments
        alertList = FXCollections.observableArrayList();
        
        mainEventList.forEach(event -> {
            LocalDateTime eventTime = event.getStartDate();
            if (eventTime.isBefore(currentTimePlus) && eventTime.isAfter(currentTime)) {
                alertList.add(event.getTitle() + " at " + eventTime.toString());
            }
        });
        if (!alertList.isEmpty()) {
            Stage popUp = new Stage();
            popUp.initModality(Modality.APPLICATION_MODAL);
            
            VBox alertBox = new VBox();
            alertBox.setMaxHeight(450);
            Label alertBoxLabel = new Label("ALERT: Upcoming Appointments");
            ListView alertListView = new ListView();
            alertListView.setItems(alertList);
            Button okayButton = new Button("Okay");
            okayButton.setDefaultButton(true);
            alertBox.setAlignment(Pos.CENTER);
            alertBox.getChildren().addAll(alertBoxLabel, alertListView, okayButton);
            okayButton.setOnAction(e -> {
                popUp.close();
                System.out.println("Closing PopUp");
            });
            Scene alertScene = new Scene(alertBox);
            popUp.setScene(alertScene);
            popUp.show();
        }
    }

    @FXML
    private void handleAddEvent(ActionEvent event) throws IOException {
        // Load Add Event
        Parent mainView = FXMLLoader.load(getClass().getResource("AddEvent.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
    }

    @FXML
    private void handleEditEvent(ActionEvent event) throws IOException, SQLException {
        // Store Selection
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            // Load addEvent Screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddEvent.fxml"));
            Parent root = loader.load();
            AddEventController editEventController = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            
            // run method to load details from event controller
            editEventController.setEventDetails(selectedEvent.getAppointmentId());
            stage.show();
            Stage window = Helpers.getWindowInfo(event);
            window.close();
            
        } else {
            Helpers.popUpBox("Please select appointment to edit.");
        }
    }
    @FXML
    private void handleDeleteEvent(ActionEvent event) throws SQLException {
        try {
            // Process Update to Delete Selected Event from DB
            Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
            String query = "DELETE FROM appointment WHERE appointmentId = " + selectedEvent.getAppointmentId() + ";";
            DBConnection.makeConnection();
            Statement queryStatement;
            queryStatement = conn.createStatement();
            queryStatement.executeUpdate(query);
            Helpers.popUpBox("Deletion of Event : \n"+ selectedEvent.getTitle() + " Successful");
        } catch (SQLException e) {
            System.out.println("SQL Exception \n" + e);
            Helpers.popUpBox("Something happened. Try again");
        } catch (NullPointerException npe) {
            Helpers.popUpBox("Nothing Selected");
        } finally {
            DBConnection.closeConnection();
            // repopulate Tables with Data from the DB
            populateTables();
        }
    }

    @FXML
    private void handleUpdate(ActionEvent event) throws SQLException {
        System.out.println("Updating Table");
        populateTables();
        System.out.println("Successfully Updated");

    }

    @FXML
    private void handleCustomer(ActionEvent event) throws IOException {
        Parent mainView = FXMLLoader.load(getClass().getResource("CustomerScreen.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
    }

    @FXML
    private void allFilter(ActionEvent event) {
        // show all events
        eventTable.setItems(mainEventList);
    }

    @FXML
    private void monthFilter(ActionEvent event) {
        FilteredList<Event> filtered = new FilteredList<>(mainEventList, p -> true);
        filtered.setPredicate(eventItem -> {
            Month localMonth = LocalDateTime.now().getMonth();
            int localYear = LocalDateTime.now().getYear();
            if (eventItem.getStartDate().getMonth() == localMonth && eventItem.getStartDate().getYear() == localYear) {
                return true;
            } 
            return false;
        });
        SortedList<Event> sortedList = new SortedList<>(filtered);
        sortedList.comparatorProperty().bind(eventTable.comparatorProperty());
        eventTable.setItems(sortedList);
        
    }

    @FXML
    private void weekFilter(ActionEvent event) {
        FilteredList<Event> filtered = new FilteredList<>(mainEventList, p -> true);
        filtered.setPredicate(eventItem -> {
            int dayOfYear = LocalDateTime.now().getDayOfYear();
            int weekValue = dayOfYear/7;
            int eventWeekValue = eventItem.getStartDate().getDayOfYear() / 7;
            if (eventWeekValue == weekValue) {
                return true;
            }
            return false;
        });
        SortedList<Event> sortedList = new SortedList<>(filtered);
        sortedList.comparatorProperty().bind(eventTable.comparatorProperty());
        eventTable.setItems(sortedList);
    }

    @FXML
    private void handleReport(ActionEvent event) {
        // Open New Screen with Reports Buttons to Run Reports
        System.out.println("Report Button clicked");
        Stage reportPopUp = new Stage();
        VBox reportsBox = new VBox();
        // instantiate String for output filename
        // output filename should be the report time and date time to the millisecond
        LocalDateTime ldt = LocalDateTime.now();
        String timeStr;
        timeStr = "" + ldt.getYear() + ldt.getMonthValue()+ ldt.getDayOfMonth() + ldt.getHour() + ldt.getMinute() + ldt.getSecond();
        // Appointment Types By Month
        Label typesByMonthLabel = new Label("Appointment Types By Month");
        Button typesByMonth = new Button("Get Report");
        // Schedule for Each Consultant
        Label consultantScheduleLabel = new Label("Consultant Schedule");
        Button consultantSchedule = new Button("Get Report");
        // My Report -> All Addresses
        Label addressReportLabel = new Label("All Addresses");
        Button addressReport = new Button("Get Report");
        // cancel button to close
        Button cancelButton = new Button("Cancel");
        
        reportsBox.setPadding(new Insets(15,15,15,15));
        typesByMonth.setOnAction(e-> {
            ResultSet rs;
            String query;
            // output report on types of Appointments by Month
            try {
                String outputFileName = timeStr + "-Appt_Types_By_Month.csv";
                PrintWriter print = new PrintWriter(new FileOutputStream(new File(outputFileName), true));
                print.append("AppointmentTypesByMonthReport \n");// Title
                print.append("Year-Month,Count,Appointment-Type\n");// Header Names 
                DBConnection.makeConnection();
                query = "SELECT type, Count(type) AS typeCount, DATE_FORMAT(start, '%m-%Y') AS monthAndYear from appointment GROUP BY DATE_FORMAT(start, '%m-%Y'), type;";
                rs = DBConnection.processQuery(query);
                ObservableList<String> reportList = FXCollections.observableArrayList();
                while(rs.next()) {
                    String yearMonth = rs.getString(3); // year-month
                    String apptType = rs.getString(1);
                    int count = rs.getInt(2); // count
                    print.append(yearMonth + "," + count + ","+ apptType + "\n");
                    reportList.add(yearMonth + " : " + apptType + " - " + count);
                    System.out.println(yearMonth + " " + apptType +" " +count);
                }
                print.close();
                Stage report = new Stage();
                VBox reportVBox = new VBox();
                ListView reportTable = new ListView();
                reportTable.setItems(reportList);
                Label reportLabel = new Label("report saved as " + outputFileName);
                reportVBox.getChildren().addAll(reportLabel, reportTable);
                report.setScene(new Scene(reportVBox));
                report.showAndWait();
                 
            } catch (SQLException sqlEx) {
                System.out.println(sqlEx);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            } finally {
                DBConnection.closeConnection();
            }
           // close window upon successful report generation
           cancelButton.fire();
        });
        consultantSchedule.setOnAction(e-> {
            // output report on schedules for each consultant(user)
            ResultSet rs;
            String query;
            // output report on types of Appointments by Month
            try {
                String outputFileName = timeStr + "-Consultant_Schedule.csv";
                PrintWriter print = new PrintWriter(new FileOutputStream(new File(outputFileName), true));
                print.append("Consultant Schedules \n");// Title
                
                
                
                query = "SELECT appointment.start, appointment.end, appointment.title, appointment.location, appointment.type, customer.customerName, appointment.createdBy " +
                "FROM appointment " +
                "INNER JOIN customer ON appointment.customerId = customer.customerId " +
                "ORDER BY createdBy;";
//                PreparedStatement prepStatement = conn.prepareStatement(query);
                
                rs = DBConnection.processQuery(query);
                
                ObservableList<String> reportList = FXCollections.observableArrayList();
                String currentUser = null;
                // 1. start
                // 2. end
                // 3. title
                // 4. location
                // 5. type
                // 6. customerName
                // 7. createdBy
                while(rs.next()) {
                    if (currentUser == null) {
                        // first row
                        currentUser = rs.getString(7);
                        // Print first row with new user
                        print.append("User : " + rs.getString(7) + "\n");
                        print.append("Start Time,End Time,Title,Location,Appointment-Type,Customer Name\n");// Header Names 
                        reportList.add("User :" + rs.getString(7));
                        
                    }
                    // new user row
                    if (!currentUser.equals(rs.getString(7))) {
                        // print new line with new user name as separator
                        print.append("User : " + rs.getString(7) + "\n");
                        print.append("Start Time,End Time,Title,Location,Appointment-Type,Customer Name\n");// Header Names 
                        reportList.add("User :" + rs.getString(7));
                        // set current User as new User
                        currentUser = rs.getString(7);
                    }
                    String startTime = Helpers.utcToLocalTime(Helpers.timestampConverter(rs.getString(1))).toString();
                    String endTime = Helpers.utcToLocalTime(Helpers.timestampConverter(rs.getString(2))).toString();
                    String title = rs.getString(3);
                    String location = rs.getString(4);
                    String type = rs.getString(5);
                    String customerName = rs.getString(6);
                    // add items to csv file
                    print.append(startTime + "," + endTime + "," + title+ "," + location+ "," + type+ "," + customerName + "\n");
                    // add items to reportList
                    reportList.add(startTime + " to " + endTime + " Title: " + title+ " at " + location + " Event-Type: " + type + " Customer: " + customerName + "\n");
                }
                
                print.close();
                // show Reports popUp
                Stage report = new Stage();
                VBox reportVBox = new VBox();
                ListView reportTable = new ListView();
                reportTable.setItems(reportList);
                Label reportLabel = new Label("report saved as " + outputFileName);
                reportVBox.getChildren().addAll(reportLabel, reportTable);
                report.setScene(new Scene(reportVBox));
                report.showAndWait();
                 
            } catch (SQLException sqlEx) {
                System.out.println(sqlEx);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            } finally {
                DBConnection.closeConnection();
            }
            // close window upon successful report generation
           cancelButton.fire();
        });
        addressReport.setOnAction(e -> {
            // output report of each address
            ResultSet rs;
            String query;
            // output report on types of Appointments by Month
            try {
                String outputFileName = timeStr + "-Address Report.csv";
                PrintWriter print = new PrintWriter(new FileOutputStream(new File(outputFileName), true));
                print.append("Address Report \n");// Title
                print.append("Address_Line_1,Address_Line_2,City,PostalCode,Country\n");// Header Names 
                DBConnection.makeConnection();
                query = "SELECT address.address, address.address2, city.city, country.country, postalCode from address INNER JOIN city ON address.cityId = city.cityId INNER JOIN country ON city.countryId = country.countryId;";
                rs = DBConnection.processQuery(query);
                ObservableList<String> reportList = FXCollections.observableArrayList();
                while(rs.next()) {
                    String address = rs.getString(1);
                    String address2 = rs.getString(2);
                    String city = rs.getString(3);
                    String country = rs.getString(4);
                    String postalCode = rs.getString(5);
                    // add to both report list for screen output and outPutFile
                    print.append(address + "," + address2 + ","+ city +"," + postalCode +"," + country + "\n");
                    reportList.add(address + " " + address2 + " City: " + city + " Postal Code: " + postalCode + " Country: " + country);
                    
                }
                print.close();
                Stage report = new Stage();
                VBox reportVBox = new VBox();
                ListView reportTable = new ListView();
                reportTable.setItems(reportList);
                Label reportLabel = new Label("report saved as " + outputFileName);
                reportVBox.getChildren().addAll(reportLabel, reportTable);
                report.setScene(new Scene(reportVBox));
                report.showAndWait();
                 
            } catch (SQLException sqlEx) {
                System.out.println(sqlEx);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            } finally {
                DBConnection.closeConnection();
            }
            // close window upon successful report generation
           cancelButton.fire();
        });
        cancelButton.setOnAction(e-> {
            reportPopUp.close();
        });
        
        reportsBox.getChildren().addAll(typesByMonthLabel, typesByMonth, consultantScheduleLabel, consultantSchedule, addressReportLabel, addressReport, cancelButton);
        
        reportsBox.setAlignment(Pos.CENTER);
        
        reportPopUp.setScene(new Scene(reportsBox));
        reportPopUp.show();
    }
}
