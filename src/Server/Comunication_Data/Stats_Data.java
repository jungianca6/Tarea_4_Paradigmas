package Server.Comunication_Data;

import Game.Ball;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats_Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "balls_data")
    @JsonProperty("puntaje")
    int puntaje;
    @JsonProperty("vidas")
    int vidas;
    @JsonProperty("nivel")
    int nivel;

    // Constructor predeterminado
    public Stats_Data() {
    }

    public Stats_Data(String typeMessage, int puntaje, int vidas, int nivel) {
        this.typeMessage = typeMessage;
        this.puntaje = puntaje;
        this.vidas = vidas;
        this.nivel = nivel;
    }
    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }


    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public int getVidas() {
        return vidas;
    }

    public void setVidas(int vidas) {
        this.vidas = vidas;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

}
