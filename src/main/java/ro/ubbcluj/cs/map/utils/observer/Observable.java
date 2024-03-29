package ro.ubbcluj.cs.map.utils.observer;

import ro.ubbcluj.cs.map.utils.observer.events.Event;

public interface Observable<E extends Event> {
    void addObserver(Observer<E> o);
    void removeObserver(Observer<E> o);
    void notify(E t);
}
