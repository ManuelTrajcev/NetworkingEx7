package tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends Thread {
    private int serverPort;
    private String serverName;

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        BufferedWriter fileWriter = null;
        String response;
        try {
            File file = new File("./src/tcp/clientData/test.txt");
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            socket = new Socket(InetAddress.getByName(this.serverName), this.serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            fileWriter.write("new client\n");
            fileWriter.flush();

            response = reader.readLine();
            if (response.contains("Hello")) {
                System.out.println("Connection successful");
                System.out.println(response);
                writer.write("Hello " + socket.getPort() + "\n");
                writer.flush();

                writer.write("File size: " + file.length() + "\n");
                writer.flush();

                writer.write("log out\n");
                writer.flush();
                response = reader.readLine();
                if (response.equals("logging out")) {
                    System.out.println("Closing connection..");
                    socket.close();
                }

            } else {
                System.out.println("Closing connection..");
                socket.close();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) {
        String serverName = System.getenv("SERVER_NAME");
        String serverPort = System.getenv("SERVER_PORT");
        if (serverPort == null) {
            throw new RuntimeException("Server port should be defined as ENV {SERVER_PORT}.");
        }
        Client client = new Client(serverName, Integer.parseInt(serverPort));
        client.start();
    }

}
