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

import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIT3;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIVis2;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import fr.jmmc.oitools.test.OITableUtils;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Creating a standard V1 file. Save it in test/test folder.
 * 
 * @author kempsc
 */
public class CreateOIFileV1Test extends JUnitBaseTest {

    private static final Random RANDOM;

    static {
        // Set seed to generate always the same values between retries
        RANDOM = new Random(0);
    }

    @Test
    public void create() throws IOException, FitsException {

        final String absFilePath = TEST_DIR_TEST + "TEST_CreateOIFileV1.fits";

        logger.log(Level.INFO, "Creating file : {0}", absFilePath);

        final OIFitsFile srcOIFitsFile = new OIFitsFile(OIFitsStandard.VERSION_1);
        fill(srcOIFitsFile);

        logger.log(Level.INFO, "create : XML DESC : \n{0}", XmlOutputVisitor.getXmlDesc(srcOIFitsFile, true));

        final OIFitsChecker checker = new OIFitsChecker();
        srcOIFitsFile.check(checker);

        // validation results
        logger.log(Level.INFO, "create : validation results\n{0}", checker.getCheckReport());

        logger.log(Level.INFO, "writeOIFits: {0}", absFilePath);

        OIFitsWriter.writeOIFits(absFilePath, srcOIFitsFile);

        Assert.assertEquals("validation failed", 0, checker.getNbSeveres() + checker.getNbWarnings());

        // verify and check :
        final OIFitsFile destOIFitsFile = OIFitsLoader.loadOIFits(absFilePath);
        if (destOIFitsFile == null) {
            fail("Error loadOIFits: " + absFilePath);
        } else if (!OITableUtils.compareOIFitsFile(srcOIFitsFile, destOIFitsFile)) {
            fail("Error compareOIFitsFile: " + absFilePath);
        }

    }

    private static void fill(final OIFitsFile oiFitsFile) {

        // OI_ARRAY :
        createOIArray(oiFitsFile);

        // OI_WAVELENGTH :
        createOIWave(oiFitsFile);

        // OI_TARGET :
        createOITarget(oiFitsFile);

        // OI_VIS :
        createOIVis(oiFitsFile);

        // OI_VIS2 :
        createOIVis2(oiFitsFile);

        // OI_T3 :
        createOIT3(oiFitsFile);

        // Corrupt integrity
        if (false) {
            oiFitsFile.getOiArray("VLTI-like").setArrName("CORRUPTED ARRNAME");
        }
    }

    //Creating methodes
    private static void createOIArray(OIFitsFile oiFitsFile) {
        final OIArray oiArray = new OIArray(oiFitsFile, 3);

        oiArray.setArrName("VLTI-like");
        oiArray.setFrame(OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC);
        oiArray.setArrayXYZ(1942042.8584924, -5455305.996911, -2654521.4011759);

        // extra keywords:
        oiArray.addHeaderCard("MY_KEY", "MY_VALUE up to 70 chars", "My comment (key is max 8 chars)");

        oiArray.addHeaderCard("BLABLA", "is a test", "headcard");
        oiArray.addHeaderCard("jglm", "iofekzfkpe", "ojetezj");
        oiArray.addHeaderCard("rtret", "464694zeko", "e");

        /*try {
        oiArray.addHeaderCard("MY_KEY is too long", "", "");
        Assert.fail("addHeaderCard: key is too long");
        } catch (IllegalArgumentException iae) {
        // ignore
        }
        
        try {
        oiArray.addHeaderCard("KEY", "01234567890123456789012345678901234567890123456789012345678901234567890123456789", "");
        Assert.fail("addHeaderCard: value is too long");
        } catch (IllegalArgumentException iae) {
        // ignore
        }*/
        oiArray.setTelName(0, "UT1");
        oiArray.setStaName(0, "U1");
        oiArray.setStaIndex(0, 2);
        oiArray.setDiameter(0, 8f);
        oiArray.setStaXYZ(0, -0.73422599479242, -9.92488562146125, -22.03283353519204);

        oiArray.setTelName(1, "UT2");
        oiArray.setStaName(1, "U2");
        oiArray.setStaIndex(1, 4);
        oiArray.setDiameter(1, 8f);
        oiArray.setStaXYZ(1, 20.45018397209875, 14.88732843219187, 24.17944630588896);

        oiArray.setTelName(2, "UT3");
        oiArray.setStaName(2, "U3");
        oiArray.setStaIndex(2, 7);
        oiArray.setDiameter(2, 8f);
        oiArray.setStaXYZ(2, 35.32766648520568, 44.91458329169021, 56.61105628712381);

        oiFitsFile.addOiTable(oiArray);
    }

    private static void createOIWave(OIFitsFile oiFitsFile) {
        final int nWave = 64;
        final OIWavelength waves = new OIWavelength(oiFitsFile, nWave);
        waves.setInsName("AMBER-like");

        final float wMin = 1.54E-6f;
        final float wMax = 1.82E-6f;
        final float step = (wMax - wMin) / (nWave - 1);

        final float[] effWave = waves.getEffWave();
        final float[] effBand = waves.getEffBand();

        float waveLength = wMin;
        for (int i = 0; i < nWave; i++) {
            effWave[i] = waveLength;
            effBand[i] = 5.48E-10f;

            waveLength += step;
        }

        oiFitsFile.addOiTable(waves);
    }

    private static void createOITarget(OIFitsFile oiFitsFile) {
        final OITarget target = new OITarget(oiFitsFile, 2);

        target.getTargetId()[0] = 1;
        target.getTarget()[0] = "Charleen's Star";

        target.getRaEp0()[0] = 11.77854d;
        target.getDecEp0()[0] = 24.268334d;
        target.getEquinox()[0] = 2000f;

        target.getRaErr()[0] = 1e-4d;
        target.getDecErr()[0] = 1e-4d;

        target.getSysVel()[0] = 120d;
        target.getVelTyp()[0] = OIFitsConstants.COLUMN_VELTYP_LSR;
        target.getVelDef()[0] = OIFitsConstants.COLUMN_VELDEF_OPTICAL;

        target.getPmRa()[0] = -2.8119e-5d;
        target.getPmDec()[0] = -2.2747e-5d;

        target.getPmRaErr()[0] = 2e-7d;
        target.getPmDecErr()[0] = 3e-7d;

        target.getParallax()[0] = 0.004999f;
        target.getParaErr()[0] = 1e-5f;

        target.getSpecTyp()[0] = "AOV";

        target.getTargetId()[1] = 2;
        target.getTarget()[1] = "Guillaume's Star";

        target.getRaEp0()[1] = 11.77854d;
        target.getDecEp0()[1] = 24.268334d;
        target.getEquinox()[1] = 2000f;

        target.getRaErr()[1] = 1e-4d;
        target.getDecErr()[1] = 1e-4d;

        target.getSysVel()[1] = 120d;
        target.getVelTyp()[1] = OIFitsConstants.COLUMN_VELTYP_LSR;
        target.getVelDef()[1] = OIFitsConstants.COLUMN_VELDEF_OPTICAL;

        target.getPmRa()[1] = -2.8119e-5d;
        target.getPmDec()[1] = -2.2747e-5d;

        target.getPmRaErr()[1] = 2e-7d;
        target.getPmDecErr()[1] = 3e-7d;

        target.getParallax()[1] = 0.004999f;
        target.getParaErr()[1] = 1e-5f;

        target.getSpecTyp()[1] = "AOV";

        oiFitsFile.addOiTable(target);
    }

    private static void createOIVis(OIFitsFile oiFitsFile) {

        final int nRows = 7;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                final OIVis vis = new OIVis(oiFitsFile, insname, nRows);

                vis.setDateObs("2017-07-06");
                vis.setArrName(arrname);
                vis.setInsName(insname);

                final int nWaveLengths = vis.getNWave();
                // Columns :
                final short[] targetIds = vis.getTargetId();
                final double[] times = vis.getTime();
                final double[] mjds = vis.getMJD();
                final double[] intTimes = vis.getIntTime();

                final float[][][] visData = vis.getVisData();
                final float[][][] visErr = vis.getVisErr();

                final double[][] visAmp = vis.getVisAmp();
                final double[][] visAmpErr = vis.getVisAmpErr();

                final double[][] visPhi = vis.getVisPhi();
                final double[][] visPhiErr = vis.getVisPhiErr();

                final double[] uCoords = vis.getUCoord();
                final double[] vCoords = vis.getVCoord();

                final short[][] staIndexes = vis.getStaIndex();
                final boolean[][] flags = vis.getFlag();

                for (int i = 0; i < nRows; i++) {
                    targetIds[i] = targetId;

                    times[i] = 0;
                    mjds[i] = 57940.0;
                    intTimes[i] = 500.0;

                    // Iterate on wave lengths :
                    for (int l = 0; l < nWaveLengths; l++) {
                        visData[i][l][0] = randFloat(0, 50000);
                        visData[i][l][1] = randFloat(0, 50000);
                        visErr[i][l][0] = randFloat(0, 20);
                        visErr[i][l][1] = randFloat(0, 20);

                        visAmp[i][l] = randFloat(0, 1000);
                        visAmpErr[i][l] = randFloat(0, 1000);

                        visPhi[i][l] = randFloat(0, 360);
                        visPhiErr[i][l] = randFloat(0, 10);

                        flags[i][l] = false;
                    }

                    uCoords[i] = randFloat(10, 300);
                    vCoords[i] = randFloat(10, 300);

                    int i1 = rand(nbStas);
                    int i2;
                    do {
                        i2 = rand(nbStas);
                    } while (i2 == i1);
                    staIndexes[i][0] = oiarray_staIndexes[i1];
                    staIndexes[i][1] = oiarray_staIndexes[i2];
                }
                oiFitsFile.addOiTable(vis);
            }
        }
    }

    private static void createOIVis2(OIFitsFile oiFitsFile) {

        final int nRows = 7;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                final OIVis2 vis2 = new OIVis2(oiFitsFile, insname, nRows);
                final int nWaveLengths = vis2.getNWave();

                vis2.setDateObs("2017-07-06");
                vis2.setArrName(arrname);
                vis2.setInsName(insname);

                //Column
                final short[] targetIds = vis2.getTargetId();
                final double[] times = vis2.getTime();
                final double[] mjds = vis2.getMJD();
                final double[] intTimes = vis2.getIntTime();

                final double[][] vis2Data = vis2.getVis2Data();
                final double[][] vis2Err = vis2.getVis2Err();

                final double[] uCoords = vis2.getUCoord();
                final double[] vCoords = vis2.getVCoord();

                final short[][] staIndexes = vis2.getStaIndex();

                final boolean[][] flags = vis2.getFlag();

                for (int i = 0; i < nRows; i++) {
                    targetIds[i] = targetId;

                    times[i] = 0;
                    mjds[i] = 57940.0;
                    intTimes[i] = 500.0;

                    // Iterate on wave lengths :
                    for (int l = 0; l < nWaveLengths; l++) {
                        vis2Data[i][l] = randFloat(0, 5000);
                        vis2Err[i][l] = randFloat(0, 20);

                        flags[i][l] = false;
                    }

                    uCoords[i] = randFloat(10, 300);
                    vCoords[i] = randFloat(10, 300);

                    int i1 = rand(nbStas);
                    int i2;
                    do {
                        i2 = rand(nbStas);
                    } while (i2 == i1);
                    staIndexes[i][0] = oiarray_staIndexes[i1];
                    staIndexes[i][1] = oiarray_staIndexes[i2];
                }
                oiFitsFile.addOiTable(vis2);
            }
        }
    }

    private static void createOIT3(OIFitsFile oiFitsFile) {

        final int nRows = 3;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                final OIT3 t3 = new OIT3(oiFitsFile, insname, nRows);
                final int nWaveLengths = t3.getNWave();

                t3.setDateObs("2017-07-06");
                t3.setArrName(arrname);
                t3.setInsName(insname);

                //Column
                final short[] targetIds = t3.getTargetId();
                final double[] times = t3.getTime();
                final double[] mjds = t3.getMJD();
                final double[] intTimes = t3.getIntTime();

                final double[][] t3amp = t3.getT3Amp();
                final double[][] t3amperr = t3.getT3AmpErr();
                final double[][] t3phi = t3.getT3Phi();
                final double[][] t3phierr = t3.getT3PhiErr();

                final double[] u1Coords = t3.getU1Coord();
                final double[] u2Coords = t3.getU2Coord();
                final double[] v1Coords = t3.getV1Coord();
                final double[] v2Coords = t3.getV2Coord();

                final short[][] staIndexes = t3.getStaIndex();

                final boolean[][] flags = t3.getFlag();

                for (int i = 0; i < nRows; i++) {
                    targetIds[i] = targetId;

                    times[i] = 0;
                    mjds[i] = 57940.0;
                    intTimes[i] = 500.0;

                    // Iterate on wave lengths :
                    for (int l = 0; l < nWaveLengths; l++) {
                        t3amp[i][l] = randFloat(0, 5000);
                        t3amperr[i][l] = randFloat(0, 20);

                        t3phi[i][l] = randFloat(0, 5000);
                        t3phierr[i][l] = randFloat(0, 20);

                        flags[i][l] = false;
                    }

                    u1Coords[i] = randFloat(10, 300);
                    u2Coords[i] = randFloat(10, 300);
                    v1Coords[i] = randFloat(10, 300);
                    v2Coords[i] = randFloat(10, 300);

                    staIndexes[i][0] = oiarray_staIndexes[0];
                    staIndexes[i][1] = oiarray_staIndexes[1];
                    staIndexes[i][2] = oiarray_staIndexes[2];
                }
                oiFitsFile.addOiTable(t3);
            }
        }
    }

    private static int rand(final int max) {
        return (int) (max * RANDOM.nextDouble());
    }

    private static float randFloat(final float max, final float min) {
        return (float) ((max - min) * RANDOM.nextDouble() + min);
    }
}
