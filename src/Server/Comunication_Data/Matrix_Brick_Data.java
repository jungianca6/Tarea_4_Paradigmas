package Server.Comunication_Data;

import Bloques.Bloque;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Matrix_Brick_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "bricks_data")

    @JsonProperty("brick_matrix")
    public Bloque[][] brick_matrix; // Tipo de mensaje (ej. "bricks_data")

    public Matrix_Brick_Data(String typeMessage, Bloque[][] brick_matrix) {
        this.typeMessage = typeMessage;
        this.brick_matrix = brick_matrix;
    }

}
