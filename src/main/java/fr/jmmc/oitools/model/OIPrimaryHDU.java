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
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageHDUFactory;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;

/**
 * This class represents the Primary HDU of an OIFITS V2 file
 * @author kempsc
 */
public final class OIPrimaryHDU extends FitsImageHDU {

    /**
     * Create an object FitsImageHDU
     */
    public final static FitsImageHDUFactory DEFAULT_FACTORY = new FitsImageHDUFactory() {
        @Override
        public FitsImageHDU create() {
            return new OIPrimaryHDU();
        }
    };

    /* constants */
    /**
     * ORIGIN keyword descriptor
     */
    private final static KeywordMeta KEYWORD_ORIGIN = new KeywordMeta(FitsConstants.KEYWORD_ORIGIN,
            "Institution responsible for file creation", Types.TYPE_CHAR);
    /**
     * DATE keyword descriptor
     */
    private final static KeywordMeta KEYWORD_DATE = new KeywordMeta(FitsConstants.KEYWORD_DATE,
            "Date the HDU was written", Types.TYPE_CHAR);
    /**
     * DATE_OBS keyword descriptor
     */
    private final static KeywordMeta KEYWORD_DATE_OBS = new KeywordMeta(FitsConstants.KEYWORD_DATE_OBS,
            "Start date of observation", Types.TYPE_CHAR);
    /**
     * CONTENT keyword descriptor
     */
    private final static KeywordMeta KEYWORD_CONTENT = new KeywordMeta(FitsConstants.KEYWORD_CONTENT,
            "Must contain only the string “OIFITS2”", Types.TYPE_CHAR);
    /**
     * AUTHOR keyword descriptor
     */
    private final static KeywordMeta KEYWORD_AUTHOR = new KeywordMeta(FitsConstants.KEYWORD_AUTHOR,
            "As defined in FITS norm", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * DATASUM keyword descriptor
     */
    private final static KeywordMeta KEYWORD_DATASUM = new KeywordMeta(FitsConstants.KEYWORD_DATASUM,
            "HDU datasum", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * CHECKSUM keyword descriptor
     */
    private final static KeywordMeta KEYWORD_CHECKSUM = new KeywordMeta(FitsConstants.KEYWORD_CHECKSUM,
            "HDU checksum", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * TELESCOP keyword descriptor
     */
    private final static KeywordMeta KEYWORD_TELESCOP = new KeywordMeta(FitsConstants.KEYWORD_TELESCOP,
            "A generic identification of the ARRAY", Types.TYPE_CHAR);
    /**
     * INSTRUME keyword descriptor
     */
    private final static KeywordMeta KEYWORD_INSTRUME = new KeywordMeta(FitsConstants.KEYWORD_INSTRUME,
            "A generic identification of the instrument", Types.TYPE_CHAR);
    /**
     * OBSERVER keyword descriptor
     */
    private final static KeywordMeta KEYWORD_OBSERVER = new KeywordMeta(FitsConstants.KEYWORD_OBSERVER,
            "Who acquired the data", Types.TYPE_CHAR);
    /**
     * INSMODE keyword descriptor
     */
    private final static KeywordMeta KEYWORD_INSMODE = new KeywordMeta(FitsConstants.KEYWORD_INSMODE,
            "Instrument mode", Types.TYPE_CHAR);
    /**
     * OBJECT keyword descriptor
     */
    private final static KeywordMeta KEYWORD_OBJECT = new KeywordMeta(FitsConstants.KEYWORD_OBJECT,
            "Object Identifier", Types.TYPE_CHAR);
    /**
     * REFERENC keyword descriptor
     */
    private final static KeywordMeta KEYWORD_REFERENC = new KeywordMeta(FitsConstants.KEYWORD_REFERENC,
            "Bibliographic reference", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * PROG_ID keyword descriptor
     */
    private final static KeywordMeta KEYWORD_PROG_ID = new KeywordMeta(OIFitsConstants.KEYWORD_PROG_ID,
            "Program ID", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * PROCSOFT keyword descriptor
     */
    private final static KeywordMeta KEYWORD_PROCSOFT = new KeywordMeta(OIFitsConstants.KEYWORD_PROCSOFT,
            "Versioned Data Reduction Software", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * OBSTECH keyword descriptor
     */
    private final static KeywordMeta KEYWORD_OBSTECH = new KeywordMeta(OIFitsConstants.KEYWORD_OBSTECH,
            "Technique of observation", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * RA keyword descriptor
     */
    private final static KeywordMeta KEYWORD_RA = new KeywordMeta(OIFitsConstants.KEYWORD_RA,
            "Target Right Ascension at mean EQUINOX (deg)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * DEC keyword descriptor
     */
    private final static KeywordMeta KEYWORD_DEC = new KeywordMeta(OIFitsConstants.KEYWORD_DEC,
            "Target Declination at mean EQUINOX (deg)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * EQUINOX keyword descriptor
     */
    private final static KeywordMeta KEYWORD_EQUINOX = new KeywordMeta(OIFitsConstants.KEYWORD_EQUINOX,
            "Standard FK5 (years)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * RADECSYS keyword descriptor
     */
    private final static KeywordMeta KEYWORD_RADECSYS = new KeywordMeta(OIFitsConstants.KEYWORD_RADECSYS,
            "Coordinate reference frame", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * SPECSYS keyword descriptor
     */
    private final static KeywordMeta KEYWORD_SPECSYS = new KeywordMeta(OIFitsConstants.KEYWORD_SPECSYS,
            "Reference frame for spectral coord.", Types.TYPE_CHAR, true, NO_STR_VALUES);
    /**
     * TEXPTIME keyword descriptor
     */
    private final static KeywordMeta KEYWORD_TEXPTIME = new KeywordMeta(OIFitsConstants.KEYWORD_TEXPTIME,
            "Maximum elapsed time for data point (s)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * MJD_OBS keyword descriptor
     */
    private final static KeywordMeta KEYWORD_MJD_OBS = new KeywordMeta(OIFitsConstants.KEYWORD_MJD_OBS,
            "Start of observations (MJD)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * MJD_END keyword descriptor
     */
    private final static KeywordMeta KEYWORD_MJD_END = new KeywordMeta(OIFitsConstants.KEYWORD_MJD_END,
            "End of observations (MJD)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * BASE_MIN keyword descriptor
     */
    private final static KeywordMeta KEYWORD_BASE_MIN = new KeywordMeta(OIFitsConstants.KEYWORD_BASE_MIN,
            "Minimum projected baseline length (m)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * BASE_MAX keyword descriptor
     */
    private final static KeywordMeta KEYWORD_BASE_MAX = new KeywordMeta(OIFitsConstants.KEYWORD_BASE_MAX,
            "Maximum projected baseline length (m)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * WAVELMIN keyword descriptor
     */
    private final static KeywordMeta KEYWORD_WAVELMIN = new KeywordMeta(OIFitsConstants.KEYWORD_WAVELMIN,
            "Minimum wavelength (nm)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * WAVELMAX keyword descriptor
     */
    private final static KeywordMeta KEYWORD_WAVELMAX = new KeywordMeta(OIFitsConstants.KEYWORD_WAVELMAX,
            "Maximum wavelength (nm)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * NUM_CHAN keyword descriptor
     */
    private final static KeywordMeta KEYWORD_NUM_CHAN = new KeywordMeta(OIFitsConstants.KEYWORD_NUM_CHAN,
            "Total number of spectral channels", Types.TYPE_INT, true, NO_STR_VALUES);
    /**
     * SPEC_RES keyword descriptor
     */
    private final static KeywordMeta KEYWORD_SPEC_RES = new KeywordMeta(OIFitsConstants.KEYWORD_SPEC_RES,
            "Reference spectral resolution (λ/Δ)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * VIS2ERR keyword descriptor
     */
    private final static KeywordMeta KEYWORD_VIS2ERR = new KeywordMeta(OIFitsConstants.KEYWORD_VIS2ERR,
            "Repesentative V² error (%)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * VISPHERR keyword descriptor
     */
    private final static KeywordMeta KEYWORD_VISPHERR = new KeywordMeta(OIFitsConstants.KEYWORD_VISPHERR,
            "Repesentative Diff. Vis. Phase error (deg)", Types.TYPE_DBL, true, NO_STR_VALUES);
    /**
     * T3PHIERR keyword descriptor
     */
    private final static KeywordMeta KEYWORD_T3PHIERR = new KeywordMeta(OIFitsConstants.KEYWORD_T3PHIERR,
            "Representative Closure Phase error (deg)", Types.TYPE_DBL, true, NO_STR_VALUES);

    /**
     * Protected class constructor
     */
    public OIPrimaryHDU() {
        super();
        // since every class constructor of OI table calls super
        // constructor, next keywords will be common to every subclass :

        // ORIGIN keyword definition
        addKeywordMeta(KEYWORD_ORIGIN);

        // DATE keyword definition
        addKeywordMeta(KEYWORD_DATE);

        // DATE_OBS keyword definition
        addKeywordMeta(KEYWORD_DATE_OBS);

        // DATE_OBS keyword definition
        addKeywordMeta(KEYWORD_CONTENT);

        // AUTHOR keyword definition
        addKeywordMeta(KEYWORD_AUTHOR);

        // DATASUM keyword definition
        addKeywordMeta(KEYWORD_DATASUM);

        // CHECKSUM keyword definition
        addKeywordMeta(KEYWORD_CHECKSUM);

        // TELESCOP keyword definition
        addKeywordMeta(KEYWORD_TELESCOP);

        // INSTRUME keyword definition
        addKeywordMeta(KEYWORD_INSTRUME);

        // OBSERVER keyword definition
        addKeywordMeta(KEYWORD_OBSERVER);

        // INSMODE keyword definition
        addKeywordMeta(KEYWORD_INSMODE);

        // OBJECT keyword definition
        addKeywordMeta(KEYWORD_OBJECT);

        // REFERENC keyword definition
        addKeywordMeta(KEYWORD_REFERENC);

        // PROG_ID keyword definition
        addKeywordMeta(KEYWORD_PROG_ID);

        // PROCSOFT keyword definition
        addKeywordMeta(KEYWORD_PROCSOFT);

        // OBSTECH keyword definition
        addKeywordMeta(KEYWORD_OBSTECH);

        // RA keyword definition
        addKeywordMeta(KEYWORD_RA);

        // DEC keyword definition
        addKeywordMeta(KEYWORD_DEC);

        // EQUINOX keyword definition
        addKeywordMeta(KEYWORD_EQUINOX);

        // RADECSYS keyword definition
        addKeywordMeta(KEYWORD_RADECSYS);

        // SPECSYS keyword definition
        addKeywordMeta(KEYWORD_SPECSYS);

        // TEXPTIME keyword definition
        addKeywordMeta(KEYWORD_TEXPTIME);

        // MJD_OBS keyword definition
        addKeywordMeta(KEYWORD_MJD_OBS);

        // MJD_END keyword definition
        addKeywordMeta(KEYWORD_MJD_END);

        // BASE_MIN keyword definition
        addKeywordMeta(KEYWORD_BASE_MIN);

        // BASE_MAX keyword definition
        addKeywordMeta(KEYWORD_BASE_MAX);

        // WAVELMIN keyword definition
        addKeywordMeta(KEYWORD_WAVELMIN);

        // WAVELMAX keyword definition
        addKeywordMeta(KEYWORD_WAVELMAX);

        // NUM_CHAN keyword definition
        addKeywordMeta(KEYWORD_NUM_CHAN);

        // SPEC_RES keyword definition
        addKeywordMeta(KEYWORD_SPEC_RES);

        // VIS2ERR keyword definition
        addKeywordMeta(KEYWORD_VIS2ERR);

        // VISPHERR keyword definition
        addKeywordMeta(KEYWORD_VISPHERR);

        // T3PHIERR keyword definition
        addKeywordMeta(KEYWORD_T3PHIERR);
    }

    /* --- keywords --- */
    /**
     * Get the value of ORIGIN keyword
     * @return the value of ORIGIN keyword
     */
    public String getOrigin() {
        return getKeyword(FitsConstants.KEYWORD_ORIGIN);
    }

    /**
     * Define the value of ORIGIN keyword
     * @param origin value of ORIGIN keyword
     */
    public void setOrigin(String origin) {
        setKeyword(FitsConstants.KEYWORD_ORIGIN, origin);
    }

    /**
     * Get the value of DATE keyword
     * @return the value of DATE keyword
     */
    public String getDate() {
        return getKeyword(FitsConstants.KEYWORD_DATE);
    }

    /**
     * Define the value of DATE keyword
     * @param date value of DATE keyword
     */
    public void setDate(String date) {
        setKeyword(FitsConstants.KEYWORD_DATE, date);
    }

    /**
     * Get the value of DATE-OBS keyword
     * @return the value of DATE-OBS keyword
     */
    public String getDateObs() {
        return getKeyword(FitsConstants.KEYWORD_DATE_OBS);
    }

    /**
     * Define the value of DATE-OBS keyword
     * @param dateObs value of DATE-OBS keyword
     */
    public void setDateObs(String dateObs) {
        setKeyword(FitsConstants.KEYWORD_DATE_OBS, dateObs);
    }

    /**
     * Get the value of CONTENT keyword
     * @return the value of CONTENT keyword
     */
    public String getContent() {
        return getKeyword(FitsConstants.KEYWORD_CONTENT);
    }

    /**
     * Define the value of DATE_OBS keyword
     * @param content value of DATE_OBS keyword
     */
    public void setContent(String content) {
        setKeyword(FitsConstants.KEYWORD_CONTENT, content);
    }

    /**
     * Get the value of AUTHOR keyword
     * @return the value of AUTHOR keyword
     */
    public String getAuthor() {
        return getKeyword(FitsConstants.KEYWORD_AUTHOR);
    }

    /**
     * Define the value of AUTHOR keyword
     * @param author value of AUTHOR keyword
     */
    public void setAuthor(String author) {
        setKeyword(FitsConstants.KEYWORD_AUTHOR, author);
    }

    /**
     * Get the value of DATASUM keyword
     * @return the value of DATASUM keyword
     */
    public String getDatasum() {
        return getKeyword(FitsConstants.KEYWORD_DATASUM);
    }

    /**
     * Define the value of DATASUM keyword
     * @param datasum value of DATASUM keyword
     */
    public void setDatasum(String datasum) {
        setKeyword(FitsConstants.KEYWORD_DATASUM, datasum);
    }

    /**
    
    public String getChecksum() {
        return getKeyword(FitsConstants.KEYWORD_CHECKSUM);
    }

    public void setChecksum(String checksum) {
        setKeyword(FitsConstants.KEYWORD_CHECKSUM, checksum);
    }
     */
    /**
     * Get the value of TELESCOP keyword
     * @return the value of TELESCOP keyword
     */
    public String getTelescop() {
        return getKeyword(FitsConstants.KEYWORD_TELESCOP);
    }

    /**
     * Define the value of TELESCOP keyword
     * @param telescop value of TELESCOP keyword
     */
    public void setTelescop(String telescop) {
        setKeyword(FitsConstants.KEYWORD_TELESCOP, telescop);
    }

    /**
     * Get the value of INSTRUME keyword
     * @return the value of INSTRUME keyword
     */
    public String getInstrume() {
        return getKeyword(FitsConstants.KEYWORD_INSTRUME);
    }

    /**
     * Define the value of INSTRUME keyword
     * @param instrume value of INSTRUME keyword
     */
    public void setInstrume(String instrume) {
        setKeyword(FitsConstants.KEYWORD_INSTRUME, instrume);
    }

    /**
     * Get the value of OBSERVER keyword
     * @return the value of OBSERVER keyword
     */
    public String getObserver() {
        return getKeyword(FitsConstants.KEYWORD_OBSERVER);
    }

    /**
     * Define the value of OBSERVER keyword
     * @param observer value of OBSERVER keyword
     */
    public void setObserver(String observer) {
        setKeyword(FitsConstants.KEYWORD_OBSERVER, observer);
    }

    /**
     * Get the value of INSMODE keyword
     * @return the value of INSMODE keyword
     */
    public String getInsMode() {
        return getKeyword(FitsConstants.KEYWORD_INSMODE);
    }

    /**
     * Define the value of INSMODE keyword
     * @param insmode value of INSMODE keyword
     */
    public void setInsMode(String insmode) {
        setKeyword(FitsConstants.KEYWORD_INSMODE, insmode);
    }

    /**
     * Get the value of OBJECT keyword
     * @return the value of OBJECT keyword
     */
    public String getObject() {
        return getKeyword(FitsConstants.KEYWORD_OBJECT);
    }

    /**
     * Define the value of OBJECT keyword
     * @param object value of OBJECT keyword
     */
    public void setObject(String object) {
        setKeyword(FitsConstants.KEYWORD_OBJECT, object);
    }

    /**
     * Get the value of REFERENC keyword
     * @return the value of REFERENC keyword
     */
    public String getReferenc() {
        return getKeyword(FitsConstants.KEYWORD_REFERENC);
    }

    /**
     * Define the value of REFERENC keyword
     * @param referenc value of REFERENC keyword
     */
    public void setReferenc(String referenc) {
        setKeyword(FitsConstants.KEYWORD_REFERENC, referenc);
    }

    /**
     * Get the value of PROG_ID keyword
     * @return the value of PROG_ID keyword
     */
    public String getProgId() {
        return getKeyword(OIFitsConstants.KEYWORD_PROG_ID);
    }

    /**
     * Define the value of PROG_ID keyword
     * @param progId value of PROG_ID keyword
     */
    public void setProgId(String progId) {
        setKeyword(OIFitsConstants.KEYWORD_PROG_ID, progId);
    }

    /**
     * Get the value of PROCSOFT keyword
     * @return the value of PROCSOFT keyword
     */
    public String getProcsoft() {
        return getKeyword(OIFitsConstants.KEYWORD_PROCSOFT);
    }

    /**
     * Define the value of PROCSOFT keyword
     * @param procsoft value of PROCSOFT keyword
     */
    public void setProcsoft(String procsoft) {
        setKeyword(OIFitsConstants.KEYWORD_PROCSOFT, procsoft);
    }

    /**
     * Get the value of OBSTECH keyword
     * @return the value of OBSTECH keyword
     */
    public String getObsTech() {
        return getKeyword(OIFitsConstants.KEYWORD_OBSTECH);
    }

    /**
     * Define the value of OBSTECH keyword
     * @param obstech value of OBSTECH keyword
     */
    public void setObsTech(String obstech) {
        setKeyword(OIFitsConstants.KEYWORD_OBSTECH, obstech);
    }

    /**
     * Get the value of RA keyword
     * @return the value of RA keyword
     */
    public double getRa() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_RA, Double.NaN);
    }

    /**
     * Define the value of RA keyword
     * @param ra value of RA keyword
     */
    public void setRa(double ra) {
        setKeywordDouble(OIFitsConstants.KEYWORD_RA, ra);
    }

    /**
     * Get the value of DEC keyword
     * @return the value of DEC keyword
     */
    public double getDec() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_DEC, Double.NaN);
    }

    /**
     * Define the value of DEC keyword
     * @param dec value of DEC keyword
     */
    public void setDec(double dec) {
        setKeywordDouble(OIFitsConstants.KEYWORD_DEC, dec);
    }

    /**
     * Get the value of EQUINOX keyword
     * @return the value of EQUINOX keyword
     */
    public double getEquinox() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_EQUINOX, Double.NaN);
    }

    /**
     * Define the value of EQUINOX keyword
     * @param equinox value of EQUINOX keyword
     */
    public void setEquinox(double equinox) {
        setKeywordDouble(OIFitsConstants.KEYWORD_EQUINOX, equinox);
    }

    /**
     * Get the value of RADECSYS keyword
     * @return the value of RADECSYS keyword
     */
    public String getRadecSys() {
        return getKeyword(OIFitsConstants.KEYWORD_RADECSYS);
    }

    /**
     * Define the value of RADECSYS keyword
     * @param radecsys  value of RADECSYS keyword
     */
    public void setRadecSys(String radecsys) {
        setKeyword(OIFitsConstants.KEYWORD_RADECSYS, radecsys);
    }

    /**
     * Get the value of SPECSYS keyword
     * @return the value of SPECSYS keyword
     */
    public String getSpecSys() {
        return getKeyword(OIFitsConstants.KEYWORD_SPECSYS);
    }

    /**
     * Define the value of SPECSYS keyword
     * @param specsys  value of SPECSYS keyword
     */
    public void setSpecSys(String specsys) {
        setKeyword(OIFitsConstants.KEYWORD_SPECSYS, specsys);
    }

    /**
     * Get the value of TEXPTIME keyword
     * @return the value of TEXPTIME keyword
     */
    public double getTexpTime() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_TEXPTIME, Double.NaN);
    }

    /**
     * Define the value of TEXPTIME keyword
     * @param texptime value of TEXPTIME keyword
     */
    public void setTexpTime(double texptime) {
        setKeywordDouble(OIFitsConstants.KEYWORD_TEXPTIME, texptime);
    }

    /**
     * Get the value of MJD_OBS keyword
     * @return the value of MJD_OBS keyword
     */
    public double getMJDObs() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_MJD_OBS, Double.NaN);
    }

    /**
     * Define the value of MJD_OBS keyword
     * @param mjdObs value of MJD_OBS keyword
     */
    public void setMJDObs(double mjdObs) {
        setKeywordDouble(OIFitsConstants.KEYWORD_MJD_OBS, mjdObs);
    }

    /**
     * Get the value of MJD_END keyword
     * @return the value of MJD_END keyword
     */
    public double getMJDEnd() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_MJD_END, Double.NaN);
    }

    /**
     * Define the value of MJD_END keyword
     * @param mjdEnd value of MJD_END keyword
     */
    public void setMJDEnd(double mjdEnd) {
        setKeywordDouble(OIFitsConstants.KEYWORD_MJD_END, mjdEnd);
    }

    /**
     * Get the value of BASE_MIN keyword
     * @return the value of BASE_MIN keyword
     */
    public double getBaseMin() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_BASE_MIN, Double.NaN);
    }

    /**
     * Define the value of BASE_MIN keyword
     * @param baseMin value of BASE_MIN keyword
     */
    public void setBaseMin(double baseMin) {
        setKeywordDouble(OIFitsConstants.KEYWORD_BASE_MIN, baseMin);
    }

    /**
     * Get the value of BASE_MAX keyword
     * @return the value of BASE_MAX keyword
     */
    public double getBaseMax() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_BASE_MAX, Double.NaN);
    }

    /**
     * Define the value of BASE_MAX keyword
     * @param baseMax value of BASE_MAX keyword
     */
    public void setBaseMax(double baseMax) {
        setKeywordDouble(OIFitsConstants.KEYWORD_BASE_MAX, baseMax);
    }

    /**
     * Get the value of WAVELMIN keyword
     * @return the value of WAVELMIN keyword
     */
    public double getWavelMin() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_WAVELMIN, Double.NaN);
    }

    /**
     * Define the value of WAVELMIN keyword
     * @param wavelmin value of WAVELMIN keyword
     */
    public void setWavelMin(double wavelmin) {
        setKeywordDouble(OIFitsConstants.KEYWORD_WAVELMIN, wavelmin);
    }

    /**
     * Get the value of WAVELMAX keyword
     * @return the value of WAVELMAX keyword
     */
    public double getWavelMax() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_WAVELMAX, Double.NaN);
    }

    /**
     * Define the value of WAVELMAX keyword
     * @param wavelmax value of WAVELMAX keyword
     */
    public void setWavelMax(double wavelmax) {
        setKeywordDouble(OIFitsConstants.KEYWORD_WAVELMAX, wavelmax);
    }

    /**
     * Get the value of NUM_CHAN keyword
     * @return the value of NUM_CHAN keyword
     */
    public double getNumChan() {
        return getKeywordInt(OIFitsConstants.KEYWORD_NUM_CHAN);
    }

    /**
     * Define the value of NUM_CHAN keyword
     * @param numChan value of NUM_CHAN keyword
     */
    public void setNumChan(double numChan) {
        setKeywordDouble(OIFitsConstants.KEYWORD_NUM_CHAN, numChan);
    }

    /**
     * Get the value of SPEC_RES keyword
     * @return the value of SPEC_RES keyword
     */
    public double getSpecRes() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_SPEC_RES, Double.NaN);
    }

    /**
     * Define the value of SPEC_RES keyword
     * @param specRes value of SPEC_RES keyword
     */
    public void setSpecRes(double specRes) {
        setKeywordDouble(OIFitsConstants.KEYWORD_SPEC_RES, specRes);
    }

    /**
     * Get the value of VIS2ERR keyword
     * @return the value of VIS2ERR keyword
     */
    public double getVis2Err() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_VIS2ERR, Double.NaN);
    }

    /**
     * Define the value of VIS2ERR keyword
     * @param vis2err value of VIS2ERR keyword
     */
    public void setVis2Err(double vis2err) {
        setKeywordDouble(OIFitsConstants.KEYWORD_VIS2ERR, vis2err);
    }

    /**
     * Get the value of VISPHERR keyword
     * @return the value of VISPHERR keyword
     */
    public double getVisPhiErr() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_VISPHERR, Double.NaN);
    }

    /**
     * Define the value of VISPHERR keyword
     * @param visphierr value of VISPHERR keyword
     */
    public void setVisPhiErr(double visphierr) {
        setKeywordDouble(OIFitsConstants.KEYWORD_VISPHERR, visphierr);
    }

    /**
     * Get the value of T3PHIERR keyword
     * @return the value of T3PHIERR keyword
     */
    public double getT3PhiErr() {
        return getKeywordDouble(OIFitsConstants.KEYWORD_T3PHIERR, Double.NaN);
    }

    /**
     * Define the value of T3PHIERR keyword
     * @param t3phierr value of T3PHIERR keyword
     */
    public void setT3PhiErr(double t3phierr) {
        setKeywordDouble(OIFitsConstants.KEYWORD_T3PHIERR, t3phierr);
    }

    /**
     * Check syntax of table's keywords. It consists in checking all mandatory
     * keywords are present, with right name, right format and right values (if
     * they do belong to a given set of accepted values).
     *
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        checkDateObsKeyword(checker, OIFitsConstants.KEYWORD_DATE_OBS, this);
        checkMJD(checker, OIFitsConstants.KEYWORD_MJD_OBS, getKeywordDouble(OIFitsConstants.KEYWORD_MJD_OBS, Double.NaN));
        checkMJD(checker, OIFitsConstants.KEYWORD_MJD_END, getKeywordDouble(OIFitsConstants.KEYWORD_MJD_END, Double.NaN));
    }

    /**
     * Check the keyword concern by the rule if the value "MULTI" is present
     * @param checker checker component
     */
    void checkMULTIKeywordHDU(final OIFitsChecker checker) {
        checkValueContains(checker, FitsConstants.KEYWORD_TELESCOP, "MULTI");
        checkValueContains(checker, FitsConstants.KEYWORD_INSTRUME, "MULTI");
        checkValueContains(checker, FitsConstants.KEYWORD_OBSERVER, "MULTI");
        checkValueContains(checker, FitsConstants.KEYWORD_OBJECT, "MULTI");
        checkValueContains(checker, FitsConstants.KEYWORD_INSMODE, "MULTI");
        checkValueContains(checker, FitsConstants.KEYWORD_REFERENC, "MULTI");
        checkValueContains(checker, OIFitsConstants.KEYWORD_PROG_ID, "MULTI");
        checkValueContains(checker, OIFitsConstants.KEYWORD_PROCSOFT, "MULTI");
        checkValueContains(checker, OIFitsConstants.KEYWORD_OBSTECH, "MULTI");
    }

    private void checkValueContains(final OIFitsChecker checker, final String keywordName, final String value) {
        if (getKeyword(keywordName) != null) {
            if (!getKeyword(keywordName).startsWith(value) || OIFitsChecker.isInspectRules()) {
                checker.ruleFailed(Rule.PRIMARYHDU_MULTI_TARGET, this, keywordName).addKeywordValue(value);
            }
        }
    }

    /**
     * Check the keyword concern by the rule Bookkeping keyword
     * @param checker checker component
     */
    void checkBookkeepingKeyword(final OIFitsChecker checker) {

        if (getKeyword(FitsConstants.KEYWORD_REFERENC) == null || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.PRIMARYHDU_TYPE_ATOMIC, this, FitsConstants.KEYWORD_REFERENC);
        }
        if (getKeyword(OIFitsConstants.KEYWORD_PROG_ID) == null || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.PRIMARYHDU_TYPE_ATOMIC, this, OIFitsConstants.KEYWORD_PROG_ID);
        }
        if (getKeyword(OIFitsConstants.KEYWORD_PROCSOFT) == null || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.PRIMARYHDU_TYPE_ATOMIC, this, OIFitsConstants.KEYWORD_PROCSOFT);
        }
        if (getKeyword(OIFitsConstants.KEYWORD_OBSTECH) == null || OIFitsChecker.isInspectRules()) {
            checker.ruleFailed(Rule.PRIMARYHDU_TYPE_ATOMIC, this, OIFitsConstants.KEYWORD_OBSTECH);
        }

    }

}
