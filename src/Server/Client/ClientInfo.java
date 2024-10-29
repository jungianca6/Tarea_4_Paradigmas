package Server.Client;


import Game.Partida;

import java.net.Socket;
import java.util.UUID;

// Clase para almacenar la información del cliente
public class ClientInfo {
    private Socket socket;
    private UUID clientId; // ID del cliente en formato GUID
    private String ipAddress; // Dirección IP del cliente
    private int port; // Puerto del cliente
    private String client_type; // Tipo de cliente: "jugador", "espectador" o "Client"
    private Partida partida; // Partida asociada al cliente

    public ClientInfo(Socket socket, UUID clientId, String ipAddress, int port) {
        this.socket = socket;
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.client_type = "Spectator"; // Valor por defecto
    }

    public Socket getSocket() {
        return socket;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getClientType() {
        return client_type; // Getter para obtener el tipo de cliente
    }

    public void setClientType(String client_type) {
        this.client_type = client_type; // Setter para actualizar el tipo de cliente
    }

    public Partida getPartida() {
        return partida; // Getter para obtener la partida asociada
    }

    public void setPartida(Partida partida) {
        this.partida = partida; // Setter para actualizar la partida asociada
    }
}