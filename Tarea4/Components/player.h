//
// Created by space-ba on 11/2/24.
//

#ifndef PLAYER_H
#define PLAYER_H

//Struct con el jugador y sus caracteristicas
struct Player {
    Rectangle rect;
    float velocity;
    int score;
    int lives;
    int level;
    float w;
    float h;
};

#endif //PLAYER_H
