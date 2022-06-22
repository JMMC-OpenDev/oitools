/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import java.util.BitSet;

/**
 * A two-dimension interface for a BitSet. Used to mask some rows, cols or cells on an OITable. Also supports 1D masks,
 * vertical [N,1] and horizontal [1,N].
 */
public class IndexMask {
    /**
     * FULL mask instance. Do not try to access its content !
     */
    public static final IndexMask FULL = new IndexMask(null, 0, 0);

    /**
     * the number of bits is (nbRows * nbCols). the order of cells is [row 0, col 0], then [row 0, col 1], etc. A bit
     * set to false means the cell is masked.
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
     * Builds a 1D BitSet with every cell masked.
       *
     * @param nbRows number of rows. Must be > 0. the number of columns will be 1.
     */
    public IndexMask(int nbRows) {
        this(nbRows, 1);
    }

    /**
     * Builds a 2D BitSet with every cell masked.
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
     * @param value
     * @return true if cell equals @param value. cell [rowIndex, colIndex]
     */
    public boolean isCell(final int rowIndex, final int colIndex, final boolean value) {
        return bitSet.get(getCellIndex(rowIndex, colIndex)) == value;
    }

    /**
     * @param rowIndex
     * @param value
     * @return true if every cell in the row equals @param value. cells [rowIndex,*]
     */
    public boolean isRow(final int rowIndex, final boolean value) {
        // shortcut for 1D masks
        if (nbCols == 1) {
            return bitSet.get(rowIndex) == value;
        }
        // check each cell in the row
        for (int i = getCellIndex(rowIndex, 0); i < getCellIndex(rowIndex, nbCols); i++) {
            if (bitSet.get(i) != value) {
                return false; // some cell is wrong
            }
        }
        return true; // all cells are good
    }

    /**
     * @param colIndex
     * @param value
     * @return true if every cell in the column equals @param value. cells [*,colIndex]
     */
    public boolean isCol(final int colIndex, final boolean value) {
        // shortCut for 1D masks
        if (nbRows == 1) {
            return bitSet.get(colIndex) == value;
        }
        // check each cell in the column
        for (int i = colIndex; i < nbRows * nbCols; i += nbCols) {
            if (bitSet.get(i) != value) {
                return false; // some cell is wrong
            }
        }
        return true; // all cells are good
    }

    /**
     * Sets value of the cell [rowIndex,colIndex].
     *
     * @param rowIndex
     * @param colIndex
     * @param value false to mask
     */
    public void setCell(final int rowIndex, final int colIndex, final boolean value) {
        bitSet.set(getCellIndex(rowIndex, colIndex), value);
    }

    /**
     * Sets value of every cell of the row. cells [rowIndex,*].
     *
     * @param rowIndex
     * @param value false to mask
     */
    public void setRow(final int rowIndex, final boolean value) {
        // shortcut for 1D masks
        if (nbCols == 1) {
            bitSet.set(rowIndex, value);
        }
        else { // for each cell in the row
            bitSet.set(getCellIndex(rowIndex, 0), getCellIndex(rowIndex, nbCols), value);
        }
    }

    /**
     * Sets value of every cell of the column. cells [*,colIndex].
     *
     * @param colIndex
     * @param value false to mask
     */
    public void setCol(final int colIndex, final boolean value) {
        // shortcut for 1D masks
        if (nbRows == 1) {
            bitSet.set(colIndex, value);
        }
        else {
            // for each cell in the column
            for (int i = colIndex; i < nbRows * nbCols; i += nbCols) {
                bitSet.set(i, value);
            }
        }
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
     * @return the nbCols
     */
    public int getNbCols() {
        return nbCols;
    }

    /**
     * Get the BitSet. Use it to call some functions on the BitSet, avoid keeping a reference to the BitSet.
     *
     * @return the bitSet.
     */
    public BitSet getBitSet() {
        return bitSet;
    }

}
