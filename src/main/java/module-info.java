module UnfinalizedSaver.Main {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens UnfinalizedSaver to javafx.fxml;
    exports UnfinalizedSaver;
}