package team22.UI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import com.formdev.flatlaf.*;
import team22.dataAccessLayer.BikeComponent;

public class MainFrame extends JFrame {
	static int WIDTH = 1600;
	static int HEIGHT = 900;

	// Used in DBDriver - if a database connection error occurs before the GUI has been initialised, it will
	// terminate the program
	public static boolean GUI_INITIALISED = false;

	static String[] cards = new String[]{"Bike Builder", "Track an Order", "Staff Access", "Edit Customer"};

	public static BikeComponent[] allFrameSets;
	public static BikeComponent[] allHandlebars;
	public static BikeComponent[] allWheels;

	public static BikeComponent[][] ALL_COMPS = new BikeComponent[3][];

	// Ensure that components are updated before BikeBuilder tries to access them
	static {
		FlatLightLaf.setup();  // Setup flatlightlaf to ensure that any error thrown in updateComponents will look pretty
		updateComponents();
	}

	JButton saveOrderButton;
	Component saveOrderStandin;

	BikeBuilder bikeBuilder;

	/**
	 * @param title the title of the Frame
	 */
	public MainFrame(String title) {
		super(title);
		setSize(WIDTH, HEIGHT);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

		int cardWidth = WIDTH * 80 / 100;
		int cardHeight = HEIGHT - 150;

		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(new CardLayout());
		cardPanel.setMaximumSize(new Dimension(cardWidth, cardHeight));

		cardPanel.add(bikeBuilder = new BikeBuilder(cardWidth, cardHeight), "Bike Builder");
		cardPanel.add(new TrackOrder(cardWidth, cardHeight), "Track an Order");
		cardPanel.add(new StaffAccess(cardWidth, cardHeight), "Staff Access");
		cardPanel.add(new CustomerDetail(cardWidth, cardHeight), "Edit Customer");

		((CardLayout) cardPanel.getLayout()).show(cardPanel, "Bike Builder");  // Set bike builder as initial state
		saveOrderButton = buildSaveOrderButton();
		saveOrderStandin = Box.createVerticalStrut(50);

		saveOrderButton.setAlignmentX(Component.LEFT_ALIGNMENT);

		add(cardPanel);
		add(Box.createHorizontalGlue());
		add(switchCardButtons(cardPanel, (CardLayout) cardPanel.getLayout()));
		add(Box.createHorizontalStrut(25));

		setVisible(true);
	} 

	/**
	 * @return the saveOrder button
	 */
	private JButton buildSaveOrderButton() {
		Font saveOrderFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 16);

		JButton saveOrder = createIconedButton(Images.SAVE, "Save Order", saveOrderFont);
		saveOrder.setPreferredSize(new Dimension(WIDTH / 10, 50));
		saveOrder.setMaximumSize(new Dimension(WIDTH / 10, 50));

		saveOrder.addActionListener(e -> bikeBuilder.saveOrder());

		return saveOrder;
	}

	/**
	 * Shows the save order button, and hides the stand-in empty space
	 */
	protected void showSaveOrderButton() {
		saveOrderButton.setVisible(true);
		saveOrderStandin.setVisible(false);
	}

	/**
	 * Hides the save order button, and shows the stand-in empty space
	 */
	protected void hideSaveOrderButton() {
		saveOrderButton.setVisible(false);
		saveOrderStandin.setVisible(true);
	}

	/**
	 * Creates the buttons used for changing visible card in the frame.
	 * @param cardShow the panel with a CardLayout that displays the cards.
	 * @param layout the actual CardLayout that controls which card to show.
	 * @return the panel containing the buttons.
	 */
	private JPanel switchCardButtons(JPanel cardShow, CardLayout layout) {
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
		buttons.add(Box.createVerticalStrut(50));
		buttons.add(saveOrderButton);
		saveOrderButton.setVisible(false);
		saveOrderStandin.setVisible(true);
		buttons.add(saveOrderStandin);

		JPanel cardButtonPanel = new JPanel(new GridBagLayout());
		cardButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel buttonHolder = new JPanel();
		buttonHolder.setLayout(new BoxLayout(buttonHolder, BoxLayout.PAGE_AXIS));

		Font buttonFont = Fonts.getSizedFont(Fonts.OPENSANS_SEMIBOLD, 24);

		String[] buttonIcons = new String[] {Images.BICYCLE, Images.GEO_ALT, Images.PERSON_LOCK, Images.EDIT_CUSTOMER};

		for (int i=0; i<cards.length; i++) {
			String card = cards[i];

			JButton button;
			// Try to create a button for each image - if more buttons than images, just create buttons with text
			if (i < buttonIcons.length) {
				button = createIconedButton(buttonIcons[i], card, buttonFont);
			} else {
				button = new JButton(card);
				button.setFont(buttonFont);
			}

			button.setPreferredSize(new Dimension(WIDTH / 5, 50));
			button.setMaximumSize(new Dimension(WIDTH / 5, 50));

			button.addActionListener(e -> {
				// Hide saveOrder button when not on build a bike screen
				saveOrderButton.setVisible(card.equals(cards[0]) && bikeBuilder.isOrderFull() && !bikeBuilder.saving);

				// Add spacing to keep other buttons from shifting up when saveOrder button is invisible
				saveOrderStandin.setVisible(!card.equals(cards[0]) || !bikeBuilder.isOrderFull() || bikeBuilder.saving);
				layout.show(cardShow, card);
			});
			buttonHolder.add(button);
			if (i<cards.length-1) {
				buttonHolder.add(Box.createVerticalStrut(15));
			}
		}
		cardButtonPanel.add(buttonHolder);
		buttons.add(cardButtonPanel);
		buttons.add(Box.createVerticalStrut(100));
		return buttons;
	}

	/**
	 * Tries to create a button with an icon, and if that icon fails to load for whatever reason, just creates button
	 * with text
	 * @param imageSrc the source folder from which to load the icon (defined in Images.java)
	 * @param text the text to show next to the image
	 * @param buttonFont the Font from which to derive the height of the icon
	 * @return the button with or without an icon
	 */
	private static JButton createIconedButton(String imageSrc, String text, Font buttonFont) {
		JButton button;
		try {
			int newImgDim = Fonts.getHeight(buttonFont) * 5 / 4;
			Image saveOrderIconImg = Images.loadSizedImage(imageSrc, newImgDim, newImgDim);
			ImageIcon saveOrderIcon = new ImageIcon(saveOrderIconImg);
			button = new JButton(saveOrderIcon);
			button.setText(text);
		} catch (IOException | NullPointerException | IllegalArgumentException e) {
			button = new JButton(text);
		}

		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setFont(buttonFont);
		return button;
	}

	/**
	 * Thread used to update the bike builder catalogue without slowing down the other operations
	 */
	class UpdateThread extends Thread {
		public void run() {
			bikeBuilder.updateCatalogue();
		}
	}

	/**
	 * Updates the components list, and also the bike builder tables
	 */
	public void updateCatalogue() {
		updateComponents();

		UpdateThread update = new UpdateThread();
		update.start();
	}

	/**
	 * Updates the component list to ensure that it is current with the database
	 */
	private static void updateComponents() {
		ArrayList<BikeComponent> fsList = new ArrayList<>();
		ArrayList<BikeComponent> hList = new ArrayList<>();
		ArrayList<BikeComponent> wList = new ArrayList<>();

		for (BikeComponent bc : BikeComponent.getAll()) {
			if (bc.getDisplayName().contains("Frame Set")) {
				fsList.add(bc);
			} else if (bc.getDisplayName().contains("Handlebar")) {
				hList.add(bc);
			} else if (bc.getDisplayName().contains("Wheel")) {
				wList.add(bc);
			}
		}

		allFrameSets = fsList.toArray(new BikeComponent[0]);
		allHandlebars = hList.toArray(new BikeComponent[0]);
		allWheels = wList.toArray(new BikeComponent[0]);

		ALL_COMPS[0] = allFrameSets;
		ALL_COMPS[1] = allHandlebars;
		ALL_COMPS[2] = allWheels;
	}

	/**
	 * Main - runs the whole program
	 */
	public static void main(String[] args) {
		UIDefaults uid = UIManager.getDefaults();

		// Add alternate row colour if not already present in look and feel
		uid.putIfAbsent("Table.alternateRowColor", Color.LIGHT_GRAY);

		new MainFrame("Build-A-Bike Sales System");
		GUI_INITIALISED = true;
	}

}
