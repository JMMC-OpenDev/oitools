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
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageWriter;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.Types;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.util.BufferedFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This state-full class writes an OIFits file from the OIFitsFile model
 *
 * @author bourgesl
 */
public class OIFitsWriter {

    /* constants */
    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OIFitsWriter.class.getName());

    static {
        FitsUtils.setup();
    }

    /**
     * Main method to write an OI Fits File
     * @param absFilePath absolute File path on file system (not URL)
     * @param oiFitsFile OIFits data model
     * @throws FitsException if the fits can not be written
     * @throws IOException IO failure
     */
    public static void writeOIFits(final String absFilePath, final OIFitsFile oiFitsFile) throws IOException, FitsException {
        oiFitsFile.setAbsoluteFilePath(absFilePath);

        final OIFitsWriter writer = new OIFitsWriter(oiFitsFile);
        writer.write(absFilePath);
    }
    /* members */
    /** OIFits data model */
    private final OIFitsFile oiFitsFile;

    /**
     * Private constructor
     * @param oiFitsFile OIFits data model
     */
    private OIFitsWriter(final OIFitsFile oiFitsFile) {
        this.oiFitsFile = oiFitsFile;
    }

    /**
     * Write the OI Fits data model into the given file.
     *
     * Note : This method supposes that the OI Fits data model was checked previously
     * i.e. no column is null and values respect the OIFits standard (length, cardinality ...)
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void write(final String absFilePath) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "writing {0}", absFilePath);
        }

        BufferedFile bf = null;
        try {
            final long start = System.nanoTime();

            // create the fits model :
            final Fits fitsFile = new Fits();

            // process all OI_* tables :
            createHDUnits(fitsFile);

            bf = new BufferedFile(absFilePath, "rw");

            // write the fits file :
            fitsFile.write(bf);

            // flush and close :
            bf.close();
            bf = null;

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "write : duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
            }

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to write the file : " + absFilePath, fe);

            throw fe;
        } finally {
            if (bf != null) {
                // flush and close :
                bf.close();
            }
        }
    }

    /**
     * Create all Fits HD units corresponding to OI_* tables, and additional HDU for IMAGE-OI (in first place) if any.
     * Primary HDU Keywords are not -yet- serialized.
     *
     * @param fitsFile fits file
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void createHDUnits(final Fits fitsFile) throws FitsException, IOException {

        // Add Primary HDU
        if (this.oiFitsFile.getPrimaryImageHDU() != null) {
            fitsFile.addHDU(
                    FitsImageWriter.createHDUnit(this.oiFitsFile.getPrimaryImageHDU(),
                            this.oiFitsFile.getFileName(), 0)
            );
        }

        // Add OiTables
        for (OITable oiTable : this.oiFitsFile.getOITableList()) {
            // add HDU to the fits file :
            fitsFile.addHDU(createBinaryTable(oiTable));
        }

        // Add remaining Image HDUs:
        final int nbImageHDus = this.oiFitsFile.getImageHDUCount();
        if (nbImageHDus > 1) {
            int nbHDUs = fitsFile.getNumberOfHDUs();
            // TODO: use extNb to order HDUs and write them in correct order:

            final List<FitsImageHDU> fitsImageHDUs = this.oiFitsFile.getFitsImageHDUs();

            for (int i = 1; i < nbImageHDus; i++) {
                final FitsImageHDU fitsImageHDU = fitsImageHDUs.get(i);
                fitsFile.addHDU(
                        FitsImageWriter.createHDUnit(fitsImageHDU,
                                this.oiFitsFile.getFileName(), nbHDUs)
                );
                nbHDUs++;
            }
        }

        // Add IMAGE-OI table if any
        if (this.oiFitsFile.getExistingImageOiData() != null) {
            createImageOiHDUnits(fitsFile, this.oiFitsFile.getExistingImageOiData());
        }
    }

    /**
     * Create all Fits HD units corresponding to IMAGE-OI content.
     * @param fitsFile fits file
     * @param imageOiData IMAGE-OI data container of images and parameters to write into give fits file.
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void createImageOiHDUnits(final Fits fitsFile, ImageOiData imageOiData) throws FitsException, IOException {
        // Add IMAGE-OI INPUT PARAM table (is binary table, not image)
        fitsFile.addHDU(createBinaryTable(imageOiData.getInputParam()));
        // Add IMAGE-OI OUTPUT PARAM table (is binary table, not image)
        // TODO: check if OutputParam is empty ?
        fitsFile.addHDU(createBinaryTable(imageOiData.getOutputParam()));
    }

    /**
     * Create a binary table HDU using the given OI table
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @return binary table HDU
     */
    private BasicHDU createBinaryTable(final FitsTable table) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "createBinaryTable : {0}", table.toString());
        }

        // Get Column descriptors :
        final Collection<ColumnMeta> columnsDescCollection = table.getColumnDescCollection();
        final int size = columnsDescCollection.size();

        // data list containing column data (not null) :
        final List<Object> dataList = new ArrayList<Object>(size);

        // index map storing the column index keyed by column name :
        final Map<String, Integer> columnIndex = new HashMap<String, Integer>(size);
        // backup of modified String keyed by column name :
        final Map<String, String> backupFirstString = new HashMap<String, String>(size);

        // define both data list and index map :
        int i = 0;
        Integer idx;
        String name;
        Object value;
        String[] values;
        String val;
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();
            value = table.getColumnValue(name);

            if (value != null) {
                // fix string length to have correct header length ('0A' issue) :
                if (column.getDataType() == Types.TYPE_CHAR) {
                    values = (String[]) value;

                    if (values.length > 0) {
                        val = values[0];

                        if (val == null) {
                            val = "";
                        }

                        // backup that string to restore it after HDU creation :
                        backupFirstString.put(name, val);

                        while (val.length() < column.getRepeat()) {
                            val += " ";
                        }

                        // set the first value of a character column to its maximum length :
                        values[0] = val;
                    }
                }

                // add column value in use :
                dataList.add(value);

                // column index corresponds to the position in the data list :
                columnIndex.put(name, NumberUtils.valueOf(i));
                i++;
            }
        }

        // Prepare the binary table to create HDU :
        final Data fitsData = (size == 0) ? new BinaryTable() : new BinaryTable(dataList.toArray());

        // Generate the header from the binary table :
        final Header header = BinaryTableHDU.manufactureHeader(fitsData);

        // create HDU :
        final BinaryTableHDU hdu = new BinaryTableHDU(header, fitsData);

        // Restore first String data :
        for (Map.Entry<String, String> e : backupFirstString.entrySet()) {
            // restore in OI FitsTable the initial string value :
            table.getColumnString(e.getKey())[0] = e.getValue();
        }

        // Finalize Header :
        // Define column information (name, description, unit) :
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();
            idx = columnIndex.get(name);

            // column in use :
            if (idx != null) {
                i = idx.intValue();

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "COLUMN [{0}] [{1} {2}]", new Object[]{name, hdu.getColumnLength(i), hdu.getColumnType(i)});
                }
                hdu.setColumnName(i, name, column.getDescription(), column.getUnits().getStandardRepresentation());
            }
        }

        // Add keywords after column definition in the header :
        FitsImageWriter.processKeywords(header, table);

        // Fix header for Fits complex columns :
        for (ColumnMeta column : columnsDescCollection) {
            if (column.getDataType() == Types.TYPE_COMPLEX) {
                idx = columnIndex.get(column.getName());

                // column in use :
                if (idx != null) {
                    i = idx.intValue();

                    // change the 2D float column to complex type :
                    hdu.setComplexColumn(i);
                }
            }
        }
        return hdu;
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Return the OIFits data model
     * @return OIFits data model
     */
    public OIFitsFile getOIFitsFile() {
        return oiFitsFile;
    }
}
