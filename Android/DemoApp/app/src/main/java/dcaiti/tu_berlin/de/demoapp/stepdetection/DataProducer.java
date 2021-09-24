package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Interface for a variety of the Producer-Consumer pattern. This file represents the producer.
 * In this variety the consumer is intended to have multiple producers.
 * The corresponding consumer is the *{@link DataCollector}*.
 *
 * Created by Joris Clement on 17.01.18.
 */
interface DataProducer<D> {

    boolean hasData();

    D pop();
}
