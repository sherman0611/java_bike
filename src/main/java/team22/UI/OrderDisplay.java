package team22.UI;

import team22.businessLogicLayer.StringUtils;
import team22.dataAccessLayer.BikeComponent;
import team22.dataAccessLayer.Customer;
import team22.dataAccessLayer.Order;
import team22.dataAccessLayer.OrderInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class OrderDisplay extends JPanel {
    private static final Font TITLE_FONT = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 20);
    private static final Font SUB_FONT = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
    private static final Font DETAILS_FONT = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 16);

    /** The staff page (if any) that this is a child of */
    private StaffAccess parentStaffPage = null;

    /** The order tracker (if any) that this is a child of */
    private TrackOrder parentOrderTracker = null;

    /** Stores the info in the orders to be displayed */
    private OrderInfo[] orders;

    /** Holds the order display in a JScrollPane */
    private JScrollPane orderScroller;

    /** Sets the size of the JScrollPane */
    private Dimension scrollerDim;

    /**
     * Creates an order display window
     * @param ois The orders to display
     * @param to The order tracker that the customer buttons should interact with
     * @param size The size of the scroller
     */
    public OrderDisplay(OrderInfo[] ois, TrackOrder to, Dimension size) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        parentOrderTracker = to;
        orders = ois;

        scrollerDim = size;

        initOrderPanel();
    }

    /**
     * Creates an order display window
     * @param ois The orders to display
     * @param sa The staff page that the staff buttons should interact with
     * @param size The size of the scroller
     */
    public OrderDisplay(OrderInfo[] ois, StaffAccess sa, Dimension size) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        parentStaffPage = sa;
        orders = ois;

        scrollerDim = size;

        initOrderPanel();
    }

    /**
     * Initialises the order display
     */
    private void initOrderPanel() {
        orderScroller = new JScrollPane(this);
        orderScroller.setViewportBorder(null);
        orderScroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        orderScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        orderScroller.setMinimumSize(scrollerDim);
        orderScroller.setPreferredSize(scrollerDim);
        orderScroller.setMaximumSize(scrollerDim);

        for (int i=0; i<orders.length; i++) {
            add(buildOrderWidget(orders[i]));
            if (i<orders.length-1) {
                // Only add a strut beneath the order widget if there is another one coming afterwards
                add(Box.createVerticalStrut(35));
            }
        }
    }

    /**
     * @param oi The OrderInfo to build a widget for
     * @return a JPanel containing all necessary information about the order
     */
    private JPanel buildOrderWidget(OrderInfo oi) {
        JPanel order = new JPanel();
        order.setLayout(new BoxLayout(order, BoxLayout.PAGE_AXIS));
        order.setBorder(new EmptyBorder(20, 20, 20, 20));

        Order o = oi.getOrder();

        JLabel titleLabel = new JLabel("Order Number: " + o.getOrderNumber());
        titleLabel.setFont(TITLE_FONT);

        String byText = "";
        if (o.getStaff() != null) {
            byText = " By " + o.getStaff();
        }

        JLabel statusLabel = new JLabel("Status: " + StringUtils.titleCase(o.getStatus()) + byText);
        statusLabel.setFont(SUB_FONT);

        JLabel dateTimeLabel = new JLabel("Placed on: " + o.getDatetime());
        dateTimeLabel.setFont(DETAILS_FONT);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel orderDetailsButton = new JPanel();
        orderDetailsButton.setLayout(new BoxLayout(orderDetailsButton, BoxLayout.LINE_AXIS));

        JPanel orderDetails = new JPanel();
        orderDetails.setLayout(new BoxLayout(orderDetails, BoxLayout.PAGE_AXIS));
        orderDetails.add(titleLabel);
        orderDetails.add(statusLabel);
        orderDetails.add(dateTimeLabel);

        JButton expandOrderButton = new JButton("Expand order details");
        expandOrderButton.setFont(SUB_FONT);

        orderDetailsButton.add(orderDetails);
        orderDetailsButton.add(Box.createHorizontalGlue());
        orderDetailsButton.add(expandOrderButton);

        orderDetailsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        order.add(orderDetailsButton);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));

        buildOrderInfo(detailsPanel, oi);

        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setVisible(false);
        order.add(detailsPanel);

        expandOrderButton.addActionListener(e -> expandOrder(expandOrderButton, detailsPanel));

        return order;
    }

    /**
     * Expands an order to show the full details
     * @param expandOrderButton the button to expand/hide the details
     * @param details the hidden JPanel containing the details
     */
    private void expandOrder(JButton expandOrderButton, JPanel details) {
        details.setVisible(true);
        expandOrderButton.setText("Collapse order details");

        for (ActionListener l : expandOrderButton.getActionListeners()) {
            expandOrderButton.removeActionListener(l);
        }

        expandOrderButton.addActionListener(e -> collapseOrder(expandOrderButton, details));
    }

    /**
     * Collapses an order, hiding the details
     * @param expandOrderButton the button that shows/hides order details
     * @param details the visible order details
     */
    private void collapseOrder(JButton expandOrderButton, JPanel details) {
        details.setVisible(false);
        expandOrderButton.setText("Expand order details");

        for (ActionListener l : expandOrderButton.getActionListeners()) {
            expandOrderButton.removeActionListener(l);
        }

        expandOrderButton.addActionListener(e -> expandOrder(expandOrderButton, details));
    }

    /**
     * Collects all the details about a given order, and places them onto a JPanel
     * @param order the JPanel on which to place the order details
     * @param oi the order to collect details about
     */
    private void buildOrderInfo(JPanel order, OrderInfo oi) {
        Order o = oi.getOrder();
        BikeComponent[] comps = oi.getComponents();
        Customer c = oi.getCustomer();
        String[] addressDisplay = oi.getAddress().getDisplayLines();

        order.add(Box.createVerticalStrut(15));

        // Build the info boxes for shipping information and bike information
        buildInfoBox(order, new String[]{
          "Ship to: ",
          c.getName(),
          addressDisplay[0],
          addressDisplay[1],
          addressDisplay[2]
        });
        buildInfoBox(order, new String[]{
          "Bike info: ",
          "Bike name: " + o.getBikeName(),
          "Bike brand: " + StringUtils.titleCase(o.getBikeBrand()),
          "Bike serial #: " + o.getBikeSerial(),
          "Components: ",
          " ¬ " + comps[0].getDisplayName() + " (Serial #: " + comps[0].getSerial() + ") - " + StringUtils.formatMoney(comps[0].getPrice()),
          " ¬ " + comps[1].getDisplayName() + " (Serial #: " + comps[1].getSerial() + ") - " + StringUtils.formatMoney(comps[1].getPrice()),
          " ¬ " + comps[2].getDisplayName() + " (Serial #: " + comps[2].getSerial() + ") - " + StringUtils.formatMoney(comps[2].getPrice()),
          "Total price: " + StringUtils.formatMoney(comps[0].getPrice() + comps[1].getPrice() + comps[2].getPrice())
        });

        // Add the necessary buttons
        if (parentStaffPage != null) {
            addStaffButtons(order, oi);
        } else if (parentOrderTracker != null) {
            addCustomerButtons(order, oi);
        }
    }

    /**
     * Builds an info box
     * @param panel the panel to build the info box on
     * @param labels the information that should be put in the box
     */
    private void buildInfoBox(JPanel panel, String[] labels) {
        JLabel titleLabel = new JLabel(labels[0]);
        titleLabel.setFont(SUB_FONT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        // Create a label for each required info field
        for (int i=1; i<labels.length; i++) {
            JLabel detail = new JLabel(labels[i]);
            if (labels[i].startsWith(" ")) {
                // If there are leading spaces in the string, indent the label
                int numSpaces = labels[i].indexOf(labels[i].trim());
                detail.setBorder(new EmptyBorder(0, 30 * numSpaces, 0, 0));
            }
            detail.setAlignmentX(Component.LEFT_ALIGNMENT);
            detail.setFont(DETAILS_FONT);
            panel.add(Box.createVerticalStrut(5));
            panel.add(detail);
        }
        panel.add(Box.createVerticalStrut(10));
    }

    /**
     * Adds the buttons that a customer can interact with
     * @param order the panel upon which to place the buttons
     * @param oi the order that the buttons should interact with
     */
    private void addCustomerButtons(JPanel order, OrderInfo oi) {
        OrderInfo[] ois = parentOrderTracker.getDisplayedOrders();
        Order o = oi.getOrder();
        JPanel actionButtons = new JPanel();
        actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.LINE_AXIS));
        if (o.getStatus().equals("PENDING")) {

            // Edit button - calls the required method in parentOrderTracker
            JButton editButton = new JButton("Edit Order Details");
            editButton.setFont(SUB_FONT);
            int editWidth = Fonts.getStringWidth(SUB_FONT, "Edit Order Details") * 3/2;
            editButton.setPreferredSize(new Dimension(editWidth, 50));
            editButton.setMaximumSize(new Dimension(editWidth, 50));

            editButton.addActionListener(e -> {
                parentOrderTracker.buildEditOrderPanel(oi, ois);
            });

            JButton deleteButton = new JButton("Delete Order");
            deleteButton.setFont(SUB_FONT);
            int deleteWidth = Fonts.getStringWidth(SUB_FONT, "Delete Order") * 3/2;
            deleteButton.setPreferredSize(new Dimension(deleteWidth, 50));
            deleteButton.setMaximumSize(new Dimension(deleteWidth, 50));

            // deleteButton deletes the order and rebuilds the order display
            deleteButton.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this order?",
                  "Delete order?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    Order.deleteOrder(o.getOrderNumber());
                    parentOrderTracker.buildOrderDisplay(Arrays.stream(ois).filter(info -> info.getOrder().getOrderNumber() != o.getOrderNumber()).toArray(OrderInfo[]::new));
                }
            });

            actionButtons.add(editButton);
            actionButtons.add(Box.createHorizontalStrut(15));
            actionButtons.add(deleteButton);
        }

        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        order.add(actionButtons);
    }

    /**
     * @param order the JPanel upon which to place the staff buttons
     * @param oi the order that the staff buttons should interact with
     */
    private void addStaffButtons(JPanel order, OrderInfo oi) {
        Order o = oi.getOrder();
        JPanel actionButtons = new JPanel();
        actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.LINE_AXIS));

        /*
        Different buttons needed for different statuses - if pending, need a button to receive payment from customer
        and then confirm the order
        If confirmed, need a button to check quantities of the order's components, and then fulfill it
         */
        if (o.getStatus().equals("PENDING")) {
            JButton progressButton = new JButton("Receive Payment from Customer");
            progressButton.setFont(SUB_FONT);
            int progressWidth = Fonts.getStringWidth(SUB_FONT, "Receive Payment from Customer") * 6 / 5;
            progressButton.setPreferredSize(new Dimension(progressWidth, 50));
            progressButton.setMaximumSize(new Dimension(progressWidth, 50));
            progressButton.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "Received payment from customer?", "Customer payment", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    if (oi.progressOrder(parentStaffPage.getStaffName())) {
                        parentStaffPage.buildOrdersPage();
                        OptionPanes.showSuccessPane("Order #" + o.getOrderNumber() + " confirmed!");
                    } else {
                        OptionPanes.showErrorPane("Error progressing order!\nOrder remains unchanged.", OptionPanes.RECOVERABLE);
                    }
                }
            });

            actionButtons.add(progressButton);
        } else if (o.getStatus().equals("CONFIRMED")) {
            JButton progressButton = new JButton("Fulfil Order");
            int progressWidth = Fonts.getStringWidth(SUB_FONT, "Fulfil Order") * 6 / 5;
            progressButton.setPreferredSize(new Dimension(progressWidth, 50));
            progressButton.setMaximumSize(new Dimension(progressWidth, 50));

            progressButton.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(null, "Fulfil Order?",
                  "Fulfil Order?", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }

                ArrayList<BikeComponent> insufficientQtys = oi.missingComps();
                if (insufficientQtys == null) {
                    OptionPanes.showErrorPane("Error fetching product quantities. Please try again.", OptionPanes.RECOVERABLE);
                } else if (insufficientQtys.size() > 0) {
                    StringBuilder sb = new StringBuilder("Insufficient Quantities for:");
                    for (BikeComponent bc : insufficientQtys) {
                        sb.append("\n").append(bc.getDisplayName()).append(" (Serial #: ").append(bc.getSerial()).append(")")
                          .append("\n-Available: ").append(bc.getQuantity())
                          .append("\n-Needed: ").append(bc.getDisplayName().contains("Wheel") ? '2' : '1');
                    }
                    OptionPanes.showErrorPane(sb.toString(), OptionPanes.WARNING);
                } else {
                    if (!oi.progressOrder(parentStaffPage.getStaffName())) {
                        OptionPanes.showErrorPane("Error progressing order!\nOrder remains unchanged.", OptionPanes.WARNING);
                    } else {
                        parentStaffPage.buildOrdersPage();
                        OptionPanes.showSuccessPane("Order #" + o.getOrderNumber() + " fulfilled!");
                    }
                }
            });

            actionButtons.add(progressButton);
        }

        actionButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        order.add(actionButtons);
    }

    /**
     * @return the JScrollPane containing the order display
     */
    public JScrollPane getScroller() {
        return orderScroller;
    }
}