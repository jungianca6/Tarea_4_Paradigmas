package Server.Comunication_Data;

import java.util.ArrayList;
import java.util.List;
import Game.Partida;
import Game.Partie_Without_Elements;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Parties_Data {
    @JsonProperty("type_message")
    private String typeMessage; // Tipo de mensaje (ej. "data_parties")

    private List<Partie_Without_Elements> parties; // Lista de partidas

    public Parties_Data() {
        this.parties = new ArrayList<>(); // Inicializa la lista de partidas
    }

    public Parties_Data(String typeMessage) {
        this.typeMessage = typeMessage;
        this.parties = new ArrayList<>(); // Inicializa la lista de partidas
    }

    // Método para agregar una partida
    public void addPartida(Partie_Without_Elements partida) {
        this.parties.add(partida);
    }

    // Método para obtener la lista de partidas
    public List<Partie_Without_Elements> getParties() {
        return parties;
    }

    // Método para convertir el objeto a JSON (usando Jackson)
    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            System.out.println("Error al convertir a JSON: " + e.getMessage());
            return null;
        }
    }

    // Método para imprimir las partidas (para pruebas)
    public void printParties() {
        for (Partie_Without_Elements partida : parties) {
            System.out.println("ID: " + partida.getId_partida() + ", IP: " + partida.getIp() + ", Puerto: " + partida.getPuerto());
        }
    }
}
