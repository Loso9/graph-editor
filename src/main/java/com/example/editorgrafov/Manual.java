package com.example.editorgrafov;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class representing manual window with instructions
 */
public class Manual extends Application {

    private boolean manualOpened;
    private String manualContent;
    private static final Stage manualStage = new Stage();

    /**
     * Method for loading Manual from file "man.txt"
     * @return String representation of manual
     * @throws FileNotFoundException iff file "man.txt" was not found in dir, where it is supposed to be placed (project_name/src)
     */
    public String loadManual() throws FileNotFoundException {
        if (manualOpened) return manualContent;
        String path = new File("man.txt").getAbsolutePath();
        File manual = new File(path);
        StringBuilder sb = new StringBuilder();
        Scanner manualReader = new Scanner(manual);
        while (manualReader.hasNextLine()) {
            sb.append(manualReader.nextLine());
            if (manualReader.hasNextLine()) {
                sb.append("\n");
            }
        }
        manualOpened = true;
        manualContent = sb.toString();
        return manualContent;
    }

    /**
     * Method for opening manual in separate window
     * @param manStage stage
     * @throws Exception exception
     */
    @Override
    public void start(Stage manStage) throws Exception {
        manStage.setTitle("Manual - Graph Editor");
        TextArea manualTextArea = new TextArea(loadManual());
        manualTextArea.setWrapText(true);

        TextFlow manPane = new TextFlow(manualTextArea);
        Scene manScene = new Scene(manPane, 800, 800);

        manualTextArea.setEditable(false);
        manStage.setScene(manScene);
        manualTextArea.prefWidthProperty().bind(Bindings.divide(manStage.getScene().widthProperty(), 1));
        manualTextArea.prefHeightProperty().bind(Bindings.divide(manStage.getScene().heightProperty(), 1));
        manScene.getStylesheets().add("styles.css");
        manualTextArea.setId("manualTextArea");
        manStage.show();
    }

    /**
     * Constructor for class Manual
     * @throws Exception exception
     */
    public Manual() throws Exception {
        start(manualStage);
    }

}
