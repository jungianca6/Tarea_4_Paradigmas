#ifndef CLIENT_H
#define CLIENT_H

#include "data.h"
#include "parties_data.h"
#include "register_data.h"
#include <arpa/inet.h> // For sockaddr_in

// Declaración externa de partyList
extern PartyList partyList;

void send_message(int socket_fd, Data data);
// Función para inicializar el socket
int initialize_socket(int *sock, struct sockaddr_in *server_addr, int port, const char *ip_address);
void close_socket(int sock);
void receive_message(int socket_fd);
void send_register_message(int socket_fd, const char* type);
void update_party_list(DataParties *data_parties);
void send_choice_message(int socket_fd, const char* party_id, const char* ip, int port);

#endif // CLIENT_H