package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import dcaiti.tu_berlin.de.demoapp.RotationHelper;
import dcaiti.tu_berlin.de.demoapp.Utils;

/**
 * Class to manage the recordings for both the android sensors and the bluetooth foot sensors.
 *
 * Created by Joris Clement on 09.11.17.
 */
class RecordManager implements DataCollector, DataGiver<List<SensorData>> {

    private static final String TAG = RecordManager.class.getSimpleName();

    @SuppressWarnings("unused")
    private static final int DEMO_VERSION = 2;

    private static final float GRAVITATIONAL_ACCELERATION = 9.81f;

    private static final int SAMPLING_RATE_IN_HZ_IN_TRAIN_DATA = 52;
    private static final int SAMPLING_PERIOD_IN_MICRO_SEC =
            1000 / SAMPLING_RATE_IN_HZ_IN_TRAIN_DATA * 1000;

    private static final int ACCELEROMETER_IDX = 0;
    private static final int GAME_ROTATION_VECTOR_IDX = 1;

    private final SensorManager sensorManager;

    private final List<SensorListener> sensorListeners;

    private long previousDataCompleteTime;

    /*
     * @note: SENSOR_TYPES are configured according to the current NN.
     *        that means that the order here matters. The entries should be in alphabetical order.
     */
    private static final int[] SENSOR_TYPES = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            // Sensor.TYPE_GYROSCOPE,
            // @note: Magnetic field is not used for further processing.
            Sensor.TYPE_MAGNETIC_FIELD
    };

    private final DataTaker<List<SensorData>> windowCreator;

    RecordManager(Context context, DataTaker<List<SensorData>> windowCreator) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        this.sensorListeners = this.initSensorListeners();

        this.windowCreator = windowCreator;
    }

    private List<SensorListener> initSensorListeners() {
        List<SensorListener> sensorListeners = new LinkedList<>();
        for (Sensor sensor : this.getSensors()) {
            SensorListener sensorListener = new SensorListener(sensor, this);
            sensorListeners.add(sensorListener);
        }
        return sensorListeners;
    }

    private static Sensor preferMpuOrMplGameRotationVectors(List<Sensor> possibleSensors) {
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

    private static Sensor choseCorrectGameRotationVector(SensorManager sensorManager) {
        List<Sensor> possibleSensors =
                sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);
        Sensor sensor = preferMpuOrMplGameRotationVectors(possibleSensors);
        if (sensor == null) {
            sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        return sensor;
    }

    // for testing
    static List<Sensor> getSensors(SensorManager sensorManager) {
        List<Sensor> sensors = new LinkedList<>();
        for (int sensorType : SENSOR_TYPES) {
            Log.d(TAG, "get sensor type: " + sensorType);
            Sensor sensor;
            switch (sensorType) {
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    sensor = choseCorrectGameRotationVector(sensorManager);
                    break;
                default:
                    sensor = sensorManager.getDefaultSensor(sensorType);
            }
            if (sensor != null) {
                sensors.add(sensor);
            } else {
                // TODO improve exception handling
                String msg = "Sensor " + SensorConfig.getName(sensorType) + " not present.";
                throw new RuntimeException(msg);
            }
        }
        return sensors;
    }

    private List<Sensor> getSensors() {
        return getSensors(this.sensorManager);
    }

    // TODO move start to constructor, if possible.
    void start() {
        for (SensorListener sensorListener : this.sensorListeners) {
            Log.d(TAG, "start recording for sensor: ");
            if (!this.sensorManager.registerListener(sensorListener, sensorListener.sensor,
                    SAMPLING_PERIOD_IN_MICRO_SEC)) {
                throw new RuntimeException("Failed to set up sensor listening.");
            }
        }
    }

    void stop() {
        for (SensorListener sensorListener : this.sensorListeners) {
            this.sensorManager.unregisterListener(sensorListener);
        }
        this.sensorListeners.clear();
    }

    @Override
    public void onNewData() {
        for (DataProducer sensorListener : this.sensorListeners) {
            if (!sensorListener.hasData()) {
                return;
            }
        }
        long currentDataCompleteTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time between 2 data sets: " +
                  (currentDataCompleteTime - this.previousDataCompleteTime));
        previousDataCompleteTime = currentDataCompleteTime;
        this.windowCreator.onNewData(this.buildDataTuple());
    }

    // @note: just works for the current NN
    private List<SensorData> buildDataTuple() {
        List<SensorData> dataTuple = new LinkedList<>();
        float[] accelerometerData = normalizeAccelerometer(
                this.sensorListeners.get(ACCELEROMETER_IDX).pop().values,
                this.sensorListeners.get(GAME_ROTATION_VECTOR_IDX).pop().values);
        dataTuple.add(new SensorData(accelerometerData));
        return dataTuple;
    }

    /**
     * normalize the accelerometer:
     *  - rotate it with the game rotation vector
     *  - deduct the gravity from the z part
     *  - move the range of value roughly to 0 to 1 by dividing by 8
     *
     * @param accelerometerValues accelerometer sensor values
     * @param gameRotationVecValues game rotation sensor values
     * @return
     */
    private float[] normalizeAccelerometer(float[] accelerometerValues,
                                           float[] gameRotationVecValues) {
        float[] shiftedGameRotationVecValues = swapPositionOfWAndZ(gameRotationVecValues);
        float[] wAndAccelerometerValues = new float[4];
        System.arraycopy(accelerometerValues, 0,
                         wAndAccelerometerValues, 1, accelerometerValues.length);
        float[] wAndNormalizedAccelerometer =
                RotationHelper.rotateQuaternion(wAndAccelerometerValues,
                                                shiftedGameRotationVecValues);
        float[] normalizedAccelerometer = this.removeW(wAndNormalizedAccelerometer);

        this.deductGravity(normalizedAccelerometer);

        for (int i = 0; i < normalizedAccelerometer.length; i++) {
            normalizedAccelerometer[i] /= 8;
        }

        return normalizedAccelerometer;
    }

    private void deductGravity(float[] normalizedAccelerometer) {
        normalizedAccelerometer[2] -= GRAVITATIONAL_ACCELERATION;
    }

    private float[] removeW(float[] wAndNormalizedAccelerometer) {
        float[] normalizedAccelerometer = new float[wAndNormalizedAccelerometer.length - 1];
        System.arraycopy(wAndNormalizedAccelerometer, 1,
                         normalizedAccelerometer, 0, normalizedAccelerometer.length);
        return normalizedAccelerometer;
    }

    private float[] swapPositionOfWAndZ(float[] gameRotationVecValues) {
        return Utils.shiftRightByOne(gameRotationVecValues);
    }
}
