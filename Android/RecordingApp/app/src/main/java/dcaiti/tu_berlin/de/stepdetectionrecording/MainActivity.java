package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Main Activity. It requests all the needed permissions (Bluetooth)
 * and starts the settings on first use.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] DANGEROUS_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.addRecordButtonListener();
        Log.d(TAG, "finished onCreate");

        Utils.assertExternalStorageWritable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SettingsActivity.areAllSettingsValuesChanged(this)) {
            Utils.showToast(this, "You need to specify all the settings first.");
            Intent settingsIntent = new Intent(MainActivity.this,
                                               SettingsActivity.class);
            startActivity(settingsIntent);
        }
    }

    private void addRecordButtonListener() {
        final Button recordStart = (Button) findViewById(R.id.recordStart);
        recordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestNeededPermissions()) {
                    startRecording();
                }
            }
        });
    }

    private void startRecording() {
        Intent recordIntent = new Intent(MainActivity.this, RecordActivity.class);
        startActivity(recordIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettings =
                    new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(startSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean requestNeededPermissions() {
        for (String permission : DANGEROUS_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "request Permission");

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        PERMISSIONS_REQUEST_CODE);

                Log.d(TAG, "requested Permissions");

                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG,"onRequestPermissionResult");

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                int result = grantResults[i];
                if (result == PackageManager.PERMISSION_DENIED) {
                    Utils.showToast(this,
                            "This app needs permission " + permissions[i] + " for recording");
                }
            }
        } else {
            Log.w(TAG, "unequal request code for permissions was unexpected");
        }
    }
}
