package utils;

import java.util.*;

/**
 * Wrapper around a list that records a history of add/remove operations. Used
 * for calculating where an element may be, if was at a certain position at a
 * certain epoch.
 *
 * @param <T> List element type
 */
public class HistoricalList<T> implements List<T> {
    // it appears i've done it again
    private List<T> listToWrap;
    private List<ListOp> opHistory;

    public HistoricalList(List<T> listToWrap) {
        this.listToWrap = listToWrap;
        this.opHistory = new LinkedList<>();
    }

    /**
     * Get the current "epoch"; each epoch represents 1 list add/remove
     * operation. For a fresh list, we start at epoch 0, and after doing 1 add
     * operation, we reach epoch 1.
     *
     * @return The current epoch
     */
    public int getCurrentEpoch() {
        return this.opHistory.size();
    }

    /**
     * Suppose we had an element at some position at some epoch in the past, see
     * where it would be now.
     *
     * @param prevPosition The position in the past
     * @param epoch The epoch in the past, from which we want to play into the future
     * @return The future position
     */
    public int forecastPosition(int prevPosition, int epoch) {
        int currPosition = prevPosition;
        ListIterator<ListOp> iterator = this.opHistory.listIterator(epoch);
        while (iterator.hasNext()) {
            ListOp op = iterator.next();
            if (op.add) {
                if (op.pos <= currPosition) {
                    currPosition++;
                }
            } else {
                if (op.pos < currPosition) {
                    currPosition--;
                }
            }
        }
        return currPosition;
    }

    /**
     * Clears the history after a certain epoch. Useful for resetting the
     * history after doing a bunch of "undo" list ops.
     *
     * @param epoch The epoch to reset to
     */
    public void resetHistoryToEpoch(int epoch) {
        while (this.opHistory.size() > epoch) {
            this.opHistory.remove(this.opHistory.size() - 1);
        }
    }

    @Override
    public int size() {
        return this.listToWrap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.listToWrap.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.listToWrap.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.listToWrap.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.listToWrap.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.listToWrap.toArray(a);
    }

    @Override
    public boolean add(T t) {
        int ind = this.listToWrap.size();
        this.opHistory.add(new ListOp(ind, true));
        return this.listToWrap.add(t);
    }

    @Override
    public boolean remove(Object o) {
        int ind = this.listToWrap.indexOf(o);
        if (ind >= 0) {
            this.listToWrap.remove(ind);
            this.opHistory.add(new ListOp(ind, false));
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.listToWrap.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int ind = this.listToWrap.size();
        for (int i = 0; i < c.size(); i++) {
            this.opHistory.add(new ListOp(ind, true));
            ind++;
        }
        return this.listToWrap.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        int ind = index;
        for (int i = 0; i < c.size(); i++) {
            this.opHistory.add(new ListOp(ind, true));
            ind++;
        }
        return this.listToWrap.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // bruh
        boolean anyRemoved = false;
        for (Object o : c) {
            if (this.remove(o)) {
                anyRemoved = true;
            }
        }
        return anyRemoved;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // do not call this
        ListIterator<T> iterator = this.listToWrap.listIterator();
        int currInd = 0;
        boolean anyRemoved = false;
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (!c.contains(item)) {
                iterator.remove();
                this.opHistory.add(new ListOp(currInd, false));
                anyRemoved = true;
            } else {
                currInd++;
            }
        }
        return anyRemoved;
    }

    @Override
    public void clear() {
        // bruh
        for (int i = 0; i < this.listToWrap.size(); i++) {
            this.opHistory.add(new ListOp(0, false));
        }
        this.listToWrap.clear();
    }

    @Override
    public T get(int index) {
        return this.listToWrap.get(index);
    }

    @Override
    public T set(int index, T element) {
        return this.listToWrap.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.listToWrap.add(index, element);
        this.opHistory.add(new ListOp(index, true));
    }

    @Override
    public T remove(int index) {
        this.opHistory.add(new ListOp(index, false));
        return this.listToWrap.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.listToWrap.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.listToWrap.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new HistoricalListIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new HistoricalListIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.listToWrap.subList(fromIndex, toIndex);
    }

    private static class ListOp {
        int pos;
        boolean add;

        ListOp(int pos, boolean add) {
            this.pos = pos;
            this.add = add;
        }
    }

    private class HistoricalListIterator implements ListIterator<T> {
        ListIterator<T> wrappedIterator;
        int currentIndex;
        boolean justRemoved;

        HistoricalListIterator(int index) {
            this.wrappedIterator = listToWrap.listIterator(index);
            this.currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return this.wrappedIterator.hasNext();
        }

        @Override
        public T next() {
            if (!this.justRemoved) {
                this.currentIndex++;
            }
            this.justRemoved = false;
            return this.wrappedIterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return this.wrappedIterator.hasPrevious();
        }

        @Override
        public T previous() {
            this.currentIndex--;
            return this.wrappedIterator.previous();
        }

        @Override
        public int nextIndex() {
            return this.wrappedIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return this.wrappedIterator.previousIndex();
        }

        @Override
        public void remove() {
            this.wrappedIterator.remove();
            opHistory.add(new ListOp(currentIndex, false));
            this.justRemoved = true;
        }

        @Override
        public void set(T t) {
            this.wrappedIterator.set(t);
        }

        @Override
        public void add(T t) {
            // ok buddy
            throw new UnsupportedOperationException();
        }
    }
}
