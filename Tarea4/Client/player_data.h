//
// Created by darga19 on 03/11/24.
//

#ifndef PLAYER_DATA_H
#define PLAYER_DATA_H

// Estructura para el mensaje
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "player_data")
    int posx;
    int posy;
    float ancho;
    float alto;

} DataPlayer;

#endif //PLAYER_DATA_H