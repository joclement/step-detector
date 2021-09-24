package dcaiti.tu_berlin.de.demoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Main Activity. It has a button to start the RecordActivity.
 *
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.addStartButtonListener();
    }

    private void addStartButtonListener() {
        final Button recordStart = (Button) findViewById(R.id.startDemo);
        recordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startDemo();
            }
        });
    }

    private void startDemo() {
        Intent demoIntent = new Intent(MainActivity.this, DemoActivity.class);
        startActivity(demoIntent);
    }
}
