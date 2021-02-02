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
        execute("/share/UnfinalizedSaver/executor.bsh --verify " + trackStartAddress + " " + trackSize,
                false, false);
        completePercent.set(.50);
    }

    public void copyDVD() throws RuntimeException
    {
        execute("/share/UnfinalizedSaver/executor.bsh --copy " + trackStartAddress + " " + trackSize
                + " /dev/sr1 " + vobFile, true, false);
    }

    private void getByteCount() throws RuntimeException
    {
        completePercent.set(.75);
        String output = execute("/share/UnfinalizedSaver/executor.bsh --bytecount", false, false);
        byteCount = Long.parseLong(output);
//        System.out.println("Byte count = " + byteCount);
        if (byteCount <= 0)
            throw new RuntimeException("Invalid byteCount =" + byteCount);
        completePercent.set(1);
    }

    private void convertVOB()
    {
        execute("/share/UnfinalizedSaver/executor.bsh --convert " + vobFile + " " + mp4File,
                false,true);
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

    private String execute(String command, boolean ddCommand, boolean handbrakeCommand)
    {
        StringBuilder stringBuilder = new StringBuilder();
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader;

            // dd writes to stderr
            if (ddCommand)
                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            else
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if (handbrakeCommand)
                {
                    var output = line.split(" ");
                    if (output.length > 6)
                        updateVOBPercent(line.split(" ")[5]);
                }
                else if (ddCommand)
                {
                    updateDDPercent(line.split(" ")[0]);
                }
                else
                {
                    stringBuilder.append(line);
                }
//               System.out.println(line);
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

    public Path getmp4File()
    {
        return mp4File;
    }
}
