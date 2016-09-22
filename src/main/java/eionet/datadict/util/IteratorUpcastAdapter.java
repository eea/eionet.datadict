package eionet.datadict.util;

import java.util.Iterator;

/**
 * An wrapper used for iterating the up-cast version of sequence items.
 * 
 * @author Nikolaos Nakas <nn@eworx.gr>
 * 
 * @param <S> The up-cast type of items to be iterated.
 * @param <T> The original type that requires up-casting.
 */
public class IteratorUpcastAdapter<S, T extends S> implements Iterator<S> {

    private final Iterator<T> iterator;
    
    public IteratorUpcastAdapter(Iterator<T> iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException();
        }
        
        this.iterator = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public S next() {
        return this.iterator.next();
    }

    @Override
    public void remove() {
        this.iterator.remove();
    }
    
}
