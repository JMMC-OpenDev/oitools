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
package fr.nom.tam.util;

/** This interface defines the properties that
 * a generic table should have.
 */
public interface DataTable {

    public abstract void setRow(int row, Object newRow)
            throws TableException;

    public abstract Object getRow(int row);

    public abstract void setColumn(int column, Object newColumn)
            throws TableException;

    public abstract Object getColumn(int column);

    public abstract void setElement(int row, int col, Object newElement)
            throws TableException;

    public abstract Object getElement(int row, int col);

    public abstract int getNRows();

    public abstract int getNCols();
}
