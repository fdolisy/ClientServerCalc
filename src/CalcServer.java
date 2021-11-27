import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Stack;

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
        if (input == null || input.length() == 0) {
            return "Invalid Equation: No input given";
        }
        String equation = input.trim().replaceAll("[ ]+", "");
        if (input == null || input.length() == 0) {
            return "Invalid Equation: No input given";
        }

        Stack<Character> operations = new Stack<Character>();
        Stack<Integer> numbers = new Stack<Integer>();

        int current = 0;
        while (current < equation.length()) {
            int number = 0;
            if (Character.isDigit(equation.charAt(current))) {
                while (current < equation.length() && Character.isDigit(equation.charAt(current))) {
                    number = number * 10 + Character.getNumericValue(equation.charAt(current));
                    ++current;
                }
                numbers.push(number);
            } else if (equation.charAt(current) == '-' && (current == 0 || !Character.isDigit(equation.charAt(current - 1)))) {
                current++;
                while (current < equation.length() && Character.isDigit(equation.charAt(current))) {
                    number = number * 10 + Character.getNumericValue(equation.charAt(current));
                    ++current;
                }
                numbers.push(-number);
            } else {
                char operation = equation.charAt(current);
                if (operations.isEmpty()) {
                    operations.push(operation);
                    ++current;
                } else if (operation == '(') {
                    operations.push(operation);
                    ++current;
                } else if (operation == ')') {
                    while (operations.peek() != '(') {
                        calculateHelper(numbers, operations);
                    }
                    operations.pop();
                    ++current;
                } else if (operation == '*' || operation == '/') {
                    char lastOperation = operations.peek();
                    if (lastOperation == '(') {
                        operations.push(operation);
                        ++current;
                    } else if (lastOperation == '*' || lastOperation == '/') {
                        calculateHelper(numbers, operations);
                    } else if (lastOperation == '+' || lastOperation == '-') {
                        operations.push(operation);
                        current++;
                    } else {
                        return "Invalid Expression";
                    }
                } else if (operation == '+' || operation == '-') {
                    char lastOperation = operations.peek();
                    if (lastOperation == '(') {
                        operations.push(operation);
                        ++current;
                    } else {
                        calculateHelper(numbers, operations);
                    }
                } else {
                    return "Invalid Expression";
                }
            }
        }

        while (!operations.isEmpty()) {
            calculateHelper(numbers, operations);
        }

        return numbers.peek().toString();
    }

    void calculateHelper(Stack<Integer> numbers, Stack<Character> operations) {
        int secondNumber = numbers.pop();
        int firstNumber = numbers.pop();
        char operation = operations.pop();

        int answer;

        if (operation == '+') {
            answer = firstNumber + secondNumber;
        } else if (operation == '-') {
            answer = firstNumber - secondNumber;
        } else if (operation == '*') {
            answer = firstNumber * secondNumber;
        } else if (operation == '/') {
            answer = firstNumber / secondNumber;
        } else {
            System.out.println("Invalid Equation");
            return;
        }
        numbers.push(answer);
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
 

           
