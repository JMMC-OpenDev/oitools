/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.ToStringable;

/**
 * This class represents the Index key type (target UID, instrument mode UID, mjd, ...),
 * to group related data. 
 * @author bourgesl
 */
public interface IndexKey extends Comparable<IndexKey>, ToStringable {

}
