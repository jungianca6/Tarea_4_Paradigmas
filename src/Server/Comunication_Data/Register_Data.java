package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Register_Data {
    @JsonProperty("type_message")
    private String typeMessage; // Tipo de mensaje (ej. "register")

    @JsonProperty("type")
    private String type; // "jugador" o "espectador"

    // Constructor por defecto
    public Register_Data() {}

    // Constructor con parámetros
    public Register_Data(String typeMessage, String type) {
        this.typeMessage = typeMessage;
        this.type = type;
    }

    // Getters y Setters
    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
