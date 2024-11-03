package Bloques;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBloque{
    // Fila del bloque
    protected int fila;
    // Columna del bloque
    protected int columna;


    /** Constructor de la clase */
    public AbstractBloque(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

}