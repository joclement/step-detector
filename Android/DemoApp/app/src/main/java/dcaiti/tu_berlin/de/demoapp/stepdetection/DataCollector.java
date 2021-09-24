package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Interface for a variety of the Producer-Consumer pattern. This file represents the consumer.
 * In this variety the consumer is intended to have multiple producers.
 * The corresponding producer is the *{@link DataProducer}*.
 *
 * Created by Joris Clement on 17.01.18.
 */
interface DataCollector<D> {

    void onNewData();
}
