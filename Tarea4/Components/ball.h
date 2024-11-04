//
// Created by space-ba on 11/2/24.
//

#ifndef BALL_H
#define BALL_H

//Struct con la bola y sus caracteristicas
typedef struct Ball {
    int id;
    Vector2 pos;
    Vector2 accel;
    float vel;
    float r;
    bool active;  // Indica si la bola está activa
} Ball;

#endif //BALL_H
