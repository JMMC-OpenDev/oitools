/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import fr.jmmc.oitools.util.FileUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Class used to get resources from inside JAR files.
 * 
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public abstract class ResourceUtils {

    /* constants */
    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ResourceUtils.class.getName());

    /**
     * Read a text file from the current class loader into a string
     *
     * @param classpathLocation file name like fr/jmmc/aspro/fileName.ext
     * @return text file content
     *
     * @throws IllegalStateException if the file is not found or an I/O
     * exception occurred
     */
    public static String readResource(final String classpathLocation) throws IllegalStateException {
        final URL url = getResource(classpathLocation);
        try {
            return FileUtils.readStream(new BufferedInputStream(url.openStream()), FileUtils.DEFAULT_BUFFER_CAPACITY);
        } catch (IOException ioe) {
            throw new IllegalStateException("unable to read file : " + classpathLocation, ioe);
        }
    }

    /**
     * Find a file in the current classloader (application class Loader)
     * Accepts filename like fr/jmmc/aspro/fileName.ext
     *
     * @param classpathLocation file name like fr/jmmc/aspro/fileName.ext
     * @return URL to the file or null
     *
     * @throws IllegalStateException if the file is not found
     */
    public static URL getResource(final String classpathLocation) throws IllegalStateException {
        logger.log(Level.FINE, "getResource : {0}", classpathLocation);
        if (classpathLocation == null) {
            throw new IllegalStateException("Invalid 'null' value for classpathLocation.");
        }
        final String fixedPath;
        if (classpathLocation.startsWith("/")) {
            fixedPath = classpathLocation.substring(1);
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "getResource : Given classpath had to be fixed : {0} to {1}", new Object[]{classpathLocation, fixedPath});
            }
        } else {
            fixedPath = classpathLocation;
        }
        final URL url = ResourceUtils.class.getClassLoader().getResource(fixedPath);
        if (url == null) {
            throw new IllegalStateException("Unable to find the file in the classpath : " + fixedPath);
        }
        return url;
    }

    /**
     * Private constructor
     */
    private ResourceUtils() {
        super();
    }
}
/*___oOo___*/
