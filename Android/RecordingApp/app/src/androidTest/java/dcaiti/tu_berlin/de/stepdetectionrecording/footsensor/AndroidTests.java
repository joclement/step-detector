package dcaiti.tu_berlin.de.stepdetectionrecording.footsensor;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test FootSensor Code.
 *
 * Created by Joris Clement on 08.01.18.
 */
@RunWith(AndroidJUnit4.class)
public class AndroidTests {

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testFootSensorSideCorrect() throws IOException {
        FootSensor leftFoot = new FootSensor(true, testFolder.newFolder());
        assertTrue(leftFoot.isLeft());
        assertFalse(leftFoot.isRight());

        FootSensor rightFoot = new FootSensor(false, testFolder.newFolder());
        assertTrue(rightFoot.isRight());
        assertFalse(rightFoot.isLeft());

        assertFalse(leftFoot.side().equals(rightFoot.side()));

        // static functions
        assertFalse(FootSensor.isLeft("unknown"));
        assertFalse(FootSensor.isRight("unknown"));

        assertFalse(FootSensor.side("unknown").equals(rightFoot.mac));
        assertFalse(FootSensor.side("unknown").equals(leftFoot.mac));
    }
}
