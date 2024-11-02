//
// Created by space-ba on 11/2/24.
//
#include "brick_factory.h"
#include "power_type.h"

#ifndef BRICK_H
#define BRICK_H

typedef struct {
    Brick_factory base;
    Color color;
    PowerType power; // Cambiamos hasPower por power
} Brick;


#endif //BRICK_H
