#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "raylib.h"
#include "./Client/data.h"
#include "./Client/client.h"

#define PORT 12346
#define MAX_INPUT_SIZE 256

int main(void)
{
    // Initialize variables
    const int screenWidth = 800;
    const int screenHeight = 450;

    InitWindow(screenWidth, screenHeight, "Raylib Client Example");

    SetTargetFPS(60);

    // Socket variables
    int sock;
    struct sockaddr_in server_addr;

    // Message to send
    Data data;
    char input[MAX_INPUT_SIZE] = "Hello from the client!"; // Default message

    // Initialize the socket
    initialize_socket(&sock, &server_addr, PORT);

    // Main loop
    while (!WindowShouldClose()) {
        // Check if the user presses ENTER to send the message
        if (IsKeyPressed(KEY_ENTER)) {
            strcpy(data.message, input); // Copy input message
            data.number = 42; // Example number
            data.status = 1;  // Example status

            // Send the message to the server
            send_message(sock, data);

            //
            receive_message(sock); // Recibir respuesta

            // Clear the input
            memset(input, 0, sizeof(input));
        }

        // Draw
        BeginDrawing();
        ClearBackground(RAYWHITE);

        DrawText("Enter a message:", 20, 20, 20, LIGHTGRAY);
        DrawText(input, 20, 50, 20, LIGHTGRAY);

        // Show instructions
        DrawText("Press ENTER to send the message.", 20, 100, 20, LIGHTGRAY);
        DrawText("Press ESC to exit.", 20, 130, 20, LIGHTGRAY);

        EndDrawing();

        // Capture text input
        if (IsKeyPressed(KEY_BACKSPACE)) {
            int len = strlen(input);
            if (len > 0) input[len - 1] = '\0'; // Remove last character
        } else {
            // Add text to the input
            for (int i = 32; i < 126; i++) {
                if (IsKeyPressed(i)) {
                    int len = strlen(input);
                    if (len < MAX_INPUT_SIZE - 1) {
                        input[len] = (char)i;
                        input[len + 1] = '\0'; // Add character and null terminator
                    }
                }
            }
        }
    }

    // Close the socket
    close_socket(sock);
    CloseWindow(); // Close the window and OpenGL context

    return 0;
}