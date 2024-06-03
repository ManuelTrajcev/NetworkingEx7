package tcp;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Worker extends Thread {
    private Socket socket;
    private File logFile;
    private RandomAccessFile counterFile;
    private static Semaphore counterSemaphote = new Semaphore(1);

    public Worker(Socket socket, File logFile, RandomAccessFile counterFile) {
        this.socket = socket;
        this.logFile = logFile;
        this.counterFile = counterFile;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void execute() throws IOException, InterruptedException {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        BufferedWriter fileWriter = null;
        String response;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
            Integer currClients = incrementCounter(counterFile);
            System.out.println("Current client: " + currClients);

            writer.write("Hello " + socket.getInetAddress() + "\n");
            writer.flush();
            fileWriter.append("Client: " + socket.getInetAddress() + " arrived ");
            writer.flush();
            response = reader.readLine();

            if (response.contains("Hello")) {
                System.out.println("Client connected successful");
                response = reader.readLine();
                fileWriter.write(response + " ");
                fileWriter.flush();

                response = reader.readLine();
                if (response.equals("log out")) {
                    writer.write("logging out");
                    writer.flush();
                    fileWriter.write("\n");
                    fileWriter.flush();
                }

            } else {
                System.out.println("Closing connection..");
                socket.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writer.flush();
            writer.close();
            fileWriter.flush();
            fileWriter.close();
            reader.close();
            socket.close();
        }

    }

    private synchronized void initializeRaf(RandomAccessFile counterRaf) throws IOException {
        if (counterRaf.length() == 0) {
            counterRaf.writeInt(0);
        }
    }

    private Integer incrementCounter(RandomAccessFile counterRaf) throws InterruptedException, IOException {
        counterSemaphote.acquire();
        counterRaf.seek(0);
        Integer curr = counterRaf.readInt();
        counterRaf.seek(0);
        counterRaf.writeInt(++curr);
        counterSemaphote.release();
        return curr++;
    }
}
