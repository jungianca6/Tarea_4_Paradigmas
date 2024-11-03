package Bloques;

public class ConcreteBloqueMenosVelocidad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteBloqueMenosVelocidad(int fila, int columna) {
        super(fila, columna);
    }

    // Aplica el efecto de disminución de velocidad
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de disminución de velocidad.");
    }
}