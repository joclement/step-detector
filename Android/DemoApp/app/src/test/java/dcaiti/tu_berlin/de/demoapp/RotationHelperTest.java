package dcaiti.tu_berlin.de.demoapp;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test rotation functions for the quaternion with the accelerometer and gyroscope input.
 *
 * Created by Joris Clement on 30.01.18.
 */
public class RotationHelperTest {

    @Test
    public void testConjugate() throws Exception {
        float[] v = {0, 1, 2, 3};
        float[] toCheckSame = Arrays.copyOf(v, v.length);
        RotationHelper.conjugate(v);
        Assert.assertArrayEquals(toCheckSame, v, 0);

        float[] result = RotationHelper.conjugate(v);
        assertEquals(-1, result[1], 0);
        assertEquals(-2, result[2], 0);
        assertEquals(-3, result[3], 0);
    }

    // Ref. https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation
    @Test
    public void testMultiply() throws Exception {
        float[] v1 = {0, 1, 2, 3};
        float[] v1Copy = Arrays.copyOf(v1, v1.length);
        float[] v2 = {1, 2, 3, 4};
        float[] v2Copy = Arrays.copyOf(v2, v2.length);

        float[] result = RotationHelper.multiply(v1, v2);
        assertArrayEquals(v1Copy, v1, 0);
        assertArrayEquals(v2Copy, v2, 0);

        assertEquals(v1.length, result.length);
        float[] resultCheck = {-20, 0, 4, 2};
        assertArrayEquals(resultCheck, result, 0);
    }

    @Test
    public void testRotate() throws Exception {
        float[] v1 = {0, 1, 2, 3};
        float[] v1Copy = Arrays.copyOf(v1, v1.length);
        float[] v2 = {1, 2, 3, 4};
        float[] v2Copy = Arrays.copyOf(v2, v2.length);

        float[] result = RotationHelper.rotateQuaternion(v1, v2);
        Assert.assertArrayEquals(v1Copy, v1, 0);
        Assert.assertArrayEquals(v2Copy, v2, 0);

        assertEquals(v1.length, result.length);
        // TODO check result content
    }
}
