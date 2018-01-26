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
/*
 * This class provides a modifiable map in which the comment fields for FITS
 * header keywords
 * produced by this library are set.  The map is a simple String -> String
 * map where the key Strings are normally class:keyword:id where class is
 * the class name where the keyword is set, keyword is the keyword set and id
 * is an integer used to distinguish multiple instances.
 *
 * Most users need not worry about this class, but users who wish to customize
 * the appearance of FITS files may update the map.  The code itself is likely
 * to be needed to understand which values in the map must be modified.
 *
 * Note that the Header writing utilities look for the prefix ntf:: in comments
 * and if this is found, the comment is replaced by looking in this map for
 * a key given by the remainder of the original comment.
 */
package fr.nom.tam.fits;

import java.util.HashMap;
import java.util.Map;

public class HeaderCommentsMap {

    private static final Map<String, String> commentMap = new HashMap<String, String>(64);

    static {
        // number of bits per data pixel

        // updateComment("header:extend:1", "Extensions are permitted");
        // updateComment("header:simple:1", "Java FITS: " + new Date());
        // updateComment("header:xtension:1", "Java FITS: " + new Date());
        updateComment("header:naxis:1", "Dimensionality");
        // updateComment("header:extend:2", "Extensions are permitted");
        updateComment("asciitable:pcount:1", "No group data");
        updateComment("asciitable:gcount:1", "One group");
        updateComment("asciitable:tbcolN:1", "Column offset");
        updateComment("asciitable:naxis1:1", "Size of row in bytes");
        updateComment("undefineddata:naxis1:1", "Number of Bytes");
        updateComment("undefineddata:extend:1", "Extensions are permitted");
        updateComment("binarytablehdu:pcount:1", "Includes heap");
        updateComment("binarytable:naxis1:1", "Bytes per row");
        // updateComment("fits:checksum:1", "as of " + FitsDate.getFitsDateString());
        updateComment("basichdu:extend:1", "Allow extensions");
        updateComment("basichdu:gcount:1", "Required value");
        updateComment("basichdu:pcount:1", "Required value");
        updateComment("imagedata:extend:1", "Extension permitted");
        updateComment("imagedata:pcount:1", "No extra parameters");
        updateComment("imagedata:gcount:1", "One group");
        /* Null entries:
         *      header:simple:2
         *      header:bitpix:2
         *      header:naxisN:1...N
         *      header:naxis:2
         *      undefineddata:pcount:1
         *      undefineddata:gcount:1
         *      randomgroupsdata:naxis1:1
         *      randomgroupsdata:naxisN:1
         *      randomgroupsdata:groups:1
         *      randomgroupsdata:gcount:1
         *      randomgroupsdata:pcount:1
         *      binarytablehdu:theap:1
         *      binarytablehdu:tdimN:1
         *      asciitable:tformN:1
         *      asciitablehdu:tnullN:1
         *      asciitablehdu:tfields:1
         *      binarytable:pcount:1
         *      binarytable:gcount:1
         *      binarytable:tfields:1
         *      binarytable:tformN:1
         *      binarytable:tdimN:1
         *      tablehdu:naxis2:1
         */

        // LAURENT: customize header to typical values:
        updateComment("header:bitpix:1", "number of bits per data pixel");
        updateComment("header:bitpix:2", "8-bit bytes per data pixel");
        updateComment("header:extend:1", "File contains extensions");
        updateComment("header:extend:2", "File contains extensions");

        updateComment("binarytable:gcount:1", "one data group (required keyword)");
        updateComment("binarytable:pcount:1", "size of special data area");

        updateComment("header:naxisN:1", "width of row in bytes");
        updateComment("header:naxisN:2", "number of rows in table");

        updateComment("header:simple:1", "file does conform to FITS standard");

        updateComment("header:xtension:BINTABLE", "binary table extension");

        updateComment("asciitable:tfields:1", "number of fields in each row");
        updateComment("binarytable:tfields:1", "number of fields in each row");
        updateComment("tablehdu:tfields:1", "number of fields in each row");

        // Columns:
        updateComment("tablehdu:tform|", "data format of field "); // data format of field $N
        updateComment("tablehdu:ttype|", "label for field "); // label for field $N
        updateComment("tablehdu:tunit|", "physical unit of field "); // physical unit of field $N

        // LAURENT: FIX CHECKSUM (compatibility issue)
        updateComment("fits:checksum:1", "encoded checksum");
        updateComment("fits:datasum:1", "checksum of data");
    }

    public static String getComment(String key) {
        int pos = key.lastIndexOf('|');
        if (pos == -1) {
            return commentMap.get(key);
        }
        pos += 1;
        String comment = commentMap.get(key.substring(0, pos));
        if (comment != null) {
            // append 
            comment += key.substring(pos);
        }
        return comment;
    }

    public static void updateComment(String key, String comment) {
        commentMap.put(key, comment);
    }

    public static void deleteComment(String key) {
        commentMap.remove(key);
    }
}
