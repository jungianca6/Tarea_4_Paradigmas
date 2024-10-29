package Bloques;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBloque{
    // Color del bloque
    protected String color;
    // Puntuación del bloque
    protected int puntaje;
    // Nivel del bloque
    protected int nivel;

    /** Constructor de la clase */
    public AbstractBloque(String color, int puntaje, int nivel) {
        this.color = color;
        this.puntaje = puntaje;
        this.nivel = nivel;
    }

}