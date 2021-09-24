package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// Note: Dropped code here due to removal of Copyright protected file
import dcaiti.tu_berlin.de.stepdetectionrecording.footsensor.FootSensorStatus;


/**
 * Activity to trigger the sensor recording process and show status information about it.
 *
 * Created by Joris Clement on 27.11.17.
 */
public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private static final long STATUS_UPDATE_DELAY = 1000;

    private Intent intentRecorder;

    private RecordService service;

    private int counter = 0;

    private final Handler footSensorStatusHandler = new Handler();

    private final Runnable footSensorStatusSyncer = new Runnable() {

        @Override
        public void run() {
            RecordActivity.this.setFootSensorStatus();
            RecordActivity.this.footSensorStatusHandler.postDelayed(this, STATUS_UPDATE_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        this.addRecordButtonListener();
        Log.d(TAG, "finished onCreate");

        this.prepare();
        this.startServiceOnce();
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            RecordActivity.this.service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        this.bindService(intentRecorder, connection, Context.BIND_AUTO_CREATE);
        this.footSensorStatusHandler.postDelayed(this.footSensorStatusSyncer, STATUS_UPDATE_DELAY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(connection);
        this.footSensorStatusHandler.removeCallbacks(this.footSensorStatusSyncer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopService();
    }

    private void addRecordButtonListener() {
        final Button recordStop = (Button) findViewById(R.id.recordStop);
        recordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
                finish();
            }
        });
    }

    private void startServiceOnce() {
        if (this.intentRecorder == null) {
            Log.d(TAG, "starting service for the first time.");
            this.intentRecorder = new Intent(this.getApplicationContext(), RecordService.class);
            if (startService(intentRecorder) == null) {
                Utils.showToast(this, "Failed to start service");
            } else {
                Utils.showToast(this, "Started service");
            }
        } else {
            Log.d(TAG, "Activity has been reactivated.");
            // FIXME toast just for debug
            Utils.showToast(this, "Service should be running.");
        }
    }

    private void stopService() {
        if (stopService(intentRecorder)) {
            Utils.showToast(this, "Stopped service");
        }
    }

    private void prepare() {
        // Note: Dropped code here due to removal of Copyright protected file
    }

    private void setFootSensorStatus() {
        final TextView leftFoot = (TextView) findViewById(R.id.leftFootStatus);
        final TextView rightFoot = (TextView) findViewById(R.id.rightFootStatus);

        Pair<FootSensorStatus, FootSensorStatus> status = this.service.getFootSensorStatus();
        leftFoot.setText(String.format("left: %s, %d", status.first.name(), counter));
        rightFoot.setText(String.format("right: %s, %d", status.second.name(), counter));
        counter += 1;
    }
}
