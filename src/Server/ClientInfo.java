package Server;


import java.net.Socket;
import java.util.UUID;

// Clase para almacenar la información del cliente
class ClientInfo {
    private Socket socket;
    private UUID clientId; // ID del cliente en formato GUID
    private String ipAddress; // Dirección IP del cliente
    private int port; // Puerto del cliente

    public ClientInfo(Socket socket, UUID clientId, String ipAddress, int port) {
        this.socket = socket;
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.port = port;
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
}