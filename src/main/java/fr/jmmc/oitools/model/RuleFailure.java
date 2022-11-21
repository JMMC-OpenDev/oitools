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
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.image.FileRef;
import java.util.regex.Pattern;

/**
 * Management of objects related to the creation of the error (toString/XML)
 * this class is a key for hash maps (hashcode / equals)
 * @author kempsc
 */
public final class RuleFailure {

    /**
     * Enum for field to allow in RuleFailure
     */
    public enum RuleFailureField {
        RULE, FILE_REF, EXTNAME, EXT_NB, MEMBER, SEVERITY;
    }
    /* members */
    /** validation rule */
    private final Rule rule;
    /* fileRef */
    /** extension name = table type */
    private final FileRef fileRef;
    /** extName (HDURef) (optional) */
    private final String extName;
    /** extNb (HDURef) */
    private final int extNb;
    /** Keyword/column Name (optional) */
    private final String member;
    /** message severity set by rule verifier (not in the key) */
    private Severity severity = Severity.Undefined;

    /**
     * Constructor
     * @param rule rules informations
     * @param fileRef fileName and AbsoluteFilePath informations
     * @param extName Hdu name
     * @param extNb Hdu Number
     * @param member member (keyword/column) name
     */
    RuleFailure(final Rule rule, final FileRef fileRef, final String extName, final int extNb, final String member) {
        this.rule = rule;
        this.fileRef = fileRef;
        // Need HDURef to have an up-to-date HDU state[extName / extNB] as extNb can be updated when writing OIFits file !
        this.extName = extName;
        this.extNb = extNb;
        this.member = member;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleFailure other = (RuleFailure) obj;
        if (this.rule != other.getRule()) {
            return false;
        }
        if (!this.fileRef.equals(other.getFileRef())) {
            return false;
        }
        if ((this.extName == null) ? (other.getExtName() != null) : !this.extName.equals(other.getExtName())) {
            return false;
        }
        if (this.extNb != other.getExtNb()) {
            return false;
        }
        if ((this.member == null) ? (other.getMember() != null) : !this.member.equals(other.getMember())) {
            return false;
        }
        // Do not compare Severity (not in the key)
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.rule.hashCode();
        hash = 37 * hash + this.fileRef.hashCode();
        hash = 37 * hash + (this.extName != null ? this.extName.hashCode() : 0);
        hash = 37 * hash + this.extNb;
        hash = 37 * hash + (this.member != null ? this.member.hashCode() : 0);
        // Do not compare Severity (not in the key)
        return hash;
    }

    /**
     * Get the correct field
     * @param field RuleFailureField enum
     * @return the correct field
     */
    public Object getField(final RuleFailureField field) {
        switch (field) {
            case RULE:
                return getRule();
            case FILE_REF:
                return getFileRef();
            case EXTNAME:
                return getExtName();
            case EXT_NB:
                return NumberUtils.valueOf(getExtNb());
            case MEMBER:
                return getMember();
            case SEVERITY:
                return getSeverity();
            default:
                return null;
        }
    }

    /* --- KEY --- */
    /**
     * @return validation rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @return FileRef
     */
    public FileRef getFileRef() {
        return fileRef;
    }

    /**
     * @return extName
     */
    public String getExtName() {
        return extName;
    }

    /**
     * @return extNb
     */
    public int getExtNb() {
        return extNb;
    }

    /**
     * @return member
     */
    public String getMember() {
        return member;
    }

    /* --- SEVERITY --- */
    /**
     * @return message severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Define the message severity
     * @param severity message severity
     */
    void setSeverity(final Severity severity) {
        this.severity = severity;
    }

    /* --- FORMAT MESSAGE --- */
    /** RegExp expression to match {{FILE}} */
    private static final Pattern PATTERN_FILE = Pattern.compile("\\{\\{FILE\\}\\}");
    /** RegExp expression to match {{HDU}} */
    private static final Pattern PATTERN_HDU = Pattern.compile("\\{\\{HDU\\}\\}");
    /** RegExp expression to match {{EXTNAME}} */
    private static final Pattern PATTERN_EXTNAME = Pattern.compile("\\{\\{EXTNAME\\}\\}");
    /** RegExp expression to match {{MEMBER}} */
    private static final Pattern PATTERN_MEMBER = Pattern.compile("\\{\\{MEMBER\\}\\}");

    /**
     * Returns a string representation for message
     * @return message
     */
    String formatMessage() {
        if (getRule() == null) {
            return "";
        }
        String msg = getRule().getMessage();

        msg = replaceAll(PATTERN_FILE, msg,
                (getFileRef() != null && getFileRef().getAbsoluteFilePath() != null) ? getFileRef().getAbsoluteFilePath() : ""
        );
        msg = replaceAll(PATTERN_HDU, msg,
                FitsHDU.getHDUId(getExtName(), getExtNb())
        );
        msg = replaceAll(PATTERN_EXTNAME, msg,
                (getExtName() != null) ? getExtName() : FitsHDU.getHDUId(null, getExtNb())
        );
        msg = replaceAll(PATTERN_MEMBER, msg,
                (getMember() != null) ? getMember() : ""
        );

        return msg;
    }

    /**
     * Returns a string representation of this class
     * @return a string representation of this class
     */
    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * Returns a string representation of this class
     * @param sb StringBuilder
     * @return a string representation of this class
     */
    public StringBuilder toString(final StringBuilder sb) {
        sb.append(getSeverity()).append('\t');

        if (getRule() != null) {
            sb.append(getRule().name());
        }
        sb.append('\t');

        if ((getFileRef() != null) && (getFileRef().getAbsoluteFilePath() != null)) {
            sb.append(getFileRef().getAbsoluteFilePath());
        }
        sb.append('\t');

        if (getExtNb() >= 0) {
            sb.append((getExtName() == null) ? "HDU" : getExtName()).append('\t');
            sb.append(getExtNb()).append('\t');
        }

        if (getMember() != null) {
            sb.append(getMember());
        }
        sb.append('\t');
        return sb;
    }

    /**
     * Returns a XML representation of this class
     * @param sb StringBuilder
     * @return a XML representation of this class
     */
    public StringBuilder toXML(StringBuilder sb) {
        sb.append("    <severity>").append(getSeverity()).append("</severity>\n");

        if (getRule() != null) {
            sb.append("    <rule>").append(getRule().name()).append("</rule>\n");
        }

        if (getFileRef() != null) {
            getFileRef().toXML(sb);
        }

        //Display HDU#0
        if (getExtNb() >= 0) {
            sb.append("    <extName>").append((getExtName() == null) ? "HDU" : getExtName()).append("</extName>\n");
            sb.append("    <extNb>").append(getExtNb()).append("</extNb>\n");
        }

        if (getMember() != null) {
            sb.append("    <member>").append(getMember()).append("</member>\n");
        }
        return sb;
    }

    static String replaceAll(final Pattern pattern, final String value, final String replacement) {
        return pattern.matcher(value).replaceAll(replacement);
    }

}
