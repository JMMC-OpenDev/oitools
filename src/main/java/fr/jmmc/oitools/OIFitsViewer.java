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
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIT3;
import fr.jmmc.oitools.model.OITarget;
import fr.jmmc.oitools.model.OIVis;
import fr.jmmc.oitools.model.OIVis2;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This command line program loads OIFits files given as arguments
 * and print their XML or simple CSV description in the system out stream
 * @author bourgesl, mella
 */
public final class OIFitsViewer extends OIFitsCommand {

    /* members */
    /** internal OIFits checker */
    private final OIFitsChecker checker;
    /** internal XML serializer (null if XML output disabled) */
    private final XmlOutputVisitor xmlSerializer;
    /** internal TSV serializer (null if TSV output disabled) */
    private final CsvOutputVisitor tsvSerializer;

    /**
     * Creates a new OifitsViewer object with default options.
     */
    public OIFitsViewer() {
        this(false, false);
    }

    /**
     * Creates a new OifitsViewer object.
     *
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public OIFitsViewer(final boolean format, final boolean verbose) {
        this(true, false, format, verbose);
    }

    /**
     * Creates a new OifitsViewer object.
     *
     * @param doXmlOutput enable/disable XML output
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public OIFitsViewer(final boolean doXmlOutput, final boolean format, final boolean verbose) {
        this(doXmlOutput, false, format, verbose);
    }

    /**
     * Creates a new OifitsViewer object.
     *
     * @param doXmlOutput enable/disable XML output
     * @param doCsvOutput enable/disable Csv output
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public OIFitsViewer(final boolean doXmlOutput, final boolean doCsvOutput, final boolean format, final boolean verbose) {
        this.checker = new OIFitsChecker();
        this.xmlSerializer = (doXmlOutput) ? new XmlOutputVisitor(format, verbose, this.checker) : null;
        this.tsvSerializer = (doCsvOutput) ? new CsvOutputVisitor(verbose) : null;
    }

    /**
     * Process the given file
     *
     * @param fileLocation absolute File Path or URL (file:// or http://)
     * @return serializer output or null if undefined
     * @throws FitsException excpetion thrown during fits reading
     * @throws IOException exception thrown on file reading error
     */
    public String process(final String fileLocation) throws IOException, FitsException {
        // Load file
        final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(this.checker, fileLocation, true);

        String output = null;
        if (this.xmlSerializer != null) {
            oiFitsFile.accept(this.xmlSerializer);
            output = this.xmlSerializer.toString();
        }

        if (this.tsvSerializer != null) {
            oiFitsFile.accept(this.tsvSerializer);
            output = this.tsvSerializer.toString();
        }

        if (this.checker != null) {
            // check mode:
            if (output == null) {
                output = this.checker.getCheckReport();
            }
            // clean the checker before processing any other files
            this.checker.clearCheckReport();
        }

        return output;
    }

    public static StringBuilder targetMetadata(final OIFitsFile oiFitsFile, final int index, final boolean xml) {
        final StringBuilder sb = new StringBuilder(1024);

        final OITarget oiTarget = oiFitsFile.getOiTarget();

        if (oiTarget != null) {
            final short targetId = oiTarget.getTargetId()[index];
            final String targetName = oiTarget.getTarget()[index];
            final double targetRa = oiTarget.getRaEp0()[index];
            final double targetDec = oiTarget.getDecEp0()[index];

            String[] insNames = oiFitsFile.getAcceptedInsNames();
            Arrays.sort(insNames);

            for (final String insName : insNames) {
                /* data from the OIWavelength */
                final OIWavelength oiWavelength = oiFitsFile.getOiWavelength(insName);
                if (oiWavelength != null) {
                    final float minWavelength = oiWavelength.getEffWaveMin();
                    final float maxWavelength = oiWavelength.getEffWaveMax();
                    final int nbChannels = oiWavelength.getNWave();

                    // Resolution = lambda / delta_lambda
                    final float resPower = oiWavelength.getResolution();

                    // TODO: use Granule here ?
                    // TODO: move such algo into Analyzer (shared)
                    /* build a list of different night ids for the couple (target, insname) ie per granule */
                    final Map<Double, Set<OIData>> oiDataPerNightId = new LinkedHashMap<Double, Set<OIData>>();
                    for (final OIData oiData : oiFitsFile.getOiDataList()) {
                        // same INSNAME (ie instrument mode):
                        // TODO: fuzzy matcher (wavelengths)
                        if (oiData.getInsName().equals(insName)) {
                            final short[] targetIds = oiData.getTargetId();
                            final double[] nightIds = oiData.getNightId();

                            for (int i = 0; i < targetIds.length; i++) {
                                // same target:
                                /* TODO: target aliases (check coordinates): use target UID ? */
                                if (targetIds[i] == targetId) {
                                    final double nightId = nightIds[i];
                                    Set<OIData> tables = oiDataPerNightId.get(nightId);
                                    if (tables == null) {
                                        tables = new LinkedHashSet<OIData>();
                                        oiDataPerNightId.put(nightId, tables);
                                    }
                                    tables.add(oiData);
                                }
                            }
                        }
                    }

                    // Statistics per granule:
                    for (Map.Entry<Double, Set<OIData>> entry : oiDataPerNightId.entrySet()) {
                        final double nightId = entry.getKey();
                        final Set<OIData> oiDataTables = entry.getValue();

                        int nbVis = 0, nbVis2 = 0, nbT3 = 0;
                        double tMin = Double.POSITIVE_INFINITY, tMax = Double.NEGATIVE_INFINITY;
                        double intTime = Double.POSITIVE_INFINITY;
                        String facilityName = "";

                        for (OIData oiData : oiDataTables) {
                            /* one oiData table, search for target by targetid (and nightid) */
                            final short[] targetIds = oiData.getTargetId();
                            final double[] nightIds = oiData.getNightId();
                            final double[] mjds = oiData.getMJD();
                            final double[] intTimes = oiData.getIntTime();

                            for (int i = 0; i < targetIds.length; i++) {
                                // same target and same night:
                                if ((targetIds[i] == targetId) && (nightIds[i] == nightId)) {
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
                                facilityName = oiData.getArrName();
                            }
                        }

                        if (xml) {
                            XmlOutputVisitor.appendRecord(sb, targetName, targetRa,
                                    targetDec, intTime, tMin, tMax, resPower,
                                    minWavelength, maxWavelength, facilityName,
                                    insName, nbVis, nbVis2, nbT3, nbChannels);
                        } else {
                            CsvOutputVisitor.appendRecord(sb, targetName, targetRa,
                                    targetDec, intTime, tMin, tMax, resPower,
                                    minWavelength, maxWavelength, facilityName,
                                    insName, nbVis, nbVis2, nbT3, nbChannels);
                        }
                    }
                }
            }
        }
        return sb;
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        boolean quiet = true;
        boolean format = false;
        boolean verbose = false;
        boolean tsv = false;
        boolean xml = true;

        final List<String> fileLocations = new ArrayList<String>(args.length);

        // parse command line arguments :
        for (final String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.equals("-t") || arg.equals("-tsv")) {
                    xml = false;
                    tsv = true;
                } else if (arg.equals("-f") || arg.equals("-format")) {
                    format = true;
                } else if (arg.equals("-v") || arg.equals("-verbose")) {
                    verbose = true;
                } else if (arg.equals("-c") || arg.equals("-check")) {
                    xml = false;
                } else if (arg.equals("-l") || arg.equals("-log")) {
                    quiet = false;
                } else if (arg.equals("-h") || arg.equals("-help")) {
                    showArgumentsHelp();
                    System.exit(0);
                } else {
                    errorArg("'" + arg + "' option not supported.");
                }
            } else {
                fileLocations.add(arg);
            }
        }

        // Initialization:
        bootstrap(quiet);

        if (fileLocations.isEmpty()) {
            errorArg("No file location given in arguments.");
        }

        process(xml, tsv, format, verbose, fileLocations);
    }

    public static void process(final boolean xml, final boolean tsv, final boolean format, final boolean verbose,
                               final List<String> fileLocations) {

        final OIFitsViewer viewer = new OIFitsViewer(xml, tsv, format, verbose);

        if (xml) {
            info("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<oifits_list>");
        }
        for (String fileLocation : fileLocations) {
            try {
                info(viewer.process(fileLocation));
            } catch (Exception e) {
                error("Error reading file '" + fileLocation + "'", e);
            }
        }
        if (xml) {
            info("</oifits_list>");
        }
    }

    /**
     * Print an error message when parsing the command line arguments
     *
     * @param message message to print
     */
    protected static void errorArg(final String message) {
        error(message);
        showArgumentsHelp();
        System.exit(1);
    }

    /** Show command arguments help */
    protected static void showArgumentsHelp() {
        info("-------------------------------------------------------------------------");
        info("Usage: " + OIFitsViewer.class.getName() + " [-f|-format] [-v|-verbose] [-t|-tsv] <file locations>");
        info("------------- Arguments help --------------------------------------------");
        info("| Key          Value           Description                              |");
        info("|-----------------------------------------------------------------------|");
        info("| [-h|-help]                   Show arguments help                      |");
        info("| [-l] or [-log]               Enable logging (quiet by default)        |");
        info("| [-f] or [-format]            Use the number formatter                 |");
        info("| [-v] or [-verbose]           Dump all column data                     |");
        info("| [-t] or [-tsv]               Dump object table in tsv format          |");
        info("| [-c] or [-check]             Check only given file(s)                 |");
        info("-------------------------------------------------------------------------");
    }

}
