package team22.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import team22.businessLogicLayer.Sanitisation;
import team22.businessLogicLayer.Validation;
import team22.dataAccessLayer.*;

import java.awt.*;

public class CustomerDetail extends JPanel {

    private final Dimension PAGE_SIZE;

    private final JPanel CONTENT_PANE;

    private int width;

    /**
     * Used to create the page on which a customer changes their details
     * @param width the width of the page
     * @param height the height of the page
     */
    public CustomerDetail(int width, int height) {
        setBorder(new EmptyBorder(15, 15, 15, 15));

        this.width = width;

        PAGE_SIZE = new Dimension(width - 60, height - 60);
		CONTENT_PANE = new JPanel();
		CONTENT_PANE.setPreferredSize(PAGE_SIZE);
		CONTENT_PANE.setMaximumSize(PAGE_SIZE);
		CONTENT_PANE.setBackground(Color.WHITE);
		CONTENT_PANE.setBorder(new EmptyBorder(15, 15, 15, 15));
		CONTENT_PANE.setLayout(new BoxLayout(CONTENT_PANE, BoxLayout.PAGE_AXIS));

        buildLoginPage();
	}

    /**
     * Everything that needs to be added at the bottom of any subpage, prevents code repetition
     */
    private void contentPaneFooter() {
		add(CONTENT_PANE);
		CONTENT_PANE.revalidate();
		CONTENT_PANE.repaint();
		revalidate();
		repaint();
	}

    /**
     * Builds the 'login' page, where a customer enters their details
     */
    private void buildLoginPage() {
        removeAll();
		setLayout(new GridBagLayout());  // Use GridBagLayout to centre the form.

        int formWidth = width / 3;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel detailsTitle = new JLabel("Customer Login");

        JLabel incorrectDetails = new JLabel("");
		incorrectDetails.setForeground(Color.RED);

        JButton login = new JButton("Login");

        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
        Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24);

        detailsTitle.setFont(titleFont);
        incorrectDetails.setFont(labelFont);
        login.setFont(labelFont);

		// Alignment doesn't seem to work properly unless all components have the same alignment setting
        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        incorrectDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding
        panel.add(detailsTitle);
		panel.add(Box.createVerticalStrut(10));
        panel.add(incorrectDetails);
        panel.add(Box.createVerticalStrut(10));

        DetailsForm customerDetailsForm = new DetailsForm(new String[] {"Forename", "Surname", "House Number", "Postcode"},
                                                            new int[]{DetailsForm.NO_VALIDATION,
                                                                        DetailsForm.NO_VALIDATION,
                                                                        DetailsForm.IS_NUMBER,
                                                                        DetailsForm.IS_POSTCODE},
                                                            formWidth);

        panel.add(customerDetailsForm);

        add(Box.createVerticalStrut(15));
		panel.add(login);

        add(Box.createHorizontalGlue());
        add(panel);

        login.addActionListener(e -> {
            // Ensure entered details are valid - inform customer if not
            if (!customerDetailsForm.validInputs()) {
                return;
            }
            try {
                String forename = customerDetailsForm.getInput("Forename");
                String surname = customerDetailsForm.getInput("Surname");
                String houseNum = customerDetailsForm.getInput("House Number");
                String postcode = customerDetailsForm.getInput("Postcode");
                CustomerInfo customer = CustomerInfo.getCustomerInfo(forename, surname, Integer.parseInt(houseNum), postcode);
                if (customer.getCustomer() == null) {
                    incorrectDetails.setText("Login failed! Make sure your details are correct!");
                } else {
                    buildLandingPage(customer);
                    CONTENT_PANE.revalidate();
                    CONTENT_PANE.repaint();
                }
            } catch (Exception ex) {
                incorrectDetails.setText("Invalid details!");
            }
        });

        revalidate();
		repaint();
    }

    /**
     * Builds a page to display a customer's details to them
     * @param ci the CustomerInfo to display
     */
    private void buildLandingPage(CustomerInfo ci) {
        removeAll();
		CONTENT_PANE.removeAll();

		Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_BOLD, 24);
        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);
        Font textFont = Fonts.getSizedFont(Fonts.OPENSANS_LIGHT, 18);
		Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 16);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Customer Details");
        title.setFont(titleFont);
        JLabel nameLabel = new JLabel("Full name:");
        nameLabel.setFont(labelFont);
        JLabel name = new JLabel(ci.getCustomer().getName());
        name.setFont(textFont);
        JLabel addressLabel = new JLabel("Full address:");
        addressLabel.setFont(labelFont);
        JLabel address = new JLabel(ci.getAddress().toString());
        address.setFont(textFont);
        JButton edit = new JButton("Edit details");
        edit.setFont(buttonFont);
        JButton previous = new JButton("Log out");
        previous.setFont(buttonFont);

        edit.addActionListener(e -> buildEditPage(ci));
        
        previous.addActionListener(e -> buildLoginPage());

        panel.add(title);
        panel.add(nameLabel);
        panel.add(name);
        panel.add(addressLabel);
        panel.add(address);
        panel.add(Box.createVerticalStrut(5));
        panel.add(edit);
        panel.add(Box.createVerticalStrut(10));
        panel.add(previous);

        CONTENT_PANE.add(panel);

        contentPaneFooter();
	}

    /**
     * Builds an edit page, where a customer can edit their personal details
     * @param ci the CustomerInfo to edit
     */
    private void buildEditPage(CustomerInfo ci) {
        removeAll();
		CONTENT_PANE.removeAll();

        Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);

        JPanel formHolder = new JPanel();
		formHolder.setLayout(new BoxLayout(formHolder, BoxLayout.PAGE_AXIS));
		formHolder.setBackground(Color.WHITE);
		formHolder.setBorder(new EmptyBorder(15, 15, 15, 15));

        CustomerForm form = new CustomerForm(ci, new Dimension(PAGE_SIZE.width / 2, PAGE_SIZE.height * 3 /4));

        JButton save = new JButton("Save details");
        save.setFont(buttonFont);
        
        JButton previous = new JButton("Return");
        previous.setFont(buttonFont);

        // Performs basic input validation before updating a customers details if validation passes
        save.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to update details?", 
                "Update details?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) { 
                if (form.getForename().equals("") || form.getSurname().equals("") || 
                    form.getHouseNum().equals("") || form.getRoad().equals("") || 
                    form.getCity().equals("") || form.getPostcode().equals("") ||
                    !Validation.isValidPostcode(Sanitisation.sanitisePostcode(form.getPostcode())) ||
                    !Validation.isValidNumber(form.getHouseNum())){
                    form.setError("Please ensure your details are valid!");;
                } else {
                    try {
                        Customer customer = ci.getCustomer();

                        boolean addressSuccess = ci.updateCustomerAddress(Integer.parseInt(form.getHouseNum()), form.getPostcode(), form.getRoad(), form.getCity());

                        if (addressSuccess) {
                            customer.setForename(form.getForename());
                            customer.setSurname(form.getSurname());

                            boolean updateSuccess = Customer.updateCustomer(customer);

                            if (updateSuccess) {
                                OptionPanes.showSuccessPane("Successfully edited details!");
                            }
                        }

                        buildLandingPage(ci);

                    } catch (Exception ex) {
                        form.setError("Error! Please make sure you are entering your details correctly!");
                    } 
                }
            }
        });

        previous.addActionListener(e -> buildLandingPage(ci));

        formHolder.add(form);
        formHolder.add(Box.createVerticalStrut(10));
        formHolder.add(save);
        formHolder.add(Box.createVerticalStrut(10));
        formHolder.add(previous);

        CONTENT_PANE.add(formHolder);

        contentPaneFooter();
	}
}
