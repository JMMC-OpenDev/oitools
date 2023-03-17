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

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.ChecksumHelper;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.image.FileRef;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageHDUFactory;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.ArrayColumnMeta;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.ImageHDU;
import fr.nom.tam.util.ArrayFuncs;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

/**
 * This state-full class loads an OIFits file to the OIFits data model with optional integrity checks
 * @author bourgesl
 */
public class OIFitsLoader {

    /* constants */
    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OIFitsLoader.class.getName());

    static {
        FitsUtils.setup();
    }

    /* members */
    /** flag to log the checker report */
    private final boolean logCheckerReport;
    /** flag to compute the file checksum */
    private final boolean doChecksum;
    /** checker */
    private final OIFitsChecker checker;
    /** OIFits data model */
    private OIFitsFile oiFitsFile = null;

    /**
     * Main method to load an OI Fits File
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @return OIFits data model
     * @throws MalformedURLException invalid url format
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final String fileLocation) throws MalformedURLException, IOException, FitsException {
        return loadOIFits(null, null, fileLocation, false);
    }

    /**
     * Main method to load an OI Fits File
     * @param std OIFITS standard to force loading in V1 or V2 (null means automatic version)
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @return OIFits data model
     * @throws MalformedURLException invalid url format
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsStandard std, final String fileLocation) throws MalformedURLException, IOException, FitsException {
        return loadOIFits(std, null, fileLocation, false);
    }

    /**
     * Main method to load an OI Fits File with the given checker component
     * @param checker checker component
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @return OIFits data model
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsChecker checker, final String fileLocation) throws IOException, FitsException {
        return loadOIFits(null, checker, fileLocation, false);
    }

    /**
     * Main method to load an OI Fits File with the given checker component
     * @param std OIFITS standard to force loading in V1 or V2 (null means automatic version)
     * @param checker checker component
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @return OIFits data model
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsStandard std, final OIFitsChecker checker, final String fileLocation) throws IOException, FitsException {
        return loadOIFits(std, checker, fileLocation, false);
    }

    /**
     * Main method to load an OI Fits File with the given checker component
     * @param checker checker component
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @param doChecksum true to compute the file checksum
     * @return OIFits data model
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsChecker checker, final String fileLocation, final boolean doChecksum) throws IOException, FitsException {
        return loadOIFits(null, checker, fileLocation, doChecksum);
    }

    /**
     * Main method to load an OI Fits File with the given checker component
     * @param std OIFITS standard to force loading in V1 or V2 (null means automatic version)
     * @param checker checker component
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @param doChecksum true to compute the file checksum
     * @return OIFits data model
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsStandard std, final OIFitsChecker checker,
                                        final String fileLocation, final boolean doChecksum) throws IOException, FitsException {
        boolean remote = false;
        final String absFilePath;
        final URI fileURI;

        // If the given file is an URL:
        if (fileLocation.contains(":/")) {
            try {
                fileURI = new URI(fileLocation);
            } catch (URISyntaxException use) {
                throw new IOException("Can not parse the file location: " + fileLocation, use);
            }
            if (fileURI.getScheme().equalsIgnoreCase("file")) {             
                // use getPath() to remove the fragment part if any
                absFilePath = new File(fileURI.getPath()).getAbsolutePath();
            } else {
                // Only remote files are retrieved:
                remote = true;

                // Store in a temporary file because the Fits library does not manage remote file.
                absFilePath = FileUtils.download(fileLocation);
            }
            if (absFilePath == null) {
                throw new IOException("Can not read the file: " + fileLocation);
            }
        } else {
            absFilePath = fileLocation;
            fileURI = null;
        }

        final OIFitsLoader loader = new OIFitsLoader(checker, doChecksum);
        try {
            loader.load(std, absFilePath);
        } finally {
            // resource cleanup
            if (remote) {
                FileUtils.deleteTempFile(absFilePath);
            }
        }

        final OIFitsFile oiFitsFile = loader.getOIFitsFile();
        if (remote) {
            // store the URL instead of the temporary file location:
            oiFitsFile.setSourceURI(fileURI);
        }
        return oiFitsFile;
    }

    /**
     * Custom constructor to give a checker instance (multiple file load / validation)
     * @param checker checker component
     * @param doChecksum flag to compute the file checksum
     */
    private OIFitsLoader(final OIFitsChecker checker, final boolean doChecksum) {
        super();
        if (checker != null) {
            this.checker = checker;
            this.logCheckerReport = false;
        } else {
            this.checker = OIFitsChecker.newInstance();
            this.logCheckerReport = (this.checker != null);
        }
        this.doChecksum = doChecksum;
    }

    /**
     * Create oiFitsFile if is not defined before
     * @param version  OIFITS version
     * @param fileRef file reference
     */
    private void createOIFitsIfNeeded(final OIFitsStandard version, final FileRef fileRef) {
        if (this.oiFitsFile == null) {
            this.oiFitsFile = new OIFitsFile(version, fileRef);
        }
    }

    /**
     * Load the given file into the OI Fits data model
     *
     * @param std OIFITS standard to force loading in V1 or V2 (null means automatic version)
     * @param absFilePath absolute File path on file system (not URL)
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void load(final OIFitsStandard std, final String absFilePath) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "loading {0}", absFilePath);
        }
        try {
            final FileRef fileRef = new FileRef(absFilePath);
            // Initialize FileRef in OIFitsChecker
            if (checker != null) {
                checker.setFileRef(fileRef, null);
            }

            // Check if the given file exists:
            final File file = new File(absFilePath);
            final boolean missing = !file.exists();

            if (missing || OIFitsChecker.isInspectRules()) {
                // rule [FILE_EXIST] check if the file exist
                if (checker != null) {
                    checker.ruleFailed(Rule.FILE_EXIST);
                }

                if (missing) {
                    throw new IOException("File not found: " + absFilePath);
                }
            }
            logger.log(Level.INFO, "Loading File: {0}", absFilePath);

            final long fileSize = file.length();

            Fits fitsFile = null;
            try {
                final long start = System.nanoTime();

                // open the fits file:
                fitsFile = new Fits(absFilePath);

                // read the complete file structure:
                // TODO: unify the readHDU with FitsImageLoader
                final BasicHDU[] hdus = fitsFile.read();

                // processHDUnit all HD units:
                // note: not null means hdus array is not empty!
                if (hdus != null) {
                    // note: oiFitsFile will be defined by createOIFitsIfNeeded()
                    this.processHDUnits(std, fileRef, hdus);
                }

                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "load: duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
                }

            } catch (FitsException fe) {
                logger.log(Level.SEVERE, "Unable to load the file: " + absFilePath, fe);
                // rule [FILE_LOAD] check if the OIFITS file is loaded properly (IO error)
                if (checker != null) {
                    checker.ruleFailed(Rule.FILE_LOAD);
                }
                throw fe;
            } finally {
                if (fitsFile != null && fitsFile.getStream() != null) {
                    try {
                        fitsFile.getStream().close();
                    } catch (IOException ioe) {
                        logger.log(Level.FINE, "Closing Fits file", ioe);
                    }
                }
            }

            // note: oiFitsFile may be null if processHDUnits() not called
            if (this.oiFitsFile == null || this.oiFitsFile.getNbOiTables() == 0) {
                throw new FitsException("Invalid OIFits format (no OI table found) !");
            }

            // Update file properties:
            this.oiFitsFile.setSize(fileSize);

            if (this.doChecksum) {
                final String md5sum = ChecksumHelper.computeMD5(file);
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "md5sum = {0}", md5sum);
                }
                this.oiFitsFile.setMd5sum(md5sum);
            }

            // Always perform validation:
            // ENABLE before second OIFits validation
            if (checker != null) {
                checker.setSkipFormat(true);
            }

            this.oiFitsFile.check(this.checker);

            if (this.logCheckerReport && logger.isLoggable(Level.INFO)) {
                // log validation results
                logger.log(Level.INFO, "validation results\n{0}", this.checker.getCheckReport());
            }
        } finally {
            if (checker != null) {
                checker.cleanup();
            }
        }
    }

    /**
     * Process all Fits HD units to load OI_* tables (skip other tables)
     * and check at least one data table is present.
     * @param std OIFITS standard version 1 or version 2
     * @param fileRef file reference
     * @param hdus array of hd unit
     * @throws FitsException if any FITS error occurred
     * @throws java.io.IOException
     * @throws IllegalArgumentException if IMAGE-OI images HDU have unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    private void processHDUnits(final OIFitsStandard std, final FileRef fileRef, final BasicHDU[] hdus) throws FitsException, IOException, IllegalArgumentException {

        final int nbHDU = hdus.length;

        // flag indicating that the HDU was processed:
        final boolean[] processed = new boolean[nbHDU];

        // processHDUnit the primary HDU to load all keywords (ESO HIERARCH ...)
        if (nbHDU != 0) {
            for (int i = 0; i < nbHDU; i++) {
                if (hdus[i] instanceof ImageHDU) {
                    final ImageHDU imgHdu = (ImageHDU) hdus[i];

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "FITS IMAGE ImageHDU#{0}:\n{1}",
                                new Object[]{i, FitsUtils.dumpHDU(imgHdu, false)});
                    }

                    logger.log(Level.INFO, "Analysing image [ImageHDU#{0}]:", i);

                    final FitsImageHDUFactory factory;

                    if (i == 0) {
                        //content keyword for OIFitsV2 information
                        final Header header = imgHdu.getHeader();

                        final String content = header.getTrimmedStringValue(FitsConstants.KEYWORD_CONTENT);

                        final OIFitsStandard version = (std != null) ? std : OIFitsFile.getOIFitsStandard(content);

                        factory = (version == OIFitsStandard.VERSION_2) ? OIPrimaryHDU.DEFAULT_FACTORY : FitsImageHDUFactory.DEFAULT_FACTORY;

                        createOIFitsIfNeeded(version, fileRef);
                    } else {
                        factory = FitsImageHDUFactory.DEFAULT_FACTORY;
                    }

                    // Create Image HDU:
                    final FitsImageHDU imageHDU = FitsImageLoader.processHDUnit(checker, this.oiFitsFile.getFileName(), imgHdu, false, i, factory);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "ImageHDU#{0}:\n{1}", new Object[]{i, imageHDU});
                    }

                    // primaryHDU is implictely defined as the first FitsImageHDU in the FitsImageFile.fitsImageHDUs list
                    this.oiFitsFile.getFitsImageHDUs().add(imageHDU);

                    processed[i] = true;
                }
            }
        }

        createOIFitsIfNeeded(OIFitsStandard.VERSION_1, fileRef);

        // Building processHDUnit will be done in 2 steps
        // First loop: build and check presence of
        // OI_TARGET, OI_ARRAY, OI_WAVELENGTH, OI_CORR, OI_INSPOL tables
        // ie reference tables
        OITable oiTable;
        String extName, tableId;
        BasicHDU hdu;
        BinaryTableHDU bh;

        // TODO: load OI_INSPOL tables after OI_WAVELENGTH
        // skip Primary HDU: so start at index = 1
        for (int i = 1; i < nbHDU; i++) {
            hdu = hdus[i];

            if (hdu instanceof ImageHDU) {
                // probably handled below by FitsImageLoader
            } else {
                extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

                if (hdu instanceof BinaryTableHDU) {
                    oiTable = null;
                    bh = (BinaryTableHDU) hdu;

                    if (OIFitsConstants.TABLE_OI_TARGET.equals(extName)) {
                        oiTable = new OITarget(this.oiFitsFile);
                    } else if (OIFitsConstants.TABLE_OI_ARRAY.equals(extName)) {
                        oiTable = new OIArray(this.oiFitsFile);
                    } else if (OIFitsConstants.TABLE_OI_WAVELENGTH.equals(extName)) {
                        oiTable = new OIWavelength(this.oiFitsFile);
                    } else if (OIFitsConstants.TABLE_OI_CORR.equals(extName)) {
                        oiTable = new OICorr(this.oiFitsFile);
                    } else if (OIFitsConstants.TABLE_OI_INSPOL.equals(extName)) {
                        oiTable = new OIInspol(this.oiFitsFile);
                    }

                    if (oiTable != null) {
                        // flag HDU:
                        processed[i] = true;

                        // define the extension number:
                        oiTable.setExtNb(i);

                        tableId = FitsHDU.getHDUId(extName, i);
                        logger.log(Level.INFO, "Analysing table [{0}]:", tableId);

                        // if we are on V1
                        if (!oiFitsFile.isOIFits2()) {
                            // rule [OIFITS_TABLE_NOT_V2] check if any OIFITS 2 specific table (OI_CORR, OI_INSPOL or OI_FLUX) is present in an OIFITS 1 file
                            // TODO: how to ensure this rule is listed in applyTo()
                            if (oiTable instanceof OICorr || oiTable instanceof OIInspol) {
                                if (checker != null) {
                                    checker.inspectRuleFailed(Rule.OIFITS_TABLE_NOT_V2, oiTable.getExtName(), OIFitsStandard.VERSION_1);
                                }
                                if (checker != null) {
                                    checker.ruleFailed(Rule.OIFITS_TABLE_NOT_V2, oiTable);
                                }
                            }
                        }

                        // load table:
                        this.processTable(bh, oiTable);

                        // register the table:
                        this.oiFitsFile.registerOiTable(oiTable);
                    }
                }
            }
        }

        // Second loop: build and check presence of
        // OI_VIS, OI_VIS2, OI_T3 tables
        boolean hasDataHdu = false;

        // skip Primary HDU: so start at index = 1
        for (int i = 1; i < nbHDU; i++) {
            hdu = hdus[i];

            if (hdu instanceof BinaryTableHDU) {
                oiTable = null;
                bh = (BinaryTableHDU) hdu;
                extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

                if (OIFitsConstants.TABLE_OI_VIS.equals(extName)) {
                    oiTable = new OIVis(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_VIS2.equals(extName)) {
                    oiTable = new OIVis2(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_T3.equals(extName)) {
                    oiTable = new OIT3(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_FLUX.equals(extName)) {
                    oiTable = new OIFlux(this.oiFitsFile);
                }
                if (oiTable != null) {
                    // flag HDU:
                    processed[i] = true;

                    // define the extension number:
                    oiTable.setExtNb(i);

                    tableId = FitsHDU.getHDUId(extName, i);
                    logger.log(Level.INFO, "Analysing table [{0}]:", tableId);

                    // if we are on V1
                    if (!oiFitsFile.isOIFits2()) {
                        // rule [OIFITS_TABLE_NOT_V2] check if any OIFITS 2 specific table (OI_CORR, OI_INSPOL or OI_FLUX) is present in an OIFITS 1 file
                        // TODO: how to ensure this rule is listed in applyTo()
                        if (oiTable instanceof OIFlux) {
                            if (checker != null) {
                                checker.inspectRuleFailed(Rule.OIFITS_TABLE_NOT_V2, oiTable.getExtName(), OIFitsStandard.VERSION_1);
                            }
                            if (checker != null) {
                                checker.ruleFailed(Rule.OIFITS_TABLE_NOT_V2, oiTable);
                            }

                        }
                    }

                    // load table:
                    this.processTable(bh, oiTable);

                    this.oiFitsFile.registerOiTable(oiTable);

                    hasDataHdu = true;
                }
            }
        }

        // try to load IMAGE-OI data
        final ImageOiData imageOiData = new ImageOiData();

        // report any non-standard OIFits binary tables:
        for (int i = 0; i < nbHDU; i++) {
            if (!processed[i] || OIFitsChecker.isInspectRules()) {
                hdu = hdus[i];

                extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

                // Register IMAGE_OI PARAM tables
                if (extName != null) {
                    if (ImageOiConstants.EXTNAME_IMAGE_OI_INPUT_PARAM.equals(extName)) {
                        // load ONLY keywords from binary table:
                        FitsImageLoader.processKeywords(checker, hdu.getHeader(), imageOiData.getInputParam());

                        // flag HDU:
                        processed[i] = true;

                        // Store reference into oifitsFile anyway
                        this.oiFitsFile.setImageOiData(imageOiData);
                    } else if (ImageOiConstants.EXTNAME_IMAGE_OI_OUTPUT_PARAM.equals(extName)) {
                        // load ONLY keywords from binary table:
                        FitsImageLoader.processKeywords(checker, hdu.getHeader(), imageOiData.getOutputParam());

                        // flag HDU:
                        processed[i] = true;

                        // Store reference into oifitsFile anyway
                        this.oiFitsFile.setImageOiData(imageOiData);
                    }
                }

                if (!processed[i] || OIFitsChecker.isInspectRules()) {
                    // rule [UNKNOWN_TABLE] check if the table belongs to the OIFITS standard and version
                    if (checker != null) {
                        checker.inspectRuleFailed(Rule.UNKNOWN_TABLE, OIFitsChecker.FILE_RULE, oiFitsFile.getVersion());
                    }

                    // TODO: other header-data units may appear in the file, provided their EXTNAMEs do not begin with “OI_”.
                    // TODO: special case:
                    if (checker != null) {
                        checker.ruleFailed(Rule.UNKNOWN_TABLE, extName, i);
                    }

                    // TODO load and handle any other FITS binary table or HDU...
                    if (logger.isLoggable(Level.INFO)) {
                        // Dump HDU header and table columns:
                        logger.log(Level.INFO, "Skipping non-standard OIFITS {0}:\n{1}",
                                new Object[]{FitsHDU.getHDUId(extName, i), FitsUtils.dumpHDU(hdu, false)});
                    }
                }
            }
        }

        // Add a severe error without any OI_ table
        if (!hasDataHdu || OIFitsChecker.isInspectRules()) {
            // rule [OIFITS_OIDATA] check if at least one data table exists in the OIFITS file
            if (checker != null) {
                checker.ruleFailed(Rule.OIFITS_OIDATA);
            }
        }
    }

    /**
     * Process a given Fits binary table to fill the given OI table object with keyword and column data
     * @param hdu binary table HDU
     * @param table OITable object
     * @throws FitsException if any FITS error occurred
     */
    private void processTable(final BinaryTableHDU hdu, final OITable table) throws FitsException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "processTable: {0}", table.getClass().getName());
        }

        // get Fits table header:
        FitsImageLoader.processKeywords(checker, hdu.getHeader(), table);

        this.processData(hdu, table);
    }

    /**
     * Process the binary table to get columns used by the OITable (see column descriptors)
     * and check missing keywords and their formats
     * @param hdu binary table
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     */
    private void processData(final BinaryTableHDU hdu, final OITable table) throws FitsException {
        final int nbRows = hdu.getNRows();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "ROWS = {0}", nbRows);
        }

        // Get Column descriptors:
        final Collection<ColumnMeta> columnsDescCollection = table.getColumnDescCollection();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("table columns:");
            for (ColumnMeta column : columnsDescCollection) {
                logger.finest(column.toString());
            }
        }

        // keep only OIFits columns i.e. extra column(s) are skipped:
        int idx;
        String name;
        Object value;
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();
            value = null;

            idx = hdu.findColumn(name);

            if (idx == -1 || OIFitsChecker.isInspectRules()) {
                if (!column.isOptional()) {
                    /* No column with columnName name */
                    // rule [GENERIC_COL_MANDATORY] check if the required column is present
                    if (checker != null) {
                        checker.ruleFailed(Rule.GENERIC_COL_MANDATORY, table, name);
                    }
                }
            }

            // lets continue reading with alias support if any
            if ((idx == -1) && (column.getAlias() != null)) {
                idx = hdu.findColumn(column.getAlias());
            }

            if (idx != -1 || OIFitsChecker.isInspectRules()) {

                final char type;
                final int length;
                final String unit;
                final Object values;

                if (OIFitsChecker.isInspectRules() && idx == -1) {
                    type = Types.TYPE_CHAR.getRepresentation();
                    length = 1;
                    unit = "TUNIT";
                    values = null;
                } else {
                    type = hdu.getColumnType(idx);
                    length = hdu.getColumnLength(idx);
                    unit = hdu.getColumnUnit(idx);
                    values = hdu.getColumn(idx);
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "COLUMN [{0}] [{1} {2}]",
                            new Object[]{name, length, type});
                }

                // read all data and convert them to arrays[][]:
                // parse column value:
                value = parseColumn(table, column, type, length, unit, values);
            }

            // Fix undefined columns:
            if ((value == null) && !column.isOptional()) {
                // create a new empty column according to its column description:
                value = table.createColumnArray(column, nbRows);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "UNDEFINED COLUMN [{0}] value:{1}",
                            new Object[]{name, FitsUtils.arrayToString(value)});
                }
            }

            // store key and value:
            if (value != null) {
                table.setColumnValue(name, value);
            }
        }

        // Report any EXTRA column:
        final Map<String, ColumnMeta> columnsDesc = table.getColumnsDesc();
        final Map<String, String> columnsAliases = table.getColumnsAliases();

        for (int i = 0, nCols = hdu.getNCols(); i < nCols; i++) {
            name = hdu.getColumnName(i);

            // Separation of the displays for the collection of the Rules from that of the Failures 
            // (we do not wish to have all the details of unknown column during the collection of the rules)
            if (OIFitsChecker.isInspectRules()) {
                final DataLocation data = checker.ruleFailed(Rule.UNKNOWN_COLUMN, table, "[[UNKNOWN]]");
                if (data.isEmpty()) {
                    data.addKeywordValue("[FORMAT]");
                }
            }
            if (!columnsDesc.containsKey(name) && !columnsAliases.containsKey(name)) {
                // rule [UNKNOWN_COLUMN] check if the column belongs to the OIFITS standard and version
                if (checker != null) {
                    checker.ruleFailed(Rule.UNKNOWN_COLUMN, table, name).addKeywordValue(hdu.getColumnLength(i) + "" + hdu.getColumnType(i));
                }
            }
        }
    }

    /**
     * Parse the column value and check its format (data type, repeat, units)
     * @param table Fits table
     * @param column column descriptor
     * @param columnType fits column type
     * @param columnRepeat fits column repeat (cardinality)
     * @param columnUnit fits column unit
     * @param columnValue column raw value
     * @return converted column value or null
     */
    private Object parseColumn(final OITable table, final ColumnMeta column, final char columnType,
                               final int columnRepeat, final String columnUnit, final Object columnValue) {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "parseColumn: {0} = {1}",
                    new Object[]{column.getName(), ArrayFuncs.arrayDescription(columnValue)});
        }

        // final column value:
        Object value = columnValue;

        // Check type and cardinality
        if (column.checkColumnFormat(checker, table, columnType, columnRepeat)) {
            // incompatible array size = ignore totally values:
            value = null;
        }

        // Check unit
        final Units parsedUnit = Units.parseUnit(columnUnit);

        if (column.getUnits() != parsedUnit || OIFitsChecker.isInspectRules()) {
            if (((parsedUnit == Units.NO_UNIT) && (!column.isCustomUnits() || column.getCustomUnits().isRequired())) || OIFitsChecker.isInspectRules()) {
                // rule [GENERIC_COL_UNIT_EXIST] check if the column unit exist
                if (checker != null) {
                    checker.ruleFailed(Rule.GENERIC_COL_UNIT_EXIST, table, column.getName()).addKeywordValue(column.getUnit());
                }
            }
            if ((parsedUnit != Units.NO_UNIT) || OIFitsChecker.isInspectRules()) {
                // Parse the user unit into CustomUnits
                if (column.isCustomUnits()) {
                    column.getCustomUnits().setRepresentation((parsedUnit != null) ? parsedUnit.getStandardRepresentation() : columnUnit);
                }
                if (!column.isCustomUnits() || OIFitsChecker.isInspectRules()) {
                    // rule [GENERIC_COL_UNIT] check if the column unit matches the expected unit
                    if (checker != null) {
                        checker.ruleFailed(Rule.GENERIC_COL_UNIT, table, column.getName()).addKeywordValue(columnUnit, column.getUnit());
                    }
                }
            }
        }

        if (value == null) {
            // fast fail:
            return null;
        }
        // convert fits data type to expected data model type:
        if (columnType != column.getType()) {
            switch (column.getDataType()) {
                case TYPE_INT:
                    value = ArrayFuncs.convertArray(value, int.class, true);
                    break;

                case TYPE_SHORT:
                    value = ArrayFuncs.convertArray(value, short.class, true);
                    break;

                case TYPE_DBL:
                    value = ArrayFuncs.convertArray(value, double.class, true);
                    break;

                case TYPE_REAL:
                    value = ArrayFuncs.convertArray(value, float.class, true);
                    break;

                case TYPE_COMPLEX:
                    // Special case for complex visibilities:
                    value = ArrayFuncs.convertArray(value, float.class, true);
                    break;

                case TYPE_LOGICAL:
                    // Try to convert what is convertible else false are returned
                    value = ArrayFuncs.convertArray(value, boolean.class, true);
                    break;

                case TYPE_CHAR:
                // only numeric types
                default:
                    // incompatible types = ignore totally values:
                    value = null;
            }
        }

        if (value == null) {
            // fast fail:
            return null;
        }

        if ((column instanceof ArrayColumnMeta) && (columnRepeat == 1)) {
            // Note: If TDIM keyword is present, the value is already a 2D array except for complex column.

            final int[] dims = ArrayFuncs.getDimensions(value);

            // Check the dimensions of the converted value:
            if (dims != null) {
                if ((column.getDataType() == Types.TYPE_COMPLEX) && (dims.length == 2)) {
                    // Special case: NWave = 1 means that Fits gives 2D arrays instead of 3D arrays:
                    // Applies to Complex types:
                    final Object flattened = ArrayFuncs.flatten(value);
                    final int[] newDims = {dims[0], 1, 2};
                    value = ArrayFuncs.curl(flattened, newDims);
                } else if (dims.length == 1) {
                    // Special case: NWave = 1 means that Fits gives 1D arrays instead of 2D arrays:
                    // Applies to Double and Logical types:
                    final int[] newDims = {Array.getLength(columnValue), 1};
                    value = ArrayFuncs.curl(value, newDims);
                }
            }
        }

        return value;
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
