package UnfinalizedSaver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class MediaViewer
{
    @FXML
    VBox videoVBox;
    @FXML
    TextArea fileName;

    Path videoFile;
    String outputDir;

    public MediaViewer(Path videoFile, String outputDir)
    {
        this.videoFile = videoFile;
        this.outputDir = outputDir;
    }

    @FXML
    public void initialize()
    {
        HBox controls = new HBox();
        Slider timeSlider = new Slider();
        Label duration = new Label();
        Label timestamp = new Label();
        MediaPlayer mediaPlayer = new MediaPlayer(new Media("file://" + videoFile.toString()));
        MediaView mediaViewer = new MediaView(mediaPlayer);

        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        controls.setMaxWidth(videoVBox.getMaxWidth());
        controls.setMinWidth(videoVBox.getMinWidth());
        mediaViewer.fitWidthProperty().bind(videoVBox.widthProperty());

        mediaPlayer.setOnReady(() -> {
            duration.setText(String.format("%.2f", mediaPlayer.getTotalDuration().toMinutes()));
        });
        timeSlider.valueProperty().addListener(x -> {
            mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
        });
        mediaPlayer.currentTimeProperty().addListener(x -> {
            timestamp.setText(String.format("%.2f", mediaPlayer.getCurrentTime().toMinutes()) + "/");
        });

        videoVBox.getChildren().addAll(mediaViewer, controls);
        controls.getChildren().addAll(timeSlider, timestamp, duration);

        mediaPlayer.play();
    }

    @FXML
    public void done()
    {
        Path destination = Paths.get(outputDir + "/" + fileName.getText()+ ".mp4");
        try {
            Files.copy(videoFile, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((Stage) videoVBox.getScene().getWindow()).close();
    }
}
