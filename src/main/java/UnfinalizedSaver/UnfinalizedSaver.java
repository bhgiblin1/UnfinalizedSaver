package UnfinalizedSaver;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UnfinalizedSaver
{
    @FXML
    TextField device;
    @FXML
    TextField saveLoc;
    @FXML
    ProgressIndicator verifyProgressInd;
    @FXML
    ProgressIndicator copyProgressInd;
    @FXML
    ProgressIndicator convertProgressInd;

    int currentStage;
    List<ProgressIndicator> stageList;

    DVDHandler dvdHandler;

    @FXML
    public void initialize()
    {
        stageList = new ArrayList<>();
        stageList.add(verifyProgressInd);
        stageList.add(copyProgressInd);
        stageList.add(convertProgressInd);
        currentStage = 0;
    }

    @FXML
    public void launchFileExplorer()
    {
        Stage stage = new Stage();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File saveDir = directoryChooser.showDialog(stage);
        saveLoc.setText(saveDir.toString());
    }

    @FXML
    public void begin()
    {
        currentStage = 0;
        for (var stage : stageList)
            stage.setProgress(0);
        dvdHandler = new DVDHandler();
        dvdHandler.getStageCompletePercent().addListener(((observable, oldValue, newValue) -> updateProgress(newValue.doubleValue())));

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    dvdHandler.begin();
                }
                catch (RuntimeException e)
                {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to copy DVD");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    // make sure setOnSucceeded is not called
                    throw new RuntimeException();
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> displayMedia(dvdHandler));
//        displayMedia(dvdHandler);

        new Thread(task).start();
    }

    private void displayMedia(DVDHandler dvdHandler)
    {
        Parent root;
        try {
            MediaViewer mediaViewer = new MediaViewer(dvdHandler.getmp4File(), saveLoc.getText());
//            MediaViewer mediaViewer = new MediaViewer(Paths.get("/dev/shm/fulldisk.mp4"), saveLoc.getText());
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("mediaViewer.fxml"));
            loader.setController(mediaViewer);
            root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setTitle("Media Viewer");
            stage.setScene(scene);
            stage.setOnCloseRequest(x -> mediaViewer.cleanup());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateProgress(double value)
    {
        if (value == 1)
            stageList.get(currentStage).setStyle("-fx-progress-color: green;");
        else if (value == 0)
            currentStage++;
        stageList.get(currentStage).setProgress(value);
    }
}
