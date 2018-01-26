/*
 * This code is part of the Java FITS library developed 1996-2012 by T.A. McGlynn (NASA/GSFC)
 * The code is available in the public domain and may be copied, modified and used
 * by anyone in any fashion for any purpose without restriction. 
 * 
 * No warranty regarding correctness or performance of this code is given or implied.
 * Users may contact the author if they have questions or concerns.
 * 
 * The author would like to thank many who have contributed suggestions, 
 * enhancements and bug fixes including:
 * David Glowacki, R.J. Mathar, Laurent Michel, Guillaume Belanger,
 * Laurent Bourges, Rose Early, Fred Romelfanger, Jorgo Baker, A. Kovacs, V. Forchi, J.C. Segovia,
 * Booth Hartley and Jason Weiss.  
 * I apologize to any contributors whose names may have been inadvertently omitted.
 * 
 *      Tom McGlynn
 */
package fr.nom.tam.fits;

import java.util.Comparator;

/** This class implements a comparator which ensures
 *  that FITS keywords are written out in a proper order.
 */
public class HeaderOrder implements Comparator {

    /** Can two cards be exchanged when being written out? */
    public boolean equals(Object a, Object b) {
        return compare(a, b) == 0;
    }

    /** Which order should the cards indexed by these keys
     *  be written out?  This method assumes that the
     *  arguments are either the FITS Header keywords as
     *  strings, and some other type (or null) for comment
     *  style keywords.
     *
     * @return -1 if the first argument should be written first <br>
     *          1 if the second argument should be written first <br>
     *          0 if either is legal.
     */
    public int compare(Object a, Object b) {

        // Note that we look at each of the ordered FITS keywords in the required
        // order.
        String c1, c2;

        if (a != null && a instanceof String) {
            c1 = (String) a;
        } else {
            c1 = " ";
        }

        if (b != null && b instanceof String) {
            c2 = (String) b;
        } else {
            c2 = " ";
        }

        // Equals are equal
        if (c1.equals(c2)) {
            return 0;
        }

        // Now search in the order in which cards must appear
        // in the header.
        if (c1.equals("SIMPLE") || c1.equals("XTENSION")) {
            return -1;
        }
        if (c2.equals("SIMPLE") || c2.equals("XTENSION")) {
            return 1;
        }

        if (c1.equals("BITPIX")) {
            return -1;
        }
        if (c2.equals("BITPIX")) {
            return 1;
        }

        if (c1.equals("NAXIS")) {
            return -1;
        }
        if (c2.equals("NAXIS")) {
            return 1;
        }

        // Check the NAXISn cards.  These must
        // be in axis order.
        if (naxisN(c1) > 0) {
            if (naxisN(c2) > 0) {
                if (naxisN(c1) < naxisN(c2)) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return -1;
        }

        if (naxisN(c2) > 0) {
            return 1;
        }

        // The EXTEND keyword is no longer required in the FITS standard
        // but in earlier versions of the standard it was required to
        // be here if present in the primary data array.
        if (c1.equals("EXTEND")) {
            return -1;
        }
        if (c2.equals("EXTEND")) {
            return 1;
        }

        if (c1.equals("PCOUNT")) {
            return -1;
        }
        if (c2.equals("PCOUNT")) {
            return 1;
        }

        if (c1.equals("GCOUNT")) {
            return -1;
        }
        if (c2.equals("GCOUNT")) {
            return 1;
        }

        if (c1.equals("TFIELDS")) {
            return -1;
        }
        if (c2.equals("TFIELDS")) {
            return 1;
        }

        // In principal this only needs to be in the first 36 cards,
        // but we put it here since it's convenient.  BLOCKED is
        // deprecated currently.
        if (c1.equals("BLOCKED")) {
            return -1;
        }
        if (c2.equals("BLOCKED")) {
            return 1;
        }

        // Note that this must be at the end, so the
        // values returned are inverted.
        if (c1.equals("END")) {
            return 1;
        }
        if (c2.equals("END")) {
            return -1;
        }

        // All other cards can be in any order.
        return 0;
    }

    /** Find the index for NAXISn keywords */
    private int naxisN(String key) {

        if (key.length() > 5 && key.substring(0, 5).equals("NAXIS")) {
            for (int i = 5; i < key.length(); i += 1) {

                char c = key.charAt(i);
                if ('0' > c || c > '9') {
                    break;
                }
                return Integer.parseInt(key.substring(5));
            }
        }
        return -1;
    }
}
