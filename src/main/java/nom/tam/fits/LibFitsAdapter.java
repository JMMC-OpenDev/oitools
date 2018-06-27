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
 * along with this program.  If not, see http://www.gnu.org/licenses/>.
 */
package nom.tam.fits;

import java.io.IOException;
import java.io.OutputStream;
import nom.tam.fits.header.Standard;
import static nom.tam.fits.utilities.FitsCheckSum.checksumEnc;
import nom.tam.util.BufferedDataOutputStream;

/**
 *
 * @author jammetv
 */
public class LibFitsAdapter {

    private final static String TYPE_TUNIT = "TUNIT";
    private final static String TYPE_TDIM = "TDIM";

    /**
     * Get the type of a column in the table.
     *
     * // LAURENT : added method
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The type char representing the FITS type or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public static final char getColumnType(final BinaryTableHDU table, final int index)
            throws FitsException {

        final String tform = table.getColumnFormat(index);
        if (tform != null) {
            return table.getData().getTFORMType(tform);
        }
        return 0;
    }

    /**
     * Get the explicit or implied length of a column in the table.
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The explicit or implied length or 0 if undefined or an invalid index was requested.
     * @exception FitsException if an invalid index was requested.
     */
    public static final int getColumnLength(final BinaryTableHDU table, final int index)
            throws FitsException {

        final String tform = table.getColumnFormat(index);
        if (tform != null) {
            return table.getData().getTFORMLength(tform);
        }
        return 0;
    }

    /**
     * Get the unit of a column in the table.
     *
     * @param table owning the column
     * @param index The 0-based column index.
     * @return The unit of a column or null if undefined or an invalid index was requested.
     */
    public static final String getColumnUnit(final TableHDU table, final int index) {
        return table.getColumnMeta(index, TYPE_TUNIT);
    }

    /*
    * Set the name and unit of a column in the table.
    *
    * @param index The 0-based column index.
    * @param name column name
    * @param comment description of the column
    * @param unit column unit
    * @throws FitsException
     */
    public static void setColumnName(
            final TableHDU table, final int index, final String name, final String comment, final String unit)
            throws FitsException {
        table.setColumnName(index, name, comment);
        if (unit != null && unit.length() > 0) {
            // Insert TUNIT keyword after TFORM
            table.setColumnMeta(index, Standard.TUNITn, unit, "ntf::tablehdu:tunit|" + (index + 1), true);
        }
    }

    /**
     * Add or update the CHECKSUM keyword.
     *
     * @param hdu the HDU to be updated.
     * @param addDataSum true to add DATASUM keyword before computing final checksum (header + data)
     * @return checksum as long value
     * @throws HeaderCardException
     * @author R J Mathar
     * @throws java.io.IOException if error occurs in write operation
     * @since 2005-10-05
     */
    public static long setChecksum(BasicHDU hdu, final boolean addDataSum)
            throws HeaderCardException, FitsException, IOException {
        /* the next line with the delete is needed to avoid some unexpected
         *  problems with non.tam.fits.Header.checkCard() which otherwise says
         *  it expected PCOUNT and found DATE.
         */
        Header hdr = hdu.getHeader();
        hdr.deleteKey("CHECKSUM");

        // LAURENT: DO NOT use date in checksum to be able to validate it (compability issue) ...
        hdr.addValue("CHECKSUM", "0000000000000000", "ntf::fits:checksum:1");

        /* Convert the entire sequence of 2880 byte header cards into a byte array.
         * The main benefit compared to the C implementations is that we do not need to worry
         * about the particular byte order on machines (Linux/VAX/MIPS vs Hp-UX, Sparc...) supposed that
         * the correct implementation is in the write() interface.
         */
        // LBO: New checksum computation using a checksum stream (only ~35K buffer allocated instead of the all byte[] !)
        ChecksumOutputStream cs;

        // DATASUM keyword.
        cs = new ChecksumOutputStream();
        hdu.getData().write(new BufferedDataOutputStream(cs, 12 * 2880));
        final long csd = cs.getChecksum(); // flush and close streams
        if (addDataSum) {
            hdr.addValue("DATASUM", csd, "ntf::fits:datasum:1");
        }

        // We already have the checksum of the data.  Lets compute it for
        // the header.
        cs = new ChecksumOutputStream();
        hdr.write(new BufferedDataOutputStream(cs, 4 * 2880));
        final long csh = cs.getChecksum(); // flush and close streams

        long cshdu = csh + csd;
        // If we had a carry it should go into the
        // beginning.
        while ((cshdu & 0xFFFFFFFF00000000L) != 0) {
            cshdu = (cshdu & 0xFFFFFFFFL) + 1;
        }
        /* This time we do not use a deleteKey() to ensure that the keyword is replaced "in place".
         * Note that the value of the checksum is actually independent to a permutation of the
         * 80-byte records within the header.
         */
        hdr.addValue("CHECKSUM", checksumEnc(cshdu, true), "ntf::fits:checksum:1");
        return cshdu;
    }

    /**
     * Compute the Seaman-Pence 32-bit 1's complement checksum over the byte stream EFFICIENTLY.
     */
    private static final class ChecksumOutputStream extends OutputStream {

        /* members */
        private boolean _open;
        /* partial sums */
        private long _hi;
        private long _lo;
        /* checksum */
        private long _checksum;

        ChecksumOutputStream() {
            reset();
        }

        void reset() {
            _open = true;
            _hi = 0l;
            _lo = 0l;
            _checksum = 0l;
        }

        long getChecksum() {
            /* close if needed to compute checksum */
            close();
            return _checksum;
        }

        @Override
        public void write(final int b) {
            throw new UnsupportedOperationException("Not supported: use write(byte b[], int off, int len).");
        }

        @Override
        public void write(byte data[], int offset, int length) {
            // check parameters:
            if (offset != 0) {
                throw new UnsupportedOperationException("Not supported: offset parameter must be 0.");
            }

            /*
             * Calculate the Seaman-Pence 32-bit 1's complement checksum over the byte stream. The option
             * to start from an intermediate checksum accumulated over another previous
             * byte stream is not implemented.
             * The implementation accumulates in two 64-bit integer values the two low-order and the two
             * high-order bytes of adjacent 4-byte groups. A carry-over of bits is never done within the main
             * loop (only once at the end at reduction to a 32-bit positive integer) since an overflow
             * of a 64-bit value (signed, with maximum at 2^63-1) by summation of 16-bit values could only
             * occur after adding approximately 140G short values (=2^47) (280GBytes) or more. We assume
             * for now that this routine here is never called to swallow FITS files of that size or larger.
             * @param data the byte sequence
             * @return the 32bit checksum in the range from 0 to 2^32-1
             * @see "http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/checksum.html"
             * @author R J Mathar
             * @since 2005-10-05
             */
 /* get current state */
            long hi = _hi;
            long lo = _lo;

            final int len = length >> 1; // 2 * (length / 4)
            // System.out.println(length + " bytes") ;
            final int remain = length % 4;
            /* a write(2) on Sparc/PA-RISC would write the MSB first, on Linux the LSB; by some kind
             * of coincidence, we can stay with the byte order known from the original C version of
             * the algorithm.
             */
            for (int i = 0, j; i < len; i += 2) {
                /* The four bytes in this block handled by a single 'i' are each signed (-128 to 127)
                 * in Java and need to be masked indivdually to avoid sign extension /propagation.
                 */
                j = i << 1;
                hi += (data[j] << 8) & 0xff00L | data[j + 1] & 0xffL;
                lo += (data[j + 2] << 8) & 0xff00L | data[j + 3] & 0xffL;
            }

            /* The following three cases actually cannot happen since FITS records are multiples of 2880 bytes.
             */
            if (remain >= 1) {
                hi += (data[2 * len] << 8) & 0xff00L;
            }
            if (remain >= 2) {
                hi += data[2 * len + 1] & 0xffL;
            }
            if (remain >= 3) {
                lo += (data[2 * len + 2] << 8) & 0xff00L;
            }

            /* update current state */
            _hi = hi;
            _lo = lo;
        }

        @Override
        public void close() {
            if (_open) {
                _open = false;
                /* get current state */
                long hi = _hi;
                long lo = _lo;

                long hicarry = hi >>> 16;
                long locarry = lo >>> 16;
                while (hicarry != 0 || locarry != 0) {
                    hi = (hi & 0xffffL) + locarry;
                    lo = (lo & 0xffffL) + hicarry;
                    hicarry = hi >>> 16;
                    locarry = lo >>> 16;
                }
                _checksum = (hi << 16) + lo;
            }
        }
    }
}
