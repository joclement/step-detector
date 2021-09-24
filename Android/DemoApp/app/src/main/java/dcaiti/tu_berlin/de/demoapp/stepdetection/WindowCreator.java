package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class to build up a window, which should be put into the the Neural Network
 *
 * Created by Joris Clement on 17.01.18.
 */
class WindowCreator implements DataTaker<List<SensorData>>, DataGiver<List<List<float[]>>> {

    private static final String TAG = WindowCreator.class.getSimpleName();

    private final DataTaker<List<List<float[]>>> nnManager;

    private final ArrayBlockingQueue<List<SensorData>> window;

    WindowCreator(DataTaker<List<List<float[]>>> nnManager, int size) {
        this.window = new ArrayBlockingQueue<>(size);
        this.nnManager = nnManager;
    }

    private void add(List<SensorData> data) {
        if (!this.window.offer(data)) {
            Log.i(TAG, "Too many inputs for WindowCreator");
        }
        if (this.windowReady()) {
            nnManager.onNewData(this.pop());
        }
    }

    @Override
    public void onNewData(List<SensorData> data) {
        this.add(data);
    }

    private List<List<float[]>> getWindow() {
        List<List<float[]>> window = new ArrayList<>();
        // TODO check if this access needs to be locked that the content is not changed.
        for (List<SensorData> dataTuple : this.window) {
            List<float[]> dataTupleConverted = new ArrayList<>(dataTuple.size());
            for (SensorData sensorData : dataTuple) {
                dataTupleConverted.add(sensorData.values);
            }
            window.add(dataTupleConverted);
        }
        return window;
    }

    private boolean windowReady() {
        return this.window.remainingCapacity() == 0;
    }

    private List<List<float[]>> pop() {
        List<List<float[]>> window = this.getWindow();
        if (this.window.poll() == null) throw new AssertionError();
        return window;
    }
}
