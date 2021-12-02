/*
    File that handles client functionalities
*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

class CalcClient {

    public static void main(String[] argv) throws Exception {
        Scanner in = new Scanner(System.in);
        String input;

        Socket clientSocket = null;
        DataOutputStream outToServer = null;
        DataInputStream inFromServer = null;

        // Get a server to connect to, from the user
        System.out.println("Enter the server's host IP (ex: 127.0.0.1):");
        String host = in.nextLine();

        // Get a valid username that is not currently being tracked by the server
        String username = null;
        while (username == null) {
            System.out.println("Enter your username:");
            input = in.nextLine();

            // Establish connection with server
            try {
                clientSocket = new Socket(host, 6789);
            } catch (Exception e) {
                System.out.println("Error connecting to server: " + e);
                return;
            }
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new DataInputStream(clientSocket.getInputStream());

            // Send username to server
            outToServer.writeUTF("USR:" + input);
            String loginResponse = inFromServer.readUTF();

            if (loginResponse.startsWith("ACC:")) {
                System.out.println("Your username '" + input + "' was accepted by the server!\n");
                System.out.println("Welcome to the calculator, This calculator supports...\nAddition: +  \nMultiplication: * \nSubtraction: - \nDivision: \\ \nGrouping of operations: ()\n");
                username = input; // break condition
            } else {
                System.out.println("Your username '" + input + "' was not accepted by the server. Please try a " +
                        "different one");
                clientSocket.close();
            }
        }

        // Now that the username is known to be valid, send equations over
        System.out.println("Enter an equation to evaluate the result, or 'quit' to quit:");
        while (!(input = in.nextLine()).equals("quit")) {
            // Send input to server
            outToServer.writeUTF("RUN:" + input);
            String response = inFromServer.readUTF();
            if (response.startsWith("ANS:")) {
                System.out.println("Server responded: " + response.substring(4));
            } else {
                System.out.println("Error: " + response.substring(4));
            }
            System.out.println("Enter an equation to evaluate the result, or 'quit' to quit:");
        }

        outToServer.writeUTF("END:Quit by client");
        clientSocket.close();
    }
}

        
