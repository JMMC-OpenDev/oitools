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
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.MathUtils;

/**
 * Class for OI_VIS2 table.
 */
public final class OIVis2 extends OIData {

    /**
     * Public OIVis2 class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIVis2(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // VIS2DATA  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2DATA, "squared visibility", Types.TYPE_DBL,
                OIFitsConstants.COLUMN_VIS2ERR, DataRange.RANGE_VIS, this));

        // VIS2ERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2ERR, "error in squared visibility", Types.TYPE_DBL,
                DataRange.RANGE_POSITIVE, this));

        // if OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {

            // VIS2 MODEL column definition (optional)
            ColumnMeta colMeta = new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_VIS2DATA, "model of the squared visibility",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, DataRange.RANGE_POSITIVE, this);
            // NS_MODEL_VIS2 column definition (bsmem convention)
            colMeta.setAlias("NS_MODEL_VIS2");
            addColumnMeta(colMeta);

        }

        // UCOORD  column definition
        addColumnMeta(COLUMN_UCOORD);

        // VCOORD  column definition
        addColumnMeta(COLUMN_VCOORD);

        // STA_INDEX  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX,
                "station numbers contributing to the data", Types.TYPE_SHORT, 2) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        if (oifitsFile.isOIFits2()) {
            // CORRINDX_VIS2DATA  column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_VIS2DATA, "Index into correlation matrix for 1st VIS2DATA element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
        }

        // Derived SPATIAL_U_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_UCOORD_SPATIAL,
                "spatial U frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VCOORD_SPATIAL,
                "spatial V frequency", Types.TYPE_DBL, this));

        // Derived SNR column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SNR_VIS2, "SNR on " + OIFitsConstants.COLUMN_VIS2DATA,
                Types.TYPE_DBL, this, "abs(" + OIFitsConstants.COLUMN_VIS2DATA + " / " + OIFitsConstants.COLUMN_VIS2ERR + ")"));
    }

    /**
     * Public OIVis2 class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIVis2(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- Columns --- */
    /**
     * Return the VIS2DATA column.
     * @return the VIS2DATA column.
     */
    public double[][] getVis2Data() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VIS2DATA);
    }

    /**
     * Return the VIS2ERR column.
     * @return the VIS2ERR column.
     */
    public double[][] getVis2Err() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VIS2ERR);
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
     * Return the optional CORRINDX_VIS2DATA column.
     * @return the CORRINDX_VIS2DATA column or null if missing.
     */
    public int[] getCorrIndxVisData() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_VIS2DATA);
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
        if (OIFitsConstants.COLUMN_UCOORD_SPATIAL.equals(name)) {
            return getSpatialUCoord();
        }
        if (OIFitsConstants.COLUMN_VCOORD_SPATIAL.equals(name)) {
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
        return getSpatialCoord(OIFitsConstants.COLUMN_UCOORD_SPATIAL, OIFitsConstants.COLUMN_UCOORD);
    }

    /**
     * Return the spatial vcoord.
     * vcoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialVCoord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_VCOORD_SPATIAL, OIFitsConstants.COLUMN_VCOORD);
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

        // Check that non-flagged data point has valid errors:
        checkColumnError(checker, getFlag(), getVis2Err(), this, OIFitsConstants.COLUMN_VIS2ERR);

        // check STA_INDEX Unique
        checkStaIndexes(checker, getStaIndex(), this);

        // OIFITS2: check OI_CORR indexes
        final OICorr oiCorr = getOiCorr();
        final int[] corrindx_vis2data = getCorrIndxVisData();

        if (corrindx_vis2data != null) {
            if (oiCorr == null || OIFitsChecker.isInspectRules()) {
                // rule [OI_VIS2_CORRINDX] check if the referenced OI_CORR table exists when the column CORRINDX_VIS2DATA is present
                checker.ruleFailed(Rule.OI_VIS2_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_VIS2DATA);
            }
            if (oiCorr != null || OIFitsChecker.isInspectRules()) {
                // column is defined
                checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_VIS2DATA, corrindx_vis2data);
            }
        }
    }
}
