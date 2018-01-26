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
 * Class for OI_T3 table.
 */
public final class OIT3 extends OIData {

    /* static descriptors */
    /** U1COORD column descriptor */
    private final static ColumnMeta COLUMN_U1COORD = new ColumnMeta(OIFitsConstants.COLUMN_U1COORD,
            "U coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** V1COORD column descriptor */
    private final static ColumnMeta COLUMN_V1COORD = new ColumnMeta(OIFitsConstants.COLUMN_V1COORD,
            "V coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** U2COORD column descriptor */
    private final static ColumnMeta COLUMN_U2COORD = new ColumnMeta(OIFitsConstants.COLUMN_U2COORD,
            "U coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
    /** V2COORD column descriptor */
    private final static ColumnMeta COLUMN_V2COORD = new ColumnMeta(OIFitsConstants.COLUMN_V2COORD,
            "V coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);

    /**
     * Public OIT3 class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIT3(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // T3AMP  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMP, "triple product amplitude", Types.TYPE_DBL,
                OIFitsConstants.COLUMN_T3AMPERR, DataRange.RANGE_POSITIVE, this));

        // T3AMPERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMPERR, "error in triple product amplitude",
                Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // T3PHI  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHI, "triple product phase", Types.TYPE_DBL,
                Units.UNIT_DEGREE, OIFitsConstants.COLUMN_T3PHIERR, DataRange.RANGE_ANGLE, this));

        // T3PHIERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHIERR, "error in triple product phase",
                Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_POSITIVE, this));

        // if OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {

            // T3 MODEL columns definition (optional)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_T3AMP, "model of the triple product amplitude",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, DataRange.RANGE_POSITIVE, this));
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_T3PHI, "model of the triple product phase",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.UNIT_DEGREE, null, DataRange.RANGE_ANGLE, this));

        }

        // U1COORD  column definition
        addColumnMeta(COLUMN_U1COORD);

        // V1COORD  column definition
        addColumnMeta(COLUMN_V1COORD);

        // U2COORD  column definition
        addColumnMeta(COLUMN_U2COORD);

        // V2COORD  column definition
        addColumnMeta(COLUMN_V2COORD);

        // STA_INDEX  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data",
                Types.TYPE_SHORT, 3) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        if (oifitsFile.isOIFits2()) {
            // CORRINDX_T3AMP  column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_T3AMP, "Index into correlation matrix for 1st T3AMP element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
            // CORRINDX_T3PHI  column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_T3PHI, "Index into correlation matrix for 1st T3PHI element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
        }

        // Derived SPATIAL_U1_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_U1COORD_SPATIAL, "spatial U1 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V1_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_V1COORD_SPATIAL, "spatial V1 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_U2_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_U2COORD_SPATIAL, "spatial U2 frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V2_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_V2COORD_SPATIAL, "spatial V2 frequency", Types.TYPE_DBL, this));

        // Derived SNR column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SNR_T3PHI, "SNR on " + OIFitsConstants.COLUMN_T3PHI,
                Types.TYPE_DBL, this, "abs(" + OIFitsConstants.COLUMN_T3PHI + " / " + OIFitsConstants.COLUMN_T3PHIERR + ")"));
    }

    /**
     * Public OIT3 class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIT3(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- Columns --- */
    /**
     * Return the T3AMP column.
     * @return the T3AMP column.
     */
    public double[][] getT3Amp() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMP);
    }

    /**
     * Return the T3AMPERR column.
     * @return the T3AMPERR column.
     */
    public double[][] getT3AmpErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMPERR);
    }

    /**
     * Return the T3PHI column.
     * @return the T3PHI column.
     */
    public double[][] getT3Phi() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHI);
    }

    /**
     * Return the T3PHIERR column.
     * @return the T3PHIERR column.
     */
    public double[][] getT3PhiErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHIERR);
    }

    /**
     * Return the U1COORD column.
     * @return the U1COORD column.
     */
    public double[] getU1Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_U1COORD);
    }

    /**
     * Return the V1COORD column.
     * @return the V1COORD column.
     */
    public double[] getV1Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_V1COORD);
    }

    /**
     * Return the U2COORD column.
     * @return the U2COORD column.
     */
    public double[] getU2Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_U2COORD);
    }

    /**
     * Return the V2COORD column.
     * @return the V2COORD column.
     */
    public double[] getV2Coord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_V2COORD);
    }

    /**
     * Return the optional CORRINDX_T3AMP column.
     * @return the CORRINDX_T3AMP column or null if missing.
     */
    public int[] getCorrIndxT3Amp() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_T3AMP);
    }

    /**
     * Return the optional CORRINDX_T3PHI column.
     * @return the CORRINDX_T3PHI column or null if missing.
     */
    public int[] getCorrIndxT3Phi() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_T3PHI);
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
        if (OIFitsConstants.COLUMN_U1COORD_SPATIAL.equals(name)) {
            return getSpatialU1Coord();
        }
        if (OIFitsConstants.COLUMN_V1COORD_SPATIAL.equals(name)) {
            return getSpatialV1Coord();
        }
        if (OIFitsConstants.COLUMN_U2COORD_SPATIAL.equals(name)) {
            return getSpatialU2Coord();
        }
        if (OIFitsConstants.COLUMN_V2COORD_SPATIAL.equals(name)) {
            return getSpatialV2Coord();
        }
        return super.getDerivedColumnAsDoubles(name);
    }

    /* --- Alternate data representation methods --- */
    /**
     * Return the spatial frequencies column. The computation is based
     * on the maximum distance of u1,v1 (AB), u2,v2 (BC) and -(u1+u2), - (v1+v2) (CA) vectors.
     *
     * @return the computed spatial frequencies.
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
                final double[] u1coord = getU1Coord();
                final double[] v1coord = getV1Coord();
                final double[] u2coord = getU2Coord();
                final double[] v2coord = getV2Coord();

                double dist1, dist2, dist3;
                double[] row;
                double c;
                for (int i = 0, j; i < nRows; i++) {
                    row = spatialFreq[i];

                    // mimic OIlib/yorick/oidata.i cridx3
                    dist1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                    dist2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                    // (u3, v3) = (u1, v1) + (u2, v2)
                    dist3 = MathUtils.carthesianNorm(u1coord[i] + u2coord[i], v1coord[i] + v2coord[i]);

                    c = Math.max(Math.max(dist1, dist2), dist3);

                    for (j = 0; j < nWaves; j++) {
                        row[j] = c / effWaves[j];
                    }
                }
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_SPATIAL_FREQ, spatialFreq);
        }

        return spatialFreq;
    }

    /**
     * Return the spatial u1coord.
     * u1coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialU1Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_U1COORD_SPATIAL, OIFitsConstants.COLUMN_U1COORD);
    }

    /**
     * Return the spatial v1coord.
     * v1coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialV1Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_V1COORD_SPATIAL, OIFitsConstants.COLUMN_V1COORD);
    }

    /**
     * Return the spatial u2coord.
     * u2coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialU2Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_U2COORD_SPATIAL, OIFitsConstants.COLUMN_U2COORD);
    }

    /**
     * Return the spatial v2coord.
     * v2coord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialV2Coord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_V2COORD_SPATIAL, OIFitsConstants.COLUMN_V2COORD);
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

            final double[] u1coord = getU1Coord();
            final double[] v1coord = getV1Coord();
            final double[] u2coord = getU2Coord();
            final double[] v2coord = getV2Coord();

            double r1, r2, r3;

            for (int i = 0; i < nRows; i++) {
                r1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                r2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                // (u3, v3) = (u1, v1) + (u2, v2)
                r3 = MathUtils.carthesianNorm(u1coord[i] + u2coord[i], v1coord[i] + v2coord[i]);

                radius[i] = Math.max(Math.max(r1, r2), r3);
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

            final double[] u1coord = getU1Coord();
            final double[] v1coord = getV1Coord();
            final double[] u2coord = getU2Coord();
            final double[] v2coord = getV2Coord();

            double u3, v3;
            double r1, r2, r3;

            for (int i = 0, j; i < nRows; i++) {
                r1 = MathUtils.carthesianNorm(u1coord[i], v1coord[i]);
                r2 = MathUtils.carthesianNorm(u2coord[i], v2coord[i]);

                // (u3, v3) = (u1, v1) + (u2, v2)
                u3 = u1coord[i] + u2coord[i];
                v3 = v1coord[i] + v2coord[i];
                r3 = MathUtils.carthesianNorm(u3, v3);

                j = (r1 >= r2) ? ((r1 >= r3) ? (1) : (3)) : ((r2 >= r3) ? (2) : (3));

                switch (j) {
                    case 1: // r1
                        angle[i] = Math.atan2(u1coord[i], v1coord[i]);
                        break;
                    case 2: // r2
                        angle[i] = Math.atan2(u2coord[i], v2coord[i]);
                        break;
                    case 3: // r3
                        angle[i] = Math.atan2(u3, v3);
                        break;
                    default:
                        angle[i] = 0d;
                }

                angle[i] = Math.toDegrees(angle[i]);
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

        checkColumnError(checker, getFlag(), getT3AmpErr(), this, OIFitsConstants.COLUMN_T3AMPERR);
        checkColumnError(checker, getFlag(), getT3PhiErr(), this, OIFitsConstants.COLUMN_T3PHIERR);

        // check STA_INDEX Unique
        checkStaIndexes(checker, getStaIndex(), this);

        // OIFITS2: check OI_CORR indexes
        final OICorr oiCorr = getOiCorr();
        final int[] corrindx_T3amp = getCorrIndxT3Amp();
        final int[] corrindx_T3phi = getCorrIndxT3Phi();

        if (corrindx_T3amp != null || corrindx_T3phi != null) {
            // rule [OI_T3_CORRINDX] check if the referenced OI_CORR exists when the column CORRINDX_T3AMP or CORRINDX_T3PHI is present
            if (oiCorr == null || OIFitsChecker.isInspectRules()) {
                if (corrindx_T3amp != null) {
                    checker.ruleFailed(Rule.OI_T3_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_T3AMP);
                }
                if (corrindx_T3phi != null) {
                    checker.ruleFailed(Rule.OI_T3_CORRINDX, this, OIFitsConstants.COLUMN_CORRINDX_T3PHI);
                }
            }
            if (oiCorr != null || OIFitsChecker.isInspectRules()) {
                // column is defined
                if (corrindx_T3amp != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_T3AMP, corrindx_T3amp);
                }
                if (corrindx_T3phi != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_T3PHI, corrindx_T3phi);
                }
            }
        }

    }
}
/*___oOo___*/
