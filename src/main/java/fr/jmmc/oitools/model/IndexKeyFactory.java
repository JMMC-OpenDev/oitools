/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * This class creates IndexKey instances to group related data. 
 * @author bourgesl
 */
public final class IndexKeyFactory {

    /** singleton */
    public final static IndexKeyFactory INSTANCE = new IndexKeyFactory();

    private final static double SEC_IN_DAY = 24 * 3600;

    private final static double DEF_PREC_MJD = 30.0; // 30s
    private final static double DEF_PREC_UV = 3e-2;  // 3cm in meters

    // members:
    // TODO: define preferences to define these precision values
    private double precMjd; // in seconds
    private double precUV; // in meters

    private IndexKeyFactory() {
        super();
        setPrecMjd(DEF_PREC_MJD);
        setPrecUV(DEF_PREC_UV);
    }

    public double getPrecMjd() {
        return precMjd;
    }

    public void setPrecMjd(double precMjd) {
        this.precMjd = precMjd;
    }

    public double getPrecUV() {
        return precUV;
    }

    public void setPrecUV(final double precUV) {
        this.precUV = precUV;
    }

    IndexKey create(final String targetUID, final String insModeUID, final double mjd, final double ucoord, final double vcoord) {
        return new IndexKeyUV(targetUID, insModeUID,
                /* rounding needed */
                round(mjd, precMjd / SEC_IN_DAY),
                round(ucoord, precUV),
                round(vcoord, precUV)
        );
    }

    IndexKey create(final String targetUID, final String insModeUID, final double mjd, final String staNames) {
        return new IndexKeyBL(targetUID, insModeUID,
                /* rounding needed */
                round(mjd, precMjd / SEC_IN_DAY),
                staNames
        );
    }

    private static double round(final double val, final double prec) {
        return Double.isFinite(val) ? (prec * Math.round(val / prec)) : Double.NaN;
    }

}
