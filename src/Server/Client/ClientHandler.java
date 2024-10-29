package Server.Client;


import Server.Connection.ConnectionManager;
import Server.Messaging.MessageHandler;
import Server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * ClientHandler handles the communication and lifecycle of a connected client.
 * It registers the client, processes messages using MessageHandler, and handles client disconnection.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UUID clientId;
    private final Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID();
        new ClientRegistration(server, socket, clientId).registerClient();
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            String messageJson;
            MessageHandler messageHandler = new MessageHandler(server, socket, clientId);

            // Continuously listens for messages from the client
            while ((messageJson = input.readLine()) != null) {
                messageHandler.handleMessage(messageJson);
            }
        } catch (IOException e) {
            System.err.println("Error in communication with client " + clientId + ": " + e.getMessage());
        } finally {
            // Handle client disconnection when the communication ends
            new ConnectionManager(server, socket).disconnectClient();
        }
    }
}