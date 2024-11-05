package Server.Comunication_Data;

import Game.Ball;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Score_Level_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "score_level_data")
    @JsonProperty("nivel")
    String nivel;
    @JsonProperty("score")
    int score;

    // Constructor predeterminado
    public Score_Level_Data() {
    }

    public Score_Level_Data(String typeMessage,String nivel, int score) {
        this.typeMessage = typeMessage;
        this.nivel = nivel;
        this.score = score;
    }
}

