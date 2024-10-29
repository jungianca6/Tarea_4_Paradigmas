//
// Created by space-ba on 10/29/24.
//
#include "partie.h"

#ifndef PARTIES_DATA_H
#define PARTIES_DATA_H


// Estructura para el mensaje de tipo data_parties
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "data")
    char message[256];   // Mensaje informativo
    Partida *parties;    // Puntero a un arreglo de partidas
    int num_parties;     // Número de partidas en el arreglo
} DataParties;

#endif //PARTIES_DATA_H
