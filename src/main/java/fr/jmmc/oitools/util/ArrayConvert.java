/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.util;

/**
 *
 * @author bourgesl
 */
public final class ArrayConvert {

    private ArrayConvert() {
        // private
    }

    public static float[][] toFloats(final int rows, final int cols, final Object array2D) {
        if (array2D instanceof float[][]) {
            return (float[][]) array2D;
        }
        // Convert data to float[][]
        final float[][] output = new float[rows][cols];

        // Ignore special case [0x0]
        if ((rows != 0) && (cols != 0)) {
            float[] oRow;

            if (array2D instanceof byte[][]) {
                final byte[][] bArray = (byte[][]) array2D;
                byte[] bRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    bRow = bArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) (bRow[i] & 0xFF);
                    }
                }
            } else if (array2D instanceof short[][]) {
                final short[][] sArray = (short[][]) array2D;
                short[] sRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    sRow = sArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) sRow[i];
                    }
                }
            } else if (array2D instanceof int[][]) {
                final int[][] iArray = (int[][]) array2D;
                int[] iRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    iRow = iArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) iRow[i];
                    }
                }
            } else if (array2D instanceof long[][]) {
                final long[][] lArray = (long[][]) array2D;
                long[] lRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    lRow = lArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) lRow[i];
                    }
                }
            } else if (array2D instanceof double[][]) {
                final double[][] dArray = (double[][]) array2D;
                double[] dRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    dRow = dArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) dRow[i];
                    }
                }
            }
        }
        return output;
    }

    public static float[][] toFloats(final int rows, final int cols, final double[][] dArray2D) {
        // Convert data to float[][]
        final float[][] output = new float[rows][cols];

        // Ignore special case [0x0]
        if ((rows != 0) && (cols != 0)) {
            float[] oRow;
            double[] dRow;
            for (int i, j = 0; j < rows; j++) {
                oRow = output[j];
                dRow = dArray2D[j];
                for (i = 0; i < cols; i++) {
                    oRow[i] = (float) dRow[i];
                }
            }
        }
        return output;
    }

    public static double[][] toDoubles(final int rows, final int cols, final float[][] fArray2D) {
        // Convert data to double[][]
        final double[][] output = new double[rows][cols];

        // Ignore special case [0x0]
        if ((rows != 0) && (cols != 0)) {
            double[] oRow;
            float[] fRow;
            for (int i, j = 0; j < rows; j++) {
                oRow = output[j];
                fRow = fArray2D[j];
                for (i = 0; i < cols; i++) {
                    oRow[i] = (double) fRow[i];
                }
            }
        }
        return output;
    }
}
