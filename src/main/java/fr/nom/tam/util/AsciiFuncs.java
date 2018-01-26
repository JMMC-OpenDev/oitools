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
package fr.nom.tam.util;

import java.io.UnsupportedEncodingException;

/**
 * This class provides conversions to ASCII strings without breaking
 * compatibility with Java 1.5.
 * @author tmcglynn
 */
public class AsciiFuncs {

    public final static String ASCII = "US-ASCII";

    /** Convert to ASCII or return null if not compatible */
    public static String asciiString(byte[] buf) {
        return asciiString(buf, 0, buf.length);
    }

    /** Convert to ASCII or return null if not compatible */
    public static String asciiString(byte[] buf, int start, int len) {
        try {
            return new String(buf, start, len, ASCII);
        } catch (java.io.UnsupportedEncodingException e) {
            // Shouldn't happen
            System.err.println("AsciiFuncs.asciiString error finding ASCII encoding");
            return null;
        }
    }

    /** Convert an ASCII string to bytes */
    public static byte[] getBytes(String in) {
        try {
            return in.getBytes(ASCII);
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Unable to find ASCII encoding");
            return null;
        }
    }
}
