package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "bricks_data")
    @JsonProperty("pos_x")
    float pos_x;
    @JsonProperty("pos_y")
    float pos_y;
    @JsonProperty("ancho")
    float ancho;
    @JsonProperty("alto")
    float alto;

    public Player_Data(String typeMessage, float pos_x, float pos_y, float ancho, float alto) {
        this.typeMessage = typeMessage;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.ancho = ancho;
        this.alto = alto;
    }
}
