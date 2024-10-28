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

                    //Prints de prueba
                    System.out.println("Cliente " + clientId);
                    System.out.println("Mensaje recibido: " + data.message);
                    System.out.println("Número recibido: " + data.number);
                    System.out.println("Estado recibido: " + data.status);


                    // Llamar al método para preparar y enviar la respuesta
                    sendResponseToAllClients(data);

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

    // Método para enviar la respuesta al cliente
    private void sendResponse(PrintWriter salida, Data data) {
        try {
            // Preparar la respuesta
            Data responseData = new Data("Mensaje recibido correctamente", data.number * 2, 1);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(responseData);

            // Enviar la respuesta al cliente
            salida.println(jsonResponse); // Enviar respuesta en formato JSON
            System.out.println("Respuesta enviada al cliente " + clientId + ": " + jsonResponse);
        } catch (IOException e) {
            System.out.println("Error al enviar respuesta al cliente " + clientId + ": " + e.getMessage());
        }
    }

    // Método para enviar la respuesta a todos los clientes conectados
    private void sendResponseToAllClients(Data data) {
        try {
            Data responseData = new Data("Mensaje recibido correctamente: " + data.message, data.number * 2, 1);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(responseData);

            synchronized (clients) {
                for (ClientInfo client : clients) {
                    // No enviar la respuesta al cliente que envió el mensaje
                    if (!client.getSocket().equals(socket)) {
                        PrintWriter clientOutput = new PrintWriter(client.getSocket().getOutputStream(), true);
                        clientOutput.println(jsonResponse); // Enviar respuesta en formato JSON
                        System.out.println("Respuesta enviada al cliente " + client.getClientId() + ": " + jsonResponse);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al enviar respuesta a los clientes: " + e.getMessage());
        }
    }
}


