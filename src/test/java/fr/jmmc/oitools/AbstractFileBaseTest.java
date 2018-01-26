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
package fr.jmmc.oitools;

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import org.junit.Assert;

/**
 * Allows to manage all the .properties (the map) 
 * 
 * @author kempsc
 */
public abstract class AbstractFileBaseTest extends JUnitBaseTest {

    /** stores key / value pairs */
    private static Properties props = null;
    /** counter of all valid assert */
    private static int assertCount;

    protected static void initializeTest() {
        props = new Properties();
    }

    protected static void shutdownTest() {
        props = null;
    }

    protected static void reset() {
        props.clear();
        assertCount = 0;
    }

    protected static void checkAssertCount() {
        // TODO: sort properties into TreeMap
        // check that all keys in TreeMap are tested (store flag into usedSet)
        // log all untested keys !!

        Assert.assertEquals("checkAssertCount", props.size(), assertCount);
    }

    protected static void load(File propFile) throws IOException {
        logger.log(Level.INFO, "Loading properties: {0}", propFile.getAbsolutePath());

        // Load the properties:
        props.load(new FileInputStream(propFile));
    }

    protected static void save(File propFile) throws IOException {
        if (!props.isEmpty()) {
            logger.log(Level.INFO, "Saving properties: {0}", propFile.getAbsolutePath());

            // Save the properties:
            props.store(new FileOutputStream(propFile), "");
        }
    }

    protected static void assertEquals(Object expected, Object actual) {
        Assert.assertEquals((expected == null) ? "null" : expected, actual);
        assertCount++;
    }

    protected static void assertEqualsInt(Object expected, int actual) {
        Assert.assertEquals(expected, Integer.toString(actual));
        assertCount++;
    }

    protected static Object get(String key) {
        return props.get(key);
    }

    protected static void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    protected static void put(String key, String value) {
        props.put(key, value);
    }

    protected static boolean contains(String key) {
        return props.containsKey(key);
    }

    protected static String getColumnValues(FitsTable table, ColumnMeta columnMeta) {
        String propValue = null;
        
        switch (columnMeta.getDataType()) {
            case TYPE_CHAR:
                String[] chvalue = table.getColumnString(columnMeta.getName());
                propValue = Arrays.deepToString(chvalue);
                break;
            case TYPE_SHORT:
                if (columnMeta.isArray()) {
                    // Use getColumnAsShorts(s) to handle both std & derived columns
                    short[][] svalues = table.getColumnAsShorts(columnMeta.getName());
                    propValue = Arrays.deepToString(svalues);
                } else {
                    short[] svalue = table.getColumnShort(columnMeta.getName());
                    propValue = Arrays.toString(svalue);
                }
                break;
            case TYPE_INT:
                if (columnMeta.isArray()) {
                    // Use getColumnAsShorts(s) to handle both std & derived columns
                    int[][] ivalues = table.getColumnInts(columnMeta.getName());
                    propValue = Arrays.deepToString(ivalues);
                } else {
                    int[] ivalue = table.getColumnInt(columnMeta.getName());
                    propValue = Arrays.toString(ivalue);
                }
                break;
            case TYPE_DBL:
                // Use getColumnAsDouble(s) to handle both std & derived columns
                // If column value dont exist, compute it
                if (columnMeta.isArray()) {
                    double[][] dvalues = table.getColumnAsDoubles(columnMeta.getName());
                    propValue = deepToString(dvalues); // FIXED FORMAT
                } else {
                    double[] dvalue = table.getColumnAsDouble(columnMeta.getName());
                    propValue = toString(dvalue); // FIXED FORMAT
                }
                break;
            case TYPE_REAL:
                if (columnMeta.isArray()) {
                    // Impossible case in OIFits
                } else {
                    float[] fvalue = table.getColumnFloat(columnMeta.getName());
                    propValue = toString(fvalue); // FIXED FORMAT
                }
                break;
            case TYPE_COMPLEX:
                if (columnMeta.isArray()) {
                    float[][][] cvalues = table.getColumnComplexes(columnMeta.getName());
                    propValue = deepToString(cvalues); // FIXED FORMAT
                } else {
                    // Impossible case in OIFits
                }
                break;
            case TYPE_LOGICAL:
                if (columnMeta.is3D()) {
                    boolean[][][] bvalues = table.getColumnBoolean3D(columnMeta.getName());
                    propValue = Arrays.deepToString(bvalues);
                } else if (columnMeta.isArray()) {
                    boolean[][] bvalues = table.getColumnBooleans(columnMeta.getName());
                    propValue = Arrays.deepToString(bvalues);
                } else {
                    // Impossible case in OIFits
                }
                break;
            default:
                // Not Applicable
                break;
        }
        return (propValue != null) ? propValue : "null";
    }
    
    protected static String getColunmMinMax(FitsTable table, ColumnMeta columnMeta) {
        String propValue = null;
        final Object minmax = table.getMinMaxColumnValue(columnMeta.getName());

        if (minmax != null) {
            switch (columnMeta.getDataType()) {
                case TYPE_CHAR:
                    // Not Applicable
                    break;

                case TYPE_SHORT:
                    short[] srange = (short[]) minmax;
                    propValue = Arrays.toString(srange);
                    break;
                case TYPE_INT:
                    int[] irange = (int[]) minmax;
                    propValue = Arrays.toString(irange);
                    break;

                case TYPE_DBL:
                    double[] drange = (double[]) minmax;
                    propValue = toString(drange); // FIXED FORMAT
                    break;

                case TYPE_REAL:
                    float[] frange = (float[]) minmax;
                    propValue = toString(frange); // FIXED FORMAT
                    break;

                case TYPE_COMPLEX:
                    // Not Applicable
                    break;

                case TYPE_LOGICAL:
                    // Not Applicable
                    break;

                default:
                // do nothing
            }
        }
        return (propValue != null) ? propValue : "null";
    }
}
