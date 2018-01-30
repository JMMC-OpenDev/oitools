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

import fr.jmmc.oitools.meta.OIFitsStandard;
import static fr.jmmc.oitools.model.XmlOutputVisitor.encodeTagContent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Enumeration for creating the rules
 * @author kempsc
 */
public enum Rule {

    ARRNAME_UNIQ(
            "check if a single OI_ARRAY table corresponds to the ARRNAME keyword",
            "V2.5.2§1",
            RuleDataType.VALUE_EXPECTED,
            "OI_ARRAY tables [{{EXPECTED}}] are identified by same ARRNAME='{{VALUE}}'"
    ),
    COL_UNKNOWN(
            "check if the column belongs to the OIFITS standard and version",
            Const.JMMC,
            RuleDataType.VALUE,
            "Skipping non-standard OIFITS column '{{HDU}}.{{MEMBER}}' with format [{{VALUE}}]"
    ),
    CORRNAME_REF(
            "check if an OI_CORR table matches the CORRNAME keyword",
            "V2.6.1§3",
            RuleDataType.VALUE,
            "Missing OI_CORR table that describes the '{{VALUE}}' correlation"
    ),
    CORRNAME_UNIQ(
            "check if a single OI_CORR table corresponds to the CORRNAME keyword",
            "V2.7.2§4",
            RuleDataType.VALUE_EXPECTED,
            "OI_CORR tables [{{EXPECTED}}] are identified by same CORRNAME='{{VALUE}}'"
    ),
    FILE_EXIST(
            "check if the file exist",
            Const.JMMC,
            RuleDataType.NONE,
            "File not found: {{FILE}}"
    ),
    FILE_LOAD("check if the OIFITS file is loaded properly (IO error)",
            Const.JMMC,
            RuleDataType.NONE,
            "Unable to load the file {{FILE}}"
    ),
    INSNAME_REF("check if an OI_WAVELENGTH table matches the INSNAME keyword",
            "V2.6.1§3",
            RuleDataType.VALUE,
            "Missing OI_WAVELENGTH table that describes the '{{VALUE}}' instrument"
    ),
    INSNAME_UNIQ("check if a single OI_WAVELENGTH table corresponds to the INSNAME keyword",
            "V1.6.3.1",
            RuleDataType.VALUE_EXPECTED,
            "OI_WAVELENGTH tables [{{EXPECTED}}] are identified by same INSNAME='{{VALUE}}'"
    ),
    GENERIC_ARRNAME_REF("check if an OI_ARRAY table matches the ARRNAME keyword",
            "V2.6.1§3",
            RuleDataType.VALUE,
            "Missing OI_ARRAY table that describes the '{{VALUE}}' array"
    ),
    GENERIC_COL_DIM("check if the dimension of column values >= 1",
            Const.JMMC,
            RuleDataType.NONE,
            "Can't check repeat for column '{{MEMBER}}'"
    ),
    GENERIC_COL_ERR("check if the UNFLAGGED *ERR column values are valid (positive or NULL)",
            Const.JMMC,
            RuleDataType.VALUE_ROW_COL,
            "Invalid value at index {{COL}} for column '{{MEMBER}}' line {{ROW}}, found '{{VALUE}}' should be >= 0 or NaN or flagged out"
    ),
    GENERIC_COL_FORMAT("check if the column format matches the expected format (data type and dimensions)",
            "V2.4§1",
            RuleDataType.VALUE_EXPECTED,
            "Invalid format for column '{{MEMBER}}', found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_COL_MANDATORY("check if the required column is present",
            "V1-V2.Tables",
            RuleDataType.NONE,
            "Missing column '{{MEMBER}}'"
    ),
    GENERIC_COL_NBROWS("check if the column length matches the expected number of rows",
            Const.JMMC,
            RuleDataType.VALUE_EXPECTED,
            "Invalid length for column '{{MEMBER}}', found {{VALUE}} row(s) should be {{EXPECTED}} row(s)"
    ),
    GENERIC_COL_UNIT("check if the column unit matches the expected unit",
            "V2.4§2",
            RuleDataType.VALUE_EXPECTED,
            "Invalid unit for column '{{MEMBER}}', found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_COL_UNIT_EXIST("check if the column unit exists",
            "V2.4§2",
            RuleDataType.VALUE,
            "Missing unit for column '{{MEMBER}}', should be not empty"
    ),
    GENERIC_COL_VAL_ACCEPTED_INT("check if column values match the 'accepted' values (integer)",
            "V1-V2.Tables",
            RuleDataType.VALUE_EXPECTED_ROW_COL,
            "Invalid value for column '{{MEMBER}}' at index {{COL}}, line {{ROW}}, found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_COL_VAL_ACCEPTED_STR("check if column values match the 'accepted' values (string)",
            "V1-V2.Tables",
            RuleDataType.VALUE_EXPECTED_ROW,
            "Invalid value for column '{{MEMBER}}' line {{ROW}}, found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_COL_VAL_POSITIVE("check if column values are finite and positive",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "Invalid value for column '{{MEMBER}}' line {{ROW}}, found '{{VALUE}}'  should be >= 0"
    ),
    GENERIC_CORRINDX_MIN("check if the CORRINDX values >= 1",
            "V2.7.2§4",
            RuleDataType.VALUE_ROW,
            "{{MEMBER}} index {{VALUE}} cannot be < 1 at row {{ROW}}"
    ),
    GENERIC_CORRINDX_MAX("check if the CORRINDX values <= NDATA",
            "V2.7.2§4",
            RuleDataType.VALUE_LIMIT_ROW,
            "{{MEMBER}} index {{VALUE}} cannot be > NDATA [{{LIMIT}}] at row {{ROW}}"
    ),
    GENERIC_CORRINDX_UNIQ("check duplicates or overlaps within correlation indexes (CORRINDX)",
            "V2.7.2§4",
            RuleDataType.VALUE_ROW_COL_DETAILS,
            "{{MEMBER}} index {{VALUE}} at row {{ROW}}, at index {{COL}} already used by {{DETAILS}}"
    ),
    GENERIC_DATE_OBS_RANGE("check if the DATE_OBS value is within 'normal' range (1933 - 2150)",
            "V2.6.1§1",
            RuleDataType.VALUE_EXPECTED,
            "The keyword {{MEMBER}} '{{VALUE}}' has an incorrect year, expected in [{{EXPECTED}}]"
    ),
    GENERIC_DATE_OBS_STANDARD("check if the DATE_OBS keyword is in the format 'YYYY-MM-DD'",
            "V2.6.1§1",
            RuleDataType.VALUE,
            "The keyword {{MEMBER}} '{{VALUE}}' is not in a standard format 'YYYY-MM-DD'"
    ),
    GENERIC_KEYWORD_FORMAT("check if the keyword format matches the expected format (data type)",
            "V2.4§2",
            RuleDataType.VALUE_EXPECTED,
            "Invalid format for keyword '{{MEMBER}}', found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_KEYWORD_MANDATORY("check if the required keyword is present",
            "V1-V2.Tables",
            RuleDataType.NONE,
            "Missing keyword '{{MEMBER}}'"
    ),
    GENERIC_KEYWORD_VAL_ACCEPTED_INT("check if the keyword value matches the 'accepted' values (integer)",
            "V1-V2.Tables",
            RuleDataType.VALUE_EXPECTED,
            "Invalid value for keyword '{{MEMBER}}', found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_KEYWORD_VAL_ACCEPTED_STR("check if the keyword value matches the 'accepted' values (string)",
            "V1-V2.Tables",
            RuleDataType.VALUE_EXPECTED,
            "Invalid value for keyword '{{MEMBER}}', found '{{VALUE}}' should be '{{EXPECTED}}'"
    ),
    GENERIC_MJD_RANGE("check if the MJD value is within 'normal' range (1933 - 2150)",
            "V2.6.1§1",
            RuleDataType.VALUE_EXPECTED,
            "{{MEMBER}} [{{VALUE}}] is out of range, expected in [{{EXPECTED}}]"
    ),
    GENERIC_OIREV_FIX(RuleType.FIX,
            "Fix the OI_REV keyword when the table is not in the proper OIFITS version",
            Const.JMMC,
            RuleDataType.VALUE,
            "Fixed OI_REVN to {{VALUE}}."
    ),
    GENERIC_STA_INDEX_UNIQ("check duplicated indexes inside each STA_INDEX column values (data table)",
            "V1.6.1.4",
            RuleDataType.VALUE_ROW_COL1_COL2,
            "STA_INDEX[{{VALUE}}] duplicated at row {{ROW}} at indexes {{DETAILS}}"
    ),
    OI_ARRAY_ARRNAME("check the ARRNAME keyword has a not null or empty value",
            "V2.5.2§1",
            RuleDataType.NONE,
            "ARRNAME identifier has blank value"
    ),
    OI_ARRAY_STA_NAME("check if the STA_NAME column values have a not null or empty value",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "STA_NAME undefined at row {{ROW}}"
    ),
    OI_ARRAY_STA_NAME_UNIQ("check duplicated values in the STA_NAME column of the OI_ARRAY table",
            Const.JMMC,
            RuleDataType.VALUE_ROW1_ROW2,
            "STA_NAME[{{VALUE}}] duplicated at rows {{DETAILS}}"
    ),
    OI_ARRAY_STA_INDEX_MIN("check if the STA_INDEX values >= 1",
            "V1.6.1.4",
            RuleDataType.VALUE_ROW,
            "STA_INDEX[{{VALUE}}] cannot be < 1 at row {{ROW}}"
    ),
    OI_ARRAY_STA_INDEX_UNIQ("check duplicated indexes in the STA_INDEX column of the OI_ARRAY table",
            "V1.6.1.4",
            RuleDataType.VALUE_ROW1_ROW2,
            "STA_INDEX[{{VALUE}}] duplicated at rows {{DETAILS}}"
    ),
    OI_ARRAY_XYZ("check if the ARRAY_XYZ keyword values corresponds to a proper coordinate on earth",
            "V1.6.1.3",
            RuleDataType.VALUE,
            "Invalid {{MEMBER}} keyword '{{VALUE}}'"
    ),
    OI_ARRAY_XYZ_FIX(RuleType.FIX,
            "fix the ARRAY_XYZ keyword values (to VLTI or CHARA according to the ARRNAME keyword) when the ARRAY_XYZ keyword values are incorrect",
            "V1.6.1.3",
            RuleDataType.VALUE,
            "Fixed ARRAY_X/Y/Z to '{{VALUE}}'."
    ),
    OI_CORR_CORRNAME("check the CORRNAME keyword has a not null or empty value",
            Const.JMMC,
            RuleDataType.NONE,
            "CORRNAME identifier has blank value"
    ),
    OI_CORR_IINDEX_MIN("check if the IINDEX values >= 1 (JINDEX >= 2)",
            "V2.OI_CORR_Table",
            RuleDataType.VALUE_ROW,
            "IINDX index {{VALUE}} cannot be < 1 at row {{ROW}}"
    ),
    OI_CORR_JINDEX_SUP("check if the JINDEX values > IINDEX values",
            "V2.OI_CORR_Table",
            RuleDataType.VALUE_LIMIT_ROW,
            "JINDX index {{VALUE}} cannot be <= IINDX index {{LIMIT}} at row {{ROW}}"
    ),
    OI_CORR_IJINDEX_MAX("check if the IINDEX values <= NDATA and JINDEX values <= NDATA",
            "V2.OI_CORR_Table",
            RuleDataType.VALUE_LIMIT_ROW,
            "{{MEMBER}} index {{VALUE}} cannot be > NDATA [{{LIMIT}}] at row {{ROW}}"
    ),
    OIFITS_OI_ARRAY_EXIST_V2("check if at least one OI_ARRAY table exists in the OIFITS 2 file",
            "V2.4.2§1",
            RuleDataType.NONE,
            "No OI_ARRAY table found: one or more must be present"
    ),
    OIFITS_OIDATA("check if at least one data table exists in the OIFITS file",
            "V2.4.2§1",
            RuleDataType.NONE,
            "No OI_VIS, OI_VIS2, OI_T3 table found: one or more of them must be present"
    ),
    OIFITS_OI_TARGET_EXIST("check if only one OI_TARGET table exists in the OIFITS file",
            "V2.4.2§1",
            RuleDataType.NONE,
            "No OI_TARGET table found: one and only one must be present"
    ),
    OIFITS_OI_WAVELENGTH_EXIST("check if at least one OI_WAVELENGTH table exists in the OIFITS file",
            "V2.4.2§1",
            RuleDataType.NONE,
            "No OI_WAVELENGTH table found: one or more must be present"
    ),
    OIFITS_PRIMARYHDU_EXIST_V2("check if the main header (PRIMARY HDU) exists in the OIFITS 2 file",
            "V2.4.1§3",
            RuleDataType.NONE,
            "No primary HDU found: one must be present"
    ),
    OIFITS_TABLE_UNKNOWN("check if the table belongs to the OIFITS standard and version",
            "V2.4.2§3-4",
            RuleDataType.NONE,
            "Skipping non-standard OIFITS '{{HDU}}'"
    ),
    OI_FLUX_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_FLUXDATA is present",
            "V2.7.2§4",
            RuleDataType.NONE,
            "Missing OI_CORR table but the column CORRINDX_FLUXDATA is defined."
    ),
    OI_INSPOL_INSNAME_UNIQ("TODO: check if the INSNAME column values are only present in a single OI_INSPOL table (compare multi OI_INSPOL table)",
            "V2.7.3§2",
            RuleDataType.NONE,
            ""
    ),
    OI_INSPOL_MJD_DIFF("check if MJD_OBS are not > at MJD_END values in OI_INSPOL table",
            "V2.6.1§3",
            RuleDataType.VALUE_LIMIT_ROW,
            "Invalid {{MEMBER}} '{{VALUE}}', cannot be > MJD_END '{{LIMIT}}' at row {{ROW}}"
    ),
    OI_INSPOL_MJD_RANGE("check if MJD values in data tables are within MJD intervals (MJD_OBS and MJD_END columns) of the referenced OI_INSPOL table",
            "V2.6.1§3",
            RuleDataType.VALUE_ROW,
            "Invalid {{MEMBER}} '{{VALUE}}', cannot be < 0 at row {{ROW}}"
    ),
    OI_T3_CORRINDX("check if the referenced OI_CORR exists when the column CORRINDX_T3AMP or CORRINDX_T3PHI is present",
            "V2.7.2§4",
            RuleDataType.NONE,
            "Missing OI_CORR table but the column {{MEMBER}} is defined."
    ),
    OI_TARGET_COORD("check if the TARGET RA and DEC values are not 0.0",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "The target {{MEMBER}} coordinate is 0.0 at row {{ROW}}"
    ),
    OI_TARGET_COORD_EXIST("check if the TARGET RA or DEC value is not undefined",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "The target coordinates are undefined at row {{ROW}}"
    ),
    OI_TARGET_TARGET("check if the TARGET column values have a not null or empty value",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "TARGET undefined at row {{ROW}}"
    ),
    OITARGET_TARGET_EXIST("check if the OI_TARGET table has at least one target",
            Const.JMMC,
            RuleDataType.NONE,
            "No target defined"
    ),
    OI_TARGET_TARGET_UNIQ("check duplicated values in the TARGET column of the OI_TARGET table",
            Const.JMMC,
            RuleDataType.VALUE_ROW1_ROW2,
            "TARGET[{{VALUE}}] duplicated at rows {{DETAILS}}"
    ),
    OI_TARGET_TARGETID_MIN("check if the TARGET_ID values >= 1",
            "V2.OI_TARGET_Table",
            RuleDataType.VALUE_ROW,
            "TARGET_ID[{{VALUE}}] cannot be < 1 at row {{ROW}}"
    ),
    OI_TARGET_TARGETID_UNIQ("check duplicated indexes in the TARGET_ID column of the OI_TARGET table",
            Const.JMMC,
            RuleDataType.VALUE_ROW1_ROW2,
            "TARGET_ID[{{VALUE}}] duplicated at rows {{DETAILS}}"
    ),
    OI_VIS_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_VISAMP, CORRINDX_VISPHI, CORRINDX_RVIS or CORRINDX_IVIS is present",
            "V2.7.2§4",
            RuleDataType.NONE,
            "Missing OI_CORR table but the column '{{MEMBER}}' is defined."
    ),
    OI_VIS2_CORRINDX("check if the referenced OI_CORR table exists when the column CORRINDX_VIS2DATA is present",
            "V2.7.2§4",
            RuleDataType.NONE,
            "Missing OI_CORR table but the column CORRINDX_VIS2DATA is defined."
    ),
    OI_WAVELENGTH_EFF_WAVE("check the EFF_WAVE column values are within range [0.1E-6 ... 20.0E-6]",
            Const.JMMC,
            RuleDataType.VALUE_ROW,
            "EFF_WAVE colunm value '{{VALUE}}' is out of range [0.1E-6 ... 20.0E-6] at row {{ROW}}"
    ),
    OI_WAVELENGTH_INSNAME("check the INSNAME keyword has a not null or empty value",
            "V2.5.3§3",
            RuleDataType.NONE,
            "INSNAME identifier has blank value"
    ),
    PRIMARYHDU_MULTI_TARGET("check if main header keywords are set to 'MULTI' for heterogenous content",
            "V2.MAIN_HEADER_Table(3)",
            RuleDataType.VALUE,
            "'{{MEMBER}}' keyword must contain the value '{{VALUE}}' when multi targets and array"
    ),
    PRIMARYHDU_TYPE_ATOMIC("check if supplementary keywords are present, when OIFITS contains only one target observed on a single interferometer",
            "V2.MAIN_HEADER_Table(3)",
            RuleDataType.NONE,
            "{{MEMBER}} keyword must be present"
    ),
    TABLE_NOT_OIFITS2(
            "check if any OIFITS 2 specific table (OI_CORR, OI_INSPOL or OI_FLUX) is present in the OIFITS 1 file",
            Const.JMMC,
            RuleDataType.NONE,
            "Unsupported table {{HDU}} in OIFITS V1.0"
    );

    // members:
    private final RuleType type;
    private final String description;
    private final String origin;
    private final RuleDataType dataType;
    private final String message;
    // TODO: replace String by ModelSource[String struct, String member]
    private final Set<String> applyToSet = (OIFitsChecker.isInspectRules()) ? new HashSet<String>() : null;
    private final Set<OIFitsStandard> standardSet = (OIFitsChecker.isInspectRules()) ? new TreeSet<OIFitsStandard>() : null;

    /**
     * Constructor, use it when we have expression or for Apply To
     * @param description String description of rules
     * @param origin of rule JMMC or Standard
     * @param dataType rule dataType
     * @param message for more failure informations
     */
    private Rule(final String description, final String origin, final RuleDataType dataType, final String message) {
        this(RuleType.CHECK, description, origin, dataType, message);
    }

    /**
     * Constructor, use it when we have expression or for Apply To
     * @param type RuleType for Rule
     * @param description String description of rules
     * @param origin of rule JMMC or Standard
     * @param dataType rule dataType
     * @param message for more failure informations
     */
    private Rule(final RuleType type, final String description, final String origin, final RuleDataType dataType, final String message) {
        this.type = type;
        this.description = description;
        this.origin = origin;
        this.dataType = dataType;
        this.message = message;
    }

    void checkDataType(final RuleDataType expected) {
        if (OIFitsChecker.isInspectRules()) {
            if (getDataType() != expected) {
                // special case GENERIC_COL_VAL_ACCEPTED_INT: RuleDataType.VALUE_EXPECTED_ROW_COL already checked
                if (this == Rule.GENERIC_COL_VAL_ACCEPTED_INT && expected == RuleDataType.VALUE_EXPECTED_ROW) {
                    return;
                }
                // special case GENERIC_COL_VAL_POSITIVE: RuleDataType.VALUE_EXPECTED_ROW_COL already checked
                if (this == Rule.GENERIC_COL_VAL_POSITIVE && expected == RuleDataType.VALUE_EXPECTED_ROW) {
                    return;
                }
                throw new IllegalStateException("Rule " + name() + " type expected: " + expected + " - type found: " + getDataType());
            }
        }
    }

    /**
     * Return the type of rule
     * @return type
     */
    public RuleType getType() {
        return type;
    }

    /**
     * Return the description of rule
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Return the origin of rule
     * @return origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Return the dataType of rule
     * @return dataType
     */
    public RuleDataType getDataType() {
        return dataType;
    }

    /**
     * Return the message of rule
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the Set for applyTo
     * @return applyToSet
     */
    public Set<String> getApplyTo() {
        return applyToSet;
    }

    void addApplyTo(String v) {
        applyToSet.add(v);
    }

    /**
     * Return the Set for standard
     * @return standardSet
     */
    public Set<OIFitsStandard> getStandard() {
        return standardSet;
    }

    void addStandard(OIFitsStandard v) {
        standardSet.add(v);
    }

    @Override
    public String toString() {
        return "Rule{name=\"" + name() + "\", description=\"" + description + "\", paragraph=\"" + origin + "\", applyTo=\"" + applyToSet + "\", standardSet=\"" + standardSet + "\"}";
    }

    /**
     * Method to sort on this Enum
     * @return Const.CMP_NAME
     */
    public static Comparator<Rule> getComparatorByName() {
        return Const.CMP_NAME;
    }

    private static final class Const {

        public final static String JMMC = "JMMC";
        public final static String UNDEFINED = "UNDEFINED";

        static Comparator<Rule> CMP_NAME = new Comparator<Rule>() {
            @Override
            public int compare(final Rule r1, final Rule r2) {
                return r1.name().compareTo(r2.name());
            }

        };

        private Const() {
            // no-op
        }
    }

    /**
     * XML display for Rules
     * @param sb StringBuilder
     * @param ignore if no OIFitsStandard ignore
     */
    public void toXml(final StringBuilder sb, final OIFitsStandard ignore) {
        sb.append("  <rule>\n");
        sb.append("    <name>").append(name()).append("</name>\n");
        sb.append("    <description>").append(encodeTagContent(getDescription())).append("</description>\n");
        sb.append("    <paragraph>").append(getOrigin()).append("</paragraph>\n");

        if (getApplyTo() != null) {
            sb.append("    <subjects>\n");

            final ArrayList<String> sorted = new ArrayList<String>(64);
            sorted.addAll(getApplyTo());
            Collections.sort(sorted);

            for (String applyTo : sorted) {
                sb.append("      <apply>").append(applyTo).append("</apply>\n");
            }
            sorted.clear();

            sb.append("    </subjects>\n");
        }

        if (getStandard() != null) {
            sb.append("    <standards>\n");
            for (OIFitsStandard standard : getStandard()) {
                if (standard != ignore) {
                    sb.append("      <standard>").append(standard).append("</standard>\n");
                }
            }
            sb.append("    </standards>\n");
        }
        sb.append("  </rule>\n");
    }

    /**
     * Unused (for test method)
     * @param unused
     */
    public static void main(String[] unused) {
        System.out.println("Rules:");
        for (Rule r : Rule.values()) {
            System.out.println(r.toString());
        }
    }
}
