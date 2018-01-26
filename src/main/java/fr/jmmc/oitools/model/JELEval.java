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
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ExpressionEvaluator implementation based on gnu JEL
 * @author grosje
 */
public class JELEval extends ExpressionEvaluator {

    /** logger */
    private final static Logger _logger = Logger.getLogger(JELEval.class.getName());

    public JELEval() {
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

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "eval: column= {0} expression= {1}", new Object[]{colNameEval, expression});
        }

        int nRows = oiData.getNbRows();
        int nWaves = oiData.getNWave();

        if (testOnly) {
            nRows = Math.min(nRows, 1);
            nWaves = Math.min(nWaves, 1);
        }

        // Prepare inputs:
        // all numerical names (original + derived):
        final List<ColumnMeta> columnsDescCollection = oiData.getNumericalColumnsDescs();

        // Create input column names:
        int n = 0;
        final String[] jelNames = new String[columnsDescCollection.size()];

        // For each data column from OIData table:
        for (ColumnMeta colMeta : columnsDescCollection) {
            final String colName = colMeta.getName();

            // skip the derived column being evaluated:
            // to avoid infinite recursion:
            if (!colName.equalsIgnoreCase(colNameEval)) {
                jelNames[n++] = colName;
            }
        }

        // Prepare the variable resolver used by compilation only:
        final VariableResolver resolver = new VariableResolver(jelNames);

        // Output: always 2D array returned
        final double[][] result = new double[nRows][nWaves];

        try {
            // Setup JEL!
            final Class<?>[] staticLib = new Class<?>[1];
            staticLib[0] = Math.class;

            final Class<?>[] dynamicLib = new Class<?>[2];

            // Both dynamicLib and context arrays must be consistent:
            dynamicLib[0] = VariableResolver.class;
            dynamicLib[1] = VariableProvider.class;

            final Library lib = new Library(staticLib, dynamicLib, null, resolver, null);

            // Math.random():
            lib.markStateDependent("random", null);

            // Compile expression
            _logger.fine("Compilation ...");

            final CompiledExpression expr_c = Evaluator.compile(expression, lib);

            // Retrieve values:
            // Uniquement les colonnes utilisées => VariableResolver.usedNames
            final Set<String> usedNames = resolver.usedNames;

            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "usedNames: {0}", usedNames);
            }

            // Create the input column array:
            // warning: array must have the same size and indices as the input column names (jelNames):
            n = 0;
            final JELColumn[] jelColums = new JELColumn[columnsDescCollection.size()];

            // For each data column from OIData table:
            for (ColumnMeta colMeta : columnsDescCollection) {
                final String colName = colMeta.getName();

                // skip the derived column being evaluated:
                // to avoid infinite recursion:
                if (!colName.equalsIgnoreCase(colNameEval)) {
                    JELColumn col = null;

                    // Retrieve only used columns:
                    // case sensitive so usedNames must contain original column names:
                    if (usedNames.contains(colName)) {
                        // only names with double[][] or double[] values:
                        if (colMeta.isArray()) {
                            // 2D column
                            col = new JELColumn(colName, oiData.getColumnAsDoubles(colName));
                        } else {
                            // 1D column
                            col = new JELColumn(colName, oiData.getColumnAsDouble(colName));
                        }
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.log(Level.FINE, "colMeta: {0}", colMeta);
                            _logger.log(Level.FINE, "JELColumn: {0}", col);
                        }
                    }
                    // Consistent with:
                    // jelNames[n++] = colName
                    jelColums[n++] = col;
                }
            }

            // Prepare the variable resolver used by evaluation only:
            final VariableProvider varProvider = new VariableProvider(jelColums);

            final Object[] context = new Object[2];
            context[0] = resolver;
            context[1] = varProvider;

            // Execute compiled expression:
            double[] row;

            // On connait les JELColumns utilisées
            // Donc on peut déduire si la boucle est en 1D ou en 2D ?
            for (int i = 0; i < nRows; i++) {
                varProvider.i = i;
                row = result[i];

                for (int j = 0; j < nWaves; j++) {
                    varProvider.j = j;

                    row[j] = expr_c.evaluate_double(context);
                }
            }
        } catch (CompilationException ce) {
            if (testOnly) {
                throw new RuntimeException("[" + ce.getMessage() + "]", ce);
            }

            _logger.log(Level.INFO, "Expression compilation error:{0}", ce.getMessage());
            _logger.log(Level.INFO, "Expression executed: \n{0}", expression);

            // reset results to NaN
            for (int i = 0; i < nRows; i++) {
                Arrays.fill(result[i], Double.NaN);
            }
        } catch (Throwable th) {
            // impossible case
            throw new IllegalStateException("JEL error: expression compilation or evaluation failure"
                    + " [" + th.getMessage() + "]", th);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "eval: result = {0}", Arrays.toString(result));
        }
        return result;
    }

    /**
     * This class is public and overridden public methods are required by JEL
     */
    public static class VariableResolver extends DVMap {

        // all possible column names
        final String[] names;
        // used variable names:
        final Set<String> usedNames = new HashSet<String>();

        VariableResolver(final String[] names) {
            this.names = names;
        }

        @Override
        public String getTypeName(final String varName) {
            final int index = getColumnIndex(varName);

            //only double type supported
            if (index >= 0) {
                // store original column name:
                usedNames.add(names[index]);
                return "Double";
            }
            return null;
        }

        @Override
        public Object translate(final String varName) {
            final int index = getColumnIndex(varName);
            // explicit object conversion:
            return (index >= 0) ? Integer.valueOf(index) : null;
        }

        private int getColumnIndex(final String colName) {
            for (int i = 0; i < names.length; i++) {
                if (names[i] != null && colName.equalsIgnoreCase(names[i])) {
                    return i;
                }
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Unsupported column: [{0}]", colName);
            }
            return -1;
        }
    }

    /**
     * This class is public and overridden public methods are required by JEL
     */
    public static class VariableProvider {

        final JELColumn[] columns;
        int i = -1;
        int j = -1;

        VariableProvider(final JELColumn[] columns) {
            this.columns = columns;
        }

        public double getDoubleProperty(final int index) {
            final JELColumn col = columns[index];

            return (col.values2D != null) ? col.values2D[i][j] : col.values1D[i];
        }
    }

    protected static final class JELColumn {

        final String name;
        final double[] values1D;
        final double[][] values2D;

        JELColumn(final String name, final double[] values1D) {
            if (values1D == null) {
                throw new IllegalArgumentException("Null value for column '" + name + "' !");
            }
            this.name = name;
            this.values1D = values1D;
            this.values2D = null;
        }

        JELColumn(final String name, final double[][] values2D) {
            if (values2D == null) {
                throw new IllegalArgumentException("Null value for column '" + name + "' !");
            }
            this.name = name;
            this.values1D = null;
            this.values2D = values2D;
        }

        @Override
        public String toString() {
            return "JELColumn{" + "name=" + name + ", values1D=" + Arrays.toString(values1D) + ", values2D=" + Arrays.toString(values2D) + '}';
        }

    }
}
