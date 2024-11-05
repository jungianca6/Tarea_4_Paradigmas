#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "raylib.h"
#include "./Client/client.h"
#include "./Config/config.h"
#include <tgmath.h>
#include <time.h>
#include "Components/ball.h"
#include "Components/brick_array.h"
#include "Components/power_type.h"
#include "Components/brick_factory.h"
#include "Components/player.h"

#define MAX_INPUT_SIZE 256
//Variables que contienen las dimensiones de la pantalla del juego.
const int screen_w = 500;
const int screen_h = 600;
const float MAX_SPEED = 450.0f;  // Velocidad máxima
const float MIN_SPEED = 220.0f;
Texture2D background_text;
struct Player player;
struct Ball ball;
BrickArray bricks;
bool gg = false;
int menuActive = 0; // Controla si el menú está activo
// Variables del socket
int sock;
struct sockaddr_in server_addr;
char* tipo_jugador = "Spectator";
float PAUS_SPEED = 0.0f;


PartyList partyList; // Lista global de partidas
int selectedPartyIndex = 0; // Índice de la partida seleccionada
#define MAX_BALLS 10 // Define el número máximo de bolas
int activeBallsCount = 0; // Define el número máximo de bolas
int Pausa = 0; // Define el número máximo de bolas

struct Ball balls[MAX_BALLS];


void Spawn_bricks(BrickArray *brick_array) {
    Brick new_brick;
    new_brick.base.w = 59.0f;
    new_brick.base.h = 17.0f;
    brick_array->size = 0; // Reset size for respawning

    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            new_brick.base.rect.x = 5 + (i * 61);
            new_brick.base.rect.y = 50 + (j * 26);
            new_brick.base.rect.width = new_brick.base.w;
            new_brick.base.rect.height = new_brick.base.h;

            // Establece el color según la fila
            if (j < 2) {
                new_brick.color = RED;
                strcpy(new_brick.cond, "r");
            } else if (j < 4) {
                new_brick.color = ORANGE;
                strcpy(new_brick.cond, "o");
            } else if (j < 6) {
                new_brick.color = YELLOW;
                strcpy(new_brick.cond, "y");
            } else {
                new_brick.color = GREEN;
                strcpy(new_brick.cond, "g");
            }


            /*
            // Asigna poderes aleatorios: 25% cada uno para aumentar longitud, disminuir longitud, aumentar vidas o ninguno
            int randomPower = rand() % 19; // Ajusta la probabilidad total
            if (randomPower < 3) {
                new_brick.power = INCREASE_LENGTH;
            } else if (randomPower < 6) {
                new_brick.power = DECREASE_LENGTH;
            } else if (randomPower < 9) {
                new_brick.power = INCREASE_LIVES;
            } else if (randomPower < 12) {
                new_brick.power = INCREASE_SPEED;  // Nuevo poder para aumentar la velocidad
            } else if (randomPower < 15) {
                new_brick.power = DECREASE_SPEED;
                // Nuevo poder para disminuir la velocidad
            }
            else if (randomPower < 18) {
            new_brick.power = CREATE_EXTRA_BALL;
            // Nuevo poder para disminuir la velocidad
            }

            else {
                new_brick.power = NO_POWER;
            }
            */


            brick_array->data[brick_array->size++] = new_brick;
        }
    }
}

#include <stdio.h>
int GetScoreForCondition(const char* condition) {
    if (strcmp(condition, "r") == 0) {
        return 50; // Puntaje para el bloque rojo
    } else if (strcmp(condition, "o") == 0) {
        return 30; // Puntaje para el bloque naranja
    } else if (strcmp(condition, "y") == 0) {
        return 20; // Puntaje para el bloque amarillo
    } else if (strcmp(condition, "g") == 0) {
        return 10; // Puntaje para el bloque verde
    }
    return 0; // Puntaje por defecto
}
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

void PrintBall(const struct Ball *ball) {
    //printf("Posición de la bola: (%.2f, %.2f)\n", ball->pos.x, ball->pos.y);
    //printf("Aceleración de la bola: (%.2f, %.2f)\n", ball->accel.x, ball->accel.y);
    //printf("Velocidad de la bola: %.2f\n", ball->vel);
    //printf("Radio de la bola: %.2f\n", ball->r);
}

void DrawMenu() {
    // Dibuja el fondo del menú
    ClearBackground(BLACK);

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
        menuActive = 1; // Cambia a modo de juego
        tipo_jugador = "Player";
        // Enviar mensaje de registro como "Player"
        send_register_message(sock, "Player");
        printf("Registrado como Player\n");
    }

    // Detecta si el botón de "Observar" fue presionado
    if (observeHover && IsMouseButtonPressed(MOUSE_LEFT_BUTTON)) {
        menuActive = 2;
        tipo_jugador = "Spectator";
        // Acción para el modo de observación
        // Enviar mensaje de registro como "Spectator"
        send_register_message(sock, "Spectator");
        printf("Registrado como Spectator\n");

    }
}

void PrintBallState(const struct Ball *ball) {
    //printf("Bola %d - Posición: (%.2f, %.2f), Aceleración: (%.2f, %.2f), Velocidad: %.2f\n",
      //     ball->id, ball->pos.x, ball->pos.y, ball->accel.x, ball->accel.y, ball->vel);
    //printf("Posición del jugador: (%.2f, %.2f), Ancho: %.2f, Alto: %.2f\n", player.rect.x, player.rect.y, player.rect.width, player.rect.height);
}
void ActivateNewBall() {
    for (int i = 0; i < MAX_BALLS; i++) {
        if (!balls[i].active) { // Busca una bola inactiva
            balls[i].active = true; // Activa la nueva bola
            activeBallsCount ++; // Incrementa el contador de bolas activas            // Inicializa las propiedades de la nueva bola aquí
            break; // Salimos del bucle al activar la nueva bola
        }
    }
}


void Game_startup(BrickArray *brick_array) {

    //Codigo que se encarga de cargar el fondo del juego en la memoria para mas adelante proyectarlo.
    Image background_img = LoadImage("../assets/Space.png");
    background_text = LoadTextureFromImage(background_img);
    UnloadImage(background_img);

    //Codigo que carga a memoria datos del jugador
    player.rect = (Rectangle) {212.0f, 540.0f, 75.0f, 4.0f};
    player.velocity = 450.0f;
    player.score = 0;
    player.w = 75.0f;
    player.h = 10.0f;
    player.lives = 1000;
    player.level = 1;

// Configuración inicial de las bolas
    for (int i = 0; i < MAX_BALLS; i++) {
        balls[i].id = i;
        balls[i].accel = (Vector2) {0.0f, 1.0f};
        balls[i].pos = (Vector2) {250, 300};

        balls[i].r = 9.0f;
        balls[i].vel = 270.0f;
        balls[i].active = false; // Inicializa como inactiva
         }
    PrintBall(&ball);
    //Codigo que carga la lista de bloques
    brick_array->size = 0;
    brick_array->capacity = 64; // Initial capacity (adjust as needed)
    brick_array->data = (Brick *)malloc(brick_array->capacity * sizeof(Brick));

    Spawn_bricks(brick_array);
    Spawn_bricks(brick_array);

    // Imprimir bloques al iniciar el juego
    PrintBricks(brick_array);
}
void Mas_velocidad() {
    for (int j = 0; j < MAX_BALLS; j++) {

        balls[j].vel *= 1.2f;
        if (balls[j].vel > MAX_SPEED) {
            balls[j].vel = MAX_SPEED;
        }
    }
}
void Menos_velocidad() {
    for (int j = 0; j < MAX_BALLS; j++) {

        balls[j].vel *= 0.8f;
        if (balls[j].vel < MIN_SPEED) {
            balls[j].vel = MIN_SPEED;
        }
    }
}

void Game_update() {
    if (activeBallsCount == 0)
    {
        // Establece el color del texto (puedes elegir otro color)
        DrawText("PRESIONE ESPACIO", GetScreenWidth() / 2 - MeasureText("PRESIONE ESPACIO", 40) / 2, GetScreenHeight() / 2 - 20, 40, RED);
    }
    float framet = GetFrameTime();
    static int printCounter = 0;  // Contador para controlar la impresión
    if (gg) return;

    // Control del jugador sobre la barra de juego.
    if(IsKeyDown(KEY_LEFT) || IsKeyDown(KEY_A)) {
        if (Pausa == 0) {
            player.rect.x -= player.velocity * framet;
        }
    }
    if(IsKeyDown(KEY_RIGHT) || IsKeyDown(KEY_D) ) {
        if (Pausa == 0) {
            player.rect.x += player.velocity * framet;
        }
    }

    if(IsKeyPressed(KEY_Q) || IsKeyPressed(KEY_P)) {
        if ( Pausa == 0) {
            Pausa = 1; // Incrementa el contador de bolas activas            // Inicializa las propiedades de la nueva bola aquí
            for (int i = 0; i < MAX_BALLS; i++) {
                PAUS_SPEED = balls[i].vel;
                balls[i].vel = 0.0f;

            }

        }
        else{
            Pausa = 0;

            for (int i = 0; i < MAX_BALLS; i++) {
                balls[i].vel = PAUS_SPEED;

            }


        }
    }

    if(IsKeyDown(KEY_SPACE)) {
        if (activeBallsCount < 1) {
            balls[0].active = true;
            activeBallsCount ++;
        }


    }
    // Actualización de la posición de la bola
    for (int i = 0; i < MAX_BALLS; i++) {
        if (balls[i].active) {  // Solo actualiza la bola activa
            balls[i].pos.x += ((balls[i].vel * balls[i].accel.x) * framet);
            balls[i].pos.y += ((balls[i].vel * balls[i].accel.y) * framet);
        }
    }


    // Solo imprime cada 30 fotogramas
 // Colisión entre la bola y los bloques
// Colisión entre la bola y los bloques
    for (int j = 0; j < MAX_BALLS; j++) {
        if (balls[j].active) {  // Solo actualiza la bola activa
            for (int i = 0; i < bricks.size; i++) {
                Brick brick = bricks.data[i];
                if (CheckCollisionCircleRec(balls[j].pos, balls[j].r,
                                            brick.base.rect)) {                // Verifica de qué lado ocurrió la colisión
                    if (balls[j].pos.x < brick.base.rect.x ||
                        balls[j].pos.x > brick.base.rect.x + brick.base.rect.width) {
                        balls[j].accel.x *= -1;
                    } else {
                        balls[j].accel.y *= -1;
                    }

                    player.score += GetScoreForCondition(brick.cond);

                    // Verifica si el bloque tiene un poder y actúa según el poder
  // Velocidad mínima

                    if (brick.power == INCREASE_LENGTH) {
                        player.w *= 2;
                        if (player.w > 150)
                            player.w = 150;
                        player.rect.width = player.w;
                    }
                     else if (brick.power == CREATE_EXTRA_BALL) {
                        ActivateNewBall();
                    }else if (brick.power == DECREASE_LENGTH) {
                        player.w *= 0.5;
                        if (player.w < 37.5)
                            player.w = 37.5;
                        player.rect.width = player.w;
                    } else if (brick.power == INCREASE_LIVES) {
                        player.lives++;
                    } else if (brick.power == INCREASE_SPEED) {
                        Mas_velocidad();

                    } else if (brick.power == DECREASE_SPEED) {
                        Menos_velocidad();

                    }

                    // Imprime mensaje de destrucción del bloque
                    int column = (brick.base.rect.x - 5) / 61;
                    int row = (brick.base.rect.y - 50) / 26;     // Calcula la fila
                    printf("El bloque se destruyó en la fila %d, columna %d\n", row, column); // Imprime fila y columna
                    send_bricks_info(sock, row, column, "normal");
                    // Eliminar el bloque
                    for (int j = i; j < bricks.size - 1; j++) {
                        bricks.data[j] = bricks.data[j + 1];
                    }

                    bricks.size--;
                    i--; // Decrementa 'i' para verificar el siguiente bloque en la próxima iteración
                    break;
                }
            }
        }
    }

    //Chequeo de si todos los bloues estan destruidos, si ese es el caso, se aumenta el nivel, se reestablecen los bloques y se aumenta la velocidad de la bola.
    if (bricks.size == 0) {
        player.level++;
        for (int i = 0; i < MAX_BALLS; i++) {

            balls[i].vel *= 1.0f;
            balls[i].accel = (Vector2) {0.0f, 1.0f};
            balls[i].pos = (Vector2) {250, 300};
            balls[i].active = false; // Inicializa como inactiva
            activeBallsCount = 0;

        }
        Spawn_bricks(&bricks);
    }

    //Colision entre la bola y las paredes, se invierte la aceleracion pues el choque causa cambio a direccion contraria.
    for (int j = 0; j < MAX_BALLS; j++) {
        if (balls[j].active) {  // Solo actualiza la bola activa

            // Colisión con paredes
            if (balls[j].pos.x > screen_w || balls[j].pos.x < 10) {
                balls[j].accel.x *= -1;
            }
            if (balls[j].pos.y < 10) {
                balls[j].accel.y *= -1;
            }
        }
    }
    for (int j = 0; j < MAX_BALLS; j++) {

        //Chequeo de si la bola se va de la pantalla abajo para posteriormente volver a jugar pero con una vida menos.
        if (balls[j].pos.y > screen_h) {
            if (balls[j].active) {  // Solo actualiza la bola activa
                if (activeBallsCount > 1) { // Permitir hasta 2 bolas activas
                    balls[j].active = false;
                    activeBallsCount--; // Incrementa el contador de bolas activas
                    balls[j].accel = (Vector2) {0.0f, 1.0f};
                    balls[j].pos = (Vector2) {250, 300};
                    balls[j].vel = 270.0f;

                }// Activa la nueva bola
                player.lives--;
                balls[j].pos = (Vector2) {250, 300};
                balls[j].accel = (Vector2) {0.0f, 1.0f};
                if (player.lives <= 0) {
                    gg = true;
                }
                return;
            }
        }
    }
    for (int j = 0; j < MAX_BALLS; j++) {
        if (balls[j].active) {  // Solo actualiza la bola activa

            // Colisión entre la bola y el jugador.
            if (CheckCollisionCircleRec(balls[j].pos, balls[j].r, player.rect)) {
                // Calcula la posición relativa de la bola con respecto al centro de la raqueta.
                float relativePosition =
                        (balls[j].pos.x - (player.rect.x + player.rect.width / 2)) / (player.rect.width / 2);

                // Ajusta el ángulo de rebote en el eje X basándose en la posición relativa.
                balls[j].accel.x = relativePosition;  // Cuanto más lejos del centro, mayor el ángulo en X.
                balls[j].accel.y = -fabsf(
                        balls[j].accel.y);  // Invierte la dirección en Y y asegura que siempre vaya hacia arriba.

                // Normaliza el vector de aceleración para mantener la velocidad constante.
                float magnitude = sqrtf(balls[j].accel.x * balls[j].accel.x + balls[j].accel.y * balls[j].accel.y);
                balls[j].accel.x /= magnitude;
                balls[j].accel.y /= magnitude;

            }
        }
    }

    //Colision entre el jugador y las paredes
    if (player.rect.x < 0) {
        player.rect.x = 0;
    }
    if (player.rect.x > (screen_w - player.rect.width)) {
        player.rect.x = (screen_w - player.rect.width);
    }

    printCounter++;
    // Imprime cada 30 fotogramas
    if (printCounter >= 500) {
        send_balls_info(sock);
        send_player_info(sock, player.rect.x,  player.rect.y,  player.rect.width, player.rect.height);
        printf("Estado de las bolas activas:\n");
        for (int i = 0; i < MAX_BALLS; i++) {
            if (balls[i].active) {
                printf("Bola %d - Posición: (%.2f, %.2f), Velocidad: %.2f\n",
                       balls[i].id, balls[i].pos.x, balls[i].pos.y, balls[i].vel);
            }
        }
        printCounter = 0;  // Reinicia el contador de fotogramas
    }

    if (Pausa == 1)
    {
        // Establece el color del texto (puedes elegir otro color)
        DrawText("PAUSA", GetScreenWidth() / 2 - MeasureText("PAUSA", 40) / 2, GetScreenHeight() / 2 - 20, 40, DARKGRAY);
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
    for (int i = 0; i < MAX_BALLS; i++) {
        if (balls[i].active) {  // Solo actualiza la bola activa

            DrawCircle(balls[i].pos.x, balls[i].pos.y, balls[i].r, RAYWHITE);
        }
    }
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

void DrawParties() {
    if (partyList.count > 0) {
        DrawText("Partidas disponibles:", 10, 160, 20, DARKGRAY);
        for (int i = 0; i < partyList.count; i++) {
            char text[128];
            snprintf(text, sizeof(text), "ID: %s, IP: %s, Puerto: %d",
                     partyList.parties[i].id_partida,
                     partyList.parties[i].ip,
                     partyList.parties[i].puerto);
            if (i == selectedPartyIndex) {
                DrawText(text, 10, 190 + i * 30, 20, WHITE); // Resaltar la partida seleccionada
            } else {
                DrawText(text, 10, 190 + i * 30, 20, DARKGRAY);
            }
        }
    } else {
        DrawText("No hay partidas disponibles.", 10, 160, 20, DARKGRAY);
    }
}


int main(void) {
    // Cargar la configuración
    Config config;
    load_config("../config.ini", &config);

    // Inicializa la lista de partidas
    partyList.parties = malloc(10 * sizeof(Partida)); // Inicializar con espacio para 10 partidas
    partyList.count = 0; // Inicializar la lista de partidas

    // Inicializa el socket
    initialize_socket(&sock, &server_addr, config.port, config.ip_address);

    InitWindow(screen_w, screen_h, "breakOutTec");

    SetTargetFPS(1000);
    srand((unsigned int)time(NULL));

    Game_startup(&bricks);

    while (!WindowShouldClose()) {
        BeginDrawing();


        // Configurar el conjunto de descriptores para select
        fd_set read_fds;
        FD_ZERO(&read_fds);
        FD_SET(sock, &read_fds);

        // Establecer timeout para select
        struct timeval timeout;
        timeout.tv_sec = 0;  // 0 segundos
        timeout.tv_usec = 1000;  // 1 milisegundos

        // Esperar actividad en el socket
        int activity = select(sock + 1, &read_fds, NULL, NULL, &timeout);

        // Verifica si hay datos para leer en el socket
        if (activity > 0 && FD_ISSET(sock, &read_fds)) {
            receive_message(sock); // Recibir respuesta
        }

        if (menuActive == 0) {
            DrawMenu(); // Dibuja el menú si está activo
        } else if (menuActive == 1){
            ClearBackground(BLACK);
            Game_update();  // Actualiza el estado del juego
            Game_render();  // Dibuja el juego
        } else if (menuActive == 2) {
            ClearBackground(BLACK);
            DrawText("Presione 'A' para actualizar las partidas disponibles", 10, 50, 20, DARKGRAY);
            // Manejar entrada del teclado para registrar espectador
            if (IsKeyPressed(KEY_A)) {
                send_register_message(sock, "Spectator");
            }
            // Dibuja la lista de partidas disponibles
            if (partyList.count > 0) {
                if (IsKeyPressed(KEY_DOWN)) {
                    selectedPartyIndex = (selectedPartyIndex + 1) % partyList.count; // Mover hacia abajo en la lista
                }
                if (IsKeyPressed(KEY_UP)) {
                    selectedPartyIndex = (selectedPartyIndex - 1 + partyList.count) % partyList.count; // Mover hacia arriba en la lista
                }
                if (IsKeyPressed(KEY_ENTER)) {
                    // Enviar mensaje de elección de partida al servidor
                    send_choice_message(sock, partyList.parties[selectedPartyIndex].id_partida,
                                         partyList.parties[selectedPartyIndex].ip,
                                         partyList.parties[selectedPartyIndex].puerto);
                    printf("Seleccionada la partida: %s\n", partyList.parties[selectedPartyIndex].id_partida);
                }
            }

            DrawParties();
            } else {
                DrawText("No hay partidas disponibles.", 10, 80, 20, DARKGRAY);
            }
        EndDrawing();
    }

    Game_shutdown();

    CloseWindow();
    return 0;
}
