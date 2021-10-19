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

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.model.DataModel;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.fits.HeaderCardException;
import fr.nom.tam.fits.ImageData;
import fr.nom.tam.fits.ImageHDU;
import fr.nom.tam.util.BufferedFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * This stateless class writes is an FitsImageFile structure into an Fits image and cube
 *
 * @author bourgesl
 */
public final class FitsImageWriter {

    /* constants */
    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImageWriter.class.getName());
    /** skip keywords when copying fits header cards present in FitsImage */
    private final static Set<String> SKIP_KEYWORDS = new HashSet<String>(32);

    static {
        FitsUtils.setup();

        SKIP_KEYWORDS.addAll(FitsUtils.FITS_STANDARD_KEYWORDS);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CUNIT1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CUNIT2);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CUNIT3);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRPIX1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRPIX2);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRPIX3);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRVAL1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRVAL2);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRVAL3);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CDELT1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CDELT2);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CDELT3);

        SKIP_KEYWORDS.add(FitsConstants.KEYWORD_DATAMIN);
        SKIP_KEYWORDS.add(FitsConstants.KEYWORD_DATAMAX);

        // Preserve few standard keywords (write pre-loaded values):
        SKIP_KEYWORDS.remove(FitsConstants.KEYWORD_EXT_NAME);
    }

    /**
     * Private constructor
     */
    private FitsImageWriter() {
        super();
    }

    /**
     * Return true if the given keyword name corresponds to a standard FITS keyword + Image keywords
     * @param name keyword name
     * @return true if the given keyword name corresponds to a standard FITS keyword
     */
    public static boolean skipKeyword(final String name) {
        return SKIP_KEYWORDS.contains(name) || FitsUtils.isOpenKeyword(name);
    }

    /**
     * Main method to write an FitsImageFile structure
     * @param absFilePath absolute File path on file system (not URL)
     * @param imgFitsFile FitsImageFile structure to write
     * @throws FitsException if the fits can not be written
     * @throws IOException IO failure
     */
    public static void write(final String absFilePath, final FitsImageFile imgFitsFile) throws IOException, FitsException {
        imgFitsFile.setAbsoluteFilePath(absFilePath);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "writing {0}", absFilePath);
        }

        BufferedFile bf = null;
        try {
            final long start = System.nanoTime();

            // create the fits model :
            final Fits fitsFile = new Fits();

            // process all fitsImageHDUs :
            createHDUnits(imgFitsFile, fitsFile);
            bf = new BufferedFile(absFilePath, "rw");

            // write the fits file :
            fitsFile.write(bf);

            // flush and close :
            bf.close();
            bf = null;

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "write : duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
            }

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to write the file : " + absFilePath, fe);

            throw fe;
        } finally {
            if (bf != null) {
                // flush and close :
                bf.close();
            }
        }
    }

    /**
     * Create all Fits HD units corresponding to Fits images
     * @param imgFitsFile FitsImageFile structure to write
     * @param fitsFile fits file
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    public static void createHDUnits(final FitsImageFile imgFitsFile, final Fits fitsFile) throws FitsException, IOException {
        createHDUnits(fitsFile, imgFitsFile.getFitsImageHDUs(), imgFitsFile.getFileName(), 0);
    }

    /**
     * Create all Fits HD units corresponding to Fits images more parameters
     * @param fitsFile fits file
     * @param imageHDUs list of FitsImageHDU
     * @param fileName 
     * @param startIdx begin
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private static void createHDUnits(final Fits fitsFile, final List<FitsImageHDU> imageHDUs,
                                      final String fileName, final int startIdx) throws FitsException, IOException {

        int i = startIdx;

        for (FitsImageHDU imageHDU : imageHDUs) {
            fitsFile.addHDU(createHDUnit(imageHDU, fileName, i));
            i++;
        }
    }

    /**
     * createHDUnit more params
     * @param imageHDU FitsImageFile structure to write
     * @param fileName 
     * @param hduIndex 
     * @return BasicHDU
     * @throws FitsException if any FITS error occurred
     */
    public static BasicHDU createHDUnit(final FitsImageHDU imageHDU, final String fileName, final int hduIndex) throws FitsException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "createHDUnit: {0}", imageHDU);
        }

        // Note: if multiple images, they must have the same dimensions and keywords
        // Prepare the image data to create HDU :
        final Data fitsData = createImageData(imageHDU, fileName, hduIndex);

        // Generate the header from the data :
        final Header header = ImageHDU.manufactureHeader(fitsData);

        // create HDU :
        final ImageHDU hdu = new ImageHDU(header, fitsData);

        // Finalize Header :
        // Add image keywords in the header :
        if (imageHDU.hasImages()) {
            processImageKeywords(header, imageHDU.getFitsImages().get(0), imageHDU.isFitsCube());
        }

        processKeywords(header, imageHDU);

        return hdu;
    }

    /**
     * createImageData
     * @param imageHDU FitsImageFile structure to write
     * @param fileName 
     * @param hduIndex 
     * @return BasicHDU
     * @throws FitsException if any FITS error occurred
     */
    private static ImageData createImageData(final FitsImageHDU imageHDU, final String fileName, final int hduIndex) throws FitsException {
        final int nImages = imageHDU.getImageCount();

        if (nImages == 0) {
            return new ImageData();
        } else {
            Object data;
            final List<FitsImage> fitsImages = imageHDU.getFitsImages();
            if (nImages == 1) {
                final FitsImage image = fitsImages.get(0);
                if (image.getData() == null) {
                    throw new FitsException("No image data in FitsImage !");
                }
                if (fileName != null) {
                    // update the fits image identifier:
                    if (DataModel.isRenameFitsImageIdentifierDuringWrite()) {
                        image.setFitsImageIdentifier(fileName + '#' + hduIndex);
                    }
                }
                data = image.getData();
            } else {
                // Fits cube (3D):
                final float[][][] fArray = new float[nImages][][];

                for (int i = 0; i < nImages; i++) {
                    final FitsImage image = fitsImages.get(i);

                    if (image.getData() == null) {
                        throw new FitsException("No image data in FitsImage !");
                    }
                    if (fileName != null) {
                        // update the fits image identifier:
                        if (DataModel.isRenameFitsImageIdentifierDuringWrite()) {
                            image.setFitsImageIdentifier(fileName + '#' + hduIndex);
                        }
                    }
                    fArray[i] = image.getData();
                }
                data = fArray;
            }
            return new ImageData(data);
        }
    }

    /**
     * Fill the given header with header cards (filtered)
     * @param header Header
     * @param hduFits HDU fits
     * @throws fr.nom.tam.fits.HeaderCardException
     */
    private static void processHeaderCards(final Header header, final FitsHDU hduFits) throws HeaderCardException {
        if (hduFits.hasHeaderCards()) {
            final boolean isImage = (hduFits instanceof FitsImageHDU);

            for (FitsHeaderCard headerCard : hduFits.getHeaderCards()) {
                final String key = headerCard.getKey();

                // skip already handled keywords:
                if (!((isImage) ? skipKeyword(key) : hduFits.hasKeywordMeta(key) || FitsUtils.isStandardKeyword(key))) {
                    // support repeated keywords
                    header.addLine(new HeaderCard(key, headerCard.getValue(), headerCard.isString(), headerCard.getComment()));
                }
            }
        }
    }

    /**
     * Process the hdu header to set keywords defined in the keyword descriptors
     * @param header HDU header
     * @param hduFits HDU fits
     * @throws FitsException if any FITS error occurred
     */
    public static void processKeywords(final Header header, final FitsHDU hduFits) throws FitsException {

        // Note : OIFits keywords only use 'A', 'I', 'D' types :
        String keywordName;
        Object keywordValue;

        for (KeywordMeta keyword : hduFits.getKeywordDescCollection()) {
            keywordName = keyword.getName();

            if (FitsConstants.KEYWORD_NAXIS2.equals(keywordName)) {
                // skip NAXIS2 (handled by nom.tam.Fits)
                continue;
            }

            // get keyword value :
            keywordValue = hduFits.getKeywordValue(keywordName);

            if (keywordValue == null) {
                // skip missing values
                continue;
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "KEYWORD {0} = ''{1}''", new Object[]{keywordName, keywordValue});
            }

            switch (keyword.getDataType()) {
                case TYPE_INT:
                    header.addValue(keywordName, ((Integer) keywordValue).intValue(), keyword.getDescription());
                    break;
                case TYPE_DBL:
                    header.addValue(keywordName, ((Double) keywordValue).doubleValue(), keyword.getDescription());
                    break;
                case TYPE_LOGICAL:
                    header.addValue(keywordName, ((Boolean) keywordValue).booleanValue(), keyword.getDescription());
                    break;
                case TYPE_CHAR:
                default:
                    header.addValue(keywordName, (String) keywordValue, keyword.getDescription());
            }
        }

        processHeaderCards(header, hduFits);
    }

    /**
     * Process the image header to set keywords defined in the FitsImage
     * @param header binary table header
     * @param image FitsImage
     * @param isFitsCube true if multiple images
     * @throws FitsException if any FITS error occurred
     */
    private static void processImageKeywords(final Header header, final FitsImage image,
                                             final boolean isFitsCube) throws FitsException {
        // Note : a fits keyword has a KEY, VALUE AND COMMENT

        // skip x-y axes dimensions (handled by nom.tam.Fits) :
        /*
         KEYWORD NAXIS = '2'	// Number of axes
         KEYWORD NAXIS1 = '512'	// Axis length
         KEYWORD NAXIS2 = '512'	// Axis length
         */
        // Process reference pixel:
        /*
         KEYWORD CRPIX1 = '256.'	// Reference pixel
         KEYWORD CRPIX2 = '256.'	// Reference pixel
         */
        header.addValue(FitsImageConstants.KEYWORD_CRPIX1, image.getPixRefCol(), "Reference pixel");
        header.addValue(FitsImageConstants.KEYWORD_CRPIX2, image.getPixRefRow(), "Reference pixel");
        if (isFitsCube) {
            header.addValue(FitsImageConstants.KEYWORD_CRPIX3, image.getPixRefWL(), "Reference pixel");
        }

        // Process coordinates at the reference pixel:
        // note: units are in radians in internal datamodel and exported in degree
        final FitsUnit modelUnit = FitsUnit.ANGLE_RAD;
        final FitsUnit exportUnit = FitsUnit.ANGLE_DEG;
        /*
         KEYWORD CRVAL1 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL2 = '0.'	// Coordinate at reference pixel

         KEYWORD CTYPE1 = ''	//  Units of coordinate
         KEYWORD CTYPE2 = ''	//  Units of coordinate
         */
        header.addValue(FitsImageConstants.KEYWORD_CRVAL1, modelUnit.convert(image.getValRefCol(), exportUnit), "Coordinate at reference pixel");
        header.addValue(FitsImageConstants.KEYWORD_CRVAL2, modelUnit.convert(image.getValRefRow(), exportUnit), "Coordinate at reference pixel");
        if (isFitsCube) {
            header.addValue(FitsImageConstants.KEYWORD_CRVAL3, image.getValRefWL(), "Wavelength at reference pixel");
        }
        // Process increments along axes:
        /*
         KEYWORD CDELT1 = '-1.2E-10'	// Coord. incr. per pixel (original value)
         KEYWORD CDELT2 = '1.2E-10'	// Coord. incr. per pixel (original value)
         */
        header.addValue(FitsImageConstants.KEYWORD_CDELT1, modelUnit.convert(image.getSignedIncCol(), exportUnit), "Coord. incr. per pixel");
        header.addValue(FitsImageConstants.KEYWORD_CDELT2, modelUnit.convert(image.getSignedIncRow(), exportUnit), "Coord. incr. per pixel");
        if (isFitsCube) {
            header.addValue(FitsImageConstants.KEYWORD_CDELT3, image.getIncWL(), "Wavelength incr. per pixel");
        }

        // Process units for 2D axes:
        /*
         CUNIT1  = 'deg     '           / Physical units for CDELT1 and CRVAL1
         CUNIT2  = 'deg     '           / Physical units for CDELT2 and CRVAL2
         */
        header.addValue(FitsImageConstants.KEYWORD_CUNIT1, exportUnit.getStandardRepresentation(), "Physical units for CDELT1 and CRVAL1");
        header.addValue(FitsImageConstants.KEYWORD_CUNIT2, exportUnit.getStandardRepresentation(), "Physical units for CDELT2 and CRVAL2");
        if (isFitsCube) {
            header.addValue(FitsImageConstants.KEYWORD_CUNIT3, FitsUnit.WAVELENGTH_METER.getStandardRepresentation(),
                    "Physical units for CDELT3 and CRVAL3");
        }

        // Process rotation (deg):
        if (image.isRotAngleDefined()) {
            header.addValue(FitsImageConstants.KEYWORD_CROTA2, image.getRotAngle(), "Rotation angle in degrees");
        }

        // Process data min/max:
        /*
         KEYWORD DATAMAX = '5120.758'	// Maximum data value
         KEYWORD DATAMIN = '0.0'	// Minimum data value
         */
        // DISABLE DATA MIN/MAX as it will not work with Fits cubes (merge data min/max)
        /*
        // note: data min/max are later recomputed (missing / invalid values or bad precision)
        if (!Double.isNaN(image.getDataMin())) {
            header.addValue(FitsConstants.KEYWORD_DATAMIN, image.getDataMin(), "Minimum data value");
        }
        if (!Double.isNaN(image.getDataMax())) {
            header.addValue(FitsConstants.KEYWORD_DATAMAX, image.getDataMax(), "Maximum data value");
        }
         */
    }

}
