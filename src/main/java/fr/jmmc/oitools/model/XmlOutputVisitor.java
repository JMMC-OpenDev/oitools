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
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This visitor implementation produces an XML output of the OIFits file structure
 * @author bourgesl, mella
 */
public final class XmlOutputVisitor implements ModelVisitor {

    /* constants */
    /** US number format symbols */
    private final static DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    /** beautifier number formatter for standard values > 1e-2 and < 1e7 */
    private final static NumberFormat DF_BEAUTY_STD = new DecimalFormat("#0.###", US_SYMBOLS);
    /** beautifier number formatter for other values */
    private final static NumberFormat DF_BEAUTY_SCI = new DecimalFormat("0.###E0", US_SYMBOLS);
    /** regexp expression to SGML entities */
    private final static Pattern PATTERN_AMP = Pattern.compile("&");
    /** regexp expression to start tag */
    private final static Pattern PATTERN_LT = Pattern.compile("<");
    /** regexp expression to end tag */
    private final static Pattern PATTERN_GT = Pattern.compile(">");
    /* members */
    /** flag to enable/disable the number formatter */
    private boolean format;
    /** flag to enable/disable the verbose output */
    private boolean verbose;
    /** internal buffer */
    private StringBuilder buffer;
    /** checker used to store checking messages */
    private final OIFitsChecker checker;

    /**
     * Return one XML string with complete OIFitsFile information
     * @param oiFitsFile OIFitsFile model to process
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean verbose) {
        return getXmlDesc(oiFitsFile, false, verbose);
    }

    /**
     * Return one XML string with complete OIFitsFile information
     * @param oiFitsFile OIFitsFile model to process
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean format, final boolean verbose) {
        final XmlOutputVisitor xmlSerializer = new XmlOutputVisitor(format, verbose);
        oiFitsFile.accept(xmlSerializer);
        return xmlSerializer.toString();
    }

    /**
     * Return one XML string with OITable information
     * @param oiTable OITable model to process
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OITable oiTable, final boolean verbose) {
        return getXmlDesc(oiTable, false, verbose);
    }

    /**
     * Return one XML string with OITable information
     * @param oiTable OITable model to process
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OITable oiTable, final boolean format, final boolean verbose) {
        final XmlOutputVisitor xmlSerializer = new XmlOutputVisitor(format, verbose);
        oiTable.accept(xmlSerializer);
        return xmlSerializer.toString();
    }

    /**
     * Create a new XmlOutputVisitor using default options (not verbose and no formatter used)
     */
    public XmlOutputVisitor() {
        this(false, false);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param verbose if true the result will contain the table content
     */
    public XmlOutputVisitor(final boolean verbose) {
        this(false, verbose);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public XmlOutputVisitor(final boolean format, final boolean verbose) {
        this(format, verbose, null);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @param checker optional OIFitsChecker to dump its report
     */
    public XmlOutputVisitor(final boolean format, final boolean verbose, final OIFitsChecker checker) {
        this.format = format;
        this.verbose = verbose;
        this.checker = checker;

        // allocate buffer size (32K or 128K):
        this.buffer = new StringBuilder(((verbose) ? 128 : 32) * 1024);
    }

    /**
     * Return the flag to enable/disable the number formatter
     * @return flag to enable/disable the number formatter
     */
    public boolean isFormat() {
        return format;
    }

    /**
     * Define the flag to enable/disable the number formatter
     * @param format flag to enable/disable the number formatter
     */
    public void setFormat(final boolean format) {
        this.format = format;
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

        // force verbosity to true for OIArray / OIWaveLength tables (to dump their data):
        final boolean verbosity = this.verbose;
        this.verbose = true;

        String[] strings;

        // targets
        final OITarget oiTarget = oiFitsFile.getOiTarget();
        if (oiTarget != null) {
            oiTarget.accept(this);
            printMetadata(oiFitsFile);
        }

        // Sort arrnames
        strings = oiFitsFile.getAcceptedArrNames();
        Arrays.sort(strings);

        for (int i = 0, len = strings.length; i < len; i++) {
            final OITable oiTable = oiFitsFile.getOiArray(strings[i]);
            if (oiTable != null) {
                oiTable.accept(this);
            }
        }

        // Sort insnames
        strings = oiFitsFile.getAcceptedInsNames();
        Arrays.sort(strings);

        for (int i = 0, len = strings.length; i < len; i++) {
            final OITable oiTable = oiFitsFile.getOiWavelength(strings[i]);
            if (oiTable != null) {
                oiTable.accept(this);
            }
        }

        // Sort corrname
        strings = oiFitsFile.getAcceptedCorrNames();
        Arrays.sort(strings);

        for (int i = 0, len = strings.length; i < len; i++) {
            final OITable oiTable = oiFitsFile.getOiCorr(strings[i]);
            if (oiTable != null) {
                oiTable.accept(this);
            }
        }

        // OIInspol:
        if (oiFitsFile.hasOiInspol()) {
            for (final OIInspol oiInspol : oiFitsFile.getOiInspol()) {
                oiInspol.accept(this);
            }
        }

        // restore verbosity :
        this.verbose = verbosity;

        // data tables
        for (final OIData oiData : oiFitsFile.getOiDataList()) {
            oiData.accept(this);
        }

        // report check message if the checker is defined
        if (checker != null) {
            /* TODO: remove once OiDB / OIFitsViewer consumers use the new xml format */
            this.buffer.append("<checkReport>\n").append(encodeTagContent(checker.getCheckReport())).append("\n</checkReport>\n");

            // note: suppose there is no encoding issues (illegal xml characters)
            checker.writeRulesUsedByFailures(this.buffer);
            checker.getFailuresAsXML(this.buffer);
        }

        exitOIFitsFile();
    }

    /**
     * Open the oifits tag with OIFitsFile description
     * @param oiFitsFile OIFitsFile to get its description (file name)
     */
    private void enterOIFitsFile(final OIFitsFile oiFitsFile) {
        this.buffer.append("<oifits>\n");

        if (oiFitsFile != null && oiFitsFile.getAbsoluteFilePath() != null) {
            this.buffer.append("<version>").append(oiFitsFile.getVersion()).append("</version>\n");
            this.buffer.append("<filename>").append(encodeTagContent(
                    (oiFitsFile.getSourceURI() != null)
                    ? oiFitsFile.getSourceURI().toString() : oiFitsFile.getAbsoluteFilePath())).append("</filename>\n");
            this.buffer.append("<local_filename>").append(encodeTagContent(oiFitsFile.getAbsoluteFilePath())).append("</local_filename>\n");
            this.buffer.append("<size>").append(oiFitsFile.getSize()).append("</size>\n");

            if (oiFitsFile.getMd5sum() != null) {
                this.buffer.append("<md5sum>").append(oiFitsFile.getMd5sum()).append("</md5sum>\n");
            }

            if (oiFitsFile.getPrimaryImageHDU() != null) {
                this.buffer.append("<hdu>\n");
                final List<FitsHeaderCard> headerCards = oiFitsFile.getPrimaryImageHDU().getHeaderCards();
                if (!headerCards.isEmpty()) {
                    this.buffer.append("<keywords>\n");
                    for (FitsHeaderCard headerCard : headerCards) {
                        final String key = headerCard.getKey();
                        if (!FitsUtils.isStandardKeyword(key)) {
                            dumpFitsHeaderCard(headerCard);
                        }
                    }
                    this.buffer.append("</keywords>\n");
                }
                this.buffer.append("</hdu>\n");
            }
        }
    }

    /**
     * Close the oifits tag
     */
    private void exitOIFitsFile() {
        this.buffer.append("</oifits>\n");
    }

    /**
     * Process the given OITable element with this visitor implementation :
     * fill the internal buffer with table information
     * @param oiTable OITable element to visit
     */
    @Override
    public void visit(final OITable oiTable) {

        final boolean doOIFitsFile = (this.buffer.length() == 0);

        if (doOIFitsFile) {
            enterOIFitsFile(oiTable.getOIFitsFile());
        }

        this.buffer.append('<').append(oiTable.getExtName()).append(">\n");

        // Print keywords
        this.buffer.append("<keywords>\n");

        Object val;
        for (final KeywordMeta keyword : oiTable.getKeywordDescCollection()) {
            val = oiTable.getKeywordValue(keyword.getName());
            // skip missing keywords :
            if (val != null) {
                this.buffer.append("<keyword><name>").append(keyword.getName()).append("</name><value>").append(val);
                this.buffer.append("</value><description>").append(encodeTagContent(keyword.getDescription())).append("</description><type>");
                this.buffer.append(keyword.getType()).append("</type><unit>").append(keyword.getUnit()).append("</unit></keyword>\n");
            }
        }
        // Extra keywords:
        if (oiTable.hasHeaderCards()) {
            for (final FitsHeaderCard headerCard : oiTable.getHeaderCards()) {
                dumpFitsHeaderCard(headerCard);
            }
        }
        this.buffer.append("</keywords>\n");

        // Print nb of rows
        this.buffer.append("<rows>").append(oiTable.getNbRows()).append("</rows>");

        // Print columns
        this.buffer.append("<columns>\n");

        final Collection<ColumnMeta> columnsDescCollection = oiTable.getColumnDescCollection();
        for (ColumnMeta column : columnsDescCollection) {
            if (oiTable.hasColumn(column)) {
                this.buffer.append("<column><name>").append(column.getName()).append("</name>");
                this.buffer.append("<description>").append(encodeTagContent(column.getDescription())).append("</description>");
                this.buffer.append("<type>").append(column.getType()).append("</type>");
                this.buffer.append("<unit>").append(column.getUnit()).append("</unit>");

                Object range = oiTable.getMinMaxColumnValue(column.getName());
                if (range != null) {

                    switch (column.getDataType()) {
                        case TYPE_CHAR:
                            // Not Applicable
                            break;

                        case TYPE_SHORT:
                            short[] srange = (short[]) range;
                            this.buffer.append("<min>").append(srange[0]).append("</min>");
                            this.buffer.append("<max>").append(srange[1]).append("</max>");
                            break;
                        case TYPE_INT:
                            int[] irange = (int[]) range;
                            this.buffer.append("<min>").append(irange[0]).append("</min>");
                            this.buffer.append("<max>").append(irange[1]).append("</max>");
                            break;

                        case TYPE_DBL:
                            double[] drange = (double[]) range;
                            this.buffer.append("<min>").append(drange[0]).append("</min>");
                            this.buffer.append("<max>").append(drange[1]).append("</max>");
                            break;

                        case TYPE_REAL:
                            float[] frange = (float[]) range;
                            this.buffer.append("<min>").append(frange[0]).append("</min>");
                            this.buffer.append("<max>").append(frange[1]).append("</max>");
                            break;

                        case TYPE_COMPLEX:
                            // Not Applicable
                            break;

                        case TYPE_LOGICAL:
                            // Not Applicable
                            break;

                        default:
                        // do nothing
                    }
                }

                this.buffer.append("</column>\n");
            }
        }

        this.buffer.append("</columns>\n");

        if (this.verbose) {
            this.buffer.append("<table>\n<tr>\n");

            for (ColumnMeta column : columnsDescCollection) {
                if (oiTable.hasColumn(column)) {
                    this.buffer.append("<th>").append(column.getName()).append("</th>");
                }
            }
            this.buffer.append("</tr>\n");

            for (int rowIndex = 0, len = oiTable.getNbRows(); rowIndex < len; rowIndex++) {
                this.buffer.append("<tr>");

                for (ColumnMeta column : columnsDescCollection) {
                    if (oiTable.hasColumn(column)) {
                        this.buffer.append("<td>");

                        this.dumpColumnRow(oiTable, column, rowIndex);

                        this.buffer.append("</td>");
                    }
                }
                this.buffer.append("</tr>\n");
            }

            this.buffer.append("</table>\n");
        }
        this.buffer.append("</").append(oiTable.getExtName()).append(">\n");

        if (doOIFitsFile) {
            exitOIFitsFile();
        }
    }

    private void dumpFitsHeaderCard(final FitsHeaderCard card) {
        this.buffer.append("<keyword><name>").append(card.getKey()).append("</name><value>");
        String str = card.getValue();
        if (str != null) {
            this.buffer.append(encodeTagContent(str));
        }
        this.buffer.append("</value><description>");
        str = card.getComment();
        if (str != null) {
            this.buffer.append(encodeTagContent(str));
        }
        this.buffer.append("</description><type>A</type><unit></unit></keyword>\n");
    }

    /**
     * Append the string representation (String or array) of the column value at the given row index
     * @param oiTable OITable element to use
     * @param column column descriptor
     * @param rowIndex row index
     */
    private void dumpColumnRow(final OITable oiTable, final ColumnMeta column, final int rowIndex) {
        switch (column.getDataType()) {
            case TYPE_CHAR:
                final String[] chValues = oiTable.getColumnString(column.getName());
                // append value :
                this.buffer.append(encodeTagContent(chValues[rowIndex]));
                break;

            case TYPE_SHORT:
                if (column.isArray()) {
                    final short[][] sValues = oiTable.getColumnShorts(column.getName());
                    final short[] rowValues = sValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        this.buffer.append(rowValues[i]);
                    }
                    break;
                }
                final short[] sValues = oiTable.getColumnShort(column.getName());
                // append value :
                this.buffer.append(sValues[rowIndex]);
                break;

            case TYPE_INT:
                if (column.isArray()) {
                    final int[][] iValues = oiTable.getColumnInts(column.getName());
                    final int[] rowValues = iValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        this.buffer.append(rowValues[i]);
                    }
                    break;
                }
                final int[] iValues = oiTable.getColumnInt(column.getName());
                // append value :
                this.buffer.append(iValues[rowIndex]);
                break;

            case TYPE_DBL:
                if (column.isArray()) {
                    final double[][] dValues = oiTable.getColumnDoubles(column.getName());
                    final double[] rowValues = dValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        if (this.format) {
                            this.buffer.append(format(rowValues[i]));
                        } else {
                            this.buffer.append(rowValues[i]);
                        }
                    }
                    break;
                }
                final double[] dValues = oiTable.getColumnDouble(column.getName());
                // append value :
                if (this.format) {
                    this.buffer.append(format(dValues[rowIndex]));
                } else {
                    this.buffer.append(dValues[rowIndex]);
                }
                break;

            case TYPE_REAL:
                if (column.isArray()) {
                    // Impossible case in OIFits
                    this.buffer.append("...");
                    break;
                }
                final float[] fValues = oiTable.getColumnFloat(column.getName());
                // append value :
                if (this.format) {
                    this.buffer.append(format(fValues[rowIndex]));
                } else {
                    this.buffer.append(fValues[rowIndex]);
                }
                break;

            case TYPE_COMPLEX:
                // Special case for complex visibilities :
                if (column.isArray()) {
                    final float[][][] cValues = oiTable.getColumnComplexes(column.getName());
                    final float[][] rowValues = cValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        // real,img pattern for complex values :
                        if (this.format) {
                            this.buffer.append(format(rowValues[i][0])).append(',').append(format(rowValues[i][1]));
                        } else {
                            this.buffer.append(rowValues[i][0]).append(',').append(rowValues[i][1]);
                        }
                    }
                    break;
                }
                // Impossible case in OIFits
                this.buffer.append("...");
                break;

            case TYPE_LOGICAL:
                if (column.is3D()) {
                    final boolean[][][] bValues = oiTable.getColumnBoolean3D(column.getName());
                    final boolean[][] rowValues = bValues[rowIndex];
                    // append values :
                    for (int i = 0, lenI = rowValues.length; i < lenI; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        final boolean[] cellValues = rowValues[i];
                        for (int j = 0, lenJ = cellValues.length; j < lenJ; j++) {
                            if (j > 0) {
                                this.buffer.append(',');
                            }
                            if (this.format) {
                                if (cellValues[j]) {
                                    this.buffer.append('T');
                                } else {
                                    this.buffer.append('F');
                                }
                            } else {
                                this.buffer.append(cellValues[j]);
                            }
                        }
                    }
                    break;
                } else if (column.isArray()) {
                    final boolean[][] bValues = oiTable.getColumnBooleans(column.getName());
                    final boolean[] rowValues = bValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(' ');
                        }
                        if (this.format) {
                            if (rowValues[i]) {
                                this.buffer.append('T');
                            } else {
                                this.buffer.append('F');
                            }
                        } else {
                            this.buffer.append(rowValues[i]);
                        }
                    }
                    break;
                }
                // Impossible case in OIFits
                this.buffer.append("...");
                break;

            default:
                // Bad type
                this.buffer.append("...");
        }
    }

    /**
     * Format the given number using the beautifier formatter
     * @param value any float or double value
     * @return string representation
     */
    private static String format(final double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        final double v = (value >= 0d) ? value : -value;
        if (v == 0d) {
            return "0";
        }
        if (v > 1e-2d && v < 1e7d) {
            synchronized (DF_BEAUTY_STD) {
                return DF_BEAUTY_STD.format(value);
            }
        }
        synchronized (DF_BEAUTY_SCI) {
            return DF_BEAUTY_SCI.format(value);
        }
    }

    /**
     * Encode special characters to entities
     * @see JMCS StringUtils.encodeTagContent(String)
     *
     * @param src input string
     * @return encoded value
     */
    public static String encodeTagContent(final String src) {
        String out = PATTERN_AMP.matcher(src).replaceAll("&amp;"); // Character [&] (xml restriction)
        out = PATTERN_LT.matcher(out).replaceAll("&lt;"); // Character [<] (xml restriction)
        out = PATTERN_GT.matcher(out).replaceAll("&gt;"); // Character [>] (xml restriction)
        return out;
    }

    private void printMetadata(final OIFitsFile oiFitsFile) {
        /* analyze structure of file to browse by target */
        oiFitsFile.analyze();

        this.buffer.append("<metadata>\n");
        if (oiFitsFile.hasOiTarget()) {
            for (int i = 0; i < oiFitsFile.getOiTarget().getNbTargets(); i++) {
                this.buffer.append(OIFitsViewer.targetMetadata(oiFitsFile, i, true));
            }
        }
        this.buffer.append("</metadata>\n");
    }

    public static void appendRecord(StringBuilder target, String targetName, double targetRa, double targetDec, double intTime, double tMin, double tMax, float resPower, float minWavelength, float maxWavelength, String facilityName, String insName, int nbVis, int nbVis2, int nbT3, int nbChannels) {
        target.append("  <target>\n")
                .append("    <target_name>").append(encodeTagContent(targetName)).append("</target_name>\n")
                .append("    <s_ra>").append(targetRa).append("</s_ra>\n")
                .append("    <s_dec>").append(targetDec).append("</s_dec>\n")
                .append("    <t_exptime>").append(intTime).append("</t_exptime>\n")
                .append("    <t_min>").append(tMin).append("</t_min>\n")
                .append("    <t_max>").append(tMax).append("</t_max>\n")
                .append("    <em_res_power>").append(resPower).append("</em_res_power>\n")
                .append("    <em_min>").append(minWavelength).append("</em_min>\n")
                .append("    <em_max>").append(maxWavelength).append("</em_max>\n")
                .append("    <facility_name>").append(encodeTagContent(facilityName)).append("</facility_name>\n")
                .append("    <instrument_name>").append(encodeTagContent(insName)).append("</instrument_name>\n")
                .append("    <nb_vis>").append(nbVis).append("</nb_vis>\n")
                .append("    <nb_vis2>").append(nbVis2).append("</nb_vis2>\n")
                .append("    <nb_t3>").append(nbT3).append("</nb_t3>\n")
                .append("    <nb_channels>").append(nbChannels).append("</nb_channels>\n")
                .append("  </target>\n");
    }

}
