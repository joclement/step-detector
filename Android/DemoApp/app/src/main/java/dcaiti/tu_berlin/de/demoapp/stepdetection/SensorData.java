package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Container for the SensorData. This class makes it easy to add additional fields later, if wished.
 *
 * Created by Joris Clement on 17.01.18.
 */
class SensorData {

    final float[] values;

    SensorData(float[] values) {
        this.values = values;
    }
}
