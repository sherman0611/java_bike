package team22.UI;

import javax.swing.*;

import team22.dataAccessLayer.*;

import java.awt.*;

public class CustomerForm extends JPanel {

    private JLabel error;
    private JTextField forename;
    private JTextField surname;
    private JTextField houseNum;
    private JTextField road;
    private JTextField city;
    private JTextField postcode;

    /**
     * A JPanel extension that creates a form for a customer to input their details
     * @param ci the CustomerInfo to edit
     * @param size the size of the form
     */
    protected CustomerForm(CustomerInfo ci, Dimension size) {
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);

        buildCustomerForm(ci);
    }

    /**
     * Builds the customer form
     * @param ci the CustomerInfo to build the form based on
     */
    private void buildCustomerForm(CustomerInfo ci) {
        Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_BOLD, 24);
        Font subtitleFont = Fonts.getSizedFont(Fonts.OPENSANS_BOLD, 22);
        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);

        JLabel title = new JLabel("Edit details");

        error = new JLabel();
        
        Customer customer = ci.getCustomer();
        JLabel subtitle1 = new JLabel("Customer name");
        JLabel forenameLabel = new JLabel("Forename:");
		forename = new JTextField(32);
        forename.setText(customer.getForename());
        JLabel surnameLabel = new JLabel("Surname:");
		surname = new JTextField(32);
        surname.setText(customer.getSurname());

        Address address = ci.getAddress();
        JLabel subtitle2 = new JLabel("Delivery address");
        JLabel houseNumLabel = new JLabel("House number:");
		houseNum = new JTextField(32);
        houseNum.setText(String.valueOf(address.getHouseNum()));
        JLabel roadLabel = new JLabel("Road:");
		road = new JTextField(32);
        road.setText(address.getRoadName());
        JLabel cityLabel = new JLabel("City:");
		city = new JTextField(32);
        city.setText(address.getCityName());
        JLabel postcodeLabel = new JLabel("Postcode:");
		postcode = new JTextField(32);
        postcode.setText(address.getPostcode());

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle1.setAlignmentX(Component.LEFT_ALIGNMENT);
        forenameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        forename.setAlignmentX(Component.LEFT_ALIGNMENT);
        surnameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        surname.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle2.setAlignmentX(Component.LEFT_ALIGNMENT);
        houseNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        houseNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        roadLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        road.setAlignmentX(Component.LEFT_ALIGNMENT);
        cityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        city.setAlignmentX(Component.LEFT_ALIGNMENT);
        postcodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        postcode.setAlignmentX(Component.LEFT_ALIGNMENT);

        title.setFont(titleFont);
        error.setFont(subtitleFont);
        error.setForeground(Color.RED);
        subtitle1.setFont(subtitleFont);
		forenameLabel.setFont(labelFont);
        surnameLabel.setFont(labelFont);
        subtitle2.setFont(subtitleFont);
		houseNumLabel.setFont(labelFont);
        roadLabel.setFont(labelFont);
        cityLabel.setFont(labelFont);
        postcodeLabel.setFont(labelFont);

        add(title);
        add(Box.createVerticalStrut(5));
        add(error);
        add(Box.createVerticalStrut(10));
        add(subtitle1);
        add(Box.createVerticalStrut(10));
        add(forenameLabel);
        add(forename);
        add(Box.createVerticalStrut(5));
        add(surnameLabel);
        add(surname);
        add(Box.createVerticalStrut(10));
        add(subtitle2);
        add(Box.createVerticalStrut(5));
        add(houseNumLabel);
        add(houseNum);
        add(Box.createVerticalStrut(5));
        add(roadLabel);
        add(road);
        add(Box.createVerticalStrut(5));
        add(cityLabel);
        add(city);
        add(Box.createVerticalStrut(5));
        add(postcodeLabel);
        add(postcode);
    }

    /**
     * @return the entered forename
     */
    public String getForename() {
        return forename.getText();
    }

    /**
     * @return the entered surname
     */
    public String getSurname() {
        return surname.getText();
    }

    /**
     * @return the entered house number
     */
    public String getHouseNum() {
        return houseNum.getText();
    }

    /**
     * @return the entered road name
     */
    public String getRoad() {
        return road.getText();
    }

    /**
     * @return the entered city
     */
    public String getCity() {
        return city.getText();
    }

    /**
     * @return the entered postcode
     */
    public String getPostcode() {
        return postcode.getText();
    }

    /**
     * adds an error message if needed
     */
    public void setError(String text) {
        error.setText(text);
    }
    
}
