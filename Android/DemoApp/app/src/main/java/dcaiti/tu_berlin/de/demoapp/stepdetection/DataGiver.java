package dcaiti.tu_berlin.de.demoapp.stepdetection;

/**
 * Interface for a variety of the Producer-Consumer pattern. This file represents the producer.
 * Because in this variety the consumer does not need a link to the producer,
 * this is just a marker interface.
 * The corresponding consumer is the *{@link DataTaker}*.
 *
 * Created by Joris Clement on 22.01.18.
 */
interface DataGiver<D> {
}
