package com.example.editorgrafov;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Class representing window for edge customization
 */
public class EdgeCustomization extends Application {

    private static final Stage edgeStage = new Stage();

    private Color newColor = Edge.color;
    private int newWidth = Edge.width;

    /**
     * Method for passing
     */
    public void passEdgeChange() {
        Editor.receiveEdgeChange(newColor, newWidth);
    }

    /**
     * Method for opening edge customization in separate window
     * @param edgeCustomizationStage stage
     * @throws Exception exception
     */
    @Override
    public void start(Stage edgeCustomizationStage) throws Exception {
        edgeCustomizationStage.setTitle("Edge Customization");

        /* VBox holding all the components in the window */
        VBox vBoxComponents = new VBox();
        vBoxComponents.setSpacing(10);
        vBoxComponents.setPadding(new Insets(10, 10, 10, 10));

        /* Color of the edge */
        Label colorPickerLabel = new Label("Pick a new color of an edge:");
        ColorPicker colorPicker = new ColorPicker(Edge.color);

        /* Width of the edge */
        Label edgeWidthLabel = new Label("Choose new width of an edge (currently chosen " + Edge.width + "):");
        Slider edgeWidthSlider = new Slider();
        edgeWidthSlider.setMin(1);
        edgeWidthSlider.setMax(5);
        //default value
        edgeWidthSlider.setValue(Edge.width);

        edgeWidthSlider.setSnapToTicks(false);
        edgeWidthSlider.setShowTickLabels(true);
        edgeWidthSlider.setShowTickMarks(true);

        HBox HBoxForButtons = new HBox();
        HBoxForButtons.setSpacing(10);
        HBoxForButtons.setPadding(new Insets(10, 10, 10, 10));

        /* Save button */
        Button saveButton = new Button("Save");

        /* Cancel button */
        Button cancelButton = new Button("Cancel");

        HBoxForButtons.getChildren().addAll(saveButton, cancelButton);
        HBoxForButtons.setAlignment(Pos.CENTER_RIGHT);

        colorPicker.setOnAction(actionEvent -> {
            newColor = colorPicker.getValue();
        });

        edgeWidthSlider.valueProperty().addListener(((observableValue, oldValue, newValue) -> {
            StringBuilder sbWidth = new StringBuilder();
            newWidth = newValue.intValue();
            sbWidth.append("Choose new width of an edge (currently chosen ")
                    .append(newWidth)
                    .append("):");
            edgeWidthLabel.setText(sbWidth.toString());
        }));

        /*
         * save button functionality, send variables representing new edge format to main application class
         */
        saveButton.setOnAction(actionEvent -> {
            passEdgeChange();
            edgeCustomizationStage.close();
        });

        /*
         * cancel button functionality, closes window for edge customization
         */
        cancelButton.setOnAction(actionEvent -> edgeCustomizationStage.close());

        /*
         * add components into VBox and set their positioning
         */
        vBoxComponents.getChildren().addAll(colorPickerLabel, colorPicker,
                edgeWidthLabel, edgeWidthSlider,
                HBoxForButtons);
        vBoxComponents.setAlignment(Pos.CENTER);

        /*
         * set ids for components for css style applying
         */
        colorPickerLabel.setId("modeSelected");
        edgeWidthLabel.setId("modeSelected");

        Scene scene = new Scene(vBoxComponents, 500, 250);
        scene.getStylesheets().add("styles.css");
        vBoxComponents.setStyle("-fx-background-color: #DADADA");
        edgeCustomizationStage.getIcons().add(new Image("https://static.vecteezy.com/system/resources/previews/005/597/848/non_2x/configuration-customize-technical-solution-service-gear-icon-free-vector.jpg"));
        edgeCustomizationStage.setResizable(false);
        edgeCustomizationStage.setScene(scene);
        edgeCustomizationStage.show();
    }

    /**
     * Constructor for class EdgeCustomization
     * @throws Exception exception
     */
    public EdgeCustomization() throws Exception {
        start(edgeStage);
    }
}
