package Server.Comunication_Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    @JsonProperty("type_message")
    public String typeMessage; // Tipo de mensaje (ej. "data")

    @JsonProperty("message")
    public String message;

    @JsonProperty("number")
    public int number;

    @JsonProperty("status")
    public int status;

    // Constructor por defecto
    public Data() {}

    // Constructor con parámetros
    public Data(String typeMessage, String message, int number, int status) {
        this.typeMessage = typeMessage;
        this.message = message;
        this.number = number;
        this.status = status;
    }
}