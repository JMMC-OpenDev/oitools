package nom.tam.fits.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/** Test reading .Z and .gz compressed files.
 */
public class CompressTester {

  @Test
  public void testgz() throws Exception {

    Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/asca/data/rev2/43021000/images/ad43021000gis25670_lo.totsky.gz");

    BasicHDU h = f.readHDU();
    int[][] data = (int[][]) h.getKernel();
    double sum = 0;
    for (int i = 0; i < data.length; i += 1) {
      for (int j = 0; j < data[i].length; j += 1) {
        sum += data[i][j];
      }
    }
    assertEquals("ZCompress", sum, 296915.);
  }

  @Test
  public void testZ() throws Exception {

    Fits f = new Fits("http://heasarc.gsfc.nasa.gov/FTP/rosat/data/pspc/processed_data/600000/rp600245n00/rp600245n00_im1.fits.Z");

    BasicHDU h = f.readHDU();
    short[][] data = (short[][]) h.getKernel();
    double sum = 0;
    for (int i = 0; i < data.length; i += 1) {
      for (int j = 0; j < data[i].length; j += 1) {
        sum += data[i][j];
      }
    }
    assertEquals("ZCompress", sum, 91806.);
  }

  @Test
  public void testStream() throws Exception {
    InputStream is;

    is = new FileInputStream("test.fits");
    assertEquals("Stream1", 300, streamRead(is, false, false));

    is = new FileInputStream("test.fits.Z");
    assertEquals("Stream2", 300, streamRead(is, false, false));

    is = new FileInputStream("test.fits.gz");
    assertEquals("Stream3", 300, streamRead(is, false, false));

    is = new FileInputStream("test.fits");
    assertEquals("Stream4", 300, streamRead(is, false, true));

    is = new FileInputStream("test.fits.Z");
    assertEquals("Stream5", 300, streamRead(is, false, true));

    is = new FileInputStream("test.fits.gz");
    assertEquals("Stream6", 300, streamRead(is, false, true));


    is = new FileInputStream("test.fits.Z");
    assertEquals("Stream7", 300, streamRead(is, true, true));

    is = new FileInputStream("test.fits.gz");
    assertEquals("Stream8", 300, streamRead(is, true, true));
  }

  @Test
  public void testFile() throws Exception {
    File is = new File("test.fits");
    assertEquals("File1", 300, fileRead(is, false, false));

    is = new File("test.fits.Z");
    assertEquals("File2", 300, fileRead(is, false, false));

    is = new File("test.fits.gz");
    assertEquals("File3", 300, fileRead(is, false, false));

    is = new File("test.fits");
    assertEquals("File4", 300, fileRead(is, false, true));

    is = new File("test.fits.Z");
    assertEquals("File7", 300, fileRead(is, true, true));

    is = new File("test.fits.gz");
    assertEquals("File8", 300, fileRead(is, true, true));
  }

  @Test
  public void testString() throws Exception {
    String is = new String("test.fits");
    assertEquals("String1", 300, stringRead(is, false, false));

    is = new String("test.fits.Z");
    assertEquals("String2", 300, stringRead(is, false, false));

    is = new String("test.fits.gz");
    assertEquals("String3", 300, stringRead(is, false, false));

    is = new String("test.fits");
    assertEquals("String4", 300, stringRead(is, false, true));

    is = new String("test.fits.Z");
    assertEquals("String7", 300, stringRead(is, true, true));

    is = new String("test.fits.gz");
    assertEquals("String8", 300, stringRead(is, true, true));
  }

  @Test
  public void testURL() throws Exception {
    String is = new String("test.fits");
    assertEquals("String1", 300, urlRead(is, false, false));

    is = new String("test.fits.Z");
    assertEquals("String2", 300, urlRead(is, false, false));

    is = new String("test.fits.gz");
    assertEquals("String3", 300, urlRead(is, false, false));

    is = new String("test.fits");
    assertEquals("String4", 300, urlRead(is, false, true));

    is = new String("test.fits.Z");
    assertEquals("String7", 300, urlRead(is, true, true));

    is = new String("test.fits.gz");
    assertEquals("String8", 300, urlRead(is, true, true));
  }

  int urlRead(String is, boolean comp, boolean useComp)
          throws Exception {
    File fil = new File(is);

    String path = fil.getCanonicalPath();
    URL u = new URL("file://" + path);

    Fits f;
    if (useComp) {
      f = new Fits(u, comp);
    } else {
      f = new Fits(u);
    }
    short[][] data = (short[][]) f.readHDU().getKernel();

    return total(data);
  }

  int streamRead(InputStream is, boolean comp, boolean useComp)
          throws Exception {
    Fits f;
    if (useComp) {
      f = new Fits(is, comp);
    } else {
      f = new Fits(is);
    }
    short[][] data = (short[][]) f.readHDU().getKernel();
    is.close();

    return total(data);
  }

  int fileRead(File is, boolean comp, boolean useComp)
          throws Exception {
    Fits f;
    if (useComp) {
      f = new Fits(is, comp);
    } else {
      f = new Fits(is);
    }
    short[][] data = (short[][]) f.readHDU().getKernel();

    return total(data);
  }

  int stringRead(String is, boolean comp, boolean useComp)
          throws Exception {
    Fits f;
    if (useComp) {
      f = new Fits(is, comp);
    } else {
      f = new Fits(is);
    }
    short[][] data = (short[][]) f.readHDU().getKernel();

    return total(data);
  }

  int total(short[][] data) {
    int total = 0;
    for (int i = 0; i < data.length; i += 1) {
      for (int j = 0; j < data[i].length; j += 1) {
        total += data[i][j];
      }
    }
    return total;
  }
}
