/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/

package fr.jmmc.oitools.model;

import java.util.BitSet;

/**
 * A two-dimension interface for a BitSet. Used to mask some rows, cols or cells on an OITable.
 *
 * @author antoine
 */
public class IndexMask {

    /**
     * the number of bits is (nbRows * nbCols). the order of cells is [row 0, col 0], then [row 0, col 1], etc. A bit
     * set to true means the cell is masked.
     */
    private final BitSet bitSet;

    /**
     * the number of rows in the BitSet. Must be > 0. If you strictly only need to mask rows in your OITable, think
     * about setting nbCols to 1.
     */

    private final int nbRows;
    /**
     * the number of columns in the BitSet. Must be > 0. If you strictly only need to mask columns in your OITable,
     * think about setting nbRows to 1.
     */
    private final int nbCols;

    /**
     * Builds a 2D BitSet with every cell unmasked.
     *
     * @param nbRows Must be > 0
     * @param nbCols Must be > 0
     */
    public IndexMask(int nbRows, int nbCols) {
        this(new BitSet(nbRows * nbCols), nbRows, nbCols);
    }

    /**
     * Builds a 2D BitSet interface from an existing BitSet.
     *
     * @param bitSet Must have nb of cells = nbRows * nbCols
     * @param nbRows Must be > 0
     * @param nbCols Must be > 0
     */
    public IndexMask(BitSet bitSet, int nbRows, int nbCols) {
        this.bitSet = bitSet;
        this.nbRows = nbRows;
        this.nbCols = nbCols;
    }

    /**
     * @param rowIndex
     * @param colIndex
     * @return the bitSet index for [rowIndex,colIndex]
     */
    public int getCellIndex(final int rowIndex, final int colIndex) {
        return (rowIndex * nbCols) + colIndex;
    }

    /**
     * @param rowIndex
     * @param colIndex
     * @return true if bitSet[rowIndex,colIndex] is masked
     */
    public boolean isCellMasked(final int rowIndex, final int colIndex) {
        return bitSet.get(getCellIndex(rowIndex, colIndex));
    }

    /**
     * @param rowIndex
     * @return true if every bitSet[rowIndex,*] is masked
     */
    public boolean isRowMasked(final int rowIndex) {
        for (int i = getCellIndex(rowIndex, 0); i < getCellIndex(rowIndex, nbCols); i++) {
            if (!bitSet.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param colIndex
     * @return true if every bitSet[*,colIndex] is masked
     */
    public boolean isColMasked(final int colIndex) {
        for (int i = colIndex; i < nbRows * nbCols; i += nbCols) {
            if (!bitSet.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets bitSet[rowIndex,colIndex].
     *
     * @param rowIndex
     * @param colIndex
     * @param value true to mask
     */
    public void setCell(final int rowIndex, final int colIndex, final boolean value) {
        bitSet.set(getCellIndex(rowIndex, colIndex), value);
    }

    /**
     * Sets every bitSet[rowIndex,*].
     *
     * @param rowIndex
     * @param value true to mask
     */
    public void setRow(final int rowIndex, final boolean value) {
        bitSet.set(getCellIndex(rowIndex, 0), getCellIndex(rowIndex, nbCols), value);
    }

    /**
     * Sets every bitSet[*,colIndex].
     *
     * @param colIndex
     * @param value true to mask
     */
    public void setCol(final int colIndex, final boolean value) {
        for (int i = colIndex; i < nbRows * nbCols; i += nbCols) {
            bitSet.set(i, value);
        }
    }

    /**
     * @return the bitSet
     */
    public BitSet getBitSet() {
        return bitSet;
    }

    /**
     * @return the nbRows
     */
    public int getNbRows() {
        return nbRows;
    }

    /**
     * @return the nbCols
     */
    public int getNbCols() {
        return nbCols;
    }
}
