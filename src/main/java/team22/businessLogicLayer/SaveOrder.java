package team22.businessLogicLayer;

import team22.dataAccessLayer.*;

public class SaveOrder {

    /**
     * Save order as an existing customer
     * @param forename the customer's forename
     * @param surname the customer's surname
     * @param postcode the customer's postcode
     * @param houseNum the customer's house number
     * @param bikeName the bike's name
     * @param frameSet the bike's frame set
     * @param handlebar the bike's handlebars
     * @param wheel the bike's wheels
     * @return the order number, or -1 if there was a fault when ordering
     */
    public static int saveOrderExistingCustomer(String forename, String surname, String postcode, int houseNum,
                                                   String bikeName, FrameSet frameSet, Handlebar handlebar, Wheel wheel) {

        // Authenticate the customer
        Customer customer = Accounts.authCustomer(forename, surname, postcode, houseNum);

        // Check if customers' details are valid
        if (customer == null) {
            return -1;
        }
        // If details are valid proceed to saveOrder
        else {
            return Order.addOrder(customer.getCustomerID(), bikeName, frameSet, handlebar, wheel);
        }
    }

    /**
     * Save order as a new customer
     * @param forename the customer's forename
     * @param surname the customer's surname
     * @param postcode the customer's postcode
     * @param houseNum the customer's house number
     * @param roadName the customer's road name
     * @param cityName the customer's city name
     * @param bikeName the bike's name
     * @param frameSet the bike's frame set
     * @param handlebar the bike's handlebars
     * @param wheel the bike's wheels
     * @return the order number, or -1 if there was a fault when ordering
     */
    public static int saveOrderNewCustomer(String forename, String surname, String postcode, int houseNum,
                                           String roadName, String cityName, String bikeName, FrameSet frameSet,
                                           Handlebar handlebar, Wheel wheel) {

        int existingOrder = saveOrderExistingCustomer(forename, surname, postcode, houseNum, bikeName, frameSet, handlebar, wheel);

        // Try and login as an existing customer in case wrong button is selected
        if (existingOrder == -1) {

            // Check if address already exists
            Address address = Address.getAddress(postcode, houseNum);
            // If address does not exist add the address to the database
            if (address.getAddressID() == 0) {
                Address.addAddress(postcode, houseNum, roadName, cityName);
                address = Address.getAddress(postcode, houseNum);
            }

            // Add the customer to the database
            Customer.addCustomer(address.getAddressID(), forename, surname);

            // SaveOrder
            return saveOrderExistingCustomer(forename, surname, postcode, houseNum, bikeName, frameSet, handlebar, wheel);
        } else {
            return existingOrder;
        }
    }
}
