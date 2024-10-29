package Fabrica_Bloques;

import Bloques.*;

public class ConcreteBloqueFactory extends AbstractBloqueFactory {
    /** Constructor de la clase */
    @Override
    public AbstractBloque crearBloque(String tipoBloque, String color, int puntaje, int nivel) {
        switch (tipoBloque.toLowerCase()) {
            // Crea un bloque normal
            case "normal":
                return new ConcreteBloqueNormal(color, puntaje, nivel);
            // Crea un bloque de mas velocidad
            case "masvelocidad":
                return new ConcreteBloqueMasVelocidad(color, puntaje, nivel);
            // Crea un bloque de menos velocidad
            case "menosvelocidad":
                return new ConcreteBloqueMenosVelocidad(color, puntaje, nivel);
            default:
                throw new IllegalArgumentException("Tipo de bloque desconocido: " + tipoBloque);
        }
    }
}