package team22.UI;

import team22.businessLogicLayer.SaveOrder;
import team22.dataAccessLayer.BikeComponent;
import team22.dataAccessLayer.FrameSet;
import team22.dataAccessLayer.Handlebar;
import team22.dataAccessLayer.Wheel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class SaveOrderPanel extends JPanel {
    private int height;
    private BikeComponent[] order;

    /**
     * Creates the saveOrder panel used to collect user details and pass them on to SaveOrder business logic
     * @param width the width of the panel
     * @param height the height of the panel
     * @param order the current order of three components
     */
    protected SaveOrderPanel(int width, int height, BikeComponent[] order) {
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding

        this.height = height;
        this.order = order;

        buildOptionPanel();
    }

    /**
     * Builds the panel that a shopper uses to tell the system if they are an existing customer or not
     */
    private void buildOptionPanel() {
        JButton newCustomer = new JButton("I am a new customer");
        JButton existingCustomer = new JButton("I am an existing customer");
        JButton returnButton = new JButton("Back to bike builder");

        Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 18);
        Dimension buttonSize = new Dimension(Fonts.getStringWidth(buttonFont, "I am an existing customer") * 3 / 2, Fonts.getHeight(buttonFont) * 2);

        newCustomer.setPreferredSize(buttonSize);
        newCustomer.setMaximumSize(buttonSize);
        existingCustomer.setPreferredSize(buttonSize);
        existingCustomer.setMaximumSize(buttonSize);
        returnButton.setPreferredSize(buttonSize);
        returnButton.setMaximumSize(buttonSize);

        newCustomer.setFont(buttonFont);
        existingCustomer.setFont(buttonFont);
        returnButton.setFont(buttonFont);

        newCustomer.setAlignmentX(Component.CENTER_ALIGNMENT);
        existingCustomer.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        newCustomer.addActionListener(e -> buildOrderForm(false));
        existingCustomer.addActionListener(e -> buildOrderForm(true));
        returnButton.addActionListener(e -> ((BikeBuilder) getParent().getParent()).buildSelectionPanel());

        add(Box.createVerticalStrut(((height - 80) - buttonSize.height * 3) / 2));
        add(newCustomer);
        add(Box.createVerticalStrut(40));
        add(existingCustomer);
        add(Box.createVerticalStrut(40));
        add(returnButton);
        add(Box.createVerticalStrut(((height - 80) - buttonSize.height * 3) / 2));
    }

    /**
     * Prevents massive repetition of swing code when creating order form for either existing or new customer
     * @param existing whether the customer is an existing one or not
     */
    private void buildOrderForm(boolean existing) {
        removeAll();
        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);

        JLabel detailsTitle = new JLabel("Input Details to Order");
        JLabel incorrectDetails = new JLabel("");
        incorrectDetails.setForeground(Color.RED);
        JButton detailsSubmit = new JButton("Place Order");
        JButton returnButton = new JButton("Return");

        detailsTitle.setFont(Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24));
        incorrectDetails.setFont(labelFont);
        detailsSubmit.setFont(labelFont);
        returnButton.setFont(labelFont);

        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        incorrectDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        returnButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(detailsTitle);
        add(Box.createVerticalStrut(10));
        add(incorrectDetails);
        add(Box.createVerticalStrut(10));

        // Set fields depending on whether customer is existing or not - whether they need to fully input their address
        String[] fields;
        int[] validation;
        if (existing) {
            fields = new String[] {"Bike Name", "Forename", "Surname", "House Number", "Postcode"};
            validation = new int[] {DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION,
                                    DetailsForm.IS_NUMBER, DetailsForm.IS_POSTCODE};
        } else {
            fields = new String[] {"Bike Name", "Forename", "Surname", "House Number", "Street Name", "City/Town", "Postcode"};
            validation = new int[] {DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION,
                                    DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION,
                                    DetailsForm.IS_POSTCODE};
        }

        DetailsForm detailsForm = new DetailsForm(fields, validation, getMaximumSize().width);  // Create a form using relevant fields
        JPanel formHolder = new JPanel();

        formHolder.setPreferredSize(new Dimension(getMaximumSize().width, detailsForm.getHeight()));
        formHolder.setMaximumSize(new Dimension(getMaximumSize().width, detailsForm.getHeight()));

        // I don't know why but detailsForm needs to be added to this container otherwise it'll mess up its sizing
        formHolder.add(detailsForm);
        add(detailsForm);
        add(Box.createVerticalStrut(15));
        add(detailsSubmit);
        add(Box.createVerticalStrut(10));
        add(returnButton);

        revalidate();
        repaint();

        if (existing) {
            detailsSubmit.addActionListener(e -> {
                if (detailsForm.validInputs()) {
                    onSaveOrderActionExistingCustomer(detailsForm, order);
                }
            });
        } else {
            detailsSubmit.addActionListener(e -> {
                if (detailsForm.validInputs()) {
                    onSaveOrderActionNewCustomer(detailsForm, order);
                }
            });
        }

        returnButton.addActionListener(e -> {
            removeAll();
            buildOptionPanel();
            revalidate();
            repaint();
        });
    }

    /**
     * Calls saveOrderNewCustomer
     * @param df the DetailsForm from which to get a customer's details
     * @param order the components of the bike being checked out
     */
    private void onSaveOrderActionNewCustomer(DetailsForm df, BikeComponent[] order) {
        String bikeName = df.getInput("Bike Name");
        String forename = df.getInput("Forename");
        String surname = df.getInput("Surname");
        int houseNum = Integer.parseInt(df.getInput("House Number"));
        String streetName = df.getInput("Street Name");
        String cityName = df.getInput("City/Town");
        String postcode = df.getInput("Postcode");
        int orderNum = SaveOrder.saveOrderNewCustomer(forename, surname, postcode, houseNum, streetName, cityName,
                        bikeName, (FrameSet) order[0], (Handlebar) order[1], (Wheel) order[2]);
        if (orderNum == -1) {
            showOrderFailedDialog();
        } else {
            showOrderSuccessDialog(orderNum);
        }
    }

    /**
     * Calls saveOrderExistingCustomer
     * @param df the DetailsForm from which to get a customer's details
     * @param order the components of the bike being checked out
     */
    private void onSaveOrderActionExistingCustomer(DetailsForm df, BikeComponent[] order) {
        String bikeName = df.getInput("Bike Name");
        String forename = df.getInput("Forename");
        String surname = df.getInput("Surname");
        int houseNum = Integer.parseInt(df.getInput("House Number"));
        String postcode = df.getInput("Postcode");
        int orderNum = SaveOrder.saveOrderExistingCustomer(forename, surname, postcode, houseNum, bikeName,
                                                                (FrameSet) order[0], (Handlebar) order[1], (Wheel) order[2]);
        if (orderNum == -1) {
            showOrderFailedDialog();
        } else {
            showOrderSuccessDialog(orderNum);
        }
    }

    /**
     * Shows the customer that their order has gone through successfully
     */
    private void showOrderSuccessDialog(int orderNum) {
        Font dialogFont = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 24);
        String successString = "Successfully saved order!\nYour order number is: " + orderNum +
                                 "\nPlease visit a member of staff and quote this number to complete your payment.";
        try {
            int newImgDim = Fonts.getHeight(dialogFont) * 3 / 2;
            Image saveOrderIconImg = Images.loadSizedImage(Images.CHECK2_CIRCLE, newImgDim, newImgDim);
            ImageIcon saveOrderIcon = new ImageIcon(saveOrderIconImg);
            JOptionPane.showMessageDialog(null, successString, "Order placed!", JOptionPane.INFORMATION_MESSAGE, saveOrderIcon);
        } catch (IOException | NullPointerException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, successString, "Order placed!", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            // Clear bike builder to allow another customer to use it. getParent() is left panel of bikeBuilder, so get parent again
            ((BikeBuilder) getParent().getParent()).reset();
        }
    }

    /**
     * Shows the customer that their order has failed to go through
     */
    private void showOrderFailedDialog() {
        Font dialogFont = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 24);
        try {
            int newImgDim = Fonts.getHeight(dialogFont) * 3 / 2;
            Image orderFailedIconImg = Images.loadSizedImage(Images.EXCLAMATION_CIRCLE, newImgDim, newImgDim);
            ImageIcon orderFailedIcon = new ImageIcon(orderFailedIconImg);
            JOptionPane.showMessageDialog(null, "Order failed to place", "Order failure", JOptionPane.ERROR_MESSAGE, orderFailedIcon);
        } catch (IOException | NullPointerException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, "Order failed to place", "Order failure", JOptionPane.ERROR_MESSAGE);
        }
    }
}
