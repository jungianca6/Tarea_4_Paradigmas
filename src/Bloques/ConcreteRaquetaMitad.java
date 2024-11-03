package Bloques;

public class ConcreteRaquetaMitad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteRaquetaMitad(int fila, int columna) {
        super(fila, columna);
    }

    // Aplica el efecto de mitad de raqueta
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de mitad de raqueta.");
    }
}