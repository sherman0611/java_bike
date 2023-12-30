package team22.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import team22.dataAccessLayer.*;

import java.awt.*;

public class TrackOrder extends JPanel {

    private final Dimension PAGE_SIZE;

    private static final JPanel RETURN_TO_MENU = new JPanel(new GridBagLayout());
    private Dimension returnButtonSize;

    private OrderInfo[] displayed;

    /**
     * The page that will allow a customer to track their orders
     * @param width the width of the page
     * @param height the height of the page
     */
    public TrackOrder(int width, int height) {
        PAGE_SIZE = new Dimension(width - 60, height - 60);

        setMinimumSize(PAGE_SIZE);
        setPreferredSize(PAGE_SIZE);
        setMaximumSize(PAGE_SIZE);

        Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);
        Dimension returnPanelSize = new Dimension(PAGE_SIZE.width, Fonts.getHeight(buttonFont) * 6/5);
        returnButtonSize = new Dimension(Fonts.getStringWidth(buttonFont, "Return") * 2, Fonts.getHeight(buttonFont) * 6/5);

        RETURN_TO_MENU.setPreferredSize(returnPanelSize);
        RETURN_TO_MENU.setMaximumSize(returnPanelSize);
        RETURN_TO_MENU.setBackground(Color.WHITE);

        JButton returnButton = new JButton("Return");
        returnButton.setPreferredSize(returnButtonSize);
        returnButton.setMaximumSize(returnButtonSize);
        returnButton.setFont(buttonFont);

        returnButton.addActionListener(e -> initPage());

        RETURN_TO_MENU.add(returnButton);

        setBorder(new EmptyBorder(15, 15, 15, 15));

        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));

        initPage();
	}

    /**
     * Initialises the page
     */
    private void initPage() {
        removeAll();
        setBackground(UIManager.getColor("Panel.background"));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        int formWidth = PAGE_SIZE.width / 3;

        JPanel orderNumPanel = new JPanel();
        orderNumPanel.setLayout(new BoxLayout(orderNumPanel, BoxLayout.PAGE_AXIS));
        orderNumPanel.setBackground(Color.WHITE);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        JLabel orderTitle = new JLabel("Track by Order Number");
        JLabel incorrectOrderNum = new JLabel("");
        incorrectOrderNum.setForeground(Color.RED);
        JButton orderSubmit = new JButton("Track order");

        JLabel detailsTitle = new JLabel("Track by Details");
        JLabel incorrectDetails = new JLabel("");
        incorrectDetails.setForeground(Color.RED);
        JButton detailsSubmit = new JButton("Track order");

        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
        Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24);

        orderTitle.setFont(titleFont);
        incorrectOrderNum.setFont(labelFont);
        orderSubmit.setFont(labelFont);

        detailsTitle.setFont(titleFont);
        incorrectDetails.setFont(labelFont);
        detailsSubmit.setFont(labelFont);

        // Alignment doesn't seem to work properly unless all components have the same alignment setting
        orderTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        incorrectOrderNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        orderSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        incorrectDetails.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);

        orderNumPanel.setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding
        orderNumPanel.add(orderTitle);
        orderNumPanel.add(Box.createVerticalStrut(10));
        orderNumPanel.add(incorrectOrderNum);
        orderNumPanel.add(Box.createVerticalStrut(10));

        DetailsForm orderNumberForm = new DetailsForm(new String[] {"Order Number"}, new int[]{DetailsForm.IS_NUMBER},
          formWidth);
        orderNumPanel.add(orderNumberForm);
        add(Box.createVerticalStrut(15));
        orderNumPanel.add(orderSubmit);

        detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding
        detailsPanel.add(detailsTitle);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(incorrectDetails);
        detailsPanel.add(Box.createVerticalStrut(10));

        DetailsForm customerDetailsForm = new DetailsForm(new String[] {"Forename", "Surname", "House Number", "Postcode"},
          new int[]{DetailsForm.NO_VALIDATION,
            DetailsForm.NO_VALIDATION,
            DetailsForm.IS_NUMBER,
            DetailsForm.IS_POSTCODE},
          formWidth);

        detailsPanel.add(customerDetailsForm);

        add(Box.createVerticalStrut(15));
        detailsPanel.add(detailsSubmit);

        add(Box.createHorizontalGlue());
        add(orderNumPanel);
        add(Box.createHorizontalStrut(25));
        add(detailsPanel);
        add(Box.createHorizontalStrut(50));

        orderSubmit.addActionListener(e -> {
            if (!orderNumberForm.validInputs()) {
                return;
            }
            try {
                OrderInfo[] order = OrderInfo.get(Integer.parseInt(orderNumberForm.getInput("Order Number")));
                displayed = order;
                if (order[0] == null) {
                    incorrectOrderNum.setText("No orders found!");
                } else {
                    buildOrderDisplay(order);
                }
            } catch (Exception ex) {
                incorrectOrderNum.setText("Invalid order number!");
            }
        });

        detailsSubmit.addActionListener(e -> {
            // Ensure entered details are valid - inform customer if not
            if (!customerDetailsForm.validInputs()) {
                return;
            }
            try {
                String forename = customerDetailsForm.getInput("Forename");
                String surname = customerDetailsForm.getInput("Surname");
                String houseNum = customerDetailsForm.getInput("House Number");
                String postcode = customerDetailsForm.getInput("Postcode");
                OrderInfo[] orders = OrderInfo.get(forename, surname, Integer.parseInt(houseNum), postcode).toArray(new OrderInfo[0]);
                if (orders.length == 0) {
                    incorrectDetails.setText("No orders found!");
                } else {
                    displayed = orders;
                    buildOrderDisplay(orders);
                }
            } catch (Exception ex) {
                incorrectDetails.setText("Invalid details!");
            }
        });

        revalidate();
        repaint();
    }

    /**
     * Builds an OrderDisplay for a set of orders
     * @param orders the orders to build an OrderDisplay for
     */
    protected void buildOrderDisplay(OrderInfo[] orders) {
        removeAll();
        JPanel holder = new JPanel();
        holder.setBorder(new EmptyBorder(15, 15, 15, 15));
        holder.setLayout(new BoxLayout(holder, BoxLayout.PAGE_AXIS));
        holder.setPreferredSize(PAGE_SIZE);
        holder.setMaximumSize(PAGE_SIZE);
        holder.setBackground(Color.WHITE);
        holder.add((new OrderDisplay(orders, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (45 + returnButtonSize.height)))).getScroller());
        holder.add(Box.createVerticalStrut(15));
        holder.add(RETURN_TO_MENU);
        add(holder);
        revalidate();
        repaint();
    }

    /**
     * @return an array of currently displayed OrderInfos
     */
    protected OrderInfo[] getDisplayedOrders() {
        return displayed;
    }

    /**
     * Builds a panel that will allow a customer to edit their order
     * @param oi the OrderInfo to be edited
     * @param orders all Orders that are being displayed (for when a customer clicks the back button)
     */
    protected void buildEditOrderPanel(OrderInfo oi, OrderInfo[] orders) {
        setBackground(UIManager.getColor("Panel.background"));
        Order order = oi.getOrder();
        Customer customer = oi.getCustomer();

        removeAll();
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(new EmptyBorder(0, 15, 0, 15));

        JScrollPane scroller = new JScrollPane(panel);
        scroller.setViewportBorder(null);
        scroller.setBackground(Color.WHITE);
        scroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setMinimumSize(new Dimension(PAGE_SIZE.width * 3/4 + 45, PAGE_SIZE.height - (15 + returnButtonSize.height)));
        scroller.setPreferredSize(new Dimension(PAGE_SIZE.width * 3/4 + 45, PAGE_SIZE.height - (15 + returnButtonSize.height)));
        scroller.setMaximumSize(new Dimension(PAGE_SIZE.width * 3/4 + 45, PAGE_SIZE.height - (15 + returnButtonSize.height)));

        CustomerInfo ci = oi.getCustomerInfo();

        CustomerForm form = new CustomerForm(ci, new Dimension(PAGE_SIZE.width * 3/4, PAGE_SIZE.height * 3/4));

        JLabel subtitle3 = new JLabel("Bike details");
        JLabel bikeNameLabel = new JLabel("Bike name:");
		JTextField bikeName = new JTextField(32);
        bikeName.setText(order.getBikeName());
        bikeName.setMinimumSize(new Dimension(PAGE_SIZE.width * 3/4, 40));
        bikeName.setMaximumSize(new Dimension(PAGE_SIZE.width * 3/4, 40));
        bikeName.setPreferredSize(new Dimension(PAGE_SIZE.width * 3/4, 40));

        JButton save = new JButton("Save details");
        JButton previous = new JButton("Back to order view");

        save.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to update details?", 
                "Update details?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) { 
                if (form.getForename().equals("") || form.getSurname().equals("") || 
                    form.getHouseNum().equals("") || form.getRoad().equals("") || 
                    form.getCity().equals("") || form.getPostcode().equals("") ||
                    bikeName.getText().equals("")) {
                    form.setError("Please enter you details!");;
                } else {
                    try {
                        boolean addressSuccess = ci.updateCustomerAddress(Integer.parseInt(form.getHouseNum()), form.getPostcode(), form.getRoad(), form.getCity());

                        if (addressSuccess) {
                            customer.setForename(form.getForename());
                            customer.setSurname(form.getSurname());

                            boolean updateSuccess = Customer.updateCustomer(customer);

                            if (updateSuccess) {
                                order.updateBikeName(bikeName.getText());

                                for(OrderInfo info : orders) {
                                    info.setAddress(ci.getAddress());
                                    info.setCustomer(customer);
                                }
                                OptionPanes.showSuccessPane("Successfully edited details!");
                            }
                        }

                        buildOrderDisplay(orders);
                    } catch (Exception ex) {
                        form.setError("Error! Please make sure you are entering your details correctly!");
                    } 
                }
            }
        });

        previous.addActionListener(e -> {
			removeAll();
            displayed = orders;
			buildOrderDisplay(orders);
			revalidate();
			repaint();
		});

        subtitle3.setAlignmentX(Component.LEFT_ALIGNMENT);
        bikeNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bikeName.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        previous.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        Font subtitleFont = Fonts.getSizedFont(Fonts.OPENSANS_BOLD, 22);
        Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
        Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);
        
        subtitle3.setFont(subtitleFont);
		bikeNameLabel.setFont(labelFont);
        save.setFont(buttonFont);
        previous.setFont(buttonFont);
        
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding
        panel.add(form);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitle3);
        panel.add(Box.createVerticalStrut(10));
        panel.add(bikeNameLabel);
        panel.add(bikeName);
        panel.add(Box.createVerticalStrut(10));
        panel.add(save);
        panel.add(Box.createVerticalStrut(10));
        panel.add(previous);

        add(scroller);

        revalidate();
        repaint();
    }

}
