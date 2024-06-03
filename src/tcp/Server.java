package tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    private int port;
    private String filePath;
    private static File logFile;
    private String counterFilePath;
    private  static RandomAccessFile raf;

    public Server(int port, String filePath, String counterFilePath) {
        this.port = port;
        this.filePath = filePath;
        logFile = new File(filePath);
        this.counterFilePath = counterFilePath;
        try {
            raf = new RandomAccessFile(filePath, "rw");
            raf.writeInt(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("SERVER: staring...");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        System.out.println("SERVER: started!");
        System.out.println("SERVER: waiting for connections...");

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("SERVER: new client");
            Worker worker = new Worker(socket, logFile, raf);
            worker.start();
        }

    }

    public static void main(String[] args) {
        Server server = new Server(9876, "log.csv", "counter");
        server.start();
    }
}
