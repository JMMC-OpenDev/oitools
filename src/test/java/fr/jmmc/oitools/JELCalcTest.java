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
package fr.jmmc.oitools;

/**
 * Basic test for the gnu JEL (Expression evaluator)
 * @author bourgesl / grosje
 */
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import org.junit.Assert;
import org.junit.Test;

public class JELCalcTest extends JUnitBaseTest {

    @Test
    public void testCalc() throws CompilationException, Throwable {

        final String expr = "1.3 + x + y * 3.0";

        logger.info("Expression: " + expr);

        final int len = 1000;
        final double[] x = new double[len];
        final double[] y = new double[len];
        final double[] result = new double[len];
        final double[] expected = new double[len];

        for (int i = 0; i < len; i++) {
            x[i] = i;
            y[i] = 5.0 * i;
            result[i] = Double.NaN;
            expected[i] = 1.3 + x[i] + y[i] * 3.0;
        }

        final Class[] staticLib = new Class[1];
        try {
            staticLib[0] = Class.forName("java.lang.Math");
        } catch (ClassNotFoundException e) {
        }

        final VariableProvider dataProvider = new VariableProvider();

        final Class[] dynamicLib = new Class[1];
        dynamicLib[0] = dataProvider.getClass();

        final Object[] context = new Object[1];
        context[0] = dataProvider;

        final Library lib = new Library(staticLib, dynamicLib, null, dataProvider, null);

        // Math.random():
        lib.markStateDependent("random", null);

        // Compile
        CompiledExpression expr_c = null;
        try {
            logger.info("Compilation ...");
            expr_c = Evaluator.compile(expr, lib);
        } catch (CompilationException ce) {
            System.err.println("COMPILATION ERROR:");
            System.err.println(ce.getMessage());
            System.err.println("Expression:");
            System.err.println(expr);
            int column = ce.getColumn(); // Column, where error was found
            for (int i = 0; i < column - 1; i++) {
                System.err.print(' ');
            }
            System.err.print('^');
            throw ce;
        }

        if (expr_c != null) {
            logger.info("Evaluation ...");

            // Evaluate (Can do it now any number of times FAST !!!)
            for (int n = 0; n < 100; n++) {
                final long startTime = System.nanoTime();

                for (int i = 0; i < x.length; i++) {
                    dataProvider.x = x[i];      // <- Value of the variable
                    dataProvider.y = y[i];      // <- Value of the variable

                    result[i] = expr_c.evaluate_double(context);
                }
                System.out.println("duration = " + 1e-6d * (System.nanoTime() - startTime));
            }
//            System.out.println("result: " + Arrays.toString(result));

            Assert.assertArrayEquals("result", expected, result, 1e-30);
        }
    }

    public static final class VariableProvider extends DVMap {

        double x;
        double y;

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        @Override
        public String getTypeName(String name) {
//            System.out.println("getTypeName: " + name);

            // return null if unknown variable
            return "Double";
        }

        public double getDoubleProperty(String name) {
//            System.out.println("getDoubleProperty: " + name);
            if ("x".equals(name)) {
                return x;
            }
            if ("y".equals(name)) {
                return y;
            }
            return Double.NaN;
        }

// --- performance: use integer mapping --
        @Override
        public Object translate(String name) {
//            System.out.println("translate: " + name);
            return super.translate(name);
        }

        public double getDoubleProperty(int index) {
            switch (index) {
                case 0:
                    return x;
                case 1:
                    return y;
                default:
            }
            return Double.NaN;
        }

    }

}
