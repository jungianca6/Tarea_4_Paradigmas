//
// Created by darga19 on 03/11/24.
//

#ifndef BRICKS_DATA_H
#define BRICKS_DATA_H

// Estructura para el mensaje

typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "bricks_data")
    int column;
    int row;
    char poder[20];

} DataBricks;

#endif //BRICKS_DATA_H