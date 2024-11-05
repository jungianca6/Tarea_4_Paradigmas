#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "client.h"
#include <sys/select.h>
#include <cjson/cJSON.h>

extern PartyList partyList; // Lista global de partidas
extern char* tipo_jugador;
extern BrickArray bricks;
extern struct Player player;

void receive_message(int socket_fd) {

    char buffer[1024]; // Buffer para almacenar la respuesta
    ssize_t bytes_received = recv(socket_fd, buffer, sizeof(buffer) - 1, 0); // Deja espacio para el terminador nulo

    if (bytes_received < 0) {
        perror("Error receiving message");
        exit(EXIT_FAILURE);
    } else if (bytes_received == 0) {
        // Si bytes_received es 0, significa que el socket se ha cerrado
        printf("El servidor ha cerrado la conexión.\n");
        close_socket(socket_fd);
        exit(EXIT_FAILURE);
    }

    buffer[bytes_received] = '\0'; // Termina la cadena

    // Parsear el JSON
    cJSON *json = cJSON_Parse(buffer);
    if (json == NULL) {
        printf("Error parsing JSON: %s\n", cJSON_GetErrorPtr());
        exit(EXIT_FAILURE);
    }
    // Imprimir el mensaje JSON recibido
    //printf("Mensaje JSON recibido: %s\n", buffer);


    // Obtener el tipo de mensaje
    cJSON *json_type_message = cJSON_GetObjectItem(json, "type_message");
    if (json_type_message == NULL || json_type_message->valuestring == NULL) {
        printf("Error: no se encontró el campo 'type_message' en el JSON.\n");
        cJSON_Delete(json);
        return; // O puedes decidir cerrar el socket aquí
    }


    if (strcmp(json_type_message->valuestring, "data_parties") == 0) {
        // Manejo del mensaje de tipo "data_parties"
        DataParties data_parties;
        strcpy(data_parties.type_message, json_type_message->valuestring);
        cJSON *json_message = cJSON_GetObjectItem(json, "message");
        if (json_message != NULL) {
            strcpy(data_parties.message, json_message->valuestring);
        }

        // Procesar la lista de partidas
        cJSON *json_parties = cJSON_GetObjectItem(json, "parties");
        data_parties.num_parties = cJSON_GetArraySize(json_parties);

        // Inicializa el arreglo de partidas
        data_parties.parties = malloc(data_parties.num_parties * sizeof(Partida));
        if (data_parties.parties == NULL) {
            perror("Error allocating memory for parties");
            cJSON_Delete(json);
            return; // O puedes decidir cerrar el socket aquí
        }

        for (int i = 0; i < data_parties.num_parties; i++) {
            cJSON *party = cJSON_GetArrayItem(json_parties, i);
            if (party != NULL) {
                cJSON *id_partida = cJSON_GetObjectItem(party, "id_partida");
                cJSON *ip = cJSON_GetObjectItem(party, "ip");
                cJSON *puerto = cJSON_GetObjectItem(party, "puerto");
                if (id_partida != NULL && ip != NULL && puerto != NULL) {
                    strcpy(data_parties.parties[i].id_partida, id_partida->valuestring);
                    strcpy(data_parties.parties[i].ip, ip->valuestring);
                    data_parties.parties[i].puerto = puerto->valueint;
                }
            }
        }

        // Llama a la función para actualizar la lista de partidas en la interfaz gráfica
        update_party_list(&data_parties);
        // Libera la memoria
        free(data_parties.parties);

    }else if (strcmp(json_type_message->valuestring, "break_block") == 0){
        if (strcmp(tipo_jugador, "Spectator") == 0){
            cJSON *json_brick_row = cJSON_GetObjectItem(json, "row");
            cJSON *json_brick_column = cJSON_GetObjectItem(json, "column");
            for (int i = 0; i < bricks.size; i++) {
                Brick brick = bricks.data[i];
                int brickRow = (brick.base.rect.y - 50) / 26;
                int brickColumn = (brick.base.rect.x - 5) / 61;
                if (brickRow == cJSON_GetNumberValue(json_brick_row) && brickColumn == cJSON_GetNumberValue(json_brick_column) ) {
                    // Elimina el bloque desplazando el resto de elementos
                    for (int j = i; j < bricks.size - 1; j++) {
                        bricks.data[j] = bricks.data[j + 1];
                    }
                    printf("Destrui un bloque :D");
                    printf("\n");
                    bricks.size--;
                    break;
                }
            }
        }
    }
    else if (strcmp(json_type_message->valuestring, "power_block") == 0) {
        printf("Mensaje JSON recibido: %s\n", buffer);
        if (strcmp(tipo_jugador, "Player") == 0) {
            cJSON *json_brick_row = cJSON_GetObjectItem(json, "row");
            cJSON *json_brick_column = cJSON_GetObjectItem(json, "column");
            cJSON *json_brick_power = cJSON_GetObjectItem(json, "power");
            /*
            // Imprimir el poder del bloque
            if (json_brick_power != NULL && json_brick_power->valuestring != NULL) {
                printf("Poder del bloque: %s\n", json_brick_power->valuestring);

            } else {
                printf("No se encontró el poder del bloque.\n");
            }*/
            for (int i = 0; i < bricks.size; i++) {
                Brick brick = bricks.data[i];
                int brickRow = (brick.base.rect.y - 50) / 26;
                int brickColumn = (brick.base.rect.x - 5) / 61;
                if (brickRow == cJSON_GetNumberValue(json_brick_row) && brickColumn == cJSON_GetNumberValue(json_brick_column) ) {
                    switch (json_brick_power->valuestring[0]) {
                        case 'N':
                            bricks.data[i].power = NO_POWER;
                            break;
                        case 'L':
                            bricks.data[i].power = INCREASE_LENGTH;
                            break;
                        case 'S':
                            bricks.data[i].power = DECREASE_LENGTH;
                            break;
                        case 'V':
                            bricks.data[i].power = INCREASE_LIVES;
                            break;
                        case 'A':
                            bricks.data[i].power = INCREASE_SPEED;
                            break;
                        case 'D':
                            bricks.data[i].power = DECREASE_SPEED;
                            break;
                        case 'E':
                            bricks.data[i].power = CREATE_EXTRA_BALL;
                            break;
                    }
                }
            }
        }
    }
    else if (strcmp(json_type_message->valuestring, "player_data") == 0) {
        if (strcmp(tipo_jugador, "Spectator") == 0) {
            cJSON *json_player_posx = cJSON_GetObjectItem(json, "posx");
            cJSON *json_player_posy = cJSON_GetObjectItem(json, "posy");
            cJSON *json_player_ancho = cJSON_GetObjectItem(json, "ancho");
            cJSON *json_player_largo = cJSON_GetObjectItem(json, "largo");
            player.rect.x = cJSON_GetNumberValue(json_player_posx);
            player.rect.y = cJSON_GetNumberValue(json_player_posy);
            player.rect.width = cJSON_GetNumberValue(json_player_ancho);
            player.rect.height = cJSON_GetNumberValue(json_player_largo);
        }
    }
    else {
        printf("Error: tipo de mensaje desconocido.\n");
    }

    memset(buffer, 0, sizeof(buffer));
    // Limpiar
    cJSON_Delete(json);
}

void update_party_list(DataParties *data_parties) {
    // Actualiza la lista global de partidas
    partyList.count = data_parties->num_parties;
    partyList.parties = realloc(partyList.parties, partyList.count * sizeof(Partida));
    for (int i = 0; i < partyList.count; i++) {
        strcpy(partyList.parties[i].id_partida, data_parties->parties[i].id_partida);
        strcpy(partyList.parties[i].ip, data_parties->parties[i].ip);
        partyList.parties[i].puerto = data_parties->parties[i].puerto;
    }

    // Imprimir las partidas en la consola para verificar
    printf("Partidas disponibles:\n");
    for (int i = 0; i < partyList.count; i++) {
        printf("ID: %s, IP: %s, Puerto: %d\n", partyList.parties[i].id_partida, partyList.parties[i].ip, partyList.parties[i].puerto);
    }
}

void send_player_info(int socket_fd, int posx, int posy, float ancho, float alto) {
    DataPlayer data_player;
    data_player.posx = posx;
    data_player.posy = posy;
    data_player.ancho = ancho;
    data_player.alto = alto;

    // Serializar la estructura a JSON
    cJSON *json = cJSON_CreateObject();
    cJSON_AddStringToObject(json, "type_message", "player_data"); // Añadir tipo de mensaje
    cJSON_AddNumberToObject(json, "posx", data_player.posx); // Añadir tipo
    cJSON_AddNumberToObject(json, "posy", data_player.posy); // Añadir tipo
    cJSON_AddNumberToObject(json, "ancho", data_player.ancho); // Añadir tipo
    cJSON_AddNumberToObject(json, "largo", data_player.alto); // Añadir tipo

    char *jsonString = cJSON_PrintUnformatted(json);
    //printf("Enviando JSON de player: %s\n", jsonString);

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

void send_bricks_info(int socket_fd, int column, int row, const char* poder) {
    DataBricks data_bricks;
    data_bricks.column = column;
    data_bricks.row = row;
    strcpy(data_bricks.poder, poder);

    // Serializar la estructura a JSON
    cJSON *json = cJSON_CreateObject();
    cJSON_AddStringToObject(json, "type_message", "bricks_data"); // Añadir tipo de mensaje
    cJSON_AddNumberToObject(json, "column", data_bricks.column); // Añadir tipo
    cJSON_AddNumberToObject(json, "row", data_bricks.row); // Añadir tipo
    cJSON_AddStringToObject(json, "poder", data_bricks.poder); // Añadir tipo

    char *jsonString = cJSON_PrintUnformatted(json);
    printf("Enviando JSON de bloque: %s\n", jsonString);

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

void send_choice_message(int socket_fd, const char* party_id, const char* ip, int port) {
    // Crear un objeto JSON
    cJSON *json = cJSON_CreateObject();
    if (json == NULL) {
        fprintf(stderr, "Error creando JSON\n");
        return;
    }

    // Añadir los campos al objeto JSON
    cJSON_AddStringToObject(json, "type_message", "choice"); // Tipo de mensaje
    cJSON_AddStringToObject(json, "id_partida", party_id); // ID de la partida
    cJSON_AddStringToObject(json, "ip", ip); // IP de la partida
    cJSON_AddNumberToObject(json, "puerto", port); // Puerto de la partida

    // Convertir el objeto JSON a una cadena
    char *jsonString = cJSON_PrintUnformatted(json);
    if (jsonString == NULL) {
        fprintf(stderr, "Error imprimiendo JSON\n");
        cJSON_Delete(json); // Liberar el objeto JSON
        return;
    }

    // Imprimir el mensaje en formato JSON
    printf("Enviando JSON de elección: %s\n", jsonString);

    // Agregar un salto de línea al final de la cadena JSON
    size_t jsonLength = strlen(jsonString);
    char *jsonWithNewline = malloc(jsonLength + 2); // +2 para '\n' y '\0'
    sprintf(jsonWithNewline, "%s\n", jsonString);

    // Enviar la cadena JSON al servidor
    ssize_t bytes_sent = send(socket_fd, jsonWithNewline, strlen(jsonWithNewline), 0);
    if (bytes_sent < 0) {
        perror("Error enviando mensaje de elección");
    }

    // Limpiar
    free(jsonWithNewline); // Liberar la cadena con el salto de línea
    free(jsonString); // Liberar la cadena JSON
    cJSON_Delete(json); // Liberar el objeto JSON
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