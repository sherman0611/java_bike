package team22.UI;

import team22.businessLogicLayer.Sanitisation;
import team22.businessLogicLayer.Validation;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple helper class to create input forms
 * Includes validation, custom form elements, and easy data retrieval
 */
public class DetailsForm extends JPanel {
    // Define cosmetic constants
    private static final int STRUT_HEIGHT = 10;
    private static final Font LABEL_FONT = Fonts.getSizedFont(Fonts.OPENSANS_MEDIUM, 18);
    private static final Font INPUT_FONT = Fonts.getSizedFont(Fonts.OPENSANS_REGULAR, 18);
    private static final int LINE_HEIGHT = Fonts.getHeight(INPUT_FONT);

    // Define validation constants
    public static final int NO_VALIDATION = 0;
    public static final int IS_NUMBER = 1;
    public static final int IS_POSTCODE = 2;
    public static final int IS_PASSWORD = 3;
    public static final int IS_MONEY = 4;
    public static final int CAN_BE_EMPTY = 5;

    // Define Maps to store form data, for easy retrieval
    private Map<String, JTextField> textFieldMap;
    private Map<String, Integer> validationMap;
    private Map<String, JLabel> labelMap;
    private Map<String, JComponent> overriddenMap;

    private int labelHeight;
    private int inputHeight;


    /**
     * Initialises a basic DetailsForm
     * @param fields the fields to create a form for
     * @param validationMethods the corrosponding validation methods for the fields
     * @param width the width of the form
     */
    public DetailsForm(String[] fields, int[] validationMethods, int width) {
        init(fields, validationMethods, width, new JComponent[fields.length]);
    }

    /**
     * Initialises a DetailsForm with custom components (i.e. a JCheckBox, or a JComboBox)
     * @param fields the fields to create a form for
     * @param validationMethods the corrosponding validation methods for the fields
     * @param width the width of the form
     * @param overrideComps a list the same length as fields, with null where you want a JTextField input, and a
     *                      component where you want a custom component
     */
    public DetailsForm(String[] fields, int[] validationMethods, int width, JComponent[] overrideComps) {
        init(fields, validationMethods, width, overrideComps);
    }

    /**
     * Creates the details form with given parameters
     * @param fields the fields of the form
     * @param validationMethods the validation methods for the form's fields
     * @param width the width of the form
     * @param overrideComps the components that should be placed instead of a JTextField
     */
    private void init(String[] fields, int[] validationMethods, int width, JComponent[] overrideComps) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(Color.WHITE);

        labelHeight = LINE_HEIGHT * 5 / 4;
        inputHeight = LINE_HEIGHT * 3 / 2;

        // Initialise the maps as HashMaps
        textFieldMap = new HashMap<>();
        labelMap = new HashMap<>();
        validationMap = new HashMap<>();
        overriddenMap = new HashMap<>();

        for (int i=0; i<fields.length; i++) {
            String s = fields[i];

            JLabel label = new JLabel(s + ":");
            JComponent input;

            // If current field should not have a custom component, add a JTextField, unless validation method is IS_PASSWORD
            // in which case add a JPasswordField
            if (overrideComps[i] == null) {
                validationMap.put(s, validationMethods[i]);

                if (validationMethods[i] == IS_PASSWORD) {
                    input = new JPasswordField();
                } else {
                    input = new JTextField();
                }
                // Add input component to textFieldMap, with the field name as a key
                textFieldMap.put(s, (JTextField) input);
            } else {
                // If current field should have a custom component, add that to overriden map
                input = overrideComps[i];
                overriddenMap.put(s, input);  // Add with field name as a key
            }

            label.setFont(LABEL_FONT);
            input.setFont(INPUT_FONT);

            label.setPreferredSize(new Dimension(width, labelHeight));
            label.setMaximumSize(new Dimension(width, labelHeight));

            input.setPreferredSize(new Dimension(width, inputHeight));
            input.setMaximumSize(new Dimension(width, inputHeight));

            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            input.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(label);
            add(input);

            add(Box.createVerticalStrut(STRUT_HEIGHT));

            // Add the label to the label map, with field name as a key (for use in validation)
            labelMap.put(s, label);
        }

        setPreferredSize(new Dimension(width, getHeight()));
        setMaximumSize(new Dimension(width, getHeight()));
    }

    /**
     * Checks if the fields that need validating are valid, and updates their labels if not
     * @return true if all fields are valid, false if there are any that are not
     */
    public boolean validInputs() {
        boolean allValid = true;
        // Only check validity of components that have a JTextField or JPasswordField associated with them
        for (String s : textFieldMap.keySet()) {
            boolean currentValid = false;
            int validationMethod = validationMap.get(s);
            String currentText = textFieldMap.get(s).getText().trim();

            if (!currentText.isEmpty() || validationMethod == CAN_BE_EMPTY) {
                // Check if current field is valid using its corrosponding validation method
                switch (validationMethod) {
                    case IS_NUMBER:
                        currentValid = Validation.isValidNumber(currentText);
                        break;
                    case IS_POSTCODE:
                        currentValid = Validation.isValidPostcode(currentText);
                        break;
                    case IS_MONEY:
                        currentValid = Validation.isValidMoney(currentText);
                        break;
                    default:
                        currentValid = true;
                        break;
                }
            }

            // Update label text to reflect validity of current field
            if (currentValid) {
                labelMap.get(s).setText(s + ":");
                labelMap.get(s).setForeground(Color.BLACK);
            } else {
                labelMap.get(s).setText("Invalid " + s + ". Please try again: ");
                labelMap.get(s).setForeground(Color.RED);
            }

            allValid &= currentValid;
        }

        return allValid;
    }

    /**
     * @return The height of the DetailsForm
     */
    public int getHeight() {
        return (inputHeight + labelHeight + STRUT_HEIGHT) * (textFieldMap.size() + overriddenMap.size());
    }

    /**
     * Gets the input from a JTextField, if s corrosponds to one
     * @param s the field from which to get an input
     * @return the input of the field, or null if an invalid field name is passed
     */
    public String getInput(String s) {
        if (textFieldMap.containsKey(s) && validationMap.get(s) != IS_PASSWORD) {
            // Sanitise postcode if in a postcode field
            if (validationMap.get(s) == IS_POSTCODE) {
                return Sanitisation.sanitisePostcode(textFieldMap.get(s).getText());
            }
            return textFieldMap.get(s).getText().trim();
        }
        return null;
    }

    /**
     * Get a JComponent from a field with a custom component
     * @param s the field from which to get a component
     * @return the JComponent associated with the field, or null if an invalid field name is passed
     */
    public JComponent getOverridenComp(String s) {
        if (overriddenMap.containsKey(s)) {
            return overriddenMap.get(s);
        }
        return null;
    }

    /**
     * Gets password input as a char array (more secure than a string, as strings will never be garbage collected)
     * @param s the field to get password input from
     * @return the char array retrieved from the JPasswordField
     */
    public char[] getPasswordInput(String s) {
        if (textFieldMap.containsKey(s) && validationMap.get(s) == IS_PASSWORD) {
            return ((JPasswordField) textFieldMap.get(s)).getPassword();
        }
        return null;
    }
}
