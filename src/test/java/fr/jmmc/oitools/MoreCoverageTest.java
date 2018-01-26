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

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIVis;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * 
 * @author kempsc
 */
public class MoreCoverageTest extends JUnitBaseTest {

    @Test
    public void hdu() {
        /*
    FitsHDU:
    KeywordMeta getKeywordDesc(final int index) (OImaging)
    boolean getKeywordLogical() pour ImageOIDataInput (OImaging)
    void updateKeyword(final String name, final String strValue) (OImaging)
         */
        FitsImageHDU imageHdu = new FitsImageHDU();
        imageHdu.getKeywordDesc(imageHdu.getExtNb());

        imageHdu.getKeywordLogical("HDUNAME");
        imageHdu.getKeywordLogical("HDUNAME", true);

        /*
    FitsImageHDU
    getHduName (OImaging)
    setHduName (OImaging)
         */
        if (imageHdu.getHduName() == null) {
            imageHdu.setHduName("HDUNAME");
        }
        imageHdu.updateKeyword("HDUNAME", "HDUNAME");
    }

    @Test
    public void colunm() {
        /*
    FitsTable
    getNumericalColumnsNames(final Set<String> columnNames)
    getColumnMeta(final String name)
         */
        FitsTable table = new OIVis(new OIFitsFile(OIFitsStandard.VERSION_1));
        Set<String> columnNames = new HashSet<String>();

        for (ColumnMeta columnMeta : table.getAllColumnDescCollection()) {
            table.getColumnMeta(columnMeta.getName());
            columnNames.add(columnMeta.getName());
        }
        table.getNumericalColumnsNames(columnNames);
    }

    /*
    OIFitsCorrChecker : TODO INFO ?
    getOriginAsString(final Integer index)
     */
 /*
    JELEval (P1)
    JELColumn(final String name, final double[][] values2D)
     */
 /*
    FitsImageLoader (conversion = P2)
    getPlaneData(final Object array3D, final int bitpix, final int imageIndex)
    getImageData(final int rows, final int cols, final int bitpix, final Object array2D, final double bZero, final double bScale)
     */
 /*
    OIFitsWriter (P3)
    createHDUnits(final Fits fitsFile) if (nbImageHDus > 1)

    ImageOiInputParam (OImaging) (P3)
    addSubTable 
    getSubTable 
     */
 /*
    OIFitsExplorer analyze (P4)
    Granule
    getField

    Instrument mode
    InstrumentMode
    
    Target
    public Target(final Target t)

    OIAbstractData (P4)
    getNbMeasurements()
    hasSingleTarget()
    getDistinctStaIndexCount()
    getDistinctStaIndexes()
    getTargetId(final String target)

    OIData (P4)
    getDateObs()
    removeExpressionColumn (final String name)
    getEffWaveRange ()

    OIFitsFile (P4)
    removeOiTable(final OIData oiTable)
    getMinWavelengthBound() (OImaging) TO KILL
    getMaxWavelengthBound() (OImaging) TO KILL
    getImageOiData() (OImaging)

    FitsUnit (P5)
    getAngleUnit(final double angle)
     */
}
