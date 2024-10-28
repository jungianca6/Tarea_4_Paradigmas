#ifndef CONFIG_H
#define CONFIG_H

#include "../INI/ini.h" // Incluir la biblioteca INIh

// Estructura para almacenar configuraciones
typedef struct {
    int port;
    int max_input_size;
    char ip_address[16]; // Espacio para almacenar una dirección IP
} Config;

// Función para cargar configuraciones desde un archivo INI
int load_config(const char* filename, Config* config);

#endif // CONFIG_H