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

import static fr.jmmc.oitools.JUnitBaseTest.TEST_DIR_TEST;
import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIArray;
import fr.jmmc.oitools.model.OICorr;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.model.OIInspol;
import fr.jmmc.oitools.model.OIPrimaryHDU;
import fr.jmmc.oitools.model.OIFlux;
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
 * Creating a standard V2 file. Save it in test/test folder.
 * @author kempsc
 */
public class CreateOIFitsV2Test {

    private static final Random RANDOM;

    //constants for all calcul corrIndx values
    private static final int N_WAVE = 7;
    private static final int N_VIS = 1;
    private static final int N_VIS2 = 2;
    private static final int N_T3 = 3;
    private static final int N_FLUX = 5;

    private static final int I_VIS = 1;
    private static final int BLK_COL_VIS = N_VIS * N_WAVE;

    private static final int I_VIS2 = I_VIS + 4 * BLK_COL_VIS;
    private static final int BLK_COL_VIS2 = N_VIS2 * N_WAVE;

    private static final int I_T3 = I_VIS2 + 1 * BLK_COL_VIS2;
    private static final int BLK_COL_T3 = N_WAVE * N_T3;

    private static final int I_FLUX = I_T3 + 2 * BLK_COL_T3;
    private static final int BLK_COL_FLUX = N_WAVE * N_FLUX;

    private static final int N_DATA = I_FLUX + BLK_COL_FLUX;

    static {
        // Set seed to generate always the same values between retries
        RANDOM = new Random(0);
    }

    @Test
    public void create() throws IOException, FitsException {

        final String absFilePath = TEST_DIR_TEST + "TEST_CreateOIFileV2.fits";

        logger.log(Level.INFO, "Creating file : {0}", absFilePath);

        final OIFitsFile srcOIFitsFile = new OIFitsFile(OIFitsStandard.VERSION_2);
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

        // Primary Header
        createPrimaryHeader(oiFitsFile);

        // OI_ARRAY :
        createOIArray(oiFitsFile);

        // OI_WAVELENGTH :
        createOIWave(oiFitsFile);

        // OI_TARGET :
        createOITarget(oiFitsFile);

        // OI_CORR :
        createOICorr(oiFitsFile);

        // OI_INSPOL:
        createOIInspol(oiFitsFile);

        // OI_VIS :
        createOIVis(oiFitsFile);

        // OI_VIS2 :
        createOIVis2(oiFitsFile);

        // OI_T3 :
        createOIT3(oiFitsFile);

        // OI_FLUX :
        createOIFlux(oiFitsFile);

    }

    //Creating methodes
    private static void createPrimaryHeader(OIFitsFile oiFitsFile) {
        final OIPrimaryHDU imageHDU = new OIPrimaryHDU();

        imageHDU.setOrigin("ESO");
        imageHDU.setDate("2017-10-04");
        imageHDU.setDateObs("2017-05-23");
        imageHDU.setContent(FitsConstants.KEYWORD_CONTENT_OIFITS2);
        imageHDU.setAuthor("Charleen");
        imageHDU.setTelescop("Etoilemagic");
        imageHDU.setInstrume("VEGA");
        imageHDU.setObserver("Gilles");
        imageHDU.setObject("MegaStar");
        imageHDU.setInsMode("Low_JHK");
        imageHDU.setReferenc("2017HDTYE.123.43.56");
        imageHDU.setProgId("074.R-456");
        imageHDU.setProcsoft("pndg 3.4");
        imageHDU.setObsTech("SCAN, PH_REF, ...");

        // todo: add optional keywords
        oiFitsFile.setPrimaryImageHdu(imageHDU);
    }

    private static void createOIArray(OIFitsFile oiFitsFile) {
        final OIArray oiArray = new OIArray(oiFitsFile, 3);

        oiArray.setArrName("Chara");
        oiArray.setFrame(OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC);
        oiArray.setArrayXYZ(1942042.8584924, -5455305.996911, -2654521.4011759);

        oiArray.setTelName(0, "UT1");
        oiArray.setStaName(0, "U1");
        oiArray.setStaIndex(0, 3);
        oiArray.setDiameter(0, 8f);
        oiArray.setStaXYZ(0, -0.73422599479242, -9.92488562146125, -22.03283353519204);
        oiArray.setFov(0, 89.345);
        oiArray.setFovType(0, OIFitsConstants.COLUMN_FOVTYPE_FWHM);

        oiArray.setTelName(1, "UT2");
        oiArray.setStaName(1, "U2");
        oiArray.setStaIndex(1, 4);
        oiArray.setDiameter(1, 8f);
        oiArray.setStaXYZ(1, 20.45018397209875, 14.88732843219187, 24.17944630588896);
        oiArray.setFov(1, 2.35);
        oiArray.setFovType(1, OIFitsConstants.COLUMN_FOVTYPE_FWHM);

        oiArray.setTelName(2, "UT3");
        oiArray.setStaName(2, "U3");
        oiArray.setStaIndex(2, 7);
        oiArray.setDiameter(2, 8f);
        oiArray.setStaXYZ(2, 35.32766648520568, 44.91458329169021, 56.61105628712381);
        oiArray.setFov(2, 12457.2);
        oiArray.setFovType(2, OIFitsConstants.COLUMN_FOVTYPE_RADIUS);

        oiFitsFile.addOiTable(oiArray);
    }

    private static void createOIWave(OIFitsFile oiFitsFile) {
        final int nWave = N_WAVE;
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
        target.getCategory()[0] = OIFitsConstants.COLUMN_CATEGORY_SCI;

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
        target.getCategory()[1] = OIFitsConstants.COLUMN_CATEGORY_SCI;

        oiFitsFile.addOiTable(target);
    }

    private static void createOICorr(OIFitsFile oiFitsFile) {

        /* magic number to fill properly the matrix (to be adjusted if ndata changes) */
        final int nRows = 37;
        final OICorr oicorr = new OICorr(oiFitsFile, nRows);

        oicorr.setCorrName("Full Correlation");
        oicorr.setNData(N_DATA);

        //Column
        final int[] iIndx = oicorr.getIindx();
        final int[] jIndx = oicorr.getJindx();
        final double[] corr = oicorr.getCorr();

        int k = 0;
        for (int i = 1; i < N_DATA && k < nRows; i += 5) {
            for (int j = i + 1; j < N_DATA && k < nRows; j += 3) {
                iIndx[k] = i;
                jIndx[k] = j;
                corr[k] = randFloat(0, 1);
                k++;
            }
        }
        oiFitsFile.addOiTable(oicorr);
    }

    private static void createOIInspol(OIFitsFile oiFitsFile) {

        final int nRows = 11;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                final OIInspol oiinspol = new OIInspol(oiFitsFile, arrname, nRows, N_WAVE);

                oiinspol.setNPol(3);
                oiinspol.setArrName(arrname);
                oiinspol.setOrient(OIFitsConstants.KEYWORD_ORIENT_LABORATORY);
                oiinspol.setModel("matrix");

                final int nWaveLengths = oiinspol.getNWave();
                // Columns :
                final short[] targetIds = oiinspol.getTargetId();
                final String[] insnames = oiinspol.getInsNames();
                final double[] mjdObs = oiinspol.getMJDObs();
                final double[] mjdEnd = oiinspol.getMJDEnd();
                final float[][][] jxx = oiinspol.getJXX();
                final float[][][] jyy = oiinspol.getJYY();
                final float[][][] jxy = oiinspol.getJXY();
                final float[][][] jyx = oiinspol.getJYX();
                final short[][] staIndexes = oiinspol.getStaIndex();

                for (int i = 0; i < nRows; i++) {

                    targetIds[i] = targetId;
                    insnames[i] = insname;
                    mjdObs[i] = 57930.0;
                    mjdEnd[i] = 57940.0;

                    for (int l = 0; l < nWaveLengths; l++) {
                        jxx[i][l][0] = randFloat(0, 5000);
                        jxx[i][l][1] = randFloat(0, 5000);
                        jyy[i][l][0] = randFloat(0, 365);
                        jyy[i][l][1] = randFloat(0, 365);
                        jxy[i][l][0] = randFloat(0, 841);
                        jxy[i][l][1] = randFloat(0, 841);
                        jyx[i][l][0] = randFloat(0, 123);
                        jyx[i][l][1] = randFloat(0, 123);
                    }

                    staIndexes[i][0] = oiarray_staIndexes[rand(nbStas)];
                }
                oiFitsFile.addOiTable(oiinspol);
            }
        }

    }

    private static void createOIVis(OIFitsFile oiFitsFile) {

        final int nRows = N_VIS;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                for (String corrname : oiFitsFile.getAcceptedCorrNames()) {

                    final OIVis vis = new OIVis(oiFitsFile, insname, nRows);

                    vis.setDateObs("2017-07-06");
                    vis.setArrName(arrname);
                    vis.setInsName(insname);
                    vis.setCorrName(corrname);
                    vis.setAmpTyp(OIFitsConstants.KEYWORD_AMPTYP_ABSOLUTE);
                    vis.setPhiTyp(OIFitsConstants.KEYWORD_PHITYP_DIFF);
                    vis.setAmpOrder(2145);
                    vis.setPhiOrder(748);

                    vis.setColumnUnit(OIFitsConstants.COLUMN_IVIS, "e");
                    vis.setColumnUnit(OIFitsConstants.COLUMN_IVISERR, "e");
                    vis.setColumnUnit(OIFitsConstants.COLUMN_RVIS, "e");
                    vis.setColumnUnit(OIFitsConstants.COLUMN_RVISERR, "e");
                    vis.setColumnUnit(OIFitsConstants.COLUMN_VISDATA, "e");
                    vis.setColumnUnit(OIFitsConstants.COLUMN_VISERR, "e");

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
                    final int[] corrindx_visamp = vis.getCorrIndxVisAmp();

                    final double[][] visPhi = vis.getVisPhi();
                    final double[][] visPhiErr = vis.getVisPhiErr();
                    final int[] corrindx_visphi = vis.getCorrIndxVisPhi();

                    final boolean[][][] visrefmap = vis.getVisRefMap();
                    final double[][] rvis = vis.getRVis();
                    final double[][] rviserr = vis.getRVisErr();
                    final int[] corrindx_rvis = vis.getCorrIndxRVis();
                    final double[][] ivis = vis.getIVis();
                    final double[][] iviserr = vis.getIVisErr();
                    final int[] corrindx_ivis = vis.getCorrIndxIVis();

                    final double[] uCoords = vis.getUCoord();
                    final double[] vCoords = vis.getVCoord();

                    final short[][] staIndexes = vis.getStaIndex();
                    final boolean[][] flags = vis.getFlag();

                    for (int i = 0; i < nRows; i++) {
                        targetIds[i] = targetId;

                        times[i] = 0;
                        mjds[i] = 57940.0;
                        intTimes[i] = 500.0;

                        // correlation stores by column blocks [visamp][visphi][rvis][ivis]
                        // each column block corresponds to [T(t=1) ... T(t=nvis)]
                        // each T(t=n) gives correlations for wavelengths [1..nwave]
                        corrindx_visamp[i] = I_VIS + (i * N_WAVE);
                        corrindx_visphi[i] = I_VIS + BLK_COL_VIS + (i * N_WAVE);
                        corrindx_rvis[i] = I_VIS + 2 * BLK_COL_VIS + (i * N_WAVE);
                        corrindx_ivis[i] = I_VIS + 3 * BLK_COL_VIS + (i * N_WAVE);

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

                            ivis[i][l] = randFloat(0, 241);
                            iviserr[i][l] = randFloat(0, 30);
                            rvis[i][l] = randFloat(0, 521);
                            rviserr[i][l] = randFloat(0, 10);

                            flags[i][l] = randBool();

                            for (int k = 0; k < nWaveLengths; k++) {
                                visrefmap[i][l][k] = randBool();
                            }
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
    }

    private static void createOIVis2(OIFitsFile oiFitsFile) {

        final int nRows = N_VIS2;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                for (String corrname : oiFitsFile.getAcceptedCorrNames()) {

                    final OIVis2 vis2 = new OIVis2(oiFitsFile, insname, nRows);
                    final int nWaveLengths = vis2.getNWave();

                    vis2.setDateObs("2017-07-06");
                    vis2.setArrName(arrname);
                    vis2.setInsName(insname);
                    vis2.setCorrName(corrname);

                    //Column
                    final short[] targetIds = vis2.getTargetId();
                    final double[] times = vis2.getTime();
                    final double[] mjds = vis2.getMJD();
                    final double[] intTimes = vis2.getIntTime();

                    final double[][] vis2Data = vis2.getVis2Data();
                    final double[][] vis2Err = vis2.getVis2Err();
                    final int[] corrindx_vis2data = vis2.getCorrIndxVisData();

                    final double[] uCoords = vis2.getUCoord();
                    final double[] vCoords = vis2.getVCoord();

                    final short[][] staIndexes = vis2.getStaIndex();

                    final boolean[][] flags = vis2.getFlag();

                    for (int i = 0; i < nRows; i++) {
                        targetIds[i] = targetId;

                        times[i] = 0;
                        mjds[i] = 57940.0;
                        intTimes[i] = 500.0;

                        corrindx_vis2data[i] = I_VIS2 + (i * N_WAVE);

                        // Iterate on wave lengths :
                        for (int l = 0; l < nWaveLengths; l++) {
                            vis2Data[i][l] = randFloat(0, 5000);
                            vis2Err[i][l] = randFloat(0, 20);

                            flags[i][l] = randBool();
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
    }

    private static void createOIT3(OIFitsFile oiFitsFile) {

        final int nRows = N_T3;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                for (String corrname : oiFitsFile.getAcceptedCorrNames()) {

                    final OIT3 t3 = new OIT3(oiFitsFile, insname, nRows);
                    final int nWaveLengths = t3.getNWave();

                    t3.setDateObs("2017-07-06");
                    t3.setArrName(arrname);
                    t3.setInsName(insname);
                    t3.setCorrName(corrname);

                    //Column
                    final short[] targetIds = t3.getTargetId();
                    final double[] times = t3.getTime();
                    final double[] mjds = t3.getMJD();
                    final double[] intTimes = t3.getIntTime();

                    final double[][] t3amp = t3.getT3Amp();
                    final double[][] t3amperr = t3.getT3AmpErr();
                    final int[] corrindx_t3amp = t3.getCorrIndxT3Amp();
                    final double[][] t3phi = t3.getT3Phi();
                    final double[][] t3phierr = t3.getT3PhiErr();
                    final int[] corrindx_t3phi = t3.getCorrIndxT3Phi();

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

                        corrindx_t3amp[i] = I_T3 + (i * N_WAVE);
                        corrindx_t3phi[i] = I_T3 + BLK_COL_T3 + (i * N_WAVE);

                        // Iterate on wave lengths :
                        for (int l = 0; l < nWaveLengths; l++) {
                            t3amp[i][l] = randFloat(0, 5000);
                            t3amperr[i][l] = randFloat(0, 20);

                            t3phi[i][l] = randFloat(0, 5000);
                            t3phierr[i][l] = randFloat(0, 20);

                            flags[i][l] = randBool();
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
    }

    private static void createOIFlux(OIFitsFile oiFitsFile) {

        final int nRows = N_FLUX;

        // Get needed infos:
        final short[] oiTarget_targetIds = oiFitsFile.getOiTarget().getTargetId();

        final short targetId = oiTarget_targetIds[0];

        for (String arrname : oiFitsFile.getAcceptedArrNames()) {
            final short[] oiarray_staIndexes = oiFitsFile.getOiArray(arrname).getStaIndex();
            final int nbStas = oiarray_staIndexes.length;

            for (String insname : oiFitsFile.getAcceptedInsNames()) {

                for (String corrname : oiFitsFile.getAcceptedCorrNames()) {

                    final OIFlux flux = new OIFlux(oiFitsFile, insname, nRows);
                    final int nWaveLengths = flux.getNWave();

                    flux.setDateObs("2017-07-06");
                    flux.setArrName(arrname);
                    flux.setInsName(insname);
                    flux.setCorrName(corrname);
                    flux.setFov(23.5);
                    flux.setFovType(OIFitsConstants.COLUMN_FOVTYPE_FWHM);
                    flux.setCalStat(OIFitsConstants.KEYWORD_CALSTAT_C);

                    flux.setColumnUnit(OIFitsConstants.COLUMN_FLUXDATA, "Jy");
                    flux.setColumnUnit(OIFitsConstants.COLUMN_FLUXERR, "Jy");

                    //Column
                    final short[] targetIds = flux.getTargetId();
                    final double[] mjds = flux.getMJD();
                    final double[] intTimes = flux.getIntTime();

                    final double[][] fluxdata = flux.getFluxData();
                    final double[][] fluxerr = flux.getFluxErr();
                    final int[] corrindx_fluxdata = flux.getCorrIndxData();

                    final short[][] staIndexes = flux.getStaIndex();

                    final boolean[][] flags = flux.getFlag();

                    for (int i = 0; i < nRows; i++) {
                        targetIds[i] = targetId;

                        mjds[i] = 57940.0;
                        intTimes[i] = 500.0;

                        corrindx_fluxdata[i] = I_FLUX + (i * N_WAVE);

                        // Iterate on wave lengths :
                        for (int l = 0; l < nWaveLengths; l++) {

                            fluxdata[i][l] = randFloat(10, 300);
                            fluxerr[i][l] = randFloat(5, 20);

                            flags[i][l] = randBool();
                        }

                        staIndexes[i][0] = oiarray_staIndexes[rand(nbStas)];
                    }

                    oiFitsFile.addOiTable(flux);
                }
            }
        }
    }

    private static int rand(final int max) {
        return (int) (max * RANDOM.nextDouble());
    }

    private static float randFloat(final float max, final float min) {
        return (float) ((max - min) * RANDOM.nextDouble() + min);
    }

    private static boolean randBool() {
        return RANDOM.nextDouble() > 0.5;
    }
}
