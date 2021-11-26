import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

/**
 * Thread to handle each client connection
 */
class ClientHandler extends Thread {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Socket socket;
    DataInputStream inFromClient;
    DataOutputStream outToClient;
    String username;
    Date startTime;

    ClientHandler(Socket socket, String username) throws Exception {
        this.socket = socket;
        this.inFromClient = new DataInputStream(socket.getInputStream());
        this.outToClient = new DataOutputStream(socket.getOutputStream());
        this.username = username;
        this.startTime = new Date();

        // Log data about client
        System.out.println("New user connected with username '" + username + "' at time " + df.format(this.startTime));
    }

    String evaluateCalculation(String input) {
        return "Example: Your input was " + input;
    }

    @Override
    public void run() {
        String clientRequest;
        try {
            while (!(clientRequest = inFromClient.readUTF()).startsWith("END:")) {
                if (clientRequest.startsWith("RUN:"))
                    outToClient.writeUTF("ANS:" + evaluateCalculation(clientRequest.substring(4)));
                else
                    outToClient.writeUTF("ERR:Invalid request: '" + clientRequest.substring(0, 3) + "'");
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("Connection with " + username + " closed unexpectedly");
        }

        Date curTime = new Date();
        long durationSecs = (curTime.getTime() - this.startTime.getTime()) / 1000;
        System.out.println("Connection with " + username + " closed at " + df.format(curTime) + " after " + durationSecs + " seconds");
    }
}

class CalcServer {
    public static void main(String[] argv) throws Exception {
        HashSet<String> usernames = new HashSet<>();
        ServerSocket welcomeSocket = new ServerSocket(6789);

        while (true) {
            // Accept new incoming connection
            Socket connectionSocket = welcomeSocket.accept();
            DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String usernameRequest = inFromClient.readUTF();
            if (usernameRequest.startsWith("USR:")) {
                String username = usernameRequest.substring(4);
                if (!usernames.contains(username)) {
                    usernames.add(username);
                    outToClient.writeUTF("ACC:User registered");

                    // kick off a thread to handle client requests
                    ClientHandler clientHandler = new ClientHandler(connectionSocket, username);
                    clientHandler.start();
                } else {
                    outToClient.writeUTF("ERR:Username already taken");
                    connectionSocket.close();
                }
            } else {
                outToClient.writeBytes("ERR:First operation must be USR to request username");
                connectionSocket.close();
            }
        }
    }
}
 

           
