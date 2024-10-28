#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "config.h"

// Callback para manejar las configuraciones
int handler(void* user, const char* section, const char* name, const char* value) {
    Config* config = (Config*)user;
    if (strcmp(section, "Settings") == 0) {
        if (strcmp(name, "port") == 0) {
            config->port = atoi(value);
            printf("Loaded port: %d\n", config->port); // Debug: Print loaded port
            return 1;
        } else if (strcmp(name, "max_input_size") == 0) {
            config->max_input_size = atoi(value);
            printf("Loaded max_input_size: %d\n", config->max_input_size); // Debug: Print loaded max_input_size
            return 1;
        } else if (strcmp(name, "ip_address") == 0) {
            strncpy(config->ip_address, value, sizeof(config->ip_address) - 1);
            config->ip_address[sizeof(config->ip_address) - 1] = '\0'; // Asegurar terminador nulo
            printf("Loaded IP address: %s\n", config->ip_address); // Debug: Print loaded IP address
            return 1;
        }
    }
    return 0; // No se manejó la clave
}

// Función para cargar configuraciones desde un archivo INI
int load_config(const char* filename, Config* config) {
    if (ini_parse(filename, handler, config) < 0) {
        printf("Can't load '%s'\n", filename);
        return 0;
    }
    return 1;
}