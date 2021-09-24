package dcaiti.tu_berlin.de.demoapp;


/**
 * Custom Utils class.
 *
 * Created by Joris Clement on 14.11.17.
 */
public class Utils {

    @SuppressWarnings("unused")
    private static final String TAG = Utils.class.getSimpleName();

    public static float[] shiftRightByOne(final float[] ary) {
        float[] result = new float[ary.length];
        System.arraycopy(ary, 0, result, 1, ary.length - 1);
        result[0] = ary[ary.length - 1];
        return result;
    }
}
