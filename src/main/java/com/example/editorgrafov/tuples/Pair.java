package com.example.editorgrafov.tuples;

import java.util.Objects;

/**
 * Class representing pair of objects, generic class
 * @param <P> type P
 * @param <Q> type Q
 */
public class Pair<P, Q> {
    private P first;
    private Q second;

    /**
     * Constructor for Pair object - pair of two objects, one of type P, second of type Q
     * @param first first object
     * @param second second object
     */
    public Pair(P first, Q second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Method for returning first object of the pair
     * @return first object of type P
     */
    public P getFirst() {
        return first;
    }

    /**
     * Method for returning second object of the pair
     * @return second object of type Q
     */
    public Q getSecond() {
        return second;
    }

    /**
     * Method for setting first object of the pair
     * @param first first object of type P
     */
    public void setFirst(P first) {
        this.first = first;
    }

    /**
     * Method for setting second object of the pair
     * @param second second object of type Q
     */
    public void setSecond(Q second) {
        this.second = second;
    }

    /**
     * Standard equals method for comparing objects of type Pair
     * @param otherPair pair to compare to
     * @return boolean value: true if pairs are equal, false otherwise
     */
    @Override
    public boolean equals(Object otherPair) {
        if (this == otherPair) return true;
        if (otherPair == null || getClass() != otherPair.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) otherPair;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    /**
     * Standard method for hashing Pair object
     * @return hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
