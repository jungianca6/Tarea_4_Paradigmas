package Server.Client;


import Game.Partida;

import java.net.Socket;
import java.util.UUID;

/**
 * Clase para almacenar la información del cliente.
 * Contiene detalles como el socket de conexión, ID del cliente, dirección IP,
 * puerto, tipo de cliente y la partida asociada.
 */
public class ClientInfo {
    private Socket socket;             // Socket de la conexión del cliente
    private UUID clientId;             // ID del cliente en formato GUID
    private String ipAddress;          // Dirección IP del cliente
    private int port;                  // Puerto del cliente
    private String client_type;        // Tipo de cliente: "jugador", "espectador" o "Client"
    private Partida partida;           // Partida asociada al cliente

    /**
     * Constructor de ClientInfo.
     *
     * @param socket    El socket de conexión del cliente.
     * @param clientId  El ID único del cliente.
     * @param ipAddress La dirección IP del cliente.
     * @param port      El puerto de conexión del cliente.
     */
    public ClientInfo(Socket socket, UUID clientId, String ipAddress, int port) {
        this.socket = socket;              // Asignación del socket
        this.clientId = clientId;          // Asignación del ID del cliente
        this.ipAddress = ipAddress;        // Asignación de la dirección IP
        this.port = port;                  // Asignación del puerto
        this.client_type = "Spectator";   // Valor por defecto del tipo de cliente
    }

    public Socket getSocket() {
        return socket; // Obtiene el socket de conexión del cliente
    }

    public UUID getClientId() {
        return clientId; // Obtiene el ID del cliente
    }

    public String getIpAddress() {
        return ipAddress; // Obtiene la dirección IP del cliente
    }

    public int getPort() {
        return port; // Obtiene el puerto del cliente
    }

    public String getClientType() {
        return client_type; // Obtiene el tipo de cliente
    }

    public void setClientType(String client_type) {
        this.client_type = client_type; // Establece el tipo de cliente
    }

    public Partida getPartida() {
        return partida; // Obtiene la partida asociada al cliente
    }

    public void setPartida(Partida partida) {
        this.partida = partida; // Establece la partida asociada al cliente
    }
}