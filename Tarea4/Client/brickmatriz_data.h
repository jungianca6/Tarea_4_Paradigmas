//
// Created by darga19 on 04/11/24.
//

#ifndef BRICKMATRIZ_DATA_H
#define BRICKMATRIZ_DATA_H
// Estructura para el mensaje
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "brick_data")
    bool active;

} BricksData;

typedef struct {
    char type_message[20]; //Tipo de mensaje (ej. "brick_matriz")
    BricksData *bricks;

} DataBricksMatriz;
#endif //BRICKMATRIZ_DATA_H