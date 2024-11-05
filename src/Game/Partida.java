package Game;

import java.util.UUID;
import Bloques.Bloque;


public class Partida {
    private UUID id_partida; // ID de la partida en formato GUID
    private String ip; // Dirección IP
    private int puerto; // Puerto
    private Bloque[][] bloques; // Matriz de bloques
    private Ball[] bolas;

    public Partida(UUID id_partida, String ip, int puerto) {
        this.id_partida = id_partida;
        this.ip = ip;
        this.puerto = puerto;
        int filas = 8;
        int columnas = 8;

        this.bloques = new Bloque[filas][columnas]; // Inicializar matriz de bloques

        // Asignar los bloques con su estado de actividad inicial
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                this.bloques[i][j] = new Bloque(true); // Bloques inactivos inicialmente
            }
        }

        this.bolas = new Ball[10];
        for (int i = 0; i < 10; i++) {
            this.bolas[i] = new Ball(0,0,0,false);  // Inicializar cada objeto Ball
        }


    }

    // Método para desactivar un bloque específico
    public void desactivarBloque(int fila, int columna) {
        if (fila >= 0 && fila < bloques.length && columna >= 0 && columna < bloques[0].length) {
            bloques[fila][columna].setActivo(false); // Desactivar el bloque
            //System.out.println("Bloque en [" + fila + "][" + columna + "] desactivado.");
        } else {
            System.out.println("Posición fuera de los límites.");
        }
    }


    // Método para verificar si un bloque específico está activo
    public boolean isBloqueActivo(int fila, int columna) {
        if (fila >= 0 && fila < bloques.length && columna >= 0 && columna < bloques[0].length) {
            return bloques[fila][columna].isActivo(); // Retorna el estado del bloque
        } else {
            System.out.println("Posición fuera de los límites.");
            return false; // Retorna false si la posición está fuera de los límites
        }
    }

    public Bloque[][] getBloques() {
        return bloques;
    }

    public void setBloques(Bloque[][] bloques) {
        this.bloques = bloques;
    }

    public UUID getId_partida() {
        return id_partida;
    }

    public String getIp() {
        return ip;
    }

    public int getPuerto() {
        return puerto;
    }

    public Ball[] getBolas() {
        return bolas;
    }

    public void setBolas(Ball[] bolas) {
        this.bolas = bolas;
    }
}