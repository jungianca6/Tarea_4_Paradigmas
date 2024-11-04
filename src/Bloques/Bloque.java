package Bloques;

public class Bloque {
    // Estado del bloque: activo o inactivo
    protected boolean activo;

    /** Constructor de la clase */
    public Bloque(boolean activo) {
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}