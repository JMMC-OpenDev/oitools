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
package fr.jmmc.oitools;

/**
 * This interface contains useful constants of the OIFITS format.
 * Some related OIFits extensions for Imaging may be part of ImageOiConstants.
 * @author bourgesl, mellag
 */
public interface OIFitsConstants {

    /** UNKNOWN value */
    public final static String UNKNOWN_VALUE = "UNKNOWN";

    /* OIFits standard 1.0 */
    /** Table OI_ARRAY */
    public final static String TABLE_OI_ARRAY = "OI_ARRAY";
    /** Table OI_TARGET */
    public final static String TABLE_OI_TARGET = "OI_TARGET";
    /** Table OI_WAVELENGTH */
    public final static String TABLE_OI_WAVELENGTH = "OI_WAVELENGTH";
    /** Table OI_VIS */
    public final static String TABLE_OI_VIS = "OI_VIS";
    /** Table OI_VIS2 */
    public final static String TABLE_OI_VIS2 = "OI_VIS2";
    /** Table OI_T3 */
    public final static String TABLE_OI_T3 = "OI_T3";

    /* OIFits standard 2.0 */
    /** Primary HDU */
    public final static String PRIMARY_HDU = "MAIN_HEADER";

    /** Table OI_FLUX */
    public final static String TABLE_OI_FLUX = "OI_FLUX";
    /** Table OI_CORR */
    public final static String TABLE_OI_CORR = "OI_CORR";
    /** Table OI_INSPOL */
    public final static String TABLE_OI_INSPOL = "OI_INSPOL";

    /* Main header keyword */
    /** PROG_ID = Program ID */
    public final static String KEYWORD_PROG_ID = "PROG_ID";
    /** PROCSOFT = Versioned Data Reduction Software */
    public final static String KEYWORD_PROCSOFT = "PROCSOFT";
    /** OBSTECH = Technique of observation */
    public final static String KEYWORD_OBSTECH = "OBSTECH";
    /** RA = Target Right Ascension at mean EQUINOX (deg) */
    public final static String KEYWORD_RA = "RA";
    /** DEC = Target Declination at mean EQUINOX (deg) */
    public final static String KEYWORD_DEC = "DEC";
    /** EQUINOX = Standard FK5 (years) */
    public final static String KEYWORD_EQUINOX = "EQUINOX";
    /** RADECSYS = Coordinate reference frame */
    public final static String KEYWORD_RADECSYS = "RADECSYS";
    /** SPECSYS = Reference frame for spectral coord */
    public final static String KEYWORD_SPECSYS = "SPECSYS";
    /** TEXPTIME = Maximum elapsed time for data point(s)*/
    public final static String KEYWORD_TEXPTIME = "TEXPTIME";
    /** MJD_OBS = Start of observation (MJD) */
    public final static String KEYWORD_MJD_OBS = "MJD-OBS";
    /** MJD_END = End of observation (MJD) */
    public final static String KEYWORD_MJD_END = "MJD-END";
    /** BASE_MIN = Minimum projected baseline length (m) */
    public final static String KEYWORD_BASE_MIN = "BASE_MIN";
    /** BASE_MAX = Maximum projected baseline length (m) */
    public final static String KEYWORD_BASE_MAX = "BASE_MAX";
    /** WAVELMIN = Minimum wavelength (m) */
    public final static String KEYWORD_WAVELMIN = "WAVELMIN";
    /** WAVELMAX = Maximum wavelength (m)*/
    public final static String KEYWORD_WAVELMAX = "WAVELMAX";
    /** NUM_CHAN = Total number of spectral channels */
    public final static String KEYWORD_NUM_CHAN = "NUM_CHAN";
    /** SPEC_RES = Reference spectral resolution (λ/Δ)*/
    public final static String KEYWORD_SPEC_RES = "SPEC_RES";
    /** VIS2ERR = Representative V² error (%) */
    public final static String KEYWORD_VIS2ERR = "VIS2ERR";
    /** VISPHERR = Representative Diff Vis Phase error (deg) */
    public final static String KEYWORD_VISPHERR = "VISPHERR";
    /** T3PHIERR = Representative Closure Phase error (deg)*/
    public final static String KEYWORD_T3PHIERR = "T3PHIERR";

    /* shared keywords or columns */
    /** OI_REVN keyword */
    public final static String KEYWORD_OI_REVN = "OI_REVN";
    /** OI_REVN = 1 keyword value */
    public final static int KEYWORD_OI_REVN_1 = 1;
    /** OI_REVN = 2 keyword value */
    public final static int KEYWORD_OI_REVN_2 = 2;
    /** ARRNAME keyword */
    public final static String KEYWORD_ARRNAME = "ARRNAME";
    /** INSNAME keyword */
    public final static String KEYWORD_INSNAME = "INSNAME";
    /** TARGET_ID column */
    public final static String COLUMN_TARGET_ID = "TARGET_ID";
    /** STA_INDEX column */
    public final static String COLUMN_STA_INDEX = "STA_INDEX";
    /** special prefix for column names (OI-Interface Model) */
    public final static String COLUMN_PREFIX_NS_MODEL = "NS_MODEL_";

    /*
    New columns in OI_VIS table
    Label             Format   Description
    NS_MODEL_VISAMP   D(NWAVE) Model of the visibility amplitude
    NSMODELVISAMPERR  D(NWAVE) Model of the error in visibility amplitude (optional)            TODO
    NSMODELVISPHI     D(NWAVE) Model of the visibility phase in degrees
    NSMODELVISPHIERR  D(NWAVE) Model of the error in visibility phase in degrees (optional)     TODO
    
    New column in OI_VIS2 table
    Label             Format   Description
    NS_MODEL_VIS2     D(NWAVE) Model of the squared visibility
    NS_MODEL_VIS2ERR  D(NWAVE) Model of the error in squared visibility (optional)
    
    New columns in OI_T3 table
    Label             Format   Description
    NS_MODEL_T3AMP    D(NWAVE) Model of the triple-product amplitude
    NS_MODEL_T3AMPERR D(NWAVE) Model of the error in triple-product amplitude (optional)        TODO
    NS_MODEL_T3PHI    D(NWAVE) Model of the triple-product phase in degrees
    NS_MODEL_T3PHIERR D(NWAVE) Model of the error in triple-product phase in degrees (optional) TODO
     */

 /* OI_ARRAY table */
    /** FRAME   keyword */
    public final static String KEYWORD_FRAME = "FRAME";
    /** FRAME GEOCENTRIC keyword value */
    public final static String KEYWORD_FRAME_GEOCENTRIC = "GEOCENTRIC";
    /** FRAME SKY keyword value */
    public final static String KEYWORD_FRAME_SKY = "SKY";
    /** ARRAYX  keyword */
    public final static String KEYWORD_ARRAY_X = "ARRAYX";
    /** ARRAYY  keyword */
    public final static String KEYWORD_ARRAY_Y = "ARRAYY";
    /** ARRAYZ  keyword */
    public final static String KEYWORD_ARRAY_Z = "ARRAYZ";
    /** TEL_NAME column */
    public final static String COLUMN_TEL_NAME = "TEL_NAME";
    /** STA_NAME column */
    public final static String COLUMN_STA_NAME = "STA_NAME";
    /** DIAMETER column */
    public final static String COLUMN_DIAMETER = "DIAMETER";
    /** STAXYZ column */
    public final static String COLUMN_STA_XYZ = "STAXYZ";
    /* OIFits standard 2.0 */
    /** FOV column */
    public final static String COLUMN_FOV = "FOV";
    /** FOV_TYPE column */
    public final static String COLUMN_FOVTYPE = "FOVTYPE";
    /** FOV_TYPE FWHM column */
    public final static String COLUMN_FOVTYPE_FWHM = "FWHM";
    /** FOV_TYPE RADIUS column */
    public final static String COLUMN_FOVTYPE_RADIUS = "RADIUS";

    /* OI_WAVELENGTH table */
    /** EFF_WAVE column */
    public final static String COLUMN_EFF_WAVE = "EFF_WAVE";
    /** EFF_BAND column */
    public final static String COLUMN_EFF_BAND = "EFF_BAND";

    /* OI_TARGET table */
    /** TARGET column */
    public final static String COLUMN_TARGET = "TARGET";
    /** RAEP0 column */
    public final static String COLUMN_RAEP0 = "RAEP0";
    /** DECEP0 column */
    public final static String COLUMN_DECEP0 = "DECEP0";
    /** EQUINOX column */
    public final static String COLUMN_EQUINOX = "EQUINOX";
    /** RA_ERR column */
    public final static String COLUMN_RA_ERR = "RA_ERR";
    /** DEC_ERR column */
    public final static String COLUMN_DEC_ERR = "DEC_ERR";
    /** SYSVEL column */
    public final static String COLUMN_SYSVEL = "SYSVEL";
    /** VELTYP column */
    public final static String COLUMN_VELTYP = "VELTYP";
    /** VELTYP LSR column value */
    public final static String COLUMN_VELTYP_LSR = "LSR";
    /** VELTYP HELIOCEN column value */
    public final static String COLUMN_VELTYP_HELIOCEN = "HELIOCEN";
    /** VELTYP BARYCENT column value */
    public final static String COLUMN_VELTYP_BARYCENT = "BARYCENT";
    /** VELTYP GEOCENTR column value */
    public final static String COLUMN_VELTYP_GEOCENTR = "GEOCENTR";
    /** VELTYP TOPOCENT column value */
    public final static String COLUMN_VELTYP_TOPOCENT = "TOPOCENT";
    /** VELDEF column */
    public final static String COLUMN_VELDEF = "VELDEF";
    /** VELDEF RADIO column value */
    public final static String COLUMN_VELDEF_RADIO = "RADIO";
    /** VELDEF OPTICAL column value */
    public final static String COLUMN_VELDEF_OPTICAL = "OPTICAL";
    /** PMRA column */
    public final static String COLUMN_PMRA = "PMRA";
    /** PMDEC column */
    public final static String COLUMN_PMDEC = "PMDEC";
    /** PMRA_ERR column */
    public final static String COLUMN_PMRA_ERR = "PMRA_ERR";
    /** PMDEC_ERR column */
    public final static String COLUMN_PMDEC_ERR = "PMDEC_ERR";
    /** PARALLAX column */
    public final static String COLUMN_PARALLAX = "PARALLAX";
    /** PARA_ERR column */
    public final static String COLUMN_PARA_ERR = "PARA_ERR";
    /** SPECTYP column */
    public final static String COLUMN_SPECTYP = "SPECTYP";
    /* OIFits standard 2.0 */
    /** CATEGORY column */
    public final static String COLUMN_CATEGORY = "CATEGORY";
    /** CATEGORY CAL column */
    public final static String COLUMN_CATEGORY_CAL = "CAL";
    /** CATEGORY SCI column */
    public final static String COLUMN_CATEGORY_SCI = "SCI";

    /* OI DATA tables */
    /** DATE-OBS keyword */
    public final static String KEYWORD_DATE_OBS = "DATE-OBS";
    /** CORRNAME keyword V2*/
    public final static String KEYWORD_CORRNAME = "CORRNAME";
    /** TIME column */
    public final static String COLUMN_TIME = "TIME";
    /** MJD column */
    public final static String COLUMN_MJD = "MJD";
    /** INT_TIME column */
    public final static String COLUMN_INT_TIME = "INT_TIME";
    /** UCOORD column */
    public final static String COLUMN_UCOORD = "UCOORD";
    /** VCOORD column */
    public final static String COLUMN_VCOORD = "VCOORD";
    /** FLAG column */
    public final static String COLUMN_FLAG = "FLAG";

    /* OI_VIS table */
    /** AMPTYP keyword V2 */
    public final static String KEYWORD_AMPTYP = "AMPTYP";
    /** AMPTYP ABSOLUTE  keyword V2 */
    public final static String KEYWORD_AMPTYP_ABSOLUTE = "absolute";
    /** AMPTYP CORR keyword V2 */
    public final static String KEYWORD_AMPTYP_CORR = "correlated flux";
    /** AMPTYP DIFF keyword V2 */
    public final static String KEYWORD_AMPTYP_DIFF = "differential";
    /** PHITYP keyword V2 */
    public final static String KEYWORD_PHITYP = "PHITYP";
    /** PHITYP ABSOLUTE  keyword V2 */
    public final static String KEYWORD_PHITYP_ABSOLUTE = "absolute";
    /** PHITYP DIFF keyword V2 */
    public final static String KEYWORD_PHITYP_DIFF = "differential";
    /** AMPORDER keyword V2 */
    public final static String KEYWORD_AMPORDER = "AMPORDER";
    /** PHIORDER keyword V2 */
    public final static String KEYWORD_PHIORDER = "PHIORDER";
    /** VISAMP column */
    public final static String COLUMN_VISAMP = "VISAMP";
    /** VISAMP MODEL column */
    public final static String COLUMN_NS_MODEL_VISAMP = COLUMN_PREFIX_NS_MODEL + COLUMN_VISAMP;
    /** VISAMPERR column */
    public final static String COLUMN_VISAMPERR = "VISAMPERR";
    /** VISPHI column */
    public final static String COLUMN_VISPHI = "VISPHI";
    /** VISPHI MODEL column */
    public final static String COLUMN_NS_MODEL_VISPHI = COLUMN_PREFIX_NS_MODEL + COLUMN_VISPHI;
    /** VISPHIERR column */
    public final static String COLUMN_VISPHIERR = "VISPHIERR";
    /* OIFits standard 2.0 */
    /** CORRINDX_VISAMP column */
    public final static String COLUMN_CORRINDX_VISAMP = "CORRINDX_VISAMP";
    /** CORRINDX_VISPHI column */
    public final static String COLUMN_CORRINDX_VISPHI = "CORRINDX_VISPHI";
    /** VISREFMAP column */
    public final static String COLUMN_VISREFMAP = "VISREFMAP";
    /** RVIS column */
    public final static String COLUMN_RVIS = "RVIS";
    /** RVISERR column */
    public final static String COLUMN_RVISERR = "RVISERR";
    /** CORRINDX_RVIS column */
    public final static String COLUMN_CORRINDX_RVIS = "CORRINDX_RVIS";
    /** IVIS column */
    public final static String COLUMN_IVIS = "IVIS";
    /** IVISERR column */
    public final static String COLUMN_IVISERR = "IVISERR";
    /** CORRINDX_IVIS column */
    public final static String COLUMN_CORRINDX_IVIS = "CORRINDX_IVIS";

    /* Aspro Extension with complex visibilities (like AMBER OIFits) */
    /** VISDATA column */
    public final static String COLUMN_VISDATA = "VISDATA";
    /** VISERR column */
    public final static String COLUMN_VISERR = "VISERR";

    /* OI_VIS2 table */
    /** VIS2DATA column */
    public final static String COLUMN_VIS2DATA = "VIS2DATA";
    /** VIS2ERR column */
    public final static String COLUMN_VIS2ERR = "VIS2ERR";
    /** VIS2DATA MODEL column */
    public final static String COLUMN_NS_MODEL_VIS2DATA = COLUMN_PREFIX_NS_MODEL + COLUMN_VIS2DATA;
    /** VIS2 MODEL column (alias for VIS2DATA MODEL) */
    public final static String COLUMN_NS_MODEL_VIS2DATA_ALIAS = COLUMN_PREFIX_NS_MODEL + "VIS2";
    /** VIS2ERR MODEL column */
    public final static String COLUMN_NS_MODEL_VIS2ERR = COLUMN_PREFIX_NS_MODEL + COLUMN_VIS2ERR;
    /* OIFits standard 2.0 */
    /** CORRINDX_VIS2DATA column */
    public final static String COLUMN_CORRINDX_VIS2DATA = "CORRINDX_VIS2DATA";

    /* Aspro Extension with square correlated flux and photometry */
    /** NS_CORRSQ column */
    public final static String COLUMN_NS_CORRSQ = "NS_CORRSQ";
    /** NS_CORRSQERR column */
    public final static String COLUMN_NS_CORRSQ_ERR = "NS_CORRSQERR";
    /** NS_PHOT column */
    public final static String COLUMN_NS_PHOT = "NS_PHOT";
    /** NS_PHOTERR column */
    public final static String COLUMN_NS_PHOT_ERR = "NS_PHOTERR";

    /* OI_T3 table */
    /** T3AMP column */
    public final static String COLUMN_T3AMP = "T3AMP";
    /** T3AMP MODEL column */
    public final static String COLUMN_NS_MODEL_T3AMP = COLUMN_PREFIX_NS_MODEL + COLUMN_T3AMP;
    /** T3AMPERR column */
    public final static String COLUMN_T3AMPERR = "T3AMPERR";
    /** T3PHI column */
    public final static String COLUMN_T3PHI = "T3PHI";
    /** T3PHI MODEL column */
    public final static String COLUMN_NS_MODEL_T3PHI = COLUMN_PREFIX_NS_MODEL + COLUMN_T3PHI;
    /** T3PHIERR column */
    public final static String COLUMN_T3PHIERR = "T3PHIERR";
    /** U1COORD column */
    public final static String COLUMN_U1COORD = "U1COORD";
    /** V1COORD column */
    public final static String COLUMN_V1COORD = "V1COORD";
    /** U2COORD column */
    public final static String COLUMN_U2COORD = "U2COORD";
    /** V2COORD column */
    public final static String COLUMN_V2COORD = "V2COORD";
    /* OIFits standard 2.0 */
    /** CORRINDX_T3AMP column */
    public final static String COLUMN_CORRINDX_T3AMP = "CORRINDX_T3AMP";
    /** CORRINDX_T3PHI column */
    public final static String COLUMN_CORRINDX_T3PHI = "CORRINDX_T3PHI";

    /* OI_FLUX table */
    /** FOV keyword */
    public final static String KEYWORD_FOV = "FOV";
    /** FOV_TYPE keyword */
    public final static String KEYWORD_FOV_TYPE = "FOVTYPE";
    /** CALSTAT keyword */
    public final static String KEYWORD_CALSTAT = "CALSTAT";
    /** CALSTAT U keyword */
    public final static String KEYWORD_CALSTAT_U = "U";
    /** CALSTAT C keyword */
    public final static String KEYWORD_CALSTAT_C = "C";
    /** FLUXDATA column */
    public final static String COLUMN_FLUXDATA = "FLUXDATA";
    /** FLUXERR column */
    public final static String COLUMN_FLUXERR = "FLUXERR";
    /** CORRINDX_FLUXDATA column */
    public final static String COLUMN_CORRINDX_FLUXDATA = "CORRINDX_FLUXDATA";
    /* OI_FLUX table */
    /** FLUX column */
    public final static String COLUMN_FLUX = "FLUX";

    /* OI_CORR table */
    /** NDATA keyword */
    public final static String KEYWORD_NDATA = "NDATA";
    /** IINDX column */
    public final static String COLUMN_IINDX = "IINDX";
    /** JINDX column */
    public final static String COLUMN_JINDX = "JINDX";
    /** CORR column */
    public final static String COLUMN_CORR = "CORR";

    /* OI_INSPOL table */
    /** NPOL keyword */
    public final static String KEYWORD_NPOL = "NPOL";
    /** ORIENT keyword */
    public final static String KEYWORD_ORIENT = "ORIENT";
    /** ORIENT NORTH keyword */
    public final static String KEYWORD_ORIENT_NORTH = "NORTH";
    /** ORIENT LABORATORY keyword */
    public final static String KEYWORD_ORIENT_LABORATORY = "LABORATORY";
    /** MODEL keyword */
    public final static String KEYWORD_MODEL = "MODEL";
    /** MJD_OBS column */
    public final static String COLUMN_MJD_OBS = "MJD_OBS";
    /** MJD_END column */
    public final static String COLUMN_MJD_END = "MJD_END";
    /** JXX column */
    public final static String COLUMN_JXX = "JXX";
    /** JYY column */
    public final static String COLUMN_JYY = "JYY";
    /** JXY column */
    public final static String COLUMN_JXY = "JXY";
    /** JYX column */
    public final static String COLUMN_JYX = "JYX";

    /* derived columns */
    /** STA_CONF derived OIData column as short[] */
    public final static String COLUMN_STA_CONF = "STA_CONF";
    /** HOUR_ANGLE derived OIData column as double[] */
    public final static String COLUMN_HOUR_ANGLE = "HOUR_ANGLE";
    /** RADIUS derived OIData column as double[] */
    public final static String COLUMN_RADIUS = "RADIUS";
    /** POS_ANGLE derived OIData column as double[] */
    public final static String COLUMN_POS_ANGLE = "POS_ANGLE";
    /** SPATIAL_FREQ derived OIData column as double[][] */
    public final static String COLUMN_SPATIAL_FREQ = "SPATIAL_FREQ";
    /** NIGHT_ID derived OiData column as double[] */
    public final static String COLUMN_NIGHT_ID = "NIGHT_ID";
    /** UCOORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_UCOORD_SPATIAL = "UCOORD_SPATIAL";
    /** VCOORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_VCOORD_SPATIAL = "VCOORD_SPATIAL";
    /** U1COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_U1COORD_SPATIAL = "U1COORD_SPATIAL";
    /** V1COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_V1COORD_SPATIAL = "V1COORD_SPATIAL";
    /** U2COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_U2COORD_SPATIAL = "U2COORD_SPATIAL";
    /** V2COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_V2COORD_SPATIAL = "V2COORD_SPATIAL";
    
    /** SNR_VIS2 derived OIVIS2 column as double[][] */
    public final static String COLUMN_SNR_VIS2 = "SNR_VIS2";
    /** SNR_MODEL_VIS2 derived OIVIS2 column as double[][] */
    public final static String COLUMN_SNR_MODEL_VIS2 = "SNR_MODEL_VIS2";
    /** SNR_NS_CORRSQ derived OIVIS2 column as double[][] */
    public final static String COLUMN_SNR_NS_CORRSQ = "SNR_NS_CORRSQ";
    /** SNR_NS_PHOT derived OIVIS2 column as double[][] */
    public final static String COLUMN_SNR_NS_PHOT = "SNR_NS_PHOT";
    /** RES_VIS2_MODEL derived OIVIS2 column as double[][] */
    public final static String COLUMN_RES_VIS2_MODEL = "RES_VIS2_MODEL";
    
    /** SNR_VISPHI derived OIVIS column as double[][] */
    public final static String COLUMN_SNR_VISPHI = "SNR_VISPHI";
    /** RES_VISAMP_MODEL derived OIVIS column as double[][] */
    public final static String COLUMN_RES_VISAMP_MODEL = "RES_VISAMP_MODEL";
    /** RES_VISPHI_MODEL derived OIVIS column as double[][] */
    public final static String COLUMN_RES_VISPHI_MODEL = "RES_VISPHI_MODEL";
    
    /** SNR_T3PHI derived T3 column as double[][] */
    public final static String COLUMN_SNR_T3PHI = "SNR_T3PHI";
    /** RES_T3AMP_MODEL derived T3 column as double[][] */
    public final static String COLUMN_RES_T3AMP_MODEL = "RES_T3AMP_MODEL";
    /** RES_T3PHI_MODEL derived T3 column as double[][] */
    public final static String COLUMN_RES_T3PHI_MODEL = "RES_T3PHI_MODEL";
    
    /** Resolution value */
    public final static String VALUE_RESOLUTION = "RESOLUTION";
}
