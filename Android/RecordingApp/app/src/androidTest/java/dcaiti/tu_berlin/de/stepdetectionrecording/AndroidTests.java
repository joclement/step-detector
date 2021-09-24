package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AndroidTests {

    private Context appContext;

    @Before
    public void init() {
        this.appContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void cleanup() {
        this.appContext = null;
    }

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    private void makeSettingsAlwaysDefault(SharedPreferences sharedPref, Context appContext) {
        sharedPref.edit().clear().commit();
        PreferenceManager.setDefaultValues(appContext, R.xml.preferences, true);
    }

    private Sensor createSensor(int sensorType) {
        SensorManager sensorManager =
                (SensorManager) this.appContext.getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        return sensorManager.getDefaultSensor(sensorType);
    }

    @Test
    public void testDefaultSettings() throws Exception {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        makeSettingsAlwaysDefault(sharedPref, appContext);

        assertEquals(false, SettingsActivity.areAllSettingsValuesChanged(appContext));

        sharedPref.edit().putString(SettingsActivity.PREF_AGE, "22").commit();
        assertEquals(false, SettingsActivity.areAllSettingsValuesChanged(appContext));

        sharedPref.edit().putString(SettingsActivity.PREF_HEIGHT, "111").commit();
        assertEquals(false, SettingsActivity.areAllSettingsValuesChanged(appContext));

        sharedPref.edit().putString(SettingsActivity.PREF_NAME, "Name").commit();
        assertEquals(false, SettingsActivity.areAllSettingsValuesChanged(appContext));

        sharedPref.edit().putString(SettingsActivity.PREF_GENDER, "Male").commit();
        assertEquals(true, SettingsActivity.areAllSettingsValuesChanged(appContext));
    }

    @Test
    public void testRecordManager() throws IOException, InterruptedException {
        RecordManager manager = new RecordManager(appContext);

        manager.start();
        manager.stop();
    }

    private int countLines(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int lines = 0;
        while (reader.readLine() != null) {
            lines += 1;
        }
        reader.close();

        return lines;
    }

    private File[] filterFilesByExtension(File folder, final String ending) {
        return folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(ending);
            }
        });
    }

    @Test
    public void testSensorListener() throws IOException {
        Sensor sensor = createSensor(Sensor.TYPE_ACCELEROMETER);
        File logFolder = testFolder.newFolder();
        assertEquals(0, logFolder.listFiles().length);

        SensorListener sensorListener = new SensorListener(sensor, logFolder);
        assertEquals(2, logFolder.listFiles().length);
        sensorListener.close();

        File[] csvFiles = filterFilesByExtension(logFolder, ".csv");
        assertEquals(2, csvFiles.length);

        assertEquals(1, countLines(csvFiles[0]));
        assertEquals(1, countLines(csvFiles[1]));
    }

    @Test
    public void testLogging() throws IOException {
        File logFile = File.createTempFile("test", "test");
        Logger logger = new Logger(logFile);
        String data = "Test";
        logger.writeln(data);
        logger.close();

        BufferedReader in = new BufferedReader(new FileReader(logFile));
        assertEquals(in.readLine(), data);
    }
}
