#ifndef DATA_H
#define DATA_H

// Estructura para el mensaje
typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "data")
    char message[512];  // Mensaje
    int number;         // Un número entero
    int status;         // Un estado como entero
} Data;

#endif // DATA_H