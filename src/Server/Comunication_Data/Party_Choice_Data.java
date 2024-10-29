package Server.Comunication_Data;

public class Party_Choice_Data {
    private String type_message; // Tipo de mensaje
    private String id_partida;    // ID de la partida
    private String ip;            // Dirección IP
    private int puerto;           // Puerto

    // Constructor
    public Party_Choice_Data(String type_message, String id_partida, String ip, int puerto) {
        this.type_message = type_message;
        this.id_partida = id_partida;
        this.ip = ip;
        this.puerto = puerto;
    }

    // Constructor por defecto
    public Party_Choice_Data() {
    }

    // Getters
    public String getType_message() {
        return type_message;
    }

    public String getId_partida() {
        return id_partida;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    // Setters
    public void setType_message(String type_message) {
        this.type_message = type_message;
    }

    public void setId_partida(String id_partida) {
        this.id_partida = id_partida;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }
}