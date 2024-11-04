//
// Created by space-ba on 11/2/24.
//

#ifndef BALL_H
#define BALL_H

//Struct con la bola y sus caracteristicas
struct Ball {
    int id; // Identificador de la bola
    Vector2 pos;
    Vector2 accel;
    float vel;
    float r;
};

#endif //BALL_H
