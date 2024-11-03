package Fabrica_Bloques;

import Bloques.*;

public abstract class AbstractBloqueFactory {
    /**
     * Constructor de la clase
     * Método abstracto para crear un bloque.
     */
    public abstract AbstractBloque crearBloque(String tipoBloque,int fila, int columna);
}