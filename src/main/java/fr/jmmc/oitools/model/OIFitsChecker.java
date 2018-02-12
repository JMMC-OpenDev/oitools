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
 * This class contains several static methods to validate the OIFits structure (keywords, columns)
 * Management and creation of the rules.
 * Creation and storage of all the objects necessary for the new management of the failures. (String/XML)
 * @author bourgesl
 */
public final class OIFitsChecker {

    enum InspectMode {
        NORMAL,
        CASE_V2_IN_V1;
    }

    /** InspectRule flag */
    private static boolean INSPECT_RULES = false;
    /** InspectMode description */
    private static InspectMode INSPECT_MODE = InspectMode.NORMAL;

    /**
     * Give information if we are in rules inspection
     * @return INSPECT_RULES true if collection of rules active
     */
    public static boolean isInspectRules() {
        return INSPECT_RULES;
    }

    /**
     * Set information if we want to go into inspection rules (DataModel)
     * @param COLLECT_RULES true if collection of rules active
     */
    public static void setInspectRules(boolean COLLECT_RULES) {
        INSPECT_RULES = COLLECT_RULES;
    }

    /**
     * Give information if we are in inspection mode (shouldSkipRule() spécial case)
     * @return INSPECT_MODE true if inspection mode active
     */
    public static InspectMode getInspectMode() {
        return INSPECT_MODE;
    }

    /**
     * Set information if we want to go into inspection mode (DataModel)
     * @param InspectMode true if inspection mode active
     */
    public static void setInspectMode(InspectMode InspectMode) {
        INSPECT_MODE = InspectMode;
    }

    /**
     * Filter the writing of the rules according to the cases
     * @param rule Rule
     * @return filter with all informations to filter the rules to write
     */
    public static boolean shouldSkipRule(Rule rule) {
        switch (INSPECT_MODE) {
            case CASE_V2_IN_V1:
                return (rule != Rule.TABLE_NOT_OIFITS2);
            default:
            case NORMAL:
                return false;
        }
    }

    private final static Logger logger = Logger.getLogger(OIFitsChecker.class.getName());

    /* members */
    /** Save fileRef */
    private FileRef fileRef = null;

    /** flag to skip keyword / column format checks (loading OIFITS) */
    private boolean skipFormat = false;

    /** Map to storing all objects for failures handling */
    private final Map<RuleFailure, DataLocation> failures;

    /** Standard mapping */
    private final Map<FileRef, OIFitsStandard> fileRefStandards = new HashMap<FileRef, OIFitsStandard>();

    /** OIFITS2: temporary state to check correlation indexes keyed by CORRNAME */
    private final Map<String, OIFitsCorrChecker> corrCheckers = new HashMap<String, OIFitsCorrChecker>();

    /**
     * Public constructor
     */
    public OIFitsChecker() {
        this.failures = new LinkedHashMap<RuleFailure, DataLocation>();
    }

    /**
     * Define the severity of all the rules of the map according to the chosen profile.
     * @param profile define severity profil for all rules
     */
    void defineSeverity(final SeverityProfile profile) {
        logger.log(java.util.logging.Level.FINE, "defineSeverity: {0}", profile);

        // first check rule is complete:
        for (Map.Entry<RuleFailure, DataLocation> entry : failures.entrySet()) {
            if (entry.getValue().isEmpty()) {
                // ensure data is non empty:
                entry.getKey().getRule().checkDataType(RuleDataType.NONE);
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
     * Clear the map
     */
    void cleanup() {
        setFileRef(null, null);
        setSkipFormat(false);
        fileRefStandards.clear();
        corrCheckers.clear();
    }

    /**
     * Management of the applyTo and standard
     * TODO do private at the end
     * @param rule rule informations
     * @param applyTo String for rule apply To
     * @param standard OIFitsStandard information
     */
    public void inspectRuleFailed(final Rule rule, final String applyTo, final OIFitsStandard standard) {
        if (isInspectRules() && !shouldSkipRule(rule)) {
            rule.addApplyTo(applyTo);
            if (standard != null) {
                rule.addStandard(standard);
            }
        }
    }

    /**
     * Management and creation of the rules and applyTo.
     * @param rule rule informations
     * @param hdu HDU informations
     * @param member member (keyword/column) name
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
                Only applies to the file existence and read cases */
                if (rule.name().startsWith("FILE") || rule.name().startsWith("OIFITS")) {

                    if (!rule.getApplyTo().contains("FILE") && !rule.getApplyTo().isEmpty()) {
                        System.out.println("BAD inspectRuleFailed applyTo conflict for '" + rule.name() + "': " + rule.getApplyTo());
                    }

                    inspectRuleFailed(rule, "FILE", OIFitsStandard.VERSION_1);
                    inspectRuleFailed(rule, "FILE", OIFitsStandard.VERSION_2);
                } else {
                    throw new IllegalStateException("error in the rules collection system, hdu cannot be null");
                }
            }
        }
    }

    /**
     * Helper when we just have the file information
     * @param rule rule informations
     * @return DataLocation
     */
    DataLocation ruleFailed(final Rule rule) {
        return ruleFailed(rule, null, null);
    }

    /**
     * Helper when we just have the file and hdu information
     * @param rule rule informations
     * @param hdu HDU informations
     * @return DataLocation
     */
    DataLocation ruleFailed(final Rule rule, final FitsHDU hdu) {
        return ruleFailed(rule, hdu, null);
    }

    /**
     * Create and store the RuleFailure and return its related DataLocation
     * @param rule rule informations
     * @param hdu HDU informations
     * @param member member (keyword/column) name
     * @return DataLocation
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
     * Exceptional version of catch fail for no hdu information but extname and extNB possible
     * @param rule rule informations
     * @param extName String for Hdu Name
     * @param extNb int for Hdu Number
     * @return DataLocation
     */
    public DataLocation ruleFailed(final Rule rule, final String extName, final int extNb) {

        if (shouldSkipRule(rule)) {
            return new DataLocation(rule);
        }
        // TODO: KILL that ruleFailred variant
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
     * @param rule rule information
     * @param hdu HDU information
     * @param member member (keyword/column) name
     * @return true if the corresponding RuleFailure is already present
     */
    public boolean hasRule(final Rule rule, final FitsHDU hdu, final String member) {
        final RuleFailure failure = createFailure(rule, hdu, member);

        return (failures.get(failure) != null);
    }

    private RuleFailure createFailure(final Rule rule, final FitsHDU hdu, final String member) {
        return new RuleFailure(rule, fileRef, ((hdu == null) ? null : hdu.getExtName()),
                ((hdu == null) ? UNDEFINED_EXT_NB : hdu.getExtNb()), member);
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

    /**
     * Get number of warning errors
     * @return number of warning errors
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
     * Get number of severe errors
     * @return number of severe errors
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
     * Return a simple string that show numbers of warnings and severe errors.
     *
     * @return a string with number of warnings and severe errors.
     */
    public String getCheckStatus() {
        return getNbWarnings() + " warnings, " + getNbSeveres() + " severe errors";
    }

    /**
     * Get the sorted map of failures according to the given comparator
     * @param comparator RuleFailureComparator instance
     * @return failures
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
     * String display of the error. Calling the String of the Key and Value
     * @return sb
     */
    public String getFailuresAsString() {
        return getFailuresAsString(new StringBuilder(failures.size() * 80), RuleFailureComparator.DEFAULT).toString();
    }

    /**
     * String display of the error. Calling the String of the Key and Value
     * @param sb StringBuilder
     * @param comparator RuleFailureComparator instance
     * @return sb
     */
    public StringBuilder getFailuresAsString(final StringBuilder sb, final RuleFailureComparator comparator) {
        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            entry.getKey().toString(sb);
            entry.getValue().toString(sb);
            sb.append('\n');
        }
        return sb;
    }

    /**
     * Get the checker's report
     *
     * @return a string containing the analysis report
     */
    public String getCheckReport() {
        return getFailuresReport(new StringBuilder(failures.size() * 80), RuleFailureComparator.DEFAULT).toString();
    }

    /**
     * Get the Failure report
     * @param sb StringBuilder
     * @param comparator RuleFailureComparator instance
     * @return a string containing the analysis report
     */
    public StringBuilder getFailuresReport(final StringBuilder sb, final RuleFailureComparator comparator) {
        FileRef last = null;
        int lastExtNb = -1;

        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            final RuleFailure failure = entry.getKey();

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

            final String ruleMessage = failure.formatMessage();

            if (entry.getValue().isEmpty()) {
                sb.append(failure.getSeverity()).append('\t');
                sb.append(ruleMessage).append('\n');
            } else {
                for (int i = 0, len = entry.getValue().getValues().size(); i < len; i++) {
                    sb.append(failure.getSeverity()).append('\t');
                    sb.append(entry.getValue().formatMessage(ruleMessage, i)).append('\n');
                }
            }
        }
        sb.append('\n').append(getCheckStatus());

        return sb;
    }

    /**
     * XML display of the error. Calling the XML of the Key and Value
     * @return sb
     */
    public String getFailuresAsXML() {
        return getFailuresAsXML(new StringBuilder(failures.size() * 256)).toString();
    }

    /**
     * XML display of the error. Calling the XML of the Key and Value
     * @param sb StringBuilder
     * @return sb 
     */
    public StringBuilder getFailuresAsXML(final StringBuilder sb) {
        return getFailuresAsXML(sb, RuleFailureComparator.DEFAULT);
    }

    /**
     * XML display of the error. Calling the XML of the Key and Value
     * @param sb StringBuilder
     * @param comparator RuleFailureComparator instance
     * @return sb 
     */
    public StringBuilder getFailuresAsXML(final StringBuilder sb, final RuleFailureComparator comparator) {
        sb.append("<failures>\n");
        sb.append("  <profile>\n");
        // TODO
        sb.append("  </profile>\n");

        // Dump failures:
        for (Map.Entry<RuleFailure, DataLocation> entry : getSortedFailures(comparator).entrySet()) {
            sb.append("  <failure>\n");
            entry.getKey().toXML(sb);

            final String ruleMessage = entry.getKey().formatMessage();

            if (entry.getValue().isEmpty()) {
                sb.append("    <message>");
                sb.append(encodeTagContent(ruleMessage));
                sb.append("</message>\n");
            } else {
                entry.getValue().toXML(sb, ruleMessage, entry.getValue());
            }

            sb.append("  </failure>\n");
        }
        sb.append("</failures>\n");
        return sb;
    }

    /**
     * Write a List of Rules
     * @return ruleSet Set
     */
    public Set<Rule> getRulesUsedByFailures() {
        final Set<Rule> ruleSet = new HashSet<Rule>();

        for (RuleFailure key : failures.keySet()) {
            ruleSet.add(key.getRule());
        }

        return ruleSet;
    }

    /**
     * Write Rules used by failures (XML format)
     * @param sb
     */
    public void writeRulesUsedByFailures(final StringBuilder sb) {
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
     * Clear the validation messages
     */
    public void clearCheckReport() {
        failures.clear();
    }

    /**
     * Give OIFitsCorrChecker map
     * @param corrname
     * @return corrChecker
     */
    public OIFitsCorrChecker getCorrChecker(final String corrname) {
        OIFitsCorrChecker corrChecker = corrCheckers.get(corrname);
        if (corrChecker == null) {
            corrChecker = new OIFitsCorrChecker();
            corrCheckers.put(corrname, corrChecker);
        }
        return corrChecker;
    }

    /**
     * Get FileRef object
     * @return fileRef
     */
    public FileRef getFileRef() {
        return fileRef;
    }

    /**
     * Set FileRef value and standard
     * @param fileRef reference on the file
     * @param std OIFitsStandard
     */
    void setFileRef(final FileRef fileRef, final OIFitsStandard std) {
        this.fileRef = fileRef;
        if (std != null) {
            this.fileRefStandards.put(fileRef, std);
        }
    }

    /**
     * Method to skip format checking when it's done the first time
     * @return skipFormat boolean
     */
    public boolean isSkipFormat() {
        return skipFormat;
    }

    void setSkipFormat(boolean skipFormat) {
        this.skipFormat = skipFormat;
    }

}
