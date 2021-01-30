package UnfinalizedSaver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DVDHandler
{
    // File format for Panasonic ???
    // Other camera formats will likely be different
    int trackStartAddress = 34811;
    int trackSize = 681376;
    int blockSize = 2048;

    public void verifyCompatibleDVD() throws RuntimeException
    {
        execute("/share/UnfinalizedSaver/executor.bsh --verify " + trackStartAddress + " " + trackSize);
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
