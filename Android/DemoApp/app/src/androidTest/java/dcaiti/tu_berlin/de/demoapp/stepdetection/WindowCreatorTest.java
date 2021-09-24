package dcaiti.tu_berlin.de.demoapp.stepdetection;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test WindowCreator Class
 *
 * Created by Joris Clement on 26.01.18.
 */
@RunWith(AndroidJUnit4.class)
public class WindowCreatorTest {

    private WindowCreator windowCreator;

    private int size;

    @Before
    public void init() {
        this.size = 10;
        Context appContext = InstrumentationRegistry.getTargetContext();
        // TODO replace this by a data taker mock
        try {
            StepDetectionManager stepDetectionManager = new StepDetectionManager(appContext);
            NNManager nnManager = new NNManager(stepDetectionManager, appContext, this.size);
            this.windowCreator = new WindowCreator(nnManager, this.size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanup() {
        this.windowCreator = null;
    }

    @Test
    public void basicTest() {
        // TODO implement after creation of DataTakerMock
    }
}