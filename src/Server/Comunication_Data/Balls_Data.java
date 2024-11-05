package Server.Comunication_Data;

import Game.Ball;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Balls_Data {
    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public Ball[] getBalls() {
        return balls;
    }

    public void setBalls(Ball[] balls) {
        this.balls = balls;
    }

    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "balls_data")
    @JsonProperty("balls")
    Ball[] balls;

    public Balls_Data(String typeMessage, Ball[] balls) {
        this.typeMessage = typeMessage;
        this.balls = balls;
    }
}
