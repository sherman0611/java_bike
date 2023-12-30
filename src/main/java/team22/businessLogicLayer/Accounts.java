package team22.businessLogicLayer;
import team22.dataAccessLayer.Address;
import team22.dataAccessLayer.Customer;
import team22.dataAccessLayer.StaffMember;

import java.util.ArrayList;

public class Accounts {

    /**
     * Attempts to login with a provided username/password combo
     * @param username the username to login with
     * @param password the password to login with
     * @return the StaffMember if login was successful, null otherwise
     */
    public static StaffMember login(String username, char[] password) {
        StaffMember acc = StaffMember.getByUsername(username);
        if (acc == null) { return null; }

        if(PasswordHash.checkPassword(password, acc.getHashedPassword())) {
            return acc;
        } else {
            return null;
        }
    }

    /**
     * Attempts to authenticate a customer
     * @param forename the inputted forename
     * @param surname the inputted surname
     * @param postcode the inputted postcode
     * @param houseNum the inputted house number
     * @return the Customer if found, null otherwise
     */
    public static Customer authCustomer(String forename, String surname, String postcode, int houseNum) {
        ArrayList<Customer> customersByName = Customer.getCustomersByName(forename, surname);
        Address address = Address.getAddress(postcode, houseNum);

        Customer theCustomer = null;
        if (customersByName.size() != 0 && address.getAddressID() != 0) {
            for (Customer customer : customersByName) {
                if ( customer.getAddressID() == address.getAddressID() ) {
                    theCustomer = customer;
                }
            }

        }

        return theCustomer;
     }
}
