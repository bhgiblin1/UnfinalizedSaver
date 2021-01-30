package UnfinalizedSaver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class GUIKickoff extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("scene.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Unfinalized Saver");
        stage.setScene(scene);
        stage.show();
    }

    public void start() {
        launch();
    }
}
