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
import fr.jmmc.oitools.fits.FitsConstants;
import static fr.jmmc.oitools.fits.FitsHDU.UNDEFINED_EXT_NB;
import fr.jmmc.oitools.image.FileRef;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.OIFitsStandard;
import static fr.jmmc.oitools.model.OIFitsChecker.isInspectRules;
import fr.jmmc.oitools.model.range.Range;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class represents the data model of an OIFits standard file.
 */
public final class OIFitsFile extends FitsImageFile {

    /* members */
    /** OIFITS standard version */
    private final OIFitsStandard version;
    /** Missing references kept to avoid repeated warnings */
    private final Set<String> missingTableNames = new HashSet<String>();

    /** source URI */
    private URI sourceURI = null;
    /** file size in bytes */
    private long fileSize = 0;
    /** (optional) MD5 sum of the file */
    private String md5sum = null;
    /** Hash table connecting each ARRNAME keyword value with associated OI_ARRAY table */
    private final Map<String, List<OIArray>> arrNameToOiArray = new HashMap<String, List<OIArray>>();
    /** Hash table connecting each INSNAME keyword value with associated OI_WAVELENGTH table */
    private final Map<String, List<OIWavelength>> insNameToOiWavelength = new HashMap<String, List<OIWavelength>>();
    /** Hash table connecting each CORRNAME keyword value with associated OI_CORR table */
    private final Map<String, List<OICorr>> corrNameToOiCorr = new HashMap<String, List<OICorr>>();
    /** (optional) ImageOi data */
    private ImageOiData imageOiData = null;

    /* OIFits structure */
    /** Storage of oi table references */
    private final List<OITable> oiTables = new LinkedList<OITable>();

    /* meta data */
    /** List storing OI_TARGET table */
    private final List<OITarget> oiTargets = new LinkedList<OITarget>();
    /** List storing OI_ARRAY table */
    private final List<OIArray> oiArrays = new LinkedList<OIArray>();
    /** List storing OI_WAVELENGTH table */
    private final List<OIWavelength> oiWavelengths = new LinkedList<OIWavelength>();
    /** List storing OI_CORR table references */
    private final List<OICorr> oiCorrs = new LinkedList<OICorr>();
    /** Storage of OI_INSPOL table references */
    private final List<OIInspol> oiInspols = new LinkedList<OIInspol>();

    /* data tables */
    /** Storage of all OI data table references */
    private final List<OIData> oiDataTables = new LinkedList<OIData>();
    /** Storage of OI_VIS table references */
    private final List<OIVis> oiVisTables = new LinkedList<OIVis>();
    /** Storage of OI_VIS2 table references */
    private final List<OIVis2> oiVis2Tables = new LinkedList<OIVis2>();
    /** Storage of OI_T3 table references */
    private final List<OIT3> oiT3Tables = new LinkedList<OIT3>();
    /** Storage of OI_FLUX table references */
    private final List<OIFlux> oiFluxTables = new LinkedList<OIFlux>();
    /* cached analyzed data */
    /** Store wavelength range of oifits file */
    private Range wavelengthRange = null;
    /** Distinct Granules */
    private final Map<Granule, Granule> distinctGranules = new HashMap<Granule, Granule>();
    /** Set of OIData tables keyed by Granule */
    private final Map<Granule, Set<OIData>> oiDataPerGranule = new HashMap<Granule, Set<OIData>>();
    /** Map of used staNames to StaNamesDir (reference StaNames / orientation) */
    private final Map<String, StaNamesDir> usedStaNamesMap = new LinkedHashMap<String, StaNamesDir>();

    /**
     * Public constructor
     * @param version OIFITS version
     */
    public OIFitsFile(final OIFitsStandard version) {
        super();
        this.version = version;
    }

    /**
     * Public constructor
     * @param version OIFITS version
     * @param fileRef file reference
     */
    public OIFitsFile(final OIFitsStandard version, final FileRef fileRef) {
        super(fileRef);
        this.version = version;
    }

    /**
     * Public OIFitsFile class constructor to copy the given file (structure only).
     * @param src file to copy
     */
    public OIFitsFile(final OIFitsFile src) {
        this(src.getVersion(), new FileRef(src.getAbsoluteFilePath()));

        this.copyFile(src);
    }

    /**
     * Copy method for the given file, shallow copy image hdus and tables
     * @param src OIFitsFile to copy
     */
    public void copyFile(final OIFitsFile src) {
        // Copy all FitsImageHDUs:
        super.copyFile(src);

        // copy OITables:
        src.getOITableList().forEach(sourceOiTable -> {
            // add a copy of the OITable with this oiFitsFile as parent
            addOiTable(this.copyTable(sourceOiTable));
        });

        // Copy IMAGE-OI tables:
        if (src.getExistingImageOiData() != null) {
            this.imageOiData = new ImageOiData(src.getExistingImageOiData());
        }

        // ignore cached data: use analyze() if needed
    }

    /**
     * Copy the given OITable (shallow copy) with this oiFitsFile as parent
     * @param oiTable table to copy
     * @return copied OITable instance
     */
    public OITable copyTable(final OITable oiTable) {
        if (oiTable instanceof OITarget) {
            return new OITarget(this, (OITarget) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            return new OIWavelength(this, (OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            return new OIArray(this, (OIArray) oiTable);
        } else if (oiTable instanceof OICorr) {
            return new OICorr(this, (OICorr) oiTable);
        } else if (oiTable instanceof OIInspol) {
            return new OIInspol(this, (OIInspol) oiTable);
        } else if (oiTable instanceof OIVis) {
            return new OIVis(this, (OIVis) oiTable);
        } else if (oiTable instanceof OIVis2) {
            return new OIVis2(this, (OIVis2) oiTable);
        } else if (oiTable instanceof OIT3) {
            return new OIT3(this, (OIT3) oiTable);
        } else if (oiTable instanceof OIFlux) {
            return new OIFlux(this, (OIFlux) oiTable);
        }
        return null;
    }

    /**
     * Add the given OI_* tables to this OIFitsFile structure
     * @param oiTable new OI_* table
     */
    public void addOiTable(final OITable oiTable) {
        if (hasOiTable(oiTable)) {
            return;
        }
        // Prepare table keywords (ExtNb and ExtVer):
        // note: avoid reentrance as OIFitsCollection can reuse OITable

        // ext number (0..n):
        if (oiTable.getExtNb() == UNDEFINED_EXT_NB) {
            // keep existing ExtNb (OIFitsFile structure per Target):
            oiTable.setExtNb(getNbOiTables());
        }

        // ext version (1..n):
        if (oiTable.getExtVer() == 0) {
            // keep existing ExtNb (OIFitsFile structure per Target):
            int extVer = 0;
            if (oiTable instanceof OITarget) {
                // only 1 OI_TARGET table allowed.
                if (hasOiTarget()) {
                    throw new IllegalArgumentException("OI_TARGET is already defined !");
                }
            } else if (oiTable instanceof OIWavelength) {
                extVer = getNbOiWavelengths();
            } else if (oiTable instanceof OIArray) {
                extVer = getNbOiArrays();
            } else if (oiTable instanceof OIVis) {
                extVer = getNbOiVis();
            } else if (oiTable instanceof OIVis2) {
                extVer = getNbOiVis2();
            } else if (oiTable instanceof OIT3) {
                extVer = getNbOiT3();
            } else if (oiTable instanceof OIFlux) {
                extVer = getNbOiFlux();
            } else if (oiTable instanceof OICorr) {
                extVer = getNbOiCorrs();
            } else if (oiTable instanceof OIInspol) {
                extVer = getNbOiInspol();
            }

            extVer++;
            oiTable.setExtVer(extVer);
        }

        this.registerOiTable(oiTable);
    }

    /**
     * Remove the given OI_* tables from this OIFitsFile structure.
     * @param oiTable OI_* table to remove
     */
    public void removeOiTable(final OITable oiTable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Unregistering object for {0}", oiTable.idToString());
        }
        unregisterOiTable(oiTable);
        if (oiTable instanceof OIData) {
            // reset cached wavelength range:
            wavelengthRange = null;
        }
    }

    /**
     * Register valid OI_* tables (keyword and column values must be defined).
     * @param oiTable reference on one OI_* table
     */
    void registerOiTable(final OITable oiTable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Registering object for {0}", oiTable.idToString());
        }
        this.oiTables.add(oiTable);

        if (oiTable instanceof OITarget) {
            this.oiTargets.add((OITarget) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            this.oiWavelengths.add((OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            this.oiArrays.add((OIArray) oiTable);
        } else if (oiTable instanceof OICorr) {
            this.oiCorrs.add((OICorr) oiTable);
        } else if (oiTable instanceof OIInspol) {
            this.oiInspols.add((OIInspol) oiTable);
        } else if (oiTable instanceof OIVis) {
            this.oiDataTables.add((OIVis) oiTable);
            this.oiVisTables.add((OIVis) oiTable);
        } else if (oiTable instanceof OIVis2) {
            this.oiDataTables.add((OIVis2) oiTable);
            this.oiVis2Tables.add((OIVis2) oiTable);
        } else if (oiTable instanceof OIT3) {
            this.oiDataTables.add((OIT3) oiTable);
            this.oiT3Tables.add((OIT3) oiTable);
        } else if (oiTable instanceof OIFlux) {
            this.oiDataTables.add((OIFlux) oiTable);
            this.oiFluxTables.add((OIFlux) oiTable);
        }

        if (oiTable instanceof OIWavelength) {
            final OIWavelength o = (OIWavelength) oiTable;
            final String insName = o.getInsName();

            if (insName != null) {
                List<OIWavelength> v = this.insNameToOiWavelength.get(insName);

                if (v == null) {
                    v = new LinkedList<OIWavelength>();
                    this.insNameToOiWavelength.put(insName, v);
                }
                v.add(o);
            } else {
                logger.warning("INSNAME of OI_WAVELENGTH table is null in register()");
            }
            // reset cached wavelength range:
            wavelengthRange = null;
        } else if (oiTable instanceof OIArray) {
            final OIArray o = (OIArray) oiTable;
            final String arrName = o.getArrName();

            if (arrName != null) {
                List<OIArray> v = this.arrNameToOiArray.get(arrName);

                if (v == null) {
                    v = new LinkedList<OIArray>();
                    this.arrNameToOiArray.put(arrName, v);
                }
                v.add(o);
            } else {
                logger.warning("ARRNAME of OI_ARRAY table is null in register()");
            }
        } else if (oiTable instanceof OICorr) {
            final OICorr o = (OICorr) oiTable;
            final String corrName = o.getCorrName();

            if (corrName != null) {
                List<OICorr> v = this.corrNameToOiCorr.get(corrName);

                if (v == null) {
                    v = new LinkedList<OICorr>();
                    this.corrNameToOiCorr.put(corrName, v);
                }
                v.add(o);
            } else {
                logger.warning("CORRNAME of OI_CORR table is null in register()");
            }
        }
    }

    /**
     * Unregister an OI_* table.
     * @param oiTable reference on one OI_* table
     */
    private void unregisterOiTable(final OITable oiTable) {
        this.oiTables.remove(oiTable);

        if (oiTable instanceof OITarget) {
            this.oiTargets.remove((OITarget) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            this.oiWavelengths.remove((OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            this.oiArrays.remove((OIArray) oiTable);
        } else if (oiTable instanceof OICorr) {
            this.oiCorrs.remove((OICorr) oiTable);
        } else if (oiTable instanceof OIInspol) {
            this.oiInspols.remove((OIInspol) oiTable);
        } else if (oiTable instanceof OIVis) {
            this.oiDataTables.remove((OIVis) oiTable);
            this.oiVisTables.remove((OIVis) oiTable);
        } else if (oiTable instanceof OIVis2) {
            this.oiDataTables.remove((OIVis2) oiTable);
            this.oiVis2Tables.remove((OIVis2) oiTable);
        } else if (oiTable instanceof OIT3) {
            this.oiDataTables.remove((OIT3) oiTable);
            this.oiT3Tables.remove((OIT3) oiTable);
        } else if (oiTable instanceof OIFlux) {
            this.oiDataTables.remove((OIFlux) oiTable);
            this.oiFluxTables.remove((OIFlux) oiTable);
        }

        if (oiTable instanceof OIWavelength) {
            final OIWavelength o = (OIWavelength) oiTable;
            final String insName = o.getInsName();

            if (insName != null) {
                final List<OIWavelength> v = this.insNameToOiWavelength.get(insName);
                if (v != null) {
                    v.remove(o);
                }
            } else {
                logger.warning("INSNAME of OI_WAVELENGTH table is null in unregister()");
            }
            // reset cached wavelength range:
            wavelengthRange = null;
        } else if (oiTable instanceof OIArray) {
            final OIArray o = (OIArray) oiTable;
            final String arrName = o.getArrName();

            if (arrName != null) {
                final List<OIArray> v = this.arrNameToOiArray.get(arrName);
                if (v != null) {
                    v.remove(o);
                }
            } else {
                logger.warning("ARRNAME of OI_ARRAY table is null in unregister()");
            }
        } else if (oiTable instanceof OICorr) {
            final OICorr o = (OICorr) oiTable;
            final String corrName = o.getCorrName();

            if (corrName != null) {
                final List<OICorr> v = this.corrNameToOiCorr.get(corrName);
                if (v != null) {
                    v.remove(o);
                }
            } else {
                logger.warning("CORRNAME of OI_CORR table is null in unregister()");
            }
        }
    }

    /**
     * Mediator method to resolve cross references. Returns OiArray associated
     * to input parameter
     *
     * @param arrName string containing ARRNAME value
     * @return the OI_ARRAY table reference associated. If none is associated,
     *  returns NULL
     */
    public OIArray getOiArray(final String arrName) {
        final List<OIArray> v = this.arrNameToOiArray.get(arrName);
        if (v == null) {
            return null;
        }
        return v.get(0);
    }

    /**
     * Mediator method to resolve cross references. Returns OiWavelength
     * associated to input parameter.
     *
     * @param insName string containing INSNAME value
     * @return the OI_WAVELENGTH table reference associated. If none is
     *  associated, returns NULL
     */
    public OIWavelength getOiWavelength(final String insName) {
        List<OIWavelength> v = this.insNameToOiWavelength.get(insName);
        if (v == null) {
            return null;
        }
        return v.get(0);
    }

    /**
     * Mediator method to resolve cross references. Returns OiCorr associated
     * to input parameter
     *
     * @param corrName string containing CORRNAME value
     * @return the OI_CORR table reference associated. If none is associated,
     *  returns NULL
     */
    public OICorr getOiCorr(final String corrName) {
        final List<OICorr> v = this.corrNameToOiCorr.get(corrName);
        if (v == null) {
            return null;
        }
        return v.get(0);
    }

    /**
     * Mediator method to resolve cross references. Returns the accepted (ie
     * valid) station indexes.
     *
     * @param oiArray OiArray where station indexes are defined
     * @return the array containing the indexes.
     */
    public short[] getAcceptedStaIndexes(final OIArray oiArray) {
        if (oiArray == null) {
            return EMPTY_SHORT_ARRAY;
        }
        return oiArray.getStaIndex();
    }

    /**
     * Get all ARRNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public String[] getAcceptedArrNames() {
        final int len = this.arrNameToOiArray.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.arrNameToOiArray.keySet().toArray(new String[len]);
    }

    /**
     * Get all INSNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public String[] getAcceptedInsNames() {
        final int len = this.insNameToOiWavelength.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.insNameToOiWavelength.keySet().toArray(new String[len]);
    }

    /**
     * Get all CORRNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public String[] getAcceptedCorrNames() {
        final int len = this.corrNameToOiCorr.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.corrNameToOiCorr.keySet().toArray(new String[len]);
    }

    /**
     * Get all target identifiers defined.
     * @return an integer array containing all accepted values.
     */
    public short[] getAcceptedTargetIds() {
        final OITarget oiTarget = getOiTarget();
        if (oiTarget == null) {
            return EMPTY_SHORT_ARRAY;
        }

        return oiTarget.getTargetId();
    }

    /**
     * Get the wavelength range used by OIDATA tables.
     * @return the wavelength range used by OIDATA tables.
     */
    public Range getWavelengthRange() {
        // lazy computation:
        if (wavelengthRange == null) {
            computeWavelengthBounds();
        }
        return wavelengthRange;
    }

    /**
     * Compute the wavelength range used by OIDATA tables.
     */
    private void computeWavelengthBounds() {
        // Traverse all OIDATA to get OI_WAVELENGTH ranges:
        this.wavelengthRange = OIDataListHelper.getWaveLengthRange(getOiDataList());
    }

    /**
     * Return a short description of OIFITS content.
     * @return short description of OIFITS content
     */
    @Override
    public String toString() {
        return "\nFilePath:" + getAbsoluteFilePath() + "\n arrNameToOiArray:" + this.arrNameToOiArray
                + "\n insNameToOiWavelength:" + this.insNameToOiWavelength
                + "\n corrNameToOiCorr:" + this.corrNameToOiCorr
                + "\n " + this.getOITableList();
    }

    /**
     * Check the global structure of oifits file, including table presence and
     * syntax correction.
     * @param checker checker component
     */
    public void check(final OIFitsChecker checker) {
        try {
            // Initialize FileRef in OIFitsChecker
            checker.setFileRef(getFileRef(), getVersion());

            final long start = System.nanoTime();

            logger.info("Analysing values and references");

            /* Checking primary HDU */
            if (isOIFits2()) {
                if ((getPrimaryImageHDU() == null) || OIFitsChecker.isInspectRules()) {
                    // rule [OIFITS_MAIN_HEADER_EXIST_V2] check if the main header (PRIMARY HDU) exists in the OIFITS 2 file
                    checker.ruleFailed(Rule.OIFITS_MAIN_HEADER_EXIST_V2);
                }
            }

            logger.finest("Checking mandatory tables");

            /* Checking presence of one and only one OI_TARGET table */
            if (!hasOiTarget() || OIFitsChecker.isInspectRules()) {
                // rule [OIFITS_OI_TARGET_EXIST] check if only one OI_TARGET table exists in the OIFITS file
                checker.ruleFailed(Rule.OIFITS_OI_TARGET_EXIST);
            }

            /* Checking presence of at least one OI_WAVELENGTH table */
            if (this.insNameToOiWavelength.isEmpty() || OIFitsChecker.isInspectRules()) {
                // rule [OIFITS_OI_WAVELENGTH_EXIST] check if at least one OI_WAVELENGTH table exists in the OIFITS file
                checker.ruleFailed(Rule.OIFITS_OI_WAVELENGTH_EXIST);
            }

            if (isOIFits2()) {
                final OIPrimaryHDU primaryHDU = getOIPrimaryHDU();

                if (primaryHDU != null) {
                    // Note: arrNames may points to identical arrays ...
                    final int nbArrNames = getAcceptedArrNames().length;

                    /* Consider one single instrument if all insnames start with the main instrume keyword */
                    final String instrume = primaryHDU.getInstrume();
                    boolean sameInstrument = true;
                    for (String insname : getAcceptedInsNames()) {
                        if ((instrume == null) || !insname.startsWith(instrume)) {
                            sameInstrument = false;
                            break;
                        }
                    }
                    final boolean multiInstruments = !sameInstrument && getAcceptedInsNames().length > 1;

                    // Note: targetIds may contain duplicates or identical targets ...
                    final int targets = getAcceptedTargetIds().length;

                    /* rule [MAIN_HEADER_TYPE_MULTI] check if main header keywords are set to 'MULTI' for heterogeneous content */
                    if (((nbArrNames > 1) || multiInstruments || (targets > 1)) || OIFitsChecker.isInspectRules()) {
                        primaryHDU.checkMultiKeywords(checker, nbArrNames, multiInstruments, targets);
                    }
                }

                /* Checking presence of at least one OI_ARRAY table in OIFITS V2 */
                if (this.arrNameToOiArray.isEmpty() || OIFitsChecker.isInspectRules()) {
                    // rule [OIFITS_OI_ARRAY_EXIST_V2] check if at least one OI_ARRAY table exists in the OIFITS 2 file
                    checker.ruleFailed(Rule.OIFITS_OI_ARRAY_EXIST_V2);
                }

                checkOIInspols(checker);
            }

            /* Starting syntactical analysis */
            logger.finest("Building list of table for keywords analysis");

            if (isOIFits2()) {
                if ((getPrimaryImageHDU() != null) || OIFitsChecker.isInspectRules()) {
                    getPrimaryImageHDU().checkSyntax(checker);
                }
            }

            // validate FitsImageHDUs:
            {
                final boolean skipFirst = isOIFits2();
                int i = 0;
                for (FitsImageHDU imgHdu : getFitsImageHDUs()) {
                    if (!skipFirst || (i != 0)) {
                        imgHdu.checkSyntax(checker);
                    }
                    i++;
                }
            }

            // NOTE: may check twice the loaded OIFits: how to avoid duplications ?
            // Only 1 case: column format => use OIFitsChecker.skipFormat flag to avoid duplicated failures
            for (OITable oiTable : getOITableList()) {
                oiTable.checkSyntax(checker);
            }

            if (getExistingImageOiData() != null) {
                getExistingImageOiData().checkSyntax(checker);
            }

            // Define Severity:
            checker.defineSeverity(SeverityProfileFactory.getInstance().getProfile(SeverityProfileFactory.PROFILE_JMMC));

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "OIFitsFile.check: duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
            }
        } finally {
            // cleanup temporary variables
            checker.cleanup();
        }
    }

    /**
     * Check validity of cross references of non-data tables, ie check both
     * tables have different identifiers, or no mandatory identifier is not
     * defined.
     *
     * @param oiTable reference on table to check
     * @param checker checker component
     */
    public void checkCrossReference(final OITable oiTable, final OIFitsChecker checker) {
        if (oiTable instanceof OITarget) {
            final OITarget oitarget = (OITarget) oiTable;

            if (oitarget.getNbTargets() < 1 || OIFitsChecker.isInspectRules()) {
                // rule [OI_TARGET_TARGET_EXIST] check if the OI_TARGET table have at least one target
                checker.ruleFailed(Rule.OI_TARGET_TARGET_EXIST, oitarget);
            }
        } else if (oiTable instanceof OIWavelength) {
            final OIWavelength oiWavelength = (OIWavelength) oiTable;
            final String insName = oiWavelength.getInsName();

            if (insName != null) {
                /* Get OiWavelength associated to INSNAME value */
                final List<OIWavelength> oiWlTables = insNameToOiWavelength.get(insName);

                if ((oiWlTables == null) || isInspectRules()) {
                    /* Problem: INSNAME value has not been encoutered during
                     * building step, that should be impossible */
                    // Problem: OI_WAVELENGTH.INSNAME can be modified without fixing cross-references 
                    // rule [INSNAME_REF] check if an OI_WAVELENGTH table matches the INSNAME keyword
                    checker.ruleFailed(Rule.INSNAME_REF, oiWavelength, OIFitsConstants.KEYWORD_INSNAME).addKeywordValue(insName);
                }
                if (((oiWlTables != null) && (oiWlTables.size() > 1)) || isInspectRules()) {
                    /* Problem: more that one OiWavelength table associated to INSNAME value, that is strictly forbidden */
                    final StringBuilder sb = new StringBuilder();

                    if (oiWlTables != null) {
                        for (Iterator<OIWavelength> it = oiWlTables.iterator(); it.hasNext();) {
                            sb.append('|').append(it.next().idToString());
                        }
                        sb.deleteCharAt(0);
                    }
                    // rule [INSNAME_UNIQ] check if a single OI_WAVELENGTH table corresponds to the INSNAME keyword
                    checker.ruleFailed(Rule.INSNAME_UNIQ, oiWavelength, OIFitsConstants.KEYWORD_INSNAME).addKeywordValue(oiWavelength.getInsName(), sb.toString());
                }
            } else {
                // already checked
            }
        } else if (oiTable instanceof OIArray) {
            final OIArray oiArray = (OIArray) oiTable;
            final String arrName = oiArray.getArrName();

            if (arrName != null || isInspectRules()) {
                /* Get OiArray associated to ARRNAME value */
                final List<OIArray> oiArrTables = arrNameToOiArray.get(arrName);

                if ((oiArrTables == null) || isInspectRules()) {
                    /* Problem: OI_ARRAY.ARRNAME can be modified without fixing cross-references */
                    // rule [ARRNAME_REF] check if an OI_ARRAY table matches the ARRNAME keyword
                    checker.ruleFailed(Rule.ARRNAME_REF, oiArray, OIFitsConstants.KEYWORD_ARRNAME).addKeywordValue(arrName);
                }
                if (((oiArrTables != null) && (oiArrTables.size() > 1)) || isInspectRules()) {
                    /* Problem: more that one OiArray table associated to ARRNAME value, that is strictly forbiden */
                    final StringBuilder sb = new StringBuilder();

                    if (oiArrTables != null) {
                        for (Iterator<OIArray> it = oiArrTables.iterator(); it.hasNext();) {
                            sb.append('|').append(it.next().idToString());
                        }
                        sb.deleteCharAt(0);
                    }
                    // rule [ARRNAME_UNIQ] check if a single OI_ARRAY table corresponds to the ARRNAME keyword
                    checker.ruleFailed(Rule.ARRNAME_UNIQ, oiArray, OIFitsConstants.KEYWORD_ARRNAME).addKeywordValue(oiArray.getArrName(), sb.toString());
                }
            } else {
                // already checked by rule [OI_ARRAY_ARRNAME]
            }
        } else if (oiTable instanceof OICorr) {
            final OICorr oiCorr = (OICorr) oiTable;
            final String corrName = oiCorr.getCorrName();

            if (corrName != null) {
                /* Get OICorr associated to CORRNAME value */
                final List<OICorr> oiCorrTables = corrNameToOiCorr.get(corrName);

                if ((oiCorrTables == null) || isInspectRules()) {
                    /* Problem: CORRNAME value has not been encoutered during
                     * building step, that should be impossible */
                    // Problem: OI_CORR.CORRNAME can be modified without fixing cross-references 
                    // rule [CORRNAME_REF] check if an OI_CORR table matches the CORRNAME keyword
                    checker.ruleFailed(Rule.CORRNAME_REF, oiCorr, OIFitsConstants.KEYWORD_CORRNAME).addKeywordValue(corrName);
                }
                if (((oiCorrTables != null) && (oiCorrTables.size() > 1)) || isInspectRules()) {
                    /* Problem: more that one OICorr table associated to CORRNAME value, that is strictly forbiden */
                    final StringBuilder sb = new StringBuilder();

                    if (oiCorrTables != null) {
                        for (Iterator<OICorr> it = oiCorrTables.iterator(); it.hasNext();) {
                            sb.append('|').append(it.next().idToString());
                        }
                        sb.deleteCharAt(0);
                    }
                    // rule [CORRNAME_UNIQ] check if a single OI_CORR table corresponds to the CORRNAME keyword
                    checker.ruleFailed(Rule.CORRNAME_UNIQ, oiCorr, OIFitsConstants.KEYWORD_CORRNAME).addKeywordValue(oiCorr.getCorrName(), sb.toString());
                }
            } else {
                // already checked
            }
        }
    }

    private void checkOIInspols(OIFitsChecker checker) {
        // Map to verify the presence of a duplicate INSNAME
        final Map<String, Set<OIInspol>> insnameToOIInspol = new HashMap<String, Set<OIInspol>>();

        for (OIInspol oiInspol : oiInspols) {

            for (String insName : oiInspol.getInsNames()) {
                /* Get OIInspol associated to INSNAME value */
                Set<OIInspol> oiInspolSet = insnameToOIInspol.get(insName);

                if (oiInspolSet == null) {
                    //preserve insertion order
                    oiInspolSet = new LinkedHashSet<OIInspol>();
                    insnameToOIInspol.put(insName, oiInspolSet);
                }

                oiInspolSet.add(oiInspol);
            }

            for (Map.Entry<String, Set<OIInspol>> entry : insnameToOIInspol.entrySet()) {
                final Set<OIInspol> oiInspolSet = entry.getValue();

                // if there are several OI_INSPOL for this INSNAME
                // And the OI_INSPOL being validated is present in the set 
                if (((oiInspolSet.size() > 1) && oiInspolSet.contains(oiInspol)) || isInspectRules()) {
                    final String insName = entry.getKey();
                    final StringBuilder sb = new StringBuilder();

                    if (!oiInspolSet.isEmpty()) {
                        for (Iterator<OIInspol> it = oiInspolSet.iterator(); it.hasNext();) {
                            sb.append('|').append(it.next().idToString());
                        }
                        sb.deleteCharAt(0);
                    }
                    // rule [OI_INSPOL_INSNAME_UNIQ] check if the INSNAME column values are only present in a single OI_INSPOL table (compare multi OI_INSPOL table)
                    checker.ruleFailed(Rule.OI_INSPOL_INSNAME_UNIQ, oiInspol, OIFitsConstants.KEYWORD_INSNAME).addKeywordValue(insName, sb.toString());
                }
            }
        }
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public void accept(final ModelVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * This equals method uses the absoluteFilePath equality
     * @param obj other object (OIFitsFile)
     * @return true if the absoluteFilePath are equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // identity comparison:
        if (this == obj) {
            return true;
        }
        // class check:
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OIFitsFile other = (OIFitsFile) obj;

        return areEquals(this.getAbsoluteFilePath(), other.getAbsoluteFilePath());
    }

    /**
     * This hashcode implementation uses only the absoluteFilePath field
     * @return hashcode
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.getAbsoluteFilePath() != null ? this.getAbsoluteFilePath().hashCode() : 0);
        return hash;
    }

    /*
     * --- data analysis  -----------------------------------------------------
     */
    /**
     * Do analyze this OIFits structure
     */
    public void analyze() {
        Analyzer.getInstance().visit(this);
    }

    /**
     * Indicate to clear any cached value (derived column ...)
     */
    public void setChanged() {
        distinctGranules.clear();
        oiDataPerGranule.clear();
        usedStaNamesMap.clear();
    }

    /**
     * Return the Distinct Granules
     * @return Distinct Granules
     */
    public Map<Granule, Granule> getDistinctGranules() {
        return distinctGranules;
    }

    /**
     * Return the Set of OIData tables keyed by Granule
     * @return Set of OIData tables keyed by Granule
     */
    public Map<Granule, Set<OIData>> getOiDataPerGranule() {
        return oiDataPerGranule;
    }

    /**
     * Return the Map of sorted staNames to StaNamesDir
     * @return Map of sorted staNames to StaNamesDir
     */
    public Map<String, StaNamesDir> getUsedStaNamesMap() {
        return usedStaNamesMap;
    }

    /*
     * Getter - Setter --------------------------------------------------------
     */
    /**
     * Get the OIPrimaryHDU if defined.
     * @return OIPrimaryHDU or null
     */
    public OIPrimaryHDU getOIPrimaryHDU() {
        final FitsImageHDU hdu = getPrimaryImageHDU();
        return (hdu instanceof OIPrimaryHDU) ? ((OIPrimaryHDU) hdu) : null;
    }

    /**
     * @return the version
     */
    public OIFitsStandard getVersion() {
        return version;
    }

    /**
     * Boolean test for OIFits Version 2
     *
     * @return true if version 2
     */
    public boolean isOIFits2() {
        return version == OIFitsStandard.VERSION_2;
    }

    /**
     * Get the OIFitsStandard corresponding to the given keyword content value
     * @param content value to determine the OIFITS standard
     * @return the version
     */
    public static OIFitsStandard getOIFitsStandard(final String content) {
        return (FitsConstants.KEYWORD_CONTENT_OIFITS2.equals(content) ? OIFitsStandard.VERSION_2 : OIFitsStandard.VERSION_1);
    }

    /**
     * Return the Missing references kept to avoid repeated warnings
     *
     * @return Missing references kept to avoid repeated warnings
     */
    public Set<String> getMissingTableNames() {
        return missingTableNames;
    }

    /**
     * Return true if the given name corresponds to a missing reference
     *
     * @param name table name
     * @return true if the given name corresponds to a missing reference
     */
    public boolean hasMissingTableName(final String name) {
        return missingTableNames.contains(name);
    }

    /**
     * Add the given name to the missing references
     *
     * @param name table name
     */
    public void addMissingTableName(final String name) {
        missingTableNames.add(name);
    }

    /**
     * Get the number of OI_* tables
     *
     * @see #getOiTable(int)
     * @return the number of OI_* tables
     */
    public int getNbOiTables() {
        return this.oiTables.size();
    }

    /**
     * Return the nth OI_* table
     *
     * @param index index
     * @return the nth OI_* table
     */
    public OITable getOiTable(final int index) {
        return this.oiTables.get(index);
    }

    /**
     * Return an array containing all OI_* tables
     *
     * @return an array containing all OI_* tables
     */
    public OITable[] getOiTables() {
        return this.oiTables.toArray(new OITable[this.oiTables.size()]);
    }

    /**
     * Return true if the given OI_* table is already present
     *
     * @param oiTable table to test
     * @return true if the given OI_* table is already present
     */
    public boolean hasOiTable(final OITable oiTable) {
        return this.oiTables.contains(oiTable);
    }

    /**
     * Return the (internal) list of OI_* tables 
     * (Used by OIFitsExplorer)
     *
     * @return the (internal) list of OI_* tables
     */
    public List<OITable> getOITableList() {
        return this.oiTables;
    }

    /**
     * Tells if the file contains a OI_TARGET table.
     *
     * @return true if the file contains a OI_TARGET table
     */
    public boolean hasOiTarget() {
        return !this.oiTargets.isEmpty();
    }

    /**
     * Return the OI_TARGET table or null if not present
     *
     * @return the OI_TARGET table
     */
    public OITarget getOiTarget() {
        if (this.oiTargets.isEmpty()) {
            return null;
        } else {
            return this.oiTargets.get(0);
        }
    }

    /**
     * Tells if the file contains some OI_ARRAY tables
     *
     * @return true if the file contains some OI_ARRAY table
     */
    public boolean hasOiArray() {
        return !this.oiArrays.isEmpty();
    }

    /**
     * Get the number of OI_ARRAY tables
     *
     * @return the number of OI_ARRAY tables
     */
    public int getNbOiArrays() {
        return this.oiArrays.size();
    }

    /**
     * Return an array containing all OI_ARRAY tables
     *
     * @return an array containing all OI_ARRAY tables
     */
    public OIArray[] getOiArrays() {
        return this.oiArrays.toArray(new OIArray[this.oiArrays.size()]);
    }

    /**
     * Tells if the file contains some OI_WAVELENGTH tables
     *
     * @return true if the file contains some OI_WAVELENGTH table
     */
    public boolean hasOiWavelengths() {
        return !this.oiWavelengths.isEmpty();
    }

    /**
     * Get the number of OI_WAVELENGTH tables
     *
     * @return the number of OI_WAVELENGTH tables
     */
    public int getNbOiWavelengths() {
        return this.oiWavelengths.size();
    }

    /**
     * Return an array containing all OI_WAVELENGTH tables
     *
     * @return an array containing all OI_WAVELENGTH tables
     */
    public OIWavelength[] getOiWavelengths() {
        return this.oiWavelengths.toArray(new OIWavelength[this.oiWavelengths.size()]);
    }

    /**
     * Tell if the file contains some OI_CORR tables
     *
     * @return true if the file contains some OI_CORR table
     */
    public boolean hasOiCorr() {
        return !this.oiCorrs.isEmpty();
    }

    /**
     * Get the number of OI_CORR tables
     *
     * @return the number of OI_CORR tables
     */
    public int getNbOiCorrs() {
        return this.oiCorrs.size();
    }

    /**
     * Return an array containing all OI_CORR tables
     *
     * @return an array containing all OI_CORR tables
     */
    public OICorr[] getOiCorrs() {
        return this.oiCorrs.toArray(new OICorr[this.oiCorrs.size()]);
    }

    /**
     * Tells if the file contains some OI data tables
     *
     * @return true if the file contains some OI data table
     */
    public boolean hasOiData() {
        return !this.oiDataTables.isEmpty();
    }

    /**
     * Get the number of OI data tables
     *
     * @return the number of OI data tables
     */
    public int getNbOiData() {
        return this.oiDataTables.size();
    }

    /**
     * Return an array containing all OI data tables
     *
     * @return an array containing all OI data tables
     */
    public OIData[] getOiDatas() {
        return this.oiDataTables.toArray(new OIData[this.oiDataTables.size()]);
    }

    /**
     * Return the (internal) list of OI data tables
     *
     * @return the (internal) list of OI data tables
     */
    public List<OIData> getOiDataList() {
        return this.oiDataTables;
    }

    /**
     * Tells if the file contains some OI_VIS tables
     *
     * @return true if the file contains some OI_VIS table
     */
    public boolean hasOiVis() {
        return !this.oiVisTables.isEmpty();
    }

    /**
     * Get the number of OI_VIS tables
     *
     * @return the number of OI_VIS tables
     */
    public int getNbOiVis() {
        return this.oiVisTables.size();
    }

    /**
     * Return an array containing all OI_VIS tables
     *
     * @return an array containing all OI_VIS tables
     */
    public OIVis[] getOiVis() {
        return this.oiVisTables.toArray(new OIVis[this.oiVisTables.size()]);
    }

    /**
     * Tell if the file contains some OI_VIS2 tables
     *
     * @return true if the file contains some OI_VIS2 table
     */
    public boolean hasOiVis2() {
        return !this.oiVis2Tables.isEmpty();
    }

    /**
     * Get the number of OI_VIS2 tables
     *
     * @return the number of OI_VIS2 tables
     */
    public int getNbOiVis2() {
        return this.oiVis2Tables.size();
    }

    /**
     * Return an array containing all OI_VIS2 tables
     *
     * @return an array containing all OI_VIS2 tables
     */
    public OIVis2[] getOiVis2() {
        return this.oiVis2Tables.toArray(new OIVis2[this.oiVis2Tables.size()]);
    }

    /**
     * Tells if the file contains some OI_T3 tables
     *
     * @return true if the file contains some OI_T3 table
     */
    public boolean hasOiT3() {
        return !this.oiT3Tables.isEmpty();
    }

    /**
     * Get the number of OI_T3 tables
     *
     * @return the number of OI_T3 tables
     */
    public int getNbOiT3() {
        return this.oiT3Tables.size();
    }

    /**
     * Return an array containing all OI_T3 tables
     *
     * @return an array containing all OI_T3 tables
     */
    public OIT3[] getOiT3() {
        return this.oiT3Tables.toArray(new OIT3[this.oiT3Tables.size()]);
    }

    /**
     * Tell if the file contains some OI_FLUX tables
     *
     * @return true if the file contains some OI_FLUX table
     */
    public boolean hasOiFlux() {
        return !this.oiFluxTables.isEmpty();
    }

    /**
     * Get the number of OI_FLUX tables
     *
     * @return the number of OI_FLUX tables
     */
    public int getNbOiFlux() {
        return this.oiFluxTables.size();
    }

    /**
     * Return an array containing all OI_FLUX tables
     *
     * @return an array containing all OI_FLUX tables
     */
    public OIFlux[] getOiFlux() {
        return this.oiFluxTables.toArray(new OIFlux[this.oiFluxTables.size()]);
    }

    /**
     * Tell if the file contains some OI_INSPOL tables
     *
     * @return true if the file contains some OI_INSPOL table
     */
    public boolean hasOiInspol() {
        return !this.oiInspols.isEmpty();
    }

    /**
     * Get the number of OI_INSPOL tables
     *
     * @return the number of OI_INSPOL tables
     */
    public int getNbOiInspol() {
        return this.oiInspols.size();
    }

    /**
     * Return an array containing all OI_INSPOL tables
     *
     * @return an array containing all OI_INSPOL tables
     */
    public OIInspol[] getOiInspol() {
        return this.oiInspols.toArray(new OIInspol[this.oiInspols.size()]);
    }

    /**
     * Define the source location associated to the local file
     * @param sourceURI source URI
     */
    public void setSourceURI(final URI sourceURI) {
        this.sourceURI = sourceURI;
    }

    /**
     * Get sourceURI
     * @return sourceURI
     */
    public URI getSourceURI() {
        return this.sourceURI;
    }

    /**
     * Define the file size
     * @param size file size in bytes
     */
    public void setSize(final long size) {
        this.fileSize = size;
    }

    /**
     * Return the size of the file
     * @return file size in bytes
     */
    public long getSize() {
        return fileSize;
    }

    /**
     * Return the (optional) MD5 sum of the file
     * @return (optional) MD5 sum of the file
     */
    public String getMd5sum() {
        return md5sum;
    }

    /**
     * Define the (optional) MD5 sum of the file
     * @param md5sum (optional) MD5 sum of the file
     */
    public void setMd5sum(final String md5sum) {
        this.md5sum = md5sum;
    }

    /**
     * Get the IMAGE-OI data if defined.
     * @return the IMAGE-OI data if defined else null.
     */
    public ImageOiData getExistingImageOiData() {
        return imageOiData;
    }

    /**
     * Get the IMAGE-OI data or create a new one.
     * @return the IMAGE-OI data or create a new one.
     */
    public ImageOiData getImageOiData() {
        if (imageOiData == null) {
            imageOiData = new ImageOiData();
        }
        return imageOiData;
    }

    /**
     * Define the (optional) IMAGE-OI data.
     * @param imageOiData
     */
    public void setImageOiData(final ImageOiData imageOiData) {
        this.imageOiData = imageOiData;
    }
}
