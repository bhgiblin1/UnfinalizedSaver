package UnfinalizedSaver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

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
        try
        {
            // TODO make this task
            new Thread(() -> dvdHandler.begin()).start();
        }
        catch (RuntimeException e)
        {
            System.out.println("FAIL");
            System.out.println(e.getMessage());
        }
    }

    public void updateProgress(double value)
    {
        if (value == 0)
            currentStage++;
        stageList.get(currentStage).setProgress(value);
    }
}
