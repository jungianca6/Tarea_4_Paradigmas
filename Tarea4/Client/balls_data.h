//
// Created by darga19 on 04/11/24.
//

#ifndef BALLS_DATA_H
#define BALLS_DATA_H

// Estructura para el mensaje
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "ball_data")
    bool active; //True o False
    int id;
    float posx;
    float posy;

} DataBall;

typedef struct {
    char type_message[20]; //Tipo de mensaje (ej. "balls_data")
    DataBall *balls;
} DataBalls;
#endif //BALLS_DATA_H