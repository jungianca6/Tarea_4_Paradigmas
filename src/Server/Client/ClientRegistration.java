package Server.Client;

import Server.Server;

import java.net.Socket;
import java.util.UUID;

/**
 * ClientRegistration handles the registration of a client, adding them to the server's list.
 */
class ClientRegistration {
    private final Server server;
    private final Socket socket;
    private final UUID clientId;

    public ClientRegistration(Server server, Socket socket, UUID clientId) {
        this.server = server;
        this.socket = socket;
        this.clientId = clientId;
    }

    public void registerClient() {
        synchronized (Server.clients) {
            Server.clients.add(new ClientInfo(socket, clientId, socket.getInetAddress().getHostAddress(), socket.getPort()));
            server.notifyClientListUpdated();
        }
    }
}