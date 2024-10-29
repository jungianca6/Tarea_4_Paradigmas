#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "raylib.h"
#include "./Client/data.h"
#include "./Client/client.h"
#include "./Config/config.h"

#define MAX_INPUT_SIZE 256

int main(void)
{
    // Cargar la configuración
    Config config;
    if (!load_config("../config.ini", &config)) {
        fprintf(stderr, "Failed to load config\n");
        return EXIT_FAILURE;
    }

    // Imprimir el puerto cargado
    printf("Puerto cargado desde config: %d\n", config.port);
    printf("Puerto cargado desde config: %d\n", config.max_input_size);

    const int screenWidth = 800;
    const int screenHeight = 450;

    InitWindow(screenWidth, screenHeight, "Raylib Client Example");
    SetTargetFPS(60);

    // Variables del socket
    int sock;
    struct sockaddr_in server_addr;
    Data data;
    char input[MAX_INPUT_SIZE] = "Hello from the client!"; // Mensaje por defecto

    // Inicializa el socket
    initialize_socket(&sock, &server_addr, config.port, config.ip_address);

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

        // Verifica si el usuario presiona ENTER para enviar el mensaje
        if (IsKeyPressed(KEY_ENTER)) {
            strcpy(data.message, input); // Copiar el mensaje de entrada
            data.number = 42; // Número de ejemplo
            data.status = 1;  // Estado de ejemplo

            // Enviar el mensaje normal al servidor
            send_message(sock, data);
            memset(input, 0, sizeof(input)); // Limpiar la entrada
        }

        // Manejo de registro como "Player" o "Spectator"
        if (IsKeyPressed(KEY_P)) {
            // Enviar mensaje de registro como "Player"
            send_register_message(sock, "Player"); // Enviar al servidor
            printf("Registrado como Player\n");
        } else if (IsKeyPressed(KEY_S)) {
            // Enviar mensaje de registro como "Spectator"
            send_register_message(sock, "Spectator"); // Enviar al servidor
            printf("Registrado como Spectator\n");
        }

        // Dibuja la interfaz gráfica
        BeginDrawing();
        ClearBackground(RAYWHITE);

        DrawText("Enter a message:", 20, 20, 20, LIGHTGRAY);
        DrawText(input, 20, 50, 20, LIGHTGRAY);

        // Mostrar instrucciones
        DrawText("Press ENTER to send the message.", 20, 100, 20, LIGHTGRAY);
        DrawText("Press P to register as Player.", 20, 130, 20, LIGHTGRAY);
        DrawText("Press S to register as Spectator.", 20, 160, 20, LIGHTGRAY);
        DrawText("Press ESC to exit.", 20, 190, 20, LIGHTGRAY);

        EndDrawing();

        // Captura la entrada de texto
        if (IsKeyPressed(KEY_BACKSPACE)) {
            int len = strlen(input);
            if (len > 0) input[len - 1] = '\0'; // Eliminar el último carácter
        } else {
            // Agregar texto a la entrada
            for (int i = 32; i < 126; i++) {
                if (IsKeyPressed(i)) {
                    int len = strlen(input);
                    if (len < MAX_INPUT_SIZE - 1) {
                        input[len] = (char)i;
                        input[len + 1] = '\0'; // Agregar carácter y terminador nulo
                    }
                }
            }
        }
    }

    // Cerrar el socket
    close_socket(sock);
    CloseWindow(); // Cerrar la ventana y el contexto OpenGL

    return 0;
}