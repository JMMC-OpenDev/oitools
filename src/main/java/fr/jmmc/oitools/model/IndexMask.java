/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import java.util.BitSet;

/**
 * A two-dimensions wrapper to BitSet. 
 * Used to mask some values (rows, cols) on an OITable. Also supports 1D masks (rows).
 */
public final class IndexMask {

    /**
     * FULL mask instance. Do not try to access its content !
     */
    public static final IndexMask FULL = new IndexMask(null, 0, 0);

    /**
     * Create a 1D mask with every element masked
     *
     * @param nbRows number of rows. Must be > 0. the number of columns will be 1.
     * @return 1D mask
     */
    public static IndexMask createMask1D(final int nbRows) {
        return new IndexMask(nbRows, 1);
    }

    /**
     * Create a 2D mask with every element masked
     *
     * @param nbRows Must be > 0
     * @param nbCols Must be > 0
     * @return 2D mask
     */
    public static IndexMask createMask2D(final int nbRows, final int nbCols) {
        // each mask row has 2 more bits to encode NONE/FULL row (nbCols + 2):
        return new IndexMask(nbRows, nbCols + 2);
    }

    /* members */
    /**
     * the number of bits is (nbRows * nbCols). the order of elements is [row 0, col 0], then [row 0, col 1], etc. 
     * A bit set to false means the element is masked.
     */
    private final BitSet bitSet;

    /**
     * the number of rows in the BitSet. Must be > 0. If you strictly only need to mask rows in your OITable, think
     * about setting nbCols to 1.
     */
    private final int nbRows;

    /**
     * the number of columns in the BitSet. Must be > 0.
     */
    private final int nbCols;

    /** flag indicating if this mask is 1D */
    private final boolean vect1D;

    /**
     * Builds a 2D BitSet with every element masked
     *
     * @param nbRows Must be > 0
     * @param nbCols Must be > 0
     */
    private IndexMask(final int nbRows, final int nbCols) {
        this(new BitSet(nbRows * nbCols), nbRows, nbCols);
    }

    /**
     * Builds a 2D BitSet interface from an existing BitSet
     *
     * @param bitSet Must have nb of elements = nbRows * nbCols
     * @param nbRows Must be > 0
     * @param nbCols Must be > 0
     */
    private IndexMask(final BitSet bitSet, final int nbRows, final int nbCols) {
        this.bitSet = bitSet;
        this.nbRows = nbRows;
        this.nbCols = nbCols;
        this.vect1D = (nbCols <= 1);
    }

    /**
     * Is the given row accepted for 1D mask ?
     * @param rowIndex Must be >= 0 and < nbRows
     * @return true to accept; false to reject
     */
    public boolean accept(final int rowIndex) {
        if (vect1D) {
            return bitSet.get(rowIndex);
        }
        throw new IllegalArgumentException("Incompatible mask dimensions (2D) !");
    }

    /**
     * Set the accepted flag(s) at the given row for 1D and 2D masks
     *
     * @param rowIndex Must be >= 0 and < nbRows
     * @param value true to accept; false to reject
     */
    public void setAccept(final int rowIndex, final boolean value) {
        // shortcut for 1D masks
        if (vect1D) {
            bitSet.set(rowIndex, value);
        } else {
            // for each cell in the row
            bitSet.set(getBitIndex(rowIndex, 0), getBitIndex(rowIndex, nbCols), value);
        }
    }

    /**
     * Is the given element accepted for 2D mask ?
     * @param rowIndex Must be >= 0 and < nbRows
     * @param colIndex Must be >= 0 and < nbCols
     * @return true if cell equals @param value. cell [rowIndex, colIndex]
     */
    public boolean accept(final int rowIndex, final int colIndex) {
        if (!vect1D) {
            return bitSet.get(getBitIndex(rowIndex, colIndex));
        }
        throw new IllegalArgumentException("Incompatible mask dimensions (1D) !");
    }

    /**
     * Set the accepted flag at the given row and column.
     *
     * @param rowIndex Must be >= 0 and < nbRows
     * @param colIndex Must be >= 0 and < nbCols
     * @param value false to mask
     */
    public void setAccept(final int rowIndex, final int colIndex, final boolean value) {
        if (!vect1D) {
            bitSet.set(getBitIndex(rowIndex, colIndex), value);
        } else {
            throw new IllegalArgumentException("Incompatible mask dimensions (2D) !");
        }
    }

    private int getBitIndex(final int rowIndex, final int colIndex) {
        return (rowIndex * nbCols) + colIndex;
    }

    public int cardinality() {
        return this.bitSet.cardinality();
    }

    public static boolean isFull(final IndexMask mask) {
        return (mask == FULL);
    }

    public static boolean isNotFull(final IndexMask mask) {
        return (mask != null) && (mask != FULL);
    }

    @Override
    public String toString() {
        return "IndexMask{" + "nbRows=" + nbRows + ", nbCols=" + nbCols + ", bitSet=" + bitSet + '}';
    }

    /**
     * @return the nbRows
     */
    public int getNbRows() {
        return nbRows;
    }

    /**
     * @return the nbCols (note for 2D masks, it corresponds to given nbCols +2)
     */
    public int getNbCols() {
        return nbCols;
    }

    /**
     * @return flag indicating if this mask is 1D
     */
    public boolean is1D() {
        return vect1D;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public int getIndexNone() {
        if (!vect1D) {
            return nbCols - 2;
        }
        return -1;
    }

    public int getIndexFull() {
        if (!vect1D) {
            return nbCols - 1;
        }
        return -1;
    }
}
