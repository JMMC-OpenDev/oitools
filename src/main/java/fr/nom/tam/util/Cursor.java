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

import java.util.Iterator;
import java.util.NoSuchElementException;

/** This interface extends the Iterator interface
 *  to allow insertion of data and move to previous entries
 *  in a collection.
 */
public interface Cursor extends Iterator {

    /** Is there a previous element in the collection? */
    public abstract boolean hasPrev();

    /** Get the previous element */
    public abstract Object prev() throws NoSuchElementException;

    /** Point the list at a particular element.
     *  Point to the end of the list if the key is not found.
     */
    public abstract void setKey(Object key);

    /** Add an unkeyed element to the collection.
     *  The new element is placed such that it will be called
     *  by a prev() call, but not a next() call.
     */
    public abstract void add(Object reference);

    /** Add a keyed element to the collection.
     *  The new element is placed such that it will be called
     *  by a prev() call, but not a next() call.
     */
    public abstract void add(Object key, Object reference);
}
