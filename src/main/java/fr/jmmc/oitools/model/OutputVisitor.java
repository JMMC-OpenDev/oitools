/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsCollectionViewer;
import fr.jmmc.oitools.OIFitsViewer;

/**
 *
 * @author bourgesl
 */
public abstract class OutputVisitor implements ModelVisitor {

    public enum TargetMetadataProvider {
        OIFITS_METADATA {
            public void targetMetadata(final OIFitsFile oiFitsFile, final OutputVisitor out) {
                OIFitsViewer.targetMetadata(oiFitsFile, out);
            }
        },
        GRANULE_METADATA {
            public void targetMetadata(final OIFitsFile oiFitsFile, final OutputVisitor out) {
                OIFitsCollectionViewer.targetMetadata(oiFitsFile, out);
            }
        };

        public abstract void targetMetadata(final OIFitsFile oiFitsFile, final OutputVisitor out);

        public static TargetMetadataProvider getInstance(final boolean useGranules) {
            return useGranules ? GRANULE_METADATA : OIFITS_METADATA;
        }
    }

    /* members */
    /** flag to enable/disable the verbose output */
    private boolean verbose;
    /** internal buffer */
    protected final StringBuilder buffer;
    /** target metadata provider */
    private final TargetMetadataProvider metadataProvider;

    /**
     * Create a new OutputVisitor with verbose output
     * @param metadataProvider target metadata provider
     * @param verbose if true the result will contain the table content
     * @param bufferCapacity initial buffer capacity
     */
    protected OutputVisitor(final TargetMetadataProvider metadataProvider, final boolean verbose, final int bufferCapacity) {
        this.verbose = verbose;

        // allocate buffer size:
        this.buffer = new StringBuilder(bufferCapacity);

        this.metadataProvider = metadataProvider;
    }

    /**
     * Return the flag to enable/disable the verbose output
     * @return flag to enable/disable the verbose output
     */
    public final boolean isVerbose() {
        return verbose;
    }

    /**
     * Define the flag to enable/disable the verbose output
     * @param verbose flag to enable/disable the verbose output
     */
    public final void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Clear the internal buffer for later reuse
     */
    public final void reset() {
        // recycle buffer :
        this.buffer.setLength(0);
    }

    /**
     * Return the buffer content as a string
     * @return buffer content
     */
    @Override
    public final String toString() {
        final String result = this.buffer.toString();

        // reset the buffer content
        reset();

        return result;
    }

    /**
     * Open the oifits element with OIFitsFile description
     * @param oiFitsFile OIFitsFile to get its description (file name)
     */
    protected abstract void enterOIFitsFile(final OIFitsFile oiFitsFile);

    /**
     * Ends the oifits element
     */
    protected abstract void exitOIFitsFile();

    protected final void printMetadata(final OIFitsFile oiFitsFile) {
        /* analyze structure of file to browse by target */
        oiFitsFile.analyze();

        enterMetadata();
        metadataProvider.targetMetadata(oiFitsFile, this);
        exitMetadata();
    }

    public abstract void enterMetadata();

    public abstract void exitMetadata();

    public abstract void appendMetadataRecord(final String targetName, final double targetRa, final double targetDec,
                                              double intTime, double tMin, double tMax,
                                              double resPower, double minWavelength, double maxWavelength,
                                              String facilityName, final String insName,
                                              int nbVis, int nbVis2, int nbT3, int nbChannels);
}
