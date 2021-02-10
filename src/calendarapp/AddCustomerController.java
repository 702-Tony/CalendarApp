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
import java.time.LocalDateTime;
import java.util.ResourceBundle;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony Adams
 */
public class AddCustomerController implements Initializable {

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    
    
    // store addresses to add to Customer  
    private ObservableList<Address> addresses;
    @FXML
    private Button selectAddress;
    @FXML
    private TextField addressField;
    
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    private int addressIdEditor;
    private Customer editingCustomer;
    private Address selectedAddress;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
    private void populateComboBox() throws SQLException {
        // will populate combo box with addresses
        String query = "SELECT address.addressId, address.address, address.address2, address.postalCode, address.cityId, city.city, city.countryId, country.country FROM address INNER JOIN city ON address.cityId = city.cityId INNER JOIN country ON city.countryId = country.countryId;";
        ResultSet result = DBConnection.processQuery(query);
        addresses = FXCollections.observableArrayList();
        while(result.next()) {
            int addressId = result.getInt(1);
            String address = result.getString(2);
            String address2 = result.getString(3);
            String postalCode = result.getString(4);
            int cityId = result.getInt(5);
            String city = result.getString(6);
            int countryId = result.getInt(7);
            String country = result.getString(8);
            
            Address newAddress = new Address(addressId, address, address2, postalCode, cityId, city, countryId, country);
            addresses.add(newAddress);
        }
        
    }
    private void addressSelectorScreen() throws SQLException {
        // Load addresses
        populateComboBox();
        Insets myPadding = new Insets(25,25,25,25);
        Stage addressPopUp = new Stage();
        addressPopUp.initModality(Modality.APPLICATION_MODAL);
        // Label Setup
        Label addressLabel = new Label("Select an Address");
        addressLabel.setTextAlignment(TextAlignment.JUSTIFY);
        // TableView Setup
        TableView  addressList = new TableView();
        addressList.setItems(addresses);
        
        TableColumn address = new TableColumn("Address");
        TableColumn cityCol = new TableColumn("City");
        addressList.getColumns().addAll(address, cityCol);
        address.setMinWidth(150);
        cityCol.setMinWidth(150);
        address.setCellValueFactory(new PropertyValueFactory<>("address1"));
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        
        
        // Button Setup
        Button okButton = new Button("Okay");
        Button cancelBtn = new Button("Cancel");
        Button addBtn = new Button("Add Address");
        Button editBtn = new Button("Edit Address");
        // container for Buttons
        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(okButton, cancelBtn, addBtn, editBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(myPadding);
        
        // main Box
        VBox mainBox = new VBox(10);
        mainBox.setPadding(myPadding);
        mainBox.getChildren().addAll(addressLabel, addressList, buttonBox);
        mainBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(mainBox, 500, 400);
        
        // Add Address Box
        TextField addAddressField = new TextField();
        addAddressField.setPromptText("Street Address 1");
        TextField addAddress2Field = new TextField();
        addAddress2Field.setPromptText("Street Address 2");
        TextField addCityField = new TextField();
        addCityField.setPromptText("City");
        
        ComboBox addCountryComboBox = new ComboBox();
        // 0. US 1. Canada 2. Norway 3. England
        // Add 1 to get correct index for DB
        addCountryComboBox.getItems().addAll("US","Canada","Norway", "England");
        addCountryComboBox.setPromptText("Country");
        
        TextField addPostalCodeField = new TextField();
        addPostalCodeField.setPromptText("Postal Code");
        // Save Button for add address
        Button saveAddressButton= new Button("Save");
        saveAddressButton.setAlignment(Pos.CENTER);
        
        VBox addForm = new VBox(10);
        // add all items to addForm
        addForm.getChildren().addAll(addAddressField, addAddress2Field, addCityField, addCountryComboBox, addPostalCodeField, saveAddressButton);
        // add button action to process adding address to DB
        addBtn.setOnAction(e -> {
            selectedAddress = null;
            try {
                // clear values
                addAddressField.setText("");
                addAddress2Field.setText("");
                addCityField.setText("");
                addCountryComboBox.getSelectionModel().clearSelection();
                addPostalCodeField.clear();
                mainBox.getChildren().add(addForm);
            } catch (IllegalArgumentException ex) {
                mainBox.getChildren().remove(addForm);
                
            }
            
        });
        // save address to DB
        saveAddressButton.setOnAction(e -> {
            PreparedStatement prepStatement;
            
            System.out.println("Saving Address");
            String queryString;
            String queryString2;
            String currentTime = Helpers.localToUTCTime(LocalDateTime.now()).toString();
            if (selectedAddress == null) {
                // this will mean its a new address
                // Use LAST_INSERT_ID() to pull last insert ID for cityId
                queryString = "INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES (?, ?, ?, ?, ?, ?);";
                queryString2 = "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                // int for storing cityId
                int editingCityId;
                try {
                    // new city
                    editingCityId = cityCheck(addCityField.getText());
                    DBConnection.makeConnection();
                    ResultSet rsKey;
                    if (editingCityId < 0) {
                        prepStatement = conn.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                        prepStatement.setString(1, addCityField.getText());
                        prepStatement.setInt(2, addCountryComboBox.getSelectionModel().getSelectedIndex() + 1);
                        prepStatement.setString(3, currentTime);
                        prepStatement.setString(4, ActiveUser.getUserName());
                        prepStatement.setString(5, currentTime);
                        prepStatement.setString(6, ActiveUser.getUserName());
                        prepStatement.executeUpdate();
                        // get returned key
                        rsKey = prepStatement.getGeneratedKeys();
                        rsKey.next();
                        editingCityId = rsKey.getInt(1);   
                    }
                    
                    // Updating Address new prepStatement
                    prepStatement = conn.prepareStatement(queryString2, Statement.RETURN_GENERATED_KEYS);
                    // assign values
                    prepStatement.setString(1, addAddressField.getText());
                    prepStatement.setString(2, addAddress2Field.getText());
                    prepStatement.setInt(3, editingCityId);
                    prepStatement.setString(4, addPostalCodeField.getText());
                    prepStatement.setString(5, "");
                    prepStatement.setString(6, currentTime);
                    prepStatement.setString(7, ActiveUser.getUserName());
                    prepStatement.setString(8, currentTime);
                    prepStatement.setString(9, ActiveUser.getUserName());
                    // execute update
                    prepStatement.executeUpdate();
                    // get returned key
                    rsKey = prepStatement.getGeneratedKeys();
                    rsKey.next();
                    addressIdEditor = rsKey.getInt(1);
                    // remove add form
                    mainBox.getChildren().remove(addForm);
                    // update view
                    populateComboBox();
                    addressList.setItems(addresses);
                    Address newAddress;
                    addresses.forEach(addy -> {
                        if (addy.getAddressId() == addressIdEditor) {
                            addressList.getSelectionModel().select(addy);
                        }
                    });
                    okButton.fire();
                    
                    
                } catch (SQLException sqlEx) {
                    System.out.println(sqlEx);
                    Helpers.popUpBox("Something happened");
                    
                } catch (NullPointerException npe) {
                    System.out.println(npe);
                    Helpers.popUpBox("Error. Check all event details are filled.");
                }
                finally {
                    DBConnection.closeConnection();
                }
                
                
            } else {
                // this means it is an update to a customer record
                try {
                    // checks for city existence and stores cityId
                    int editingCityId = cityCheck(addCityField.getText());
                    DBConnection.makeConnection();
                    if (editingCityId < 0) {
                        // if dont have cityId create city and return city
                        queryString = "INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES(?, ?, ?, ?, ?, ?)";
                        // prepStatement will return cityId
                        prepStatement = conn.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                        
                        prepStatement.setString(1, addCityField.getText());
                        prepStatement.setInt(2, addCountryComboBox.getSelectionModel().getSelectedIndex() + 1);
                        prepStatement.setString(3, currentTime);
                        prepStatement.setString(4, ActiveUser.getUserName());
                        prepStatement.setString(5, currentTime);
                        prepStatement.setString(6, ActiveUser.getUserName());
                        prepStatement.executeUpdate();
                        // editingCityId will now be the correct city
                        ResultSet rsKey = prepStatement.getGeneratedKeys();
                        rsKey.next();
                        editingCityId = rsKey.getInt(1);                        
                    } 
                    System.out.println("City ID : " + editingCityId);
                    // now process update to address
                    queryString = "UPDATE address SET address=?, address2=?, cityId=?, postalCode =?, lastUpdate=?, lastUpdateBy=? WHERE addressId =?";
                    /*
                    1. address
                    2. address2
                    3. cityId editingCityId
                    4. postalCode
                    5. phone
                    6. lastUpdate
                    7. lastUpdateBy
                    */
                    prepStatement = conn.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
                    prepStatement.setString(1, addAddressField.getText());
                    prepStatement.setString(2, addAddress2Field.getText());
                    prepStatement.setInt(3, editingCityId);
                    prepStatement.setString(4, addPostalCodeField.getText());
                    prepStatement.setString(5, currentTime);
                    prepStatement.setString(6, ActiveUser.getUserName());
                    prepStatement.setInt(7, selectedAddress.getAddressId());
                    // executeUpdate
                    prepStatement.executeUpdate();
                    // store Address ID to select later
                    addressIdEditor = selectedAddress.getAddressId();
                    populateComboBox();
                    addressList.setItems(addresses);
                    addresses.forEach(addy -> {
                        if (addy.getAddressId() == addressIdEditor) {
                            addressList.getSelectionModel().select(addy);
                        }
                    });
                    editBtn.fire();
                    okButton.fire();
                } catch (SQLException sqlE) {
                    System.out.println(" Add Address : " + sqlE);
                } finally {
                    DBConnection.closeConnection();
                } 
            }
            
            
        });
        
        okButton.setOnAction(e -> {
            
            selectedAddress = (Address) addressList.getSelectionModel().getSelectedItem();
            addressField.setText(selectedAddress.getAddress());
            addressPopUp.close();
        });
        // Clicking cancel to 
        cancelBtn.setOnAction(e -> {
            selectedAddress = null;
            addressPopUp.close();
        });
        editBtn.setOnAction(e -> {
            
            System.out.println("Edit Clicked");
            // if edit Not already Clicked
            if (!mainBox.getChildren().contains(addForm)) {
                okButton.setDisable(true);
                selectedAddress = (Address) addressList.getSelectionModel().getSelectedItem();
                if (selectedAddress != null) {
                    mainBox.getChildren().add(addForm);
                    addAddressField.setText(selectedAddress.getAddress1());
                    addAddress2Field.setText(selectedAddress.getAddress2());
                    addCityField.setText(selectedAddress.getCity());
                    addCountryComboBox.getSelectionModel().select(selectedAddress.getCountryId()-1);
                    addPostalCodeField.setText(selectedAddress.getPostalCode());
                } else {
                    Helpers.popUpBox("Nothing Selected");
                }
            } else {
                // if edit already clicked
                // then, reset values
                selectedAddress = null;
                addAddressField.setText("");
                addAddress2Field.setText("");
                addCityField.setText("");
                addCountryComboBox.getSelectionModel().clearSelection();
                addPostalCodeField.setText("");
                // and remove form
                mainBox.getChildren().remove(addForm);
                okButton.setDisable(false);
            }
        });
        addressPopUp.setScene(scene);
        addressPopUp.show();
    }
    @FXML
    private void handleSelectAddress(ActionEvent event) throws SQLException{
        addressSelectorScreen();
        if (selectedAddress != null) {
            System.out.println("Address Selected! " + selectedAddress.getAddressId());
            addressField.setText(selectedAddress.getAddress());
        }
    }
    private boolean fieldsNotValid() {
        // checks for all fields to make sure they are filled will return a false if any are empty
        // checks date pickers first
        if (nameField.getText().length() == 0) { return true; } // t if empty
        if (addressField.getText().length() == 0) { return true; } // t if empty
        // Check Phone number is valid
        if (!Helpers.phoneNumberValidator(phoneField.getText())) { return true; } // t if phone number is not valid
        return false;
        
    }
    @FXML
    private void handleSave(ActionEvent event) {
        // Saving to DB
        String queryString;
        PreparedStatement prepStatement;
        try {
            if (fieldsNotValid()) {
                throw new IOException("Please check your fields");
            }
            DBConnection.makeConnection();
            String time = Helpers.localToUTCTime(LocalDateTime.now()).toString();
            if(editingCustomer == null) {
                queryString = "INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES(?, ?, ?, ?, ?, ?, ?);";
                // this is a new Customer Entry
                prepStatement = conn.prepareStatement(queryString);
                prepStatement.setString(1, nameField.getText());
                prepStatement.setInt(2, selectedAddress.getAddressId());
                prepStatement.setInt(3, 1);
                prepStatement.setString(4, time);
                prepStatement.setString(5, ActiveUser.getUserName());
                prepStatement.setString(6, time);
                prepStatement.setString(7, ActiveUser.getUserName());
            } else {
                // this is an existing customer entry to be updated
                queryString = "UPDATE customer SET customerName = ?, addressId = ?, lastUpdate = ?, lastUpdateBy = ? WHERE customerId = ?;";
                prepStatement = conn.prepareStatement(queryString);
                prepStatement.setString(1, nameField.getText());
                prepStatement.setInt(2, selectedAddress.getAddressId());
                prepStatement.setString(3, time);
                prepStatement.setString(4, ActiveUser.getUserName());
                prepStatement.setInt(5, editingCustomer.getCustomerId());
            }
            prepStatement.executeUpdate();
            System.out.println("Success");
            // updating Phone number
            System.out.println("Updating Phone Number...");
            queryString = "UPDATE address SET phone = ? WHERE addressId = ?";
            prepStatement = conn.prepareStatement(queryString);
            prepStatement.setString(1, phoneField.getText());
            prepStatement.setInt(2, selectedAddress.getAddressId());
            prepStatement.executeUpdate();
            System.out.println("Success");
            System.out.println("Returning to Customer screen");
            // Back to Customer screen
            Parent mainView = FXMLLoader.load(getClass().getResource("CustomerScreen.fxml"));
            Scene mainScene = new Scene(mainView);
            Stage window = Helpers.getWindowInfo(event);
            window.setScene(mainScene);
            
        } catch (SQLException e) {
            System.out.println(e);
        } catch (IOException ioEx) {
            Helpers.popUpBox("Please check your fields");
            System.out.println(ioEx);
        } finally {
            DBConnection.closeConnection();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        System.out.println("Returning to Customer screen");
        // Back to Customer screen
        Parent mainView = FXMLLoader.load(getClass().getResource("CustomerScreen.fxml"));
        Scene mainScene = new Scene(mainView);
        Stage window = Helpers.getWindowInfo(event);
        window.setScene(mainScene);
    }
    public void setCustomerDetails(Customer customer) {
        // sets Customer Details on screen on Edit Customer Selection from main screen
        System.out.println("Editing customerId :" + customer.getCustomerId()+ " Customer Name : " + customer.getCustomerName());
        editingCustomer = customer;
        selectedAddress = editingCustomer.getCustomerAddress();
        addressField.setText(selectedAddress.getAddress());
        nameField.setText(editingCustomer.getCustomerName());
        phoneField.setText(editingCustomer.getPhone());
        
    }
    private int cityCheck(String cityName) throws SQLException{
        // Will check City name against DB and if exists will return cityId
        try {
            DBConnection.makeConnection();
            PreparedStatement prepStatement;
            String cityCheckQuery = "SELECT cityID FROM city WHERE city = ?;";
            prepStatement = conn.prepareStatement(cityCheckQuery);
            prepStatement.setString(1, cityName);
            ResultSet rs = prepStatement.executeQuery();
            if (rs.next()) {
                // if match found return cityId
                System.out.println("Match Found!" + rs.getInt(1));
                return rs.getInt(1);
            }
        } catch (SQLException sqlE) {
            System.out.println("cityCheckError" + sqlE);
            return -1;
        } finally {
            DBConnection.closeConnection();
        }
        // if no city found in DB that matches name return -1
        return -1;
    }
}
