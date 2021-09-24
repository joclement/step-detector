package dcaiti.tu_berlin.de.stepdetectionrecording;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class TestUtils {

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testCreateCsvFile() throws Exception {
        File folder = testFolder.newFolder();
        String filename = "test";
        File csvFile = Utils.createCsvFile(folder, filename);
        assertTrue(csvFile.exists());
        assertEquals(csvFile.getName(), filename + ".csv");
        exception.expect(RuntimeException.class);
        Utils.createCsvFile(folder, filename);
    }
}