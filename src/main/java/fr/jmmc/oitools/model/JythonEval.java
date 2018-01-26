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

import fr.jmmc.oitools.meta.ColumnMeta;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.python.core.PyArray;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.util.PythonInterpreter;

/**
 * ExpressionEvaluator implementation based on Jython
 * @author grosje
 */
final class JythonEval extends ExpressionEvaluator {

    private final static boolean DEBUG = false;
    /** logger */
    private final static Logger _logger = Logger.getLogger(JythonEval.class.getName());

    JythonEval() {
        // no-op
    }

    /**
     * Evaluate an expression and return the result in a 2D array of double
     * @param oiData oiData table
     * @param colNameEval name of the derived column to evaluate
     * @param expression expression entered by the user
     * @param testOnly boolean which indicates if the method is used to check the expression (true) or to perform the script
     * @return 2D Array of double
     * @throws RuntimeException
     */
    @Override
    public double[][] eval(final OIData oiData, final String colNameEval,
            final String expression, final boolean testOnly)
            throws RuntimeException {

        // From OIData table:
        int nRows = oiData.getNbRows();
        int nWaves = oiData.getNWave();

        if (testOnly) {
            nRows = Math.min(nRows, 1);
            nWaves = Math.min(nWaves, 1);
        }

        // attention: dimensions en sortie ?
        // deduire la dimensionalité ? ucoord /3 => 1d
        final double[][] result = new double[nRows][nWaves];

        final StringBuilder script = new StringBuilder(1024);

        try {
            System.setProperty("python.import.site", "false");

            // Create an instance of the PythonInterpreter
            PythonInterpreter interp = new PythonInterpreter();

            String colName, varName;

            interp.set("_rows", new PyInteger(nRows));
            interp.set("_waves", new PyInteger(nWaves));

            // Set Inputs within the PythonInterpreter instance
            // Special case for wavelengths:
            colName = "EFF_WAVE";
            varName = "_input_" + "EFF_WAVE";
            interp.set(varName, new PyArray(double[][].class, oiData.getEffWaveAsDoubles()));

            if (DEBUG) {
                interp.exec("print '" + colName + ":'," + varName);
            }

            // TODO: use a new OITable method giving all numerical ColumnMeta (original + derived):
            final Collection<ColumnMeta> columnsDescCollection = oiData.getColumnDescCollection();

            // For each data column from OIData table:
            for (ColumnMeta column : columnsDescCollection) {
                colName = column.getName();
                varName = "_input_" + colName;

                // Warning: only columns with double[][] or double[] values in PythonInterpreter:
                // TODO: handle null values !
                switch (column.getDataType()) {
                    case TYPE_DBL:
                        if (column.isArray()) {
                            final double[][] dValues = oiData.getColumnDoubles(colName);

                            interp.set(varName, new PyArray(double[][].class, dValues));

                            if (DEBUG) {
                                interp.exec("print '" + colName + ":'," + varName);
                            }
                            break;
                        }
                        final double[] dValues = oiData.getColumnDouble(colName);

                        interp.set(varName, new PyArray(double[].class, dValues));

                        if (DEBUG) {
                            interp.exec("print '" + colName + ":'," + varName);
                        }
                        break;

                    case TYPE_CHAR:
                    case TYPE_SHORT:
                    case TYPE_INT:
                    case TYPE_REAL:
                    case TYPE_LOGICAL:
                    case TYPE_COMPLEX:
                    default:

                }
            }   // get column name + values as 1d or 2d array
            // attention au type ! double[][] ou double[] ou float[]
            // sets variables in python: '_input_<column_name>'
            for (ColumnMeta column : columnsDescCollection) {
                colName = column.getName();
                varName = "_input_" + colName;

                // Warning: only columns with double[][] or double[] values in PythonInterpreter:
                // TODO: handle null values !
                switch (column.getDataType()) {
                    case TYPE_DBL:
                        if (column.isArray()) {
                            final double[][] dValues = oiData.getColumnDoubles(colName);

                            interp.set(varName, new PyArray(double[][].class, dValues));

                            if (DEBUG) {
                                interp.exec("print '" + colName + ":'," + varName);
                            }
                            break;
                        }
                        final double[] dValues = oiData.getColumnDouble(colName);

                        interp.set(varName, new PyArray(double[].class, dValues));

                        if (DEBUG) {
                            interp.exec("print '" + colName + ":'," + varName);
                        }
                        break;

                    case TYPE_CHAR:
                    case TYPE_SHORT:
                    case TYPE_INT:
                    case TYPE_REAL:
                    case TYPE_LOGICAL:
                    case TYPE_COMPLEX:
                    default:

                }
            }

            // Output (fixed)
            interp.set("_output", new PyArray(double[][].class, result));

            script.append("from array import array \n");
            script.append("from math import *\n\n");

            script.append("def user_func():\n");
            script.append("\treturn " + expression + "\n\n");

            // Loops on rows x wavelengths:
            script.append("for _i in range(_rows):\n");
            script.append("\tfor _j in range(_waves):\n");

            if (DEBUG) {
                script.append("\t\tprint 'indices: ', _i, _j \n");
            }
            script.append('\n');

            // Get scalar values from input arrays:
            // Special case for wavelengths:
            colName = "EFF_WAVE";
            varName = "_input_" + colName;
            // 1D on wavelengths:
            script.append("\t\t" + colName + " = " + varName + "[_i][_j]\n");
            // attention aux tableaux:
            // 2D [i][j]
            // 1D [i] // rows
            // 1D [j] // wavelengths
            // For each data column from OIData table:
            for (ColumnMeta column : columnsDescCollection) {
                colName = column.getName();
                varName = "_input_" + colName;

                // Warning: only columns with double[][] or double[] values in PythonInterpreter:
                switch (column.getDataType()) {
                    case TYPE_DBL:
                        if (column.isArray()) {
                            // 2D:
                            script.append("\t\t" + colName + " = " + varName + "[_i][_j]\n");
                            break;
                        }

                        // 1D on rows:
                        script.append("\t\t" + colName + " = " + varName + "[_i]\n");
                        break;

                    case TYPE_CHAR:
                    case TYPE_SHORT:
                    case TYPE_INT:
                    case TYPE_REAL:
                    case TYPE_LOGICAL:
                    case TYPE_COMPLEX:
                    default:
                    // ignore
                }
            }
            script.append("\n\t\t_output[_i][_j] = user_func() \n");

            if (DEBUG) {

                // Special case for wavelengths:
                colName = "EFF_WAVE";
                script.append("\n\t\tprint '" + colName + ":'," + colName + "\n");

                for (ColumnMeta column : columnsDescCollection) {

                    colName = column.getName();

                    // Warning: only columns with double[][] or double[] values in PythonInterpreter:
                    switch (column.getDataType()) {
                        case TYPE_DBL:
                            script.append("\t\tprint '" + colName + ":'," + colName + "\n");
                            break;

                        case TYPE_CHAR:
                        case TYPE_SHORT:
                        case TYPE_INT:
                        case TYPE_REAL:
                        case TYPE_LOGICAL:
                        case TYPE_COMPLEX:
                        default:
                        // ignore
                    }
                }
                script.append("\t\tprint 'output:',_output[_i][_j] \n");
            }

            if (DEBUG) {
                _logger.log(Level.INFO, "script:\n{0}", script);
            }

            interp.exec(script.toString());

        } catch (PyException pe) {
            if (testOnly) {
                throw new RuntimeException("[" + pe.value + "]", pe);
            }

            _logger.log(Level.INFO, "Python script error:{0}", pe.value);
            _logger.log(Level.INFO, "Script executed: \n{0}", script);

            // reset results to NaN
            for (int i = 0; i < nRows; i++) {
                Arrays.fill(result[i], Double.NaN);
            }
        }

        // retourner les resultats ...
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "result: {0}", Arrays.deepToString(result));
        }

        return result;
    }

}
