package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Custom utils class.
 *
 * Created by Joris Clement on 14.11.17.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    static void assertExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            throw new RuntimeException("Can't use external storage");
        }
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG) .show();
    }

    public static File createCsvFile(File folder, String logName) throws IOException {
        File logFile = new File(folder, logName + ".csv");
        checkedCreateFile(logFile);
        return logFile;
    }

    private static void checkedCreateFile(File file) throws IOException {
        if (!file.createNewFile()) {
            throw new RuntimeException("File already exists.");
        }
        Log.d(TAG, "logFile: " + file.getAbsolutePath());
    }
}
