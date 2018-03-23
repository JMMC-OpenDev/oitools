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
import fr.jmmc.oitools.meta.CustomUnits;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;

/**
 * Class for OI_Flux table.
 */
public final class OIFlux extends OIData {

    /** 
     * Public OIFlux class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIFlux(final OIFitsFile oifitsFile) {
        // do not use common columns:
        super(oifitsFile, false);

        //keyword
        //CALSTAT keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_CALSTAT, "'C': Spectrum is calibred, 'U': Uncalibred",
                Types.TYPE_CHAR, false, new String[]{OIFitsConstants.KEYWORD_CALSTAT_C, OIFitsConstants.KEYWORD_CALSTAT_U}));
        //WARNING: The keywords FOV and FOVTYPE can be completely different and therefore independent of the column FOV and FOVTYPE from OI_ARRAY
        //FOV keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_FOV, "Area on sky over which flux is integrated",
                Types.TYPE_DBL, Units.UNIT_ARCSEC, true));
        //FOVTYPE keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_FOV_TYPE, "Model for FOV: 'FWHM' or 'RADIUS'",
                Types.TYPE_CHAR, true, new String[]{OIFitsConstants.COLUMN_FOVTYPE_FWHM, OIFitsConstants.COLUMN_FOVTYPE_RADIUS}));

        // FLUXDATA column definition (User unit)
        ColumnMeta colMeta = new WaveColumnMeta(OIFitsConstants.COLUMN_FLUXDATA, "flux per telescope", Types.TYPE_DBL,
                new CustomUnits(), OIFitsConstants.COLUMN_FLUXERR, DataRange.RANGE_POSITIVE, this);
        // GRAVITY: FLUX column definition (optional)
        colMeta.setAlias(OIFitsConstants.COLUMN_FLUX);
        addColumnMeta(colMeta);

        // FLUXERR  column definition (User unit)
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLUXERR, "error in flux", Types.TYPE_DBL,
                new CustomUnits(), DataRange.RANGE_POSITIVE, this));

        // STA_INDEX  column definition
        addColumnMeta(new ArrayColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station number contributing to the data",
                Types.TYPE_SHORT, 1, true) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        // CORRINDX_FLUXDATA  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_FLUXDATA, "Index into correlation matrix for 1st FLUXDATA element",
                Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
    }

    /**
     * Public OIFlux class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIFlux(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- Keywords --- */
    /**
     * Get the value of CALSTAT keyword
     * @return the value of CALSTAT keyword
     */
    public String getCalStat() {
        return getKeyword(OIFitsConstants.KEYWORD_CALSTAT);
    }

    /**
     * Define the value of CALSTAT keyword
     * @param calstat value of CALSTAT keyword
     */
    public void setCalStat(String calstat) {
        setKeyword(OIFitsConstants.KEYWORD_CALSTAT, calstat);
    }

    /**
     * Get the optional value of FOV keyword
     * @return the value of FOV keyword or null if missing.
     */
    public double getFov() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_FOV);
    }

    /**
     * Define the value of FOV keyword
     * @param fov value of FOV keyword
     */
    public void setFov(double fov) {
        setKeywordDouble(OIFitsConstants.KEYWORD_FOV, fov);
    }

    /**
     * Get the optional value of FOVTYPE keyword
     * @return the value of FOVTYPE keyword or null if missing.
     */
    public String getFovType() {
        return getKeyword(OIFitsConstants.KEYWORD_FOV_TYPE);
    }

    /**
     * Define the value of FOVTYPE keyword
     * @param fovtype value of FOVTYPE keyword
     */
    public void setFovType(String fovtype) {
        setKeyword(OIFitsConstants.KEYWORD_FOV_TYPE, fovtype);
    }

    /* --- Columns --- */
    /**
     * Return the FLUXDATA column.
     * @return the FLUXDATA column.
     */
    public double[][] getFluxData() {
        double[][] values = this.getColumnDoubles(OIFitsConstants.COLUMN_FLUXDATA);
        if (values == null) {
            // GRAVITY OI_FLUX:
            values = this.getColumnDoubles(OIFitsConstants.COLUMN_FLUX);
        }
        return values;
    }

    /**
     * Return the FLUXERR column.
     * @return the FLUXERR column.
     */
    public double[][] getFluxErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_FLUXERR);
    }

    /**
     * Return the optional CORRINDX_FLUXDATA column.
     * @return the CORRINDX_FLUXDATA column or null if missing.
     */
    public int[] getCorrIndxData() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_FLUXDATA);
    }

    /* --- Alternate data representation methods --- */
    /**
     * Return the spatial frequencies column.  The computation is based
     * on ucoord and vcoord.
     * sqrt(ucoord^2+vcoord^2)/effWave
     *
     * @return the computed spatial frequencies r[x][y] (x,y for coordIndex,effWaveIndex)
     */
    @Override
    public double[][] getSpatialFreq() {
        return null; // undefined
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    @Override
    public double[] getRadius() {
        return null; // undefined
    }

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    @Override
    public double[] getPosAngle() {
        return null; // undefined
    }

    /* 
     * --- public data access --------------------------------------------------------- 
     */
    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name 
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        return super.getDerivedColumnAsDoubles(name);
    }

    /* --- Other methods --- */
    /** 
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        checkColumnError(checker, getFlag(), getFluxErr(), this, OIFitsConstants.COLUMN_FLUXERR);

        // OIFITS2: check OI_CORR indexes
        final OICorr oiCorr = getOiCorr();
        final int[] corrindx_data = getCorrIndxData();

        if (corrindx_data != null) {
            // rule [OI_FLUX_CORRINDX] check if the referenced OI_CORR table exists when the column CORRINDX_FLUXDATA is present
            if (oiCorr == null || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.OI_FLUX_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_FLUXDATA);
            }
            if (oiCorr != null) {
                // column is defined
                checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_FLUXDATA, corrindx_data);
            }
        }
    }
}
/*___oOo___*/
