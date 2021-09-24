package dcaiti.tu_berlin.de.stepdetectionrecording;

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

    private static final Map<Integer, Boolean> sensorRequired;

    static {
        sensorNames = new HashMap<>();
        sensorRequired = new HashMap<>();

        setUp(Sensor.TYPE_ACCELEROMETER, "Accelerometer", true);
        setUp(Sensor.TYPE_GYROSCOPE, "Gyroscope", true);
        setUp(Sensor.TYPE_MAGNETIC_FIELD, "MagneticField", false);
        setUp(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "GeoMagneticRotationVector", false);

        setUp(Sensor.TYPE_GRAVITY, "Gravity", false);
        setUp(Sensor.TYPE_ROTATION_VECTOR, "RotationVector", false);
        setUp(Sensor.TYPE_GAME_ROTATION_VECTOR, "GameRotationVector", true);
        setUp(Sensor.TYPE_STEP_DETECTOR, "StepDetector", false);
        setUp(Sensor.TYPE_SIGNIFICANT_MOTION, "SignificantMotion", false);

        // Just for testing
        setUp(Sensor.TYPE_HEART_RATE, "UnavailableOnSamsung6", true);
    }

    private static void setUp(int sensorType, String name, boolean required) {
        sensorNames.put(sensorType, name);
        sensorRequired.put(sensorType, required);
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

    static boolean isRequired(int type) {
        if (!sensorRequired.containsKey(type)) {
            throw new RuntimeException("Sensor not configured.");
        }
        return sensorRequired.get(type);
    }
}
