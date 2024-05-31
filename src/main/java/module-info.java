module com.example.editorgrafov {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.example.editorgrafov to javafx.fxml;
    exports com.example.editorgrafov;
    exports com.example.editorgrafov.enums;
    opens com.example.editorgrafov.enums to javafx.fxml;
}