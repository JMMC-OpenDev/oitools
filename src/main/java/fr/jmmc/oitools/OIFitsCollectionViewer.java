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

import fr.jmmc.oitools.model.CsvOutputVisitor;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIT3;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIVis2;
import fr.jmmc.oitools.model.OutputVisitor;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.TargetIdMatcher;
import fr.jmmc.oitools.model.TargetManager;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This utility class loads OIFits files given as arguments
 * and print their XML or simple CSV description in the system out stream
 * @author bourgesl, mella
 */
public final class OIFitsCollectionViewer {

    private OIFitsCollectionViewer() {
        super();
    }

    public static void process(final OIFitsCollection oiFitsCollection) {
        final CsvOutputVisitor out = new CsvOutputVisitor(false);

        out.enterMetadata();
        targetMetadata(oiFitsCollection, out);
        out.exitMetadata();

        OIFitsCommand.info(out.toString());
    }

    public static void targetMetadata(final OIFitsFile oiFitsFile, final OutputVisitor out) {
        targetMetadata(OIFitsCollection.create(oiFitsFile), out);
    }

    public static void targetMetadata(final OIFitsCollection oiFitsCollection, final OutputVisitor out) {

        final List<Granule> granules = oiFitsCollection.getSortedGranules();

        final Map<Granule, Set<OIData>> oiDataPerGranule = oiFitsCollection.getOiDataPerGranule();

        final TargetManager tm = oiFitsCollection.getTargetManager();

        for (Granule granule : granules) {
            final Target target = granule.getTarget();
            // Target info
            final String targetName = target.getTarget(); // global UID
            final double targetRa = target.getRaEp0();
            final double targetDec = target.getDecEp0();

            final InstrumentMode insMode = granule.getInsMode();
            // OIWavelength info
            final String insName = insMode.getInsName(); // global UID
            final float minWavelength = insMode.getLambdaMin();
            final float maxWavelength = insMode.getLambdaMax();
            final int nbChannels = insMode.getNbChannels();
            // Resolution = lambda / delta_lambda
            final float resPower = insMode.getResPower();

            // night
            final double nightId = granule.getNight().getNightId();

            final Set<OIData> oiDatas = oiDataPerGranule.get(granule);
            if (oiDatas != null) {
                // Statistics per granule:
                int nbVis = 0, nbVis2 = 0, nbT3 = 0;
                double tMin = Double.POSITIVE_INFINITY, tMax = Double.NEGATIVE_INFINITY;
                double intTime = Double.POSITIVE_INFINITY;
                String facilityName = "";

                for (OIData oiData : oiDatas) {
                    final TargetIdMatcher targetIdMatcher = oiData.getTargetIdMatcher(tm, target);

                    if (targetIdMatcher != null) {
                        /* one oiData table, search for target by targetid (and nightid) */
                        final short[] targetIds = oiData.getTargetId();
                        final double[] nightIds = oiData.getNightId();
                        final double[] mjds = oiData.getMJD();
                        final double[] intTimes = oiData.getIntTime();

                        for (int i = 0; i < targetIds.length; i++) {
                            // same target and same night:
                            if (targetIdMatcher.match(targetIds[i]) && (nightId == nightIds[i])) {
                                // TODO: count flag? what to do with flagged measures?
                                // TODO: check for NaN values ?
                                // number of rows in data tables:
                                if (oiData instanceof OIVis) {
                                    nbVis += 1;
                                } else if (oiData instanceof OIVis2) {
                                    nbVis2 += 1;
                                } else if (oiData instanceof OIT3) {
                                    nbT3 += 1;
                                }
                                // TODO: add OIFlux ?

                                /* search for minimal and maximal MJD for target */
 /* TODO: make use of DATE-OBS+TIME[idx] if no MJD */
                                final double mjd = mjds[i];
                                if (mjd < tMin) {
                                    tMin = mjd;
                                }
                                if (mjd > tMax) {
                                    tMax = mjd;
                                }

                                /* search for minimal (?) INT_TIME for target */
                                final double t = intTimes[i];
                                if (t < intTime) {
                                    intTime = t;
                                }
                            }
                        }

                        if (facilityName.isEmpty() && oiData.getArrName() != null) {
                            facilityName = oiData.getArrName(); // potential multiple ARRNAME values !
                        }
                    }
                }

                out.appendMetadataRecord(targetName, targetRa, targetDec,
                        intTime, tMin, tMax,
                        resPower, minWavelength, maxWavelength,
                        facilityName, insName,
                        nbVis, nbVis2, nbT3, nbChannels);
            }
        }
    }
}
