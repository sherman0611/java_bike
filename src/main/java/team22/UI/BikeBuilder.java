package team22.UI;

import team22.dataAccessLayer.BikeComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;

public class BikeBuilder extends JPanel {

    private static final String[] COMPS = new String[]{"Frame Set", "Handlebars", "Wheels"};

    private final JScrollPane[] TABLES = new JScrollPane[COMPS.length];

    private final JLabel[] COMP_LABELS = new JLabel[]{new JLabel(), new JLabel(), new JLabel()};

    private final JLabel ASSEMBLY_LABEL;
    private final JLabel TOTAL_LABEL;

    private final BikeComponent[] CURRENT_ORDER = new BikeComponent[3];

    private final int DETAILS_WIDTH;
    private final int SELECTOR_WIDTH;

    private JPanel selectionPane;

    private int width;
    private int height;

    public boolean saving = false;
    /**
     * Constructs a card in which a shopper can design their bike
     * @param w the width of the card
     * @param h the height of the card
     */
    public BikeBuilder(int w, int h) {
        width = w;
        height = h;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setMaximumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));

        // Account for padding either side
        width -= 50;

        DETAILS_WIDTH = width * 48 / 100;
        SELECTOR_WIDTH = width * 50 / 100;

        for (JLabel jl : COMP_LABELS) {
            jl.setFont(Fonts.getSizedFont(Fonts.FAKERECEIPT_REGULAR, 16));
        }

        ASSEMBLY_LABEL = new JLabel();
        ASSEMBLY_LABEL.setFont(Fonts.getSizedFont(Fonts.FAKERECEIPT_REGULAR, 16));
        
        TOTAL_LABEL = new JLabel();
        TOTAL_LABEL.setFont(Fonts.getSizedFont(Fonts.FAKERECEIPT_REGULAR, 16));

        JScrollPane textScroller = buildDetailsPanel(new Dimension(DETAILS_WIDTH, height));

        for (int i=0; i<TABLES.length; i++) {
            TABLES[i] = buildSelectorTable(MainFrame.ALL_COMPS[i], i);
        }

        selectionPane = new JPanel();
        buildSelectionPanel();

        add(Box.createHorizontalStrut(25));
        add(selectionPane);
        add(Box.createHorizontalStrut(width - SELECTOR_WIDTH - DETAILS_WIDTH));
        add(textScroller);
        add(Box.createHorizontalStrut(25));
    }

    /**
     * Returns formatted text to be rendered in a JLabel
     * @param name the name of the component
     * @param cost the cost of the component
     * @return a formatted string containing type, name, and cost
     */
    private String getDescriptorText(String name, int cost) {
        // Receipt font is monowidth so can use any arbitrary character here
        int charWidth = Fonts.getStringWidth(Fonts.getSizedFont(Fonts.FAKERECEIPT_REGULAR, 16), "A");
        int numChars = (DETAILS_WIDTH) / charWidth;

        String costString = String.format("%.2f", ((float) cost) / 100);
        int rightChars = numChars - 4 - name.length();  // Subtract 4 to ensure no text overflows

        // Format to be in the form of a receipt, with name left-aligned, and cost right-aligned
        return name + String.format("%"+rightChars+"s", "£" + costString);
    }

    /**
     * Used by MainFrame to decide when to show the saveOrder button, used in this class to decide whether to add
     * assembly fee
     * @return true if order is full, otherwise false
     */
    protected boolean isOrderFull() {
        return Arrays.stream(CURRENT_ORDER).noneMatch(Objects::isNull);
    }

    /**
     * Builds a JScrollPane conatining a table from which you can view details of/select components of a certain type
     * @param type the Array of components from which to build a table
     * @return the JScrollPane
     */
    private JScrollPane buildSelectorTable(BikeComponent[] type, int currentCompType) {
        Font radioFont = Fonts.getSizedFont(Fonts.OPENSANS_LIGHT, 16);

        String[] headers = type[0].getFieldTitles();

        // Create table with field values from the array of components
        DefaultTableModel model = new DefaultTableModel(Arrays.stream(type).map(BikeComponent::getFieldValues).toArray(String[][]::new), headers);
        JTable t = new JTable(model);

        t.setFont(radioFont);
        t.setDefaultEditor(Object.class, null);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setResizingAllowed(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Find indices of any columns that should be sorted as numbers
        int[] numCols = IntStream.range(0, headers.length).filter(i -> headers[i].matches("Diameter|Size|Gears|Price|Qty.")).toArray();

        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        sorter.setModel(t.getModel());

        Comparator<String> numComparator = (o1, o2) -> {
            int int1 = Integer.parseInt(o1.replace("£", "").replace(".", ""));
            int int2 = Integer.parseInt(o2.replace("£", "").replace(".", ""));
            return int1 - int2; // Just need to return a number, so can just subtract int2 from int1
        };

        for (int i : numCols) {
            sorter.setComparator(i, numComparator);
        }

        t.setRowSorter(sorter);


        // Resize columns to fit data
        for (int col=0; col<t.getColumnCount(); col++) {
            TableColumn column = t.getColumnModel().getColumn(col);

            int width = column.getMinWidth() + 50;
            int max = column.getMaxWidth();

            for (int row=0; row<t.getRowCount(); row++) {
                TableCellRenderer tcr = t.getCellRenderer(row, col);
                int newWidth = t.prepareRenderer(tcr, row, col).getPreferredSize().width + t.getIntercellSpacing().width;
                width = Math.max(width, newWidth);

                if (width >= max) {
                    width = max;
                    break;
                }
            }

            column.setPreferredWidth(width);
        }

        t.getSelectionModel().addListSelectionListener(e -> {
            // Gets called with selected row -1 when sorting columns, so just return to prevent an error
            if (t.getSelectedRow() < 0 || t.getSelectedRow() >= t.getRowCount()) {
                return;
            }

            // Convert row index to model to ensure consistent selection, no matter how the data is sorted
            BikeComponent comp = MainFrame.ALL_COMPS[currentCompType][t.getRowSorter().convertRowIndexToModel(t.getSelectedRow())];

            if ((comp.getDisplayName().contains("Wheel") && comp.getQuantity() < 2) || comp.getQuantity() < 1) {
                OptionPanes.showErrorPane("There currently isn't enough of " + comp.getDisplayName() + " in stock to process your request." +
                                            "\nPlease wait for stock to be replenished.", OptionPanes.WARNING);
                return;
            }

            String compName = comp.getDisplayName();
            int compPrice = comp.getPrice();
            if (currentCompType == 2) {
                // Shopper will always need 2 wheels, so append a quantity indicator and double the line cost
                compName += " (2)";
                compPrice *= 2;
            }
            COMP_LABELS[currentCompType].setText(getDescriptorText(compName, compPrice));
            CURRENT_ORDER[currentCompType] = comp;
            updateTotalCostLabel();
        });

        return new JScrollPane(t);
    }

    /**
     * Calculates and updates the label for the total cost of the bike
     */
    private void updateTotalCostLabel() {
        int total = 0;
        for (int i=0; i<CURRENT_ORDER.length; i++) {
            BikeComponent bc = CURRENT_ORDER[i];
            total += bc == null ? 0 : (i == 2 ? bc.getPrice() * 2 : bc.getPrice());
        }

        // Iff nothing in CURRENT_ORDER is null, add assembly cost to total
        if (isOrderFull()) {
            total += 1000;
            ASSEMBLY_LABEL.setText(getDescriptorText("Additional assembly cost", 1000));
            ((MainFrame) SwingUtilities.getWindowAncestor(this)).showSaveOrderButton();
        }

        TOTAL_LABEL.setText(getDescriptorText("Total", total));
    }

    /**
     * Builds a JPanel containing the details of the currently built bike.
     * @param scrollerDim the size of the scrollPane
     * @return the JScrollPane containing the details (using JScrollPane in case text is too large)
     */
    private JScrollPane buildDetailsPanel(Dimension scrollerDim) {
        Font receiptFont = Fonts.getSizedFont(Fonts.FAKERECEIPT_REGULAR, 32);
        JPanel detailsPane = new JPanel();

        detailsPane.setBackground(Color.WHITE);
        detailsPane.setLayout(new BoxLayout(detailsPane, BoxLayout.PAGE_AXIS));
        detailsPane.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel label = new JLabel("Your Bike");
        label.setFont(receiptFont);

        int numDashes = (DETAILS_WIDTH - 30) / Fonts.getStringWidth(receiptFont, "-");

        // Get string of just - by creating an empty char array, converting it to string, and replacing null char with -
        JLabel dashLabel = new JLabel(new String(new char[numDashes-1]).replace('\0', '-'));
        dashLabel.setFont(receiptFont);

        // Create new JPanel to allow the title text to be centered while everything else is left-aligned
        JPanel titlePanel = new JPanel();
        titlePanel.setMaximumSize(new Dimension(DETAILS_WIDTH, Fonts.getHeight(receiptFont)));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(label);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsPane.add(Box.createVerticalStrut(25));
        detailsPane.add(titlePanel);
        detailsPane.add(dashLabel);
        detailsPane.add(Box.createVerticalStrut(15));
        detailsPane.add(COMP_LABELS[0]);
        detailsPane.add(Box.createVerticalStrut(15));
        detailsPane.add(COMP_LABELS[1]);
        detailsPane.add(Box.createVerticalStrut(15));
        detailsPane.add(COMP_LABELS[2]);
        detailsPane.add(Box.createVerticalStrut(15));
        detailsPane.add(ASSEMBLY_LABEL);
        detailsPane.add(Box.createVerticalStrut(35));
        detailsPane.add(TOTAL_LABEL);


        JScrollPane textScroller = new JScrollPane(detailsPane);
        textScroller.setMaximumSize(scrollerDim); // Upper bound
        textScroller.setPreferredSize(scrollerDim); // Ensure text size gets as close to upper as possible

        textScroller.setAlignmentY(Component.CENTER_ALIGNMENT);
        textScroller.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
        textScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return textScroller;
    }

    /**
     * Creates a new order
     */
    protected void saveOrder() {
        // Hide saveOrder button when checking out
        ((MainFrame) SwingUtilities.getWindowAncestor(this)).hideSaveOrderButton();
        saving = true;

        selectionPane.removeAll();
        selectionPane.add(new SaveOrderPanel(SELECTOR_WIDTH, selectionPane.getHeight(), CURRENT_ORDER));
        selectionPane.revalidate();
        selectionPane.repaint();
    }

    /**
     * Creates a panel containing both the radio buttons for current category, and buttons to change category
     */
    protected void buildSelectionPanel() {
        selectionPane.removeAll();
        saving = false;
        Dimension size = new Dimension(SELECTOR_WIDTH, height);
        selectionPane.setMaximumSize(size);
        selectionPane.setPreferredSize(size);
        selectionPane.setLayout(new BoxLayout(selectionPane, BoxLayout.PAGE_AXIS));

        for (JScrollPane c : TABLES) {
            c.setPreferredSize(size);
            c.getVerticalScrollBar().setUnitIncrement(16); // Make scroll speed normal
            c.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();

        for (int i=0; i<COMPS.length; i++) {
            tabbedPane.addTab(COMPS[i], TABLES[i]);
        }

        tabbedPane.setMaximumSize(size);
        tabbedPane.setPreferredSize(size);

        selectionPane.add(tabbedPane);

        selectionPane.revalidate();
        selectionPane.repaint();

        if (isOrderFull()) {
            ((MainFrame) SwingUtilities.getWindowAncestor(this)).showSaveOrderButton();
        }
    }

    /**
     * Updates the selection tables to be current with the database
     */
    protected void updateCatalogue() {
        for (int i=0; i<TABLES.length; i++) {
            TABLES[i] = buildSelectorTable(MainFrame.ALL_COMPS[i], i);
        }
    }

    /**
     * Resets the bike builder, ready for the next customer
     */
    protected void reset() {
        for (int i=0; i<COMPS.length; i++) {
            COMP_LABELS[i].setText("");
            CURRENT_ORDER[i] = null;
            ((JTable) TABLES[i].getViewport().getView()).clearSelection();  // Deselects currently selected BikeComponent
        }

        ASSEMBLY_LABEL.setText("");
        TOTAL_LABEL.setText("");
        ((MainFrame) SwingUtilities.getWindowAncestor(this)).hideSaveOrderButton();

        selectionPane.removeAll();

        buildSelectionPanel();

        selectionPane.revalidate();
        selectionPane.repaint();

        ((MainFrame) SwingUtilities.getWindowAncestor(this)).updateCatalogue();
    }
}
