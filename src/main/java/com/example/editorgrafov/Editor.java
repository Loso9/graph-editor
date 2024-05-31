package com.example.editorgrafov;

import com.example.editorgrafov.enums.Action;
import com.example.editorgrafov.enums.Mode;
import com.example.editorgrafov.tuples.Pair;
import com.example.editorgrafov.tuples.Triplet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main class of the project representing whole application
 */
public class Editor extends Application {

    private Stage stage;
    private Pane canvas;
    private File file;
    private boolean changedStatus;
    private MenuBar menuBar;
    private List<Pair<String, TextField>> listOfTextFields;
    private List<Triplet<String, Button, Mode>> listOfButtons;
    private List<Pair<RadioMenuItem, Mode>> listOfModes;
    private List<String> activeModes;
    private Map<String, Vertex> vertices;
    private List<Edge> edges;
    private Label lastAction;
    private List<Node> nodesAddedToCanvas;

    private static final int defaultWindowHeight = 720;
    private static final int defaultWindowWidth = 1280;
    private static final int VBoxWidth = 210;

    private static final int canvasHeight = defaultWindowHeight;

    private boolean insertModeSwitch = false;
    private boolean deleteModeSwitch = false;
    private boolean addEdgesModeSwitch = false;
    private boolean removeEdgesModeSwitch = false;

    /**
     * Updates current session status.
     * @param file file or null, if session is not currently saved in any file
     * @param change change in content of current session
     */
    public void updateStatus(File file, boolean change) {
        this.file = file;
        changedStatus = change;
        updateTitle();
    }

    /**
     * Updates title. <br>
     * - if not saved yet - Untitled <br>
     * - else file name <br>
     * - if user change content of current session, "*" is added in front of file name <br>
     * + (- Graph editor)
     */
    public void updateTitle() {
        StringBuilder title = new StringBuilder();
        if (file == null) {
            title.append("Untitled");
        }
        else {
            title.append(file.getName());
        }
        if (changedStatus) {
            title.append("*");
        }
        title.append(" - Graph Editor");
        stage.setTitle(title.toString());
    }

    /**
     * Method for displaying "Save before closing" alert in case the current session was not saved.
     * @return boolean value, whether alert was handled correctly or if it was needed at all
     */
    public boolean saveBeforeClosing() {
        if (changedStatus) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Graph Editor");
            alert.setHeaderText(null);
            StringBuilder alertString = new StringBuilder();
            if (file == null) alertString.append("Do you want to save before closing?");
            else {
                alertString = new StringBuilder("Do you want to save changes to ");
                String path;
                path = file.getAbsolutePath();
                alertString.append(path);
            }
            alert.setContentText(alertString.toString());

            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(save, dontSave, cancel);
            Optional<ButtonType> resultAction = alert.showAndWait();
            if (resultAction.isPresent()) {
                if (resultAction.get() == save) {
                    return saveAction();
                }
                return resultAction.get() == dontSave;
            }
            return false;
        }
        return true;
    }

    /**
     * Invokes window for choosing file with specific filters.
     * @return window for choosing file
     */
    public FileChooser fileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        //possible to add more extensions
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Documents (*.txt)", "*.txt"));
        return fileChooser;
    }

    /**
     * Opens fileChooser and lets user choose file to open
     * @return file chosen by user to open
     */
    public File chooseFileToOpen() {
        FileChooser fileChooser = fileChooser();
        fileChooser.setTitle("Open");
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Opens fileChooser and lets user choose file to save current session.
     * @return file chosen by user to save current session
     */
    public File chooseFileToSave() {
        FileChooser fileChooser = fileChooser();
        fileChooser.setTitle("Save");
        return fileChooser.showSaveDialog(stage);
    }

    /**
     * Method that handles close request
     * @param event event
     */
    public void closeWindowRequest(WindowEvent event) {
        if (saveBeforeClosing()) {
            return;
        }
        event.consume();
    }

    /**
     * Method for handling process after clicking "New" option in File tab in menu. <br>
     * - if current session has not been saved yet, saveBeforeClosing() method gets called
     */
    public void newAction() {
        if (saveBeforeClosing()) {
            updateStatus(null, true);
            clearTextFields();
            clearNodes();
            lastAction.setText("Last action: None");
            turnOnModesOnStartup();
        }
    }

    /**
     * Method for handling process after clicking "Open" option in File tab in menu. <br>
     * - if current session has not been saved yet, saveBeforeClosing() method gets called
     * @return boolean whether Open action was handled correctly (false = either some error occurred or file to Open does not exit)
     */
    public boolean openAction() {
        if (saveBeforeClosing()) {
            File fileToOpen = chooseFileToOpen();
            if (fileToOpen == null) {
                return false;
            }
            else {
                try {
                    //read and display graph
                    updateStatus(fileToOpen, false);
                    clearTextFields();
                    clearNodes();
                    lastAction.setText("Last action: None");
                    readFromFile(fileToOpen);
                    turnOnModesOnStartup();
                }
                catch (Exception e) {
                    errorAction(Action.OPEN);
                }
                return true;
            }
        }
        else return false;
    }

    /**
     * Method for handling process after clicking "Save" option in File tab in menu. <br>
     * - if file has not been saved yet, saveAsAction() gets called instead <br>
     * - else content of current session gets written into file
     * @return boolean value whether Save action was handled correctly (false = iff saveAsAction() returns false)
     */
    public boolean saveAction() {
        if (file == null) {
            return saveAsAction();
        }
        else {
            try {
                updateStatus(file, false);
                writeToFile(file);
                clearTextFields();
            }
            catch (Exception e) {
                errorAction(Action.SAVE);
            }
            return true;
        }

    }

    /**
     * Method for handling process after clicking "Save As" option in File tab in menu. <br>
     * - firstly, fileChooser window opens for user to choose file to save current session into <br>
     * - current session is saved into chosen file afterwards
     * @return boolean value whether Save As action was handled correctly (false = either file to save does not exit or error occurred)
     */
    public boolean saveAsAction() {
        File fileToSave = chooseFileToSave();
        if (fileToSave == null)
            return false;
        else {
            try {
                updateStatus(fileToSave, false);
                writeToFile(fileToSave);
                clearTextFields();
            }
            catch (Exception e) {
                errorAction(Action.SAVE);
            }
            return true;
        }
    }

    /**
     * Method for handling "Exit" option in File tab in menu.
     * - window closes iff file current session is saved (or if there was no change)
     */
    public void exitAction() {
        if (saveBeforeClosing()) {
            Platform.exit();
        }
    }

    /**
     * Method for handling errors that occur during Actions from File tab in menu.
     * @param action action from File tab during which the error occurred
     */
    private void errorAction(Action action) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        try {
            alert.setTitle("Error");
            String errorName = action.name().toLowerCase();
            alert.setContentText("There was an error during " + errorName + " action!");
        }
        catch (Exception e) {
            alert.setContentText("There was an error during error action!");
        }
        finally {
            //ButtonType OK = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
            //alert.getButtonTypes().add(OK);

            Optional<ButtonType> actionResult = alert.showAndWait();
            if (actionResult.isPresent()) {
                alert.close();
            }
        }

    }

    /**
     * Method for clearing textfields.
     */
    public void clearTextFields() {
        for (Pair<String, TextField> textField : listOfTextFields) {
            textField.getSecond().clear();
        }
    }

    /**
     * Method that resets canvas and deletes nodes internally.
     */
    public void clearNodes() {
        for (String value : vertices.keySet()) {
            Vertex vertexToRemove = vertices.get(value);
            canvas.getChildren().remove(vertexToRemove);
        }
        vertices.clear();
        for (Edge edge : edges) {
            canvas.getChildren().remove(edge.getEdge());
        }
        edges.clear();
        nodesAddedToCanvas.clear();
    }

    /**
     * Method for updating status of session if canvas change occurred.
     */
    public void handleCanvasChange() {
        if (!changedStatus) {
            updateStatus(file, true);
        }
    }

    /**
     * Method for making vertex draggable in canvas, bounds
     * for vertex not to go outside of canvas included and
     * tracking last action made in canvas.
     * @param vertex vertex which is set to be draggable
     */
    public void makeVertexDraggable(Vertex vertex) {
        AtomicReference<Double> dragX = new AtomicReference<>((double) 0);
        AtomicReference<Double> dragY = new AtomicReference<>((double) 0);
        vertex.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                vertex.getScene().setCursor(Cursor.HAND);
                lastAction.setText("Last action: Mouse cursor is currently over a vertex with a value of \"" + vertex.getString() + "\".");
            }
        });

        vertex.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                vertex.getScene().setCursor(Cursor.DEFAULT);
                lastAction.setText("Last action: Mouse cursor exited a vertex with a value of \"" + vertex.getString() + "\".");
            }
        });

        vertex.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isPrimaryButtonDown()) {
                vertex.getScene().setCursor(Cursor.DEFAULT);
            }
            dragX.set(mouseEvent.getSceneX() - vertex.getTranslateX());
            dragY.set(mouseEvent.getSceneY() - vertex.getTranslateY());
            vertex.getScene().setCursor(Cursor.MOVE);
            lastAction.setText("Last action: Currently dragging a vertex with a value of \"" + vertex.getString() + "\".");
        });

        vertex.setOnMouseReleased(mouseEvent -> {
            vertex.getScene().setCursor(Cursor.HAND);
            lastAction.setText("Last action: Dropped a vertex with a value of \"" + vertex.getString() + "\" at: [" + (int) vertex.getX() + "," + (int) vertex.getY() + "].");
        });

        vertex.setOnMouseDragged(mouseEvent -> {
            vertex.setTranslateX(mouseEvent.getSceneX() - dragX.get());
            vertex.setTranslateY(mouseEvent.getSceneY() - dragY.get());
            Bounds vertexBounds = vertex.getBoundsInParent();
            double vertexWidth = vertexBounds.getWidth();
            double vertexHeight = vertexBounds.getHeight();

            double currentX = vertexBounds.getCenterX();
            double currentY = vertexBounds.getCenterY();

            if (vertex.getTranslateX() < 0) {
                currentX = 0;
                vertex.setTranslateX(currentX);
            } else if (vertex.getTranslateX() + vertexWidth > canvas.getWidth()) {
                currentX = canvas.getWidth() - vertexWidth;
                vertex.setTranslateX(currentX);
            }

            if (vertex.getTranslateY() < 0) {
                currentY = 0;
                vertex.setTranslateY(currentY);
            } else if (vertex.getTranslateY() + vertexHeight > canvas.getHeight()) {
                currentY = canvas.getHeight() - vertexHeight;
                vertex.setTranslateY(currentY);
            }

            vertex.setX(currentX);
            vertex.setY(currentY);

            // checkVertexCollision(vertex);
        });
    }

    /**
     * Method for checking if there exists an edge containing vertex in graph.
     * @param edges list of edges in the graph
     * @param vertex vertex that is made to be looked for in list of edges
     * @return list of edges containing said vertex (empty list if there is none)
     */
    public List<Edge> edgesContainingVertex(List<Edge> edges, Vertex vertex) {
        List<Edge> returnEdges = new ArrayList<>();
        for (Edge edge : edges) {
            if (edge.getFrom().equals(vertex) || edge.getTo().equals(vertex)) {
                returnEdges.add(edge);
            }
        }
        return returnEdges;
    }


    /**
     * Method for checking if there exists edge [vertexFrom, vertexTo] in graph.
     * @param edges list of edges in the graph
     * @param vertexFrom start vertex of an edge
     * @param vertexTo end vertex of an edge
     * @return edge, if there exists one (null, if there is not)
     */
    public Edge existsEdge(List<Edge> edges, Vertex vertexFrom, Vertex vertexTo) {
        for (Edge edge : edges) {
            if ((edge.getFrom().equals(vertexFrom) && edge.getTo().equals(vertexTo))
                    || (edge.getFrom().equals(vertexTo) && edge.getTo().equals(vertexFrom))) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Method for reading graph from .txt file and putting it onto canvas. <br>
     * - the way graph is represented in .txt file is written in manual
     * @param file file represennting graph in .txt format
     * @throws IOException exception
     */
    public void readFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String[] nums = reader.readLine().split(" ");

        int numOfVertices = Integer.parseInt(nums[0]);
        int numOfEdges = Integer.parseInt(nums[1]);
        for (int i = 0; i < numOfVertices; i++) {
            String[] vertexRepr = reader.readLine().split(" ");
            String value = vertexRepr[0];
            Text textValue = new Text(value);
            double x = Double.parseDouble(vertexRepr[1]);
            double y = Double.parseDouble(vertexRepr[2]);
            Vertex newVertex = new Vertex(textValue, x, y);
            makeVertexDraggable(newVertex);
            vertices.put(value, newVertex);
            canvas.getChildren().add(newVertex);
        }

        for (int i = 0; i < numOfEdges; i++) {
            String[] edgeRepr = reader.readLine().split(" ");
            String valueVertexFrom = edgeRepr[0];
            String valueVertexTo = edgeRepr[1];
            Vertex vertexFrom = vertices.get(valueVertexFrom);
            Vertex vertexTo = vertices.get(valueVertexTo);
            Edge newEdge = new Edge(vertexFrom, vertexTo);
            edges.add(newEdge);
            canvas.getChildren().add(newEdge.getEdge());
            canvas.getChildren().remove(vertexFrom);
            canvas.getChildren().remove(vertexTo);
            canvas.getChildren().addAll(vertexFrom, vertexTo);
        }

        reader.close();
    }

    /**
     * Method for writing graph into .txt file. <br>
     * - the way graph is represented in .txt file is written in manual
     * @param file file in which graph will be saved
     * @throws IOException exception
     */
    public void writeToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        StringBuilder sb = new StringBuilder();
        int numOfVertices = vertices.size();
        int numOfEdges = edges.size();
        sb.append(numOfVertices)
                .append(" ")
                .append(numOfEdges)
                .append("\n");
        writer.write(sb.toString());

        for (String value : vertices.keySet()) {
            //clear stringBuilder object
            sb.setLength(0);
            Vertex vertex = vertices.get(value);
            double x = vertex.getX();
            double y = vertex.getY();
            sb.append(value)
                    .append(" ")
                    .append(x)
                    .append(" ")
                    .append(y)
                    .append("\n");
            writer.write(sb.toString());
        }

        for (Edge edge : edges) {
            //clear stringBuilder object
            sb.setLength(0);
            String valueVertexFrom = edge.getFrom().getString();
            String valueVertexTo = edge.getTo().getString();
            sb.append(valueVertexFrom)
                    .append(" ")
                    .append(valueVertexTo)
                    .append("\n");
            writer.write(sb.toString());
        }

        writer.close();
    }

    /**
     * Method for reading graph from .txt file and putting it onto canvas. <br>
     * - the way graph is represented in .txt file is written in manual <br>
     * - graph has all the modifications user set during the session graph was made
     * @param file file represennting graph in .txt format
     * @throws IOException exception
     */
    public void readFromFileReal(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int numOfNodes = Integer.parseInt(reader.readLine());
        for (int i = 0; i < numOfNodes; i++) {
            String[] nodeInfo = reader.readLine().split(" ");
            if (nodeInfo[0].equals("V")) {
                String vertexValue = nodeInfo[1];
                Text vertexTextValue = new Text(vertexValue);
                double x = Double.parseDouble(nodeInfo[2]);
                double y = Double.parseDouble(nodeInfo[3]);
                Color vertexColor = Color.web(nodeInfo[4]);
                int textSize = Integer.parseInt(nodeInfo[5]);
                int vertexRadius = Integer.parseInt(nodeInfo[6]);
                String fontName = nodeInfo[7];
                Vertex.setColor(vertexColor);
                Vertex.setRadius(vertexRadius);
                Vertex.setTextSize(textSize);
                Vertex.setFontName(fontName);
                Vertex newVertex = new Vertex(vertexTextValue, x, y);
                makeVertexDraggable(newVertex);
                vertices.put(vertexValue, newVertex);
                canvas.getChildren().add(newVertex);
            }
            if (nodeInfo[0].equals("E")) {
                String valueVertexFromString = nodeInfo[1];
                String valueVertexToString = nodeInfo[2];
                Color edgeColor = Color.web(nodeInfo[3]);
                int edgeWidth = Integer.parseInt(nodeInfo[4]);
                Vertex vertexFrom = vertices.get(valueVertexFromString);
                Vertex vertexTo = vertices.get(valueVertexToString);
                Edge.setColor(edgeColor);
                Edge.setWidth(edgeWidth);
                Edge newEdge = new Edge(vertexFrom, vertexTo);
                edges.add(newEdge);
                canvas.getChildren().add(newEdge.getEdge());
                canvas.getChildren().remove(vertexFrom);
                canvas.getChildren().remove(vertexTo);
                canvas.getChildren().addAll(vertexFrom, vertexTo);
            }
        }
        reader.close();
    }


    /**
     * Method for writing graph into .txt file. <br>
     * - the way graph is represented in .txt file is written in manual <br>
     * - graph has all the modifications user set during the session graph was made
     * @param file file in which graph will be saved
     * @throws IOException exception
     */
    public void writeToFileReal(File file) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(canvas.getChildren().size() - 2 + "\n");
        StringBuilder sb = new StringBuilder();
        for (Node node : nodesAddedToCanvas) {
            //clear stringBuilder object
            sb.setLength(0);
            if (node instanceof Vertex) {
                Vertex vertex = (Vertex) node;
                String vertexValue = vertex.getString();
                Color vertexColor = vertex.getColorFill();
                int textSize = vertex.getTextSize();
                int vertexRadius = vertex.getRadius();
                String fontName = vertex.getFontName();
                double x = vertex.getX();
                double y = vertex.getY();
                sb.append("V ")
                        .append(vertexValue)
                        .append(" ")
                        .append(x)
                        .append(" ")
                        .append(y)
                        .append(" ")
                        .append(vertexColor.toString())
                        .append(" ")
                        .append(vertexRadius)
                        .append(" ")
                        .append(textSize)
                        .append(" ")
                        .append(fontName)
                        .append("\n");
            }
            else if (node instanceof Line) {
                Edge edge = findEdgeToLine((Line) node);
                String vertexFrom = edge.getFrom().getString();
                String vertexTo = edge.getTo().getString();
                Color edgeColor = edge.getColor();
                int edgeWidth = edge.getWidth();
                sb.append("E ")
                        .append(vertexFrom)
                        .append(" ")
                        .append(vertexTo)
                        .append(" ")
                        .append(edgeColor.toString())
                        .append(" ")
                        .append(edgeWidth)
                        .append("\n");
            }
            writer.write(sb.toString());
        }
        writer.close();
    }

    /**
     * Method for using to test, whether there exists an edge to the corresponding line (doesnt work properly!)
     * @param line line
     * @return edge corresponding to the line
     */
    public Edge findEdgeToLine(Line line) {
        for (Edge edge : edges) {
            if (edge.getEdge().equals(line)) return edge;
        }
        return null;
    }

    /**
     * Generic method used for mode switching (on or off) in app.
     * @param modeSwitch boolean representing whether mode is currently off (false) or on (true) - gets changed to opposite value
     * @param button button tied to mode that needs to be either turned on or off based of modeSwitch boolean
     * @param textFields list of textfields tied to mode that need to be either turned on or off based of modeSwitch boolean
     * @return boolean value representing new state of mode (if it was on = true, method returns false = off)
     */
    private boolean toggleMode(boolean modeSwitch, Button button, TextField... textFields) {
        boolean newState = !modeSwitch;
        button.setDisable(!newState);
        for (TextField textField : textFields) {
            textField.setDisable(!newState);
        }
        return newState;
    }

    /**
     * Method for switching insert mode on or off based on previous state (opposite).
     * @param addVertexButton button tied to insert mode that is being turned on or off
     * @param addVertexTextField button tied to insert mode that is being turned on or off
     */
    public void insertMode(Button addVertexButton, TextField addVertexTextField) {
        insertModeSwitch = toggleMode(insertModeSwitch, addVertexButton, addVertexTextField);
        if (insertModeSwitch) activeModes.add("INSERT VERTEX MODE");
        else activeModes.remove("INSERT VERTEX MODE");
    }

    /**
     * Method for switching delete mode on or off based on previous state (opposite).
     * @param deleteVertexButton button tied to delete mode that is being turned on or off
     * @param deleteVertexTextField button tied to delete mode that is being turned on or off
     */
    public void deleteMode(Button deleteVertexButton, TextField deleteVertexTextField) {
        deleteModeSwitch = toggleMode(deleteModeSwitch, deleteVertexButton, deleteVertexTextField);
        if (deleteModeSwitch) activeModes.add("DELETE VERTEX MODE");
        else activeModes.remove("DELETE VERTEX MODE");
    }

    /**
     * Method for switching add edge mode on or off based on previous state (opposite).
     * @param addEdgeButton button tied to add edge mode that is being turned on or off
     * @param vertexFrom textfield that is tied to add edge mode that is being turned on or off
     * @param vertexTo textfield that is tied to add edge mode that is being turned on or off
     */
    public void addEdgesMode(Button addEdgeButton, TextField vertexFrom, TextField vertexTo) {
        addEdgesModeSwitch = toggleMode(addEdgesModeSwitch, addEdgeButton, vertexFrom, vertexTo);
        if (addEdgesModeSwitch) activeModes.add("ADD EDGE MODE");
        else activeModes.remove("ADD EDGE MODE");
    }

    /**
     * Method for switching add edge mode on or off based on previous state (opposite).
     * @param removeEdgeButton tied to remove edge mode that is being turned on or off
     * @param vertexFrom textfield that is tied to remove edge mode that is being turned on or off
     * @param vertexTo textfield that is tied to remove edge mode that is being turned on or off
     */
    public void removeEdgesMode(Button removeEdgeButton, TextField vertexFrom, TextField vertexTo) {
        removeEdgesModeSwitch = toggleMode(removeEdgesModeSwitch, removeEdgeButton, vertexFrom, vertexTo);
        if (removeEdgesModeSwitch) activeModes.add("REMOVE EDGE MODE");
        else activeModes.remove("REMOVE EDGE MODE");
    }

    /**
     * Method for turning on all modes at the start of application. <br>
     * -- enables buttons, textfields, selects all items in Mode Menu
     */
    public void turnOnModesOnStartup() {
        for (Triplet<String, Button, Mode> buttonTriplet : listOfButtons) {
            Button button = buttonTriplet.getSecond();
            button.setDisable(false);
        }

        for (Pair<String, TextField> textFieldPair : listOfTextFields) {
            TextField textField = textFieldPair.getSecond();
            textField.setDisable(false);
        }

        for (Pair<RadioMenuItem, Mode> radioMenuItemModePair : listOfModes) {
            RadioMenuItem radioMenuItem = radioMenuItemModePair.getFirst();
            radioMenuItem.setSelected(true);
        }

        insertModeSwitch = true;
        activeModes.add("INSERT VERTEX MODE");
        deleteModeSwitch = true;
        activeModes.add("DELETE VERTEX MODE");
        addEdgesModeSwitch = true;
        activeModes.add("ADD EDGE MODE");
        removeEdgesModeSwitch = true;
        activeModes.add("REMOVE EDGE MODE");
    }

    /**
     * Method for updating label modeSelected based off items selected in menu Mode.
     * @param modeSelected label on top of canvas holding information about enabled modes
     */
    public void updateModeSelected(Label modeSelected) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mode(s) selected: ");
        int numOfModesSelected = activeModes.size();
        int counter = 0;
        for (String mode : activeModes) {
            counter++;
            sb.append(mode);
            if (counter == numOfModesSelected) {
                sb.append(".");
            }
            else sb.append(", ");
        }
        modeSelected.setText(sb.toString());
    }

    //Methods for detecting collisions and preparations for their resolution (could be used in the future update(s))

    /*
    public void checkVertexCollision(Vertex vertex) {
        for (Node node : canvas.getChildren()) {
            if (node instanceof Vertex && !node.equals(vertex)) {
                Vertex anotherVertex = (Vertex) node;
                if (vertex.getBoundsInParent().intersects(anotherVertex.getBoundsInParent()) && circleCollisions(vertex, anotherVertex)) {
                    System.out.println("Collision");
                    //System.out.println("First vertex: [" + vertex.getX() + ", " + vertex.getY() + "]");
                    //System.out.println("Second vertex: [" + anotherVertex.getX() + ", " + anotherVertex.getY() + "]");
                    resolveVertexCollision(vertex, anotherVertex);
                }
            }
        }
    }

    public boolean circleCollisions(Vertex vertex, Vertex anotherVertex) {
        Circle vertexCircle = vertex.getCircle();
        Circle anotherVertexCircle = anotherVertex.getCircle();

        double vertexCenterX = vertex.getLayoutX() + vertexCircle.getCenterX();
        double vertexCenterY = vertex.getLayoutY() + vertexCircle.getCenterY();
        double anotherVertexCenterX = anotherVertex.getLayoutX() + anotherVertexCircle.getCenterX();
        double anotherVertexCenterY = anotherVertex.getLayoutY() + anotherVertexCircle.getCenterY();

        double diffX = vertexCenterX - anotherVertexCenterX;
        double diffY = vertexCenterY - anotherVertexCenterY;

        double getDistanceFromCenters = Math.sqrt(diffX * diffX + diffY * diffY);
        double minDistance = vertexCircle.getRadius() + anotherVertex.getRadius();

        return minDistance > getDistanceFromCenters;
    }

     */

    /**
     * Method for receiving variables representing customization of vertex and adjusting said values (not implemented yet).
     * @param newVertexColor new vertex color
     * @param newVertexFontName new font of the text inside of a vertex
     * @param newTextSize new size of the text inside of a vertex
     * @param newRadiusSize new radius size of a vertex
     */
    public static void receiveVertexChange(Color newVertexColor, String newVertexFontName, int newTextSize, int newRadiusSize) {
        Vertex.setColor(newVertexColor);
        Vertex.setFontName(newVertexFontName);
        Vertex.setTextSize(newTextSize);
        Vertex.setRadius(newRadiusSize);
    }

    /**
     * Method for receiving variables representing customization of edge and adjusting said values (not implemented yet).
     * @param newEdgeColor new edge color
     * @param newEdgeWidth new edge width
     */
    public static void receiveEdgeChange(Color newEdgeColor, int newEdgeWidth) {
       Edge.setColor(newEdgeColor);
       Edge.setWidth(newEdgeWidth);
    }


    /**
     * Method for opening application Editor
     * @param stage stage
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        updateStatus(null, true);
        //root node
        BorderPane pane = new BorderPane();

        Scene scene = new Scene(pane, defaultWindowWidth, defaultWindowHeight);

        /* Canvas - graph display
         * - pref size: scene size - Vbox size
         * - simple bordering around the edges of canvas
         * - white background
         * - canvas size changes according to window size changes, and so is border
         * - added listener for changes in canvas (list of nodes)
         */
        this.canvas = new Pane();
        canvas.setPrefWidth(scene.getWidth() - VBoxWidth);
        Border border = new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
        canvas.setBorder(border);
        canvas.setStyle("-fx-background-color: white");
        pane.setStyle("-fx-background-color: #DADADA");

        /* Vbox
         * - default size: width = 200
         * - spacing between elements set to 10
         * - 10pt margins around the vbox, aligned to top and center
         * - addVertexButton, deleteVertexButton, addEdgeButton, removeEdgeButton
         * - textField for addVertexButton, deleteVertexButton, addEdgeButton, removeEdgeButton
         * - randomGraphButton, clearGraphButton
         */
        VBox vBox = new VBox();

        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        Button addVertex = new Button("Add Vertex");
        TextField textFieldAddVertex = new TextField();
        textFieldAddVertex.setPromptText("Enter a vertex value to insert");

        Button deleteVertex = new Button("Delete Vertex");
        TextField textFieldDeleteVertex = new TextField();
        textFieldDeleteVertex.setPromptText("Enter a vertex value to delete");

        Button addEdge = new Button("Add Edge");
        TextField textFieldAddEdgeFrom = new TextField();
        textFieldAddEdgeFrom.setPromptText("Add an edge from a vertex");
        TextField textFieldAddEdgeTo = new TextField();
        textFieldAddEdgeTo.setPromptText("Add an edge to a vertex");

        Button removeEdge = new Button("Remove Edge");
        TextField textFieldRemoveEdgeFrom = new TextField();
        textFieldRemoveEdgeFrom.setPromptText("Remove an edge from vertex");
        TextField textFieldRemoveEdgeTo = new TextField();
        textFieldRemoveEdgeTo.setPromptText("Remove an edge to vertex");

        Region spacer = new Region();
        spacer.setMinHeight(Region.USE_PREF_SIZE);

        Button randomGraph = new Button("Random Graph");
        Button clearCanvas = new Button("Clear Canvas");

        Label functions = new Label("Functions");
        Label misc = new Label("Misc");


        vBox.setMaxWidth(VBoxWidth);
        vBox.setMinWidth(VBoxWidth);
        vBox.getChildren().addAll(functions, new Separator(),
                addVertex, textFieldAddVertex, new Separator(),
                deleteVertex, textFieldDeleteVertex, new Separator(),
                addEdge, textFieldAddEdgeFrom, textFieldAddEdgeTo, new Separator(),
                removeEdge, textFieldRemoveEdgeFrom, textFieldRemoveEdgeTo, new Separator(),
                spacer, misc, new Separator(),
                randomGraph, clearCanvas);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        vBox.setAlignment(Pos.TOP_CENTER);


        /* MenuBar
         * 4 tabs: File, Mode, Customization, Help
         * - File: possible actions
         * -- New: Opens new file
         * -- Save: Save file
         * -- Save as: Save file as
         * -- Open: Open file
         * -- Exit: Exit application
         *
         * - Mode: toggles
         * -- insert: Enables addVertex button and textfield to specify name of vertex to be added
         * -- delete: Enables deleteVertex button and textfield to specify name of vertex to be deleted
         * -- edgeAdd: Enables edgeAdd button and textfields for specifying vertices from which edge is to be made from
         * -- edgeRemove: Enables edgeRemove button and textfields for specifying vertices which edge is to be removed from
         *
         * - Customization
         * -- Light/Dark Mode
         * -- Vertex Customization: color of the vertex, font of the text inside of the vertex, radius of the vertex (not used yet)
         * -- Edge Customization: color of the edge, width of the edge, style of the edge (not used yet)
         *
         * - Help: for manual
         */
        menuBar = new MenuBar();
        Menu mFile = new Menu("File");
        MenuItem menuItemNew = new MenuItem("New");
        MenuItem menuItemSave = new MenuItem("Save");
        MenuItem menuItemOpen = new MenuItem("Open");
        MenuItem menuItemExport = new MenuItem("Save as");
        MenuItem menuItemExit = new MenuItem("Exit");

        scene.widthProperty().addListener((observableValue, number, t1) -> {
            canvas.setMinWidth(scene.getWidth() - 200);
            canvas.setMaxWidth(scene.getWidth() - 200);
            canvas.setBorder(border);
        });
        scene.heightProperty().addListener((observableValue, number, t1) -> {
            canvas.setMinHeight(scene.getHeight() - menuBar.getHeight());
            canvas.setMaxHeight(scene.getHeight() - menuBar.getHeight());
            canvas.setBorder(border);
            lastAction.setLayoutY(scene.getHeight() - 60);
        });

        canvas.getChildren().addListener((ListChangeListener<Node>) change -> handleCanvasChange());
        mFile.getItems().addAll(menuItemNew, menuItemSave, menuItemOpen, menuItemExport, new SeparatorMenuItem(), menuItemExit);

        Menu mModes = new Menu("Mode");
        listOfButtons = new ArrayList<>();
        Triplet<String, Button, Mode> insert = new Triplet<>("Insert Vertices...", addVertex, Mode.INSERT);
        listOfButtons.add(insert);
        Triplet<String, Button, Mode> delete = new Triplet<>("Delete Vertices...", deleteVertex, Mode.DELETE);
        listOfButtons.add(delete);
        Triplet<String, Button, Mode> edgeAdd = new Triplet<>("Add Edges...", addEdge, Mode.ADDEDGE);
        listOfButtons.add(edgeAdd);
        Triplet<String, Button, Mode> edgeRemove = new Triplet<>("Remove Edges...", removeEdge, Mode.REMOVEEDGE);
        listOfButtons.add(edgeRemove);
        RadioMenuItem menuItemInsert = new RadioMenuItem(insert.getFirst());
        RadioMenuItem menuItemDelete = new RadioMenuItem(delete.getFirst());
        RadioMenuItem menuItemAddEdges = new RadioMenuItem(edgeAdd.getFirst());
        RadioMenuItem menuItemRemoveEdges = new RadioMenuItem(edgeRemove.getFirst());

        //list of active (selected) modes
        activeModes = new ArrayList<>();

        listOfModes = new ArrayList<>();
        listOfModes.add(new Pair<>(menuItemInsert, Mode.INSERT));
        listOfModes.add(new Pair<>(menuItemDelete, Mode.DELETE));
        listOfModes.add(new Pair<>(menuItemAddEdges, Mode.ADDEDGE));
        listOfModes.add(new Pair<>(menuItemRemoveEdges, Mode.REMOVEEDGE));
        mModes.getItems().addAll(menuItemInsert, menuItemDelete, menuItemAddEdges, menuItemRemoveEdges);

        /*
         * Pairs for clearing textFields
         */
        listOfTextFields = new ArrayList<>();
        listOfTextFields.add(new Pair<>(insert.getFirst(), textFieldAddVertex));
        listOfTextFields.add(new Pair<>(delete.getFirst(), textFieldDeleteVertex));
        listOfTextFields.add(new Pair<>(edgeAdd.getFirst(), textFieldAddEdgeFrom));
        listOfTextFields.add(new Pair<>(edgeAdd.getFirst(), textFieldAddEdgeTo));
        listOfTextFields.add(new Pair<>(edgeRemove.getFirst(), textFieldRemoveEdgeFrom));
        listOfTextFields.add(new Pair<>(edgeRemove.getFirst(), textFieldRemoveEdgeTo));

        //set all modes to be toggled on on the startup of the app
        turnOnModesOnStartup();

        Menu mCustomization = new Menu("Customization");
        Menu mTheme = new Menu("Theme");
        MenuItem mVertex = new MenuItem("Vertex Customization");
        MenuItem mEdge = new MenuItem("Edge Customization");
        ToggleGroup customizationModes = new ToggleGroup();
        RadioMenuItem menuItemLight = new RadioMenuItem("Light Mode");
        RadioMenuItem menuItemDark = new RadioMenuItem("Dark Mode");

        mTheme.getItems().addAll(menuItemLight, menuItemDark);
        mCustomization.getItems().addAll(mTheme, mVertex, mEdge);

        //default - light mode selected
        menuItemLight.setSelected(true);
        menuItemLight.setToggleGroup(customizationModes);
        menuItemDark.setToggleGroup(customizationModes);

        /*
         * MenuItem Light Mode functionality
         */
        menuItemLight.setOnAction(actionEvent -> {
            canvas.setStyle("-fx-background-color: white");
            pane.setStyle("-fx-background-color: #DADADA");
            for (Node node : vBox.getChildren()) {
                if ((node instanceof TextField)
                        || (node instanceof Button)
                        || (node instanceof Separator)) {
                    node.setStyle(null);
                }
            }
        });

        /*
         * MenuItem Dark Mode functionality
         */
        menuItemDark.setOnAction(actionEvent -> {
            canvas.setStyle("-fx-background-color: #dddddd");
            pane.setStyle("-fx-background-color: #a6a6a6");
            for (Node node : vBox.getChildren()) {
                if ((node instanceof TextField) || (node instanceof Button)) {
                    node.setStyle("-fx-border-color: black; -fx-background-insets: 0");
                }
                else if (node instanceof Separator) {
                    node.setStyle("-fx-border-color:black; -fx-background-color:transparent");
                }
            }
        });

        /*
         * MenuItem Vertex Customization - opens new window with choices to customize properties of vertex
         */
        mVertex.setOnAction(actionEvent -> {
            try {
                VertexCustomization vertexCustomization = new VertexCustomization();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        /*
         * MenuItem Edge Customization - opens new window with choices to customize properties of edge
         */
        mEdge.setOnAction(actionEvent -> {
            try {
                EdgeCustomization edgeCustomization = new EdgeCustomization();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        /* Menu Help
         * -- provides user Manual for how the app works
         */
        Menu mHelp = new Menu("Help");
        MenuItem openManual = new MenuItem("Open manual");
        openManual.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
        openManual.setOnAction(actionEvent -> {
            try {
                Manual manual = new Manual();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        mHelp.getItems().add(openManual);
        menuBar.getMenus().addAll(mFile, mModes, mCustomization, mHelp);


        /*
         * Label for showing, which mode is currently being used
         * - placed on top-right of canvas
         */
        Label modeSelected = new Label("");
        updateModeSelected(modeSelected);
        modeSelected.setLayoutX(10);
        modeSelected.setLayoutY(5);
        canvas.getChildren().add(modeSelected);


        /*
         * Label for showing last action
         */
        lastAction = new Label("Last action: None");
        canvas.getChildren().add(lastAction);
        lastAction.setLayoutX(10);
        lastAction.setLayoutY(canvasHeight - 60);

        /*
         * shortcuts for some actions
         * - CTRL + N - New window
         * - CTRL + M - Open manual
         * - CTRL + O - Open file (load)
         * - CTRL + S - Save
         * - CTRL + Shift + S - Save as
         * - ALT+F4 - Exit application
         */
        menuItemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuItemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuItemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        menuItemExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        menuItemExit.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));

        /*
         * Actions when clicked on certain button/tab
         */
        menuItemNew.setOnAction(actionEvent -> newAction());
        menuItemSave.setOnAction(actionEvent -> saveAction());
        menuItemOpen.setOnAction(actionEvent -> openAction());
        menuItemExport.setOnAction(actionEvent -> saveAsAction());
        menuItemExit.setOnAction(actionEvent -> exitAction());

        menuItemInsert.setOnAction(actionEvent -> {
            insertMode(addVertex, textFieldAddVertex);
            updateModeSelected(modeSelected);
        });
        menuItemDelete.setOnAction(actionEvent -> {
            deleteMode(deleteVertex, textFieldDeleteVertex);
            updateModeSelected(modeSelected);
        });
        menuItemAddEdges.setOnAction(actionEvent -> {
            addEdgesMode(addEdge, textFieldAddEdgeFrom, textFieldAddEdgeTo);
            updateModeSelected(modeSelected);
        });
        menuItemRemoveEdges.setOnAction(actionEvent -> {
            removeEdgesMode(removeEdge, textFieldRemoveEdgeFrom, textFieldRemoveEdgeTo);
            updateModeSelected(modeSelected);
        });

        vertices = new HashMap<>();
        edges = new ArrayList<>();
        Random rnd = new Random(); //for generating random coordinates of node
        nodesAddedToCanvas = new ArrayList<>();

        /*
         * addVertex button functionality
         */
        addVertex.setOnAction(actionEvent -> {
            Text value = new Text(textFieldAddVertex.getText());
            if (value.getText().isEmpty()) {
                lastAction.setText("Last action: Could not add a vertex without specified value.");
                return;
            }

            if (vertices.containsKey(value.getText())) {
                lastAction.setText("Last action: Could not add a vertex with a value of \"" + value.getText() + "\", because there already exists one.");
                return;
            }

            double startX = 75 + rnd.nextDouble() * ((double) 4/5 * canvas.getWidth() - 75);
            double startY = 75 + rnd.nextDouble() * ((double) 4/5 * canvas.getHeight() - 75);

            Vertex newVertex = new Vertex(value, startX, startY);
            vertices.put(value.getText(), newVertex);

            canvas.getChildren().add(newVertex);
            nodesAddedToCanvas.add(newVertex);
            lastAction.setText("Last action: Inserted a node with a value of \"" + newVertex.getString() + "\"");

            makeVertexDraggable(newVertex);
            textFieldAddVertex.clear();
        });

        /*
         * deleteVertex button functionality
         */
        deleteVertex.setOnAction(actionEvent -> {
            String valueOfNode = textFieldDeleteVertex.getText();
            StringBuilder labelText = new StringBuilder();
            if (valueOfNode.isEmpty()) {
                labelText.append("Last action: Could not remove a vertex without specified value.");
            }
            else if (vertices.containsKey(valueOfNode)) {
                Vertex vertexToRemove = vertices.get(valueOfNode);
                List<Edge> edgesToRemove = edgesContainingVertex(edges, vertexToRemove);
                int numOfEdgesToRemove = edgesToRemove.size();
                if (!edgesToRemove.isEmpty()) {

                    labelText.append("Last action: Removed a node with a value of \"")
                            .append(valueOfNode)
                            .append("\" as well as edge(s): ");

                    int counter = 0;

                    for (Edge edge : edgesToRemove) {
                        counter++;
                        canvas.getChildren().remove(edge.getEdge());
                        labelText.append("[");
                        labelText.append(edge.getFrom().getString());
                        labelText.append(";");
                        labelText.append(edge.getTo().getString());
                        if (counter == numOfEdgesToRemove) labelText.append("].");
                        else labelText.append("], ");
                        edges.remove(edge);
                    }

                    vertices.remove(valueOfNode);
                    nodesAddedToCanvas.remove(vertexToRemove);
                    canvas.getChildren().remove(vertexToRemove);
                    lastAction.setText(labelText.toString());
                    textFieldDeleteVertex.clear();

                    return;
                }
                labelText.append("Last action: Removed a node with a value of \"")
                        .append(valueOfNode)
                        .append("\".");
                vertices.remove(valueOfNode);
                canvas.getChildren().remove(vertexToRemove);

            }
            else {
                labelText.append("Last action: Could not remove a vertex because it does not exist.");
            }
            lastAction.setText(labelText.toString());
            textFieldDeleteVertex.clear();
        });

        /*
         * addEdge button functionality
         */
        addEdge.setOnAction(actionEvent -> {
            String vertexFromString = textFieldAddEdgeFrom.getText();
            String vertexToString = textFieldAddEdgeTo.getText();
            StringBuilder labelText = new StringBuilder();
            if (vertexFromString.isEmpty() || vertexToString.isEmpty()) {
                labelText.append("Last action: Failed to add an edge, because you did not specify one (or both) of the vertices to add an edge.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (vertexFromString.equals(vertexToString)) {
                labelText.append("Last action: Failed to add an edge, we do not allow edges to be loops.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (!vertices.containsKey(vertexFromString)) {
                labelText.append("Last action: Failed to add an edge, because a vertex with value of \"")
                        .append(vertexFromString)
                        .append("\" does not exist.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (!vertices.containsKey(vertexToString)) {
                labelText.append("Last action: Failed to add an edge, because a vertex with value of \"")
                        .append(vertexToString)
                        .append("\" does not exist.");
                lastAction.setText(labelText.toString());
                return;
            }
            Vertex vertexFrom = vertices.get(vertexFromString);
            Vertex vertexTo = vertices.get(vertexToString);

            Edge newEdge = new Edge(vertexFrom, vertexTo);
            Edge reverseNewEdge = new Edge(vertexTo, vertexFrom);
            if (edges.contains(newEdge) || edges.contains(reverseNewEdge)) {
                labelText.append("Last action: Failed to add an edge, because edge from vertex with a value of \"")
                        .append(vertexFromString)
                        .append("\" to a vertex with a value \"")
                        .append(vertexToString)
                        .append("\" already exists.");
                lastAction.setText(labelText.toString());
                return;
            }
            edges.add(newEdge);
            nodesAddedToCanvas.add(newEdge);
            canvas.getChildren().add(newEdge.getEdge());

            /* redraw vertices so they are on top of the edge */
            canvas.getChildren().remove(vertexFrom);
            canvas.getChildren().remove(vertexTo);
            canvas.getChildren().addAll(vertexFrom, vertexTo);
            labelText.append("Last action: Added an edge from vertex with a value of \"")
                    .append(vertexFromString)
                    .append("\" to a vertex with a value of \"")
                    .append(vertexToString)
                    .append("\".");
            lastAction.setText(labelText.toString());
            textFieldAddEdgeFrom.clear();
            textFieldAddEdgeTo.clear();
        });

        /*
         * removeEdge button functionality
         */
        removeEdge.setOnAction(actionEvent -> {
            String vertexFromString = textFieldRemoveEdgeFrom.getText();
            String vertexToString = textFieldRemoveEdgeTo.getText();
            StringBuilder labelText = new StringBuilder();
            if (vertexFromString.isEmpty() || vertexToString.isEmpty()) {
                labelText.append("Last action: Failed to remove an edge, because you did not specify one (or both) of the vertices to remove an edge.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (vertexFromString.equals(vertexToString)) {
                labelText.append("Last action: Failed to remove an edge, because we do not allow loops in the graph, hence there cannot be any.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (!vertices.containsKey(vertexFromString)) {
                labelText.append("Last action: Failed to remove an edge, because vertex with a value of \"")
                        .append(vertexFromString)
                        .append("\" does not exist.");
                lastAction.setText(labelText.toString());
                return;
            }
            else if (!vertices.containsKey(vertexToString)) {
                labelText.append("Last action: Failed to remove an edge, because vertex with a value of \"")
                        .append(vertexToString)
                        .append("\" does not exist.");
                lastAction.setText(labelText.toString());
                return;
            }
            Vertex vertexFrom = vertices.get(vertexFromString);
            Vertex vertexTo = vertices.get(vertexToString);
            Edge edgeToRemove = existsEdge(edges, vertexFrom, vertexTo);
            if (edgeToRemove == null) {
                labelText.append("Last action: Failed to remove an edge, because edge from vertex with a value of \"")
                        .append(vertexFromString)
                        .append("\" to a vertex with a value of \"")
                        .append(vertexToString)
                        .append("\" does not exists.");
            }
            else {
                labelText.append("Last action: Removed an edge from vertex with a value of \"")
                        .append(vertexToString)
                        .append("\" to a vertex with a value of \"")
                        .append(vertexFromString)
                        .append("\".");
                canvas.getChildren().remove(edgeToRemove.getEdge());
                edges.remove(edgeToRemove);
                nodesAddedToCanvas.remove(edgeToRemove);
            }
            lastAction.setText(labelText.toString());
            textFieldRemoveEdgeFrom.clear();
            textFieldRemoveEdgeTo.clear();
        });

        /*
         * textfields functionality, to enhance UX - if ENTER key is pressed,
         * button tied to the textfield is pressed triggering corresponding action
         */
        textFieldAddVertex.setOnAction(actionEvent -> {
            addVertex.fire();
            textFieldAddVertex.clear();
        });
        textFieldDeleteVertex.setOnAction(actionEvent -> {
            deleteVertex.fire();
            textFieldDeleteVertex.clear();
        });
        textFieldAddEdgeFrom.setOnAction(actionEvent -> {
            addEdge.fire();
            textFieldAddEdgeFrom.clear();
            textFieldAddEdgeTo.clear();
        });
        textFieldAddEdgeTo.setOnAction(actionEvent -> {
            addEdge.fire();
            textFieldAddEdgeFrom.clear();
            textFieldAddEdgeTo.clear();
        });
        textFieldRemoveEdgeFrom.setOnAction(actionEvent -> {
            removeEdge.fire();
            textFieldAddEdgeFrom.clear();
            textFieldAddEdgeTo.clear();
        });
        textFieldRemoveEdgeTo.setOnAction(actionEvent -> {
            removeEdge.fire();
            textFieldAddEdgeFrom.clear();
            textFieldAddEdgeTo.clear();
        });

        /*
         * textfields functionality to further enhance UX
         * - if UP/DOWN arrow is pressed, focus is moved to textfield ABOVE/BELOW
         */

        textFieldAddVertex.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.DOWN)) textFieldDeleteVertex.requestFocus();
        });

        textFieldDeleteVertex.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) textFieldAddVertex.requestFocus();
            else if (keyEvent.getCode().equals(KeyCode.DOWN)) textFieldAddEdgeFrom.requestFocus();
        });

        textFieldAddEdgeFrom.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) textFieldDeleteVertex.requestFocus();
            else if (keyEvent.getCode().equals(KeyCode.DOWN)) textFieldAddEdgeTo.requestFocus();
        });

        textFieldAddEdgeTo.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) textFieldAddEdgeFrom.requestFocus();
            else if (keyEvent.getCode().equals(KeyCode.DOWN)) textFieldRemoveEdgeFrom.requestFocus();
        });

        textFieldRemoveEdgeFrom.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) textFieldAddEdgeTo.requestFocus();
            else if (keyEvent.getCode().equals(KeyCode.DOWN)) textFieldRemoveEdgeTo.requestFocus();
        });

        textFieldRemoveEdgeTo.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP)) textFieldRemoveEdgeFrom.requestFocus();
        });

        /*
         * randomGraph button functionality
         */
        randomGraph.setOnAction(actionEvent -> {
            clearNodes();

            //random number of vertices - at least 3, max 10
            int numOfVertices = (int) (Math.random() * 8) + 3;
            for (int i = 0; i < numOfVertices; i++) {
                Text vertexValue = new Text(String.valueOf(i + 1));
                double posX = 75 + rnd.nextDouble() * ((double) 4/5 * canvas.getWidth() - 75);
                double posY = 75 + rnd.nextDouble() * ((double) 4/5 * canvas.getHeight() - 75);
                Vertex newVertex = new Vertex(vertexValue, posX, posY);
                makeVertexDraggable(newVertex);
                vertices.put(vertexValue.getText(), newVertex);
                nodesAddedToCanvas.add(newVertex);
                canvas.getChildren().add(newVertex);
            }


            //max number of edges
            int maxNumberOfEdges = numOfVertices * (numOfVertices - 1) / 2;

            //nice random number of edges to be generated
            int numOfEdges = (int) (Math.random() * (maxNumberOfEdges - numOfVertices));

            for (int i = 0; i < numOfEdges; i++) {
                int firstRandomIndex = (int) (Math.random() * numOfVertices);
                int secondRandomIndex = (int) (Math.random() * numOfVertices);
                if (firstRandomIndex == secondRandomIndex) continue;
                Vertex firstVertex = vertices.get(String.valueOf(firstRandomIndex + 1));
                Vertex secondVertex = vertices.get(String.valueOf(secondRandomIndex + 1));
                if (existsEdge(edges, firstVertex, secondVertex) != null) continue;
                Edge newEdge = new Edge(firstVertex, secondVertex);
                edges.add(newEdge);
                nodesAddedToCanvas.add(newEdge);
                canvas.getChildren().add(newEdge.getEdge());
                canvas.getChildren().remove(firstVertex);
                canvas.getChildren().remove(secondVertex);
                canvas.getChildren().addAll(firstVertex, secondVertex);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Last action: Generated a random graph with ")
                    .append(numOfVertices)
                    .append(" vertices and ")
                    .append(numOfEdges)
                    .append(" edge(s).");

            lastAction.setText(sb.toString());
        });

        /*
         * clearCanvas button functionality
         */
        clearCanvas.setOnAction(actionEvent -> {
            clearNodes();
            lastAction.setText("Last action: Removed graph (cleared canvas).");
        });

        /*
         * positioning of nodes on screen
         */
        pane.setTop(menuBar);
        pane.setLeft(canvas);
        pane.setRight(vBox);

        /*
         * setting ids to enforce css styles on components
         */
        scene.getStylesheets().add("styles.css");
        modeSelected.setId("modeSelected");
        lastAction.setId("modeSelected");
        functions.setId("modeSelected");
        misc.setId("modeSelected");

        /*
         * stage format
         */
        stage.setMinWidth(defaultWindowWidth);
        stage.setMinHeight(defaultWindowHeight);
        stage.setTitle("Graph Editor");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image("https://raw.githubusercontent.com/Loso9/editor-grafov/master/icons/icon2.png"));
        stage.show();
        stage.setOnCloseRequest(this::closeWindowRequest);
    }

    /**
     * Method for starting the application start
     * @param args args
     */
    public static void main(String[] args) {
        launch(args);
    }
}