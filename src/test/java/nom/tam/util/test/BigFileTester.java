package nom.tam.util.test;

import fr.nom.tam.util.BufferedDataInputStream;
import fr.nom.tam.util.BufferedFile;
import java.io.FileInputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BigFileTester {

  @Test
  public void test() throws Exception {

    // First create a 3 GB file.
    System.out.println("Big file test.  Takes quite a while.");
    byte[] buf = new byte[100000000]; // 100 MB
    BufferedFile bf = new BufferedFile("temp.out", "rw");
    byte sample = 13;

    for (int i = 0; i < 30; i += 1) {
      bf.write(buf);  // 30 x 100 MB = 3 GB.
      if (i == 24) {
        bf.write(new byte[]{sample});
      } // Add a marker.
    }
    bf.close();

    // Now try to skip within the file.
    bf = new BufferedFile("temp.out", "r");
    long skip = 2500000000L; // 2.5 G

    long val1 = bf.skipBytes(skip);
    long val2 = bf.getFilePointer();
    int val = bf.read();
    bf.close();

    assertEquals("SkipResult", skip, val1);
    assertEquals("SkipPos", skip, val2);
    assertEquals("SkipVal", (int) sample, val);

    BufferedDataInputStream bdis = new BufferedDataInputStream(
            new FileInputStream("temp.out"));
    val1 = bdis.skipBytes(skip);
    val = bdis.read();
    bdis.close();
    assertEquals("SSkipResult", skip, val1);
    assertEquals("SSkipVal", (int) sample, val);
  }
}
