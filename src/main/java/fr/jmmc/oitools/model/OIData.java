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
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHDU;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import static fr.jmmc.oitools.model.ModelBase.logger;
import fr.jmmc.oitools.util.MathUtils;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * OIData table is the base class for OI_VIS, OI_VIS2 and OI_T3 tables.
 */
public abstract class OIData extends OIAbstractData {

    /* constants */
    /** Inverse of century */
    public final static double INV_CENTURY = 1d / 36525d; // 1/ (100 * YEAR)

    /* static descriptors */
    /** DATE-OBS keyword descriptor */
    final static KeywordMeta KEYWORD_DATE_OBS = new KeywordMeta(OIFitsConstants.KEYWORD_DATE_OBS,
            "UTC start date of observations", Types.TYPE_CHAR);
    /** TIME column descriptor */
    private final static ColumnMeta COLUMN_TIME = new ColumnMeta(OIFitsConstants.COLUMN_TIME,
            "UTC time of observation", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE);
    /** MJD column descriptor */
    private final static ColumnMeta COLUMN_MJD = new ColumnMeta(OIFitsConstants.COLUMN_MJD,
            "modified Julian Day", Types.TYPE_DBL, Units.UNIT_MJD);
    /** INT_TIME column descriptor */
    private final static ColumnMeta COLUMN_INT_TIME = new ColumnMeta(OIFitsConstants.COLUMN_INT_TIME,
            "integration time", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE_STRICT);
    /** UCOORD column descriptor */
    protected final static ColumnMeta COLUMN_UCOORD = new ColumnMeta(OIFitsConstants.COLUMN_UCOORD,
            "U coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
    /** VCOORD column descriptor */
    protected final static ColumnMeta COLUMN_VCOORD = new ColumnMeta(OIFitsConstants.COLUMN_VCOORD,
            "V coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);

    /** members */
    /** cached reference on OI_WAVELENGTH table associated to this OIData table */
    private OIWavelength oiWavelengthRef = null;
    /** cached reference on OI_CORR table associated to this OIData table */
    private OICorr oiCorrRef = null;
    /* cached analyzed data */
    /** number of data flagged out (-1 means undefined) */
    private int nFlagged = -1;
    /** distinct StaConf values present in this table (station configuration) (sorted) */
    private final Set<short[]> distinctStaConf = new LinkedHashSet<short[]>();

    /**
     * Protected OIData class constructor
     * @param oifitsFile main OifitsFile
     */
    protected OIData(final OIFitsFile oifitsFile) {
        this(oifitsFile, true);
    }

    /**
     * Protected OIData class constructor
     * @param oifitsFile main OifitsFile
     * @param useCommonCols flag indicating to addValueAtRows common columns (OI_VIS, OI_VIS2, OI_T3, OI_FLUX)
     */
    protected OIData(final OIFitsFile oifitsFile, final boolean useCommonCols) {
        super(oifitsFile);

        // since every child class constructor calls the super
        // constructor, next keywords will be common to every subclass :
        // DATE-OBS  keyword definition
        addKeywordMeta(KEYWORD_DATE_OBS);

        // INSNAME  keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_INSNAME, "name of corresponding detector", Types.TYPE_CHAR) {
            @Override
            public String[] getStringAcceptedValues() {
                return getOIFitsFile().getAcceptedInsNames();
            }
        });

        if (oifitsFile.isOIFits2()) {
            // CORRNAME optional keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_CORRNAME, "Identifies corresponding OI_CORR table", Types.TYPE_CHAR, true, NO_STR_VALUES) {
                @Override
                public String[] getStringAcceptedValues() {
                    return getOIFitsFile().getAcceptedCorrNames();
                }
            });
        }

        if (useCommonCols) {
            // TIME  column definition
            addColumnMeta(COLUMN_TIME);
        }

        // MJD  column definition
        addColumnMeta(COLUMN_MJD);

        // INT_TIME  column definition
        addColumnMeta(COLUMN_INT_TIME);

        // Derived STA_CONF column definition
        addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_CONF, "station configuration", Types.TYPE_SHORT, 2)); // fake repeat to mimic 2D array

        // Derived EFF_WAVE (double) column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE, "effective wavelength of channel", Types.TYPE_DBL, Units.UNIT_METER, this));

        if (useCommonCols) {
            // Derived HOUR_ANGLE column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_HOUR_ANGLE, "hour angle", Types.TYPE_DBL, Units.UNIT_HOUR));

            // Derived RADIUS column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_RADIUS, "radius i.e. projected base line", Types.TYPE_DBL, Units.UNIT_METER, DataRange.RANGE_POSITIVE));

            // Derived POS_ANGLE column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_POS_ANGLE, "position angle of the projected base line", Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_ANGLE));

            // Derived SPATIAL_FREQ column definition
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SPATIAL_FREQ, "spatial frequencies", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));
        }

        // Derived NIGHT_ID column definition
        addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_NIGHT_ID, "night identifier", Types.TYPE_DBL));
    }

    /*
     * --- Keywords ------------------------------------------------------------
     */
    /**
     * Get the DATE-OBS keyword value.
     * @return the value of DATE-OBS keyword
     */
    public final String getDateObs() {
        final String dateObs = getRawDateObs();
        if (dateObs != null && dateObs.length() > FitsConstants.FORMAT_DATE.length()) {
            return dateObs.substring(0, FitsConstants.FORMAT_DATE.length());
        }
        return dateObs;
    }

    /**
     * Get the raw DATE-OBS keyword value.
     * @return the value of DATE-OBS keyword (raw)
     */
    public final String getRawDateObs() {
        return getKeyword(OIFitsConstants.KEYWORD_DATE_OBS);
    }

    /**
     * Define the DATE-OBS keyword value
     * @param dateObs value of DATE-OBS keyword
     */
    public final void setDateObs(final String dateObs) {
        setKeyword(OIFitsConstants.KEYWORD_DATE_OBS, dateObs);
    }

    /**
     * Get the INSNAME keyword value.
     * @return the value of INSNAME keyword
     */
    public final String getInsName() {
        return getKeyword(OIFitsConstants.KEYWORD_INSNAME);
    }

    /**
     * Define the INSNAME keyword value
     * @param insName value of INSNAME keyword
     */
    public final void setInsName(final String insName) {
        setKeyword(OIFitsConstants.KEYWORD_INSNAME, insName);
        // reset cached reference :
        this.oiWavelengthRef = null;
    }

    /**
     * Get the CORRNAME keyword value.
     * @return the value of CORRNAME keyword
     */
    public final String getCorrName() {
        return getKeyword(OIFitsConstants.KEYWORD_CORRNAME);
    }

    /**
     * Define the CORRNAME keyword value
     * @param corrName value of CORRNAME keyword
     */
    public final void setCorrName(final String corrName) {
        setKeyword(OIFitsConstants.KEYWORD_CORRNAME, corrName);
        // reset cached reference :
        this.oiCorrRef = null;
    }

    /*
     * --- Columns -------------------------------------------------------------
     */
    /**
     * Return the TIME column.
     * @return the TIME column.
     */
    public double[] getTime() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_TIME);
    }

    /**
     * Return the MJD column.
     * @return the MJD column.
     */
    public double[] getMJD() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_MJD);
    }

    /**
     * Return the INT_TIME column.
     * @return the INT_TIME column.
     */
    public double[] getIntTime() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_INT_TIME);
    }

    /**
     * Return the FLAG column.
     * @return the FLAG column.
     */
    public final boolean[][] getFlag() {
        return this.getColumnBooleans(OIFitsConstants.COLUMN_FLAG);
    }

    /* --- Derived Column from expression --- */
    /**
     * Verify the validity of the expression
     * @param name name of the new column
     * @param expression expression to verify
     * @throws RuntimeException
     */
    public void checkExpression(final String name, final String expression) throws RuntimeException {

        // Test if name conflicts with standard columns (VIS2DATA)
        if (getColumnDesc(name) != null) {
            throw new IllegalArgumentException("Column name [" + name + "] already existing (OIFITS standard) !");
        }

        // Try expression now: may throw RuntimeException
        ExpressionEvaluator.getInstance().eval(this, name, expression, true);
    }

    /**
     * Make the creation or modification of a column given its name and expression
     * @param name name of the column
     * @param expression expression of the column
     */
    public void updateExpressionColumn(final String name, final String expression) {
        // remove column (descriptor and values) if existing:
        removeExpressionColumn(name);

        addDerivedColumnMeta(new WaveColumnMeta(name, "expression: " + expression,
                Types.TYPE_DBL, this, expression));

        // Force computation now (not lazy):
        getExprColumnDoubles(name, expression);
    }

    /**
     * Remove the column given its name.
     * @param name name of the column
     */
    public void removeExpressionColumn(final String name) {
        final ColumnMeta column = getColumnDerivedDesc(name);

        // check if the column has an expression
        if (column instanceof WaveColumnMeta) {
            final WaveColumnMeta colMeta = (WaveColumnMeta) column;

            if (colMeta.getExpression() != null) {
                // remove descriptor:
                removeDerivedColumnMeta(name);

                // remove values:
                removeColumnDerivedValue(name);
            }
        }
    }


    /* --- Alternate data representation methods --- */
    /**
     * Return the effective wavelength of channel as double arrays (2D)
     * @return the wavelength of channel array as double arrays (2D)
     */
    public final double[][] getEffWaveAsDoubles() {
        // lazy:
        double[][] effWaveDbls = this.getColumnDerivedDoubles(OIFitsConstants.COLUMN_EFF_WAVE);

        if (effWaveDbls == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            effWaveDbls = new double[nRows][nWaves];

            if (nWaves != 0) {
                final double[] effWaves = getOiWavelength().getEffWaveAsDouble();

                for (int i = 0; i < nRows; i++) {
                    effWaveDbls[i] = effWaves;
                }
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_EFF_WAVE, effWaveDbls);
        }

        return effWaveDbls;
    }

    /**
     * Return the station configuration as short arrays (2D)
     * @see Analyzer#processStaConf(fr.jmmc.oitools.model.OIData) which fills that column
     * @return the station configuration as short arrays (2D)
     */
    public short[][] getStaConf() {
        // lazy:
        short[][] staConfs = this.getColumnDerivedShorts(OIFitsConstants.COLUMN_STA_CONF);

        if (staConfs == null) {
            staConfs = new short[getNbRows()][];

            // not filled here: see Analyzer
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_STA_CONF, staConfs);
        }

        return staConfs;
    }

    /**
     * Return the spatial frequencies column.
     *
     * @return the computed spatial frequencies f[x][y] (x,y for coordIndex, effWaveIndex)
     */
    public abstract double[][] getSpatialFreq();

    /**
     * Return the spatial coordinates given the coordinates array = coordinates / effWave
     * @param name derived column name to get/store spatial coordinates
     * @param coordName coord column name
     * @return the computed spatial coordinates f[x][y] (x,y for coordIndex, effWaveIndex) .
     */
    protected double[][] getSpatialCoord(final String name, final String coordName) {
        // lazy:
        double[][] spatialCoord = this.getColumnDerivedDoubles(name);

        if (spatialCoord == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            spatialCoord = new double[nRows][nWaves];

            if (nWaves != 0) {
                final double[] effWaves = getOiWavelength().getEffWaveAsDouble();
                final double[] coord = getColumnAsDouble(coordName);

                double[] row;
                double c;
                for (int i = 0, j; i < nRows; i++) {
                    row = spatialCoord[i];
                    c = coord[i];
                    for (j = 0; j < nWaves; j++) {
                        row[j] = c / effWaves[j];
                    }
                }
            }
            this.setColumnDerivedValue(name, spatialCoord);
        }

        return spatialCoord;
    }

    /**
     * Return the array with the given expression
     * @param name derived column name
     * @param expression expression entered by the user
     * @return the computed expression f[x][y]
     */
    protected double[][] getExprColumnDoubles(final String name, final String expression) {
        // lazy: get previously computed results:
        double[][] exprResults = this.getColumnDerivedDoubles(name);

        // check if expression changed ?
        if (exprResults == null) {
            // not computed; do it now (LAZY):
            exprResults = ExpressionEvaluator.getInstance().eval(this, name, expression, false);

            // store computed results for next time:
            this.setColumnDerivedValue(name, exprResults);
        }
        return exprResults;
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    public abstract double[] getRadius();

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    public abstract double[] getPosAngle();

    /**
     * Return the hour angle column.
     *
     * @return the computed hour angle
     */
    public double[] getHourAngle() {
        // lazy:
        double[] hourAngle = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_HOUR_ANGLE);

        if (hourAngle == null) {
            final boolean isLogDebug = logger.isLoggable(Level.FINE);

            final int nRows = getNbRows();
            hourAngle = new double[nRows];

            // Initialize hour angle to NaN:
            Arrays.fill(hourAngle, UNDEFINED_DBL);

            // Get array and target tables:
            final OIArray oiArray = getOiArray();

            if (oiArray != null && OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC.equalsIgnoreCase(oiArray.getFrame())) {
                final OITarget oiTarget = getOiTarget();

                if (oiTarget != null) {
                    final double[] arrayXYZ = oiArray.getArrayXYZ();

                    // ensure coordinates are on earth (not undefined; expected correctly set)
                    if (MathUtils.carthesianNorm(arrayXYZ[0], arrayXYZ[1], arrayXYZ[2]) > OIArray.MIN_EARTH_RADIUS) {

                        final double[] lonLatDist = MathUtils.cartesianToSpherical(arrayXYZ);
                        final double arrayLongitude = Math.toDegrees(lonLatDist[0]);

                        if (isLogDebug) {
                            logger.log(Level.FINE, "arrayLongitude = {0} (deg)", arrayLongitude);
                        }

                        // Get Target RA/DE columns:
                        final double[] ra = oiTarget.getRaEp0();

                        // Get Target Id column:
                        final short[] targetId = getTargetId();

                        // Get MJD column:
                        final double[] mjd = getMJD();

                        Integer rowTarget;
                        double j2000, gmst, T, EPS, OMEGA, L, L1, dL, dE, dT, gast, last;
                        double targetRA, ha;

                        for (int i = 0; i < nRows; i++) {
                            // From Aspro1 formula:
                            // Modified using Matlab JD2GAST:
                            // http://www.mathworks.com/matlabcentral/fileexchange/28232-convert-julian-date-to-greenwich-apparent-sidereal-time/content/JD2GAST.m

                            // let j2000 OI_DATA%OI_VIS2%COL%MJD-51544.5
                            j2000 = mjd[i] - MJD_2000; // days from J2000

                            // let julcen j2000/36525.0 (fraction of epoch/century time elapsed since J2000)
                            T = j2000 * INV_CENTURY;

                            // let gmst mod(280.46061837+360.98564736629*j2000,360.0)
                            // gmst = (280.46061837 + 360.98564736629 * j2000) % 360d; // Greenwich Mean Sidereal Time (deg)
                            gmst = ((280.46061837 + 360.98564736629 * j2000) + 0.000387933 * T * T - T * T * T / 38710000.0) % 360.0;

                            // Obliquity of the Ecliptic
                            // let eps 23.43929111-46.815/60/60*julcen
                            EPS = 23.439291 - 0.0130111 * T - 1.64E-07 * T * T + 5.04E-07 * T * T * T; // matlab (deg)

                            // let Om mod(125.04452-1934.136261*julcen,360.0)*PI|180
                            OMEGA = Math.toRadians((125.04452 - 1934.136261 * T) % 360d); // ascending node of sun

                            // let L mod(280.4665+36000.7698*julcen,360.0)*PI|180
                            L = Math.toRadians((280.4665 + 36000.7698 * T) % 360d); // MeanLongOfSun

                            // let L1 mod(218.3165+481267.8813*JULCEN,360.0)*PI|180
                            L1 = Math.toRadians((218.3165 + 481267.8813 * T) % 360d); // MeanLongOfMoon

                            // Explanations:
                            // http://www.cv.nrao.edu/~rfisher/Ephemerides/earth_rot.html#nut
                            // change in the ecliptic longitude of a star due to the nutation (good to about 0.5arcsec)
                            // let dp -17.2*sin(Om)-1.32*sin(2*L)-0.23*sin(2*l1)+0.21*sin(2*Om)
                            dL = -17.2 * Math.sin(OMEGA) - 1.32 * Math.sin(2d * L) - 0.23 * Math.sin(2d * L1) + 0.21 * Math.sin(2d * OMEGA); // arcsec

                            // shift in angle between the ecliptic and equator (good to about 0.1 arcsec)
                            // let de 9.2*cos(Om)+0.57*cos(2*L)+0.1*cos(2*l1)-0.09*cos(2*Om)
                            dE = 9.2 * Math.cos(OMEGA) + 0.57 * Math.cos(2d * L) + 0.1 * Math.cos(2d * L1) - 0.09 * Math.cos(2d * OMEGA); // arcsec

                            // Convert arcsec to degrees:
                            dL /= 3600d;
                            dE /= 3600d;

                            // difference between Mean and Apparent Sidereal Times
                            // let dT dp*cos((de+eps)*PI/180)/3600 seems wrong as de is expressed in arcsec not in degrees !
                            dT = dL * Math.cos(Math.toRadians(dE + EPS)); // deg

                            // Greenwich Apparent sidereal time
                            gast = gmst + dT;

                            // Local Apparent Sidereal Time
                            last = gast + arrayLongitude;

                            if (isLogDebug) {
                                logger.log(Level.FINE, "gmst = {0} (deg)", gmst);
                                logger.log(Level.FINE, "dT   = {0} (deg)", dT);
                                logger.log(Level.FINE, "gast = {0} (deg)", gast);
                                logger.log(Level.FINE, "last = {0} (deg)", last);
                            }

                            // Get RA (target):
                            rowTarget = oiTarget.getRowIndex(Short.valueOf(targetId[i])); // requires previously OIFits Analyzer call()

                            if (rowTarget != null) {
                                targetRA = ra[rowTarget.intValue()]; // deg

                                /*
                                 * Note: target's coordinates are not precessed up to mjd (as Aspro 2 does)
                                 */
                                if (isLogDebug) {
                                    logger.log(Level.FINE, "ra = {0} (deg)", targetRA);
                                }

                                // let oi%ha (last-oi_data%oi_target%COL%RAEP0)|15.0D0
                                ha = (last - targetRA) / 15d;

                                if (isLogDebug) {
                                    logger.log(Level.FINE, "ha = {0} (deg)", ha);
                                }

                                // ensure HA in within [-12;12]
                                while (ha < -12d) {
                                    ha += 24d;
                                }
                                while (ha > 12d) {
                                    ha -= 24d;
                                }

                                if (isLogDebug) {
                                    logger.log(Level.FINE, "ha (fixed) = {0} (deg)", ha);
                                }

                                hourAngle[i] = ha;
                            }
                        }
                    }
                }
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_HOUR_ANGLE, hourAngle);
        }

        return hourAngle;
    }

    /**
     * Return the night identifier column.
     *
     * @return the computed night identifier
     */
    public double[] getNightId() {
        // lazy:
        double[] nightId = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_NIGHT_ID);

        if (nightId == null) {
            final int nRows = getNbRows();
            nightId = new double[nRows];

            double[] mjds = getMJD();
            for (int i = 0; i < nRows; i++) {
                // TODO: use array center coordinates, adjust night
                // TODO: if no MJD, use DATE-OBS + TIME[i] instead
                nightId[i] = (double) Math.round(mjds[i]);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_NIGHT_ID, nightId);
        }

        return nightId;
    }

    /* --- Utility methods for cross-referencing --- */
    /**
     * Return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s).
     * @return the number of distinct spectral channels (NWAVE) of the associated OI_WAVELENGTH table(s)
     * or 0 if the OI_WAVELENGTH table(s) are missing !
     * Note: this method is used by WaveColumnMeta.getRepeat() to determine the column dimensions
     */
    @Override
    public final int getNWave() {
        final OIWavelength oiWavelength = getOiWavelength();
        if (oiWavelength != null) {
            return oiWavelength.getNWave();
        }
        return 0;
    }

    /**
     * Return the associated OIWavelength table.
     * @return the associated OIWavelength
     */
    public final OIWavelength getOiWavelength() {
        /** cached resolved reference */
        if (this.oiWavelengthRef != null) {
            return this.oiWavelengthRef;
        }

        final String insName = getInsName();
        if (insName != null) {
            final OIWavelength oiWavelength = getOIFitsFile().getOiWavelength(insName);

            if (oiWavelength != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Resolved OI_WAVELENGTH reference [{0} | NWAVE={1} ] to {2}", new Object[]{oiWavelength.getExtNb(), oiWavelength.getNWave(), super.toString()});
                }
                this.oiWavelengthRef = oiWavelength;
            } else if (!getOIFitsFile().hasMissingTableName(insName) && logger.isLoggable(Level.WARNING)) {
                getOIFitsFile().addMissingTableName(insName);
                logger.log(Level.WARNING, "Missing OI_WAVELENGTH table identified by INSNAME=''{0}''", insName);
            }
            return oiWavelength;
        }

        return null;
    }

    /**
     * Return the associated optional OICorr table.
     * @return the associated OICorr or null if the keyword CORRNAME is undefined
     */
    public final OICorr getOiCorr() {
        /** cached resolved reference */
        if (this.oiCorrRef != null) {
            return this.oiCorrRef;
        }

        final String corrName = getCorrName();
        if (corrName != null) {
            final OICorr oiCorr = getOIFitsFile().getOiCorr(corrName);

            if (oiCorr != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Resolved OI_Corr reference [{0}] to {1}", new Object[]{oiCorr.getExtNb(), super.toString()});
                }
                this.oiCorrRef = oiCorr;
            } else if (!getOIFitsFile().hasMissingTableName(corrName) && logger.isLoggable(Level.WARNING)) {
                getOIFitsFile().addMissingTableName(corrName);
                logger.log(Level.WARNING, "Missing OI_Corr table identified by CORRNAME=''{0}''", corrName);
            }
            return oiCorr;
        }

        return null;
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(super.toString());
        sb.append(" [ INSNAME=").append(getInsName());
        sb.append(" NB_MEASUREMENTS=").append(getNbMeasurements());

        if (nFlagged > 0) {
            sb.append(" (").append(nFlagged).append(" data flagged out - ");
            sb.append(getNbRows() * getNWave() - nFlagged).append(" data ok )");
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Check arrname / oiarray and MJD range in addition to OITable.checkKeywords()
     * @param checker checker component
     */
    @Override
    public final void checkKeywords(final OIFitsChecker checker) {
        super.checkKeywords(checker);

        FitsHDU.checkDateObsKeyword(checker, OIFitsConstants.KEYWORD_DATE_OBS, this);
        checkMJDColumn(checker, OIFitsConstants.COLUMN_MJD);
    }

    /**
     * Checker to validate rule on StaIndexes (OIDatas)
     * @param checker checker component
     * @param staIndexes station number
     * @param oiData oiData Tables
     */
    protected static void checkStaIndexes(final OIFitsChecker checker, final short[][] staIndexes, final OIData oiData) {
        // check STA_INDEX Unique
        for (int i = 0; i < staIndexes.length; i++) {
            final short[] indexes = staIndexes[i];

            for (int k = 0; k < indexes.length; k++) {
                final short refId = indexes[k];

                for (int j = k + 1; j < indexes.length; j++) {
                    // rule [GENERIC_STA_INDEX_UNIQ] check duplicated indexes inside each STA_INDEX column values (data table)
                    if (refId == indexes[j] || OIFitsChecker.isInspectRules()) {
                        checker.ruleFailed(Rule.GENERIC_STA_INDEX_UNIQ, oiData, OIFitsConstants.COLUMN_STA_INDEX).addValueAtCols(refId, i, k, j);
                    }
                }
            }

        }
    }

    /**
     * Checker to validate rules on CorrIndexes (OICorr / OIData relation)
     * @param checker checker component
     * @param oiCorr oiCorr table
     * @param oidata oiData table
     * @param colName column name
     * @param corrindx corrindex of the column of the oiDatas table
     */
    protected static void checkCorrIndex(final OIFitsChecker checker, final OICorr oiCorr,
                                         final OIData oidata, final String colName, final int[] corrindx) {

        // get OIFitsCorrChecker
        final OIFitsCorrChecker corrChecker = checker.getCorrChecker(oiCorr.getCorrName());

        final int nWaves = oidata.getNWave();

        // ndata gives the square matrix dimension N [N x N]
        final int ndata = oiCorr.getNData();

        for (int row = 0; row < corrindx.length; row++) {
            final int idxI = corrindx[row];

            // rule [GENERIC_CORRINDX_MIN] check if the CORRINDX values >= 1
            if (idxI < 1 || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.GENERIC_CORRINDX_MIN, oidata, colName).addValueAt(idxI, row);
            }
            // rule [GENERIC_CORRINDX_MAX] check if the CORRINDX values <= NDATA
            if (idxI > ndata || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.GENERIC_CORRINDX_MAX, oidata, colName).addValuesAt(idxI, ndata, row);
            }

            for (int l = 0; l < nWaves; l++) {
                final Integer index = NumberUtils.valueOf(idxI + l);

                // rule [GENERIC_CORRINDX_UNIQ] check duplicates or overlaps within correlation indexes (CORRINDX)
                if (corrChecker.contains(index) || OIFitsChecker.isInspectRules()) {
                    checker.ruleFailed(Rule.GENERIC_CORRINDX_UNIQ, oidata, colName).addColValueAt(index, row, l, ((OIFitsChecker.isInspectRules()) ? "[[ORIGIN]]" : corrChecker.getOriginAsString(index)));
                } else {
                    corrChecker.put(index, oidata.getExtName(), oidata.getExtNb(), colName, row, l);
                }
            }
        }

    }


    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the derived column data as double array (1D) for the given column name
     * To be override in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong type
     */
    @Override
    protected double[] getDerivedColumnAsDouble(final String name) {
        if (OIFitsConstants.COLUMN_RADIUS.equals(name)) {
            return getRadius();
        }
        if (OIFitsConstants.COLUMN_POS_ANGLE.equals(name)) {
            return getPosAngle();
        }
        if (OIFitsConstants.COLUMN_HOUR_ANGLE.equals(name)) {
            return getHourAngle();
        }
        if (OIFitsConstants.COLUMN_NIGHT_ID.equals(name)) {
            return getNightId();
        }
        return null;
    }

    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        if (OIFitsConstants.COLUMN_EFF_WAVE.equals(name)) {
            return getEffWaveAsDoubles();
        }
        if (OIFitsConstants.COLUMN_SPATIAL_FREQ.equals(name)) {
            return getSpatialFreq();
        }
        // handle user expressions
        for (ColumnMeta column : getColumnDerivedDescCollection()) {
            if (column.getName().equals(name)) {

                if (column instanceof WaveColumnMeta) {
                    WaveColumnMeta colMeta = (WaveColumnMeta) column;

                    if (colMeta.getExpression() != null) {
                        return getExprColumnDoubles(name, colMeta.getExpression());
                    }
                }
            }
        }
        return null;
    }

    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        distinctStaConf.clear();
        nFlagged = -1;
    }

    /**
     * Return the wavelenth range (min - max)
     * @return float[]{min, max}
     */
    public final float[] getEffWaveRange() {
        final OIWavelength oiWavelength = getOiWavelength();
        if (oiWavelength != null) {
            return oiWavelength.getEffWaveRange();
        }
        return null;
    }

    /**
     * Get the distinct StaConf values present in this table (station configuration)
     * @return distinctStaConf
     */
    public Set<short[]> getDistinctStaConf() {
        return distinctStaConf;
    }

    /**
     * Get the size of distinct StaConf values present in this table
     * @return distinctStaConf size
     */
    public int getDistinctStaConfCount() {
        return distinctStaConf.size();
    }

    /**
     * Get number of data flagged out
     * @return nFlagged
     */
    public int getNFlagged() {
        return nFlagged;
    }

    /**
     * Set number of data flagged out
     * @param nFlagged number of data flagged out
     */
    protected void setNFlagged(final int nFlagged) {
        this.nFlagged = nFlagged;
    }

    /**
     * Return true if the given error value is valid ie. NaN or is positive or equals to 0
     * @param checker checker component
     * @param flags the FLAG column
     * @param err error value
     * @param oidata oiData table
     * @param colName column name
     */
    public static void checkColumnError(final OIFitsChecker checker, final boolean[][] flags, final double[][] err,
                                        final OIData oidata, final String colName) {

        boolean[] rowFlag;
        double[] rowErr;

        for (int i = 0, j; i < err.length; i++) {
            rowFlag = flags[i];
            rowErr = err[i];

            for (j = 0; j < rowErr.length; j++) {
                if (!rowFlag[j] || OIFitsChecker.isInspectRules()) {
                    // Not flagged:
                    if (!isErrorValid(rowErr[j]) || OIFitsChecker.isInspectRules()) {
                        // rule [GENERIC_COL_ERR] check if the UNFLAGGED *ERR column values are valid (positive or NULL)
                        checker.ruleFailed(Rule.GENERIC_COL_ERR, oidata, colName).addColValueAt(rowErr[j], i, j);
                    }
                }
            }
        }
    }

    /**
     * Return true if the given error value is valid ie. NaN or is positive or equals to 0
     * @param err error value
     * @return true if the given error value is valid
     */
    public static boolean isErrorValid(final double err) {
        return NumberUtils.isFinitePositive(err) || Double.isNaN(err);
    }

    /**
     * Set the unit value of column (colName) if this column unit is alterable.
     * @param colName cloumn name
     * @param unit new unit
     */
    public void setColumnUnit(final String colName, final String unit) {
        final ColumnMeta colMeta = getColumnMeta(colName);
        if (colMeta == null) {
            logger.log(Level.SEVERE, "Can not modify a non existing column in this table or this file: ", colName);
        } else {
            if (colMeta.isCustomUnits()) {
                colMeta.getCustomUnits().setRepresentation(unit);
            } else {
                logger.log(Level.WARNING, "Can not modify {0} is not a CustomUnits column: ", colName);
            }
        }
    }
}
/*___oOo___*/
