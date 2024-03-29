package ro.ubbcluj.cs.map.utils.observer;

import ro.ubbcluj.cs.map.utils.observer.events.Event;

public interface Observer<E extends Event> {
    void update(E o);
}
