package com.couchbase.mobileclient.triggers;

import com.couchbase.mobileclient.utils.Resetable;

public interface Trigger<T> extends Resetable {
    public boolean onNextElement(T element);
    public default boolean onNextElement() {
        return onNextElement(null);
    }

}
