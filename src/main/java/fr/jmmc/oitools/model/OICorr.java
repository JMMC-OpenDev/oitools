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
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;

/**
 * Describe correlation between any kinds of OIFITS observable
 * @author kempsc
 */
public final class OICorr extends OITable {

    /* constants */
 /* static descriptors */
    /** CORRNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_CORRNAME = new KeywordMeta(OIFitsConstants.KEYWORD_CORRNAME,
            "name of correlated data set", Types.TYPE_CHAR);
    /** NDATA keyword descriptor */
    private final static KeywordMeta KEYWORD_NDATA = new KeywordMeta(OIFitsConstants.KEYWORD_NDATA,
            "Number of correlated data", Types.TYPE_INT);

    /** IINDX  keyword descriptor */
    private final static ColumnMeta COLUMN_IINDX = new ColumnMeta(OIFitsConstants.COLUMN_IINDX,
            "Frist index of correlation matrix element", Types.TYPE_INT);

    /** JINDX  keyword descriptor */
    private final static ColumnMeta COLUMN_JINDX = new ColumnMeta(OIFitsConstants.COLUMN_JINDX,
            "Second index of correlation matrix element", Types.TYPE_INT);

    /** CORR  keyword descriptor */
    private final static ColumnMeta COLUMN_CORR = new ColumnMeta(OIFitsConstants.COLUMN_CORR,
            "Matrix element (IINDX, JINDX)", Types.TYPE_DBL, Units.NO_UNIT, DataRange.RANGE_POSITIVE_STRICT);

    /**
     * Public OICorr class constructor
     * @param oifitsFile main OifitsFile
     */
    public OICorr(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // CORRNAME  keyword definition
        addKeywordMeta(KEYWORD_CORRNAME);

        // NDATA  keyword definition
        addKeywordMeta(KEYWORD_NDATA);

        // IINDX  keyword definition
        addColumnMeta(COLUMN_IINDX);

        // JINDX  keyword definition
        addColumnMeta(COLUMN_JINDX);

        // CORR  keyword definition
        addColumnMeta(COLUMN_CORR);
    }

    /**
     * Public OICorr class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OICorr(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /* --- keywords --- */
    /**
     * Get the CORRNAME keyword value.
     * @return the value of CORRNAME keyword
     */
    public String getCorrName() {
        return getKeyword(OIFitsConstants.KEYWORD_CORRNAME);
    }

    /**
     * Define the CORRNAME keyword value
     * @param corrName value of CORRNAME keyword
     */
    public void setCorrName(final String corrName) {
        setKeyword(OIFitsConstants.KEYWORD_CORRNAME, corrName);
    }

    /**
     * Get the value of NDATA keyword
     * @return the value of NDATA keyword
     */
    public int getNData() {
        return getKeywordInt(OIFitsConstants.KEYWORD_NDATA);
    }

    /**
     * Define the NDATA keyword value
     * @param nData value of NDATA keyword
     */
    public void setNData(final int nData) {
        setKeywordInt(OIFitsConstants.KEYWORD_NDATA, nData);
    }

    /* --- column --- */
    /**
     * Return the IINDX column.
     * @return the IINDX column.
     */
    public int[] getIindx() {
        return this.getColumnInt(OIFitsConstants.COLUMN_IINDX);
    }

    /**
     * Return the JINDX column.
     * @return the JINDX column.
     */
    public int[] getJindx() {
        return this.getColumnInt(OIFitsConstants.COLUMN_JINDX);
    }

    /**
     * Return the CORR column.
     * @return the CORR column.
     */
    public double[] getCorr() {
        return this.getColumnAsDouble(OIFitsConstants.COLUMN_CORR);
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ CORRNAME=" + getCorrName() + " | " + getNData() + " Number off correlation data ]";
    }

    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        // rule [OI_CORR_CORRNAME] check the CORRNAME keyword has a not null or empty value
        if ((getCorrName() != null && getCorrName().length() == 0) || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.OI_CORR_CORRNAME, this, OIFitsConstants.KEYWORD_CORRNAME);
        }
        final int nRows = getNbRows();

        // ndata gives the square matrix dimensions [N x N]
        final int ndata = getNData();

        final int[] iIndx = getIindx();
        final int[] jIndx = getJindx();

        for (int i = 0; i < nRows; i++) {
            final int idxI = iIndx[i];
            final int idxJ = jIndx[i];

            // rule [OI_CORR_IINDEX_MIN] check if the IINDEX values >= 1 (JINDEX >= 2)
            if (idxI < 1 || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_CORR_IINDEX_MIN, this, OIFitsConstants.COLUMN_IINDX).addValueAt(idxI, i);
            }
            // rule [OI_CORR_JINDEX_SUP] check if the JINDEX values > IINDEX values
            if (idxJ <= idxI || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_CORR_JINDEX_SUP, this, OIFitsConstants.COLUMN_JINDX).addValuesAt(idxJ, idxI, i);
            }
            // rule [OI_CORR_IJINDEX_MAX] check if the IINDEX values <= NDATA and JINDEX values <= NDATA
            if (idxI > ndata || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_CORR_IJINDEX_MAX, this, OIFitsConstants.COLUMN_IINDX).addValuesAt(idxI, ndata, i);
            }
            if (idxJ > ndata || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_CORR_IJINDEX_MAX, this, OIFitsConstants.COLUMN_JINDX).addValuesAt(idxJ, ndata, i);
            }
        }

        getOIFitsFile().checkCrossReference(this, checker);

    }
}
