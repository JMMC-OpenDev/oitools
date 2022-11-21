/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import static fr.jmmc.oitools.model.RuleFailure.replaceAll;
import static fr.jmmc.oitools.model.XmlOutputVisitor.encodeTagContent;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Management of datas related to the creation of the error (toString/XML)
 * this class is a value for hash maps
 * @author kempsc
 */
public final class DataLocation {

    /** shared empty instance */
    final static DataLocation EMPTY = new DataLocation(null);

    /* members */
    /** associated Rule */
    private final Rule rule;
    /** values */
    private ArrayList<Object> values = null;
    /** limits */
    private ArrayList<Object> limits = null;
    /** expecteds */
    private ArrayList<String> expecteds = null;
    /** rows */
    private ArrayList<Integer> rows = null;
    /** column */
    private ArrayList<Integer> cols = null;
    /** extra details */
    private ArrayList<String> details = null;

    DataLocation(final Rule rule) {
        // null table
        this.rule = rule;
    }

    /**
     * Clear the temporary state (cleanup)
     */
    void cleanup() {
        if (!isEmpty()) {
            values.trimToSize();
            limits.trimToSize();
            expecteds.trimToSize();
            rows.trimToSize();
            cols.trimToSize();
            details.trimToSize();
        }
    }

    /* ------------  ALL HELPER FOR DIFFERENT ARGUMENTS  --------------- */
    // Value 
    // 1
    /**
     * Add data for a Value (Fixed information)
     * @param value int
     */
    public void addFixedValue(int value) {
        rule.checkDataType(RuleDataType.VALUE);
        setValues(NumberUtils.valueOf(value), null, null, null, null, null);
    }

    // 3
    /**
     * Add data for a Value (Fixed information)
     * @param value String
     */
    public void addFixedValue(String value) {
        rule.checkDataType(RuleDataType.VALUE);
        setValues(value, null, null, null, null, null);
    }

    // 12
    /**
     * Add data for a Value
     * @param value String
     */
    public void addKeywordValue(String value) {
        rule.checkDataType(RuleDataType.VALUE);
        setValues(value, null, null, null, null, null);
    }

    // 3
    /**
     * Add data for a Value 
     * @param value double
     */
    public void addKeywordValue(double value) {
        rule.checkDataType(RuleDataType.VALUE);
        setValues(Double.valueOf(value), null, null, null, null, null);
    }

    // 6
    /**
     * Add data for a Value and an expected Value
     * @param value String
     * @param expected String
     */
    public void addKeywordValue(String value, String expected) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED);
        setValues(value, null, expected, null, null, null);
    }

    // 4
    /**
     * Add data for a Value and an expected Value
     * @param value char
     * @param expected char
     */
    public void addKeywordValue(char value, char expected) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED);
        setValues(String.valueOf(value), null, String.valueOf(expected), null, null, null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value
     * @param value short
     * @param expected String
     */
    public void addKeywordValue(short value, String expected) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED);
        setValues(Short.valueOf(value), null, expected, null, null, null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value
     * @param value int
     * @param expected int
     */
    public void addKeywordValue(int value, int expected) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED);
        setValues(NumberUtils.valueOf(value), null, String.valueOf(expected), null, null, null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value
     * @param value double
     * @param expected String
     */
    public void addKeywordValue(double value, String expected) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED);
        setValues(Double.valueOf(value), null, expected, null, null, null);
    }

    // Value / row
    // 2
    /**
     * Add data for a Value and a Row
     * @param value String
     * @param row int
     */
    public void addValueAt(String value, int row) {
        rule.checkDataType(RuleDataType.VALUE_ROW);
        setValues(value, null, null, NumberUtils.valueOf(row), null, null);
    }

    // 2
    /**
     * Add data for a Value and a Row
     * @param value short
     * @param row int
     */
    public void addValueAt(short value, int row) {
        rule.checkDataType(RuleDataType.VALUE_ROW);
        setValues(Short.valueOf(value), null, null, NumberUtils.valueOf(row), null, null);
    }

    // 2
    /**
     * Add data for a Value and a Row
     * @param value int
     * @param row int
     */
    public void addValueAt(int value, int row) {
        rule.checkDataType(RuleDataType.VALUE_ROW);
        setValues(NumberUtils.valueOf(value), null, null, NumberUtils.valueOf(row), null, null);
    }

    // 2
    /**
     * Add data for a Value and a Row
     * @param value float
     * @param row int
     */
    public void addValueAt(float value, int row) {
        rule.checkDataType(RuleDataType.VALUE_ROW);
        setValues(Float.valueOf(value), null, null, NumberUtils.valueOf(row), null, null);
    }

    // 7
    /**
     * Add data for a Value and a Row
     * @param value double
     * @param row int
     */
    public void addValueAt(double value, int row) {
        rule.checkDataType(RuleDataType.VALUE_ROW);
        setValues(Double.valueOf(value), null, null, NumberUtils.valueOf(row), null, null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value in a Row
     * @param value short
     * @param expected String
     * @param row int
     */
    public void addValueAt(short value, String expected, int row) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED_ROW);
        setValues(Short.valueOf(value), null, expected, NumberUtils.valueOf(row), null, null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value in a Row
     * @param value String
     * @param expected String
     * @param row int
     */
    public void addValueAt(String value, String expected, int row) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED_ROW);
        setValues(value, null, expected, NumberUtils.valueOf(row), null, null);
    }

    // 4
    /**
     * Add data for a Value and a limit Value in a Row
     * @param value int
     * @param limit int
     * @param row int
     */
    public void addValuesAt(int value, int limit, int row) {
        rule.checkDataType(RuleDataType.VALUE_LIMIT_ROW);
        setValues(NumberUtils.valueOf(value), NumberUtils.valueOf(limit), null, NumberUtils.valueOf(row), null, null);
    }

    // 1
    /**
     * Add data for a Value and a limit Value in a Row
     * @param value double
     * @param limit double
     * @param row int
     */
    public void addValuesAt(double value, double limit, int row) {
        rule.checkDataType(RuleDataType.VALUE_LIMIT_ROW);
        setValues(Double.valueOf(value), Double.valueOf(limit), null, NumberUtils.valueOf(row), null, null);
    }

    // 2
    /**
     * Add data for a Value in two Row
     * @param value short
     * @param row1 int
     * @param row2 int
     */
    public void addValueAtRows(short value, int row1, int row2) {
        rule.checkDataType(RuleDataType.VALUE_ROW1_ROW2);
        setValues(Short.valueOf(value), null, null, null, null, "" + row1 + " | " + row2);
    }

    // 2
    /**
     * Add data for a Value in two Row
     * @param value String
     * @param row1 int
     * @param row2 int
     */
    public void addValueAtRows(String value, int row1, int row2) {
        rule.checkDataType(RuleDataType.VALUE_ROW1_ROW2);
        setValues(value, null, null, null, null, "" + row1 + " | " + row2);
    }

    // Value / row / col    
    // 2
    /**
     * Add data for a Value in a Row and a Column
     * @param value String
     * @param row int
     * @param col int
     */
    public void addColValueAt(String value, int row, int col) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL);
        setValues(value, null, null, NumberUtils.valueOf(row), NumberUtils.valueOf(col), null);
    }

    // 2
    /**
     * Add data for a Value in a Row and a Column
     * @param value short
     * @param row int
     * @param col int
     */
    public void addColValueAt(short value, int row, int col) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL);
        setValues(Short.valueOf(value), null, null, NumberUtils.valueOf(row), NumberUtils.valueOf(col), null);
    }

    // 1
    /**
     * Add data for a Value in a Row and a Column
     * @param value float
     * @param row int
     * @param col int
     */
    public void addColValueAt(float value, int row, int col) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL);
        setValues(Float.valueOf(value), null, null, NumberUtils.valueOf(row), NumberUtils.valueOf(col), null);
    }

    // 2
    /**
     * Add data for a Value in a Row and a Column
     * @param value double
     * @param row int
     * @param col int
     */
    public void addColValueAt(double value, int row, int col) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL);
        setValues(Double.valueOf(value), null, null, NumberUtils.valueOf(row), NumberUtils.valueOf(col), null);
    }

    // 1
    /**
     * Add data for a Value and an expected Value in a Row and a Column
     * @param value short
     * @param expected String
     * @param row int
     * @param col int
     */
    public void addColValueAt(short value, String expected, int row, int col) {
        rule.checkDataType(RuleDataType.VALUE_EXPECTED_ROW_COL);
        setValues(Short.valueOf(value), null, expected, NumberUtils.valueOf(row), NumberUtils.valueOf(col), null);
    }

    // 1
    /**
     * Add data for a Value in a Row and two Column
     * @param value short
     * @param row int
     * @param col1 int
     * @param col2 int
     */
    public void addValueAtCols(short value, int row, int col1, int col2) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL1_COL2);
        setValues(Short.valueOf(value), null, null, NumberUtils.valueOf(row), null, "" + col1 + " | " + col2);
    }

    // 1
    /**
     * Add data for a Value in a Row and a Column and more details informations
     * @param value int
     * @param row int
     * @param col int
     * @param details String
     */
    public void addColValueAt(int value, int row, int col, String details) {
        rule.checkDataType(RuleDataType.VALUE_ROW_COL_DETAILS);
        setValues(NumberUtils.valueOf(value), null, null, NumberUtils.valueOf(row), NumberUtils.valueOf(col), details);
    }

    /**
     * Private constructor and ensureCapacity for all ArrayLists
     * @param value value Object 
     * @param limit limit values (optional)
     * @param expected expected values (optional)
     * @param row row's failures information (optional)
     * @param col column's failures information (optional)
     * @param details more details information on failures (optional)
     */
    private void setValues(Object value, Object limit, String expected, Integer row, Integer col, String details) {
        ensureCapacity();
        this.values.add(value);
        this.limits.add(limit);
        this.expecteds.add(expected);
        this.rows.add(row);
        this.cols.add(col);
        this.details.add(details);
    }

    private void ensureCapacity() {
        if (isEmpty()) {
            final int newSize = 10;
            values = new ArrayList<Object>(newSize);
            limits = new ArrayList<Object>(newSize);
            expecteds = new ArrayList<String>(newSize);
            rows = new ArrayList<Integer>(newSize);
            cols = new ArrayList<Integer>(newSize);
            details = new ArrayList<String>(newSize);
        } else {
            final int newSize = values.size() * 2;
            values.ensureCapacity(newSize);
            limits.ensureCapacity(newSize);
            expecteds.ensureCapacity(newSize);
            rows.ensureCapacity(newSize);
            cols.ensureCapacity(newSize);
            details.ensureCapacity(newSize);
        }
    }

    /**
     * Check Array values is null or empty
     * @return boolean true if array values isEmpty
     */
    public boolean isEmpty() {
        return values == null;
    }

    /* --------------- GETTER --------------------*/
    /**
     * Return the values
     * @return values
     */
    public ArrayList<Object> getValues() {
        return values;
    }

    /**
     * Return the limits
     * @return limits
     */
    public ArrayList<Object> getLimits() {
        return limits;
    }

    /**
     * Return the expecteds values
     * @return expecteds
     */
    public ArrayList<String> getExpecteds() {
        return expecteds;
    }

    /**
     * Return the rows
     * @return rows
     */
    public ArrayList<Integer> getRows() {
        return rows;
    }

    /**
     * Return the cols
     * @return cols
     */
    public ArrayList<Integer> getCols() {
        return cols;
    }

    /**
     * Return the details
     * @return details
     */
    public ArrayList<String> getDetails() {
        return details;
    }

    /* --- FORMAT MESSAGE --- */
    /** RegExp expression to match {{VALUE}} */
    private static final Pattern PATTERN_VALUE = Pattern.compile("\\{\\{VALUE\\}\\}");
    /** RegExp expression to match {{LIMIT}} */
    private static final Pattern PATTERN_LIMIT = Pattern.compile("\\{\\{LIMIT\\}\\}");
    /** RegExp expression to match {{EXPECTED}} */
    private static final Pattern PATTERN_EXPECTED = Pattern.compile("\\{\\{EXPECTED\\}\\}");
    /** RegExp expression to match {{ROW}} */
    private static final Pattern PATTERN_ROW = Pattern.compile("\\{\\{ROW\\}\\}");
    /** RegExp expression to match {{COL}} */
    private static final Pattern PATTERN_COL = Pattern.compile("\\{\\{COL\\}\\}");
    /** RegExp expression to match {{DETAILS}} */
    private static final Pattern PATTERN_DETAILS = Pattern.compile("\\{\\{DETAILS\\}\\}");

    String formatMessage(final String ruleMessage, final int index) {
        if (isEmpty()) {
            // rule message should be complete:
            return ruleMessage;
        }
        String msg = ruleMessage;

        msg = replaceAll(PATTERN_VALUE, msg,
                (getValues().get(index) != null) ? getValues().get(index).toString() : ""
        );
        msg = replaceAll(PATTERN_LIMIT, msg,
                (getLimits().get(index) != null) ? getLimits().get(index).toString() : ""
        );
        msg = replaceAll(PATTERN_EXPECTED, msg,
                (getExpecteds().get(index) != null) ? getExpecteds().get(index) : ""
        );
        msg = replaceAll(PATTERN_ROW, msg,
                (getRows().get(index) != null) ? getRows().get(index).toString() : ""
        );
        msg = replaceAll(PATTERN_COL, msg,
                (getCols().get(index) != null) ? getCols().get(index).toString() : ""
        );
        msg = replaceAll(PATTERN_DETAILS, msg,
                (getDetails().get(index) != null) ? getDetails().get(index) : ""
        );
        return msg;
    }

    /**
     * Returns a string representation of this class
     * @return a string representation of this class
     */
    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * Returns a string representation of this class
     * @param sb StringBuilder
     * @return a string representation of this class
     */
    public StringBuilder toString(final StringBuilder sb) {
        if (!isEmpty()) {
            // TODO FIX: message
            for (int i = 0, len = getValues().size(); i < len; i++) {
                sb.append('\n');
                sb.append('[');
                sb.append(getValues().get(i));
                sb.append('\t');

                if (!getLimits().isEmpty() && getLimits().get(i) != null) {
                    sb.append(getLimits().get(i));
                }
                sb.append('\t');

                if (!getExpecteds().isEmpty() && getExpecteds().get(i) != null) {
                    sb.append(getExpecteds().get(i));
                }
                sb.append('\t');

                if (!getRows().isEmpty() && getRows().get(i) != null) {
                    sb.append(getRows().get(i));
                }
                sb.append('\t');

                if (!getCols().isEmpty() && getCols().get(i) != null) {
                    sb.append(getCols().get(i));

                }
                sb.append('\t');

                if (!getDetails().isEmpty() && getDetails().get(i) != null) {
                    sb.append(getDetails().get(i));

                }
                sb.append(']');
            }
        }
        return sb;
    }

    /**
     * Returns a XML representation of this class
     * @param sb StringBuilder
     * @param ruleMessage
     * @param data
     * @return a XML representation of this class
     */
    public StringBuilder toXML(final StringBuilder sb, final String ruleMessage, final DataLocation data) {
        if (!isEmpty()) {
            for (int i = 0, len = getValues().size(); i < len; i++) {
                sb.append("    <data>\n");
                sb.append("      <value>").append(getValues().get(i)).append("</value>\n");

                final Object limit = getLimits().get(i);
                if (limit != null) {
                    sb.append("      <limit>").append(limit).append("</limit>\n");
                }

                final String expected = getExpecteds().get(i);
                if (expected != null) {
                    sb.append("      <expected>").append(expected).append("</expected>\n");
                }

                final Integer row = getRows().get(i);
                if (row != null) {
                    sb.append("      <row>").append(row).append("</row>\n");
                }

                final Integer col = getCols().get(i);
                if (col != null) {
                    sb.append("      <col>").append(col).append("</col>\n");
                }

                final String detail = getDetails().get(i);
                if (detail != null) {
                    sb.append("      <detail>").append(detail).append("</detail>\n");
                }

                sb.append("      <message>");
                sb.append(encodeTagContent(data.formatMessage(ruleMessage, i)));
                sb.append("</message>\n");

                sb.append("    </data>\n");
            }
        }
        return sb;
    }

}
