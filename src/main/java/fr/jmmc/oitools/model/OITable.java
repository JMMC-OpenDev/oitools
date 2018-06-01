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
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.nom.tam.fits.HeaderCard;
import java.util.logging.Level;

/**
 * Base Class for all OI_* tables.
 */
public abstract class OITable extends FitsTable {

    /* static descriptors */
    /** EXTNAME for V1 OIFITS keyword descriptor */
    private final static KeywordMeta KEYWORD_EXTNAME_1 = new KeywordMeta(FitsConstants.KEYWORD_EXT_NAME, "extension name", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.TABLE_OI_ARRAY, OIFitsConstants.TABLE_OI_TARGET, OIFitsConstants.TABLE_OI_WAVELENGTH,
                         OIFitsConstants.TABLE_OI_VIS, OIFitsConstants.TABLE_OI_VIS2, OIFitsConstants.TABLE_OI_T3
            });
    /** EXTNAME for V2 OIFITS keyword descriptor */
    private final static KeywordMeta KEYWORD_EXTNAME_2 = new KeywordMeta(FitsConstants.KEYWORD_EXT_NAME, "extension name", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.TABLE_OI_ARRAY, OIFitsConstants.TABLE_OI_TARGET, OIFitsConstants.TABLE_OI_WAVELENGTH,
                         OIFitsConstants.TABLE_OI_VIS, OIFitsConstants.TABLE_OI_VIS2, OIFitsConstants.TABLE_OI_T3,
                         OIFitsConstants.TABLE_OI_FLUX, OIFitsConstants.TABLE_OI_CORR, OIFitsConstants.TABLE_OI_INSPOL
            });
    /** OI_REVN=1 keyword descriptor */
    private final static KeywordMeta KEYWORD_OI_REVN_1 = new KeywordMeta(OIFitsConstants.KEYWORD_OI_REVN,
            "revision number of the table definition", Types.TYPE_INT, new short[]{OIFitsConstants.KEYWORD_OI_REVN_1});

    /** OI_REVN=2 keyword descriptor */
    private final static KeywordMeta KEYWORD_OI_REVN_2 = new KeywordMeta(OIFitsConstants.KEYWORD_OI_REVN,
            "revision number of the table definition", Types.TYPE_INT, new short[]{OIFitsConstants.KEYWORD_OI_REVN_2});

    /* members */
    /** Main OIFitsFile */
    private OIFitsFile oifitsFile;

    /**
     * Protected OITable class constructor
     * @param oifitsFile main OifitsFile
     */
    protected OITable(final OIFitsFile oifitsFile) {
        super();
        this.oifitsFile = oifitsFile;

        final String extName;
        if (this instanceof OITarget) {
            extName = OIFitsConstants.TABLE_OI_TARGET;
        } else if (this instanceof OIWavelength) {
            extName = OIFitsConstants.TABLE_OI_WAVELENGTH;
        } else if (this instanceof OIArray) {
            extName = OIFitsConstants.TABLE_OI_ARRAY;
        } else if (this instanceof OIVis) {
            extName = OIFitsConstants.TABLE_OI_VIS;
        } else if (this instanceof OIVis2) {
            extName = OIFitsConstants.TABLE_OI_VIS2;
        } else if (this instanceof OIT3) {
            extName = OIFitsConstants.TABLE_OI_T3;
        } else if (this instanceof OIFlux) {
            extName = OIFitsConstants.TABLE_OI_FLUX;
        } else if (this instanceof OICorr) {
            extName = OIFitsConstants.TABLE_OI_CORR;
        } else if (this instanceof OIInspol) {
            extName = OIFitsConstants.TABLE_OI_INSPOL;
        } else {
            throw new IllegalStateException("Invalid child class: " + getClass().getName());
        }

        // Override EXTNAME  keyword definition
        addKeywordMeta((oifitsFile.isOIFits2()) ? KEYWORD_EXTNAME_2 : KEYWORD_EXTNAME_1);

        // OI_REVN  keyword definition
        final int revision = computeOiRevn();
        addKeywordMeta((revision == OIFitsConstants.KEYWORD_OI_REVN_1) ? KEYWORD_OI_REVN_1 : KEYWORD_OI_REVN_2);

        // Always define keyword values:
        this.setExtName(extName);
        this.setOiRevn(revision);
    }

    /**
     * Copy the table into this instance
     *
     * @param src table to copy
     */
    protected final void copyTable(final OITable src) throws IllegalArgumentException {
        // Copy keyword values:
        for (KeywordMeta keyword : getKeywordDescCollection()) {
            final String keywordName = keyword.getName();

            if (FitsConstants.KEYWORD_EXT_NAME.equals(keywordName)
                    || OIFitsConstants.KEYWORD_OI_REVN.equals(keywordName)) {
                // Ignore ExtName / OiRevn (v1/2) defined in previous constructor
                continue;
            }

            // get keyword value:
            final Object keywordValue = src.getKeywordValue(keywordName);

            // potentially missing values
            if (keywordValue != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "KEYWORD {0} = ''{1}''", new Object[]{keywordName, keywordValue});
                }
                setKeywordValue(keywordName, keywordValue);
            }
        }

        // Copy header cards:
        if (src.hasHeaderCards()) {
            // Copy references to Fits header cards:
            getHeaderCards().addAll(src.getHeaderCards());
        }

        // Copy column values:
        String columnName;
        Object columnValue;

        // Copy columns:
        for (ColumnMeta column : getColumnDescCollection()) {
            columnName = column.getName();
            columnValue = src.getColumnValue(columnName);

            if (columnValue == null) {
                columnValue = createColumnArray(column, getNbRows());
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "COLUMN {0} = ''{1}''", new Object[]{columnName, columnValue});
            }
            setColumnValue(columnName, columnValue);
        }
    }

    /*
     * --- OIFits standard Keywords --------------------------------------------
     */
    private int computeOiRevn() {
        final boolean isOiFits2 = getOIFitsFile().isOIFits2();
        // OIFITS 1 or 2 logic:
        int revision = (isOiFits2) ? OIFitsConstants.KEYWORD_OI_REVN_2 : OIFitsConstants.KEYWORD_OI_REVN_1;

        if ((this instanceof OIFlux) || (this instanceof OICorr) || (this instanceof OIInspol)) {
            // new OIFITS2 tables => oiRev=1
            // Laurent thinks it's TOTALLY stupid:
            revision = OIFitsConstants.KEYWORD_OI_REVN_1;
        }
        return revision;
    }

    /**
     * Get the OI_REVN keyword value
     *
     * @return value of OI_REVN keyword
     */
    public final int getOiRevn() {
        return getKeywordInt(OIFitsConstants.KEYWORD_OI_REVN);
    }

    /**
     * Define the OI_REVN keyword value
     *
     * @param oiRevn value of OI_REVN keyword
     */
    protected final void setOiRevn(final int oiRevn) {
        setKeywordInt(OIFitsConstants.KEYWORD_OI_REVN, oiRevn);
    }

    /**
     * Check syntax of table's keywords. It consists in checking all mandatory
     * keywords are present, with right name, right format and right values (if
     * they do belong to a given set of accepted values).
     *
     * @param checker checker component
     */
    @Override
    public void checkKeywords(final OIFitsChecker checker) {
        super.checkKeywords(checker);

        final int revision = computeOiRevn();
        if (revision != getOiRevn() || OIFitsChecker.isInspectRules()) {
            // rule [GENERIC_OIREV_FIX] Fix the OI_REV keyword when the table is not in the proper OIFITS version
            checker.ruleFailed(Rule.GENERIC_OIREV_FIX, this).addFixedValue(revision);
            setOiRevn(computeOiRevn());
        }
    }

    /**
     * Return the main OIFitsFile
     * @return OIFitsFile
     */
    public final OIFitsFile getOIFitsFile() {
        return this.oifitsFile;
    }

    /**
     * Set parent structure. 
TODO: temporary, to remove when copyTable() is done 
     * @param file 
     */
    public void setOIFitsFile(OIFitsFile file) {
        this.oifitsFile = file;
    }
}
