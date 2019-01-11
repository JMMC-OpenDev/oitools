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

/**
 * This visitor implementation produces an CSV output of the OIFits file structure
 * @author bourgesl, mella
 */
public final class CsvOutputVisitor extends OutputVisitor {

    /* constants */
    private final static String SEP = "\t";

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
        this(TargetMetadataProvider.OIFITS_METADATA, verbose);
    }

    /**
     * Create a new CsvOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param metadataProvider target metadata provider
     * @param verbose if true the result will contain the table content
     */
    public CsvOutputVisitor(final TargetMetadataProvider metadataProvider, final boolean verbose) {
        super(metadataProvider, verbose, 4 * 1024);
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
    @Override
    protected void enterOIFitsFile(final OIFitsFile oiFitsFile) {
        if (isVerbose() && oiFitsFile != null && oiFitsFile.getAbsoluteFilePath() != null) {
            this.buffer.append("# filename       ").append((oiFitsFile.getSourceURI() != null)
                    ? oiFitsFile.getSourceURI().toString() : oiFitsFile.getAbsoluteFilePath()).append('\n');
            this.buffer.append("# local_filename ").append(oiFitsFile.getAbsoluteFilePath()).append('\n');
        }
    }

    /**
     * Close the oifits tag
     */
    @Override
    protected void exitOIFitsFile() {
        // no op
    }

    @Override
    public void enterMetadata() {
        // respect the same order has the one provided in the appendRecord
        this.buffer.append("target_name").append(SEP)
                .append("s_ra").append(SEP)
                .append("s_dec").append(SEP)
                .append("t_exptime").append(SEP)
                .append("t_min").append(SEP)
                .append("t_max").append(SEP)
                .append("em_res_power").append(SEP)
                .append("em_min").append(SEP)
                .append("em_max").append(SEP)
                .append("facility_name").append(SEP)
                .append("instrument_name").append(SEP)
                .append("nb_vis").append(SEP)
                .append("nb_vis2").append(SEP)
                .append("nb_t3").append(SEP)
                .append("nb_channels").append(SEP)
                .append('\n');
    }

    @Override
    public void exitMetadata() {
        // no op
    }

    @Override
    public void appendMetadataRecord(final String targetName, final double targetRa, final double targetDec,
                                     double intTime, double tMin, double tMax,
                                     float resPower, float minWavelength, float maxWavelength,
                                     String facilityName, final String insName,
                                     int nbVis, int nbVis2, int nbT3, int nbChannels) {
        this.buffer.append(targetName).append(SEP)
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
