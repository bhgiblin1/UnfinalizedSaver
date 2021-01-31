package UnfinalizedSaver;

import javafx.beans.property.SimpleDoubleProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DVDHandler
{
    // File format for Panasonic ???
    // Other camera formats will likely be different
    int trackStartAddress = 34816;
    int trackSize = 681376;
    long byteCount = 0;
    Path vobFile;

    SimpleDoubleProperty completePercent;

    public DVDHandler()
    {
        completePercent = new SimpleDoubleProperty();
    }

    public void verifyCompatibleDVD() throws RuntimeException
    {
        completePercent.set(.25);
        execute("/share/UnfinalizedSaver/executor.bsh --verify " + trackStartAddress + " " + trackSize);
        completePercent.set(.50);
        getByteCount();
    }

    public void copyDVD() throws RuntimeException
    {
        try {
            vobFile = Files.createTempFile(null, ".vob");
            executeDD("/share/UnfinalizedSaver/executor.bsh --copy " + trackStartAddress + " " + trackSize
                    + " /dev/sr1 " + vobFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getByteCount() throws RuntimeException
    {
        completePercent.set(.75);
        String output = execute("/share/UnfinalizedSaver/executor.bsh --bytecount");
        byteCount = Long.parseLong(output);
        System.out.println("Byte count = " + byteCount);
        if (byteCount <= 0)
            throw new RuntimeException("Invalid byteCount =" + byteCount);
        completePercent.set(1);
    }

    public void begin()
    {
        verifyCompatibleDVD();
        getByteCount();
        completePercent.set(0);
        //copyDVD();
        completePercent.set(0);
        convertVOB();
    }

    private void convertVOB()
    {

    }


    private String execute(String command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line ;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
//                System.out.println(line);
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

    private void executeDD(String command)
    {
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            // dd writes to stderr
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                updateDDPercent(line.split(" ")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDDPercent(String bytesCopiedStr)
    {
        try
        {
            long bytesCopied = Long.parseLong(bytesCopiedStr);
            completePercent.set(bytesCopied / (double) byteCount);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Error parsing bytes read");
        }
    }

    public SimpleDoubleProperty getStageCompletePercent()
    {
        return completePercent;
    }
}
