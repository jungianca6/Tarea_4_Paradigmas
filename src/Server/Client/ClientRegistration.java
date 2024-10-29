package Server.Client;

import Server.Server;

import java.net.Socket;
import java.util.UUID;

/**
 * ClientRegistration maneja el registro de un cliente,
 * añadiéndolo a la lista del servidor.
 */
class ClientRegistration {
    private final Server server;     // Referencia al servidor
    private final Socket socket;      // Socket del cliente
    private final UUID clientId;      // ID único del cliente

    /**
     * Constructor de ClientRegistration.
     *
     * @param server   El servidor donde se registrará el cliente.
     * @param socket   El socket del cliente.
     * @param clientId El ID único del cliente.
     */
    public ClientRegistration(Server server, Socket socket, UUID clientId) {
        this.server = server;           // Asignación del servidor
        this.socket = socket;           // Asignación del socket
        this.clientId = clientId;       // Asignación del ID del cliente
    }

    /**
     * Registra al cliente en el servidor.
     * Añade la información del cliente a la lista de clientes del servidor
     * y notifica la actualización de la lista de clientes.
     */
    public void registerClient() {
        synchronized (Server.clients) {
            // Se agrega un nuevo cliente a la lista
            Server.clients.add(new ClientInfo(socket, clientId, socket.getInetAddress().getHostAddress(), socket.getPort()));
            server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
        }
    }
}