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

/**
 * Class for OI_WAVELENGTH table.
 */
public final class OIWavelength extends OITable {

    /* constants */
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
    public Float getResolution() {
        /* retrieve value in columnsRangeValue map of associated column */
        Float value = (Float) getColumnsRangeValue().get(OIFitsConstants.VALUE_RESOLUTION);

        /* compute resolution value if not previously set */
        if (value == null) {
            final int nWaves = getNWave();

            final float[] effWaves = getEffWave();
            final float[] effBands = getEffBand();

            int n = 0;
            double total = 0d;

            for (int i = 0; i < nWaves; i++) {
                double res_ch = effWaves[i] / effBands[i];
                if (NumberUtils.isFinite(res_ch)) {
                    total += res_ch;
                    n++;
                }
            }

            value = (n != 0) ? Float.valueOf((float) (total / n)) : Float.NaN;

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
     * Return the wavelength range
     * @return the wavelength range
     */
    public float[] getEffWaveRange() {
        return (float[]) getMinMaxColumnValue(OIFitsConstants.COLUMN_EFF_WAVE);
    }

    /**
     * Return the minimum wavelength
     * @return the minimum wavelength
     */
    public float getEffWaveMin() {
        return getEffWaveRange()[0];
    }

    /**
     * Return the maximum wavelength
     * @return the maximum wavelength
     */
    public float getEffWaveMax() {
        return getEffWaveRange()[1];
    }

    /**
     * Return the bandwidth range
     * @return the bandwidth range
     */
    public float[] getEffBandRange() {
        return (float[]) getMinMaxColumnValue(OIFitsConstants.COLUMN_EFF_BAND);
    }

    /**
     * Return the minimum bandwidth
     * @return the minimum bandwidth
     */
    public float getEffBandMin() {
        return getEffWaveRange()[0];
    }

    /**
     * Return the maximum bandwidth
     * @return the maximum bandwidth
     */
    public float getEffBandMax() {
        return getEffWaveRange()[1];
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

        if ((getInsName() != null && getInsName().length() == 0) || OIFitsChecker.isInspectRules()) {
            /* Problem: INSNAME keyword has value "", that should not be
             * possible. */
            // rule [OI_WAVELENGTH_INSNAME] check the INSNAME keyword has a not null or empty value
            checker.ruleFailed(Rule.OI_WAVELENGTH_INSNAME, this, OIFitsConstants.KEYWORD_INSNAME);
        }

        for (int i = 0; i < getEffWave().length; i++) {
            if ((getEffWave()[i] < 1.00E-7f || getEffWave()[i] > 20.00E-6f) || OIFitsChecker.isInspectRules()) {
                // rule [OI_WAVELENGTH_EFFWAVE] check if the EFF_WAVE column values are within range [0.1x10^-6 - 20x10^-6]
                checker.ruleFailed(Rule.OI_WAVELENGTH_EFF_WAVE, this, OIFitsConstants.COLUMN_EFF_WAVE).addValueAt(getEffWave()[i], i);
            }
        }
        // TODO: check effband <0 ?

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
