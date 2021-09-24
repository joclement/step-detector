package dcaiti.tu_berlin.de.demoapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testShiftRightByOne() throws Exception {
        {
            float[] toBeShifted = {5,6,7,8};
            float[] result = Utils.shiftRightByOne(toBeShifted);
            float[] resultCheck = {8,5,6,7};
            assertArrayEquals(resultCheck, result, 0);
        }
        {
            float[] toBeShifted = {5};
            float[] result = Utils.shiftRightByOne(toBeShifted);
            assertArrayEquals(toBeShifted, result, 0);
        }
    }
}