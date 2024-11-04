package Fabrica_Bloques;

import Bloques.*;

public class ConcreteBloqueFactory extends AbstractBloqueFactory {
    /** Constructor de la clase */
    @Override
    public AbstractBloque crearBloque(String tipoBloque,int fila, int columna) {
        switch (tipoBloque.toLowerCase()) {
            // Crea un bloque normal
            case "normal":
                return new ConcreteBloqueNormal(fila, columna);
            // Crea un bloque de mas velocidad
            case "masvelocidad":
                return new ConcreteBloqueMasVelocidad(fila, columna);
            // Crea un bloque de menos velocidad
            case "menosvelocidad":
                return new ConcreteBloqueMenosVelocidad(fila, columna);
            case "raquetadoble":
                return new ConcreteRaquetaDoble(fila, columna);
            case "raquetamitad":
                return new ConcreteRaquetaMitad(fila, columna);
            case "masbolas":
                return new ConcreteBloqueMasBolas(fila, columna);
            case "masvidas":
                return new ConcreteBloqueMasVidas(fila, columna);
            default:
                throw new IllegalArgumentException("Tipo de bloque desconocido: " + tipoBloque);
        }
    }
}