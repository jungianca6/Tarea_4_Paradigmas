package Game;

import java.util.UUID;
import Bloques.Bloque;


public class Partida {
    private UUID id_partida; // ID de la partida en formato GUID
    private String ip; // Dirección IP
    private int puerto; // Puerto
    private Bloque[][] bloques; // Matriz de bloques



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
    }

    // Método para desactivar un bloque específico
    public void desactivarBloque(int fila, int columna) {
        if (fila >= 0 && fila < bloques.length && columna >= 0 && columna < bloques[0].length) {
            bloques[fila][columna].setActivo(false); // Desactivar el bloque
            System.out.println("Bloque en [" + fila + "][" + columna + "] desactivado.");
        } else {
            System.out.println("Posición fuera de los límites.");
        }
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
}