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
 * Rule dataType, gives more information on the type of data present
 * @author kempsc
 */
public enum RuleDataType {

    NONE,
    VALUE,
    VALUE_EXPECTED,
    VALUE_ROW,
    VALUE_EXPECTED_ROW,
    VALUE_LIMIT_ROW,
    VALUE_ROW1_ROW2,
    VALUE_ROW_COL,
    VALUE_EXPECTED_ROW_COL,
    VALUE_ROW_COL1_COL2,
    VALUE_ROW_COL_DETAILS, /* EOF */;
}
