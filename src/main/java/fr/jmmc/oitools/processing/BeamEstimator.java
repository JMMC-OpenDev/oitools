/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import fr.jmmc.oitools.model.IndexMask;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIVis2;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Based on https://github.com/JMMC-OpenDev/oibeam
 *  
 * See https://en.wikipedia.org/wiki/Eigenvalue_algorithm
 *
 * @author bourgesl
 */
public class BeamEstimator {

    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(BeamEstimator.class.getName());

    private final static boolean DO_CHECKS = false;

    private static final double STDDEV_TO_HWHM = Math.sqrt(2.0 * Math.log(2.0));

    private final static double SCALE_FACTOR = STDDEV_TO_HWHM / (2.0 * Math.PI);
    /** Specify the value of one milli arcsecond in degrees */
    public static final double DEG_IN_MILLI_ARCSEC = 3600000d;

    public static BeamInfo computeBeamInfo(final SelectorResult result) {
        int n = 0;
        double s11 = 0.0;
        double s22 = 0.0;
        double s12 = 0.0;

        // TODO: how to handle only OIVIS or OIT3 coords or mix (VIS/VIS2/T3) tables that may have repeated U-V coords ?
        for (final OIData oiData : result.getOIDatas()) {
            final double[][] u; // rad-1
            final double[][] v; // rad-1

            if (oiData instanceof OIVis2) {
                final OIVis2 vis2 = (OIVis2) oiData;

                u = vis2.getSpatialUCoord();
                v = vis2.getSpatialVCoord();
                /*
            } 
            else if (oiData instanceof OIVis) {
                final OIVis vis = (OIVis) oiData;

                u = vis.getSpatialUCoord();
                v = vis.getSpatialVCoord();
                 */
            } else {
                continue;
            }

            // get the optional masks for this OIData table:
            final IndexMask maskOIData1D = result.getDataMask1DNotFull(oiData);
            final IndexMask maskOIData2D = result.getDataMask2DNotFull(oiData);
            // get the optional wavelength mask for the OIData's wavelength table:
            final IndexMask maskWavelength = result.getWavelengthMaskNotFull(oiData.getOiWavelength());

            final int idxNone = (maskOIData2D != null) ? maskOIData2D.getIndexNone() : -1;
            final int idxFull = (maskOIData2D != null) ? maskOIData2D.getIndexFull() : -1;

            final int nRows = oiData.getNbRows();
            final int nWaves = oiData.getNWave();

            final boolean[][] flags = oiData.getFlag();

            IndexMask maskOIData2DRow = null;

            // Iterate on table rows (i):
            for (int i = 0; i < nRows; i++) {

                // check optional data mask 1D:
                if ((maskOIData1D != null) && !maskOIData1D.accept(i)) {
                    // if bit is false for this row, we hide this row
                    continue;
                }

                // check mask 2D for row None flag:
                if (maskOIData2D != null) {
                    if (maskOIData2D.accept(i, idxNone)) {
                        // row flagged as None:
                        continue;
                    }
                    // check row flagged as Full:
                    maskOIData2DRow = (maskOIData2D.accept(i, idxFull)) ? null : maskOIData2D;
                }

                final boolean[] rowFlags = (flags != null) ? flags[i] : null;

                // Iterate on wave channels (l):
                for (int l = 0; l < nWaves; l++) {

                    // check optional wavelength mask:
                    if ((maskWavelength != null) && !maskWavelength.accept(l)) {
                        // if bit is false for this row, we hide this row
                        continue;
                    }

                    // check optional data mask 2D (and its Full flag):
                    if ((maskOIData2DRow != null) && !maskOIData2DRow.accept(i, l)) {
                        // if bit is false for this row, we hide this row
                        continue;
                    }

                    if ((rowFlags != null) && rowFlags[l]) {
                        // data point is flagged so skip it:
                        continue;
                    }

                    // data point is valid and not flagged:
                    final double uc = u[i][l];
                    final double vc = v[i][l];

                    s11 += 2.0 * (uc * uc);
                    s22 += 2.0 * (vc * vc);
                    s12 += 2.0 * (uc * vc);
                    n++;
                }
            }
        }
        logger.log(Level.FINE, "n: {0}", n);

        if (n != 0) {
            // normalize by (2n + 1):
            final double invNorm = 1.0 / (2.0 * n + 1.0);

            s11 *= invNorm;
            s22 *= invNorm;
            s12 *= invNorm;

            final double[] covMatrix = {s11, s12, s12, s22};
            return computeBeam(covMatrix);
        }
        return null;
    }

    public static BeamInfo computeBeamInfo(final Collection<OIData> oiDatas) {
        int n = 0;
        double s11 = 0.0;
        double s22 = 0.0;
        double s12 = 0.0;

        // TODO: how to handle only OIVIS or OIT3 coords or mix (VIS/VIS2/T3) tables that may have repeated U-V coords ?
        for (OIData oiData : oiDatas) {
            final double[][] u; // rad-1
            final double[][] v; // rad-1

            if (oiData instanceof OIVis2) {
                final OIVis2 vis2 = (OIVis2) oiData;

                u = vis2.getSpatialUCoord();
                v = vis2.getSpatialVCoord();
                /*
            } 
            else if (oiData instanceof OIVis) {
                final OIVis vis = (OIVis) oiData;

                u = vis.getSpatialUCoord();
                v = vis.getSpatialVCoord();
                 */
            } else {
                continue;
            }

            final boolean[][] flags = oiData.getFlag();

            final int nRows = oiData.getNbRows();
            final int nWaves = oiData.getNWave();

            for (int i = 0, j; i < nRows; i++) {
                final boolean[] rowFlags = (flags != null) ? flags[i] : null;

                for (j = 0; j < nWaves; j++) {
                    if ((rowFlags == null) || !rowFlags[j]) {
                        final double uc = u[i][j];
                        final double vc = v[i][j];

                        s11 += 2.0 * (uc * uc);
                        s22 += 2.0 * (vc * vc);
                        s12 += 2.0 * (uc * vc);
                        n++;
                    }
                }
            }
        }
        logger.log(Level.FINE, "n: {0}", n);

        if (n != 0) {
            // normalize by (2n + 1):
            final double invNorm = 1.0 / (2.0 * n + 1.0);

            s11 *= invNorm;
            s22 *= invNorm;
            s12 *= invNorm;

            final double[] covMatrix = {s11, s12, s12, s22};
            return computeBeam(covMatrix);
        }
        return null;
    }

    private static BeamInfo computeBeam(final double[] covMatrix) {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "S | {0} {1} |", new Object[]{covMatrix[0], covMatrix[1]});
            logger.log(Level.FINE, "  | {0} {1} |", new Object[]{covMatrix[2], covMatrix[3]});
        }

        // the input matrix: covariance matrix Gaussian (uv plane)
        final EigenResults results = getEigenValuesAndVectors(covMatrix);

        if (results != null) {
            // variance Gaussian (uv plane):
            logger.log(Level.FINE, "Eigen values: ({0}, {1})", new Object[]{results.ev1, results.ev2});
            logger.log(Level.FINE, "Eigen vectors: ({0}, {1})", new Object[]{Arrays.toString(results.vec1), Arrays.toString(results.vec2)});

            logger.log(Level.FINE, "HW-HM (uv-plane): ({0}, {1})", new Object[]{STDDEV_TO_HWHM * Math.sqrt(results.ev1), STDDEV_TO_HWHM * Math.sqrt(results.ev2)});

            // Principal components:
            final double rx = Math.toDegrees(SCALE_FACTOR / Math.sqrt(results.ev1)) * DEG_IN_MILLI_ARCSEC;
            final double ry = Math.toDegrees(SCALE_FACTOR / Math.sqrt(results.ev2)) * DEG_IN_MILLI_ARCSEC;

            double angle = Math.toDegrees(Math.atan2(results.vec1[0], results.vec1[1]));

            // correct angle in range [-90; 90] as collinearity !
            if (angle < -90.0) {
                angle += 180.0;
            } else if (angle > 90.0) {
                angle -= 180.0;
            }

            logger.log(Level.FINE, "Major-axis hwhm (mas): {0}", rx);
            logger.log(Level.FINE, "Minor-axis hwhm (mas): {0}", ry);
            logger.log(Level.FINE, "Angle (deg):           {0}", angle);

            return new BeamInfo(rx, ry, angle);
        }
        return null;
    }

    public static EigenResults getEigenValuesAndVectors(final double[] matrix) {
        /**
        matrix A given as [a b c d]
        | a b |
        | c d |
         */
        final double a = matrix[0];
        final double b = matrix[1];
        final double c = matrix[2];
        final double d = matrix[3];

        // Av = λv 
        // | (a - λ)    b    | = 0
        // |    c    (d - λ) |
        // det ( A − λI ) = 0 
        // characteristic polynom: λ^2 - (a + d) λ + (ad - bc) = 0 (quadratic polynom)
        // A = 1
        // B = - (a + d)
        // C = (ad - bc)
        // discriminant:
        // D = B*B - 4C = (a - d)^2 + 4 * b * c
        double discriminant = (a - d) * (a - d) + 4.0 * b * c;

        if (discriminant < 0.0) {
            // no real root, only complex ?
            return null;
        }
        // if discriminant = 0: then ev1 = ev2
        discriminant = Math.sqrt(discriminant);

        // smallest first:
        final double ev1 = ((a + d) - discriminant) / 2.0;
        final double ev2 = ((a + d) + discriminant) / 2.0;

        logger.log(Level.FINE, "λ1: {0}", ev1);
        logger.log(Level.FINE, "λ2: {0}", ev2);

        final EigenResults results = new EigenResults();
        results.ev1 = ev1;
        results.ev2 = ev2;

        // Av - λv = 0 gives two equations for eigen vectors (x y):
        // | (a - λ)    b    | * | x | = 0
        // |    c    (d - λ) |   | y |
        // gives:
        // (1) (a - λ) * x + b * y = 0
        // (2) c * x + (d - λ) * y = 0
        double e, x, y, r;
        // (1) (a - λ) * x + b * y = 0
        e = ev1;
        x = b;
        y = e - a;
        r = Math.sqrt(x * x + y * y);
        if (r > 0.0) {
            x /= r;
            y /= r;
        } else {
            // c * x + (d - e) * y == 0
            x = e - d;
            y = c;
            r = Math.sqrt(x * x + y * y);
            if (r > 0.0) {
                x /= r;
                y /= r;
            } else {
                x = 1;
                y = 0;
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "v1: ({0} {1})", new Object[]{x, y});
        }

        if (DO_CHECKS) {
            System.out.println("λ1: " + ev1);
            System.out.println("v1: (" + x + "," + y + ")");
            System.out.println("(a - λ) * x + b * y = " + ((a - e) * x + b * y));
            System.out.println("c * x + (d - λ) * y = " + (c * x + (d - e) * y));
        }

        results.vec1 = new double[]{x, y};

        // (1) (a - λ) * x + b * y = 0
        e = ev2;
        x = b;
        y = e - a;
        r = Math.sqrt(x * x + y * y);
        if (r > 0.0) {
            x /= r;
            y /= r;
        } else {
            x = e - d;
            y = c;
            r = Math.sqrt(x * x + y * y);
            if (r > 0.0) {
                x /= r;
                y /= r;
            } else {
                x = 0;
                y = 1;
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "v2: ({0} {1})", new Object[]{x, y});
        }

        if (DO_CHECKS) {
            System.out.println("λ2: " + ev2);
            System.out.println("v2: (" + x + "," + y + ")");
            System.out.println("(a - λ) * x + b * y = " + ((a - e) * x + b * y));
            System.out.println("c * x + (d - λ) * y = " + (c * x + (d - e) * y));
        }

        results.vec2 = new double[]{x, y};

        return results;
    }

    final static class EigenResults {

        double ev1, ev2;

        double[] vec1, vec2;
    }

    private BeamEstimator() {
        // forbidden constructor
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        try {
            OIFitsFile oiFits = OIFitsLoader.loadOIFits("/home/bourgesl/Documents/vlti2023/data/vlti/jmmctools/ImageReconstruction/data/RCar/R_CAR_all.fits");

            final BeamInfo beamInfo = computeBeamInfo(oiFits.getOiDataList());

            System.out.println(beamInfo);

            showEllipse(beamInfo.rx, beamInfo.ry, beamInfo.angle);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "exception", e);
        }
    }

    private static void showEllipse(final double rx, final double ry, final double angle) {

        final double maxRadius = Math.max(rx, ry);

        final JFrame frame = new JFrame("Ellipse");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(800, 800));

        frame.add(new JPanel() {
            @Override
            public void paint(Graphics g) {
                final Graphics2D g2d = (Graphics2D) g;

                final int w = getWidth();
                final int h = getHeight();

                g2d.setColor(Color.BLACK);
                g2d.drawLine(0, h / 2, w, h / 2);
                g2d.drawLine(w / 2, 0, w / 2, h);

                final double maxDevice = Math.max(w, h);

                final double scale = maxDevice / (4 * maxRadius);

                final Ellipse2D ellipse = new Ellipse2D.Double();

                double wx = scale * maxRadius;
                double wy = scale * maxRadius;

                g2d.translate(w / 2.0, h / 2.0);
                g2d.rotate(Math.toRadians(angle));

                g2d.setColor(Color.GRAY);
                ellipse.setFrame(-wx, -wy, 2.0 * wx, 2.0 * wy);
                g2d.draw(ellipse);

                // inverse orientation to match angle:
                wx = scale * ry; // minor
                wy = scale * rx; // major

                g2d.setStroke(new BasicStroke(4f));

                g2d.setColor(Color.ORANGE);
                ellipse.setFrame(-wx, -wy, 2.0 * wx, 2.0 * wy);
                g2d.draw(ellipse);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

}
