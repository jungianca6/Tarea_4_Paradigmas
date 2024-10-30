#include <stdio.h>
#include <stdlib.h>
#include <raylib.h>
#include <string.h>
#include <time.h>
#include <math.h>

//Variables que contienen las dimensiones de la pantalla del juego.
const int screen_w = 500;
const int screen_h = 600;

typedef struct {
    Rectangle rect;
    float w;
    float h;
} Brick_factory;

typedef struct {
    Brick_factory base;
    Color color;
} Brick;

typedef struct {
    Brick *data;
    size_t size;
    size_t capacity;
} BrickArray;


//Struct con la bola y sus caracteristicas
struct Ball {
    Vector2 pos;
    Vector2 accel;
    float vel;
    float r;
};

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

typedef struct {
    float time;
    float duration;
    bool active;
} Timer;


int main(void) {

    InitWindow(screen_w, screen_h, "breakOutTec");

    SetTargetFPS(60);

    srand((unsigned int)time(NULL));


    while (!WindowShouldClose()) {
        
        BeginDrawing();
        ClearBackground(BLUE);


        EndDrawing();
    }



    CloseWindow();
    return 0;
}