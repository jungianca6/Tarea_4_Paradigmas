package Server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.Socket;

// Clase para manejar la comunicación con cada cliente
class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            String lectura_json;

            // Bucle para recibir múltiples mensajes
            while ((lectura_json = entrada.readLine()) != null) {
                System.out.println("Mensaje recibido: " + lectura_json); // Imprimir el mensaje recibido

                // Asegúrate de que la lectura no sea null o vacía
                if (!lectura_json.isEmpty()) {
                    // Deserializar el mensaje JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    Data data = objectMapper.readValue(lectura_json, Data.class);

                    System.out.println("Mensaje recibido: " + data.message);
                    System.out.println("Número recibido: " + data.number);
                    System.out.println("Estado recibido: " + data.status);

                    // Preparar la respuesta
                    Data responseData = new Data("Mensaje recibido correctamente", data.number * 2, 1);
                    String jsonResponse = objectMapper.writeValueAsString(responseData);

                    // Enviar la respuesta al cliente
                    salida.println(jsonResponse); // Enviar respuesta en formato JSON
                    System.out.println("Respuesta enviada: " + jsonResponse);
                } else {
                    System.out.println("El mensaje recibido está vacío.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error en la comunicación: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }

}

