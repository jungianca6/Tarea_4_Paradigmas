package Server;

class Data {
    String message;
    int number;
    int status;

    public Data(String message, int number, int status) {
        this.message = message;
        this.number = number;
        this.status = status;
    }

    // Constructor vacío
    public Data() {
        this.message = ""; // Inicializa message como una cadena vacía
        this.number = 0;   // Inicializa number en 0
        this.status = 0; // Inicializa status como false
    }
}

