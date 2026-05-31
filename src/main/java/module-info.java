module RunOfAshes {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    exports com.runofashes.model;
    opens com.runofashes.model to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.runofashes.engine;
    opens com.runofashes.engine to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.runofashes.ui;
    opens com.runofashes.ui to com.fasterxml.jackson.databind, javafx.fxml;
    exports com.runofashes.utils;
    opens com.runofashes.utils to com.fasterxml.jackson.databind, javafx.fxml;
}