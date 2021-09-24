package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Interface for a variety of the Producer-Consumer pattern. This file represents the consumer.
 * The corresponding producer is the *{@link DataGiver}*.
 *
 * Created by Joris Clement on 19.01.18.
 */
interface DataTaker<D> {

    void onNewData(D data);
}
