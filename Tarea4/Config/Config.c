#include <stdio.h>
#include <string.h>
#include "../INI_Library/ini.h" // Incluir la biblioteca inih
#include "Config.h"
#include <stdlib.h>

// Manejador para procesar cada línea del archivo INI
int handler(void* user, const char* section, const char* name, const char* value) {
    Config* config = (Config*)user;

    // Verifica la sección y el nombre para almacenar los valores adecuados
    if (strcmp(section, "Network") == 0) {
        if (strcmp(name, "ip_address") == 0) {
            strncpy(config->ip_address, value, sizeof(config->ip_address));
            config->ip_address[sizeof(config->ip_address) - 1] = '\0'; // Asegurarse de que la cadena esté terminada
        } else if (strcmp(name, "port") == 0) {
            config->port = atoi(value); // Convertir a entero
        }
    }
    return 1; // Continúa procesando
}

// Función para leer la configuración desde un archivo INI
int read_config(const char* filename, Config* config) {
    int result = ini_parse(filename, handler, config); // Llama al manejador
    if (result < 0) {
        printf("Error leyendo el archivo INI: %s\n", filename);
        return -1; // Error al leer el archivo
    }
    return 0; // Éxito
}