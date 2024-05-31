package com.example.editorgrafov;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Class representing vertex in a graph
 */
public class Vertex extends StackPane {

    private final Circle circle;
    private final Text text;

    /**
     * Variable holding color of circle representing vertex
     */
    protected static Color colorFill = Color.YELLOW;

    /**
     * Variable holding radius of circle representing vertex
     */
    protected static int radius = 30;

    /**
     * Variable holding font family of text inside of vertex
     */
    protected static String fontName = "System Regular";

    /**
     * Variable holding text size of text inside of vertex
     */
    protected static int textSize = 12;

    /**
     * Variable holding color of text inside of vertex
     */
    protected static Color textColor = Color.BLACK;

    /**
     * Constructor of Vertex object
     * @param text value stored inside of vertex
     * @param x coordinate, where vertex is placed
     * @param y coordinate, where vertex is placed
     */
    public Vertex(Text text, double x, double y) {
        this.text = text;
        text.setFont(Font.font(fontName, FontWeight.BOLD, textSize));
        double textWidth = text.getBoundsInLocal().getWidth();
        if (textWidth > radius * 2) {
            int biggerRadius = (int) textWidth / 2 + 15;
            this.circle = new Circle(x, y, biggerRadius);
        }
        else this.circle = new Circle(x, y, radius);
        //default
        circle.setFill(colorFill);
        circle.setStroke(Color.BLACK);
        text.setStyle("-fx-text-fill: " + textColor);
        getChildren().add(circle);
        getChildren().add(text);
        setTranslateX(x - radius);
        setTranslateY(y - radius);
    }

    /**
     * Constructor using atomicreferences of double for coordinates of vertex
     * @param text value stored inside of vertex
     * @param x coordinate, where vertex is placed
     * @param y coordinate, where vertex is placed
     */
    public Vertex(Text text, AtomicReference<Double> x, AtomicReference<Double> y) {
        this(text, x.get(), y.get());
    }

    /**
     * Method for setting color of vertex
     * @param colorFill new color of vertex
     */
    public static void setColor(Color colorFill) {
        Vertex.colorFill = colorFill;
    }

    /**
     * Method which returns color fill of Shape which represents vertex
     * @return color of object representing vertex
     */
    public Color getColorFill() {
        return colorFill;
    }

    /**
     * Method for setting font family of text inside of vertex
     * @param fontName font family
     */
    public static void setFontName(String fontName) {
        Vertex.fontName = fontName;
    }

    /**
     * Method for getting font family of text inside of vertex
     * @return font family
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Method for setting size of text inside of vertex
     * @param textSize text size
     */
    public static void setTextSize(int textSize) {
        Vertex.textSize = textSize;
    }

    /**
     * Method for getting size of text inside of vertex
     * @return size of text inside of vertex
     */
    public int getTextSize() {
        return textSize;
    }

    /**
     * Method for setting radius of vertex
     * @param radius radius
     */
    public static void setRadius(int radius) {
        Vertex.radius = radius;
    }

    /**
     * Method for getting radius of vertex
     * @return radius of circle representing vertex
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Method that returns x coordinate of vertex center
     * @return x coordinate of vertex
     */
    public double getX() {
        return circle.getCenterX();
    }

    /**
     * Method that returns y coordinate of vertex center
     * @return y coordinate of vertex
     */
    public double getY() {
        return circle.getCenterY();
    }

    /**
     * Method that sets x coordinate of vertex center
     * @param x coordinate of center
     */
    public void setX(double x) {
        circle.setCenterX(x);
    }

    /**
     * Method that sets y coordinate of vertex center
     * @param y coordinate of center
     */
    public void setY(double y) {
        circle.setCenterY(y);
    }

    /**
     * Method which returns value of vertex
     * @return Text value of vertex
     */
    public Text getText() {
        return text;
    }

    /**
     * Method which returns value of vertex
     * @return String value of vertex
     */
    public String getString() {
        return text.getText();
    }

    /**
     * Method which returns the Shape of vertex
     * @return Circle representation of vertex
     */
    public Circle getCircle() {
        return circle;
    }

    /*
     * possible customization options
     */

    /**
     * Method that sets color of value inside vertex
     * @param color color
     */
    public void setValueColor(Color color) {
        Vertex.textColor = color;
    }

    /**
     * Standard equals method for comparing vertices
     * @param otherVertex vertex to compare to
     * @return boolean value: true if vertices are equal, false otherwise
     */
    @Override
    public boolean equals(Object otherVertex) {
        if (this == otherVertex) return true;
        if (otherVertex == null || getClass() != otherVertex.getClass()) return false;
        Vertex vertex = (Vertex) otherVertex;
        return Objects.equals(circle, vertex.circle) && Objects.equals(text, vertex.text);
    }

    /**
     * Standard method for hashing vertex object
     * @return hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(circle, text);
    }
}
