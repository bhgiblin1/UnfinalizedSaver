package UnfinalizedSaver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class GUIKickoff extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("scene.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("Unfinalized Saver");
        stage.setScene(scene);
        stage.setOnCloseRequest(x -> {
            try {
                Runtime.getRuntime().exec("./executor.bsh --cleanup");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        stage.show();
    }

    public void start() {
        launch();
    }
}
