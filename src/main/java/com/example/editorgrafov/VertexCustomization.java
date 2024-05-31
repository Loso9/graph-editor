package com.example.editorgrafov;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Class representing window for vertex customization
 */
public class VertexCustomization extends Application {

    private static final Stage vertexStage = new Stage();

    private Color newColor = Vertex.colorFill;
    private String newFont = Vertex.fontName;
    private int newTextSize = Vertex.textSize;
    private int newRadiusSize = Vertex.radius;


    /**
     * Method for passing variables representing customization of vertex to main application class
     */
    public void passVertexChange() {
        Editor.receiveVertexChange(newColor, newFont, newTextSize, newRadiusSize);
    }


    /**
     * Method for opening vertex customization in separate window
     * @param vertexCustomizationStage stage
     * @throws Exception exception
     */
    @Override
    public void start(Stage vertexCustomizationStage) throws Exception {
        vertexCustomizationStage.setTitle("Vertex Customization");

        /* VBox holding all the components in the window */
        VBox vBoxComponents = new VBox();
        vBoxComponents.setSpacing(10);
        vBoxComponents.setPadding(new Insets(10, 10, 10, 10));

        /* Color of vertex */
        Label colorPickerLabel = new Label("Pick a new color of a vertex:");
        ColorPicker colorPicker = new ColorPicker(Vertex.colorFill);

        /* Font of text inside of vertex - font, size */
        Label fontLabel = new Label("Choose font of a text inside a vertex:");
        ObservableList<String> fontNames = FXCollections.observableList(Font.getFontNames());
        ComboBox<String> fontNameSelector = new ComboBox<>(fontNames);
        fontNameSelector.setValue(Vertex.fontName);

        Label fontSize = new Label("Choose font size of a text inside a vertex (currently chosen "+ Vertex.textSize +"):");
        Slider textSizeSlider = new Slider();
        textSizeSlider.setMin(12);
        textSizeSlider.setMax(50);
        //default value
        textSizeSlider.setValue(Vertex.textSize);

        textSizeSlider.setSnapToTicks(false);
        textSizeSlider.setShowTickLabels(true);
        textSizeSlider.setShowTickMarks(true);

        /* Radius of vertex */
        Label radiusLabel = new Label("Choose radius size of a vertex (currently chosen " + Vertex.radius + "):");
        Slider radiusSlider = new Slider();
        radiusSlider.setMin(20);
        radiusSlider.setMax(200);
        //default value
        radiusSlider.setValue(Vertex.radius);

        radiusSlider.setSnapToTicks(false);
        radiusSlider.setShowTickLabels(true);
        radiusSlider.setShowTickMarks(true);

        HBox HBoxForButtons = new HBox();
        HBoxForButtons.setSpacing(10);
        HBoxForButtons.setPadding(new Insets(10, 10, 10, 10));

        /* Save button */
        Button saveButton = new Button("Save");

        /* Cancel button */
        Button cancelButton = new Button("Cancel");

        HBoxForButtons.getChildren().addAll(saveButton, cancelButton);
        HBoxForButtons.setAlignment(Pos.CENTER_RIGHT);

        /*
         * saving values from components to corresponding variables
         */
        colorPicker.setOnAction(actionEvent -> {
            newColor = colorPicker.getValue();
        });

        fontNameSelector.setOnAction(actionEvent -> {
            newFont = fontNameSelector.getValue();
        });

        /*
         * adjusting value of text size in label realtime by adding listener to the value of the slider
         */
        textSizeSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            StringBuilder sbFont = new StringBuilder();
            newTextSize = newValue.intValue();
            sbFont.append("Choose font size of a text inside a vertex (currently chosen ")
                    .append(newTextSize)
                    .append("):");
            fontSize.setText(sbFont.toString());
        });

        /*
         * adjusting value of radius in label realtime by adding listener to the value of the slider
         */
        radiusSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            StringBuilder sbRadius = new StringBuilder();
            newRadiusSize = newValue.intValue();
            sbRadius.append("Choose radius size of a vertex (currently chosen ")
                    .append(newRadiusSize)
                    .append("):");
            radiusLabel.setText(sbRadius.toString());
        }));

        /*
         * saveButton functionality, sends values to main application class
         */
        saveButton.setOnAction(actionEvent -> {
            passVertexChange();
            vertexCustomizationStage.close();
        });

        /*
         * cancelButton functionality, closes window for vertex customization
         */
        cancelButton.setOnAction(actionEvent -> vertexCustomizationStage.close());


        /*
         * add components to VBox and set their positioning
         */
        vBoxComponents.getChildren().addAll(colorPickerLabel, colorPicker,
                fontLabel, fontNameSelector, fontSize, textSizeSlider,
                radiusLabel, radiusSlider,
                HBoxForButtons);

        vBoxComponents.setAlignment(Pos.CENTER);

        /*
         * set components ids for styling
         */
        colorPickerLabel.setId("modeSelected");
        fontLabel.setId("modeSelected");
        fontSize.setId("modeSelected");
        radiusLabel.setId("modeSelected");

        Scene scene = new Scene(vBoxComponents, 500, 400);
        scene.getStylesheets().add("styles.css");
        vBoxComponents.setStyle("-fx-background-color: #DADADA");
        vertexCustomizationStage.getIcons().add(new Image("https://static.vecteezy.com/system/resources/previews/005/597/848/non_2x/configuration-customize-technical-solution-service-gear-icon-free-vector.jpg"));
        vertexCustomizationStage.setResizable(false);
        vertexCustomizationStage.setScene(scene);
        vertexCustomizationStage.show();
    }

    /**
     * Constructor for class VertexCustomization
     * @throws Exception exception
     */
    public VertexCustomization() throws Exception {
        start(vertexStage);
    }

}
