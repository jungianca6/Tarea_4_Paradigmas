//
// Created by space-ba on 10/29/24.
//

#ifndef PARTIE_H
#define PARTIE_H

// Estructura para almacenar la información de una partida
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "choice")
    char id_partida[37]; // ID de la partida (UUID en formato string, 36 caracteres + terminador nulo)
    char ip[16];         // Dirección IP (máximo 15 caracteres + terminador nulo)
    int puerto;          // Puerto
} Partida;

// Estructura para la lista de partidas
typedef struct {
    Partida *parties;    // Puntero a un arreglo de partidas
    int count;           // Número de partidas en el arreglo
} PartyList;

#endif //PARTIE_H

