package utils;

import java.util.*;
import java.util.function.Consumer;

// wrapper class to make sure cards always have the right cardpos/team field (to
// make the card references always accurate), and that the add() operation
// handles the out-of-bounds case (where -1 means adding to end of list), and
// the get operation returns null for out of index
// like a list but ensures each card remembers its index in this list
// actually i kinda regret writing this
// composition over inheritance amirite
public class PositionedList<T extends Indexable> implements List<T> {
    private final List<T> indexables;
    private final Consumer<T> setter;

    public PositionedList(List<T> listToWrap, Consumer<T> setter) {
        this.indexables = listToWrap;
        this.setter = setter;
        this.updatePositions(0);
        for (T t : listToWrap) {
            this.setter.accept(t);
        }
    }

    public PositionedList(List<T> listToWrap) {
        this(listToWrap, t -> {});
    }

    @Override
    public int size() {
        return this.indexables.size();
    }

    @Override
    public boolean isEmpty() {
        return this.indexables.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.indexables.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new PositionedListIterator(0);
    }

    @Override
    public Object[] toArray() {
        return this.indexables.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return this.indexables.toArray(a);
    }

    @Override
    public boolean add(T card) {
        boolean success = this.indexables.add(card);
        if (success) {
            this.updatePositions(this.indexables.size() - 1);
            this.setter.accept(card);
        }
        return success;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexables.indexOf(o);
        if (index >= 0) {
            this.indexables.remove(index);
            this.updatePositions(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.indexables.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int oldSize = this.indexables.size();
        boolean changed = this.indexables.addAll(c);
        if (changed) {
            this.updatePositions(oldSize - 1);
            for (T t : c) {
                this.setter.accept(t);
            }
        }
        return changed;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean changed = this.indexables.addAll(index, c);
        if (changed) {
            this.updatePositions(index);
            for (T t : c) {
                this.setter.accept(t);
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = this.indexables.removeAll(c);
        if (changed) {
            this.updatePositions(0);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = this.indexables.retainAll(c);
        if (changed) {
            this.updatePositions(0);
        }
        return changed;
    }

    @Override
    public void clear() {
        this.indexables.clear();
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= this.indexables.size()) {
            return null;
        }
        return this.indexables.get(index);
    }

    @Override
    public T set(int index, T element) {
        T ret = this.indexables.set(index, element);
        element.setIndex(index);
        this.setter.accept(element);
        return ret;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > this.indexables.size()) {
            index = this.indexables.size();
        }
        this.indexables.add(index, element);
        this.updatePositions(index);
        this.setter.accept(element);
    }

    @Override
    public T remove(int index) {
        T ret = this.indexables.remove(index);
        this.updatePositions(index);
        return ret;
    }

    @Override
    public int indexOf(Object o) {
        return this.indexables.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.indexables.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new PositionedListIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new PositionedListIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.indexables.subList(fromIndex, toIndex);
    }

    private void updatePositions(int startIndex) {
        ListIterator<T> iterator = this.indexables.listIterator(startIndex);
        int i = startIndex;
        while (iterator.hasNext()) {
            iterator.next().setIndex(i);
            i++;
        }
    }

    private class PositionedListIterator implements ListIterator<T> {
        ListIterator<T> wrappedIterator;
        T current;

        PositionedListIterator(int index) {
            this.wrappedIterator = indexables.listIterator(index);
        }

        @Override
        public boolean hasNext() {
            return this.wrappedIterator.hasNext();
        }

        @Override
        public T next() {
            this.current = this.wrappedIterator.next();
            return this.current;
        }

        @Override
        public boolean hasPrevious() {
            return this.wrappedIterator.hasPrevious();
        }

        @Override
        public T previous() {
            this.current = this.wrappedIterator.previous();
            return this.current;
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
            updatePositions(this.current.getIndex());
        }

        @Override
        public void set(T t) {
            this.wrappedIterator.set(t);
            t.setIndex(this.current.getIndex());
            setter.accept(t);
        }

        @Override
        public void add(T t) {
            this.wrappedIterator.add(t);
            updatePositions(this.current.getIndex());
            setter.accept(t);
        }
    }
}
