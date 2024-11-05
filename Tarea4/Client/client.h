#ifndef CLIENT_H
#define CLIENT_H

#include "data.h"
#include "raylib.h"
#include "player_data.h"
#include "bricks_data.h"
#include "parties_data.h"
#include "register_data.h"
#include "balls_data.h"
#include "brickmatriz_data.h"
#include <arpa/inet.h> // For sockaddr_in
#include "../Components/brick.h"
#include "../Components/brick_array.h"
#include "../Components/player.h"
#include "../Components/ball.h"

// Declaración externa de partyList
extern PartyList partyList;
extern BrickArray bricks;
extern char* tipo_jugador;;
extern struct Player player;
extern struct Ball balls[10];
extern int puntaje_rojo;
extern int puntaje_naranja;
extern int puntaje_amarillo;
extern int puntaje_verde;

void send_message(int socket_fd, Data data);
// Función para inicializar el socket
int initialize_socket(int *sock, struct sockaddr_in *server_addr, int port, const char *ip_address);
void close_socket(int sock);
void receive_message(int socket_fd);
void send_register_message(int socket_fd, const char* type);
void update_party_list(DataParties *data_parties);
void send_choice_message(int socket_fd, const char* party_id, const char* ip, int port);
void send_player_info(int socket_fd, int posx, int posy, float ancho, float alto);
void send_bricks_info(int socket_fd, int column, int row, const char* poder);
void send_balls_info(int socket_fd);
void send_bricks_matriz_info(int socket_fd);


#endif // CLIENT_H