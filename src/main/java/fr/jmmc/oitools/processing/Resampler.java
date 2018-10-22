/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.processing;

import java.util.Arrays;

/*
 * Copyright (c) 2008, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 /*
 *******************************************************************************
 *
 *  Based on example code found in Graphics Gems III, Filtered Image Rescaling
 *  (filter_rcg.c), available from http://www.acm.org/tog/GraphicsGems/.
 *
 *  Public Domain 1991 by Dale Schumacher. Mods by Ray Gardener
 *
 *  Original by Dale Schumacher (fzoom)
 *
 *  Additional changes by Ray Gardener, Daylon Graphics Ltd.
 *  December 4, 1999
 *
 *******************************************************************************
 *
 *  Additional changes inspired by ImageMagick's resize.c.
 *
 *******************************************************************************
 *
 *  Java port and additional changes/bugfixes by Harald Kuhr, Twelvemonkeys.
 *  February 20, 2006
 *
 *******************************************************************************
 */
/**
 * Resamples (scales) a {@code BufferedImage} to a new width and height, using
 * high performance and high quality algorithms.
 * Several different interpolation algorithms may be specifed in the
 * constructor, either using the
 * <a href="#field_summary">filter type constants</a>, or one of the
 * {@code RendereingHints}.
 * 
 * For fastest results, use {@link #FILTER_POINT} or {@link #FILTER_BOX}.
 * In most cases, {@link #FILTER_TRIANGLE} will produce acceptable results, while
 * being relatively fast.
 * For higher quality output, use more sophisticated interpolation algorithms,
 * like {@link #FILTER_MITCHELL} or {@link #FILTER_LANCZOS}.
 * 
 * Example:
 * <blockquote><pre>
 * BufferedImage image;
 * 
 * //...
 * 
 * ResampleOp resampler = new ResampleOp(100, 100, ResampleOp.FILTER_TRIANGLE);
 * BufferedImage thumbnail = resampler.filter(image, null);
 * </pre></blockquote>
 * 
 * If your imput image is very large, it's possible to first resample using the
 * very fast {@code FILTER_POINT} algorithm, then resample to the wanted size,
 * using a higher quality algorithm:
 * <blockquote><pre>
 * BufferedImage verylLarge;
 * 
 * //...
 * 
 * int w = 300;
 * int h = 200;
 * 
 * BufferedImage temp = new ResampleOp(w * 2, h * 2, FILTER_POINT).filter(verylLarge, null);
 * 
 * BufferedImage scaled = new ResampleOp(w, h).filter(temp, null);
 * </pre></blockquote>
 * 
 * For maximum performance, this class will use native code, through
 * <a href="http://www.yeo.id.au/jmagick/">JMagick</a>, when available.
 * Otherwise, the class will silently fall back to pure Java mode.
 * Native code may be disabled globally, by setting the system property
 * {@code com.twelvemonkeys.image.accel} to {@code false}.
 * To allow debug of the native code, set the system property
 * {@code com.twelvemonkeys.image.magick.debug} to {@code true}.
 * 
 * This {@code BufferedImageOp} is based on C example code found in
 * <a href="http://www.acm.org/tog/GraphicsGems/">Graphics Gems III</a>,
 * Filtered Image Rescaling, by Dale Schumacher (with additional improvments by
 * Ray Gardener).
 * Additional changes are inspired by
 * <a href="http://www.imagemagick.org/">ImageMagick</a> and
 * Marco Schmidt's <a href="http://schmidt.devlib.org/jiu/">Java Imaging Utilities</a>
 * (which are also adaptions of the same original code from Graphics Gems III).
 * 
 * For a description of the various interpolation algorithms, see
 * <em>General Filtered Image Rescaling</em> in <em>Graphics Gems III</em>,
 * Academic Press, 1994.
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 */
public final class Resampler {

    public static final Filter FILTER_DEFAULT = Filter.FILTER_MITCHELL;

    public enum Filter {
        /**
         * Point interpolation (also known as "nearest neighbour").
         * Very fast, but low quality
         */
        FILTER_POINT("Point"),
        /**
         * Box interpolation. Fast, but low quality.
         */
        FILTER_BOX("Box"),
        /**
         * Triangle interpolation (also known as "linear" or "bilinear").
         * Quite fast, with acceptable quality
         */
        FILTER_TRIANGLE("Triangle"),
        /**
         * Hermite interpolation.
         */
        FILTER_HERMITE("Hermite"),
        /**
         * Hanning interpolation.
         */
        FILTER_HANNING("Hanning"),
        /**
         * Hamming interpolation.
         */
        FILTER_HAMMING("Hamming"),
        /**
         * Blackman interpolation..
         */
        FILTER_BLACKMAN("Blackman"),
        /**
         * Gaussian interpolation.
         */
        FILTER_GAUSSIAN("Gaussian"),
        /**
         * Quadratic interpolation.
         */
        FILTER_QUADRATIC("Quadratic"),
        /**
         * Cubic interpolation.
         */
        FILTER_CUBIC("Cubic"),
        /**
         * Catrom interpolation.
         */
        FILTER_CATROM("Catrom"),
        /**
         * Mitchell interpolation. High quality.
         * IM default scale with palette or alpha, or scale up
         */
        FILTER_MITCHELL("Mitchell"),
        /**
         * Lanczos interpolation. High quality.
         * IM default
         */
        FILTER_LANCZOS("Lanczos"),
        /**
         * Blackman-Bessel interpolation. High quality.
         */
        FILTER_BLACKMAN_BESSEL("Blackman Bessel"),
        /**
         * Blackman-Sinc interpolation. High quality.
         */
        FILTER_BLACKMAN_SINC("Blackman Sinc");

        // member
        private final String name;

        Filter(final String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Re-samples (scales) the image to the size, and using the algorithm
     * specified in the constructor.
     *
     * @param pSource  The image (double[][]) to be filtered
     * @param pDest    The re-sampled image (double[][]) to fill (width x height)
     * @param filter interpolation filter algorithm
     * @return The re-sampled image (double[][]).
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public static final double[][] filter(final double[][] pSource, final double[][] pDest, final Filter filter, final boolean positive) {
        if (pSource == null) {
            throw new NullPointerException("Input == null");
        }
        if (pDest == null) {
            throw new NullPointerException("Dest == null");
        }
        if (pSource == pDest) {
            throw new IllegalStateException("Input == Dest");
        }

        final InterpolationFilter filterClass = createFilter(filter);

        resample(pSource, pDest, filterClass, positive);

        return pDest;
    }

    private static InterpolationFilter createFilter(Filter filter) {
        switch (filter) {
            case FILTER_POINT:
                return new PointFilter();
            case FILTER_BOX:
                return new BoxFilter();
            case FILTER_TRIANGLE:
                return new TriangleFilter();
            case FILTER_HERMITE:
                return new HermiteFilter();
            case FILTER_HANNING:
                return new HanningFilter();
            case FILTER_HAMMING:
                return new HammingFilter();
            case FILTER_BLACKMAN:
                return new BlacmanFilter();
            case FILTER_GAUSSIAN:
                return new GaussianFilter();
            case FILTER_QUADRATIC:
                return new QuadraticFilter();
            case FILTER_CUBIC:
                return new CubicFilter();
            case FILTER_CATROM:
                return new CatromFilter();
            case FILTER_MITCHELL:
                return new MitchellFilter();
            case FILTER_LANCZOS:
                return new LanczosFilter();
            case FILTER_BLACKMAN_BESSEL:
                return new BlackmanBesselFilter();
            case FILTER_BLACKMAN_SINC:
                return new BlackmanSincFilter();
            default:
                throw new IllegalStateException("Unknown filter: " + filter);
        }
    }

    /*
     *	filter function definitions
     */
    interface InterpolationFilter {

        double filter(double t);

        double support();
    }

    static final class HermiteFilter implements InterpolationFilter {

        public final double filter(double t) {
            /* f(t) = 2|t|^3 - 3|t|^2 + 1, -1 <= t <= 1 */
            if (t < 0.0) {
                t = -t;
            }
            if (t < 1.0) {
                return (2.0 * t - 3.0) * t * t + 1.0;
            }
            return 0.0;
        }

        public final double support() {
            return 1.0;
        }
    }

    static final class PointFilter extends BoxFilter {

        public PointFilter() {
            super(0.0);
        }
    }

    static class BoxFilter implements InterpolationFilter {

        private final double mSupport;

        public BoxFilter() {
            mSupport = 0.5;
        }

        protected BoxFilter(double pSupport) {
            mSupport = pSupport;
        }

        public final double filter(final double t) {
            if ((t >= -0.5) && (t < 0.5)) {
                return 1.0;
            }
            return 0.0;
        }

        public final double support() {
            return mSupport;
        }
    }

    static final class TriangleFilter implements InterpolationFilter {

        public final double filter(double t) {
            if (t < 0.0) {
                t = -t;
            }
            if (t < 1.0) {
                return 1.0 - t;
            }
            return 0.0;
        }

        public final double support() {
            return 1.0;
        }
    }

    static final class QuadraticFilter implements InterpolationFilter {

        // AKA Bell
        public final double filter(double t)/* box (*) box (*) box */ {
            if (t < 0) {
                t = -t;
            }
            if (t < 0.5) {
                return 0.75 - (t * t);
            }
            if (t < 1.5) {
                t = (t - 1.5);
                return 0.5 * (t * t);
            }
            return 0.0;
        }

        public final double support() {
            return 1.5;
        }
    }

    static final class CubicFilter implements InterpolationFilter {

        // AKA B-Spline
        public final double filter(double t)/* box (*) box (*) box (*) box */ {
            final double tt;

            if (t < 0) {
                t = -t;
            }
            if (t < 1) {
                tt = t * t;
                return (0.5 * tt * t) - tt + (2.0 / 3.0);
            } else if (t < 2) {
                t = 2 - t;
                return (1.0 / 6.0) * (t * t * t);
            }
            return 0.0;
        }

        public final double support() {
            return 2.0;
        }
    }

    private static double sinc(double x) {
        x *= Math.PI;
        if (x != 0.0) {
            return Math.sin(x) / x;
        }
        return 1.0;
    }

    static final class LanczosFilter implements InterpolationFilter {

        // AKA Lanczos3
        public final double filter(double t) {
            if (t < 0) {
                t = -t;
            }
            if (t < 3.0) {
                return sinc(t) * sinc(t / 3.0);
            }
            return 0.0;
        }

        public final double support() {
            return 3.0;
        }
    }

    private final static double B = 1.0 / 3.0;
    private final static double C = 1.0 / 3.0;
    private final static double P0 = (6.0 - 2.0 * B) / 6.0;
    private final static double P2 = (-18.0 + 12.0 * B + 6.0 * C) / 6.0;
    private final static double P3 = (12.0 - 9.0 * B - 6.0 * C) / 6.0;
    private final static double Q0 = (8.0 * B + 24.0 * C) / 6.0;
    private final static double Q1 = (-12.0 * B - 48.0 * C) / 6.0;
    private final static double Q2 = (6.0 * B + 30.0 * C) / 6.0;
    private final static double Q3 = (-1.0 * B - 6.0 * C) / 6.0;

    static final class MitchellFilter implements InterpolationFilter {

        public final double filter(double t) {
            if (t < -2.0) {
                return 0.0;
            }
            if (t < -1.0) {
                return Q0 - t * (Q1 - t * (Q2 - t * Q3));
            }
            if (t < 0.0) {
                return P0 + t * t * (P2 - t * P3);
            }
            if (t < 1.0) {
                return P0 + t * t * (P2 + t * P3);
            }
            if (t < 2.0) {
                return Q0 + t * (Q1 + t * (Q2 + t * Q3));
            }
            return 0.0;
        }

        public final double support() {
            return 2.0;
        }
    }

    private static double j1(final double t) {
        final double[] pOne = {
            0.581199354001606143928050809e+21,
            -0.6672106568924916298020941484e+20,
            0.2316433580634002297931815435e+19,
            -0.3588817569910106050743641413e+17,
            0.2908795263834775409737601689e+15,
            -0.1322983480332126453125473247e+13,
            0.3413234182301700539091292655e+10,
            -0.4695753530642995859767162166e+7,
            0.270112271089232341485679099e+4
        };
        final double[] qOne = {
            0.11623987080032122878585294e+22,
            0.1185770712190320999837113348e+20,
            0.6092061398917521746105196863e+17,
            0.2081661221307607351240184229e+15,
            0.5243710262167649715406728642e+12,
            0.1013863514358673989967045588e+10,
            0.1501793594998585505921097578e+7,
            0.1606931573481487801970916749e+4,
            0.1e+1
        };

        double p = pOne[8];
        double q = qOne[8];
        for (int i = 7; i >= 0; i--) {
            p = p * t * t + pOne[i];
            q = q * t * t + qOne[i];
        }
        return p / q;
    }

    private static double p1(final double t) {
        final double[] pOne = {
            0.352246649133679798341724373e+5,
            0.62758845247161281269005675e+5,
            0.313539631109159574238669888e+5,
            0.49854832060594338434500455e+4,
            0.2111529182853962382105718e+3,
            0.12571716929145341558495e+1
        };
        final double[] qOne = {
            0.352246649133679798068390431e+5,
            0.626943469593560511888833731e+5,
            0.312404063819041039923015703e+5,
            0.4930396490181088979386097e+4,
            0.2030775189134759322293574e+3,
            0.1e+1
        };

        double p = pOne[5];
        double q = qOne[5];
        for (int i = 4; i >= 0; i--) {
            p = p * (8.0 / t) * (8.0 / t) + pOne[i];
            q = q * (8.0 / t) * (8.0 / t) + qOne[i];
        }
        return p / q;
    }

    private static double q1(final double t) {
        final double[] pOne = {
            0.3511751914303552822533318e+3,
            0.7210391804904475039280863e+3,
            0.4259873011654442389886993e+3,
            0.831898957673850827325226e+2,
            0.45681716295512267064405e+1,
            0.3532840052740123642735e-1
        };
        final double[] qOne = {
            0.74917374171809127714519505e+4,
            0.154141773392650970499848051e+5,
            0.91522317015169922705904727e+4,
            0.18111867005523513506724158e+4,
            0.1038187585462133728776636e+3,
            0.1e+1
        };

        double p = pOne[5];
        double q = qOne[5];
        for (int i = 4; i >= 0; i--) {
            p = p * (8.0 / t) * (8.0 / t) + pOne[i];
            q = q * (8.0 / t) * (8.0 / t) + qOne[i];
        }
        return p / q;
    }

    static double besselOrderOne(double t) {
        double p, q;

        if (t == 0.0) {
            return 0.0;
        }
        p = t;
        if (t < 0.0) {
            t = -t;
        }
        if (t < 8.0) {
            return p * j1(t);
        }
        q = Math.sqrt(2.0 / (Math.PI * t)) * (p1(t) * (1.0 / Math.sqrt(2.0) * (Math.sin(t) - Math.cos(t))) - 8.0 / t * q1(t)
                * (-1.0 / Math.sqrt(2.0) * (Math.sin(t) + Math.cos(t))));
        if (p < 0.0) {
            q = -q;
        }
        return q;
    }

    private static double bessel(final double t) {
        if (t == 0.0) {
            return Math.PI / 4.0;
        }
        return besselOrderOne(Math.PI * t) / (2.0 * t);
    }

    private static double blackman(final double t) {
        return 0.42 + 0.50 * Math.cos(Math.PI * t) + 0.08 * Math.cos(2.0 * (Math.PI * t));
    }

    static final class BlacmanFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return blackman(t);
        }

        public final double support() {
            return 1.0;
        }
    }

    static final class CatromFilter implements InterpolationFilter {

        public final double filter(double t) {
            if (t < 0) {
                t = -t;
            }
            if (t < 1.0) {
                return 0.5 * (2.0 + t * t * (-5.0 + t * 3.0));
            }
            if (t < 2.0) {
                return 0.5 * (4.0 + t * (-8.0 + t * (5.0 - t)));
            }
            return 0.0;
        }

        public final double support() {
            return 2.0;
        }
    }

    static final class GaussianFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return Math.exp(-2.0 * t * t) * Math.sqrt(2.0 / Math.PI);
        }

        public final double support() {
            return 1.25;
        }
    }

    static final class HanningFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return 0.5 + 0.5 * Math.cos(Math.PI * t);
        }

        public final double support() {
            return 1.0;
        }
    }

    static final class HammingFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return 0.54 + 0.46 * Math.cos(Math.PI * t);
        }

        public final double support() {
            return 1.0;
        }
    }

    static final class BlackmanBesselFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return blackman(t / 3.2383) * bessel(t);
        }

        public final double support() {
            return 3.2383;
        }
    }

    static final class BlackmanSincFilter implements InterpolationFilter {

        public final double filter(final double t) {
            return blackman(t / 4.0) * sinc(t);
        }

        public final double support() {
            return 4.0;
        }
    }

    /*
     *	image rescaling routine
     */
    static final class ContributorList {

        /* number of contributors (may be < p.length) */
        int n;
        /* list of contributions */
        final int[] pixels;
        final double[] weights;

        ContributorList(final int len) {
            pixels = new int[len];
            weights = new double[len];
        }

        void add(final int pixel, final double weight) {
            final int i = n++;
            pixels[i] = pixel;
            weights[i] = weight;
        }

        public String toString() {
            return "ContributorList[" + n + "]:\n" + Arrays.toString(pixels) + "\n" + Arrays.toString(weights);
        }
    }

    /*
        calcXContrib()
        Calculates the filter weights for a single target dimension.
     */
    private static ContributorList calcContribs(final double scale, final int srcLen, final InterpolationFilter pFilter, final int i) {
        // TODO: What to do when fwidth > srcWidth or dstWidth
        final int srcMax = srcLen - 1;

        double width;
        double fscale;
        double center;
        double weight;
        int left, right;

        final ContributorList contribs;

        final double fwidth = pFilter.support();

        if (scale < 1.0) {
            /* Shrinking image */
            width = fwidth / scale;

            if (width <= 0.5) {
                // Reduce to point sampling.
                width = 0.5 + 1.0e-6;
                fscale = 1.0;
            } else {
                fscale = scale;
            }
        } else {
            /* Expanding image */
            width = fwidth;
            fscale = 1.0;
        }

        center = (i) / scale; // centerOffset = 0.5 / scale ?
        left = (int) Math.ceil(center - width);
        right = (int) Math.floor(center + width);

        contribs = new ContributorList(right - left + 1);

        double density = 0.0;

        for (int j = left, n; j <= right; j++) {
            weight = pFilter.filter((center - j) * fscale) * fscale;

            if (j < 0) {
                n = -j;
            } else if (j >= srcLen) {
                n = (srcLen - j) + srcMax;
            } else {
                n = j;
            }

            if (n >= srcLen) {
                n = n % srcLen;
            } else if (n < 0) {
                n = srcMax;
            }

            contribs.add(n, weight);
            density += weight;
        }

        if ((density != 0.0) && (density != 1.0)) {
            // Normalize:
            density = 1.0 / density;
            for (int k = 0; k < contribs.n; k++) {
                contribs.weights[k] *= density;
            }
        }

//        System.out.println(contribs.toString());
        return contribs;
    }

    /*
        resample()

        Resizes bitmaps while resampling them.
     */
    private static double[][] resample(final double[][] pSource, final double[][] pDest, final InterpolationFilter pFilter, final boolean positive) {
        final int dstWidth = pDest[0].length;
        final int dstHeight = pDest.length;

        final int srcWidth = pSource[0].length;
        final int srcHeight = pSource.length;

        final double xscale = (double) dstWidth / (double) srcWidth;
        final double yscale = (double) dstHeight / (double) srcHeight;

        // TODO: What to do when fwidth > srcHeight or dstHeight
        final double fwidth = pFilter.support();

        if (fwidth > srcWidth || fwidth > srcHeight) {
            throw new IllegalStateException("Input image too small for the given filter: " + pFilter.getClass().getSimpleName());
        }

        // Contribs on the X axis:
        final ContributorList[] contribsX = new ContributorList[dstWidth];

        for (int x = 0; x < dstWidth; x++) {
            contribsX[x] = calcContribs(xscale, srcWidth, pFilter, x);
        }

        // Contribs on the Y axis:
        final ContributorList[] contribsY;
        if ((xscale == yscale) && (srcWidth == srcHeight)) {
            contribsY = contribsX;
        } else {
            contribsY = new ContributorList[dstHeight];

            for (int y = 0; y < dstHeight; y++) {
                contribsY[y] = calcContribs(yscale, srcHeight, pFilter, y);
            }
        }

        // TODO: parallelize loops:
        final double[] work = new double[srcHeight];
        double[] row;
        double weight;
        ContributorList contrib;

        for (int x = 0; x < dstWidth; x++) {
            contrib = contribsX[x];

            /* Apply horiz filter to make dst column in work. */
            for (int k = 0; k < srcHeight; k++) {
                row = pSource[k];
                weight = 0.0;

                for (int j = 0; j < contrib.n; j++) {
                    weight += row[contrib.pixels[j]] * contrib.weights[j];
                }
                work[k] = weight;
            }

            /* The temp column has been built. Now stretch it vertically into dst column. */
            for (int y = 0; y < dstHeight; y++) {
                contrib = contribsY[y];
                weight = 0.0;

                for (int j = 0; j < contrib.n; j++) {
                    weight += work[contrib.pixels[j]] * contrib.weights[j];
                }
                if (positive) {
                    pDest[y][x] = (weight > 0.0) ? weight : 0.0;
                } else {
                    pDest[y][x] = weight;
                }
            }
        }
        return pDest;
    }
}
