import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * TCP Chat Client - connects to ChatServer on localhost:12345.
 * Encoding: UTF-8
 * Type 'exit' to close the connection gracefully.
 */
public class ChatClient {

    private static final String HOST = "100.77.145.120";  // ← replace x.x.x with real values
    private static final int PORT = 12345;            // keep this — TCP always needs a port

    public static void main(String[] args) throws IOException {
        System.out.println("=== TCP Chat Client ===");
        System.out.println("Connecting to " + HOST + ":" + PORT + " ...");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader serverIn = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter serverOut = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner keyboard = new Scanner(System.in, StandardCharsets.UTF_8);
        ) {
            System.out.println("Connected!\n");

            // Thread: read messages coming from the server and print them
            Thread readerThread = new Thread(() -> {
                try {
                    String serverLine;
                    while ((serverLine = serverIn.readLine()) != null) {
                        System.out.println(serverLine);
                    }
                } catch (IOException e) {
                    // Server closed the connection
                    System.out.println("\n[!] Connection closed by server.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Main thread: read user input and send to server
            while (keyboard.hasNextLine()) {
                String userInput = keyboard.nextLine();
                serverOut.println(userInput);          // send UTF-8 text

                if (userInput.equalsIgnoreCase("exit")) {
                    // Give the server a moment to reply, then quit
                    Thread.sleep(300);
                    System.out.println("[Client] Socket closed. Goodbye!");
                    break;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ConnectException e) {
            System.out.println("[!] Could not connect to server. Is ChatServer running?");
        }
    }
}
