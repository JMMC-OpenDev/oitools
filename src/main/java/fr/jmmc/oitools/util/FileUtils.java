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
package fr.jmmc.oitools.util;

import fr.jmmc.jmcs.network.NetworkSettings;
import fr.jmmc.jmcs.util.StringUtils;
import fr.nom.tam.fits.FitsUtil;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 * This class gathers file utility methods
 * @author bourgesl
 */
public final class FileUtils {

    /* constants */
    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FileUtils.class.getName());
    /** File encoding use UTF-8 */
    public static final String FILE_ENCODING = "UTF-8";
    /** Default read buffer capacity: 8K */
    public static final int DEFAULT_BUFFER_CAPACITY = 8192;

    static {
        NetworkSettings.defineDefaults();
    }

    /**
     * Forbidden constructor
     */
    private FileUtils() {
        super();
    }

    /**
     * Save remote file into a temporary file.
     * This temporary file must be deleted by the caller after use.
     *
     * @param urlLocation remote filename
     * @return absolute file path of the temporary file
     *
     * @throws MalformedURLException invalid URL format
     * @throws IOException IO failure
     */
    public static String download(final String urlLocation) throws MalformedURLException, IOException {
        /* Input parameter is an url */
        final URL url = new URL(urlLocation);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Download URL: {0}", url);
        }

        // Follow up to 5 redirects:
        final InputStream in = FitsUtil.getURLStream(url, 0);

        /* Generating temporary file relative to input parameter */
        final File outFile = File.createTempFile(cleanupFileName(new File(url.getFile()).getName()), null);

        final String absFilePath = outFile.getCanonicalPath();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Creating temp file for {0} with filename={1}", new Object[]{url, absFilePath});
        }

        final OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
        try {
            // Transfer bytes from the compressed file to the output file
            final byte[] buf = new byte[8192]; // 8K
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException ioe) {
            logger.log(Level.INFO, "IO failure while downloading remote file {0}: " + ioe.getMessage(), url);
            // resource cleanup
            deleteTempFile(absFilePath);
        } finally {
            // Close the file and stream
            in.close();
            out.close();
        }
        return absFilePath;
    }

    public static void deleteTempFile(final String absFilePath) {
        new File(absFilePath).delete();
    }

    /**
     * Read a text file from the given file
     *
     * @param file local file
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readFile(final File file) throws IOException {
        return readStream(new FileInputStream(file), (int) file.length());
    }

    /**
     * Read a text file from the given input stream into a string
     *
     * @param inputStream stream to load
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readStream(final InputStream inputStream) throws IOException {
        return readStream(inputStream, DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Read a text file from the given input stream into a string
     *
     * @param inputStream stream to load
     * @param bufferCapacity initial buffer capacity (chars)
     * @return text file content
     *
     * @throws IOException if an I/O exception occurred
     */
    public static String readStream(final InputStream inputStream, final int bufferCapacity) throws IOException {

        String result = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, FILE_ENCODING));

            // Use one string buffer with the best guessed initial capacity:
            final StringBuilder sb = new StringBuilder(bufferCapacity);

            // Use a char buffer to consume reader using DEFAULT_BUFFER_CAPACITY:
            final char[] cbuf = new char[DEFAULT_BUFFER_CAPACITY];

            int len;
            while ((len = reader.read(cbuf)) > 0) {
                sb.append(cbuf, 0, len);
            }

            result = sb.toString();

        } finally {
            closeFile(reader);
        }
        return result;
    }

    /**
     * Write the given string into the given file
     *
     * @param file file to write
     * @param content content to write
     *
     * @throws IOException if an I/O exception occurred
     */
    public static void writeFile(final File file, final String content) throws IOException {
        final Writer w = openFile(file);
        try {
            w.write(content);
        } finally {
            closeFile(w);
        }
    }

    /**
     * Returns a Writer for the given file and use the default writer buffer
     * capacity
     *
     * @param file file to write
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final File file) {
        return openFile(file, DEFAULT_BUFFER_CAPACITY);
    }

    /**
     * Returns a Writer for the given file and use the given buffer capacity
     *
     * @param file file to write
     * @param bufferSize write buffer capacity
     * @return Writer (buffered) or null
     */
    public static Writer openFile(final File file, final int bufferSize) {
        try {
            // Should define UTF-8 encoding for cross platform compatibility
            // but we must stay compatible with existing files (windows vs unix)
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset()), bufferSize);
        } catch (final FileNotFoundException fnfe) {
            logger.log(Level.SEVERE, "IO failure : ", fnfe);
        }

        return null;
    }

    /**
     * Close the given reader
     *
     * @param r reader to close
     *
     * @return null to optionally reset variable (fluent API)
     */
    public static Reader closeFile(final Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (IOException ioe) {
                logger.log(Level.FINE, "IO close failure.", ioe);
            }
        }
        return null;
    }

    /**
     * Close the given writer
     *
     * @param w writer to close
     *
     * @return null to optionally reset variable (fluent API)
     */
    public static Writer closeFile(final Writer w) {
        if (w != null) {
            try {
                w.close();
            } catch (IOException ioe) {
                logger.log(Level.FINE, "IO failure : ", ioe);
            }
        }
        return null;
    }

    /**
     * Remove accents from characters and replace wild chars with '_'.
     * @param fileName the string to clean up
     * @return cleaned up file name
     */
    public static String cleanupFileName(final String fileName) {
        // Remove accent from characters (if any) (Java 1.6)
        final String removed = StringUtils.removeAccents(fileName);

        // Replace wild characters with '_'
        final String cleaned = StringUtils.replaceNonFileNameCharsByUnderscore(removed);

        if (logger.isLoggable(Level.FINE) && !cleaned.equals(fileName)) {
            logger.log(Level.FINE, "Had to clean up file name (was '{0}', became '{1}').",
                    new Object[]{fileName, cleaned});
        }

        return cleaned;
    }
}
