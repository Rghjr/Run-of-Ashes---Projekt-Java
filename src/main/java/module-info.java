module RunOfAshes {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;

    opens com.runofashes to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.runofashes;
}