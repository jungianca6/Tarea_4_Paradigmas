#ifndef DATA_H
#define DATA_H

// Estructura para el mensaje
typedef struct {
    char message[512];  // Mensaje
    int number;         // Un número entero
    int status;         // Un estado como entero
} Data;

#endif // DATA_H