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
package fr.jmmc.oitools.image;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import fr.jmmc.oitools.util.ArrayConvert;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.FitsUtil;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.fits.ImageData;
import fr.nom.tam.fits.ImageHDU;
import fr.nom.tam.fits.PaddingException;
import fr.nom.tam.util.ArrayDataInput;
import fr.nom.tam.util.RandomAccess;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This stateless class loads is an Fits image and cube into the FitsImageFile structure
 *
 * From ASPRO1:
 *  -------About the FITS file Format ------------------------------
 * First, the FITS file should describe a flux distribution on
 * sky, so the 2 first axes are in offset in RADIANS on the sky. The
 * following is an example of a typical header:
 *
 * NAXIS   =                    2 /2 minimum!
 * NAXIS1  =                  512 /size 1st axis (for example)
 * NAXIS2  =                  512 /size 2nd axis
 * CRVAL1  =  0.0000000000000E+00 / center is at 0
 * CRPIX1  =  0.2560000000000E+03 / reference pixel is 256 in Alpha
 * CDELT1  = -0.4848136811095E-10 / increment is 0.1 milliseconds (radians),
 * / and 'astronomy oriented' (i.e.
 * / RA decreases with pixel number)
 * CRVAL2  =  0.0000000000000E+00 / center is at 0
 * CRPIX2  =  0.2560000000000E+03 / reference pixel is 256 in Delta
 * CDELT2  =  0.4848136811095E-10 / increment is 0.1 milliseconds.
 *
 * The position of the "keyword = value / comment" fields is FIXED by
 * the fits norm. In doubt, see http://www.cv.nrao.edu/fits/aah2901.pdf
 *
 * Axes increments map pixel images to RA and DEC sky coordinates, which
 * are positive to the West and North (and position angles are counted
 * West of North).
 *
 * For single (monochromatic) images (NAXIS = 2), ASPRO assume that this
 * image is observed at the current observing wavelength, usually the
 * mean wavelength of the current instrument setup. ASPRO has a support
 * for polychromatic images, as FITS cubes, with a few more keywords
 * (see below), in which case the used wavelengths will be those defined
 * in the FITS file, not those of the current instrument/interferometer.
 *
 * The file may be a data-cube (N images at different wavelengths) in
 * which case the 3rd axis must be sampled evenly. In the absence of
 * further keywords (see below), it will be assumed that th 3rd axis
 * is in MICRONS as in:
 *
 * NAXIS   =                    3 /data-cube
 * NAXIS1  =                  512 /size 1st axis (for example)
 * NAXIS2  =                  512 /size 2nd axis
 * NAXIS3  =                   32 /size 2nd axis
 * CRVAL1  =  0                   / center is at 0 RA offset on sky
 * CRPIX1  =  256                 / reference pixel is 256 in Alpha
 * CDELT1  = -0.4848136811095E-10 / increment is -0.1 milliseconds (radians),
 * CRVAL2  =  0                   / center is at 0 DEC offset on sky
 * CRPIX2  =  256                 / reference pixel is 256 in Delta
 * CDELT2  =  0.4848136811095E-10 / increment is 0.1 milliseconds.
 * CRPIX3  =  1                   / reference pixel(channel) is 1 on 3rd axis
 * CRVAL3  =  2.2                 / 2.2 microns for this pix/channel
 * CDELT3  =  0.01                / microns channel width
 *
 * However, the additional presence of CTYPE3 can affect
 * the 3rd axis definition:
 * CTYPE3  = 'FREQUENCY'          / means that Cxxxx3 are in Hz
 * or
 * CTYPE3  = 'WAVELENGTH'         / means that Cxxxx3 are in Microns
 *
 * Added support for CUNITn keywords (aspro_keywords.fits):
 * CRPIX1  = '128.500000'
 * CDELT1  = '0.000750'
 * CRVAL1  = '0.000375'
 * CUNIT1  = '  ARCSEC'
 * CRPIX2  = '128.500000'
 * CDELT2  = '0.000750'
 * CRVAL2  = '0.000375'
 * CUNIT2  = '  ARCSEC'
 * CRPIX3  = '1.000000'
 * CDELT3  = '0.000000'
 * CRVAL3  = '1.600000'
 * CUNIT3  = '  MICRON'
 * BUNIT   = 'JY/PIXEL'
 * BTYPE   = 'INTENSITY'
 *
 * @author bourgesl
 */
public final class FitsImageLoader {

    /* constants */
    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImageLoader.class.getName());
    /** undefined image index */
    private final static int UNDEFINED_INDEX = -1;

    static {
        FitsUtils.setup();
    }

    /**
     * Private constructor
     */
    private FitsImageLoader() {
        super();
    }

    /**
     * Load the given file and return a FitsImageFile structure
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @param firstOnly load only the first valid Image HDU
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @return FitsImageFile structure on success
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    public static FitsImageFile load(final String absFilePath, final boolean firstOnly) throws FitsException, IOException, IllegalArgumentException {
        return load(absFilePath, firstOnly, false);
    }

    /**
     * Load the given file and return a FitsImageFile structure
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @param firstOnly load only the first valid Image HDU
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     * @return FitsImageFile structure on success
     */
    public static FitsImageFile load(final String absFilePath, final boolean firstOnly, final boolean requireCdeltKeywords) throws FitsException, IOException, IllegalArgumentException {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "loading {0}", absFilePath);
        }

        // Check if the given file exists :
        if (!new File(absFilePath).exists()) {
            throw new IOException("File not found: " + absFilePath);
        }

        Fits fitsFile = null;
        try {
            // create new Fits image structure:
            final FitsImageFile imgFitsFile = new FitsImageFile(absFilePath);

            final long start = System.nanoTime();

            // open the fits file :
            fitsFile = new Fits(absFilePath);

            // read the complete file structure :
            // TODO: unify the readHDU with OIFitsLoader
            final List<BasicHDU> hduList = read(fitsFile);

            // processHDUnit all HD units :
            if (!hduList.isEmpty()) {
                processHDUnits(imgFitsFile, hduList, firstOnly, requireCdeltKeywords);
            }

            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "load: duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
            }

            return imgFitsFile;

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to load the file: " + absFilePath, fe);
            throw fe;
        } finally {
            if (fitsFile != null && fitsFile.getStream() != null) {
                try {
                    fitsFile.getStream().close();
                } catch (IOException ioe) {
                    logger.log(Level.FINE, "Closing Fits file", ioe);
                }
            }
        }
    }

    /**
     * Update the checksum keyword for the given HDU
     * @param hdu hdu to processHDUnit
     * @return checksum value
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    public static long updateChecksum(final BasicHDU hdu) throws FitsException, IOException {
        // compute and add checksum into HDU (header):
        return Fits.setChecksum(hdu, false);
    }

    /**
     * Return all image HDUs read from the given Fits object
     * @param fitsFile Fits object to read
     * @return list of ImageHDU
     * @throws FitsException if any fits or IO exception occurs
     */
    private static List<BasicHDU> read(final Fits fitsFile) throws FitsException {
        final List<BasicHDU> hduList = new LinkedList<BasicHDU>();

        try {
            while (fitsFile.getStream() != null) {
                final BasicHDU hdu = readHDU(fitsFile);
                if (hdu == null) {
                    break;
                }
                hduList.add(hdu);
            }
        } catch (IOException e) {
            throw new FitsException("IO error: " + e);
        }
        return hduList;
    }

    /**
     * Read the next HDU on the default input stream.
     * Note: it skips truncated HDU
     *
     * @param fitsFile Fits object to read
     * @return The HDU read, or null if an EOF was detected.
     * Note that null is only returned when the EOF is detected immediately
     * at the beginning of reading the HDU.
     *
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private static BasicHDU readHDU(final Fits fitsFile) throws FitsException, IOException {

        final ArrayDataInput dataStr = fitsFile.getStream();
        if (dataStr == null || fitsFile.isAtEOF()) {
            return null;
        }

        if (dataStr instanceof RandomAccess && fitsFile.getLastFileOffset() > 0) {
            FitsUtil.reposition(dataStr, fitsFile.getLastFileOffset());
        }

        final Header hdr = Header.readHeader(dataStr);
        if (hdr == null) {
            fitsFile.setAtEOF(true);
            return null;
        }

        if (ImageHDU.isHeader(hdr)) {
            // Hack for ImageHDU having NAXIS > 2 and NAXISn=1
            fixAxesInHeader(hdr);
        }

        final Data datum = hdr.makeData();
        try {
            datum.read(dataStr);
        } catch (PaddingException pe) {
            // ignore truncated HDU ...
            fitsFile.setAtEOF(true);
            return null;
        }

        fitsFile.setLastFileOffset(FitsUtil.findOffset(dataStr));

        return FitsFactory.HDUFactory(hdr, datum);
    }

    /**
     * Fix header for degenerated AXES (ie NAXISn = 1) ie remove such axes
     * to get data arrays having less dimensions
     * @param hdr fits header
     * @throws FitsException if any IO / Fits exception occurs
     */
    private static void fixAxesInHeader(final Header hdr) throws FitsException {
        final int nAxis = hdr.getIntValue(FitsConstants.KEYWORD_NAXIS, 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > 999) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }

        if (nAxis == 0) {
            return;
        }

        final int[] axes = new int[nAxis];
        for (int i = 1; i <= nAxis; i++) {
            axes[i - 1] = hdr.getIntValue(FitsConstants.KEYWORD_NAXIS + i, 0);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, FitsConstants.KEYWORD_NAXIS + "{0} = {1}", new Object[]{i, axes[i - 1]});
            }
        }

        int newNAxis = 0;
        // Find axes with NAxisn != 1
        for (int i = nAxis - 1; i >= 0; i--) {
            if (axes[i] <= 1) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "remove NAXIS{0}{1}", new Object[]{i, 1});
                }
                hdr.removeCard(FitsConstants.KEYWORD_NAXIS + (i + 1));
            } else {
                newNAxis++;
            }
        }

        // Update NAXIS:
        if (newNAxis != nAxis) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "updated NAXIS = {0}", newNAxis);
            }
            hdr.setNaxes(newNAxis);
        }
    }

    /**
     * Process all Fits HD units to load Fits images (skip other HDU) into the given FitsImageFile structure
     * @param imgFitsFile FitsImageFile structure to use
     * @param hdus list of HD units
     * @param firstOnly load only the first valid Image HDU
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    private static void processHDUnits(final FitsImageFile imgFitsFile, final List<BasicHDU> hdus,
                                       final boolean firstOnly, final boolean requireCdeltKeywords) throws FitsException, IOException, IllegalArgumentException {

        imgFitsFile.getFitsImageHDUs().addAll(processHDUnits(imgFitsFile.getFileName(), hdus, firstOnly, requireCdeltKeywords));
    }

    /**
     * Process all Fits HD units and build a FitsImageHDU list (skip other HDU).
     * @param filename name of related fits file
     * @param hdus list of HD units
     * @param firstOnly load only the first valid Image HDU
     * @return List(FitsImageHDU) is list for all imageHDU find
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    public static List<FitsImageHDU> processHDUnits(final String filename, final Collection<BasicHDU> hdus, final boolean firstOnly) throws FitsException, IOException, IllegalArgumentException {
        return processHDUnits(filename, hdus, firstOnly, false);
    }

    /**
     * Process all Fits HD units and build a FitsImageHDU list (skip other HDU).
     * @param filename name of related fits file
     * @param hdus list of HD units
     * @param firstOnly load only the first valid Image HDU
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @return List(FitsImageHDU) is list for all imageHDU find
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    public static List<FitsImageHDU> processHDUnits(final String filename, final Collection<BasicHDU> hdus,
                                                    final boolean firstOnly, final boolean requireCdeltKeywords) throws FitsException, IOException, IllegalArgumentException {

        final int nbHDU = hdus.size();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "processHDUnits: number of HDU = {0}", nbHDU);
        }

        final List<FitsImageHDU> fitsImageHDUs = new LinkedList<FitsImageHDU>();

        // start from Primary HDU
        int i = 0;
        for (BasicHDU hdu : hdus) {

            if (hdu instanceof ImageHDU) {
                final ImageHDU imgHdu = (ImageHDU) hdu;

                final FitsImageHDU imageHDU = processHDUnit(null, filename, imgHdu, requireCdeltKeywords, i, FitsImageHDUFactory.DEFAULT_FACTORY);

                // TODO: imageHDU should be returned even if no image to hold keywords
                //          -> could rely on the firstOnly flag ?
                // Finish ImageHDU:
                if (imageHDU.hasImages()) {
                    // register the image HDU:
                    fitsImageHDUs.add(imageHDU);

                    if (firstOnly) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("First HDU loaded; skipping other HDUs ...");
                        }
                        break;
                    }
                }

            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Skipped {0}#{1} [{2}]", new Object[]{hdu.getClass().getSimpleName(), i, filename});
                }
            }

            // increment i:
            i++;
        }

        return fitsImageHDUs;
    }

    /**
     * Process all Fits HD units and build a FitsImageHDU list.
     * @param checker OIFitsChecker
     * @param filename name of related fits file
     * @param imgHdu Fits image
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @param hduIndex load only the first valid Image HDU
     * @param factory Create an object FitsImageHDU
     * @return FitsImageHDU is the imageHDU find
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    public static FitsImageHDU processHDUnit(final OIFitsChecker checker, final String filename,
                                             final ImageHDU imgHdu, final boolean requireCdeltKeywords,
                                             final int hduIndex, final FitsImageHDUFactory factory) throws FitsException, IOException {

        final int nAxis = getNAxis(imgHdu);

        final int imgCount;

        if (nAxis > 3) {
            logger.log(Level.INFO, "Skipped image in ImageHDU#{0} [{1}] - Unsupported NAXIS = {2}", new Object[]{hduIndex, filename, nAxis});

            // Just parse header:
            imgCount = 0;

        } else if (nAxis == 3) {
            // Fits cube:
            final int nAxis3 = imgHdu.getHeader().getIntValue(FitsConstants.KEYWORD_NAXIS3);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "FITS CUBE ImageHDU#{0} [{1}] - NAXIS3 = {2}", new Object[]{hduIndex, filename, nAxis3});
            }

            // multiple images:
            imgCount = nAxis3;

        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "FITS IMAGE ImageHDU#{0} [{1}]", new Object[]{hduIndex, filename});
            }

            // single image:
            imgCount = 1;
        }

        // Load images:
        final FitsImageHDU imageHDU = createImageHDU(checker, filename, imgHdu, requireCdeltKeywords, hduIndex, factory, imgCount);

        if (imageHDU.getImageCount() != 0) {
            // update checksum:
            imageHDU.setChecksum(updateChecksum(imgHdu));
        }
        return imageHDU;
    }

    /**
     * Process all Fits HD units and build a FitsImageHDU list.
     * @param checker OIFitsChecker
     * @param filename name of related fits file
     * @param imgHdu Fits image
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @param hduIndex load only the first valid Image HDU
     * @param factory Create an object FitsImageHDU
     * @param imgCount nb of image
     * @return FitsImageHDU is the imageHDU find
     * @throws FitsException if any FITS error occurred
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    private static FitsImageHDU createImageHDU(final OIFitsChecker checker, final String filename,
                                               final ImageHDU imgHdu, final boolean requireCdeltKeywords, final int hduIndex,
                                               final FitsImageHDUFactory factory, final int imgCount) throws FitsException {

        // Create Image HDU:
        final FitsImageHDU imageHDU = factory.create();
        imageHDU.setExtNb(hduIndex);

        processKeywords(checker, imgHdu.getHeader(), imageHDU);

        // load all images in fits cube:
        for (int imageIndex = 1; imageIndex <= imgCount; imageIndex++) {
            final FitsImage image = new FitsImage();
            // define image HDU:
            image.setFitsImageHDU(imageHDU);

            // define the fits image identifier:
            image.setFitsImageIdentifier(filename + '#' + hduIndex + '-' + imageIndex + '/' + imgCount);
            image.setImageIndex(imageIndex);

            // load image:
            //if nb image > 1 we don't have a index
            processImage(imgHdu, image, (imgCount != 1) ? imageIndex : UNDEFINED_INDEX, requireCdeltKeywords);

            // skip empty images:
            if (image.getNbRows() <= 0 || image.getNbCols() <= 0) {
                logger.log(Level.INFO, "Skipped image in ImageHDU#{0} [{1}][{2}/{3}] - Incorrect size = {4} x {5}", new Object[]{hduIndex, filename, imageIndex, imgCount, image.getNbCols(), image.getNbRows()});
            } else {
                // register the image :
                imageHDU.getFitsImages().add(image);
            }
        }
        return imageHDU;
    }

    /**
     * Return the NAXIS keyword value
     * @param imgHdu image HDU
     * @return NAXIS keyword value
     * @throws FitsException if NAXIS < 0 or > 999
     */
    private static int getNAxis(final ImageHDU imgHdu) throws FitsException {
        final int nAxis = imgHdu.getHeader().getIntValue(FitsConstants.KEYWORD_NAXIS, 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > 999) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }
        return nAxis;
    }

    /**
     * Process a given Fits image to fill the given FitsImage object with header and image data
     * @param imgHdu image HDU
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @throws FitsException if any FITS error occurred
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    private static void processImage(final ImageHDU imgHdu, final FitsImage image, final int imageIndex, final boolean requireCdeltKeywords) throws FitsException, IllegalArgumentException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "processImage: {0}", image);
        }

        processImageKeywords(imgHdu.getHeader(), image, imageIndex, requireCdeltKeywords);

        processData(imgHdu, image, imageIndex);
    }

    /**
     * Process the binary table header to get keywords used by the OITable (see keyword descriptors)
     * and check missing keywords and their formats
     * @param checker
     * @param header binary table header
     * @param hduFits OI table
     * @throws FitsException if any FITS error occurred
     */
    public static void processKeywords(final OIFitsChecker checker, final Header header, final FitsHDU hduFits) throws FitsException {
        // Note: a fits keyword has a KEY, VALUE AND COMMENT

        // Get Keyword descriptors:
        final Collection<KeywordMeta> keywordsDescCollection = hduFits.getKeywordDescCollection();

        // Dump table descriptors:
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("table keywords:");
            for (KeywordMeta keyword : keywordsDescCollection) {
                logger.finest(keyword.toString());
            }
        }

        for (KeywordMeta keyword : keywordsDescCollection) {
            final String keywordName = keyword.getName();

            final boolean hasKeyword = header.containsKey(keywordName);

            // check mandatory keywords:
            if (!hasKeyword || OIFitsChecker.isInspectRules()) {
                if (!keyword.isOptional()) {
                    /* No keyword with keywordName name */
                    if (checker != null) {
                        // rule [GENERIC_KEYWORD_MANDATORY] check if the required keyword is present
                        checker.ruleFailed(Rule.GENERIC_KEYWORD_MANDATORY, hduFits, keywordName);
                    }
                }
            }
            if (hasKeyword || OIFitsChecker.isInspectRules()) {
                // parse keyword value:
                final Object keywordValue = parseKeyword(checker, hduFits, keyword, header.getValue(keywordName));

                // store key and value:
                // potentially missing values
                if (keywordValue != null) {
                    hduFits.setKeywordValue(keywordName, keywordValue);
                }
            }
        }

        // Copy all header cards:
        loadHeaderCards(header, hduFits);
    }

    /**
     * Parse the keyword value and check its format
     * @param checker OIFitsChecker (optional)
     * @param keyword keyword descriptor
     * @param keywordValue keyword raw string value (maybe null)
     * @param hduFits HDU
     * @return converted keyword value or null
     */
    private static Object parseKeyword(final OIFitsChecker checker, final FitsHDU hduFits, final KeywordMeta keyword, final String keywordValue) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "KEYWORD {0} = ''{1}''", new Object[]{keyword.getName(), keywordValue});
        }
        Object value;
        final Types dataType = keyword.getDataType();
        Types kDataType = dataType;

        // Note: OIFits keywords only use 'A', 'J', 'D' types:
        // but ImageOI use 'L'
        if (dataType == Types.TYPE_CHAR) {
            value = (keywordValue != null) ? keywordValue.trim() : null;
        } else if (dataType == Types.TYPE_LOGICAL) {
            value = (keywordValue != null) ? Boolean.valueOf(keywordValue.startsWith("T")) : null;
        } else {
            value = null;
            // Numeric keywords:
            if (keywordValue != null) {
                if (keywordValue.indexOf('.') == -1) {
                    // check for Integers:
                    value = parseInteger(keywordValue);
                }
                if (value != null) {
                    // parsed value is really an integer:
                    kDataType = Types.TYPE_INT;
                } else {
                    // check for Doubles:
                    value = parseDouble(keywordValue);
                    if (value != null) {
                        // parsed value is really a double:
                        kDataType = Types.TYPE_DBL;
                    }
                }
            }
        }
        // Check Format
        if (checker != null) {
            hduFits.checkKeywordFormat(checker, hduFits, keyword, kDataType);
        }
        if (dataType == Types.TYPE_INT || dataType == Types.TYPE_DBL) {
            if (value == null) {
                if (keywordValue != null) {
                    // TODO: kill when the keywords are always initialized to a default value...
                    // default value if keyword value is not a number:
                    value = Double.valueOf(0d);
                }
            }
            if (value != null) {
                // cross conversion:
                if (dataType == Types.TYPE_INT) {
                    value = NumberUtils.valueOf(((Number) value).intValue());
                } else {
                    value = Double.valueOf(((Number) value).doubleValue());
                }
            }
        }
        return value;
    }

    /**
     * Process the image header to get image keyword values
     * @param header image header
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @param requireCdeltKeywords throw an exception if CDELT keywords are missing
     * @throws FitsException if any FITS error occurred
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed or missing CDELT keyword
     */
    private static void processImageKeywords(final Header header, final FitsImage image, final int imageIndex, final boolean requireCdeltKeywords) throws FitsException, IllegalArgumentException {
        // Note : a fits keyword has a KEY, VALUE AND COMMENT

        // Handle x-y axes dimensions:
        /*
         KEYWORD NAXIS = '2'	// Number of axes
         KEYWORD NAXIS1 = '512'	// Axis length
         KEYWORD NAXIS2 = '512'	// Axis length
         */
        // note: x axis has keyword index 1:
        image.setNbCols(header.getIntValue(FitsConstants.KEYWORD_NAXIS1, 0));
        // note: y axis has keyword index 2:
        image.setNbRows(header.getIntValue(FitsConstants.KEYWORD_NAXIS2, 0));

        // Parse all axis units:
        FitsUnit unit1 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT1));
        FitsUnit unit2 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT2));
        final FitsUnit unit3 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT3));

        // Process reference pixel:
        /*
         KEYWORD CRPIX1 = '256.'	// Reference pixel
         KEYWORD CRPIX2 = '256.'	// Reference pixel
         KEYWORD CRPIX3 = '1.000000'
         */
        image.setPixRefCol(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX1, FitsImageConstants.DEFAULT_CRPIX));
        image.setPixRefRow(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX2, FitsImageConstants.DEFAULT_CRPIX));
        image.setPixRefWL(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX3, FitsImageConstants.DEFAULT_CRPIX));

        // Process coordinates at the reference pixel:
        // note: units are ignored but default unit is Degrees
        if (unit2.equals(FitsUnit.NO_UNIT)) {
            if (!unit1.equals(FitsUnit.NO_UNIT)) {
                // use same unit (if missing)
                unit2 = unit1;
            } else {
                unit2 = FitsUnit.ANGLE_DEG;
            }
        }
        if (unit1.equals(FitsUnit.NO_UNIT)) {
            unit1 = FitsUnit.ANGLE_DEG;
        }
        /*
         KEYWORD CRVAL1 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL2 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL3 = '1.600000'
         */
        image.setValRefCol(unit1.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL1, FitsImageConstants.DEFAULT_CRVAL), FitsUnit.ANGLE_RAD));
        image.setValRefRow(unit2.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL2, FitsImageConstants.DEFAULT_CRVAL), FitsUnit.ANGLE_RAD));
        image.setValRefWL(unit3.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL3, Double.NaN), FitsUnit.WAVELENGTH_METER));

        // Process increments along axes:
        /*
         KEYWORD CDELT1 = '-1.2E-10' // Coord. incr. per pixel (original value)
         KEYWORD CDELT2 = '1.2E-10'	 // Coord. incr. per pixel (original value)
         KEYWORD CDELT3 = '0.000000'
         */
        // but check before if we have data and that CDELT is defined if requested mandatory
        if (image.getNbCols() > 0 && image.getNbRows() > 0 && requireCdeltKeywords
                && (header.getValue(FitsImageConstants.KEYWORD_CDELT1) == null
                || header.getValue(FitsImageConstants.KEYWORD_CDELT2) == null)) {
            logger.log(Level.WARNING, " Missing keyword(s) [ " + FitsImageConstants.KEYWORD_CDELT1 + "={0} or "
                    + FitsImageConstants.KEYWORD_CDELT2 + "={1} ] in HDU #{2}", new Object[]{header.getStringValue(FitsImageConstants.KEYWORD_CDELT1), header.getStringValue(FitsImageConstants.KEYWORD_CDELT2), image.getFitsImageHDU().getExtNb()});
            throw new IllegalArgumentException(" Missing keyword(s) [ " + FitsImageConstants.KEYWORD_CDELT1 + " or " + FitsImageConstants.KEYWORD_CDELT2 + " ] in HDU #" + image.getFitsImageHDU().getExtNb());
        }
        image.setSignedIncCol(unit1.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT1, FitsImageConstants.DEFAULT_CDELT), FitsUnit.ANGLE_RAD));
        image.setSignedIncRow(unit2.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT2, FitsImageConstants.DEFAULT_CDELT), FitsUnit.ANGLE_RAD));
        image.setIncWL(unit3.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT3, Double.NaN), FitsUnit.WAVELENGTH_METER));

        // Fix missing CUNIT3 but values given in microns instead of meters:
        if (image.getValRefWL() > 1e-1d) {
            image.setValRefWL(image.getValRefWL() * 1e-6d);
            image.setIncWL(image.getIncWL() * 1e-6d);

            if (imageIndex <= 1) {
                // only report on the first image:
                logger.log(Level.WARNING, "Fixed missing Wavelength unit (microns instead of meter): CRVAL3={0} - CDELT3={1}", new Object[]{image.getValRefWL(), image.getIncWL()});
            }
        }

        // Process rotation (deg):
        image.setRotAngle(header.getDoubleValue(FitsImageConstants.KEYWORD_CROTA2, 0.0));

        // Process data min/max:
        /*
         KEYWORD DATAMAX = '5120.758'	// Maximum data value
         KEYWORD DATAMIN = '0.0'	// Minimum data value
         */
        // note: data min/max are later recomputed (missing / invalid values or bad precision)
        image.setDataMin(header.getDoubleValue(FitsConstants.KEYWORD_DATAMIN, Double.NaN));
        image.setDataMax(header.getDoubleValue(FitsConstants.KEYWORD_DATAMAX, Double.NaN));

        // set initial image FOV:
        image.defineOrigMaxAngle();
    }

    /**
     * Process the header to copy all keywords
     * @param header image header
     * @param hduFits Fits image HDU container
     * @throws FitsException if any FITS error occurred
     */
    private static void loadHeaderCards(final Header header, final FitsHDU hduFits) throws FitsException {
        final List<FitsHeaderCard> headerCards = hduFits.getHeaderCards(header.getNumberOfCards());

        final boolean isImage = (hduFits instanceof FitsImageHDU);

        final Map<String, KeywordMeta> keywordsDesc = hduFits.getKeywordsDesc();

        HeaderCard card;
        String key, value;
        for (Iterator<?> it = header.iterator(); it.hasNext();) {
            card = (HeaderCard) it.next();

            key = card.getKey();

            if ("END".equals(key)) {
                break;
            }

            // For tables ignore standard keywords
            // Anyway do not store already handled keywords
            if (!keywordsDesc.containsKey(key)
                    && (isImage || !FitsUtils.isStandardKeyword(key))) {
                // no trim:
                value = card.getValue();

                headerCards.add(new FitsHeaderCard(key, value, card.isStringValue(), card.getComment()));
            }
        }

        hduFits.trimHeaderCards();

        if (logger.isLoggable(Level.FINE) && hduFits.hasHeaderCards()) {
            logger.log(Level.FINE, "{0} contains extra keywords:\n{1}",
                    new Object[]{hduFits.idToString(), hduFits.getHeaderCardsAsString("\n")});
        }
    }

    /**
     * Process the image data and store them in the given FitsImage
     * @param hdu image HDU
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @throws FitsException if any FITS error occured
     */
    private static void processData(final ImageHDU hdu, final FitsImage image, final int imageIndex) throws FitsException {

        // load the complete image:
        final ImageData fitsData = (ImageData) hdu.getData();

        final Object allData = fitsData.getData();

        if (allData != null) {
            // interpret also BSCALE / BZERO (BUNIT) if present
            final int bitPix = hdu.getBitPix();
            final double bZero = hdu.getBZero();
            final double bScale = hdu.getBScale();

            final int nbCols = image.getNbCols();
            final int nbRows = image.getNbRows();

            // get image plane:
            final Object planeData = (imageIndex != UNDEFINED_INDEX) ? getPlaneData(allData, bitPix, imageIndex) : allData;

            // convert any data to float[][]:
            final float[][] imgData = getImageData(nbRows, nbCols, bitPix, planeData, bZero, bScale);

            image.setData(imgData);
        }
    }

    /**
     * Extract from the given 3D array the image plane at the given index
     * @param array3D raw data array
     * @param bitpix bit per pixels
     * @param imageIndex image plane index [1..n]
     * @return array2D or null if invalid bitpix or image index
     */
    private static Object getPlaneData(final Object array3D, final int bitpix, final int imageIndex) {
        if (array3D == null || imageIndex < 0) {
            return null;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "bitPix     = {0}", bitpix);
            logger.log(Level.FINE, "imageIndex = {0}", imageIndex);
        }

        final int imgIndex = imageIndex - 1;

        switch (bitpix) {
            case BasicHDU.BITPIX_BYTE:
                final byte[][][] bArray = (byte[][][]) array3D;
                if (imgIndex < bArray.length) {
                    return bArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_SHORT:
                final short[][][] sArray = (short[][][]) array3D;
                if (imgIndex < sArray.length) {
                    return sArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_INT:
                final int[][][] iArray = (int[][][]) array3D;
                if (imgIndex < iArray.length) {
                    return iArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_LONG:
                final long[][][] lArray = (long[][][]) array3D;
                if (imgIndex < lArray.length) {
                    return lArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_FLOAT:
                final float[][][] fArray = (float[][][]) array3D;
                if (imgIndex < fArray.length) {
                    return fArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_DOUBLE:
                final double[][][] dArray = (double[][][]) array3D;
                if (imgIndex < dArray.length) {
                    return dArray[imgIndex];
                }
                break;
            default:
        }
        return null;
    }

    /**
     * Convert and optionaly scale the given array2D to float[][]
     * @param rows number of rows
     * @param cols number of columns
     * @param bitpix bit per pixels
     * @param array2D input array2D to convert
     * @param bZero zero point in scaling equation
     * @param bScale linear factor in scaling equation
     * @return float[][]
     */
    private static float[][] getImageData(final int rows, final int cols, final int bitpix, final Object array2D,
                                          final double bZero, final double bScale) {

        if (array2D == null) {
            return null;
        }

        final boolean doZero = (bZero != 0d);
        final boolean doScaling = (bScale != 1d);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "bitPix    = {0}", bitpix);
            logger.log(Level.FINE, "doZero    = {0}", doZero);
            logger.log(Level.FINE, "doScaling = {0}", doScaling);
        }

        if (bitpix == BasicHDU.BITPIX_FLOAT && !(doZero || doScaling)) {
            return (float[][]) array2D;
        }

        // 1 - convert data to float[][]
        final float[][] output = ArrayConvert.toFloats(rows, cols, array2D);

        // 2 - scale data:
        if (doZero || doScaling) {
            float[] oRow;
            for (int i, j = 0; j < rows; j++) {
                oRow = output[j];
                for (i = 0; i < cols; i++) {
                    if (doScaling) {
                        oRow[i] = (float) (oRow[i] * bScale);
                    }
                    if (doZero) {
                        oRow[i] = (float) (oRow[i] + bZero);
                    }
                }
            }
        }
        return output;
    }

    /**
     * Parse the String value as a double
     * @param value string value
     * @return Double or null if number format exception
     */
    private static Double parseDouble(final String value) {
        Double res = null;
        try {
            res = Double.valueOf(value);
        } catch (NumberFormatException nfe) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "parseDouble failure: {0}", value);
            }
        }
        return res;
    }

    /**
     * Parse the String value as an integer
     * @param value string value
     * @return Integer or null if number format exception
     */
    private static Integer parseInteger(final String value) {
        Integer res = null;
        try {
            res = NumberUtils.valueOf(value);
        } catch (NumberFormatException nfe) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "parseInteger failure: {0}", value);
            }
        }
        return res;
    }

}
