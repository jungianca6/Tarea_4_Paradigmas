package Server.Connection;

import Server.Server;

import java.net.Socket;

/**
 * ConnectionManager handles the disconnection of a client, removing them from the server's list.
 */
public class ConnectionManager {
    private final Server server;
    private final Socket socket;

    public ConnectionManager(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void disconnectClient() {
        synchronized (Server.clients) {
            Server.clients.removeIf(client -> client.getSocket().equals(socket));
            server.notifyClientListUpdated();
            System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
        }
    }
}