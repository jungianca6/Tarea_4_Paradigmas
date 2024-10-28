package Server;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            // Leer hasta que no haya más líneas
            while ((line = in.readLine()) != null) {
                jsonBuilder.append(line).append("\n");
                System.out.println("Mensaje recibido: " + line); // Imprimir cada línea recibida

                // Aquí podrías agregar lógica para terminar la conexión si se recibe un mensaje específico
                if (line.trim().equalsIgnoreCase("CERRAR")) {
                    System.out.println("Conexión cerrada por el cliente.");
                    break; // Salir del bucle si se recibe el comando de cierre
                }
            }

            // Convertir a String
            String jsonString = jsonBuilder.toString().trim(); // Trim para quitar espacios en blanco
            System.out.println("JSON recibido: " + jsonString); // Imprimir el JSON recibido

            if (!jsonString.isEmpty()) {
                // Deserializa el mensaje JSON
                Data data = new Gson().fromJson(jsonString, Data.class);
                System.out.println("Mensaje recibido: " + data.message);
                System.out.println("Número recibido: " + data.number); // Imprimir número
                System.out.println("Estado recibido: " + data.status); // Imprimir estado

                // Preparar la respuesta
                Data responseData = new Data("Mensaje recibido correctamente", data.number * 2, 1); // Ejemplo de respuesta
                String jsonResponse = new Gson().toJson(responseData);

                // Enviar la respuesta al cliente
                out.println(jsonResponse); // Enviar respuesta en formato JSON
                System.out.println("Respuesta enviada: " + jsonResponse);
            } else {
                System.out.println("El mensaje recibido está vacío.");
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
