module cft {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;

    // Database
    requires com.zaxxer.hikari;
    requires java.sql;

    // Utilities
    requires org.apache.commons.csv;
    requires org.slf4j;

    // iTextPDF (use automatic module names)
    requires kernel;
    requires layout;
    requires io;
    requires java.desktop;
    requires de.jensd.fx.glyphs.fontawesome;

    opens com.cofaktory.footprint to javafx.fxml;
    opens views to javafx.fxml;
    opens com.cofaktory.footprint.controllers to javafx.fxml;

    opens com.cofaktory.footprint.model to javafx.base, javafx.fxml;

    exports com.cofaktory.footprint;

    // Export your packages for use by JavaFX and FXML
    exports com.cofaktory.footprint.controllers;
    exports com.cofaktory.footprint.model;


}
