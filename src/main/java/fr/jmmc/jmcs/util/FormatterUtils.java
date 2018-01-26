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
 * Helper methods related to Formatter (NumberFormat, DateFormat ...)
 * @author bourgesl
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
