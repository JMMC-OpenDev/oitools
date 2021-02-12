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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Date;

/**
 * This class is copied from Jmcs (same package) in order to let OITools compile properly 
 * but at runtime only one implementation will be loaded (by class loader)
 * 
 * Note: Jmcs Changes must be reported here to avoid runtime issues !
 * 
 * 
 * Helper methods related to Formatter (NumberFormat, DateFormat ...)
 *
 * @author Laurent BOURGES.
 */
public final class FormatterUtils {

    /** formatter string buffer argument */
    private final static StringBuffer _fmtBuffer = new StringBuffer(32);
    /** ignore formatter position argument */
    private final static FieldPosition _ignorePosition = new FieldPosition(0);

    /**
     * Private constructor
     */
    private FormatterUtils() {
        super();
    }

    /* NumberFormat */
    /**
     * Format the given double value using given formater
     * 
     * Note: this method is not thread safe (synchronization must be performed by callers)
     * 
     * @param fmt formatter to use
     * @param val double value
     * @return formatted value
     */
    public static String format(final NumberFormat fmt, final double val) {
        // reset shared buffer:
        _fmtBuffer.setLength(0);

        return format(fmt, _fmtBuffer, val).toString();
    }

    /**
     * Format the given double value using given formater and append into the given string buffer
     * 
     * Note: this method is thread safe
     * 
     * @param fmt formatter to use
     * @param sb string buffer to append to
     * @param val double value
     * @return formatted value
     */
    public static StringBuffer format(final NumberFormat fmt, final StringBuffer sb, final double val) {
        return fmt.format(val, sb, _ignorePosition);
    }

    /* DateFormat */
    /**
     * Format the given date using given formater
     * 
     * Note: this method is not thread safe (synchronization must be performed by callers)
     * 
     * @param fmt formatter to use
     * @param val date
     * @return formatted value
     */
    public static String format(final DateFormat fmt, final Date val) {
        // reset shared buffer:
        _fmtBuffer.setLength(0);

        return format(fmt, _fmtBuffer, val).toString();
    }

    /**
     * Format the given date using given formater and append into the given string buffer
     * 
     * Note: this method is thread safe
     * 
     * @param fmt formatter to use
     * @param sb string buffer to append to
     * @param val date
     * @return formatted value
     */
    public static StringBuffer format(final DateFormat fmt, final StringBuffer sb, final Date val) {
        return fmt.format(val, sb, _ignorePosition);
    }
}
