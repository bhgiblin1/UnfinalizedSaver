module UnfinalizedSaver.main {
    requires javafx.controls;
    requires javafx.fxml;

    opens UnfinalizedSaver to javafx.fxml;
    exports UnfinalizedSaver;
}