import java.io.*;

/*
Data holding class.
 */
public class Storage implements Serializable {

    private String command;
    private byte[] data;

    public Storage(String command, ByteArrayOutputStream baos) {
        setData(baos);
        setCommand(command);
    }

    public Storage(String command) {
        setCommand(command);
    }

    public Storage(String command, byte[] data) {
        setData(data);
        setCommand(command);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setData(ByteArrayOutputStream baos) {
        data = baos.toByteArray();
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}