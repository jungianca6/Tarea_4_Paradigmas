package Server.Connection;
import Server.Server;
import java.net.Socket;


/**
 * ConnectionManager maneja la desconexión de un cliente,
 * eliminándolo de la lista del servidor.
 */
public class ConnectionManager {
    private final Server server;     // Referencia al servidor
    private final Socket socket;      // Socket del cliente a desconectar

    /**
     * Constructor de ConnectionManager.
     *
     * @param server El servidor donde se manejará la desconexión.
     * @param socket El socket del cliente que se va a desconectar.
     */
    public ConnectionManager(Server server, Socket socket) {
        this.server = server;           // Asignación del servidor
        this.socket = socket;           // Asignación del socket
    }

    /**
     * Desconecta al cliente del servidor.
     * Elimina la información del cliente de la lista de clientes
     * y notifica la actualización de la lista de clientes.
     */
    public void disconnectClient() {
        synchronized (Server.clients) {
            // Se elimina al cliente de la lista
            Server.clients.removeIf(client -> client.getSocket().equals(socket));
            server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
            System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress()); // Mensaje de desconexión
        }
    }
}