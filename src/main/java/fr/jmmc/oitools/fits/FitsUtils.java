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
package fr.jmmc.oitools.fits;

import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.util.ArrayFuncs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * This utility class gathers several methods related to Fits handling
 * @author bourgesl
 */
public final class FitsUtils {

    /** Logger associated to test classes */
    public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsUtils.class.getName());
    /** Fits standard keywords */
    public final static Set<String> FITS_STANDARD_KEYWORDS = new HashSet<String>(64);
    /** Fits open keywords (prefix) */
    public final static List<String> FITS_OPEN_KEYWORDS = new ArrayList<String>(8);

    static {
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_SIMPLE);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_EXTEND);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_XTENSION);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_EXT_NAME);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_EXT_VER);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_BITPIX);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_NAXIS);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_NAXIS1);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_NAXIS2);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_NAXIS3);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_GCOUNT);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_PCOUNT);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_BZERO);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_BSCALE);

        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_TFIELDS);

        // checksum
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_CHECKSUM);
        FITS_STANDARD_KEYWORDS.add(FitsConstants.KEYWORD_DATASUM);

        FITS_OPEN_KEYWORDS.add(FitsConstants.KEYWORD_NAXIS);

        //Keywords for column description
        FITS_OPEN_KEYWORDS.add(FitsConstants.KEYWORD_TFORM);
        FITS_OPEN_KEYWORDS.add(FitsConstants.KEYWORD_TTYPE);

        FITS_OPEN_KEYWORDS.add(FitsConstants.KEYWORD_TDIM);
        FITS_OPEN_KEYWORDS.add(FitsConstants.KEYWORD_TUNIT);
    }

    /**
     * Define defaults for nom.tam.Fits library
     */
    public static void setup() {
        // Allow junk after a valid FITS file:
        FitsFactory.setAllowTerminalJunk(true);
        // Enable checking of strings values used in tables:
        FitsFactory.setCheckAsciiStrings(true);
        // Enable hierarchical keyword processing:
        FitsFactory.setUseHierarch(true);
        // Force to use binary tables:
        FitsFactory.setUseAsciiTables(false);
    }

    /**
     * Forbidden constructor
     */
    private FitsUtils() {
        super();
    }

    /**
     * Return true if the given keyword name corresponds to a standard FITS keyword
     * @param name keyword name
     * @return true if the given keyword name corresponds to a standard FITS keyword
     */
    public static boolean isStandardKeyword(final String name) {
        if (FITS_STANDARD_KEYWORDS.contains(name)) {
            return true;
        }

        return isOpenKeyword(name);
    }

    /**
     * Return true if the given keyword name corresponds to an open FITS keyword
     * @param name keyword name
     * @return true if the given keyword name corresponds to an open FITS keyword
     */
    public static boolean isOpenKeyword(final String name) {

        for (String prefix : FITS_OPEN_KEYWORDS) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Dump Fits file given its absolute file path
     * @param absFilePath absolute file path
     * @param dumpValues true indicates to dump column values
     * @return true if any error occurred; false otherwise
     */
    public static boolean dumpFile(final String absFilePath, final boolean dumpValues) {
        boolean error = false;

        logger.log(Level.INFO, "Dump file: {0}", absFilePath);

        final StringBuilder sb = new StringBuilder((dumpValues ? 128 : 16) * 1024);

        final long start = System.nanoTime();
        try {

            final Fits s = new Fits(absFilePath);

            final BasicHDU[] hdus = s.read();

            final int len = hdus.length;
            sb.append("HDUs = ").append(len).append('\n');

            BasicHDU hdu;
            for (int i = 0; i < len; i++) {
                hdu = hdus[i];

                // dump HDU:
                FitsUtils.dumpHDU(sb, hdu, dumpValues);
            }

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "FitsUtils.dumpFile: failure occured while dumping file : " + absFilePath, th);
            error = true;
        } finally {
            final long end = System.nanoTime();

            logger.info(sb.toString());
            logger.log(Level.INFO, "FitsUtils.dumpFile: duration = {0} ms.", 1e-6d * (end - start));
        }

        return error;
    }

    /**
     * Dump HDU (header and column meta data) and also column values if dumpValues is true
     * @param hdu HDU to dump
     * @param dumpValues true indicates to dump column values
     * @throws FitsException if any FITS exception occurs
     * @return output as string
     */
    public static String dumpHDU(final BasicHDU hdu, final boolean dumpValues) throws FitsException {
        final StringBuilder sb = new StringBuilder((dumpValues ? 16 : 2) * 1024);

        // dump HDU:
        FitsUtils.dumpHDU(sb, hdu, dumpValues);

        return sb.toString();
    }

    /**
     * Dump HDU (header and column meta data) and also column values if dumpValues is true
     * @param sb output buffer
     * @param hdu HDU to dump
     * @param dumpValues true indicates to dump column values
     * @throws FitsException if any FITS exception occurs
     */
    public static void dumpHDU(final StringBuilder sb, final BasicHDU hdu, final boolean dumpValues) throws FitsException {
        dumpHeader(sb, hdu.getHeader());

        // dump binary HDU only :
        if (hdu instanceof BinaryTableHDU) {
            dumpData(sb, (BinaryTableHDU) hdu, dumpValues);
        } else {
            logger.log(Level.WARNING, "Unsupported HDU: {0}", hdu.getClass());
        }
    }

    /**
     * Dump HDU header
     * @param sb output buffer
     * @param header HDU header to dump
     */
    public static void dumpHeader(final StringBuilder sb, final Header header) {

        final String extName = header.getTrimmedStringValue("EXTNAME");

        sb.append("--------------------------------------------------------------------------------\n");
        if (extName != null) {
            sb.append("EXTNAME = ").append(extName).append('\n');
        }

        final int nCards = header.getNumberOfCards();

        sb.append("KEYWORDS = ").append(nCards).append('\n');

        HeaderCard card;
        String key;
        for (Iterator<?> it = header.iterator(); it.hasNext();) {
            card = (HeaderCard) it.next();

            key = card.getKey();

            if ("END".equals(key)) {
                break;
            }

            sb.append("KEYWORD ").append(key).append(" = ");
            if (card.getValue() != null) {
                sb.append("'").append(card.getValue()).append("'");
            }
            sb.append("\t// ");
            if (card.getComment() != null) {
                sb.append(card.getComment());
            }
            sb.append('\n');
        }
    }

    /**
     * Dump binary table HDU (column meta data) and also column values if dumpValues is true
     * @param sb output buffer
     * @param hdu binary HDU to dump
     * @param dumpValues true indicates to dump column values
     * @throws FitsException if any FITS exception occurs
     */
    public static void dumpData(final StringBuilder sb, final BinaryTableHDU hdu, final boolean dumpValues) throws FitsException {

        final BinaryTable data = (BinaryTable) hdu.getData();

        final int nCols = data.getNCols();

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("NCOLS = ").append(nCols).append('\n');

        final int nRows = data.getNRows();

        sb.append("NROWS = ").append(nRows).append('\n');

        String unit;
        Object array;
        for (int i = 0; i < nCols; i++) {
            sb.append("COLUMN ").append(hdu.getColumnName(i)).append(" [");
            sb.append(hdu.getColumnLength(i));
            sb.append(' ');
            sb.append(hdu.getColumnType(i));
            sb.append("] (");
            unit = hdu.getColumnUnit(i);
            if (unit != null) {
                sb.append(unit);
            }
            sb.append(")\t");

            if (dumpValues) {
                // read all data and convert them to arrays[][] (encurl):
                array = data.getColumn(i);
                // array = data.getFlattenedColumn(i);
                sb.append(ArrayFuncs.arrayDescription(array));
                sb.append('\n').append(arrayToString(array));
            }
            sb.append('\n');
        }
    }

    /**
     * Return a string representation of the given array
     * @param o array (may be null)
     * @return string representation of the given array
     */
    public static String arrayToString(final Object o) {

        if (o == null) {
            return "null";
        }

        final Class<?> oClass = o.getClass();

        if (!oClass.isArray()) {
            return o.toString();
        }

        if (oClass == double[].class) {
            return Arrays.toString((double[]) o);
        } else if (oClass == float[].class) {
            return Arrays.toString((float[]) o);
        } else if (oClass == int[].class) {
            return Arrays.toString((int[]) o);
        } else if (oClass == long[].class) {
            return Arrays.toString((long[]) o);
        } else if (oClass == boolean[].class) {
            return Arrays.toString((boolean[]) o);
        } else if (oClass == short[].class) {
            return Arrays.toString((short[]) o);
        } else if (oClass == char[].class) {
            return Arrays.toString((char[]) o);
        } else if (oClass == byte[].class) {
            return Arrays.toString((byte[]) o);
        } else {
            // Non-primitive and multidimensional arrays can be cast to Object[]
            return Arrays.deepToString((Object[]) o);
        }
    }
}
