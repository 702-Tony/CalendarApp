/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

import static calendarapp.DBConnection.conn;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony Adams
 */
public class CustomerScreenController implements Initializable {

    @FXML
    private Button addButton;
    @FXML
    private Insets x1;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableColumn<Customer, String> customerCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    @FXML
    private TableColumn<Customer, String> cityCol;
    @FXML
    private TableColumn<Customer, String> countryCol;
    @FXML
    private TableView<Customer> customerTable;
    
    private ObservableList<Customer> customerList;

    /**
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // TODO Create customer objects and populate table
            populateTables();
        } catch (SQLException ex) {
            System.out.println("Couldn't Load Table, Try Again");
        }
    }
    private void populateTables() throws SQLException {
        DBConnection.makeConnection();
        String queryString = "SELECT customer.customerId, customer.customerName, address.addressId, address.address, address.address2, address.postalCode, address.phone, city.cityId, city.city, country.countryId, country.country FROM customer INNER JOIN address ON customer.addressId = address.addressId INNER JOIN city ON address.cityId = city.cityId INNER JOIN country ON city.countryId = country.countryId;";
        // holds data pulled from DB
        ResultSet tableData = DBConnection.processQuery(queryString);
        customerList = FXCollections.observableArrayList();
        // create customer Objects to Populate TableView
        while(tableData.next()) {
            int custId = tableData.getInt(1);
            String custName = tableData.getString(2);
            int addId = tableData.getInt(3);
            String address1 = tableData.getString(4);
            String address2 = tableData.getString(5);
            String postCode = tableData.getString(6);
            String phone = tableData.getString(7);
            int cityId = tableData.getInt(8);
            String city = tableData.getString(9);
            int countryId = tableData.getInt(10);
            String country = tableData.getString(11);
            Customer cust = new Customer(custId, custName, addId, address1, address2, postCode, phone, cityId, city, countryId, country);
            customerList.add(cust);
        }
        customerTable.setItems(customerList);
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        
        
    }     

    @FXML
    private void handleAdd(ActionEvent event) throws IOException {
        System.out.println("Add Clicked");
        // open AddCustomerScreen 
        Parent mainView = FXMLLoader.load(getClass().getResource("AddCustomer.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
        
    }

    @FXML
    private void handleEdit(ActionEvent event) throws IOException {
    
        System.out.println("Edit Clicked");
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddCustomer.fxml"));
            Parent root = loader.load();
            AddCustomerController addCustomerController = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            addCustomerController.setCustomerDetails(selectedCustomer);
            stage.show();
            Stage window = Helpers.getWindowInfo(event);
            window.close();
        } else {
            Helpers.popUpBox("Please select Customer to edit.");
        }
        
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        System.out.println("Delete Clicked");
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        String query = "DELETE From customer WHERE customerId = " + selectedCustomer.getCustomerId();
        System.out.println(query);
        try {
            DBConnection.makeConnection();
            Statement queryStatement;
            queryStatement = conn.createStatement();
            queryStatement.executeUpdate(query);
            Helpers.popUpBox("Deletion of Customer " + selectedCustomer.getCustomerName() + " Successful");
            populateTables();
            System.out.println("Customer : " + selectedCustomer.getCustomerName() + " Deleted.");

        } catch (SQLException e ) {
            Helpers.popUpBox("Cannot Delete Customer with Open Appointments. \n" + "Please delete or reassign open appointments.");
            System.out.println(e);
        } finally {
            DBConnection.closeConnection();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        System.out.println("Returning to main screen");
        // Back to main screen
        Parent mainView = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
        
        
    }
    
}
