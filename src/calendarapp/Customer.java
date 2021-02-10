/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calendarapp;

/**
 *
 * @author Anthony Adams
 */
/*
customer {
    customerId: INT(10)	>>k
    customerName: VARCHAR(45)
    addressId: INT(10)
    active: TINYINT(1)
    createDate: DATETIME
    createdBy: VARCHAR(4)
    lastUpdate: TIMESTAMP
    lastUpdateBy: VARCHAR(40)
}
*/
public class Customer {
    // Create customerClass for storing customer info to add to list. 
    // gameplan is to only load items necessary to view customer info
    // customer.customerName, address.address, address.address2, address.postalCode, address.phone, city.city, country.country
    private int customerId;
    private String customerName;
//    private int addressId;
//    private String address;
//    private String address2;
//    private String postalCode;
    private String phone;
//    private int cityId;
//    private String city;
//    private int countryId;
//    private String country;
    private Address addressObj;
    
    // Object for storing Customer Data in ObservableList
    Customer(
            int customerId, 
            String customerName, 
            int addressId, 
            String address, 
            String address2,
            String postalCode,
            String phone,
            int cityId,
            String city,
            int countryId,
            String country) {
        // Create Constructor to create customer obj
        this.customerId = customerId;
        this.customerName = customerName;
//        this.addressId = addressId;
//        this.address = address;
//        this.address2 = address2;
//        this.postalCode = postalCode;
        this.phone = phone;
//        this.cityId = cityId;
//        this.city = city;
//        this.countryId = countryId;
//        this.country = country;
        this.addressObj = new Address(addressId, address, address2, postalCode, cityId, city, countryId, country);
    }
    Customer(String customerName, int customerId) {
        this.customerName = customerName;
        this.customerId = customerId;
    }

    Customer() {
        this.customerName = null;
        this.customerId = 0;
    }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getCity() { return addressObj.getCity(); }
    public String getCountry() { return addressObj.getCountry(); }
    public int getCustomerId() { return customerId; }
    // for Customer Id
    public String getIdandName() { return customerId + ": " + customerName; }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public void setCustomerId (int customerId) {
        this.customerId = customerId;
    }
    public Address getCustomerAddress() { return addressObj; }



    
    
}
