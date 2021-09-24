package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Skeleton of the Neural Network Manager Class
 *
 * Created by Joris Clement on 18.01.18.
 */
class NNManager implements DataTaker<List<List<float[]>>>, DataProducer<ClassifyResult> {

    private static final String TAG = NNManager.class.getSimpleName();

    @SuppressWarnings("unused")
    private static final String CN_FILE_PATH = "convolutional-network-model.tflite";
    @SuppressWarnings("unused")
    private static final String MLP_FILE_PATH = "mlp-model.tflite";
    private static final String MODEL_FILE_PATH = MLP_FILE_PATH;

    private final DataCollector<Boolean> stepDetectionManager;

    private static final int OUTPUT_LEN = 2;

    /** multi-stage low pass filter * */
    private final float[][] filterProbabilities;
    private static final int FILTER_STAGES = 1;  // 1 = disabled
    private static final float FILTER_FACTOR = 1.f;  // 1 = disabled

    private Interpreter interpreter;

    private ByteBuffer data;

    private ClassifyResult result;
    private long classificationStartTime;
    private long classificationEndTime;

    NNManager(DataCollector<Boolean> stepDetectionManager, Context context, int windowSize)
            throws IOException {
        this.stepDetectionManager = stepDetectionManager;
        this.result = null;
        this.filterProbabilities = new float[FILTER_STAGES][OUTPUT_LEN];
        classificationStartTime = classificationEndTime = SystemClock.uptimeMillis();

        this.interpreter = new Interpreter(loadModelFile(context));
        this.data = ByteBuffer.allocateDirect(Float.BYTES * windowSize * 3);
        this.data.order(ByteOrder.nativeOrder());
    }

    @Override
    public void onNewData(List<List<float[]>> data) {
        this.data.clear();
        for (List<float[]> sensorsData: data) {
            for (float[] sensorData : sensorsData) {
                for (float value : sensorData) {
                    this.data.putFloat(value);
                }
            }
        }

        this.updateResult(this.classify());
    }

    private void validateNNOutput(final float[][] resultProbabilities)
    {
        // Validates that (resultProbabilities[0][0] + resultProbabilities[0][1]) == 1.
        float sum = resultProbabilities[0][0] + resultProbabilities[0][1];
        float tolerance = 0.001f;
        if (!(sum <= 1 + tolerance && sum >= 1 - tolerance)) throw new AssertionError();
    }

    private ClassifyResult classify() {
        float[][] resultProbabilities = new float[1][OUTPUT_LEN];

        classificationStartTime = SystemClock.uptimeMillis();
        // calc time diff to previous classification result
        long timeDiff = classificationStartTime - classificationEndTime;
        this.interpreter.run(this.data, resultProbabilities);
        classificationEndTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time cost to run model inference: " + Long.toString(classificationEndTime - classificationStartTime));

        validateNNOutput(resultProbabilities);

        return this.createResult(this.filterResult(resultProbabilities), timeDiff);
    }

    private ClassifyResult createResult(float[][] resultProbabilities, long timeDiff) {
        float notWalking = resultProbabilities[0][0];
        float walking = resultProbabilities[0][1];
        return new ClassifyResult(notWalking, walking, timeDiff);
    }

    private void updateResult(ClassifyResult result) {
        this.result = result;

        this.stepDetectionManager.onNewData();
    }

    @Override
    public boolean hasData() {
        return this.result != null;
    }

    @Override
    public ClassifyResult pop() {
        ClassifyResult result = this.result;
        this.result = null;
        return result;
    }

    /**
     * Code from the tensorflow lite demo
     * Ref: https://github.com/tensorflow/tensorflow/tree/master/tensorflow/contrib/lite/java/demo
     */
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Code from the tensorflow lite demo
     * Ref: https://github.com/tensorflow/tensorflow/tree/master/tensorflow/contrib/lite/java/demo
     */
    private float[][] filterResult(float[][] probabilities) {
        // Low pass filter `labelProbArray` into the first stage of the filter.
        for (int j = 0; j < OUTPUT_LEN; ++j) {
            filterProbabilities[0][j] +=
                    FILTER_FACTOR * (probabilities[0][j] - filterProbabilities[0][j]);
        }

        // Low pass filter each stage into the next.
        for (int i = 1; i < FILTER_STAGES; ++i) {
            for (int j = 0; j < OUTPUT_LEN; ++j) {
                filterProbabilities[i][j] +=
                        FILTER_FACTOR * (filterProbabilities[i - 1][j] - filterProbabilities[i][j]);
            }
        }

        // Copy the last stage filter output back to `labelProbArray`.
        System.arraycopy(filterProbabilities[FILTER_STAGES - 1], 0, probabilities[0], 0, OUTPUT_LEN);

        return probabilities;
    }

    void close() {
        this.interpreter.close();
        this.interpreter = null;
    }
}
