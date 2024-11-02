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

typedef enum {
    NO_POWER,
    INCREASE_LENGTH,
    DECREASE_LENGTH,
    INCREASE_LIVES,  // Poder para aumentar vidas
    INCREASE_SPEED,  // Nuevo poder para aumentar la velocidad de la bola
    DECREASE_SPEED   // Nuevo poder para disminuir la velocidad de la bola
} PowerType;

typedef struct {
    Brick_factory base;
    Color color;
    PowerType power; // Cambiamos hasPower por power
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

Texture2D background_text;
struct Player player;
struct Ball ball;
BrickArray bricks;
bool gg = false;
bool menuActive = true; // Controla si el menú está activo

void Spawn_bricks(BrickArray *brick_array) {
    Brick new_brick;
    new_brick.base.w = 54.0f;
    new_brick.base.h = 17.0f;
    brick_array->size = 0; // Reset size for respawning

    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            new_brick.base.rect.x = 15 + (i * 60);
            new_brick.base.rect.y = 50 + (j * 26);
            new_brick.base.rect.width = new_brick.base.w;
            new_brick.base.rect.height = new_brick.base.h;

            // Establece el color según la fila
            if (j < 2) {
                new_brick.color = RED;
            } else if (j < 4) {
                new_brick.color = ORANGE;
            } else if (j < 6) {
                new_brick.color = YELLOW;
            } else {
                new_brick.color = GREEN;
            }

            // Asigna poderes aleatorios: 25% cada uno para aumentar longitud, disminuir longitud, aumentar vidas o ninguno
            int randomPower = rand() % 16; // Ajusta la probabilidad total
            if (randomPower < 3) {
                new_brick.power = INCREASE_LENGTH;
            } else if (randomPower < 6) {
                new_brick.power = DECREASE_LENGTH;
            } else if (randomPower < 9) {
                new_brick.power = INCREASE_LIVES;
            } else if (randomPower < 12) {
                new_brick.power = INCREASE_SPEED;  // Nuevo poder para aumentar la velocidad
            } else if (randomPower < 15) {
                new_brick.power = DECREASE_SPEED;  // Nuevo poder para disminuir la velocidad
            } else {
                new_brick.power = NO_POWER;
            }


            brick_array->data[brick_array->size++] = new_brick;
        }
    }
}

#include <stdio.h>

void PrintBricks(const BrickArray *brick_array) {
    for (int i = 0; i < brick_array->size; i++) {
        Brick brick = brick_array->data[i];
        printf("Brick %d: Color: %d, Position: (%f, %f), Size: (%f, %f), Power Type: %s\n",
               i,
               brick.color, // Suponiendo que 'color' es un entero o enum
               brick.base.rect.x,
               brick.base.rect.y,
               brick.base.rect.width,
               brick.base.rect.height,
               (brick.power == NO_POWER) ? "None" :
               (brick.power == INCREASE_LENGTH) ? "Increase Length" :
               (brick.power == DECREASE_LENGTH) ? "Decrease Length" :
               (brick.power == INCREASE_LIVES) ? "Increase Lives" : "Unknown"); // Agregado el poder de aumentar vidas
    }
}



void DrawMenu() {
    // Dibuja el fondo del menú
    ClearBackground(DARKBLUE);

    // Dibuja el título del juego
    DrawText("BREAKOUT", screen_w / 2 - MeasureText("BREAKOUT", 40) / 2, screen_h / 2 - 100, 40, RAYWHITE);

    // Dibuja el botón de "Play"
    Rectangle playButton = { screen_w / 2 - 50, screen_h / 2, 100, 50 };
    bool playHover = CheckCollisionPointRec(GetMousePosition(), playButton);
    DrawRectangleRec(playButton, playHover ? LIGHTGRAY : GRAY);
    DrawText("Play", playButton.x + 20, playButton.y + 10, 30, BLACK);

    // Dibuja el botón de "Observar"
    Rectangle observeButton = { screen_w / 2 - 50, screen_h / 2 + 100, 100, 50 };
    bool observeHover = CheckCollisionPointRec(GetMousePosition(), observeButton);
    DrawRectangleRec(observeButton, observeHover ? LIGHTGRAY : GRAY);
    DrawText("Spectate", observeButton.x + 10, observeButton.y + 10, 30, BLACK);

    // Detecta si el botón de "Play" fue presionado
    if (playHover && IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) {
        menuActive = false; // Cambia a modo de juego
    }

    // Detecta si el botón de "Observar" fue presionado
    if (observeHover && IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) {
        // Acción para el modo de observación
    }
}


void Game_startup(BrickArray *brick_array) {

    //Codigo que se encarga de cargar el fondo del juego en la memoria para mas adelante proyectarlo.
    Image background_img = LoadImage("../assets/Space.png");
    background_text = LoadTextureFromImage(background_img);
    UnloadImage(background_img);

    //Codigo que carga a memoria datos del jugador
    player.rect = (Rectangle) {250.0f, 540.0f, 75.0f, 4.0f};
    player.velocity = 450.0f;
    player.score = 0;
    player.w = 75.0f;
    player.h = 10.0f;
    player.lives = 3;
    player.level = 1;

    //Codigo que carga a memoria datos de la bola
    ball.accel = (Vector2) {1.0f, -1.0f};
    ball.r = 9.0f;
    ball.pos = (Vector2) {350, 500};
    ball.vel = 300.0f;

    //Codigo que carga la lista de bloques
    brick_array->size = 0;
    brick_array->capacity = 64; // Initial capacity (adjust as needed)
    brick_array->data = (Brick *)malloc(brick_array->capacity * sizeof(Brick));

    Spawn_bricks(brick_array);
    Spawn_bricks(brick_array);

    // Imprimir bloques al iniciar el juego
    PrintBricks(brick_array);
}

void Game_update() {
    float framet = GetFrameTime();

    if (gg) return;

    // Control del jugador sobre la barra de juego.
    if(IsKeyDown(KEY_LEFT)) {
        player.rect.x -= player.velocity * framet;
    }
    if(IsKeyDown(KEY_RIGHT)) {
        player.rect.x += player.velocity * framet;
    }

    // Actualización de la posición de la bola
    ball.pos.x = ball.pos.x + ((ball.vel * ball.accel.x) * framet);
    ball.pos.y = ball.pos.y + ((ball.vel * ball.accel.y) * framet);

    // Colisión entre la bola y los bloques
// Colisión entre la bola y los bloques
    for (int i = 0; i < bricks.size; i++) {
        Brick brick = bricks.data[i];
        if (CheckCollisionCircleRec(ball.pos, ball.r, brick.base.rect)) {
            // Verifica de qué lado ocurrió la colisión
            if (ball.pos.x < brick.base.rect.x || ball.pos.x > brick.base.rect.x + brick.base.rect.width) {
                ball.accel.x = ball.accel.x * -1;
            } else {
                ball.accel.y = ball.accel.y * -1;
            }

            player.score += 10; // Aumenta el puntaje

            // Verifica si el bloque tiene un poder y actúa según el poder
            const float MAX_SPEED = 450.0f;  // Velocidad máxima
            const float MIN_SPEED = 150.0f;   // Velocidad mínima

            if (brick.power == INCREASE_LENGTH) {
                player.w *= 2;
                if (player.w > 150)
                    player.w = 150;
                player.rect.width = player.w;
            } else if (brick.power == DECREASE_LENGTH) {
                player.w *= 0.5 ;
                if (player.w < 37.5)
                    player.w = 37.5;
                player.rect.width = player.w;
            } else if (brick.power == INCREASE_LIVES) {
                player.lives++;
            } else if (brick.power == INCREASE_SPEED) {
                ball.vel *= 1.2f;
                if (ball.vel > MAX_SPEED) {
                    ball.vel = MAX_SPEED;
                }
            } else if (brick.power == DECREASE_SPEED) {
                ball.vel *= 0.8f;
                if (ball.vel < MIN_SPEED) {
                    ball.vel = MIN_SPEED;
                }
            }
            printf("Velocidad de la bola: %.2f\n", ball.vel);  // C para C++

            // Imprime mensaje de destrucción del bloque
            printf("El bloque se destruyó en la posición: (x: %.2f, y: %.2f)\n", brick.base.rect.x, brick.base.rect.y);
            // Eliminar el bloque
            for (int j = i; j < bricks.size - 1; j++) {
                bricks.data[j] = bricks.data[j + 1];
            }

            bricks.size--;
            i--; // Decrementa 'i' para verificar el siguiente bloque en la próxima iteración
            break;
        }
    }




    //Chequeo de si todos los bloues estan destruidos, si ese es el caso, se aumenta el nivel, se reestablecen los bloques y se aumenta la velocidad de la bola.
    if (bricks.size == 0) {
        player.level++;
        ball.vel *= 1.2f;
        ball.accel = (Vector2) {1.0f, -1.0f};
        ball.pos = (Vector2) {350, 500};
        Spawn_bricks(&bricks);
    }

    //Colision entre la bola y las paredes, se invierte la aceleracion pues el choque causa cambio a direccion contraria.
    if (ball.pos.x > screen_w || ball.pos.x < 10) {
        ball.accel.x = ball.accel.x * -1;
    }
    if (ball.pos.y < 10) {
        ball.accel.y = ball.accel.y * -1;
    }

    //Chequeo de si la bola se va de la pantalla abajo para posteriormente volver a jugar pero con una vida menos.
    if (ball.pos.y > screen_h) {
        player.lives--;
        ball.pos = (Vector2){350, 500};
        ball.accel = (Vector2){1.0f, -1.0f};
        if (player.lives <= 0) {
            gg = true;
        }
        return;
    }


    //Colision entre la bola y el jugador.
// Colisión entre la bola y el jugador.
    if (CheckCollisionCircleRec(ball.pos, ball.r, player.rect)) {
        // Calcula la posición relativa de la bola con respecto al centro de la raqueta.
        float relativePosition = (ball.pos.x - (player.rect.x + player.rect.width / 2)) / (player.rect.width / 2);

        // Ajusta el ángulo de rebote en el eje X basándose en la posición relativa.
        ball.accel.x = relativePosition;  // Cuanto más lejos del centro, mayor el ángulo en X.
        ball.accel.y = -fabs(ball.accel.y);  // Invierte la dirección en Y y asegura que siempre vaya hacia arriba.

        // Normaliza el vector de aceleración para mantener la velocidad constante.
        float magnitude = sqrt(ball.accel.x * ball.accel.x + ball.accel.y * ball.accel.y);
        ball.accel.x /= magnitude;
        ball.accel.y /= magnitude;
    }


    //Colision entre el jugador y las paredes
    if (player.rect.x < 0) {
        player.rect.x = 0;
    }
    if (player.rect.x > (screen_w - player.rect.width)) {
        player.rect.x = (screen_w - player.rect.width);
    }


}

void Game_render() {

    //Codigo que renderiza el fondo, se expande la imagen original para que se vea en toda la pantalla.
    Rectangle source = {0,0,background_text.width, background_text.height};
    Rectangle dest = {0,0,600,600};
    Vector2 origin = {0,0};

    DrawTexturePro(background_text, source, dest, origin, 0.0f, RAYWHITE);

    //Codigo que renderiza los bloques del Breakout.
    for (size_t i = 0; i < bricks.size; i++) {
        Brick brick = bricks.data[i];
        DrawRectangle(
                brick.base.rect.x,
                brick.base.rect.y,
                brick.base.rect.width,
                brick.base.rect.height,
                brick.color
        );
    }

    //Codigo que renderiza a la bola
    DrawCircle(ball.pos.x, ball.pos.y, ball.r, RAYWHITE);

    //Codigo que renderiza al jugador
    DrawRectangle(player.rect.x, player.rect.y, player.rect.width, player.rect.height, WHITE);

    //Codigo que renderiza el puntaje del jugador como si fuese un string en la pantalla.
    char score_txt[70] = "PUNTAJE: ";

    char score[60];
    sprintf(score, "%d", player.score);

    strcat(score_txt, score);
    DrawText(score_txt, 10, 10, 15, RAYWHITE);

    //Codigo que renderiza el puntaje del jugador como si fuese un string en la pantalla.
    char lives_txt[70] = "VIDAS: ";

    char lives[60];
    sprintf(lives, "%d", player.lives);

    strcat(lives_txt, lives);
    DrawText(lives_txt, screen_w-75, 10, 15, RAYWHITE);

    //Codigo que renderiza el puntaje del jugador como si fuese un string en la pantalla.
    char level_txt[70] = "NIVEL: ";

    char level[60];
    sprintf(level, "%d", player.level);

    strcat(level_txt, level);
    DrawText(level_txt, 230, 10, 15, RAYWHITE);

    if (gg) {
        DrawText("HAS PERDIDO: TE QUEDASTE SIN VIDAS", screen_w / 2 - 200, screen_h / 2 - 10, 20, RED);
        DrawText("PRESIONA ESC PARA SALIR", screen_w / 2 - MeasureText("Press R to restart or Q to quit", 15) / 2, screen_h / 2 + 40, 15, RAYWHITE);
        if (IsKeyPressed(KEY_ESCAPE)) {
            CloseWindow(); // Exit the game
        }
    }

}

void Game_shutdown() {

    free(bricks.data);

}

int main(void) {
    InitWindow(screen_w, screen_h, "breakOutTec");

    SetTargetFPS(60);
    srand((unsigned int)time(NULL));

    Game_startup(&bricks);

    while (!WindowShouldClose()) {
        BeginDrawing();

        if (menuActive) {
            DrawMenu(); // Dibuja el menú si está activo
        } else {
            ClearBackground(BLUE);
            Game_update();  // Actualiza el estado del juego
            Game_render();  // Dibuja el juego
        }

        EndDrawing();
    }

    Game_shutdown();

    CloseWindow();
    return 0;
}