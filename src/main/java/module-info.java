module com.example.noteflowfrontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.example.noteflowfrontend to javafx.fxml;
    opens com.example.noteflowfrontend.core.dto to com.fasterxml.jackson.databind;
    exports com.example.noteflowfrontend;
}