module com.example.noteflowfrontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens com.example.noteflowfrontend to javafx.fxml;
    exports com.example.noteflowfrontend;
}