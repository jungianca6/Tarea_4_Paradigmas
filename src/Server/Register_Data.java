package Server;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Register_Data {
    @JsonProperty("type")
    private String type; // "jugador" o "espectador"

    // Constructor por defecto
    public Register_Data() {}

    // Constructor con parámetros
    public Register_Data(String type) {
        this.type = type;
    }

    // Getters y Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}