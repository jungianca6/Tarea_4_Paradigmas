package Server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Data {
    @JsonProperty("message")
    public String message;

    @JsonProperty("number")
    public int number;

    @JsonProperty("status")
    public int status;


    // Constructor por defecto
    public Data() {}

    // Constructor con parámetros
    public Data(String message, int number, int status) {
        this.message = message;
        this.number = number;
        this.status = status;
    }
}