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
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.OIFitsProcessor;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.IndexMask;
import fr.jmmc.oitools.model.ModelBase;
import static fr.jmmc.oitools.model.ModelBase.UNDEFINED;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OICorr;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIPrimaryHDU;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.TargetManager;
import fr.jmmc.oitools.util.OITableComparator;
import fr.nom.tam.fits.FitsDate;
import fr.nom.tam.util.ArrayFuncs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processing class to merge the given OIFitsCollection into a single OIFitsFile instance
 *
 * @author jammetv
 */
public final class Merger {

    private static final Logger logger = Logger.getLogger(Merger.class.getName());

    private static final Short UNDEFINED_SHORT = Short.valueOf(ModelBase.UNDEFINED_SHORT);

    /**
     * Utility class
     */
    private Merger() {
        super();
    }

    /**
     * Merge the given OIFitsFile instances.
     *
     * @param oiFitsToMerge OIFitsFile instances
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final OIFitsFile... oiFitsToMerge) throws IllegalArgumentException {
        if (oiFitsToMerge == null || oiFitsToMerge.length < 1) {
            throw new IllegalArgumentException("Merge: Missing OIFits inputs");
        }
        final OIFitsCollection oiFitsCollection = OIFitsCollection.create(oiFitsToMerge);

        return process(oiFitsCollection);
    }

    /**
     * Merge the given OIFitsCollection
     *
     * @param oiFitsCollection OIFits collection
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final OIFitsCollection oiFitsCollection) throws IllegalArgumentException {
        return process(oiFitsCollection, null, null);
    }

    /**
     * Merge the given OIFitsCollection with the given Selector
     *
     * @param oiFitsCollection OIFits collection
     * @param selector optional Selector instance to filter content
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final OIFitsCollection oiFitsCollection, final Selector selector) throws IllegalArgumentException {
        return process(oiFitsCollection, selector, null);
    }

    /**
     * Merge of OIFitsFile structure.
     *
     * @param oiFitsCollection OIFits collection
     * @param selector optional Selector instance to filter content
     * @param std OIFits standard for the output OIFitsFile
     * @return new OIFitsFile instance
     * @throws IllegalArgumentException
     */
    public static OIFitsFile process(final OIFitsCollection oiFitsCollection, final Selector selector, final OIFitsStandard std) throws IllegalArgumentException {
        if (oiFitsCollection == null || oiFitsCollection.isEmpty()) {
            throw new IllegalArgumentException("Merge: Missing OIFits inputs");
        }

        final SelectorResult result = filterData(oiFitsCollection, selector);

        return process(result, std);
    }

    public static OIFitsFile process(final SelectorResult result) {
        return process(result, null);
    }

    public static OIFitsFile process(final SelectorResult result, final OIFitsStandard std) {
        final OIFitsFile resultFile;
        if (result == null) {
            logger.log(Level.INFO, "Merge: no matching data");
            resultFile = null;
        } else {
            logger.log(Level.INFO, "selected targets:  {0}", result.getDistinctTargets());
            logger.log(Level.INFO, "selected insModes: {0}", result.getDistinctInstrumentModes());
            logger.log(Level.INFO, "selected nightIds: {0}", result.getDistinctNightIds());

            // 1. CreateOIFits anyway
            resultFile = createOIFits(std, result);

            final Context ctx = new Context(result, resultFile);

            // 2. Get all referenced OIPrimaryHDU, Target, OIWavelength, OIArray and OICorr in data tables
            collectTables(ctx);

            // 2.1 Process Primary header
            processOIPrimaryHDU(ctx);

            // 3. process Meta Data to prepare mappings:
            // Process OI_TARGET:
            processOITarget(ctx);

            // Process OI_WAVELENGTH tables
            processOIWavelengths(ctx);

            // Process OI_ARRAY tables 
            processOIArrays(ctx);

            // Specific to OIFits V2
            if (resultFile.isOIFits2()) {
                // Process OI_CORR tables
                processOICorrs(ctx);

                // TODO: OIINSPOL ?
            }

            // 4. process Data:
            processOIData(ctx);

            // Update history:
            final FitsImageHDU primaryHdu = ctx.resultFile.getPrimaryImageHDU();

            // Add used oifits files:
            primaryHdu.addHeaderHistory(generateCLIInputs(result.getSortedOIFitsFiles()));

            // Add CLI args:
            if (result.hasSelector()) {
                final Selector selector = result.getSelector();
                primaryHdu.addHeaderHistory(OIFitsProcessor.generateCLIargs(selector));
            }
        }
        return resultFile;
    }

    private static String generateCLIInputs(final List<OIFitsFile> oiFitsFiles) {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("CLI Inputs: ");

        for (OIFitsFile oiFitsFile : oiFitsFiles) {
            sb.append(oiFitsFile.getFileName()).append(" ");
        }
        return sb.toString();
    }

    private static OIFitsFile createOIFits(final OIFitsStandard std, final SelectorResult result) {
        OIFitsStandard version = std;

        if (version == null) {
            if (result == null) {
                version = OIFitsStandard.VERSION_1;
            } else {
                // Use the highest version from the OIFitsFile instances corresponding to the result:
                for (OIFitsFile oiFits : result.getSortedOIFitsFiles()) {
                    if (version == null || oiFits.getVersion().ordinal() > version.ordinal()) {
                        version = oiFits.getVersion();

                        if (version == OIFitsStandard.VERSION_2) {
                            // max(version) selected:
                            break;
                        }
                    }
                }
            }
        }

        logger.log(Level.FINE, "Using OIFITS {0}", version);

        return new OIFitsFile(version);
    }

    /**
     * Filter OiData tables
     *
     * @param oiFitsCollection OIFits collection
     * @param selector optional Selector instance to filter content
     * @return Selector result or null if no data match
     */
    private static SelectorResult filterData(final OIFitsCollection oiFitsCollection, final Selector selector) {
        logger.log(Level.FINE, "Selector: {0}", selector);

        // Query OIData matching criteria:
        final SelectorResult result = oiFitsCollection.findOIData(selector);

        logger.log(Level.FINE, "selectorResult: {0}", result);

        return result;
    }

    /**
     * Collect all OIWavelength, OIArray and OICorr tables from OiData tables
     *
     * @param ctx merge context
     */
    private static void collectTables(final Context ctx) {
        // Collect all OIWavelength, OIArray and OICorr tables:
        for (OIData oiData : ctx.selectorResult.getSortedOIDatas()) {
            // Collect Primary header:
            final OIPrimaryHDU primaryHDU = oiData.getOIFitsFile().getOIPrimaryHDU();
            if (primaryHDU != null) {
                ctx.usedOIPrimaryHDU.add(primaryHDU);
            }
            // Collect referenced tables:
            final OITarget oiTarget = oiData.getOiTarget();
            if (oiTarget != null) {
                ctx.usedOITargets.add(oiTarget);
            }
            final OIWavelength oiWavelength = oiData.getOiWavelength();
            if (oiWavelength != null) {
                ctx.usedOIWavelengths.add(oiWavelength);
            }
            final OIArray oiArray = oiData.getOiArray();
            if (oiArray != null) {
                ctx.usedOIArrays.add(oiArray);
            }
            final OICorr oiCorr = oiData.getOiCorr();
            if (oiCorr != null) {
                ctx.usedOICorrs.add(oiCorr);
            }
            // TODO: OIInspol ?
        }
    }

    /**
     * Process Primary header
     *
     * @param ctx merge context
     * @throws IllegalArgumentException
     */
    private static void processOIPrimaryHDU(final Context ctx) throws IllegalArgumentException {
        final String date = FitsDate.getFitsDateString();

        final FitsImageHDU imageHdu;

        if (ctx.resultFile.getVersion() == OIFitsStandard.VERSION_2) {
            final OIPrimaryHDU primaryHdu;

            if (ctx.usedOIPrimaryHDU.size() == 1) {
                // single file => keep this primary HDU:
                primaryHdu = ctx.usedOIPrimaryHDU.iterator().next();
            } else {
                primaryHdu = new OIPrimaryHDU();

                // note: ignore header cards (extra keywords)
                final Map<String, Set<String>> keyValues = new HashMap<String, Set<String>>(32);

                // Collect distinct values for mandatory keywords:
                for (OIPrimaryHDU hdu : ctx.usedOIPrimaryHDU) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Dump HDU: {0}", hdu.idToString());
                    }

                    for (KeywordMeta keyword : hdu.getKeywordDescCollection()) {
                        final String keywordName = keyword.getName();

                        if (!keyword.isOptional()) {
                            // get keyword value :
                            final Object keywordValue = hdu.getKeywordValue(keywordName);

                            if (keywordValue == null) {
                                // skip missing values
                                continue;
                            }

                            if (logger.isLoggable(Level.FINE)) {
                                logger.log(Level.FINE, "Get {0} = {1}", new Object[]{keywordName, keywordValue});
                            }

                            Set<String> values = keyValues.get(keywordName);
                            if (values == null) {
                                values = new LinkedHashSet<String>();
                                keyValues.put(keywordName, values);
                            }
                            values.add(keywordValue.toString());
                        }
                    }
                }
                // fill OIFITS2 Mandatory keywords:
                logger.log(Level.FINE, "keyValues: {0}", keyValues);

                // note: not really correct as filters can reduce the number of valid entries (TARGET ...)
                for (KeywordMeta keyword : primaryHdu.getKeywordDescCollection()) {
                    final String keywordName = keyword.getName();

                    if (!keyword.isOptional()) {
                        final Set<String> values = keyValues.get(keywordName);

                        final String keywordValue;
                        if (values == null) {
                            keywordValue = UNDEFINED;
                        } else {
                            if (values.size() == 1) {
                                keywordValue = values.iterator().next();
                            } else {
                                keywordValue = OIPrimaryHDU.VALUE_MULTI;
                            }
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Set {0} = {1}", new Object[]{keywordName, keywordValue});
                        }
                        primaryHdu.setKeyword(keywordName, keywordValue);
                    }
                }
            }

            if (ctx.resultFile.getVersion() == OIFitsStandard.VERSION_2) {
                primaryHdu.setContent(FitsConstants.KEYWORD_CONTENT_OIFITS2);
            }

            // set date:
            primaryHdu.setDate(date);

            imageHdu = primaryHdu;
        } else {
            imageHdu = new FitsImageHDU();

            // set date:
            imageHdu.addHeaderCard(FitsConstants.KEYWORD_DATE, date, "Date the HDU was written");
        }

        // Update history:
        imageHdu.addHeaderHistory("Written by JMMC OITools Merger on " + date);

        ctx.resultFile.setPrimaryImageHdu(imageHdu);
    }

    /**
     * Process OITarget table
     *
     * @param ctx merge context
     * @throws IllegalArgumentException
     */
    private static void processOITarget(final Context ctx) throws IllegalArgumentException {
        final OIFitsFile resultFile = ctx.resultFile;

        // Targets:
        final TargetManager tm = ctx.selectorResult.getOiFitsCollection().getTargetManager();
        // keep targets associated to selected Granules ONLY:
        final List<Target> gTargets = ctx.selectorResult.getDistinctTargets();
        final int nbTargets = gTargets.size();

        final Map<Target, Short> newTargetIds = new IdentityHashMap<Target, Short>();

        final OITarget newOiTarget = new OITarget(resultFile, nbTargets);

        int i = 0;
        for (Target target : gTargets) {
            final Short targetId = Short.valueOf((short) (i + 1));
            newOiTarget.setTarget(i++, targetId, target);
            newTargetIds.put(target, targetId);
        }

        resultFile.addOiTable(newOiTarget);

        final Map<OITarget, Map<Short, Short>> mapOITargetIDs = ctx.mapOITargetIDs;

        // Define mapping per OITarget:
        for (OITarget oiTarget : ctx.usedOITargets) {
            final Map<Short, Short> mapIds = new HashMap<Short, Short>(4);

            for (Target target : gTargets) {
                final Set<Short> targetIds = oiTarget.getTargetIds(tm, target);

                if (targetIds != null) {
                    final Short newId = newTargetIds.get(target);

                    for (Short id : targetIds) {
                        mapIds.put(id, newId);
                    }
                }
            }
            mapOITargetIDs.put(oiTarget, mapIds);
        }

        logger.log(Level.FINE, "newTargetIds:   {0}", newTargetIds);
        logger.log(Level.FINE, "mapOITargetIDs: {0}", mapOITargetIDs);
    }

    /**
     * Process OIWavelength tables
     *
     * @param ctx merge context
     */
    private static void processOIWavelengths(final Context ctx) {
        // Browse all used tables, change name if already present in result, add to map [old table <=> new table]
        if (!ctx.usedOIWavelengths.isEmpty()) {
            final SelectorResult selectorResult = ctx.selectorResult;
            final OIFitsFile resultFile = ctx.resultFile;
            final Map<OIWavelength, OIWavelength> mapOIWavelengths = ctx.mapOIWavelengths;

            // Reduce duplicated tables (distinct table to duplicates):
            final IdentityHashMap<OIWavelength, ArrayList<OIWavelength>> dedupOIWavelengths = new IdentityHashMap<>();
            // insname to tables:
            final LinkedHashMap<String, ArrayList<OIWavelength>> nameToDistinctOIWavelengths = new LinkedHashMap<>();

            deduplicateTables(OIFitsConstants.KEYWORD_INSNAME, ctx.usedOIWavelengths, dedupOIWavelengths, nameToDistinctOIWavelengths);

            // Copy all distinct OIWavelength tables:
            for (ArrayList<OIWavelength> listOIWavelengths : nameToDistinctOIWavelengths.values()) {

                for (OIWavelength oiWavelength : listOIWavelengths) {
                    final String name = oiWavelength.getInsName();

                    // If name is already present in result, 
                    // change the name and memorise this change to update data later
                    String newName = name;
                    int idx = 0;
                    OIWavelength prevOiWavelength = null;

                    for (;;) {
                        prevOiWavelength = resultFile.getOiWavelength(newName);

                        if (prevOiWavelength == null) {
                            // table is not present
                            break;
                        }
                        // use another suffix (_nn):
                        idx++;
                        newName = name + "_" + idx;
                    }

                    final OIWavelength newOiWavelength;

                    if (prevOiWavelength != null) {
                        // use the previous table
                        newOiWavelength = prevOiWavelength;
                    } else {
                        // add the new table (copy):
                        newOiWavelength = (OIWavelength) resultFile.copyTable(oiWavelength);
                        newOiWavelength.setInsName(newName);

                        // get the wavelength mask for this wavelength table:
                        final IndexMask maskWavelength = selectorResult.getWavelengthMaskNotFull(oiWavelength);

                        if (maskWavelength != null) {
                            final int nKeepRows = maskWavelength.cardinality();
                            // redim the table to the correct row count to prune invalid rows:
                            newOiWavelength.resizeTable(nKeepRows, maskWavelength.getBitSet());

                            logger.log(Level.FINE, "Table[{0}] filtered from Table[{1}]",
                                    new Object[]{newOiWavelength, oiWavelength});
                        }
                        resultFile.addOiTable(newOiWavelength);
                    }
                    mapOIWavelengths.put(oiWavelength, newOiWavelength);

                    // handle duplicated tables:
                    final ArrayList<OIWavelength> duplicatedOIWavelengths = dedupOIWavelengths.get(oiWavelength);
                    if (duplicatedOIWavelengths != null) {
                        for (OIWavelength duplicatedOIWavelength : duplicatedOIWavelengths) {
                            mapOIWavelengths.put(duplicatedOIWavelength, newOiWavelength);
                        }
                    }
                }
            }
            logger.log(Level.FINE, "insNames: {0}", Arrays.toString(resultFile.getAcceptedInsNames()));
            logger.log(Level.FINE, "mapOIWavelengths: {0}", mapOIWavelengths);
        }
    }

    /**
     * Process OIArray tables
     *
     * @param ctx merge context
     */
    private static void processOIArrays(final Context ctx) {
        // Browse all used tables, change name if already present in result, add to map [old table <=> new table]
        if (!ctx.usedOIArrays.isEmpty()) {
            final OIFitsFile resultFile = ctx.resultFile;
            final Map<OIArray, OIArray> mapOIArrays = ctx.mapOIArrays;

            // Reduce duplicated tables (distinct table to duplicates):
            final IdentityHashMap<OIArray, ArrayList<OIArray>> dedupOIArrays = new IdentityHashMap<>();
            // insname to tables:
            final LinkedHashMap<String, ArrayList<OIArray>> nameToDistinctOIArrays = new LinkedHashMap<>();

            deduplicateTables(OIFitsConstants.KEYWORD_ARRNAME, ctx.usedOIArrays, dedupOIArrays, nameToDistinctOIArrays);

            // Copy all distinct OIArray tables:
            for (ArrayList<OIArray> listOIArrays : nameToDistinctOIArrays.values()) {

                for (OIArray oiArray : listOIArrays) {
                    final String name = oiArray.getArrName();

                    // If name is already present in result, 
                    // change the name and memorise this change to update data later
                    String newName = name;
                    int idx = 0;
                    OIArray prevOiArray = null;

                    for (;;) {
                        prevOiArray = resultFile.getOiArray(newName);

                        if (prevOiArray == null) {
                            // table is not present
                            break;
                        }
                        // use another suffix (_nn):
                        idx++;
                        newName = name + "_" + idx;
                    }

                    final OIArray newOiArray;

                    if (prevOiArray != null) {
                        // use the previous table
                        newOiArray = prevOiArray;
                    } else {
                        // add the new table (copy):
                        newOiArray = (OIArray) resultFile.copyTable(oiArray);
                        newOiArray.setArrName(newName);

                        resultFile.addOiTable(newOiArray);
                    }
                    mapOIArrays.put(oiArray, newOiArray);

                    // handle duplicated tables:
                    final ArrayList<OIArray> duplicatedOIArrays = dedupOIArrays.get(oiArray);
                    if (duplicatedOIArrays != null) {
                        for (OIArray duplicatedOIArray : duplicatedOIArrays) {
                            mapOIArrays.put(duplicatedOIArray, newOiArray);
                        }
                    }
                }
            }
            logger.log(Level.FINE, "arrNames: {0}", Arrays.toString(resultFile.getAcceptedArrNames()));
            logger.log(Level.FINE, "mapOIArrays: {0}", mapOIArrays);
        }
    }

    /**
     * Process OICorr tables
     *
     * @param ctx merge context
     */
    private static void processOICorrs(final Context ctx) {
        // Browse all used tables, change name if already present in result, add to map [old table <=> new table]
        if (!ctx.usedOICorrs.isEmpty()) {
            final OIFitsFile resultFile = ctx.resultFile;
            final Map<OICorr, OICorr> mapOICorrs = ctx.mapOICorrs;

            // Browse all used OICorr tables:
            for (OICorr oiCorr : ctx.usedOICorrs) {
                final String name = oiCorr.getCorrName();

                // If name is already present in result, 
                // change the name and memorise this change to update data information later
                String newName = name;
                int idx = 0;
                while (resultFile.getOiCorr(newName) != null) {
                    idx++;
                    newName = name + "_" + idx;
                }

                final OICorr newOiCorr = (OICorr) resultFile.copyTable(oiCorr);
                newOiCorr.setCorrName(newName);
                resultFile.addOiTable(newOiCorr);

                mapOICorrs.put(oiCorr, newOiCorr);
            }
            logger.log(Level.FINE, "corrNames:  {0}", Arrays.toString(resultFile.getAcceptedCorrNames()));
            logger.log(Level.FINE, "mapOICorrs: {0}", mapOICorrs);
        }
    }

    /**
     *
     * @param ctx merge context
     */
    private static void processOIData(final Context ctx) {
        final SelectorResult selectorResult = ctx.selectorResult;
        final List<OIData> oiDatas = selectorResult.getSortedOIDatas();

        if (!oiDatas.isEmpty()) {
            logger.log(Level.FINE, "oiDatas: {0}", oiDatas);

            final OIFitsFile resultFile = ctx.resultFile;

            final Map<OIWavelength, OIWavelength> mapOIWavelengths = ctx.mapOIWavelengths;
            final Map<OIArray, OIArray> mapOIArrays = ctx.mapOIArrays;
            final Map<OICorr, OICorr> mapOICorrs = ctx.mapOICorrs;

            final Map<OITarget, Map<Short, Short>> mapOITargetIDs = ctx.mapOITargetIDs;

            // Process selected OIData tables:
            for (OIData oiData : oiDatas) {
                final String newInsName;
                final String newArrName;
                final String newCorrName;

                // INSNAME:
                final OIWavelength newOiWavelength = mapOIWavelengths.get(oiData.getOiWavelength());
                if (newOiWavelength == null) {
                    logger.log(Level.WARNING, "Invalid INSNAME[{0}] found !", oiData.getInsName());
                    continue;
                }
                newInsName = newOiWavelength.getInsName();

                // ARRNAME:
                final OIArray newOiArray = mapOIArrays.get(oiData.getOiArray());
                if (newOiArray == null) {
                    newArrName = UNDEFINED;
                    logger.log(Level.WARNING, "Invalid ARRNAME[{0}] found ! Using [{1}] instead",
                            new Object[]{oiData.getArrName(), newArrName});
                } else {
                    newArrName = newOiArray.getArrName();
                }

                // Optional CORRNAME:
                if (oiData.getCorrName() == null) {
                    newCorrName = null;
                } else {
                    final OICorr newOiCorr = mapOICorrs.get(oiData.getOiCorr());
                    if (newOiCorr == null) {
                        newCorrName = null;
                        logger.log(Level.WARNING, "Invalid CORRNAME[{0}] found !", oiData.getCorrName());
                    } else {
                        newCorrName = newOiCorr.getCorrName();
                    }
                }

                // check Targets:
                boolean checkTargetId = false;
                // Should filter targetId on each data row ?
                final Map<Short, Short> mapTargetIds = mapOITargetIDs.get(oiData.getOiTarget());

                for (Short id : oiData.getDistinctTargetId()) {
                    final Short newId = mapTargetIds.get(id);
                    if (newId != null) {
                        // targetId value are different between input and output tables:
                        if (!id.equals(newId)) {
                            checkTargetId = true;
                        }
                    }
                }

                logger.log(Level.FINE, "checkTargetId: {0}", checkTargetId);
                logger.log(Level.FINE, "mapIds:        {0}", mapTargetIds);

                // get the optional wavelength mask for the OIData's wavelength table:
                final IndexMask maskWavelength = selectorResult.getWavelengthMaskNotFull(oiData.getOiWavelength());
                // get the optional masks for this OIData table:
                final IndexMask maskOIData1D = selectorResult.getDataMask1DNotFull(oiData);
                final IndexMask maskOIData2D = selectorResult.getDataMask2DNotFull(oiData);

                logger.log(Level.FINE, "maskOIData1D:   {0}", maskOIData1D);
                logger.log(Level.FINE, "maskOIData2D:   {0}", maskOIData2D);
                logger.log(Level.FINE, "maskWavelength: {0}", maskWavelength);

                final int idxNone = (maskOIData2D != null) ? maskOIData2D.getIndexNone() : -1;
                final int idxFull = (maskOIData2D != null) ? maskOIData2D.getIndexFull() : -1;

                final Set<String> relatedColumnsFiltersOIData2D = selectorResult.getRelatedColumnsFiltersOIData2D();
                logger.log(Level.FINE, "relatedColumnsFiltersOIData2D: {0}", relatedColumnsFiltersOIData2D);

                // Copy table and filter out useless rows:
                final OIData newOIData = (OIData) resultFile.copyTable(oiData);

                // Change INSNAME, ARRNAME & CORRNAME keywords:
                newOIData.setArrName(newArrName);
                newOIData.setInsName(newInsName);
                newOIData.setCorrName(newCorrName);

                boolean filterRows = false;

                if (checkTargetId || (maskOIData1D != null) || (maskOIData2D != null) || (maskWavelength != null)) {
                    final int nRows = newOIData.getNbRows();
                    final int nWaves = newOIData.getNWave();

                    // prepare mask to indicate rows to keep in output table:
                    final BitSet maskRows = (maskOIData1D != null) ? maskOIData1D.getBitSet() : new BitSet(nRows);

                    // Update targetId column:
                    final short[] targetIds = newOIData.getTargetId();
                    final short[] newTargetIds = new short[nRows];

                    // Update filtered columns 2D:
                    final Map<String, double[][]> newColumns2D = new LinkedHashMap<String, double[][]>();

                    // get all related standard columns:
                    for (String columnName : relatedColumnsFiltersOIData2D) {
                        // clone standard 2D columns present in this table to set values to NaN (filtered out) below:
                        if (newOIData.getColumnDesc(columnName) != null) {
                            final double[][] prevColumnValue = newOIData.getColumnDoubles(columnName);
                            if (prevColumnValue != null) {
                                // TODO: check dims !
                                newColumns2D.put(columnName, (double[][]) ArrayFuncs.deepClone(prevColumnValue));
                            }
                        }
                    }

                    final String[] newColumns2DKeys = newColumns2D.keySet().toArray(new String[newColumns2D.size()]);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "newColumns2DKeys: {0}", Arrays.toString(newColumns2DKeys));
                    }

                    final double[][] newColumns2DRows = new double[newColumns2DKeys.length][];

                    // Iterate on table rows (i):
                    for (int i = 0; i < nRows; i++) {

                        // check optional data mask 1D:
                        if ((maskOIData1D != null) && !maskOIData1D.accept(i)) {
                            // if bit is false for this row, we hide this row
                            continue;
                        }

                        boolean skip = false;

                        // check mask 2D for row None flag:
                        IndexMask maskOIData2DRow = null;

                        if (maskOIData2D != null) {
                            if (maskOIData2D.accept(i, idxNone)) {
                                // row flagged as None:
                                skip = true;
                            }
                            // check row flagged as Full:
                            maskOIData2DRow = (skip || maskOIData2D.accept(i, idxFull)) ? null : maskOIData2D;
                        }

                        if (checkTargetId) {
                            final Short oldTargetId = Short.valueOf(targetIds[i]);
                            Short newTargetId = mapTargetIds.get(oldTargetId);
                            if (newTargetId == null) {
                                newTargetId = UNDEFINED_SHORT; // should never happen
                            }
                            newTargetIds[i] = newTargetId.shortValue();
                        } else {
                            // preserve id:
                            newTargetIds[i] = targetIds[i];
                        }
                        if (!skip && (newTargetIds[i] == ModelBase.UNDEFINED_SHORT)) {
                            skip = true;
                        }

                        // update mask:
                        if (skip) {
                            filterRows = true;
                            if (maskOIData1D != null) {
                                maskRows.set(i, false); // to be sure
                            }
                        } else if (maskOIData1D == null) {
                            maskRows.set(i);
                        }

                        if (!skip && (maskOIData2DRow != null)) {
                            for (int k = 0; k < newColumns2DKeys.length; k++) {
                                newColumns2DRows[k] = newColumns2D.get(newColumns2DKeys[k])[i];
                            }

                            // Iterate on wave channels (l):
                            for (int l = 0; l < nWaves; l++) {

                                // check optional wavelength mask:
                                if ((maskWavelength != null) && !maskWavelength.accept(l)) {
                                    // if bit is false for this row, we hide this row
                                    continue;
                                }

                                // check optional data mask 2D (and its Full flag):
                                if (!maskOIData2DRow.accept(i, l)) {
                                    // if bit is false for this row, we hide this row

                                    // set column value to NaN:
                                    for (int k = 0; k < newColumns2DRows.length; k++) {
                                        newColumns2DRows[k][l] = Double.NaN;
                                    }
                                }
                            } // wave channels
                        }
                    } // rows

                    // update targetId column before table filter:
                    newOIData.setTargetId(newTargetIds);

                    // Update filtered columns 2D before table filter:
                    for (int k = 0; k < newColumns2DKeys.length; k++) {
                        final String columnName = newColumns2DKeys[k];

                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Column[{0}] filtered: {1}",
                                    new Object[]{columnName,
                                                 Arrays.deepToString(newColumns2D.get(columnName))
                                    });
                        }
                        newOIData.setColumnValue(columnName, newColumns2D.get(columnName));
                    }

                    if (filterRows || (maskOIData1D != null) || (maskOIData2D != null) || (maskWavelength != null)) {
                        final int nKeepRows = maskRows.cardinality();

                        if (nKeepRows <= 0) {
                            // skip table as no remaining row
                            continue;
                        } else {
                            // redim the table to the correct row count to prune invalid rows:
                            newOIData.resizeTable(nKeepRows, maskRows,
                                    (maskWavelength != null) ? maskWavelength.getBitSet() : null);
                        }
                    }
                }
                resultFile.addOiTable(newOIData);

                if (filterRows) {
                    logger.log(Level.FINE, "Table[{0}] filtered from Table[{1}]",
                            new Object[]{newOIData, oiData});
                }
            }
        }
    }

    private static <K extends OITable> void deduplicateTables(final String keywordName, final Set<K> oiTables,
                                                              final IdentityHashMap<K, ArrayList<K>> dedupOITables,
                                                              final LinkedHashMap<String, ArrayList<K>> nameToDistinctOITables) {

        for (K oiTable : oiTables) {
            final String name = oiTable.getKeyword(keywordName);

            ArrayList<K> listOITables = nameToDistinctOITables.get(name);
            if (listOITables == null) {
                listOITables = new ArrayList<>();
                nameToDistinctOITables.put(name, listOITables);
            }

            K prevOiTable = null;

            if (!listOITables.isEmpty()) {

                for (K otherOiTable : listOITables) {
                    // check if the previous table is exactly the same ? (to remove duplicates)
                    if (OITableComparator.STRICT_COMPARATOR.compareTable(oiTable, otherOiTable)) {
                        // table is the same
                        prevOiTable = otherOiTable;
                        logger.log(Level.FINE, "Same tables: {0} vs {1}", new Object[]{oiTable, prevOiTable});
                        break;
                    }
                }
            }
            if (prevOiTable == null) {
                logger.log(Level.FINE, "New distinct table: {0}", oiTable);
                listOITables.add(oiTable);
            } else {
                // update table mapping:
                ArrayList<K> duplicatedOITables = dedupOITables.get(prevOiTable);
                if (duplicatedOITables == null) {
                    duplicatedOITables = new ArrayList<>();
                    dedupOITables.put(prevOiTable, duplicatedOITables);
                }
                duplicatedOITables.add(oiTable);
            }
        }
        logger.log(Level.FINE, "nameToDistinctOITables: {0}", nameToDistinctOITables);
        logger.log(Level.FINE, "dedupOITables: {0}", dedupOITables);
    }

    /**
     * Hold temporary data of merge operation
     */
    static final class Context {

        /** Selector result */
        final SelectorResult selectorResult;
        /** output OIFits */
        final OIFitsFile resultFile;
        /* Set of OIPrimaryHDU, OITarget, OIWavelength, OIArray and OICorr tables to process */
        final Set<OIPrimaryHDU> usedOIPrimaryHDU = new LinkedHashSet<OIPrimaryHDU>();
        final Set<OITarget> usedOITargets = new LinkedHashSet<OITarget>();
        final Set<OIWavelength> usedOIWavelengths = new LinkedHashSet<OIWavelength>();
        final Set<OIArray> usedOIArrays = new LinkedHashSet<OIArray>();
        final Set<OICorr> usedOICorrs = new LinkedHashSet<OICorr>();
        /** Map per OITarget between old targetIds (local) to new targetId (global) */
        final Map<OITarget, Map<Short, Short>> mapOITargetIDs = new IdentityHashMap<OITarget, Map<Short, Short>>();
        /* Map between old table to new tables for OIWavelength, OIArray and OICorr tables */
        final Map<OIWavelength, OIWavelength> mapOIWavelengths = new IdentityHashMap<OIWavelength, OIWavelength>();
        final Map<OIArray, OIArray> mapOIArrays = new IdentityHashMap<OIArray, OIArray>();
        final Map<OICorr, OICorr> mapOICorrs = new IdentityHashMap<OICorr, OICorr>();

        private Context(final SelectorResult selectorResult, final OIFitsFile resultFile) {
            this.selectorResult = selectorResult;
            this.resultFile = resultFile;
        }
    }

}
