package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bricks_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "bricks_data")
    @JsonProperty("columna")
    int columna;
    @JsonProperty("fila")
    int fila;
    @JsonProperty("poder")
    String poder;

    public Bricks_Data(String typeMessage, int columna, int fila, String poder) {
        this.typeMessage = typeMessage;
        this.columna = columna;
        this.fila = fila;
        this.poder = poder;
    }
}
