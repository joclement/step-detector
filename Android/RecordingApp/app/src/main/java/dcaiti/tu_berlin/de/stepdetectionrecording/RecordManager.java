package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

// Note: Dropped code here due to removal of Copyright protected file
import dcaiti.tu_berlin.de.stepdetectionrecording.footsensor.FootSensorStatus;

/**
 * Class to manage the recordings for both the android sensors and the bluetooth foot sensors.
 *
 * Created by Joris Clement on 09.11.17.
 */
class RecordManager {

    private static final String TAG = RecordManager.class.getSimpleName();

    private static final int RECORD_VERSION = 3;

    private final SensorManager sensorManager;

    private List<SensorListener> sensorListeners;

    private static final int[] RECORD_SENSOR_TYPES = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_STEP_DETECTOR
    };

    // Note: Dropped code here due to removal of Copyright protected file

    private final Context context;

    RecordManager(Context context) throws IOException {
        this.context = context;

        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.init();
    }

    private void init() throws IOException {
        File logFolder = this.logFolder();
        this.writeMeta(logFolder);

        this.initFootSensors(logFolder);

        this.sensorListeners = this.initSensorListeners(logFolder);
    }

    private void initFootSensors(File logFolder) throws IOException {
        // Note: Dropped code here due to removal of Copyright protected file
    }

    private List<SensorListener> initSensorListeners(File logFolder) throws IOException {
        List<SensorListener> sensorListeners = new LinkedList<>();
        for (Sensor sensor: this.getSensors()) {
            SensorListener sensorListener = new SensorListener(sensor, logFolder);
            sensorListeners.add(sensorListener);
        }
        return sensorListeners;
    }

    private Sensor preferMpuOrMplGameRotationVectors(List<Sensor> possibleSensors) {
        Sensor sensor = null;
        for (int i = 0; i < possibleSensors .size(); i++) {
            if (possibleSensors .get(i).getName().toLowerCase().contains("mpu") ||  possibleSensors
                    .get(i).getName().toLowerCase().contains("mpl")) {
                sensor = possibleSensors.get(i);
                Log.d(TAG, "Found: " + sensor.getName());
                break;
            }
        }
        return sensor;
    }

    private Sensor choseCorrectGameRotationVector() {
        List<Sensor> possibleSensors =
                this.sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Sensor sensor = preferMpuOrMplGameRotationVectors(possibleSensors);
        if (sensor == null) {
            this.sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        return sensor;
    }

    private List<Sensor> getSensors() {
        List<Sensor> sensors = new LinkedList<>();
        for (int sensorType : RECORD_SENSOR_TYPES) {
            Log.d(TAG, "get sensor type: " + sensorType);
            Sensor sensor;
            switch (sensorType) {
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    sensor = choseCorrectGameRotationVector();
                    break;
                default:
                    sensor = this.sensorManager.getDefaultSensor(sensorType);
            }
            if (sensor != null) {
                sensors.add(sensor);
            } else if (SensorConfig.isRequired(sensorType)) {
                // TODO improve exception handling
                String msg = "Sensor " + SensorConfig.getName(sensorType) + " not present.";
                Utils.showToast(this.context, msg);
                throw new RuntimeException(msg);
            }
        }
        return sensors;
    }

    void start() {
        for (SensorListener sensorListener : this.sensorListeners) {
            Log.d(TAG, "start recording for sensor: " +
                    SensorConfig.getName(sensorListener.getSensor()));
            this.sensorManager.registerListener(sensorListener, sensorListener.getSensor(),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // Note: Dropped code here due to removal of Copyright protected file
    }

    void stop() {
        for (SensorListener sensorListener : this.sensorListeners) {
            this.sensorManager.unregisterListener(sensorListener);
            sensorListener.close();
        }
        this.sensorListeners.clear();
        // Note: Dropped code here due to removal of Copyright protected file
        // Note: Dropped code here due to Copyright
    }

    private File logFolder() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        String name = sharedPref.getString(SettingsActivity.PREF_NAME, null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date now = Calendar.getInstance().getTime();
        String folderPath = "recordings" + File.separator + name.toLowerCase() + "_record" + sdf.format(now);
        File logFolder = new File(this.context.getExternalFilesDir(null),
                                  folderPath);
        if (!logFolder.mkdirs()) throw new RuntimeException("io fault.");
        Log.d(TAG, "logFolder: " + logFolder.getAbsolutePath());
        return logFolder;
    }

    private void writeMeta(File logFolder) throws IOException {
        File meta = new File(logFolder, "meta.txt");
        StringBuilder builder = new StringBuilder();
        String n = System.lineSeparator();

        builder.append("Meta: ").append(n);
        builder.append("record version: ").append(RECORD_VERSION).append(n);
        builder.append("now: ").append(SystemClock.elapsedRealtimeNanos()).append(n);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        for (String pref : SettingsActivity.PREFS) {
            String prefValue = sharedPref.getString(pref, null);
            builder.append(pref).append(": ").append(prefValue).append(n);
        }
        if (!meta.createNewFile()) throw new RuntimeException("io fault.");
        try (FileWriter writer = new FileWriter(meta)) {
            writer.write(builder.toString());
        }
    }

    // Note: Dropped code here due to removal of Copyright protected file
}
