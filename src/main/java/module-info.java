module com.example.noteflowfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.prefs;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires javafx.swing;

    opens com.example.noteflowfrontend to javafx.fxml;
    opens com.example.noteflowfrontend.core.dto to com.fasterxml.jackson.databind;
    exports com.example.noteflowfrontend;
}
