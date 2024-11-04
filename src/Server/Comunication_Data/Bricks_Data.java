package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bricks_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "bricks_data")
    @JsonProperty("column")
    int column;
    @JsonProperty("row")
    int row;
    @JsonProperty("poder")
    String poder;

    public Bricks_Data(String typeMessage, int column, int row, String poder) {
        this.typeMessage = typeMessage;
        this.column = column;
        this.row = row;
        this.poder = poder;
    }
}
