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
public class Address {
    private int addressId;
    private String address;
    private String address2;
    private String postalCode;
    private int cityId;
    private String city;
    private int countryId;
    private String country;
    Address(int addressId, String address, String address2, String postalCode, int cityId, String city, int countryId, String country) {
        this.addressId = addressId;
        this.address = address;
        this.address2 = address2;
        this.postalCode = postalCode;
        this.cityId = cityId;
        this.city = city;
        this.countryId = countryId;
        this.country = country;
    }
    public String getAddress() {
        return address + ", " + city + ", "+ country;
    }
    public String getCountry() {
        return country;
    }
    public String getCity() {
        return city;
    }
    public String getAddress1() {
        return address;
    }
    public int getAddressId() {
        return addressId;
    }

    public String getAddress2() {
        return address2;
    }

    public String getPostalCode() {
        return postalCode;
    }
    int getCityId() {
        return cityId;
    }
    int getCountryId() {
        return countryId;
    }
    
}
