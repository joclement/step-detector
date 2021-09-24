package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RecordManagerTest {

    private Context appContext;

    @Before
    public void init() {
        this.appContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void cleanup() {
        this.appContext = null;
    }

    private final Comparator<Sensor> sensorComparator = new Comparator<Sensor>() {

        @Override
        public int compare(Sensor sensor1, Sensor sensor2) {
            String name1 = SensorConfig.getName(sensor1);
            String name2 = SensorConfig.getName(sensor2);
            return name1.compareTo(name2);
        }
    };

    @Test
    public void checkSensorTypesOrder() throws Exception {
        SensorManager sensorManager =
                (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sortedSensors = RecordManager.getSensors(sensorManager);
        Collections.sort(sortedSensors, sensorComparator);
        assertEquals(sortedSensors, RecordManager.getSensors(sensorManager));
    }
}
