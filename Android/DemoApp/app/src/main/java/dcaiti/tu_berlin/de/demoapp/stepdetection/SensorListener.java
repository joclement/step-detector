package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


/**
 * Class, which listens to 1 sensor and stores the data until it is gathered by the DataCollector.
 *
 * Created by Joris Clement on 09.11.17.
 */
public class SensorListener implements SensorEventListener, DataProducer<SensorData> {

    @SuppressWarnings("unused")
    private static final String TAG = SensorListener.class.getSimpleName();

    final Sensor sensor;

    private final DataCollector consumer;

    private SensorData data;

    SensorListener(Sensor sensor, DataCollector consumer) {
        this.sensor = sensor;
        this.consumer = consumer;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.data = new SensorData(event.values);
        this.consumer.onNewData();
    }

    @Override
    public boolean hasData() {
        return this.data != null;
    }

    @Override
    public SensorData pop() {
        SensorData tmp =  this.data;
        this.data = null;
        return tmp;
    }
}
