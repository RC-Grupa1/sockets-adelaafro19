import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * TCP Chat Server - listens on port 12345 and broadcasts messages to all clients.
 * Encoding: UTF-8
 */
public class ChatServer {

    private static final int PORT = 12345;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {
        System.out.println("=== TCP Chat Server ===");
        System.out.println("Listening on port " + PORT + " (UTF-8 encoding)");
        System.out.println("Capture traffic in Wireshark: tcp.port == " + PORT);
        System.out.println("Waiting for connections...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // Accept clients indefinitely
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] New connection from: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);

                Thread t = new Thread(handler);
                t.setDaemon(true);
                t.start();
            }
        }
    }

    /** Broadcast a message to every connected client except the sender. */
    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /** Remove a disconnected client from the pool. */
    static void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[-] Client disconnected. Active clients: " + clients.size());
    }

    // -------------------------------------------------------------------------

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private String username = "Anonymous";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            ) {
                out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                // First message from client is the username
                out.println("SERVER: Enter your username:");
                username = in.readLine();
                if (username == null || username.isBlank()) username = "Anonymous";

                System.out.println("[*] User joined: " + username);
                broadcast("SERVER: " + username + " has joined the chat.", this);
                out.println("SERVER: Welcome, " + username + "! Type 'exit' to quit.\n");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("exit")) {
                        // Graceful disconnect
                        out.println("SERVER: Goodbye, " + username + "!");
                        broadcast("SERVER: " + username + " has left the chat.", this);
                        System.out.println("[*] " + username + " sent EXIT — closing socket.");
                        break;
                    }

                    String formatted = "[" + username + "]: " + line;
                    System.out.println(formatted);
                    broadcast(formatted, this);
                }

            } catch (IOException e) {
                System.out.println("[!] Connection error for " + username + ": " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                removeClient(this);
            }
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
