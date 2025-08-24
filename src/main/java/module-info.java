module com.example.noteflowfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;                   // ✅ for HTMLEditor

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    // requires com.dlsc.formsfx;          // keep if you still use it elsewhere

    opens com.example.noteflowfrontend to javafx.fxml;
    opens com.example.noteflowfrontend.core.dto to com.fasterxml.jackson.databind;
    exports com.example.noteflowfrontend;
}
