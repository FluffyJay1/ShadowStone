package utils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * If you have two points on the same timeline where one point is ahead of the
 * other, this class tracks the diff between the two. The one that's ahead is
 * the "producer" of diffs and the one that's behind is the "consumer" of diffs.
 * Basically a queue except the decision to enqueue or dequeue is handled by
 * polymorphism :)
 * @param <T> Items in the timeline to track
 */
public class PendingManager<T> {
    Queue<T> queue;

    public PendingManager() {
        this.queue = new LinkedList<>();
    }

    // optional hooks to override
    public void onProduce(T item) {

    }

    public void onConsume(T item) {

    }

    public Iterable<T> getPending() {
        return this.queue;
    }

    public Producer getProducer() {
        return new Producer();
    }

    public Consumer getConsumer() {
        return new Consumer();
    }

    public interface Processor<T> {
        void process(T item);
    }

    public class Producer implements Processor<T> {
        @Override
        public void process(T item) {
            queue.add(item);
            onProduce(item);
        }
    }

    public class Consumer implements Processor<T> {
        @Override
        public void process(T item) {
            onConsume(queue.remove());
        }
    }
}
