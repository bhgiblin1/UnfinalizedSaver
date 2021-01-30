package UnfinalizedSaver;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
        try
        {
            dvdHandler.verifyCompatibleDVD();

        }
        catch (RuntimeException e)
        {
            System.out.println("FAIL");
            System.out.println(e.getMessage());
        }
    }
}
