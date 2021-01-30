package UnfinalizedSaver;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DVDHandler
{
    // File format for Panasonic ???
    // Other camera formats will likely be different
    int trackStartAddress = 34816;
    int trackSize = 681376;
    long byteCount = 0;

    SimpleIntegerProperty completePercent;

    public DVDHandler()
    {
        completePercent = new SimpleIntegerProperty();
    }

    public void verifyCompatibleDVD() throws RuntimeException
    {
        completePercent.set(25);
        execute("/share/UnfinalizedSaver/executor.bsh --verify " + trackStartAddress + " " + trackSize);
        completePercent.set(50);
        getByteCount();
    }

    private void getByteCount() throws RuntimeException
    {
        completePercent.set(75);
        String output = execute("/share/UnfinalizedSaver/executor.bsh --bytecount");
        byteCount = Long.parseLong(output);
        System.out.println(byteCount);
        if (byteCount <= 0)
            throw new RuntimeException("Invalid byteCount =" + byteCount);
        completePercent.set(100);
    }

    public SimpleIntegerProperty getStageCompletePercent()
    {
        return completePercent;
    }

    public String execute(String command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }
            process.waitFor();
            if (process.exitValue() != 0)
                throw new RuntimeException(stringBuilder.toString());
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
