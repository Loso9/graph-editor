package com.example.editorgrafov;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.Objects;


/**
 * Class representing edge in a graph
 */
public class Edge extends Line {

    private final Vertex from;
    private final Vertex to;
    private final Line line;

    /**
     * Variable holding color of edge
     */
    protected static Color color = Color.BLACK;

    /**
     * Variable holding width of edge
     */
    protected static int width = 1;

    /**
     * Constructor of Edge object
     * @param from vertex from which edge is created
     * @param to vertex to which edge is created
     */
    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
        this.line = new Line();
        line.setStroke(color);
        line.setFill(null);
        line.setStrokeWidth(width);
        line.startXProperty().bind(from.layoutXProperty().add(from.translateXProperty()).add(from.widthProperty().divide(2)));
        line.startYProperty().bind(from.layoutYProperty().add(from.translateYProperty()).add(from.heightProperty().divide(2)));
        line.endXProperty().bind(to.layoutXProperty().add(to.translateXProperty()).add(to.widthProperty().divide(2)));
        line.endYProperty().bind(to.layoutYProperty().add(to.translateYProperty()).add(to.heightProperty().divide(2)));
    }

    /**
     * Method for setting color of edge
     * @param color color of edge
     */
    public static void setColor(Color color) {
        Edge.color = color;
    }

    /**
     * Method for getting color of edge
     * @return color of edge
     */
    public Color getColor() {
        return color;
    }

    /**
     * Method for setting width of edge
     * @param width width of edge
     */
    public static void setWidth(int width) {
        Edge.width = width;
    }

    /**
     * Method for getting width of edge
     * @return width of edge
     */
    public int getWidth() {
        return width;
    }

    /**
     * Method for getting "from" vertex
     * @return vertex
     */
    public Vertex getFrom() {
        return from;
    }

    /**
     * Method for getting "to" vertex
     * @return vertex
     */
    public Vertex getTo() {
        return to;
    }

    /**
     * Method for getting Line object, internally representing edge
     * @return line object
     */
    public Line getEdge() { return line; }

    /**
     * Standard equals method for comparing edges
     * @param otherEdge edge to compare to
     * @return boolean value: true if edges are equal, false otherwise
     */
    @Override
    public boolean equals(Object otherEdge) {
        if (this == otherEdge) return true;
        if (otherEdge == null || getClass() != otherEdge.getClass()) return false;
        Edge edge = (Edge) otherEdge;
        return Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
    }

    /**
     * Standard method for hashing edge object
     * @return hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
