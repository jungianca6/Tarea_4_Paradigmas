#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "client.h"
#include <sys/select.h>
#include <cjson/cJSON.h>

void send_register_message(int socket_fd, const char* type) {
    // Crear la estructura de registro
    RegisterData register_data;
    strcpy(register_data.type, type); // Copiar el tipo a la estructura

    // Serializar la estructura a JSON
    cJSON *json = cJSON_CreateObject();
    cJSON_AddStringToObject(json, "type_message", "register"); // Añadir tipo de mensaje
    cJSON_AddStringToObject(json, "type", register_data.type); // Añadir tipo

    char *jsonString = cJSON_PrintUnformatted(json);
    printf("Enviando JSON de registro: %s\n", jsonString);

    size_t jsonLength = strlen(jsonString);
    char *jsonWithNewline = malloc(jsonLength + 2); // +2 para '\n' y '\0'
    sprintf(jsonWithNewline, "%s\n", jsonString);

    ssize_t bytes_sent = send(socket_fd, jsonWithNewline, strlen(jsonWithNewline), 0);
    if (bytes_sent < 0) {
        perror("Error sending register message");
    }

    cJSON_Delete(json);
    free(jsonString);
    free(jsonWithNewline);
}

void send_message(int socket_fd, Data data) {
    cJSON *json = cJSON_CreateObject();
    cJSON_AddStringToObject(json, "type_message", "data_message"); // Añadir tipo de mensaje
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

    // Obtener el tipo de mensaje
    cJSON *json_type_message = cJSON_GetObjectItem(json, "type_message");
    if (json_type_message != NULL && json_type_message->valuestring != NULL) {
        if (strcmp(json_type_message->valuestring, "data") == 0) {
            // Manejo del mensaje de tipo "data"
            cJSON *json_message = cJSON_GetObjectItem(json, "message");
            cJSON *json_number = cJSON_GetObjectItem(json, "number");
            cJSON *json_status = cJSON_GetObjectItem(json, "status");

            if (json_message != NULL && json_number != NULL && json_status != NULL) {
                printf("Mensaje: %s\n", json_message->valuestring);
                printf("Número: %d\n", json_number->valueint);
                printf("Estado: %d\n", json_status->valueint);
            } else {
                printf("Error: no se encontraron todos los campos en el JSON de tipo 'data'.\n");
            }
        } else if (strcmp(json_type_message->valuestring, "data_parties") == 0) {
            // Manejo del mensaje de tipo "data_parties"
            cJSON *json_message = cJSON_GetObjectItem(json, "message");
            cJSON *json_parties = cJSON_GetObjectItem(json, "parties");
            cJSON *json_num_parties = cJSON_GetObjectItem(json, "num_parties");

            if (json_message != NULL && json_parties != NULL && json_num_parties != NULL) {
                printf("Mensaje de partidas: %s\n", json_message->valuestring);
                int num_parties = json_num_parties->valueint;
                printf("Número de partidas: %d\n", num_parties);

                // Procesar la lista de partidas
                if (cJSON_IsArray(json_parties)) {
                    for (int i = 0; i < num_parties; i++) {
                        cJSON *party = cJSON_GetArrayItem(json_parties, i);
                        if (party != NULL) {
                            cJSON *id_partida = cJSON_GetObjectItem(party, "id_partida");
                            cJSON *ip = cJSON_GetObjectItem(party, "ip");
                            cJSON *puerto = cJSON_GetObjectItem(party, "puerto");
                            if (id_partida != NULL && ip != NULL && puerto != NULL) {
                                printf("Partida %d: ID = %s, IP = %s, Puerto = %d\n",
                                       i + 1, id_partida->valuestring, ip->valuestring, puerto->valueint);
                            }
                        }
                    }
                }
            } else {
                printf("Error: no se encontraron todos los campos en el JSON de tipo 'data_parties'.\n");
            }
        } else {
            printf("Error: tipo de mensaje desconocido.\n");
        }
    } else {
        printf("Error: no se encontró el campo 'type_message' en el JSON.\n");
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