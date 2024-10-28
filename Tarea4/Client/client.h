#ifndef CLIENT_H
#define CLIENT_H

#include "data.h"
#include <arpa/inet.h> // For sockaddr_in

void send_message(int socket_fd, Data data);
int initialize_socket(int *sock, struct sockaddr_in *server_addr, int port);
void close_socket(int sock);
void receive_message(int socket_fd);

#endif // CLIENT_H

