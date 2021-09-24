package dcaiti.tu_berlin.de.demoapp;

import java.util.Arrays;

/**
 * Class containing quaternion rotation function for the accelerometer and game rotation vector
 * rotation.
 *
 * Code functionality is equivalent and should be kept in sync to the rotation python code
 * in the utils.py file.
 */
public class RotationHelper {

    public static float[] rotateQuaternion(float[] vector, float[] quats) {
        if (vector.length != quats.length) throw new AssertionError();
        if (vector.length != 4) throw new AssertionError();

        return multiply(multiply(quats, vector), conjugate(quats));
    }

    static float[] multiply(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) throw new AssertionError();
        if (vector1.length != 4) throw new AssertionError();

        float nw = vector1[0] * vector2[0] -
                   vector1[1] * vector2[1] -
                   vector1[2] * vector2[2] -
                   vector1[3] * vector2[3];
        float nx = vector1[0] * vector2[1] +
                   vector1[1] * vector2[0] +
                   vector1[2] * vector2[3] -
                   vector1[3] * vector2[2];
        float ny = vector1[0] * vector2[2] +
                   vector1[2] * vector2[0] +
                   vector1[3] * vector2[1] -
                   vector1[1] * vector2[3];
        float nz = vector1[0] * vector2[3] +
                   vector1[3] * vector2[0] +
                   vector1[1] * vector2[2] -
                   vector1[2] * vector2[1];

        float [] result = new float[vector1.length];

        result[0] = nw;
        result[1] = nx;
        result[2] = ny;
        result[3] = nz;

        return result;
    }

    static float[] conjugate(float[] v) {
        if (v.length != 4) throw new AssertionError();

        float[] result = Arrays.copyOf(v, v.length);
        result[1] = -v[1];
        result[2] = -v[2];
        result[3] = -v[3];
        return result;
    }
}
