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
import fr.jmmc.oitools.fits.FitsHDU;
import static fr.jmmc.oitools.fits.FitsHDU.UNDEFINED_EXT_NB;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.FileRef;
import fr.jmmc.oitools.meta.CellMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import static fr.jmmc.oitools.model.XmlOutputVisitor.encodeTagContent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains several static methods to validate the OIFits structure (keywords, columns).
 * @author bourgesl
 */
public final class OIFitsChecker {

    /** logger */
    private final static Logger logger = Logger.getLogger(OIFitsChecker.class.getName());

    public final static int COMPACT_MAX_VALUES = 10;

    /** enable checker flag */
    private static boolean ENABLE_CHECKER = "true".equalsIgnoreCase(System.getProperty("oitools.checker", "true"));

    public static boolean isEnableChecker() {
        return ENABLE_CHECKER;
    }

    public static void setEnableChecker(boolean flag) {
        ENABLE_CHECKER = flag;
    }

    /**
     * @return new OIFitsChecker instance if checker is enabled; null otherwise
     */
    public static OIFitsChecker newInstance() {
        if (ENABLE_CHECKER) {
            return new OIFitsChecker();
        }
        logger.info("disabled OIFITS validator ...");
        return null;
    }

    /** 'FILE' rules = applyTo & Rule name matcher */
    public final static String FILE_RULE = "FILE";

    public enum InspectMode {
        NORMAL,
        CASE_V2_IN_V1;
    }

    /** Inspect rule flag */
    private static boolean INSPECT_RULES = false;
    /** Inspect mode value */
    private static InspectMode INSPECT_MODE = InspectMode.NORMAL;

    /**
     * Return the Inspect rule flag
     * @return Inspect rule flag
     */
    public static boolean isInspectRules() {
        return INSPECT_RULES;
    }

    /**
     * Define the Inspect rule flag: true to collect rules and their applyTo (DataModel)
     * @param inspectRules Inspect rule flag
     */
    public static void setInspectRules(final boolean inspectRules) {
        INSPECT_RULES = inspectRules;
    }

    /**
     * Return the Inspect mode value
     * @return Inspect mode value
     */
    public static InspectMode getInspectMode() {
        return INSPECT_MODE;
    }

    /**
     * Define the Inspect mode value
     * @param InspectMode Inspect mode value
     */
    public static void setInspectMode(final InspectMode InspectMode) {
        INSPECT_MODE = InspectMode;
    }

    /**
     * Return true if the given rule should be ignored by ruleFailed(Rule)
     * @param rule Rule to check
     * @return true if the given rule should be ignored
     */
    private static boolean shouldSkipRule(final Rule rule) {
        switch (INSPECT_MODE) {
            case CASE_V2_IN_V1:
                return (rule != Rule.OIFITS_TABLE_NOT_V2);
            default:
            case NORMAL:
                return false;
        }
    }

    /* members */
    /** current FileRef */
    private FileRef fileRef = null;

    /** flag to skip keyword / column format checks (loading OIFITS) */
    private boolean skipFormat = false;

    /** DataLocation mapping keyed by RuleFailure */
    private final Map<RuleFailure, DataLocation> failures;

    /** OIFitsStandard mapping keyed by FileRef */
    private final Map<FileRef, OIFitsStandard> fileRefStandards = new HashMap<FileRef, OIFitsStandard>();

    /** OIFITS2: temporary state to check correlation indexes (OIFitsCorrChecker) keyed by CORRNAME */
    private final Map<String, OIFitsCorrChecker> corrCheckers = new HashMap<String, OIFitsCorrChecker>();

    /**
     * Public constructor
     */
    public OIFitsChecker() {
        this.failures = new LinkedHashMap<RuleFailure, DataLocation>();
    }

    /**
     * Define the severity of all rules according to the given profile.
     * @param profile severity profile to use
     */
    void defineSeverity(final SeverityProfile profile) {
        logger.log(java.util.logging.Level.FINE, "defineSeverity: {0}", profile);

        // first check rule is complete:
        for (Map.Entry<RuleFailure, DataLocation> entry : failures.entrySet()) {
            final RuleFailure failure = entry.getKey();
            final DataLocation datas = entry.getValue();

            if (datas.isEmpty()) {
                // ensure data is non empty:
                failure.getRule().checkDataType(RuleDataType.NONE);
            }
        }

        for (RuleFailure failure : failures.keySet()) {
            final OIFitsStandard std = fileRefStandards.get(failure.getFileRef());

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "defineSeverity[{0}] for std = {1}", new Object[]{failure.getRule().name(), std});
            }

            profile.defineSeverity(failure, std);
        }
        logger.fine("defineSeverity: done");
    }

    /**
     * Reset
     */
    public void reset() {
        cleanup();
        clearCheckReport();
    }

    /**
     * Clear the failures
     */
    public void clearCheckReport() {
        failures.clear();
    }

    /**
     * Clear the temporary state (cleanup)
     */
    void cleanup() {
        setFileRef(null, null);
        setSkipFormat(false);
        fileRefStandards.clear();
        corrCheckers.clear();

        /* prune failure storage */
        for (Map.Entry<RuleFailure, DataLocation> entry : failures.entrySet()) {
            if (entry.getValue().isEmpty()) {
                entry.setValue(DataLocation.EMPTY);
            } else {
                entry.getValue().cleanup();
            }
        }
    }

    /**
     * Define the applyTo information and standard of the given rule (Inspect rule enabled)
     * @param rule failed rule
     * @param applyTo apply To information
     * @param standard OIFitsStandard information
     */
    void inspectRuleFailed(final Rule rule, final String applyTo, final OIFitsStandard standard) {
        if (isInspectRules() && !shouldSkipRule(rule)) {
            rule.addApplyTo(applyTo);
            if (standard != null) {
                rule.addStandard(standard);
            }
        }
    }

    /**
     * Define the applyTo information and standard of the given rule 
     * AND the applyRules of the given data model element (Inspect rule enabled)
     * @param rule failed rule
     * @param hdu FitsHDU instance (may be null)
     * @param member member (keyword/column) name (may be null)
     */
    private void inspectRuleFailed(final Rule rule, final FitsHDU hdu, final String member) {
        if (isInspectRules()) {
            if (hdu != null) {
                final CellMeta meta = (member != null) ? getMeta(hdu, member) : null;

                // Update rule to indicate applyTo / standard:
                String applyTo = ((hdu.getExtName() == null) ? OIFitsConstants.PRIMARY_HDU : hdu.getExtName());
                if (meta != null) {
                    applyTo += "." + meta.getName();
                }

                final OIFitsStandard standard = getStandard(hdu);

                inspectRuleFailed(rule, applyTo, standard);

                // Update meta data to indicate rules:
                if (meta != null) {
                    meta.getApplyRules().add(rule);
                } else {
                    hdu.getApplyRules().add(rule);
                }
            } else {
                /* Special case when the hdu has not been defined yet. 
                 * Only applies to the file and structure checks (FILE_* & OIFITS_* rules) */
                if (rule.name().startsWith("FILE") || rule.name().startsWith("OIFITS")) {

                    if (!rule.getApplyTo().contains(FILE_RULE) && !rule.getApplyTo().isEmpty()) {
                        logger.log(Level.SEVERE, "BAD inspectRuleFailed applyTo conflict for ''{0}'': {1}", new Object[]{rule.name(), rule.getApplyTo()});
                    }
                    if (!rule.name().endsWith("_V2")) {
                        inspectRuleFailed(rule, FILE_RULE, OIFitsStandard.VERSION_1);
                    }
                    inspectRuleFailed(rule, FILE_RULE, OIFitsStandard.VERSION_2);
                } else {
                    throw new IllegalStateException("error in the rules collection system, hdu cannot be null");
                }
            }
        }
    }

    /**
     * Create and store the RuleFailure for the given information and return its related DataLocation
     * @param rule failed rule
     * @return related DataLocation
     */
    DataLocation ruleFailed(final Rule rule) {
        return ruleFailed(rule, null, null);
    }

    /**
     * Create and store the RuleFailure for the given information and return its related DataLocation
     * @param rule failed rule
     * @param hdu FitsHDU instance (may be null)
     * @return related DataLocation
     */
    DataLocation ruleFailed(final Rule rule, final FitsHDU hdu) {
        return ruleFailed(rule, hdu, null);
    }

    /**
     * Create and store the RuleFailure for the given information and return its related DataLocation
     * @param rule failed rule
     * @param hdu FitsHDU instance (may be null)
     * @param member member (keyword/column) name (may be null)
     * @return related DataLocation
     */
    public DataLocation ruleFailed(final Rule rule, final FitsHDU hdu, final String member) {
        if (shouldSkipRule(rule)) {
            return new DataLocation(rule);
        }

        final RuleFailure failure = createFailure(rule, hdu, member);

        DataLocation datas = failures.get(failure);

        // exist ?
        if (datas == null) {
            //Just the first time
            if (isInspectRules()) {
                inspectRuleFailed(rule, hdu, member);
            }
            datas = new DataLocation(rule);
            failures.put(failure, datas);
        }
        return datas;
    }

    /**
     * Create and store the RuleFailure for the given information and return its related DataLocation
     * ONLY used by OIFITS_TABLE_UNKNOWN (TODO KILL)
     * @param rule failed rule
     * @param extName Hdu name
     * @param extNb hdu number
     * @return related DataLocation
     */
    public DataLocation ruleFailed(final Rule rule, final String extName, final int extNb) {
        if (shouldSkipRule(rule)) {
            return new DataLocation(rule);
        }
        final RuleFailure ruleFail = new RuleFailure(rule, fileRef, extName, extNb, null);

        DataLocation datas = failures.get(ruleFail);

        // exist ?
        if (datas == null) {
            // already done in code:
            /*            
            if (isInspectRules()) {
                inspectRuleFailed(rule, hdu, member);
            }
             */
            datas = new DataLocation(rule);
            failures.put(ruleFail, datas);
        }
        return datas;
    }

    /**
     * Return true if the corresponding RuleFailure is already present
     * @param rule failed rule 
     * @param hdu FitsHDU instance (may be null)
     * @param member member (keyword/column) name (may be null)
     * @return true if the corresponding RuleFailure is already present
     */
    public boolean hasRule(final Rule rule, final FitsHDU hdu, final String member) {
        final RuleFailure failure = createFailure(rule, hdu, member);

        return (failures.get(failure) != null);
    }

    private RuleFailure createFailure(final Rule rule, final FitsHDU hdu, final String member) {
        return new RuleFailure(rule, fileRef, ((hdu == null) ? null : hdu.getExtName()),
                ((hdu == null) ? UNDEFINED_EXT_NB : hdu.getExtNb()),
                member);
    }

    private static CellMeta getMeta(final FitsHDU hdu, final String member) {
        final CellMeta meta = hdu.getKeywordsDesc(member);
        if (meta != null) {
            return meta;
        }
        if (hdu instanceof FitsTable) {
            return ((FitsTable) hdu).getColumnDesc(member);
        }
        return null;
    }

    private static OIFitsStandard getStandard(final FitsHDU hdu) {
        if (hdu instanceof OITable) {
            return ((OITable) hdu).getOIFitsFile().getVersion();
        }
        if (hdu instanceof OIPrimaryHDU) {
            return OIFitsStandard.VERSION_2;
        }
        // For HDU:
        return null;
    }

    public boolean isEmpty() {
        return this.failures.isEmpty();
    }

    /**
     * Get number of failures with severity = Warning
     * @return number of failures with severity = Warning
     */
    public int getNbWarnings() {
        int warnings = 0;

        for (RuleFailure failure : failures.keySet()) {
            if (Severity.Warning == failure.getSeverity()) {
                warnings++;
            }
        }
        return warnings;
    }

    /**
     * Get number of failures with severity = Error
     * @return number of failures with severity = Error
     */
    public int getNbSeveres() {
        int severes = 0;

        for (RuleFailure failure : failures.keySet()) {
            if (Severity.Error == failure.getSeverity()) {
                severes++;
            }
        }
        return severes;
    }

    /**
     * Return a string that gives numbers of warnings and errors.
     *
     * @return a string with number of warnings and severe errors.
     */
    public String getCheckStatus() {
        return getNbWarnings() + " warnings, " + getNbSeveres() + " severe errors";
    }

    /**
     * Get the sorted map of failures according to the given comparator
     * @param comparator RuleFailureComparator instance
     * @return sorted map of failures
     */
    private Map<RuleFailure, DataLocation> getSortedFailures(final RuleFailureComparator comparator) {
        final ArrayList<RuleFailure> keys = new ArrayList<RuleFailure>(failures.keySet());

        Collections.sort(keys, comparator);

        // create a new LinkedHashMap to store sorted entries
        final LinkedHashMap<RuleFailure, DataLocation> sorted = new LinkedHashMap<RuleFailure, DataLocation>();

        for (RuleFailure failure : keys) {
            sorted.put(failure, failures.get(failure));
        }

        return sorted;
    }

    /**
     * Return the failures (ASCII) sorted by the default comparator as String.
     * @return string containing sorted failures (ASCII)
     */
    public String getFailuresAsString() {
        return appendFailuresAsString(new StringBuilder(failures.size() * 80), RuleFailureComparator.DEFAULT).toString();
    }

    /**
     * Append the failures (ASCII) sorted by the given comparator into the given buffer.
     * @param sb buffer to append into
     * @param comparator RuleFailureComparator instance
     * @return sb given buffer
     */
    public StringBuilder appendFailuresAsString(final StringBuilder sb, final RuleFailureComparator comparator) {
        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            entry.getKey().toString(sb);
            entry.getValue().toString(sb);
            sb.append('\n');
        }
        return sb;
    }

    /**
     * Get the failures report sorted by the default comparator in FULL mode
     * @return string containing the analysis report
     */
    public String getCheckReport() {
        return getCheckReport(false);
    }

    /**
     * Get the failures report sorted by the default comparator
     * @param compact flag to use COMPACT mode (true) or FULL mode (false)
     * @return string containing the analysis report
     */
    public String getCheckReport(final boolean compact) {
        return appendFailuresReport(compact, new StringBuilder(failures.size() * 80), RuleFailureComparator.DEFAULT).toString();
    }

    /**
     * Append the failures report sorted by the given comparator into the given buffer.
     * @param compact flag to use COMPACT mode (true) or FULL mode (false)
     * @param sb buffer to append into
     * @param comparator RuleFailureComparator instance
     * @return a string containing the analysis report
     */
    public StringBuilder appendFailuresReport(final boolean compact, final StringBuilder sb, final RuleFailureComparator comparator) {
        sb.append(getCheckStatus()).append("\n\n");

        FileRef last = null;
        int lastExtNb = -1;

        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            final RuleFailure failure = entry.getKey();
            final DataLocation datas = entry.getValue();

            if (last != failure.getFileRef()) {
                last = failure.getFileRef();
                sb.append("Analysing File: ").append(last.getStringId()).append('\n');
            }
            if (lastExtNb != failure.getExtNb()) {
                lastExtNb = failure.getExtNb();
                sb.append("Analysing ");
                sb.append((failure.getExtName() == null || failure.getExtName().isEmpty()) ? "HDU [" : "table [");
                ModelBase.getHDUId(sb, failure.getExtName(), failure.getExtNb()).append("]\n");
            }

            if (compact && !failure.getSeverity().isHigher(Severity.Information)) {
                // in compact mode, skip failures whose severity is lower or equal to INFO:
                continue;
            }

            final String ruleMessage = failure.formatMessage();

            if (datas.isEmpty()) {
                sb.append(failure.getSeverity()).append('\t');
                sb.append(ruleMessage).append('\n');
            } else {
                final int len = datas.getValues().size();
                int outLen = len;
                if (compact || !failure.getSeverity().isHigher(Severity.Information)) {
                    outLen = Math.min(COMPACT_MAX_VALUES, len);
                }
                for (int i = 0; i < outLen; i++) {
                    sb.append(failure.getSeverity()).append('\t');
                    sb.append(datas.formatMessage(ruleMessage, i)).append('\n');
                }
                if (outLen < len) {
                    sb.append("... (").append(len).append(" occurences)\n");
                }
            }
        }
        return sb;
    }

    /**
     * Return the failures (XML) sorted by the default comparator as String.
     * @return string containing sorted failures (XML)
     */
    public String getFailuresAsXML() {
        return appendFailuresAsXML(new StringBuilder(failures.size() * 256)).toString();
    }

    /**
     * Append the failures (XML) sorted by the default comparator into the given buffer.
     * XML display of the error. Calling the XML of the Key and Value
     * @param sb buffer to append into
     * @return sb given buffer
     */
    public StringBuilder appendFailuresAsXML(final StringBuilder sb) {
        return appendFailuresAsXML(sb, RuleFailureComparator.DEFAULT);
    }

    /**
     * Append the failures (XML) sorted by the given comparator into the given buffer.
     * @param sb buffer to append into
     * @param comparator RuleFailureComparator instance
     * @return sb given buffer
     */
    public StringBuilder appendFailuresAsXML(final StringBuilder sb, final RuleFailureComparator comparator) {
        sb.append("<failures>\n");

        // Dump failures:
        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            final RuleFailure failure = entry.getKey();
            final DataLocation datas = entry.getValue();

            sb.append("  <failure>\n");
            failure.toXML(sb);

            final String ruleMessage = entry.getKey().formatMessage();

            if (datas.isEmpty()) {
                sb.append("    <message>");
                sb.append(encodeTagContent(ruleMessage));
                sb.append("</message>\n");
            } else {
                datas.toXML(sb, ruleMessage, datas);
            }

            sb.append("  </failure>\n");
        }
        sb.append("</failures>\n");
        return sb;
    }

    /**
     * Return the rules only used by failures
     * @return rule set (unordered)
     */
    Set<Rule> getRulesUsedByFailures() {
        final Set<Rule> ruleSet = new HashSet<Rule>();

        for (RuleFailure key : failures.keySet()) {
            ruleSet.add(key.getRule());
        }

        return ruleSet;
    }

    /**
     * Write rules only used by failures (XML format)
     * @param sb buffer to append into
     */
    void appendRulesUsedByFailures(final StringBuilder sb) {
        final Set<Rule> ruleSet = getRulesUsedByFailures();

        final Rule[] rules = new Rule[ruleSet.size()];
        ruleSet.toArray(rules);
        Arrays.sort(rules);

        sb.append("<rules>\n");
        for (Rule rule : rules) {
            rule.toXml(sb, null);
        }
        sb.append("</rules>\n");
    }

    /**
     * Return or create the OIFitsCorrChecker instance given the CORRNAME value
     * @param corrName CORRNAME value
     * @return OIFitsCorrChecker instance
     */
    OIFitsCorrChecker getCorrChecker(final String corrName) {
        OIFitsCorrChecker corrChecker = corrCheckers.get(corrName);
        if (corrChecker == null) {
            corrChecker = new OIFitsCorrChecker();
            corrCheckers.put(corrName, corrChecker);
        }
        return corrChecker;
    }

    /**
     * Return the current FileRef
     * @return current FileRef
     */
    FileRef getFileRef() {
        return fileRef;
    }

    /**
     * Set the current FileRef and standard
     * @param fileRef FileRef instance
     * @param std OIFitsStandard
     */
    void setFileRef(final FileRef fileRef, final OIFitsStandard std) {
        this.fileRef = fileRef;
        if (std != null) {
            this.fileRefStandards.put(fileRef, std);
        }
    }

    /**
     * Return the flag to skip keyword / column format checks
     * @return flag to skip keyword / column format checks
     */
    public boolean isSkipFormat() {
        return skipFormat;
    }

    void setSkipFormat(boolean skipFormat) {
        this.skipFormat = skipFormat;
    }

}
