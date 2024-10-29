package Bloques;

public class ConcreteBloqueMasVelocidad extends AbstractBloque {

    /** Constructor de la clase */
    public ConcreteBloqueMasVelocidad(String color, int puntaje, int nivel) {
        super(color, puntaje, nivel);
    }

    // Aplica el efecto de aumento de velocidad
    public void aplicarEfecto() {
        System.out.println("Aplicando efecto de aumento de velocidad.");
    }
}
