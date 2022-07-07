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
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.model.range.Range;
import java.util.logging.Level;

/**
 * Class for OI_WAVELENGTH table.
 */
public final class OIWavelength extends OITable {

    /* static descriptors */
    /** INSNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_INSNAME = new KeywordMeta(OIFitsConstants.KEYWORD_INSNAME,
            "name of detector for cross-referencing", Types.TYPE_CHAR);
    /** EFF_WAVE column descriptor */
    private final static ColumnMeta COLUMN_EFF_WAVE = new ColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE,
            "effective wavelength of channel", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** EFF_BAND column descriptor */
    private final static ColumnMeta COLUMN_EFF_BAND = new ColumnMeta(OIFitsConstants.COLUMN_EFF_BAND,
            "effective bandpass of channel", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** EFF_WAVE derived column descriptor (double) */
    private final static ColumnMeta COLUMN_EFF_WAVE_DBL = new ColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE,
            "effective wavelength of channel (double)", Types.TYPE_DBL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** EFF_BAND derived column descriptor (double) */
    private final static ColumnMeta COLUMN_EFF_BAND_DBL = new ColumnMeta(OIFitsConstants.COLUMN_EFF_BAND,
            "effective bandpass of channel (double)", Types.TYPE_DBL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /* members */
 /* cached analyzed data */
    private InstrumentMode insMode = null;

    /** 
     * Public OIWavelength class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIWavelength(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // INSNAME  keyword definition
        addKeywordMeta(KEYWORD_INSNAME);

        // EFF_WAVE  column definition
        addColumnMeta(COLUMN_EFF_WAVE);

        // EFF_BAND  column definition
        addColumnMeta(COLUMN_EFF_BAND);

        // Derived EFF_WAVE column definition
        addDerivedColumnMeta(COLUMN_EFF_WAVE_DBL);
        // Derived EFF_BAND column definition
        addDerivedColumnMeta(COLUMN_EFF_BAND_DBL);
    }

    /**
     * Public OIWavelength class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIWavelength(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /**
     * Public OIWavelength class constructor to copy the given table (structure only)
     * @param oifitsFile main OifitsFile
     * @param src table to copy
     */
    public OIWavelength(final OIFitsFile oifitsFile, final OIWavelength src) {
        this(oifitsFile);

        this.copyTable(src);
    }

    /**
     * Get number of wavelengths
     * @return the number of wavelengths.
     */
    public int getNWave() {
        return getNbRows();
    }

    /* --- Keywords --- */
    /**
     * Get the INSNAME keyword value.
     * @return the value of INSNAME keyword
     */
    public String getInsName() {
        return getKeyword(OIFitsConstants.KEYWORD_INSNAME);
    }

    /**
     * Define the INSNAME keyword value
     * @param insName value of INSNAME keyword
     */
    public void setInsName(final String insName) {
        setKeyword(OIFitsConstants.KEYWORD_INSNAME, insName);
    }

    /* --- Columns --- */
    /**
     * Return the effective wavelength of channel
     * @return the wavelength of channel array
     */
    public float[] getEffWave() {
        return this.getColumnFloat(OIFitsConstants.COLUMN_EFF_WAVE);
    }

    /**
     * Return the effective bandpass of channel
     * @return the bandpass of channel array
     */
    public float[] getEffBand() {
        return this.getColumnFloat(OIFitsConstants.COLUMN_EFF_BAND);
    }

    /* --- Derived Column --- */
    /**
     * Return the resolution (mean) from all couples lambda / delta_lambda ie (wavelength / bandpass)
     * @return resolution
     */
    public Double getResolution() {
        /* retrieve value in columnsRangeValue map of associated column */
        Double value = (Double) getColumnsRangeValue().get(OIFitsConstants.VALUE_RESOLUTION);

        /* compute resolution value if not previously set */
        if (value == null) {
            final int nWaves = getNWave();

            final double[] effWaves = getEffWaveAsDouble();
            final float[] effBands = getEffBand();

            int n = 0;
            double total = 0d;

            for (int i = 0; i < nWaves; i++) {
                double res_ch = effWaves[i] / effBands[i];

                if (!NumberUtils.isFinite(res_ch)) {
                    // TODO: use half distance between channels !
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Infinite @ {0}: {1} {2}", new Object[]{i, effWaves[i], effBands[i]});
                    }
                } else if (res_ch > 0.0) {
                    total += res_ch;
                    n++;
                }
            }
            value = (n != 0) ? Double.valueOf(total / n) : UNDEFINED_DBL;

            /* store value in associated column range value */
            getColumnsRangeValue().put(OIFitsConstants.VALUE_RESOLUTION, value);
        }
        return value;
    }

    /**
     * Return the effective wavelength of channel as double array
     * @return the wavelength of channel array as double array
     */
    public double[] getEffWaveAsDouble() {
        // lazy:
        double[] effWaveDbls = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_EFF_WAVE);

        if (effWaveDbls == null) {
            final int nWaves = getNWave();
            effWaveDbls = new double[nWaves];

            final float[] effWaves = getEffWave();

            for (int j = 0; j < nWaves; j++) {
                effWaveDbls[j] = effWaves[j];
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_EFF_WAVE, effWaveDbls);
        }
        return effWaveDbls;
    }

    /**
     * Return the effective bandpass of channel as double array
     * @return the effective bandpass of channel array as double array
     */
    public double[] getEffBandAsDouble() {
        // lazy:
        double[] effBandDbls = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_EFF_BAND);

        if (effBandDbls == null) {
            final int nWaves = getNWave();
            effBandDbls = new double[nWaves];

            final float[] effBands = getEffBand();

            for (int j = 0; j < nWaves; j++) {
                effBandDbls[j] = effBands[j];
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_EFF_BAND, effBandDbls);
        }
        return effBandDbls;
    }

    /**
     * Return the wavelength range
     * @return the wavelength range
     */
    public Range getEffWaveRange() {
        return getColumnRange(OIFitsConstants.COLUMN_EFF_WAVE);
    }

    /**
     * Return the bandwidth range
     * @return the bandwidth range
     */
    public Range getEffBandRange() {
        return getColumnRange(OIFitsConstants.COLUMN_EFF_BAND);
    }

    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the derived column data as double array (1D) for the given column
     * name To be overriden in child classes for lazy computed columns
     *
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong
     * type
     */
    protected double[] getDerivedColumnAsDouble(final String name) {
        if (OIFitsConstants.COLUMN_EFF_WAVE.equals(name)) {
            return getEffWaveAsDouble();
        }
        if (OIFitsConstants.COLUMN_EFF_BAND.equals(name)) {
            return getEffBandAsDouble();
        }
        return super.getDerivedColumnAsDouble(name);
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ INSNAME=" + getInsName() + " | NWAVE=" + getNWave() + " ]";
    }

    /** 
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        if (((getInsName() != null) && (getInsName().length() == 0)) || OIFitsChecker.isInspectRules()) {
            /* Problem: INSNAME keyword has value "", that should not be
             * possible. */
            // rule [OI_WAVELENGTH_INSNAME] check the INSNAME keyword has a not null or empty value
            checker.ruleFailed(Rule.OI_WAVELENGTH_INSNAME, this, OIFitsConstants.KEYWORD_INSNAME);
        }

        final int nWaves = getNWave();
        final float[] effWaves = getEffWave();

        for (int i = 0; i < nWaves; i++) {
            final float effWave = effWaves[i];
            if (((effWave < 0.1E-6f) || (effWave > 20.0E-6f)) || OIFitsChecker.isInspectRules()) {
                // rule [OI_WAVELENGTH_EFFWAVE] check if the EFF_WAVE column values are within range [0.1x10^-6 - 20x10^-6]
                checker.ruleFailed(Rule.OI_WAVELENGTH_EFF_WAVE, this, OIFitsConstants.COLUMN_EFF_WAVE).addValueAt(effWave, i);
            }
        }

        getOIFitsFile().checkCrossReference(this, checker);
    }


    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        setInstrumentMode(null);
    }

    /**
     * Get the associated InstrumentMode
     * @return associated InstrumentMode (or null if Analyzer not run)
     */
    public InstrumentMode getInstrumentMode() {
        return insMode;
    }

    void setInstrumentMode(final InstrumentMode insMode) {
        this.insMode = insMode;
    }
}
