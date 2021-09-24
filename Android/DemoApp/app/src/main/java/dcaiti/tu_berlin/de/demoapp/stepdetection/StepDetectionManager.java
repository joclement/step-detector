package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.content.Context;

import java.io.IOException;
import java.util.Observable;

/**
 * Manages/holds all the parts/classes needed for the step detection logic.
 * This class is the mainly initializes the internals and passes the detection result to the
 * outside.
 *
 * Created by Joris Clement on 18.01.18.
 */
public class StepDetectionManager extends Observable implements DataCollector<Boolean> {

    private static final int WINDOW_SIZE = 120;

    private final NNManager nnManager;

    private final RecordManager recordManager;

    public StepDetectionManager(Context context) throws IOException {
        this.nnManager = new NNManager(this, context, WINDOW_SIZE);
        WindowCreator windowCreator = new WindowCreator(this.nnManager, WINDOW_SIZE);
        this.recordManager = new RecordManager(context, windowCreator);
    }

    // TODO this can be avoided maybe
    public void start() {
        this.recordManager.start();
    }

    // TODO this can be avoided maybe
    public void stop() {
        this.recordManager.stop();
        this.nnManager.close();
    }

    public ClassifyResult getResult() {
        return this.nnManager.pop();
    }

    @Override
    public void onNewData() {
        this.setChanged();
        this.notifyObservers();
    }
}
