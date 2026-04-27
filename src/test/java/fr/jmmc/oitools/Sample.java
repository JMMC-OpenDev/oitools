/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIFlux;
import fr.jmmc.oitools.model.OIPrimaryHDU;
import fr.jmmc.oitools.model.OIVis;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 *
 * @author bourgesl
 */
public class Sample {

    /** Logger associated to model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Sample.class.getName());

    private final static boolean ENABLE_VALIDATION = false;

    public static void convert(final OIFitsStandard std,
                               final String pathFile,
                               final Consumer<OIFitsFile> processor) throws IOException, FitsException {

        logger.log(Level.INFO, "convert(): std = {0}", std);
        final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(std, pathFile);

        if (oiFitsFile != null) {
            if (processor != null) {
                processor.accept(oiFitsFile);
            }

            final String pathFileTo = oiFitsFile.getAbsoluteFilePath().replaceFirst("\\.", "-fix.");

            logger.log(Level.INFO, "convert(): writing {0}", pathFileTo);
            OIFitsWriter.writeOIFits(pathFileTo, oiFitsFile);
        }
    }

    public static void main(final String[] args) {
        // Set the default locale to en-US locale (for Numerical Fields "." ",")
        Locale.setDefault(Locale.US);

        // Set the default timezone to GMT to handle properly the date in UTC
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // Discard validation (verbose):
        OIFitsChecker.setEnableChecker(ENABLE_VALIDATION);
        try {
            // Load:
            final String folder = "/home/bourgesl/OIFitsExplorer/SPIE_2026/";

            if (false) {
                convert(OIFitsStandard.VERSION_1, folder + "OBJ1/OBJECT1_LM.fits", null);
                convert(OIFitsStandard.VERSION_1, folder + "OBJ1/OBJECT1_N.fits", null);
            }

            if (true) {
                convert(OIFitsStandard.VERSION_2, folder + "OBJ2/OBJECT2_K-fix2.oifits",
                        (oiFitsFile) -> {
                            // custom processor:
                            final OIPrimaryHDU primaryHDU = oiFitsFile.getOIPrimaryHDU();

                            if (primaryHDU != null) {
                                // hdu["CONTENT"] = "OIFITS2"
                                primaryHDU.setContent(FitsConstants.KEYWORD_CONTENT_OIFITS2);

                                // hdu["DATE"] = oi.vis2[1].date_obs
                                // hdu["DATE-OBS"] = oi.vis2[1].date_obs
                                final String dateObs = oiFitsFile.getOiVis2()[0].getDateObs();
                                primaryHDU.setDate(dateObs);
                                primaryHDU.setDateObs(dateObs);

                                // hdu["OBJECT"] = target.target
                                primaryHDU.setObject(oiFitsFile.getOiTarget().getTarget()[0]);
                                // hdu["TELESCOP"] = array.arrname
                                primaryHDU.setTelescop(oiFitsFile.getOiArrays()[0].getArrName());
                                // hdu["INSTRUME"] = instr.insname
                                primaryHDU.setInstrume(oiFitsFile.getOiWavelengths()[0].getInsName());

                                // hdu["ORIGIN"] =  "Interferomtric Imaging Contest"
                                // hdu["AUTHOR"] = "J. Drevon & F. Soulez"
                                primaryHDU.setOrigin("Interferometric Imaging Contest");
                                primaryHDU.setAuthor("J. Drevon & F. Soulez");
                            }

                            if (false) {
                                // hdu = f["OI_FLUX"]
                                // hdu["TUNIT$(hdu.column_number("FLUXDATA"))"] = "Jy"
                                // hdu["TUNIT$(hdu.column_number("FLUXERR"))"] = "Jy"
                                for (OIFlux oiFlux : oiFitsFile.getOiFlux()) {
                                    oiFlux.getColumnMeta(OIFitsConstants.COLUMN_FLUXDATA).getCustomUnits().setRepresentation("Jy");
                                    oiFlux.getColumnMeta(OIFitsConstants.COLUMN_FLUXERR).getCustomUnits().setRepresentation("Jy");
                                }

                                // hdu = f["OI_VIS"]
                                // hdu["TUNIT$(hdu.column_number("IVIS"))"] = "ADU"
                                // hdu["TUNIT$(hdu.column_number("IVISERR"))"] = "ADU"
                                // hdu["TUNIT$(hdu.column_number("RVIS"))"] = "ADU"
                                // hdu["TUNIT$(hdu.column_number("RVISERR"))"] = "ADU"
                                for (OIVis oiVis : oiFitsFile.getOiVis()) {
                                    oiVis.getColumnMeta(OIFitsConstants.COLUMN_IVIS).getCustomUnits().setRepresentation("ADU");
                                    oiVis.getColumnMeta(OIFitsConstants.COLUMN_IVISERR).getCustomUnits().setRepresentation("ADU");
                                    oiVis.getColumnMeta(OIFitsConstants.COLUMN_RVIS).getCustomUnits().setRepresentation("ADU");
                                    oiVis.getColumnMeta(OIFitsConstants.COLUMN_RVISERR).getCustomUnits().setRepresentation("ADU");
                                }
                            }
                        });
            }

            if (true) {
                OIFitsChecker.setEnableChecker(true);
                OIFitsLoader.loadOIFits(folder + "OBJ2/OBJECT2_K-fix2-fix.oifits");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "failure: ", e);
        }
    }

}
