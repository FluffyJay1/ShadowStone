package utils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * When the player plays cards, we have to preview their position on the board.
 * This has to work if the player has chained multiple play card actions, and it
 * also has to behave consistently, even if the board state gets completely
 * changed between each card being played.
 */
public class PendingListManager<T> {
    /*
    previewed playing cards take up imaginary space
    base pos: canonical position given to us, without pending garbage
    play pos: position after we insert the play card placeholders
    real pos: position after all the list ops have happened
    some operations "boot" other items, i.e. inserting an item at index 1
    "boots" the item already at that index to index 2
    some operations that don't "kick" other items, i.e. inserting an item at
    index 1 will actually insert it at 2
    order of booting: play preview -> list ops -> base pos
     */
    // spaghet
    private final List<ListOp<T>> localListOps;

    // when player queues up multiple play card actions, we have to preview them
    private final List<Pending<T>> pendingQueue;

    private Supplier<List<T>> consumerStateSupplier;
    private boolean dirtyPending;
    private List<T> cachedWithPending;

    public PendingListManager() {
        this.localListOps = new LinkedList<>();
        this.pendingQueue = new LinkedList<>();
        this.dirtyPending = true;
    }

    /**
     * Tell us what is the list that we want to superimpose on
     * @param consumerStateSupplier Supplier that gives us the original list of items
     */
    public void trackConsumerState(Supplier<List<T>> consumerStateSupplier) {
        this.consumerStateSupplier = consumerStateSupplier;
    }

    /**
     * Gets the list of the original items with the pending items superimposed
     * onto it. Don't worry about efficiency too much, this will cache the
     * results if nothing has changed (assuming that the original only changes
     * when we do consumeListOp).
     * @return A list with the pending items inserted at the appropriate locations.
     */
    public List<T> getConsumerStateWithPending() {
        if (this.consumerStateSupplier == null) {
            return null;
        }
        if (this.dirtyPending) {
            this.cachedWithPending = this.getConsumerStateWithPending(this.consumerStateSupplier.get());
            this.dirtyPending = false;
        }
        return this.cachedWithPending;
    }

    /**
     * Superimposes the list of pending playing items onto the intended target,
     * essentially previewing what it will look like.
     * @param original The original list of items, will not be modified
     * @return A new list with the pending playing items inserted at the appropriate locations.
     */
    private List<T> getConsumerStateWithPending(List<T> original) {
        List<T> retList = new LinkedList<>(original);
        for (Pending<? extends T> pp : this.pendingQueue) {
            retList.add(pp.pos, pp.item);
        }
        return retList;
    }

    /**
     * Given an item's original (base) pos in the original list, return where it
     * will end up after we show all the previewed items as well.
     * @param basePos The original (base) pos
     * @return The new pos in our preview list
     */
    public int baseToPending(int basePos) {
        for (Pending<T> p : this.pendingQueue) {
            if (p.pos <= basePos) {
                basePos++;
            }
        }
        return basePos;
    }

    public int pendingToBase(int pendingPos) {
        // collapse the Pending things
        ListIterator<Pending<T>> iter = this.pendingQueue.listIterator(this.pendingQueue.size());
        while (iter.hasPrevious()) {
            Pending<T> p = iter.previous();
            if (p.pos <= pendingPos) {
                pendingPos--;
            }
        }
        return pendingPos;
    }

    /**
     * Given an item in the original list (without pending stuff), forecast
     * where it will end up after all the added list ops.
     * @param basePos The original (base) pos of the item
     * @return The place it should finally reside
     */
    public int baseToReal(int basePos) {
        int realPos = basePos;
        for (ListOp<T> op : this.localListOps) {
            if (op.pos <= realPos) {
                if (op.add) {
                    realPos++;
                } else {
                    realPos--;
                }
            }
        }
        // TODO giga test this
        return realPos;
    }

    /**
     * If we try to add something in the future but preview it now, this finds
     * out where the item will reside in our current previewed list.
     * @param realPos The final (real) index of the item, after all the operations
     * @return The index into our previewed list where it should be inserted
     */
    public int realToPending(int realPos) {
        int pendingPos = realPos;
        ListIterator<ListOp<T>> iter = this.localListOps.listIterator(this.localListOps.size());
        List<MutatableWrapper<Integer>> pendingsActive = new LinkedList<>();
        while (iter.hasPrevious()) {
            ListOp<T> op = iter.previous();
            int opActualPos = baseToPending(op.pos, pendingsActive);
            if (opActualPos < pendingPos) {
                if (!op.add) {
                    pendingPos++;
                } else if (op.pp == null) {
                    pendingPos--;
                }
                // if it's being played, keep it
            }
            if (op.pp != null) {
                addPending(op.pos, 0, pendingsActive);
            }
        }
        return pendingPos;
    }

    /**
     * When trying to insert an index into our currently previewed list of
     * played items, find out where that index will end up after all the list
     * operations.
     * @param pendingPos The place where we are planning to insert
     * @return The final (real) index where it will end up
     */
    public int pendingToReal(int pendingPos) {
        int realPos = pendingPos;
        List<MutatableWrapper<Integer>> pendingsActive = this.pendingQueue.stream().map(p -> new MutatableWrapper<>(p.pos)).collect(Collectors.toCollection(LinkedList::new));
        for (ListOp<T> op : this.localListOps) {
            if (op.pp != null) {
                removePending(0, pendingsActive);
            }
            int opActualPos = baseToPending(op.pos, pendingsActive);
            if (op.add) {
                if (opActualPos <= realPos && op.pp == null) {
                    realPos++;
                }
            } else {
                if (opActualPos < realPos) {
                    realPos--;
                }
            }
        }
        assert pendingsActive.isEmpty();
        return realPos;
    }

    private int baseToPending(int basePos, List<MutatableWrapper<Integer>> pendingsActive) {
        for (MutatableWrapper<Integer> mwi : pendingsActive) {
            if (mwi.item <= basePos) {
                basePos++;
            }
        }
        return basePos;
    }

    private void addPending(int pos, int index, List<MutatableWrapper<Integer>> pendingsActive) {
        pendingsActive.add(index, new MutatableWrapper<>(pos));
        ListIterator<MutatableWrapper<Integer>> iter = pendingsActive.listIterator(index + 1);
        while (iter.hasNext()) {
            MutatableWrapper<Integer> mwi = iter.next();
            // consider booted
            if (mwi.item >= pos) {
                mwi.item++;
            }
        }
    }


    private void removePending(int index, List<MutatableWrapper<Integer>> pendingsActive) {
        int oldPos = pendingsActive.remove(index).item;
        ListIterator<MutatableWrapper<Integer>> iter = pendingsActive.listIterator(index);
        while (iter.hasNext()) {
            MutatableWrapper<Integer> mwi = iter.next();
            // if mwi.item == oldPos, that would boot this
            if (mwi.item > oldPos) {
                mwi.item--;
            }
        }
    }

    public Producer getProducer() {
        return new Producer();
    }

    public Consumer getConsumer() {
        return new Consumer();
    }

    // interface for processing a list op, whether the underlying object is a
    // producer or a consumer will affect the behavior
    public interface Processor<T> {
        /**
         * Generically process a list op
         * @param pos Position to insert/remove at
         * @param pendingItem If non null, item to add to pending list
         * @param add Whether to add or remove at that index
         */
        void processOp(int pos, T pendingItem, boolean add);
    }

    public class Producer implements Processor<T> {
        /**
         * Add an operation that indicates we will change the original list sometime
         * in the future, when we get around to processing it with consumeListOp.
         *
         * @param pos The index to change
         * @param add Whether an element was added/removed at that index
         */
        private void addListOp(int pos, boolean add) {
            localListOps.add(new ListOp<>(pos, add, null));
        }

        /**
         * Add an operation that will add to the original list sometime in the
         * future, except we want to preview this change right now.
         *
         * @param item    The item to add later
         * @param realPos The position to add it in (in the future)
         */
        private void addPending(T item, int realPos) {
            int playPos = realToPending(realPos);
            Pending<T> pp = new Pending<>(item, playPos);
            pendingQueue.add(pp);
            localListOps.add(new ListOp<>(realPos, true, pp));
            dirtyPending = true;
        }

        @Override
        public void processOp(int pos, T pendingItem, boolean add) {
            if (pendingItem != null && add) {
                this.addPending(pendingItem, pos);
            } else {
                this.addListOp(pos, add);
            }
        }
    }

    public class Consumer implements Processor<T> {
        /**
         * Indicate that the earliest list op has been processed as was prophesized
         * when we first called addListOp or addPending, so we update our play
         * positions accordingly. Must be called whenever the original list changes,
         * and it must change exactly according to the list op that was consumed.
         */
        public void consumeListOp() {
            ListOp<T> op = localListOps.remove(0);
            if (op.pp != null) {
                pendingQueue.remove(op.pp);
            }
            int actualOpPos = op.pos;
            for (Pending<T> pp : pendingQueue) {
                if (op.add) {
                    if (op.pp == null) {
                        if (actualOpPos <= pp.pos) {
                            pp.pos++;
                        } else {
                            actualOpPos++;
                        }
                    }
                } else {
                    if (actualOpPos < pp.pos) {
                        pp.pos--;
                    } else {
                        actualOpPos++;
                    }
                }
            }
            dirtyPending = true;
        }

        @Override
        public void processOp(int pos, T pendingItem, boolean add) {
            this.consumeListOp(); //kekl
        }
    }

    private static class Pending<T> {
        T item;
        // position to insert into at this point in time, taking into account the pendings before it
        int pos;
        public Pending(T item, int pos) {
            this.item = item;
            this.pos = pos;
        }
    }

    private static class ListOp<T> {
        // position to operate on at this point in time
        int pos;
        boolean add;
        Pending<T> pp;
        ListOp(int pos, boolean add, Pending<T> pp) {
            this.pos = pos;
            this.add = add;
            this.pp = pp;
        }
    }

    // biggest bruh
    private static class MutatableWrapper<T> {
        T item;
        public MutatableWrapper(T item) {
            this.item = item;
        }
    }
}
