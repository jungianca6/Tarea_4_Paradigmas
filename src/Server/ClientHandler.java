package Server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class ClientHandler implements Runnable {
    private Socket socket;
    private static final List<ClientInfo> clients = new ArrayList<>(); // Lista de clientes conectados
    private UUID clientId; // Usar UUID como ID del cliente

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = UUID.randomUUID(); // Generar un ID único en formato GUID
        synchronized (clients) {
            // Guardar la información del cliente
            clients.add(new ClientInfo(socket, clientId, socket.getInetAddress().getHostAddress(), socket.getPort()));
        }
    }

    @Override
    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            String lectura_json;

            // Bucle para recibir múltiples mensajes
            while ((lectura_json = entrada.readLine()) != null) {

                System.out.println("Cliente conectado: " + clientId + " desde " + socket.getInetAddress() + ":" + socket.getPort());


                System.out.println("Mensaje recibido: " + lectura_json); // Imprimir el mensaje recibido

                // Asegúrate de que la lectura no sea null o vacía
                if (!lectura_json.isEmpty()) {
                    // Deserializar el mensaje JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    Data data = objectMapper.readValue(lectura_json, Data.class);
                    System.out.println("Cliente " + clientId);
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
            System.out.println("Error en la comunicación con el cliente " + clientId + ": " + e.getMessage());
        } finally {
            // Remover el cliente de la lista cuando se desconecta
            synchronized (clients) {
                clients.removeIf(client -> client.getSocket().equals(socket));
                printConnectedClients(); // Imprimir la lista de clientes al desconectarse
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket del cliente " + clientId + ": " + e.getMessage());
            }
        }
    }

    // Método para imprimir los clientes conectados
    private void printConnectedClients() {
        System.out.println("Clientes conectados:");
        synchronized (clients) {
            for (ClientInfo client : clients) {
                System.out.println("ID: " + client.getClientId() + ", IP: " + client.getIpAddress() + ", Puerto: " + client.getPort());
            }
        }
        System.out.println("Total de clientes conectados: " + clients.size());
    }
}


