package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage for the configuration of the Android sensors. The configuration consists of the name
 * and a flag, which shows, whether the Sensor is required for the application to run.
 * The name is stored, because it differs for the same sensor on different phones.
 *
 * Created by Joris Clement on 17.11.17.
 */
class SensorConfig {

    private static final Map<Integer, String> sensorNames;

    static {
        sensorNames = new HashMap<>();

        setUp(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        setUp(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        setUp(Sensor.TYPE_MAGNETIC_FIELD, "MagneticField");
        setUp(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "GeoMagneticRotationVector");

        setUp(Sensor.TYPE_GRAVITY, "Gravity");
        setUp(Sensor.TYPE_ROTATION_VECTOR, "RotationVector");
        setUp(Sensor.TYPE_GAME_ROTATION_VECTOR, "GameRotationVector");
        setUp(Sensor.TYPE_STEP_DETECTOR, "StepDetector");
        setUp(Sensor.TYPE_MOTION_DETECT, "MotionDetect");
        setUp(Sensor.TYPE_SIGNIFICANT_MOTION, "SignificantMotion");

        // Just for testing
        setUp(Sensor.TYPE_HEART_RATE, "UnavailableOnSamsung6");
    }

    private static void setUp(int sensorType, String name) {
        sensorNames.put(sensorType, name);
    }

    static String getName(int type) {
        String name = sensorNames.get(type);
        if (name == null) {
            throw new RuntimeException("Sensor not configured.");
        }
        return name;
    }

    static String getName(Sensor sensor) {
        return getName(sensor.getType());
    }
}
