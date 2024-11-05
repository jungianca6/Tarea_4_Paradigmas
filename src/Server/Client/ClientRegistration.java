package Server.Client;

import Game.Partida;
import Server.Comunication_Data.Register_Data;
import Server.Server;
import Server.Messaging.MessageSender;

import java.net.Socket;
import java.util.UUID;

/**
 * ClientRegistration maneja el registro de un cliente,
 * añadiéndolo a la lista del servidor.
 */
public class ClientRegistration {
    private final Server server;     // Referencia al servidor
    private final Socket socket;      // Socket del cliente
    private final UUID clientId;      // ID único del cliente
    private final MessageSender messageSender;

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
        messageSender = new MessageSender(server, clientId);
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

    /**
     * Registra el tipo de cliente (Player o Spectator) y maneja la creación de juegos o la unión de espectadores.
     *
     * @param registerData Los datos del mensaje de registro.
     */
    public void registerClientType(Register_Data registerData) {
        String type = registerData.getType(); // Obtiene el tipo de cliente

        // Verifica si el tipo de cliente es válido
        if (!isValidClientType(type)) {
            System.out.println("Tipo de cliente inválido: " + type);
            return; // Sale del método si el tipo es inválido
        }
        synchronized (Server.clients) { // Sincroniza el acceso a la lista de clientes
            ClientInfo clientInfo = server.getClientInfobyID(clientId); // Obtiene la información del cliente
            clientInfo.setClientType(type); // Establece el tipo de cliente
            System.out.println("Cliente " + clientId + " registrado como " + type.toLowerCase() + ".");

            // Si es un jugador, crea un nuevo juego
            if ("Player".equals(type)) {
                createNewGameForClient(clientInfo);
            } else { // Si es un espectador, envía la lista de juegos disponibles
                messageSender.sendGameListToClientMessage(clientInfo);
                //sendGameListToClient(clientInfo);
            }
        }
        server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
    }

    /**
     * Verifica si el tipo de cliente es válido (Player o Spectator).
     *
     * @param type El tipo de cliente.
     * @return true si el tipo es válido, false de lo contrario.
     */
    private boolean isValidClientType(String type) {
        return "Player".equals(type) || "Spectator".equals(type);
    }

    /**
     * Crea un nuevo juego para el cliente.
     *
     * @param clientInfo La información del cliente que está creando el juego.
     */
    private void createNewGameForClient(ClientInfo clientInfo) {
        UUID gameId = clientInfo.getClientId(); // Genera un ID único para la nueva partida
        Partida newGame = new Partida(gameId, clientInfo.getIpAddress(), clientInfo.getPort()); // Crea la nueva partida
        server.addPartie(newGame); // Agrega la partida al servidor
        clientInfo.setPartida(newGame); // Asocia la partida al cliente
        System.out.println("Juego creado con ID: " + newGame.getId_partida());
    }


}