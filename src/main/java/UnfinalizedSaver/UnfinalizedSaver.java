package UnfinalizedSaver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;

public class UnfinalizedSaver
{
    @FXML
    TextField device;
    @FXML
    TextField saveLoc;
    @FXML
    ProgressIndicator verifyProgressInd;

    ProgressIndicator currentStage;

    DVDHandler dvdHandler;

    @FXML
    public void initialize()
    {
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
        dvdHandler = new DVDHandler();
        dvdHandler.getStageCompletePercent().addListener(((observable, oldValue, newValue) -> updateProgress(newValue.intValue())));
        try
        {
            currentStage = verifyProgressInd;
            dvdHandler.verifyCompatibleDVD();

        }
        catch (RuntimeException e)
        {
            System.out.println("FAIL");
            System.out.println(e.getMessage());
        }
    }

    public void updateProgress(int value)
    {
        currentStage.setProgress((double) value / 100);
    }
}
