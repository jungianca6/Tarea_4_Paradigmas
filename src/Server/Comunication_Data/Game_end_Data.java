package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Game_end_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "end")

    public Game_end_Data() {
       this.typeMessage = "end";
    }
}
