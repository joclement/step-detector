package dcaiti.tu_berlin.de.demoapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import dcaiti.tu_berlin.de.demoapp.stepdetection.ClassifyResult;
import dcaiti.tu_berlin.de.demoapp.stepdetection.StepDetectionManager;

/**
 * Show simple demo for the Tensorflow trained step detection model.
 *
 * Created by Joris Clement on 17.01.18.
 */
public class DemoActivity extends Activity implements Observer {

    private StepDetectionManager stepDetectionManager;

    private int counter;

    private LineGraphSeries<DataPointInterface> walkingProbSeries;
    private LineGraphSeries<DataPointInterface> classificationRateSeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            this.stepDetectionManager = new StepDetectionManager(this);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO improve exception handling
            throw new RuntimeException(e.getMessage());
        }
        this.stepDetectionManager.addObserver(this);
        this.stepDetectionManager.start();

        setContentView(R.layout.activity_demo);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        walkingProbSeries = new LineGraphSeries<>();
        classificationRateSeries = new LineGraphSeries<>();
        classificationRateSeries.setColor(Color.parseColor("#8BC34A"));

        graph.addSeries(classificationRateSeries);
        graph.addSeries(walkingProbSeries);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0d);
        graph.getViewport().setMaxY(100d);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(500);

        walkingProbSeries.setTitle("Walking Probability");
        classificationRateSeries.setTitle("Classification Rate [Hz]");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


        this.addStopButtonListener();

        this.counter = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.stepDetectionManager.stop();
    }

    private void addStopButtonListener() {
        final Button recordStop = (Button) findViewById(R.id.stopDemo);
        recordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setWalking(ClassifyResult result) {
        String resultText = result.isWalking() ? getString(R.string.walking) : getString(R.string.notWalking);
        String text = counter++ + "\n";
        text += resultText + "\n";
        text += String.format("Walking: %02.2f%%\n", result.walkingProbability * 100);
        text += String.format("Not Walking: %02.2f%%", result.notWalkingProbability * 100);
        walkingProbSeries.appendData(new DataPoint(counter, result.walkingProbability * 100),
                true, 5000);
        classificationRateSeries.appendData(new DataPoint(counter, result.classificationRate()),
                true, 5000);

        TextView walkingView = (TextView) findViewById(R.id.showWalking);
        walkingView.setText(text);
    }

    @Override
    public void update(Observable observable, Object o) {
        ClassifyResult result = this.stepDetectionManager.getResult();
        this.setWalking(result);
    }
}
