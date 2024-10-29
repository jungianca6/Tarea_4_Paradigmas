package Bloques;

public class ConcreteRaquetaMitad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteRaquetaMitad(String color, int puntaje, int nivel) {
        super(color, puntaje, nivel);
    }

    // Aplica el efecto de mitad de raqueta
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de mitad de raqueta.");
    }
}