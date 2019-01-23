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
        if (this.rule != other.rule) {
            return false;
        }
        if (!this.fileRef.equals(other.fileRef)) {
            return false;
        }
        if ((this.extName == null) ? (other.extName != null) : !this.extName.equals(other.extName)) {
            return false;
        }
        if (this.extNb != other.extNb) {
            return false;
        }
        if ((this.member == null) ? (other.member != null) : !this.member.equals(other.member)) {
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
     * Return the validation rule
     * @return validation rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Return the FileRef
     * @return FileRef
     */
    public FileRef getFileRef() {
        return fileRef;
    }

    /**
     * Return the extName
     * @return extName
     */
    public String getExtName() {
        return extName;
    }

    /**
     * Return the extNb
     * @return extNb
     */
    public int getExtNb() {
        return extNb;
    }

    /**
     * Return the member
     * @return member
     */
    public String getMember() {
        return member;
    }

    /* --- SEVERITY --- */
    /**
     * Return the message severity
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
    /**
     * Returns a string representation for message
     * @return message
     */
    String formatMessage() {
        if (getRule() == null) {
            return "";
        }
        String msg = getRule().getMessage();

        msg = msg.replaceAll("\\{\\{FILE\\}\\}",
                (getFileRef() != null && getFileRef().getAbsoluteFilePath() != null) ? getFileRef().getAbsoluteFilePath() : ""
        );
        msg = msg.replaceAll("\\{\\{HDU\\}\\}",
                FitsHDU.getHDUId(getExtName(), getExtNb())
        );
        msg = msg.replaceAll("\\{\\{MEMBER\\}\\}",
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
            sb.append(getRule().name()).append('\t');
        }

        if (getFileRef() != null && getFileRef().getAbsoluteFilePath() != null) {
            sb.append(getFileRef().getAbsoluteFilePath());
        }
        sb.append('\t');
        if (getExtNb() >= 0 && getExtName() == null) {
            sb.append("HDU").append('\t');
            sb.append(getExtNb()).append('\t');
        } else if (getExtNb() >= 0) {
            sb.append(getExtName()).append('\t');
            sb.append(getExtNb()).append('\t');
        }

        if (getMember() != null) {
            sb.append(getMember());
        }
        sb.append('\t');

//        sb.append(formatMessage()).append('\t');
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
            sb.append("    <extName>");
            sb.append((getExtName() == null) ? "HDU" : getExtName());
            sb.append("</extName>\n");
            sb.append("    <extNb>");
            sb.append(getExtNb()).append("</extNb>\n");
        }

        if (getMember() != null) {
            sb.append("    <member>").append(getMember()).append("</member>\n");
        }

        return sb;
    }
}
