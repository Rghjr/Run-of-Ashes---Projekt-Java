module RunOfAshes {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.runofashes to javafx.fxml;
    exports com.runofashes;
}