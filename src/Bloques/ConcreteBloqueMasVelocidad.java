package Bloques;

public class ConcreteBloqueMasVelocidad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteBloqueMasVelocidad(int fila, int columna) {
        super(fila, columna);
    }

    // Aplica el efecto de aumento de velocidad
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de aumento de velocidad.");
    }
}
