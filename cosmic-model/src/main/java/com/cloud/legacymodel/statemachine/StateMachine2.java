package com.cloud.legacymodel.statemachine;

import com.cloud.legacymodel.exceptions.NoTransitionException;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specifically, it implements the Moore machine.
 * so someone else can add/modify states easily without regression.
 * business logic anyways.
 *
 * @param <S> state
 * @param <E> event
 */
public class StateMachine2<S, E, V extends StateObject<S>> {
    private final HashMap<S, StateEntry> _states = new HashMap<S, StateEntry>();
    private final StateEntry _initialStateEntry;

    private final List<StateListener<S, E, V>> _listeners = new ArrayList<>();

    public StateMachine2() {
        _initialStateEntry = new StateEntry(null);
    }

    public void addTransition(final S currentState, final E event, final S toState) {
        addTransition(new Transition<>(currentState, event, toState, null));
    }

    public void addTransition(final Transition<S, E> transition) {
        final S currentState = transition.getCurrentState();
        final E event = transition.getEvent();
        final S toState = transition.getToState();
        StateEntry entry = null;
        if (currentState == null) {
            entry = _initialStateEntry;
        } else {
            entry = _states.get(currentState);
            if (entry == null) {
                entry = new StateEntry(currentState);
                _states.put(currentState, entry);
            }
        }

        entry.addTransition(event, toState, transition);

        entry = _states.get(toState);
        if (entry == null) {
            entry = new StateEntry(toState);
            _states.put(toState, entry);
        }
        entry.addFromTransition(event, currentState);
    }

    public Set<E> getPossibleEvents(final S s) {
        final StateEntry entry = _states.get(s);
        return entry.nextStates.keySet();
    }

    public List<S> getFromStates(final S s, final E e) {
        final StateEntry entry = _states.get(s);
        if (entry == null) {
            return new ArrayList<>();
        }

        return entry.prevStates.get(e);
    }

    public S getNextState(final S s, final E e) throws NoTransitionException {
        return getTransition(s, e).getToState();
    }

    public Transition<S, E> getTransition(final S s, final E e) throws NoTransitionException {
        StateEntry entry = null;
        if (s == null) {
            entry = _initialStateEntry;
        } else {
            entry = _states.get(s);
            assert entry != null : "Cannot retrieve transitions for state " + s;
        }

        final Transition<S, E> transition = entry.nextStates.get(e);
        if (transition == null) {
            throw new NoTransitionException("Unable to transition to a new state from " + s + " via " + e);
        }
        return transition;
    }

    public boolean registerListener(final StateListener<S, E, V> listener) {
        synchronized (_listeners) {
            return _listeners.add(listener);
        }
    }

    public List<StateListener<S, E, V>> getListeners() {
        synchronized (_listeners) {
            return _listeners;
        }
    }

    private class StateEntry {
        public S state;
        public HashMap<E, Transition<S, E>> nextStates;
        public HashMap<E, List<S>> prevStates;

        public StateEntry(final S state) {
            this.state = state;
            prevStates = new HashMap<>();
            nextStates = new HashMap<>();
        }

        public void addTransition(final E e, final S s, final Transition<S, E> transition) {
            assert !nextStates.containsKey(e) : "State " + getStateStr() + " already contains a transition to state " + nextStates.get(e).toString() + " via event " +
                    e.toString() + ".  Please revisit the rule you're adding to state " + s.toString();
            nextStates.put(e, transition);
        }

        protected String getStateStr() {
            return state == null ? "Initial" : state.toString();
        }

        public void addFromTransition(final E e, final S s) {
            List<S> l = prevStates.get(e);
            if (l == null) {
                l = new ArrayList<>();
                prevStates.put(e, l);
            }

            assert !l.contains(s) : "Already contains the from transition " + e.toString() + " from state " + s.toString() + " to " + getStateStr();
            l.add(s);
        }

        public void buildString(final StringBuilder str) {
            str.append("State: ").append(getStateStr()).append("\n");
            for (final Map.Entry<E, Transition<S, E>> nextState : nextStates.entrySet()) {
                str.append("  --> Event: ");
                final Formatter format = new Formatter();
                str.append(format.format("%-30s", nextState.getKey().toString()));
                str.append("----> State: ");
                str.append(nextState.getValue().toString());
                str.append("\n");
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder(1024);
        _initialStateEntry.buildString(str);
        for (final StateEntry entry : _states.values()) {
            entry.buildString(str);
        }
        return str.toString();
    }
}
