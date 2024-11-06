package Server.Comunication_Data;

import Game.Ball;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "balls_data")
    @JsonProperty("score")
    int score;
    @JsonProperty("lives")
    int lives;
    @JsonProperty("level")
    int level;

    // Constructor predeterminado
    public Stats_Data() {
    }

    public Stats_Data(String typeMessage, int score, int lives, int level) {
        this.typeMessage = typeMessage;
        this.score = score;
        this.lives = lives;
        this.level = level;
    }
    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
