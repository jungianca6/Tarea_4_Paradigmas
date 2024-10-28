#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "client.h"
#include <sys/select.h>
#include <cjson/cJSON.h>


void send_message(int socket_fd, Data data) {
    cJSON *json = cJSON_CreateObject();
    cJSON_AddStringToObject(json, "message", data.message);
    cJSON_AddNumberToObject(json, "number", data.number);
    cJSON_AddNumberToObject(json, "status", data.status);

    char *jsonString = cJSON_PrintUnformatted(json);
    printf("Enviando JSON: %s\n", jsonString);

    size_t jsonLength = strlen(jsonString);
    char *jsonWithNewline = malloc(jsonLength + 2); // +2 para '\n' y '\0'
    sprintf(jsonWithNewline, "%s\n", jsonString);

    ssize_t bytes_sent = send(socket_fd, jsonWithNewline, strlen(jsonWithNewline), 0);
    if (bytes_sent < 0) {
        perror("Error sending message");
    }

    cJSON_Delete(json);
    free(jsonString);
    free(jsonWithNewline);

}


void receive_message(int socket_fd) {
    char buffer[1024]; // Buffer para almacenar la respuesta
    ssize_t bytes_received = recv(socket_fd, buffer, sizeof(buffer) - 1, 0); // Deja espacio para el terminador nulo

    if (bytes_received < 0) {
        perror("Error receiving message");
        exit(EXIT_FAILURE);
    }

    buffer[bytes_received] = '\0'; // Termina la cadena
    printf("Mensaje recibido: %s\n", buffer); // Imprimir el mensaje recibido

    // Parsear el JSON
    cJSON *json = cJSON_Parse(buffer);
    if (json == NULL) {
        printf("Error parsing JSON: %s\n", cJSON_GetErrorPtr());
        exit(EXIT_FAILURE);
    }

    // Deserializar y imprimir los datos
    cJSON *json_message = cJSON_GetObjectItem(json, "message");
    cJSON *json_number = cJSON_GetObjectItem(json, "number");
    cJSON *json_status = cJSON_GetObjectItem(json, "status");

    if (json_message != NULL && json_number != NULL && json_status != NULL) {
        printf("Mensaje: %s\n", json_message->valuestring);
        printf("Número: %d\n", json_number->valueint);
        printf("Estado: %d\n", json_status->valueint);
    } else {
        printf("Error: no se encontraron todos los campos en el JSON.\n");
    }

    // Limpiar
    cJSON_Delete(json);
}

// Function to initialize the socket
int initialize_socket(int *sock, struct sockaddr_in *server_addr, int port, const char *ip_address) {
    // Create socket
    *sock = socket(AF_INET, SOCK_STREAM, 0);
    if (*sock < 0) {
        perror("Error creating socket");
        exit(1);
    }

    // Configure the server address
    server_addr->sin_family = AF_INET;
    server_addr->sin_port = htons(port);
    server_addr->sin_addr.s_addr = inet_addr(ip_address); // Usar la dirección IP proporcionada

    // Connect to the server
    if (connect(*sock, (struct sockaddr*)server_addr, sizeof(*server_addr)) < 0) {
        perror("Error connecting to server");
        close(*sock);
        exit(1);
    }

    return *sock; // Return the socket file descriptor
}

// Function to close the socket
void close_socket(int sock) {
    close(sock);
}