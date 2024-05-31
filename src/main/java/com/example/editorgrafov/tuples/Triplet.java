package com.example.editorgrafov.tuples;

import java.util.Objects;

/**
 * Class representing triplet of objects, generic class
 * @param <P> type P
 * @param <Q> type Q
 * @param <R> type R
 */
public class Triplet<P, Q, R> {
    private P first;
    private Q second;
    private R third;

    /**
     * Constructor for Triplet object - triplet of three objects, one of type P, second of type Q, third of type R
     * @param first first object
     * @param second second object
     * @param third third object
     */
    public Triplet(P first, Q second, R third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Method for returning first object of the triplet
     * @return first object of type P
     */
    public P getFirst() {
        return first;
    }

    /**
     * Method for returning second object of the triplet
     * @return second object of type Q
     */
    public Q getSecond() {
        return second;
    }

    /**
     * Method for returning third object of the triplet
     * @return third object of type R
     */
    public R getThird() {
        return third;
    }

    /**
     * Method for setting first object of the triplet
     * @param first first object of type P
     */
    public void setFirst(P first) {
        this.first = first;
    }

    /**
     * Method for setting second object of the triplet
     * @param second second object of type Q
     */
    public void setSecond(Q second) {
        this.second = second;
    }

    /**
     * Method for setting third object of the triplet
     * @param third third object of type R
     */
    public void setThird(R third) {
        this.third = third;
    }

    /**
     * Standard equals method for comparing objects of type Triplet
     * @param otherTriplet triplet to compare to
     * @return boolean value: true if triplets are equal, false otherwise
     */
    @Override
    public boolean equals(Object otherTriplet) {
        if (this == otherTriplet) return true;
        if (otherTriplet == null || getClass() != otherTriplet.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) otherTriplet;
        return Objects.equals(first, triplet.first) && Objects.equals(second, triplet.second) && Objects.equals(third, triplet.third);
    }

    /**
     * Standard method for hashing Pair object
     * @return hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
