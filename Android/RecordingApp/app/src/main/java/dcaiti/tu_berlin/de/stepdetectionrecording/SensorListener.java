package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;


/**
 * Class to log sensor event and extra data from Android Sensors.
 *
 * Created by Joris Clement on 09.11.17.
 */
class SensorListener implements SensorEventListener {

    private static final String TAG = SensorListener.class.getSimpleName();

    private final Sensor sensor;

    private final Logger logger;
    private final Logger extraInfoLogger;

    SensorListener(Sensor sensor, File logFolder) throws IOException {
        this.sensor = sensor;
        this.logger = new Logger(Utils.createCsvFile(logFolder, SensorConfig.getName(sensor)));
        this.extraInfoLogger =
                new Logger(Utils.createCsvFile(logFolder, SensorConfig.getName(sensor) + "-extra"));
        this.writeHeader();
        this.writeExtraInfoHeader();
    }

    /**
     * FIXME do we need a header at all for the sensors,
     * because it could be different for every sensor
     */
    private void writeHeader() throws IOException {
        String csv_header = "arrival_ts,event_ts,val_x,val_y,val_z";
        if (this.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            csv_header += ",val_w";
        }
        this.logger.writeln(csv_header);
    }

    private void writeExtraInfoHeader() throws IOException {
        String csv_header = "arrival_ts,accuracy";
        this.extraInfoLogger.writeln(csv_header);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.v(TAG, sensor.getName() + " changed accuracy to " + accuracy);
        try {
            this.extraInfoLogger.write(SystemClock.elapsedRealtimeNanos() + ",");
            this.extraInfoLogger.writeln(accuracy + "");
        } catch (IOException e) {
            Log.e(TAG, "onAccuracyChanged IOException");
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Log.v(TAG, event.sensor.getName() + " onSensorChanged");
            this.logger.write(SystemClock.elapsedRealtimeNanos() + ",");
            this.logger.write(event.timestamp + ",");

            this.logger.write(event.values[0] + "");
            for (int i = 1; i < event.values.length; i++) {
                this.logger.write("," + event.values[i]);
            }
            this.logger.writeln("");
        } catch (IOException e) {
            // TODO improve exception handling
            Log.e(TAG, "onSensorChanged IOException");
            e.printStackTrace();
        }
    }

    Sensor getSensor() {
        return this.sensor;
    }

    void close() {
        this.logger.close();
        this.extraInfoLogger.close();
    }
}
