package Bloques;

public class ConcreteBloqueMenosVelocidad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteBloqueMenosVelocidad(String color, int puntaje, int nivel) {
        super(color, puntaje, nivel);
    }

    // Aplica el efecto de disminución de velocidad
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de disminución de velocidad.");
    }
}