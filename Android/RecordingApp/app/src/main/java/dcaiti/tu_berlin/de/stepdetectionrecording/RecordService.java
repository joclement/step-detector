package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;

import dcaiti.tu_berlin.de.stepdetectionrecording.footsensor.FootSensorStatus;

/**
 * Service to hold the sensor recording process.
 *
 * Created by Joris Clement on 13.11.17.
 */
public class RecordService extends Service {

    private static final String TAG = RecordService.class.getSimpleName();

    private static final int ONGOING_NOTIFICATION_ID = 42;

    private final IBinder binder = new RecordBinder();

    public class RecordBinder extends Binder {
        RecordService getService() {
            return RecordService.this;
        }
    }

    private RecordManager recordManager;

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        try {
            // FIXME will this be called once?
            this.recordManager = new RecordManager(getApplicationContext());
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("starting Service failed.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(this.recordManager != null) {
            this.recordManager.start();
            this.makeMeForeground();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.recordManager != null) {
            this.recordManager.stop();
        }
    }

    private void makeMeForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.recorder_notification_title))
                        .setContentText(getText(R.string.recorder_notification_msg))
                        .setSmallIcon(R.drawable.ic_info_black_24dp)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .build();

        Log.d(TAG, "startForeground!");
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    public Pair<FootSensorStatus, FootSensorStatus> getFootSensorStatus() {
        return this.recordManager.getFootSensorStatus();
    }
}
