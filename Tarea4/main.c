#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "raylib.h"
#include "./Client/data.h"
#include "./Client/client.h"
#include "./Config/config.h"
#define MAX_INPUT_SIZE 256


PartyList partyList; // Lista global de partidas
int selectedPartyIndex = 0; // Índice de la partida seleccionada

int main(void) {
    // Cargar la configuración
    Config config;
    load_config("../config.ini", &config);

    const int screenWidth = 800;
    const int screenHeight = 450;

    InitWindow(screenWidth, screenHeight, "Raylib Client Example");
    SetTargetFPS(60);

    // Inicializa la lista de partidas
    partyList.parties = malloc(10 * sizeof(Partida)); // Inicializar con espacio para 10 partidas
    partyList.count = 0; // Inicializar la lista de partidas

    // Variables del socket
    int sock;
    struct sockaddr_in server_addr;

    // Inicializa el socket
    initialize_socket(&sock, &server_addr, config.port, config.ip_address);

    // Bandera para controlar el estado del registro
    int registered = 0;

    // Bucle principal
    while (!WindowShouldClose()) {
        // Configurar el conjunto de descriptores para select
        fd_set read_fds;
        FD_ZERO(&read_fds);
        FD_SET(sock, &read_fds);

        // Establecer timeout para select
        struct timeval timeout;
        timeout.tv_sec = 0;  // 0 segundos
        timeout.tv_usec = 10000;  // 10 milisegundos

        // Esperar actividad en el socket
        int activity = select(sock + 1, &read_fds, NULL, NULL, &timeout);

        // Verifica si hay datos para leer en el socket
        if (activity > 0 && FD_ISSET(sock, &read_fds)) {
            receive_message(sock); // Recibir respuesta
        }

        // Manejo de registro como "Player" o "Spectator"
        if (IsKeyPressed(KEY_P) && !registered) {
            // Enviar mensaje de registro como "Player"
            send_register_message(sock, "Player");
            printf("Registrado como Player\n");
            registered = 1; // Cambiar el estado a registrado
        } else if (IsKeyPressed(KEY_S) && !registered) {
            // Enviar mensaje de registro como "Spectator"
            send_register_message(sock, "Spectator");
            printf("Registrado como Spectator\n");
            registered = 1; // Cambiar el estado a registrado
        }

        // Manejo de selección de partidas
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

        // Limpiar la pantalla
        BeginDrawing();
        ClearBackground(RAYWHITE);
        DrawText("Presiona 'P' para registrarte como Player", 10, 10, 20, DARKGRAY);
        DrawText("Presiona 'S' para registrarte como Spectator", 10, 40, 20, DARKGRAY);

        // Dibujar la lista de partidas disponibles
        if (partyList.count > 0) {
            DrawText("Partidas disponibles:", 10, 80, 20, DARKGRAY);
            for (int i = 0; i < partyList.count; i++) {
                char text[128];
                snprintf(text, sizeof(text), "ID: %s, IP: %s, Puerto: %d",
                         partyList.parties[i].id_partida,
                         partyList.parties[i].ip,
                         partyList.parties[i].puerto);
                if (i == selectedPartyIndex) {
                    DrawText(text, 10, 110 + i * 30, 20, BLUE); // Resaltar la partida seleccionada
                } else {
                    DrawText(text, 10, 110 + i * 30, 20, DARKGRAY);
                }
            }
        } else {
            DrawText("No hay partidas disponibles.", 10, 80, 20, DARKGRAY);
        }

        EndDrawing();
    }

    // Cerrar el socket y liberar memoria
    close_socket(sock);
    free(partyList.parties);
    CloseWindow();

    return 0;
}