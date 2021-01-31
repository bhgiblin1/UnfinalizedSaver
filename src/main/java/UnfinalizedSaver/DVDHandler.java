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
    Path mp4File;

    SimpleDoubleProperty completePercent;

    public DVDHandler()
    {
        completePercent = new SimpleDoubleProperty();
        try {
            vobFile = Files.createTempFile(null, ".vob");
            mp4File = Files.createTempFile(null, ".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void verifyCompatibleDVD() throws RuntimeException
    {
        completePercent.set(.25);
        execute("/share/UnfinalizedSaver/executor.bsh --verify " + trackStartAddress + " " + trackSize, false);
        completePercent.set(.50);
    }

    public void copyDVD() throws RuntimeException
    {
        executeDD("/share/UnfinalizedSaver/executor.bsh --copy " + trackStartAddress + " " + trackSize
                + " /dev/sr1 " + vobFile);
    }

    private void getByteCount() throws RuntimeException
    {
        completePercent.set(.75);
        String output = execute("/share/UnfinalizedSaver/executor.bsh --bytecount", false);
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
        copyDVD();
        completePercent.set(0);
        convertVOB();
    }

    private void convertVOB()
    {
        execute("/share/UnfinalizedSaver/executor.bsh --convert " + vobFile + " " + mp4File, true);
    }

    private String execute(String command, boolean vobConvert)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (vobConvert)
                {
                    System.out.println(line);
                    var output = line.split(" ");
                    if (output.length > 6)
                        updateVOBPercent(line.split(" ")[5]);
                }
                else
                {
                    stringBuilder.append(line);
                }
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
            process.waitFor();
            if (process.exitValue() != 0)
                throw new RuntimeException();
        } catch (IOException | InterruptedException e) {
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

    private void updateVOBPercent(String percent)
    {
        try
        {
            double percentComplete = Double.parseDouble(percent);
            completePercent.set(percentComplete / 100);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing HandBrake % complete");
        }
    }

    public SimpleDoubleProperty getStageCompletePercent()
    {
        return completePercent;
    }
}
