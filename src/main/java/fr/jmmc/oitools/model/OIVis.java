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
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.CustomUnits;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.MathUtils;

/**
 * Class for OI_VIS table.
 */
public final class OIVis extends OIData {

    /** CORRINDX_VISAMP column descriptor */
    private final static ColumnMeta COLUMN_CORRINDX_VISAMP = new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_VISAMP,
            "Index into correlation matrix for 1st VISAMP element", Types.TYPE_INT, 1, true, false, Units.NO_UNIT);

    /** CORRINDX_VISPHI column descriptor */
    private final static ColumnMeta COLUMN_CORRINDX_VISPHI = new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_VISPHI,
            "Index into correlation matrix for 1st VISPHI element", Types.TYPE_INT, 1, true, false, Units.NO_UNIT);

    /** CORRINDX_RVIS column descriptor */
    private final static ColumnMeta COLUMN_CORRINDX_RVIS = new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_RVIS,
            "Index into correlation matrix for 1st RVIS element", Types.TYPE_INT, 1, true, false, Units.NO_UNIT);

    /** CORRINDX_IVIS column descriptor */
    private final static ColumnMeta COLUMN_CORRINDX_IVIS = new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_IVIS,
            "Index into correlation matrix for 1st IVIS element", Types.TYPE_INT, 1, true, false, Units.NO_UNIT);

    /**
     * Public OIVis class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIVis(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        if (oifitsFile.isOIFits2()) {
            // AMPTYP  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_AMPTYP,
                    "'absolute', 'differential', 'correlated flux'", Types.TYPE_CHAR, true,
                    new String[]{OIFitsConstants.KEYWORD_AMPTYP_ABSOLUTE, OIFitsConstants.KEYWORD_AMPTYP_DIFF,
                                 OIFitsConstants.KEYWORD_AMPTYP_CORR}));
            // PHITYP  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_PHITYP,
                    "'absolute', 'differential'", Types.TYPE_CHAR, true,
                    new String[]{OIFitsConstants.KEYWORD_PHITYP_ABSOLUTE, OIFitsConstants.KEYWORD_PHITYP_DIFF}));
            // AMPORDER  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_AMPORDER,
                    "Polynomial fit order for differential chromatic amplitudes", Types.TYPE_INT, true, NO_STR_VALUES));
            // PHIORDER  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_PHIORDER,
                    "Polynomial fit order for differential chromatic phases", Types.TYPE_INT, true, NO_STR_VALUES));
        }

        if (DataModel.hasOiVisComplexSupport()) {
            // Optional Complex visibilities (ASPRO or AMBER - not OIFits) :
            // VISDATA column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISDATA, "raw complex visibilities",
                    Types.TYPE_COMPLEX, true, false, NO_STR_VALUES, new CustomUnits(), OIFitsConstants.COLUMN_VISERR, null, this)
                    .setOrientationDependent(true));

            // VISERR  column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISERR, "error in raw complex visibilities",
                    Types.TYPE_COMPLEX, true, false, NO_STR_VALUES, new CustomUnits(), null, null, this));
        }

        if (oifitsFile.isOIFits2()) {
            // VISAMP  column definition (User unit required only for AMPTYPE='correlated flux')
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMP, "visibility amplitude",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(false), OIFitsConstants.COLUMN_VISAMPERR, DataRange.RANGE_VIS, this));

            // VISAMPERR  column definition (User unit required only for AMPTYPE='correlated flux')
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMPERR, "error in visibility amplitude",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(false), null, DataRange.RANGE_POSITIVE, this));
        } else {
            // VISAMP  column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMP, "visibility amplitude",
                    Types.TYPE_DBL, OIFitsConstants.COLUMN_VISAMPERR, DataRange.RANGE_VIS, this));

            // VISAMPERR  column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMPERR, "error in visibility amplitude",
                    Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));
        }

        // VISPHI  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHI, "visibility phase", Types.TYPE_DBL,
                Units.UNIT_DEGREE, OIFitsConstants.COLUMN_VISPHIERR, DataRange.RANGE_ANGLE, this)
                .setOrientationDependent(true));

        // VISPHIERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHIERR, "error in visibility phase",
                Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_POSITIVE, this));

        // if IMAGE_OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {
            // VIS MODEL columns definition (optional)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_VISAMP, "model of the visibility amplitude",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, DataRange.RANGE_VIS, this));
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_VISPHI, "model of the visibility phase",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.UNIT_DEGREE, null, DataRange.RANGE_ANGLE, this)
                    .setOrientationDependent(true));
        }

        // UCOORD  column definition
        addColumnMeta(COLUMN_UCOORD);

        // VCOORD  column definition
        addColumnMeta(COLUMN_VCOORD);

        // STA_INDEX  column definition
        addColumnMeta(new ArrayColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data",
                Types.TYPE_SHORT, 2, false) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        if (oifitsFile.isOIFits2()) {
            // CORRINDX_VISAMP column definition
            addColumnMeta(COLUMN_CORRINDX_VISAMP);
            // CORRINDX_VISPHI column definition
            addColumnMeta(COLUMN_CORRINDX_VISPHI);
            // VISREFMAP column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISREFMAP, "Matrix of indexes for establishing the reference channels",
                    Types.TYPE_LOGICAL, true, true, NO_STR_VALUES, Units.NO_UNIT, null, null, this));

            // RVIS column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RVIS, "Complex coherent flux (Real) in units of TUNITn",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(), OIFitsConstants.COLUMN_RVISERR, null, this)
                    .setOrientationDependent(true));
            // RVISERR column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RVISERR, "Error RVIS",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(), null, DataRange.RANGE_POSITIVE, this));

            // IVIS column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_IVIS, "Complex coherent flux (Imaginary) in units of TUNITn",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(), OIFitsConstants.COLUMN_IVISERR, null, this)
                    .setOrientationDependent(true));
            // IVISERR column definition (User unit)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_IVISERR, "Error IVIS",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, new CustomUnits(), null, DataRange.RANGE_POSITIVE, this));

            // CORRINDX_RVIS column definition
            addColumnMeta(COLUMN_CORRINDX_RVIS);
            addColumnMeta(COLUMN_CORRINDX_IVIS);
        }

        // Derived SPATIAL_U_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_U,
                "spatial U frequency", Types.TYPE_DBL, this).setOrientationDependent(true)
                .setAlias(OIFitsConstants.COLUMN_UCOORD_SPATIAL));

        // Derived SPATIAL_V_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_V,
                "spatial V frequency", Types.TYPE_DBL, this).setOrientationDependent(true)
                .setAlias(OIFitsConstants.COLUMN_VCOORD_SPATIAL));

        if (false) {
            // invalid SNR definition => disabled
            // Derived SNR column definition
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SNR_VISAMP, "SNR on " + OIFitsConstants.COLUMN_VISAMP,
                    Types.TYPE_DBL, this, "abs(" + OIFitsConstants.COLUMN_VISAMP + " / " + OIFitsConstants.COLUMN_VISAMPERR + ")"));
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SNR_VISPHI, "SNR on " + OIFitsConstants.COLUMN_VISPHI,
                    Types.TYPE_DBL, this, "abs(" + OIFitsConstants.COLUMN_VISPHI + " / " + OIFitsConstants.COLUMN_VISPHIERR + ")"));
        }

        // if IMAGE_OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {
            // Derived RES_VISAMP_MODEL columns definition (optional)
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RES_VISAMP_MODEL, "Residual between on " + OIFitsConstants.COLUMN_VISAMP
                    + " vs " + OIFitsConstants.COLUMN_NS_MODEL_VISAMP + " (sigma)", Types.TYPE_DBL, this,
                    "(" + OIFitsConstants.COLUMN_VISAMP + " - " + OIFitsConstants.COLUMN_NS_MODEL_VISAMP + ") / " + OIFitsConstants.COLUMN_VISAMPERR,
                    DataRange.RANGE_SIGMA));
            // Derived RES_VISPHI_MODEL columns definition (optional)
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RES_VISPHI_MODEL, "Residual between on " + OIFitsConstants.COLUMN_VISPHI
                    + " vs " + OIFitsConstants.COLUMN_NS_MODEL_VISPHI + " (sigma)", Types.TYPE_DBL, this,
                    "distanceAngle(" + OIFitsConstants.COLUMN_VISPHI + "," + OIFitsConstants.COLUMN_NS_MODEL_VISPHI + ") / " + OIFitsConstants.COLUMN_VISPHIERR,
                    DataRange.RANGE_SIGMA)
                    .setOrientationDependent(true));
        }
    }

    /**
     * Public OIVis class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIVis(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /**
     * Public OIVis class constructor to copy the given table (structure only)
     * @param oifitsFile main OifitsFile
     * @param src table to copy
     */
    public OIVis(final OIFitsFile oifitsFile, final OIVis src) {
        this(oifitsFile);

        this.copyTable(src);
    }

    /* --- keywords --- */
    /**
     * Get the optional value of AMPTYPE keyword
     * @return the value of AMPTYPE keyword or null if missing.
     */
    public String getAmpTyp() {
        return getKeyword(OIFitsConstants.KEYWORD_AMPTYP);
    }

    /**
     * Define the value of AMPTYPE keyword
     * @param amptype value of AMPTYPE keyword
     */
    public void setAmpTyp(String amptype) {
        setKeyword(OIFitsConstants.KEYWORD_AMPTYP, amptype);
    }

    /**
     * Get the optional value of PHITYP keyword
     * @return the value of PHITYP keyword or null if missing.
     */
    public String getPhiType() {
        return getKeyword(OIFitsConstants.KEYWORD_PHITYP);
    }

    /**
     * Define the value of PHITYP keyword
     * @param phitype value of PHITYP keyword
     */
    public void setPhiTyp(String phitype) {
        setKeyword(OIFitsConstants.KEYWORD_PHITYP, phitype);
    }

    /**
     * Get the optional value of AMPORDER keyword
     * @return the value of AMPORDER keyword or null if missing.
     */
    public int getAmpOrder() {
        return getKeywordInt(OIFitsConstants.KEYWORD_AMPORDER);
    }

    /**
     * Define the value of AMPORDER, keyword
     * @param amporder value of AMPORDER, keyword
     */
    public void setAmpOrder(int amporder) {
        setKeywordInt(OIFitsConstants.KEYWORD_AMPORDER, amporder);
    }

    /**
     * Get the optional value of PHIORDER keyword
     * @return the value of PHIORDER keyword or null if missing.
     */
    public int getPhiOrder() {
        return getKeywordInt(OIFitsConstants.KEYWORD_PHIORDER);
    }

    /**
     * Define the value of PHIORDER, keyword
     * @param phiorder value of PHIORDER, keyword
     */
    public void setPhiOrder(int phiorder) {
        setKeywordInt(OIFitsConstants.KEYWORD_PHIORDER, phiorder);
    }

    /* --- Columns --- */
    /**
     * Return the optional VISDATA column.
     * @return the VISDATA column or null if missing.
     */
    public float[][][] getVisData() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_VISDATA);
    }

    /**
     * Return the optional VISERR column.
     * @return the VISERR column or null if missing.
     */
    public float[][][] getVisErr() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_VISERR);
    }

    /**
     * Return the VISAMP column.
     * @return the VISAMP column.
     */
    public double[][] getVisAmp() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISAMP);
    }

    /**
     * Return the VISAMPERR column.
     * @return the VISAMPERR column.
     */
    public double[][] getVisAmpErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISAMPERR);
    }

    /**
     * Return the VISPHI column.
     * @return the VISPHI column.
     */
    public double[][] getVisPhi() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISPHI);
    }

    /**
     * Return the VISPHIERR column.
     * @return the VISPHIERR column.
     */
    public double[][] getVisPhiErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISPHIERR);
    }

    /**
     * Return the UCOORD column.
     * @return the UCOORD column.
     */
    public double[] getUCoord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_UCOORD);
    }

    /**
     * Return the VCOORD column.
     * @return the VCOORD column.
     */
    public double[] getVCoord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_VCOORD);
    }

    /**
     * Return the optional CORRINDX_VISAMP column.
     * @return the CORRINDX_VISAMP column or null if missing.
     */
    public int[] getCorrIndxVisAmp() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_VISAMP);
    }

    /**
     * Return the optional CORRINDX_VISPHI column.
     * @return the CORRINDX_VISPHI column or null if missing.
     */
    public int[] getCorrIndxVisPhi() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_VISPHI);
    }

    /**
     * Return the optional VISREFMAP column.
     * @return the VISREFMAP column or null if missing.
     */
    public boolean[][][] getVisRefMap() {
        return this.getColumnBoolean3D(OIFitsConstants.COLUMN_VISREFMAP);
    }

    /**
     * Return the optional RVIS column.
     * @return the RVIS column or null if missing.
     */
    public double[][] getRVis() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_RVIS);
    }

    /**
     * Return the optional RVISERR column.
     * @return the RVISERR column or null if missing.
     */
    public double[][] getRVisErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_RVISERR);
    }

    /**
     * Return the optional CORRINDX_RVIS column.
     * @return the CORRINDX_RVIS column or null if missing.
     */
    public int[] getCorrIndxRVis() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_RVIS);
    }

    /**
     * Return the optional IVIS column.
     * @return the IVIS column or null if missing.
     */
    public double[][] getIVis() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_IVIS);
    }

    /**
     * Return the optional IVISERR column.
     * @return the IVISERR column or null if missing.
     */
    public double[][] getIVisErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_IVISERR);
    }

    /**
     * Return the optional CORRINDX_IVIS column.
     * @return the CORRINDX_IVIS column or null if missing.
     */
    public int[] getCorrIndxIVis() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_IVIS);
    }

    /* --- Optional extra columns for OI-Interface Model --- */
    /**
     * Return the NS_MODEL_VISAMP column.
     * @return the NS_MODEL_VISAMP column.
     */
    public double[][] getModelVisAmp() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_NS_MODEL_VISAMP);
    }

    /**
     * Return the NS_MODEL_VISPHI column.
     * @return the NS_MODEL_VISPHI column.
     */
    public double[][] getModelVisPhi() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_NS_MODEL_VISPHI);
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
        if (OIFitsConstants.COLUMN_U.equals(name)) {
            return getSpatialUCoord();
        }
        if (OIFitsConstants.COLUMN_V.equals(name)) {
            return getSpatialVCoord();
        }
        return super.getDerivedColumnAsDoubles(name);
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
        // lazy:
        double[][] spatialFreq = this.getColumnDerivedDoubles(OIFitsConstants.COLUMN_SPATIAL_FREQ);

        if (spatialFreq == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            spatialFreq = new double[nRows][nWaves];

            if (nWaves != 0) {
                final double[] effWaves = getOiWavelength().getEffWaveAsDouble();
                final double[] ucoord = getUCoord();
                final double[] vcoord = getVCoord();

                double[] row;
                double r;
                for (int i = 0, j; i < nRows; i++) {
                    row = spatialFreq[i];
                    r = MathUtils.carthesianNorm(ucoord[i], vcoord[i]);

                    for (j = 0; j < nWaves; j++) {
                        row[j] = r / effWaves[j];
                    }
                }
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_SPATIAL_FREQ, spatialFreq);
        }

        return spatialFreq;
    }

    /**
     * Return the spatial ucoord.
     * ucoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialUCoord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_U, OIFitsConstants.COLUMN_UCOORD);
    }

    /**
     * Return the spatial vcoord.
     * vcoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialVCoord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_V, OIFitsConstants.COLUMN_VCOORD);
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    @Override
    public double[] getRadius() {
        // lazy:
        double[] radius = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_RADIUS);

        if (radius == null) {
            final int nRows = getNbRows();
            radius = new double[nRows];

            final double[] ucoord = getUCoord();
            final double[] vcoord = getVCoord();

            for (int i = 0; i < nRows; i++) {
                radius[i] = MathUtils.carthesianNorm(ucoord[i], vcoord[i]);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_RADIUS, radius);
        }

        return radius;
    }

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    @Override
    public double[] getPosAngle() {
        // lazy:
        double[] angle = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_POS_ANGLE);

        if (angle == null) {
            final int nRows = getNbRows();
            angle = new double[nRows];

            final double[] ucoord = getUCoord();
            final double[] vcoord = getVCoord();

            for (int i = 0, j; i < nRows; i++) {
                angle[i] = Math.toDegrees(Math.atan2(ucoord[i], vcoord[i]));
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_POS_ANGLE, angle);
        }

        return angle;
    }

    /* --- Other methods --- */
    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        checkColumnError(checker, getFlag(), getVisAmpErr(), this, OIFitsConstants.COLUMN_VISAMPERR);
        checkColumnError(checker, getFlag(), getVisPhiErr(), this, OIFitsConstants.COLUMN_VISPHIERR);

        // optional columns
        final double[][] rvisErr = getRVisErr();
        final double[][] ivisErr = getIVisErr();
        if (rvisErr != null) {
            checkColumnError(checker, getFlag(), rvisErr, this, OIFitsConstants.COLUMN_RVISERR);
        }
        if (ivisErr != null) {
            checkColumnError(checker, getFlag(), ivisErr, this, OIFitsConstants.COLUMN_IVISERR);
        }

        // check STA_INDEX Unique
        checkStaIndexes(checker, getStaIndex(), this);

        // OIFITS2: check OI_CORR indexes
        final OICorr oiCorr = getOiCorr();
        final int[] corrindx_visAmp = getCorrIndxVisAmp();
        final int[] corrindx_visPhi = getCorrIndxVisPhi();
        final int[] corrindx_visRvis = getCorrIndxRVis();
        final int[] corrindx_visIvis = getCorrIndxIVis();

        if ((corrindx_visAmp != null) || (corrindx_visPhi != null) || (corrindx_visRvis != null) || (corrindx_visIvis != null)) {

            if ((oiCorr == null) || OIFitsChecker.isInspectRules()) {
                // rule [OI_VIS_CORRINDX] check if the referenced OI_CORR table exists when the column CORRINDX_VISAMP, CORRINDX_VISPHI, CORRINDX_RVIS or CORRINDX_IVIS is present

                // column is defined
                if (corrindx_visAmp != null) {
                    if (checker != null) {
                        checker.ruleFailed(Rule.OI_VIS_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_VISAMP);
                    }
                }
                if (corrindx_visPhi != null) {
                    if (checker != null) {
                        checker.ruleFailed(Rule.OI_VIS_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_VISPHI);
                    }
                }
                if (corrindx_visRvis != null) {
                    if (checker != null) {
                        checker.ruleFailed(Rule.OI_VIS_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_RVIS);
                    }
                }
                if (corrindx_visIvis != null) {
                    if (checker != null) {
                        checker.ruleFailed(Rule.OI_VIS_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_IVIS);
                    }
                }
            }
            if ((oiCorr != null) || OIFitsChecker.isInspectRules()) {
                // column is defined
                if (corrindx_visAmp != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_VISAMP, corrindx_visAmp);
                }
                if (corrindx_visPhi != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_VISPHI, corrindx_visPhi);
                }
                if (corrindx_visRvis != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_RVIS, corrindx_visRvis);
                }
                if (corrindx_visIvis != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_IVIS, corrindx_visIvis);
                }
            }
        }
    }
}
