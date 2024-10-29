package Game;

import java.util.UUID;

public class Partida {
    private UUID id_partida; // ID de la partida en formato GUID
    private String ip; // Dirección IP
    private int puerto; // Puerto

    public Partida(UUID id_partida, String ip, int puerto) {
        this.id_partida = id_partida;
        this.ip = ip;
        this.puerto = puerto;
    }

    public UUID getId_partida() {
        return id_partida;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }
}