package Server.Connection;
import Server.Client.ClientInfo;
import Server.Server;
import java.net.Socket;
import java.util.UUID;


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
     * Desconecta a un cliente del servidor.
     * Elimina la información del cliente de la lista de clientes
     * y notifica la actualización de la lista. Si el cliente es
     * del tipo "Player", también se elimina la partida asociada.
     */
    public void disconnectClient() {
        synchronized (Server.clients) {
            // Buscar el cliente y su partida asociada
            for (ClientInfo client : Server.clients) {
                if (client.getSocket().equals(socket)) {
                    // Si el cliente es de tipo "Player", eliminar la partida
                    if ("Player".equals(client.getClientType())) {
                        UUID clientId = client.getClientId(); // Obtener el ID del cliente
                        // Intentar eliminar la partida correspondiente
                        Server.parties.removeIf(party -> party.getId_partida().equals(clientId));
                    }
                    // Eliminar al cliente de la lista
                    Server.clients.remove(client);
                    server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
                    return; // Salir del método una vez que se ha desconectado el cliente
                }
            }
        }
    }
}