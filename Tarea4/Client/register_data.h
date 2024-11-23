
#ifndef REGISTER_DATA_H
#define REGISTER_DATA_H

typedef struct {
    char type_message[20]; // Tipo de mensaje (ej. "data")
    char type[20]; // "jugador" o "espectador"
} RegisterData;

#endif //REGISTER_DATA_H
