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
import fr.jmmc.oitools.meta.ArrayColumnMeta;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;

/**
 *
 * @author kempsc
 */
public final class OIInspol extends OIAbstractData {

    /* static descriptors */
    /** NPOL keyword descriptor */
    private final static KeywordMeta KEYWORD_NPOL = new KeywordMeta(OIFitsConstants.KEYWORD_NPOL,
            "Number of polarisation type int this table", Types.TYPE_INT);
    /** ORIENT keyword descriptor */
    private final static KeywordMeta KEYWORD_ORIENT = new KeywordMeta(OIFitsConstants.KEYWORD_ORIENT,
            "Orientation of Jones matrix, could be 'NORTH' " + "(for on-sky orientation), or 'LABORATORY", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.KEYWORD_ORIENT_NORTH, OIFitsConstants.KEYWORD_ORIENT_LABORATORY});
    /** MODEL keyword descriptor */
    private final static KeywordMeta KEYWORD_MODEL = new KeywordMeta(OIFitsConstants.KEYWORD_MODEL,
            "A string keyword that describe the way the Jones matrix is estimated", Types.TYPE_CHAR);

    /** MJD_OBS  column descriptor */
    private final static ColumnMeta COLUMN_MJD_OBS = new ColumnMeta(OIFitsConstants.COLUMN_MJD_OBS,
            "Modified Julian day, start of time lapse", Types.TYPE_DBL, Units.UNIT_DAYS, DataRange.RANGE_POSITIVE_STRICT);
    /** MJD_END  column descriptor */
    private final static ColumnMeta COLUMN_MJD_END = new ColumnMeta(OIFitsConstants.COLUMN_MJD_END,
            "Modified Julian day, end of time lapse", Types.TYPE_DBL, Units.UNIT_DAYS, DataRange.RANGE_POSITIVE_STRICT);

    /** number of wavelengths (TODO) */
    private final int nWaves;

    /**
     * Public OICorr class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIInspol(final OIFitsFile oifitsFile) {
        // TODO: how to set nWaves as columns are loaded after the constructor
        this(oifitsFile, 0); // cyclic-dependency or chicken-egg problem !
    }

    /**
     * Private OIInspol class constructor
     * @param oifitsFile main OifitsFile
     * @param nWaves TODO
     */
    private OIInspol(final OIFitsFile oifitsFile, final int nWaves) {
        super(oifitsFile);

        // NPOL  keyword definition
        addKeywordMeta(KEYWORD_NPOL);
        // ORIENT  keyword definition
        addKeywordMeta(KEYWORD_ORIENT);
        // MODEL  keyword definition
        addKeywordMeta(KEYWORD_MODEL);

        // INSNAME  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.KEYWORD_INSNAME, "name of corresponding detector",
                Types.TYPE_CHAR, 70) {
            @Override
            public String[] getStringAcceptedValues() {
                return getOIFitsFile().getAcceptedInsNames();
            }
        });
        // MJD_OBS  keyword definition
        addColumnMeta(COLUMN_MJD_OBS);
        // MJD_END  keyword definition
        addColumnMeta(COLUMN_MJD_END);
        // JXX  keyword definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_JXX,
                "Complex Jones matrix component along X axis", Types.TYPE_COMPLEX, this));
        // JYY  keyword definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_JYY,
                "Complex Jones matrix component along Y axis", Types.TYPE_COMPLEX, this));
        // JXY  keyword definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_JXY,
                "Complex Jones matrix component between X and Y axis", Types.TYPE_COMPLEX, this));
        // JYX  keyword definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_JYX,
                "Complex Jones matrix component between Y and X axis", Types.TYPE_COMPLEX, this));

        // STA_INDEX  column definition
        addColumnMeta(new ArrayColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station number contributing to the data",
                Types.TYPE_SHORT, 1, false) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });
        // TODO: how to set nWaves as columns are loaded after the constructor
        this.nWaves = nWaves; // cyclic-dependency or chicken-egg problem !

        // Positive side-effect: it works well except the validation:
        /*
        SEVERE	Can't check repeat for column 'JXX'
        SEVERE	Can't check repeat for column 'JYY'
        SEVERE	Can't check repeat for column 'JXY'
        SEVERE	Can't check repeat for column 'JYX'        
         */
    }

    /**
     * Public OIInspol class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     * @param nWaves number of nwaves
     */
    public OIInspol(final OIFitsFile oifitsFile, final int nbRows, final int nWaves) {
        this(oifitsFile, nWaves);

        this.initializeTable(nbRows);
    }

    /**
     * Public OIInspol class constructor to copy the given table (structure only)
     * @param oifitsFile main OifitsFile
     * @param src table to copy
     */
    public OIInspol(final OIFitsFile oifitsFile, final OIInspol src) {
        this(oifitsFile);

        this.copyTable(src);
    }

    /* --- keywords --- */
    /**
     * Get the value of NPOL keyword
     * @return the value of NPOL keyword
     */
    public int getNPol() {
        return getKeywordInt(OIFitsConstants.KEYWORD_NPOL);
    }

    /**
     * Define the NPOL keyword value
     * @param nPol value of NPOL keyword
     */
    public void setNPol(final int nPol) {
        setKeywordInt(OIFitsConstants.KEYWORD_NPOL, nPol);
    }

    /**
     * Get the value of ORIENT keyword
     * @return the value of ORIENT keyword
     */
    public String getOrient() {
        return getKeyword(OIFitsConstants.KEYWORD_ORIENT);
    }

    /**
     * Define the ORIENT keyword value
     * @param orient value of ORIENT keyword
     */
    public void setOrient(final String orient) {
        setKeyword(OIFitsConstants.KEYWORD_ORIENT, orient);
    }

    /**
     * Get the value of MODEL keyword
     * @return the value of MODEL keyword
     */
    public String getModel() {
        return getKeyword(OIFitsConstants.KEYWORD_MODEL);
    }

    /**
     * Define the MODEL keyword value
     * @param model value of MODEL keyword
     */
    public void setModel(final String model) {
        setKeyword(OIFitsConstants.KEYWORD_MODEL, model);
    }


    /* --- columns --- */
    /**
     * Get the INSNAME column.
     * @return the INSNAME column
     */
    public String[] getInsNames() {
        return this.getColumnString(OIFitsConstants.KEYWORD_INSNAME);
    }

    /**
     * Return the MJD_OBS column.
     * @return the MJD_OBS column.
     */
    public double[] getMJDObs() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_MJD_OBS);
    }

    /**
     * Return the MJD_END column.
     * @return the MJD_END column.
     */
    public double[] getMJDEnd() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_MJD_END);
    }

    /**
     * Return the JXX column.
     * @return the JXX column.
     */
    public float[][][] getJXX() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_JXX);
    }

    /**
     * Return the JYY column.
     * @return the JYY column.
     */
    public float[][][] getJYY() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_JYY);
    }

    /**
     * Return the JXY column.
     * @return the JXY column.
     */
    public float[][][] getJXY() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_JXY);
    }

    /**
     * Return the JYX column.
     * @return the JYX column.
     */
    public float[][][] getJYX() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_JYX);
    }

    /* --- Utility methods for cross-referencing --- */
    /**
     * Return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s).
     * @return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s)
     * or 0 if the OI_WAVELENGTH table(s) are missing !
     * Note: this method is used by WaveColumnMeta.getRepeat() to determine the column dimensions
     */
    @Override
    public int getNWave() {
        return this.nWaves;
    }

    /*
     * --- Checker -------------------------------------------------------------
     */
    /**
     * Do syntactical analysis of the table
     *
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        checkMJDInspol(checker, getMJDObs(), getMJDEnd());
    }

    private void checkMJDInspol(OIFitsChecker checker, double[] mjdObs, double[] mjdEnd) {
        for (int i = 0; i < mjdObs.length; i++) {
            if ((mjdObs[i] < 0) || OIFitsChecker.isInspectRules()) {
                // rule [OI_INSPOL_MJD_RANGE] check if MJD values in data tables are within MJD intervals (MJD_OBS and MJD_END columns) of the referenced OI_INSPOL table [!! TBD in data tables !!]
                // WARNING: MJD_OBS can be begin a 0, there are no expected bounds
                checker.ruleFailed(Rule.OI_INSPOL_MJD_RANGE, this, OIFitsConstants.COLUMN_MJD_OBS).addValueAt(mjdObs[i], i);
            }
            if ((mjdEnd[i] < 0) || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_INSPOL_MJD_RANGE, this, OIFitsConstants.COLUMN_MJD_END).addValueAt(mjdEnd[i], i);
            }
            if ((mjdObs[i] > mjdEnd[i]) || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_INSPOL_MJD_DIFF, this, OIFitsConstants.COLUMN_MJD_OBS).addValuesAt(mjdObs[i], mjdEnd[i], i);
            }
        }
    }
}
