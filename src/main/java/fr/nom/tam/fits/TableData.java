/*
 * This code is part of the Java FITS library developed 1996-2012 by T.A. McGlynn (NASA/GSFC)
 * The code is available in the public domain and may be copied, modified and used
 * by anyone in any fashion for any purpose without restriction. 
 * 
 * No warranty regarding correctness or performance of this code is given or implied.
 * Users may contact the author if they have questions or concerns.
 * 
 * The author would like to thank many who have contributed suggestions, 
 * enhancements and bug fixes including:
 * David Glowacki, R.J. Mathar, Laurent Michel, Guillaume Belanger,
 * Laurent Bourges, Rose Early, Fred Romelfanger, Jorgo Baker, A. Kovacs, V. Forchi, J.C. Segovia,
 * Booth Hartley and Jason Weiss.  
 * I apologize to any contributors whose names may have been inadvertently omitted.
 * 
 *      Tom McGlynn
 */
package fr.nom.tam.fits;

/** This class allows FITS binary and ASCII tables to
 *  be accessed via a common interface.
 */
public interface TableData {

    public abstract Object[] getRow(int row) throws FitsException;

    public abstract Object getColumn(int col) throws FitsException;

    public abstract Object getElement(int row, int col) throws FitsException;

    public abstract void setRow(int row, Object[] newRow) throws FitsException;

    public abstract void setColumn(int col, Object newCol) throws FitsException;

    public abstract void setElement(int row, int col, Object element) throws FitsException;

    public abstract int addRow(Object[] newRow) throws FitsException;

    public abstract int addColumn(Object newCol) throws FitsException;

    public abstract void deleteRows(int row, int len) throws FitsException;

    public abstract void deleteColumns(int row, int len) throws FitsException;

    public abstract void updateAfterDelete(int oldNcol, Header hdr) throws FitsException;

    public abstract int getNCols();

    public abstract int getNRows();

}
