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
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Checksum computation helper class
 *
 * @author bourgesl
 */
public final class ChecksumHelper {

    /** Default read buffer capacity: 8K */
    private static final int DEFAULT_BUFFER_CAPACITY = 8192;
    /** toHexString characters */
    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private ChecksumHelper() {
        // forbidden
    }

    /**
     * Update the checksum keyword for the given HDU
     * @param hdu hdu to processHDUnit
     * @return checksum value
     * @throws FitsException if any FITS error occurred
     */
    public static long updateChecksum(final BasicHDU hdu) throws FitsException {
        // compute and add checksum into HDU (header):
        return Fits.setChecksum(hdu, false);
    }

    public static String computeMD5(final File file) {
        return computeChecksum(file, "MD5");
    }

    /**
     * Return the checksum for the given file using the given algorithm
     * @param file file to use
     * @param algorithm the name of the algorithm requested
     * @return file checksum
     */
    public static String computeChecksum(final File file, final String algorithm) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException("Unsupported algorithm : " + algorithm, nsae);
        }

        return toHexString(createChecksum(digest, file));
    }

    private static byte[] createChecksum(final MessageDigest digest, final File file) {
        final byte[] buffer = new byte[DEFAULT_BUFFER_CAPACITY];

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_CAPACITY);

            int len;
            while ((len = in.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }

        } catch (IOException ioe) {
            throw new IllegalArgumentException("IO exception occured : ", ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }

        return digest.digest();
    }

    private static String toHexString(final byte[] bytes) {
        final int len = bytes.length;
        final char[] hexChars = new char[len * 2];

        for (int i = 0, v; i < len; i++) {
            v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
