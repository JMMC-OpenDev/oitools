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
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsViewer;

/**
 * This visitor implementation produces an CSV output of the OIFits file structure
 * @author bourgesl, mella
 */
public final class CsvOutputVisitor implements ModelVisitor {

    /* constants */
    private final static String SEP = "\t";

    /* members */
    /** flag to enable/disable the verbose output */
    private boolean verbose;
    /** internal buffer */
    private StringBuilder buffer;

    /**
     * Return one CSV string with complete OIFitsFile information
     * @param oiFitsFile OIFitsFile model to process
     * @param verbose if true the result will contain the table content
     * @return the CSV description
     */
    public static String getCsvDesc(final OIFitsFile oiFitsFile, final boolean verbose) {
        final CsvOutputVisitor csvSerializer = new CsvOutputVisitor(verbose);
        oiFitsFile.accept(csvSerializer);
        return csvSerializer.toString();
    }

    /**
     * Return one CSV string with OITable information
     * @param oiTable OITable model to process
     * @param verbose if true the result will contain the table content
     * @return the CSV description
     */
    public static String getCsvDesc(final OITable oiTable, final boolean verbose) {
        final CsvOutputVisitor csvSerializer = new CsvOutputVisitor(verbose);
        oiTable.accept(csvSerializer);
        return csvSerializer.toString();
    }

    /**
     * Create a new CsvOutputVisitor using default options (not verbose and no formatter used)
     */
    public CsvOutputVisitor() {
        this(false);
    }

    /**
     * Create a new CsvOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param verbose if true the result will contain the table content
     */
    public CsvOutputVisitor(final boolean verbose) {
        this.verbose = verbose;

        // allocate buffer size (32K or 128K):
        this.buffer = new StringBuilder(4 * 1024);
    }

    /**
     * Return the flag to enable/disable the verbose output
     * @return flag to enable/disable the verbose output
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Define the flag to enable/disable the verbose output
     * @param verbose flag to enable/disable the verbose output
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Clear the internal buffer for later reuse
     */
    public void reset() {
        // recycle buffer :
        this.buffer.setLength(0);
    }

    /**
     * Return the buffer content as a string
     * @return buffer content
     */
    @Override
    public String toString() {
        final String result = this.buffer.toString();

        // reset the buffer content
        reset();

        return result;
    }

    /**
     * Process the given OIFitsFile element with this visitor implementation :
     * fill the internal buffer with file information
     * @param oiFitsFile OIFitsFile element to visit
     */
    @Override
    public void visit(final OIFitsFile oiFitsFile) {

        enterOIFitsFile(oiFitsFile);

        // targets
        final OITarget oiTarget = oiFitsFile.getOiTarget();
        if (oiTarget != null) {
            oiTarget.accept(this);
            printMetadata(oiFitsFile);
        }

        exitOIFitsFile();
    }

    /**
     * Process the given OITable element with this visitor implementation :
     * csv don't care the content
     * @param oiTable OITable element to visit
     */
    @Override
    public void visit(final OITable oiTable) {
        // no op
    }

    /**
     * Open the oifits tag with OIFitsFile description
     * @param oiFitsFile OIFitsFile to get its description (file name)
     */
    private void enterOIFitsFile(final OIFitsFile oiFitsFile) {

        if (isVerbose() && oiFitsFile != null && oiFitsFile.getAbsoluteFilePath() != null) {
            this.buffer.append("# filename       ").append((oiFitsFile.getSourceURI() != null)
                    ? oiFitsFile.getSourceURI().toString() : oiFitsFile.getAbsoluteFilePath()).append('\n');
            this.buffer.append("# local_filename ").append(oiFitsFile.getAbsoluteFilePath()).append('\n');
        }
    }

    /**
     * Close the oifits tag
     */
    private void exitOIFitsFile() {
        // no op
    }

    private void printMetadata(final OIFitsFile oiFitsFile) {
        /* analyze structure of file to browse by target */
        oiFitsFile.analyze();
        appendHeader(this.buffer, SEP);
        if (oiFitsFile.hasOiTarget()) {
            for (int i = 0; i < oiFitsFile.getOiTarget().getNbTargets(); i++) {
                this.buffer.append(OIFitsViewer.targetMetadata(oiFitsFile, i, false));
            }
        }
    }

    public static void appendHeader(StringBuilder buffer, String sep) {
        // respect the same order has the one provided in the appendCsvRecord
        buffer.append("target_name").append(sep)
                .append("s_ra").append(sep)
                .append("s_dec").append(sep)
                .append("t_exptime").append(sep)
                .append("t_min").append(sep)
                .append("t_max").append(sep)
                .append("em_res_power").append(sep)
                .append("em_min").append(sep)
                .append("em_max").append(sep)
                .append("facility_name").append(sep)
                .append("instrument_name").append(sep)
                .append("nb_vis").append(sep)
                .append("nb_vis2").append(sep)
                .append("nb_t3").append(sep)
                .append("nb_channels").append(sep)
                .append('\n');
    }

    public static void appendRecord(final StringBuilder buffer, final String targetName, final double targetRa, final double targetDec, double intTime, double tMin, double tMax, float resPower, float minWavelength, float maxWavelength, String facilityName, final String insName, int nbVis, int nbVis2, int nbT3, int nbChannels) {
        buffer.append(targetName).append(SEP)
                .append(targetRa).append(SEP)
                .append(targetDec).append(SEP)
                .append(intTime).append(SEP)
                .append(tMin).append(SEP)
                .append(tMax).append(SEP)
                .append(resPower).append(SEP)
                .append(minWavelength).append(SEP)
                .append(maxWavelength).append(SEP)
                .append(facilityName).append(SEP)
                .append(insName).append(SEP)
                .append(nbVis).append(SEP)
                .append(nbVis2).append(SEP)
                .append(nbT3).append(SEP)
                .append(nbChannels).append(SEP)
                .append('\n');
    }

}
