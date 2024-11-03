package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "bricks_data")
    @JsonProperty("pos_x")
    int pos_x;
    @JsonProperty("pos_y")
    int pos_y;
    @JsonProperty("ancho")
    int ancho;
    @JsonProperty("alto")
    int alto;

    public Player_Data(String typeMessage, int pos_x, int pos_y, int ancho, int alto) {
        this.typeMessage = typeMessage;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.ancho = ancho;
        this.alto = alto;
    }
}
