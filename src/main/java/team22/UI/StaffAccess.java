package team22.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import team22.businessLogicLayer.Accounts;
import team22.businessLogicLayer.Sanitisation;
import team22.dataAccessLayer.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;


public class StaffAccess extends JPanel {

	private static final JPanel RETURN_TO_MENU = new JPanel(new GridBagLayout());

	private StaffMember loggedInStaff = null;

	private final Dimension PAGE_SIZE;

	private Dimension returnButtonSize;

	private final JPanel CONTENT_PANE;

	private OrderInfo[] allOrders;

	private CustomerInfo[] allCustomers;

	/**
	 * The Staff Access UI panel
	 * @param width the width of the page
	 * @param height the height of the page
	 */
	public StaffAccess(int width, int height) {
		setBorder(new EmptyBorder(15, 15, 15, 15));

		PAGE_SIZE = new Dimension(width - 60, height - 60);
		CONTENT_PANE = new JPanel();
		CONTENT_PANE.setPreferredSize(PAGE_SIZE);
		CONTENT_PANE.setMaximumSize(PAGE_SIZE);
		CONTENT_PANE.setBackground(Color.WHITE);
		CONTENT_PANE.setBorder(new EmptyBorder(15, 15, 15, 15));
		CONTENT_PANE.setLayout(new BoxLayout(CONTENT_PANE, BoxLayout.PAGE_AXIS));

		Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);
		Dimension returnPanelSize = new Dimension(PAGE_SIZE.width, Fonts.getHeight(buttonFont) * 6/5);
		returnButtonSize = new Dimension(Fonts.getStringWidth(buttonFont, "Return to menu") * 3/2, Fonts.getHeight(buttonFont) * 6/5);

		RETURN_TO_MENU.setPreferredSize(returnPanelSize);
		RETURN_TO_MENU.setMaximumSize(returnPanelSize);
		RETURN_TO_MENU.setBackground(Color.WHITE);

		JButton returnButton = new JButton("Return to menu");
		returnButton.setPreferredSize(returnButtonSize);
		returnButton.setMaximumSize(returnButtonSize);
		returnButton.setFont(buttonFont);

		returnButton.addActionListener(e -> buildLandingPage());

		RETURN_TO_MENU.add(returnButton);

		buildLoginPage();
	}

	/**
	 * Everything that needs to be added at the bottom of any subpage, prevents code repetition
	 * @param returnButton whether to add a return to menu button (false when on menu)
	 */
	private void contentPaneFooter(boolean returnButton) {
		if (returnButton) {
			CONTENT_PANE.add(Box.createVerticalStrut(15));
			CONTENT_PANE.add(RETURN_TO_MENU);
		}
		add(CONTENT_PANE);
		CONTENT_PANE.revalidate();
		CONTENT_PANE.repaint();
		revalidate();
		repaint();
	}

	/**
	 * Builds the staff login page
	 */
	private void buildLoginPage() {
		removeAll();
		setLayout(new GridBagLayout());  // Use GridBagLayout to centre the form.

		JPanel compHolder = new JPanel();
		compHolder.setLayout(new BoxLayout(compHolder, BoxLayout.PAGE_AXIS));
		compHolder.setBackground(Color.WHITE);

		JLabel title = new JLabel("Staff login");

		JLabel incorrectLoginHint = new JLabel("");
		incorrectLoginHint.setForeground(Color.RED);

		JButton submit = new JButton("Log in");

		Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
		Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24);
		title.setFont(titleFont);
		incorrectLoginHint.setFont(labelFont);
		submit.setFont(labelFont);

		// Alignment doesn't seem to work properly unless all components have the same alignment setting
		incorrectLoginHint.setAlignmentX(Component.LEFT_ALIGNMENT);
		submit.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Set width of login form to 1.5 times the width of the invalid login hint, to ensure it will always fit
		int loginFormWidth = Fonts.getStringWidth(incorrectLoginHint.getFont(), "Invalid username/password!") * 3 / 2;
		DetailsForm loginForm = new DetailsForm(new String[] {"Username", "Password"},
		new int[] {DetailsForm.NO_VALIDATION, DetailsForm.IS_PASSWORD},
		loginFormWidth);

		compHolder.setBorder(new EmptyBorder(15, 15, 15, 15));  // Add padding
		compHolder.add(title);
		compHolder.add(incorrectLoginHint);
		compHolder.add(loginForm);
		compHolder.add(Box.createVerticalStrut(15));
		compHolder.add(submit);

		add(compHolder);

		submit.addActionListener(e -> {
			if (!loginForm.validInputs()) {
				return;
			}
			StaffMember acc = Accounts.login(loginForm.getInput("Username"), loginForm.getPasswordInput("Password"));
			if (acc == null) {
				incorrectLoginHint.setText("Invalid username/password!");
			} else {
				loggedInStaff = acc;
				buildLandingPage();
			}
		});

		revalidate();
		repaint();
	}

	/**
	 * Builds the staff landing page, containing all of the buttons they will need to access their pages
	 */
	private void buildLandingPage() {
		removeAll();
		CONTENT_PANE.removeAll();
		Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_LIGHT, 24);
		Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 20);

		JLabel title = new JLabel("Staff Member Logged In: " + loggedInStaff.getUsername());
		title.setFont(titleFont);
		JButton customers = new JButton("View customer information");
		customers.setFont(buttonFont);
		JButton orders = new JButton("Order Management");
		orders.setFont(buttonFont);
		JButton products = new JButton("Inventory Management");
		products.setFont(buttonFont);
		JButton logout = new JButton("Log Out");
		logout.setFont(buttonFont);

		customers.addActionListener(e -> buildCustomerPage());

		orders.addActionListener(e -> buildOrdersPage());

		products.addActionListener(e -> buildProductsPage("Frame Set"));

		logout.addActionListener(e -> {
			loggedInStaff = null;
			buildLoginPage();
		});

        Dimension buttonSize = new Dimension(Fonts.getStringWidth(buttonFont, "Review, search and update orders") * 3/2,
												Fonts.getHeight(buttonFont) * 3/2);

        customers.setPreferredSize(buttonSize);
        customers.setMaximumSize(buttonSize);
        orders.setPreferredSize(buttonSize);
        orders.setMaximumSize(buttonSize);
        products.setPreferredSize(buttonSize);
        products.setMaximumSize(buttonSize);
		logout.setPreferredSize(buttonSize);
        logout.setMaximumSize(buttonSize);

		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		customers.setAlignmentX(Component.CENTER_ALIGNMENT);
		orders.setAlignmentX(Component.CENTER_ALIGNMENT);
		products.setAlignmentX(Component.CENTER_ALIGNMENT);
		logout.setAlignmentX(Component.CENTER_ALIGNMENT);

		add(Box.createHorizontalStrut(20));
		CONTENT_PANE.add(Box.createVerticalStrut(20));
		CONTENT_PANE.add(title);
		CONTENT_PANE.add(Box.createVerticalStrut(30));
		CONTENT_PANE.add(customers);
		CONTENT_PANE.add(Box.createVerticalStrut(20));
		CONTENT_PANE.add(orders);
		CONTENT_PANE.add(Box.createVerticalStrut(20));
		CONTENT_PANE.add(products);
		CONTENT_PANE.add(Box.createVerticalStrut(20));
		CONTENT_PANE.add(logout);

		contentPaneFooter(false);
	}

	/**
	 * Builds the customer view page - shows CustomerInfo objects in a table
	 */
	private void buildCustomerPage() {
		removeAll();
		CONTENT_PANE.removeAll();

		Font titleFont = Fonts.getSizedFont(Fonts.OPENSANS_BOLD, 24);

		JLabel title = new JLabel("Customer information");
		title.setFont(titleFont);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		DefaultTableModel model = new DefaultTableModel();
		JTable table = new JTable(model);
		model.addColumn("ID");
		model.addColumn("Name");
		model.addColumn("Address");

		allCustomers = CustomerInfo.getAll().toArray(new CustomerInfo[0]);

		for (CustomerInfo ci : allCustomers) {
			String[] rowData = {String.valueOf(ci.getCustomer().getCustomerID()), ci.getCustomer().getName(), ci.getAddress().toString()};
			model.addRow(rowData);
		}

		table.setFont(Fonts.getSizedFont(Fonts.OPENSANS_LIGHT, 16));
		table.setDefaultEditor(Object.class, null);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        sorter.setModel(table.getModel());

		Comparator<String> numComparator = (o1, o2) -> {
            int int1 = Integer.parseInt(o1);
            int int2 = Integer.parseInt(o2);
            return int1 - int2;
        };

        sorter.setComparator(0, numComparator);

		table.setRowSorter(sorter);
		table.getRowSorter().toggleSortOrder(0);

		// Resize columns to fit data
		for (int col=0; col<table.getColumnCount(); col++) {
            TableColumn col1 = table.getColumnModel().getColumn(col);

            int width = col1.getMinWidth() + 50;
            int max = col1.getMaxWidth();

            for (int row1=0; row1<table.getRowCount(); row1++) {
                TableCellRenderer tcr = table.getCellRenderer(row1, col);
                int newWidth = table.prepareRenderer(tcr, row1, col).getPreferredSize().width + table.getIntercellSpacing().width;
                width = Math.max(width, newWidth);

                if (width >= max) {
                    width = max;
                    break;
                }
            }

            col1.setPreferredWidth(width);
        }

		JScrollPane scroller = new JScrollPane(table);
        scroller.setViewportBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setMinimumSize(new Dimension(PAGE_SIZE.width - 30, table.getRowHeight() * 22));
		scroller.setPreferredSize(new Dimension(PAGE_SIZE.width - 30, table.getRowHeight() * 22));
		scroller.setMaximumSize(new Dimension(PAGE_SIZE.width - 30, table.getRowHeight() * 22));

        CONTENT_PANE.add(title);
		CONTENT_PANE.add(Box.createVerticalStrut(10));
		CONTENT_PANE.add(scroller);

		contentPaneFooter(true);

	}

	/**
	 * Builds the orders page - shows all orders, and allows a staff member to filter them
	 */
	protected void buildOrdersPage() {
		removeAll();
		CONTENT_PANE.removeAll();

		allOrders = OrderInfo.getAll().toArray(new OrderInfo[0]);
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
		filterPanel.setBackground(Color.WHITE);

		Font filterFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 16);
		
		JLabel label1 = new JLabel("Filter by status: ");
		JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Show all orders", "Pending", "Confirmed", "Fulfilled"});
		label1.setFont(filterFont);
		statusFilter.setFont(filterFont);

		JLabel label2 = new JLabel("Filter by order number: ");
		JTextField orderNumFilter = new JTextField(32);
		label2.setFont(filterFont);
		orderNumFilter.setFont(filterFont);

		JButton search = new JButton("Search order");
		search.setFont(filterFont);
		
		JButton clear = new JButton("Clear filter");
		clear.setFont(filterFont);

		filterPanel.add(label1);
		filterPanel.add(statusFilter);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(label2);
		filterPanel.add(orderNumFilter);
		filterPanel.add(Box.createHorizontalStrut(5));
		filterPanel.add(search);
		filterPanel.add(Box.createHorizontalStrut(10));
		filterPanel.add(clear);

		JPanel noneFoundPanel = new JPanel(new GridBagLayout());
		noneFoundPanel.setPreferredSize(new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height)));
		noneFoundPanel.setMaximumSize(new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height)));

		JLabel noneFoundLabel = new JLabel("No orders found!");
		noneFoundLabel.setFont(Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 32));

		noneFoundPanel.add(noneFoundLabel);

		JPanel orderDisplayPane = new JPanel();
		orderDisplayPane.setBackground(Color.WHITE);
		orderDisplayPane.add((new OrderDisplay(allOrders, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height))).getScroller()));

		CONTENT_PANE.add(filterPanel);
		CONTENT_PANE.add(Box.createVerticalStrut(10));
		CONTENT_PANE.add(orderDisplayPane);

		statusFilter.addActionListener(e -> {
			orderDisplayPane.removeAll();
			String selected = (String)statusFilter.getSelectedItem();

			if ("Show all orders".equals(selected)) {
				orderDisplayPane.add((new OrderDisplay(allOrders, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height))).getScroller()));
			} else {
				String status = selected.toUpperCase();
				OrderInfo[] ordersWithStatus = Arrays.stream(allOrders).filter(oi -> oi.getOrder().getStatus().equals(status)).toArray(OrderInfo[]::new);
				if (ordersWithStatus.length > 0) {
					orderDisplayPane.add((new OrderDisplay(ordersWithStatus, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height))).getScroller()));
				} else {
					orderDisplayPane.add(noneFoundPanel);
				}
			}

			orderDisplayPane.revalidate();
			orderDisplayPane.repaint();
		});

		search.addActionListener(e -> {
			if (orderNumFilter.getText().isEmpty()) {
				return;
			}
			orderDisplayPane.removeAll();

			int orderNum = Integer.parseInt(orderNumFilter.getText());
			OrderInfo[] withNum = Arrays.stream(allOrders).filter(oi -> oi.getOrder().getOrderNumber() == orderNum).toArray(OrderInfo[]::new);
			if (withNum.length > 0) {
				orderDisplayPane.add((new OrderDisplay(withNum, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height)))).getScroller());
			} else {
				orderDisplayPane.add(noneFoundLabel);
			}

			orderDisplayPane.revalidate();
			orderDisplayPane.repaint();
		});

		clear.addActionListener(e -> {
			statusFilter.setSelectedIndex(0);
			orderNumFilter.setText("");

			orderDisplayPane.removeAll();

			orderDisplayPane.add((new OrderDisplay(allOrders, this, new Dimension(PAGE_SIZE.width - 35, PAGE_SIZE.height - (100 + returnButtonSize.height)))).getScroller());

			orderDisplayPane.revalidate();
			orderDisplayPane.repaint();
		});

		contentPaneFooter(true);
	}

	/**
	 * Builds a product page, showing all products of a certain type
	 * @param currentlySelected the type of product to show
	 */
	private void buildProductsPage(String currentlySelected) {
		removeAll();
		CONTENT_PANE.removeAll();

		((MainFrame) SwingUtilities.getWindowAncestor(this)).updateCatalogue();

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.setBackground(Color.WHITE);

		Font topFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 16);
		
		JLabel label = new JLabel("Filter product type: ");
		JComboBox typeFilter = new JComboBox(new String[]{"Frame Set", "Handlebar", "Wheel"});
		label.setFont(topFont);
		typeFilter.setFont(topFont);

		JButton add = new JButton("Add product");
		add.setFont(topFont);

		JButton edit = new JButton("Edit quantity");
		edit.setFont(topFont);

		JButton delete = new JButton("Delete product");
		delete.setFont(topFont);

		topPanel.add(label);
		topPanel.add(typeFilter);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(add);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(edit);
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(delete);

		DefaultTableModel model = new DefaultTableModel();
		JTable table = new JTable(model);
		model.addColumn("Brand");
		model.addColumn("Serial");
		model.addColumn("Product name");
		model.addColumn("Quantity");

		// Add column to model, remove from view
		model.addColumn("brandID");
		table.removeColumn(table.getColumnModel().getColumn(4));

		int selectIndex = currentlySelected.equals("Frame Set") ? 0 : (currentlySelected.equals("Handlebar") ? 1 : 2);
		for (BikeComponent c : MainFrame.ALL_COMPS[selectIndex]) {
			String[] rowData = {c.getBrandName(), String.valueOf(c.getSerial()), c.getName(), String.valueOf(c.getQuantity()), String.valueOf(c.getBrandID())};
			model.addRow(rowData);
		}

		table.setFont(Fonts.getSizedFont(Fonts.OPENSANS_LIGHT, 16));
		table.setDefaultEditor(Object.class, null);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        sorter.setModel(table.getModel());

		// Custom Comparator used to sort numbers as numbers, rather than as strings
		Comparator<String> numComparator = (o1, o2) -> {
            int int1 = Integer.parseInt(o1);
            int int2 = Integer.parseInt(o2);
            return int1 - int2;
        };

        sorter.setComparator(1, numComparator);
        sorter.setComparator(3, numComparator);

		table.setRowSorter(sorter);

		// Resize columns to fit data
		for (int col=0; col<table.getColumnCount(); col++) {
            TableColumn col1 = table.getColumnModel().getColumn(col);

            int width = col1.getMinWidth() + 50;
            int max = col1.getMaxWidth();

            for (int row1=0; row1<table.getRowCount(); row1++) {
                TableCellRenderer tcr = table.getCellRenderer(row1, col);
                int newWidth = table.prepareRenderer(tcr, row1, col).getPreferredSize().width + table.getIntercellSpacing().width;
                width = Math.max(width, newWidth);

                if (width >= max) {
                    width = max;
                    break;
                }
            }

            col1.setPreferredWidth(width);
        }

		typeFilter.addActionListener(e -> {
			model.setNumRows(0);
			
			String selected = (String)typeFilter.getSelectedItem();
			switch (selected) {
				case "Frame Set":
				    for (BikeComponent c : MainFrame.ALL_COMPS[0]) {
						String[] rowData = {c.getBrandName(), String.valueOf(c.getSerial()), c.getName(), String.valueOf(c.getQuantity()), String.valueOf(c.getBrandID())};
						model.addRow(rowData);
					}
					break;
				case "Handlebar":
				    for (BikeComponent c : MainFrame.ALL_COMPS[1]) {
						String[] rowData = {c.getBrandName(), String.valueOf(c.getSerial()), c.getName(), String.valueOf(c.getQuantity()), String.valueOf(c.getBrandID())};
						model.addRow(rowData);
					}
					break;
				case "Wheel":
				    for (BikeComponent c : MainFrame.ALL_COMPS[2]) {
						String[] rowData = {c.getBrandName(), String.valueOf(c.getSerial()), c.getName(), String.valueOf(c.getQuantity()), String.valueOf(c.getBrandID())};
						model.addRow(rowData);
					}
					break;
			}
		});

		edit.addActionListener(e -> {
			if (table.getSelectedRow() == -1) {
				OptionPanes.showErrorPane("Please select a product first!", OptionPanes.WARNING);
			} else {
				int selectedRow = table.convertRowIndexToModel(table.getSelectedRow()); // Protect against sorting
				// Need the model rather than the view to get hidden values
				int brand = Integer.parseInt((String) table.getModel().getValueAt(selectedRow, 4));
				String bName = (String) table.getModel().getValueAt(selectedRow, 0);
			    int serial = Integer.parseInt((String) table.getModel().getValueAt(selectedRow, 1));
				String name = (String) table.getModel().getValueAt(selectedRow, 2);
				String input = JOptionPane.showInputDialog("Enter new quantity for " + bName + " " + name + ": ");
				try {
					int qty = Integer.parseInt(input);
					if (qty >= 0 && BikeComponent.updateQuantity(brand, serial, qty)) {
						OptionPanes.showSuccessPane("Quantity update successful!");
						buildProductsPage((String) typeFilter.getSelectedItem());
					} else {
						OptionPanes.showErrorPane("Failed to update quantity, please double-check the entered value", OptionPanes.RECOVERABLE);
					}
				} catch (Exception ex) {
					if (input != null) {
						OptionPanes.showErrorPane("Please enter a valid number!", OptionPanes.WARNING);
					}
				}
				table.clearSelection();
			}
		});

		delete.addActionListener(e -> {
			if (table.getSelectedRow() == -1) {
				OptionPanes.showErrorPane("Please select a product first.", OptionPanes.WARNING);
			} else {
				int selectedRow = table.convertRowIndexToModel(table.getSelectedRow()); // Protect against sorting
				int brand = Integer.parseInt((String) table.getModel().getValueAt(selectedRow, 4));
				String bName = (String) table.getModel().getValueAt(selectedRow, 0);
			    int serial = Integer.parseInt((String) table.getModel().getValueAt(selectedRow, 1));
				String name = (String) table.getModel().getValueAt(selectedRow, 2);
				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + bName + " " + name + "?",
                    "Delete product?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
					if (BikeComponent.deleteBikeComponent(brand, serial)) {
						OptionPanes.showSuccessPane("Successfully deleted product!");
						buildProductsPage((String) typeFilter.getSelectedItem());
					} else {
						OptionPanes.showErrorPane("Failed to delete product!", OptionPanes.RECOVERABLE);
					}
                }
				table.clearSelection();
			}
		});

		add.addActionListener(e -> buildAddProductPage((String) typeFilter.getSelectedItem()));

		JScrollPane scroller = new JScrollPane(table);
        scroller.setViewportBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setMinimumSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (85 + returnButtonSize.height)));
		scroller.setPreferredSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (85 + returnButtonSize.height)));
		scroller.setMaximumSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (85 + returnButtonSize.height)));

		CONTENT_PANE.add(topPanel);
		CONTENT_PANE.add(Box.createVerticalStrut(10));
		CONTENT_PANE.add(scroller);

		contentPaneFooter(true);
	}

	/**
	 * Builds the page that a staff member will use to add products to the database
	 * @param type the type of product to add
	 */
	private void buildAddProductPage(String type) {
		removeAll();
		CONTENT_PANE.removeAll();

		JPanel formHolder = new JPanel();
		formHolder.setLayout(new BoxLayout(formHolder, BoxLayout.PAGE_AXIS));
		formHolder.setBackground(Color.WHITE);
		formHolder.setBorder(new EmptyBorder(15, 15, 15, 15));

		Font labelFont = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);

		JLabel detailsTitle = new JLabel("Input new " + type.toLowerCase() + " details:");
		JButton detailsSubmit = new JButton("Submit Component");
		JButton returnButton = new JButton("Return to component view");

		detailsTitle.setFont(Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24));
		detailsSubmit.setFont(labelFont);
		returnButton.setFont(labelFont);

		detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		returnButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		formHolder.add(detailsTitle);
		formHolder.add(Box.createVerticalStrut(10));

		JComboBox<Brand> brandBox = new JComboBox<>(Brand.getAll().toArray(new Brand[0]));

		String[] fields;
		int[] validation;
		JComponent[] overrides;

		switch (type) {
			// Set DetailsForm initialisation variables differently depending on which type of product is being added
			case "Frame Set":
				fields = new String[] {"Brand", "New Brand (if not in list)", "Serial #", "Product Name", "Price (in GBP)", "Available Quantity", "Shocks", "Size (cm)", "Gears"};
				validation = new int[] {DetailsForm.NO_VALIDATION, DetailsForm.CAN_BE_EMPTY, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.IS_MONEY, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.IS_NUMBER, DetailsForm.IS_NUMBER};
				overrides = new JComponent[]{brandBox, null, null, null, null, null, new JCheckBox(), null, null};
				break;
			case "Handlebar":
				fields = new String[] {"Brand", "New Brand (if not in list)", "Serial #", "Product Name", "Price (in GBP)", "Available Quantity", "Style"};
				validation = new int[] {DetailsForm.NO_VALIDATION, DetailsForm.CAN_BE_EMPTY, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.IS_MONEY, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION};
				overrides = new JComponent[]{brandBox, null, null, null, null, null, new JComboBox<>(new String[] {"Straight", "High", "Dropped"})};
				break;
			default:
				fields = new String[] {"Brand", "New Brand (if not in list)", "Serial #", "Product Name", "Price (in GBP)", "Available Quantity", "Diameter (cm)", "Style", "Brakes"};
				validation = new int[] {DetailsForm.NO_VALIDATION, DetailsForm.CAN_BE_EMPTY, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.IS_MONEY, DetailsForm.IS_NUMBER, DetailsForm.IS_NUMBER, DetailsForm.NO_VALIDATION, DetailsForm.NO_VALIDATION};
				overrides = new JComponent[]{brandBox, null, null, null, null, null, null, new JComboBox<>(new String[]{"Road", "Mountain", "Hybrid"}), new JComboBox<>(new String[]{"Rim", "Disk"})};
				break;
		}

		DetailsForm df = new DetailsForm(fields, validation, PAGE_SIZE.width - 75, overrides);

		formHolder.add(df);
		formHolder.add(Box.createVerticalStrut(15));
		formHolder.add(detailsSubmit);

		JScrollPane scroller = new JScrollPane(formHolder);
		scroller.setBackground(Color.WHITE);
        scroller.setViewportBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setMinimumSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (100 + returnButtonSize.height)));
		scroller.setPreferredSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (100 + returnButtonSize.height)));
		scroller.setMaximumSize(new Dimension(PAGE_SIZE.width - 30, PAGE_SIZE.height - (100 + returnButtonSize.height)));

        CONTENT_PANE.add(scroller);

		CONTENT_PANE.add(Box.createVerticalStrut(10));

		JPanel returnHolder = new JPanel(new GridBagLayout());
		returnHolder.setBackground(Color.WHITE);
		returnHolder.add(returnButton);
		CONTENT_PANE.add(returnHolder);

		returnButton.addActionListener(e -> buildProductsPage(type));
		detailsSubmit.addActionListener(e -> {
			if (df.validInputs()) {
				boolean success = addProduct(type, df);
				if (success) {
					OptionPanes.showSuccessPane("Successfully added " + type + " to database!");
					buildProductsPage(type);
				} else {
					OptionPanes.showErrorPane("Failed to add " + type + ".\nPlease try again.\nMake sure you have entered brand/serial correctly, and that they are a unique combination.", OptionPanes.RECOVERABLE);
				}
			}
		});

		contentPaneFooter(true);
	}

	/**
	 * Gets component data from the DetailsForm, and attempts to add to database
	 * @param type the type of component to add
	 * @param df the DetailsForm storing all of the data
	 * @return
	 */
	private boolean addProduct(String type, DetailsForm df) {
		Brand bInput = ((Brand) ((JComboBox)df.getOverridenComp("Brand")).getSelectedItem());
		String newBrand = df.getInput("New Brand (if not in list)");
		int serial = Integer.parseInt(df.getInput("Serial #"));
		String name = df.getInput("Product Name");
		int price = Integer.parseInt(Sanitisation.sanitisePrice(df.getInput("Price (in GBP)")));
		int qty = Integer.parseInt(df.getInput("Available Quantity"));

		String style = "ROAD";

		if (type.equals("Wheel") || type.equals("Handlebar")) {
			style = ((String) ((JComboBox) df.getOverridenComp("Style")).getSelectedItem()).toUpperCase();
		}

		if (!newBrand.isEmpty()) {
			bInput = Brand.addBrand(newBrand);
			if (bInput == null) {
				return false;
			}
		}

		int brandID = bInput.getID();

		boolean success;

		switch (type) {
			case "Frame Set":
				int shocks = ((JCheckBox)df.getOverridenComp("Shocks")).isSelected() ? 1 : 0;
				int size = Integer.parseInt(df.getInput("Size (cm)"));
				int gears = Integer.parseInt(df.getInput("Gears"));
				success = FrameSet.addFrameSet(brandID, serial, price, name, qty, shocks, size, gears);
				break;
			case "Handlebar":
				success = Handlebar.addHandlebar(brandID, serial, price, name, qty, style);
				break;
			default:
				int diameter = Integer.parseInt(df.getInput("Diameter (cm)"));
				String brakes = ((String) ((JComboBox)df.getOverridenComp("Brakes")).getSelectedItem()).toUpperCase();
				success = Wheel.addWheel(brandID, serial, price, name, qty, diameter, style, brakes);
				break;
		}

		return success;
	}

	/**
	 * @return the username of the currently logged in staff member
	 */
	protected String getStaffName() {
		return loggedInStaff.getUsername();
	}
}
