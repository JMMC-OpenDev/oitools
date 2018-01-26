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
 * This interface defines methods used by the visitor pattern
 * @author bourgesl
 */
public interface ModelVisitor {

    /**
     * Process the given OIFitsFile element with this visitor implementation
     * @param oiFitsFile OIFitsFile element to visit
     */
    public void visit(final OIFitsFile oiFitsFile);

    /**
     * Process the given OITable element with this visitor implementation
     * @param oiTable OITable element to visit
     */
    public void visit(final OITable oiTable);
}
