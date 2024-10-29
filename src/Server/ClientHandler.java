package Server;

import Observer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class ClientHandler implements Runnable{
    private Socket socket;
    private UUID clientId; // Usar UUID como ID del cliente
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID();
        synchronized (Server.clients) {
            Server.clients.add(new ClientInfo(socket, clientId, socket.getInetAddress().getHostAddress(), socket.getPort()));
            server.notifyClientListUpdated(); // Notificar a los observadores
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
                    ObjectMapper objectMapper = new ObjectMapper();

                    // Determinar el tipo de mensaje
                    if (lectura_json.contains("type")) {
                        // Mensaje de registro
                        Register_Data registerData = objectMapper.readValue(lectura_json, Register_Data.class);
                        clientRegister(registerData); // Lógica para manejar el registro
                    } else if (lectura_json.contains("message")) {
                        // Mensaje de Data
                        Data data = objectMapper.readValue(lectura_json, Data.class);
                        receive_client_Data(data); // Lógica para manejar el mensaje de datos
                    }

                } else {
                    System.out.println("El mensaje recibido está vacío.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente " + clientId + ": " + e.getMessage());
        } finally {
            // Remover el cliente de la lista cuando se desconecta
            synchronized (server.clients) {
                server.clients.removeIf(client -> client.getSocket().equals(socket));
                server.notifyClientListUpdated(); // Notificar a los observadores
                printConnectedClients(); // Imprimir la lista de clientes al desconectarse
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket del cliente " + clientId + ": " + e.getMessage());
            }
        }
    }

    // Manejar el mensaje de datos
    private void receive_client_Data(Data data) {
        System.out.println("Cliente " + clientId);
        System.out.println("Mensaje recibido: " + data.message);
        System.out.println("Número recibido: " + data.number);
        System.out.println("Estado recibido: " + data.status);

        // Llamar al método para preparar y enviar la respuesta
        sendResponseToAllClients(data);
    }


    private void clientRegister(Register_Data registerData) {
        System.out.println("Cliente ID: " + clientId.toString());

        synchronized (Server.clients) {
            System.out.println("Clientes registrados: ");
            if (Server.clients.isEmpty()) {
                System.out.println("No hay clientes registrados.");
            } else {
                for (ClientInfo client : Server.clients) {
                    System.out.println("ID del cliente: " + client.getClientId());
                }
            }

            // Lógica para procesar el registro
            String type = registerData.getType();
            if ("Player".equals(type) || "Spectator".equals(type)) {
                System.out.println("Cliente " + clientId + " registrado como " + type.toLowerCase() + ".");
                for (ClientInfo client : Server.clients) {
                    if (client.getClientId().equals(clientId)) {
                        client.setClientType(type);
                        System.out.println("Cliente actualizado: " + client.getClientId() + ", Tipo: " + client.getClientType());
                        break; // Salir del bucle una vez encontrado
                    }
                }
            }
        }
        // Notificar a los observadores sobre la actualización
        server.notifyClientListUpdated();
    }


    // Método para imprimir los clientes conectados
    private void printConnectedClients() {
        System.out.println("Clientes conectados:");
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                System.out.println("ID: " + client.getClientId() + ", IP: " + client.getIpAddress() + ", Puerto: " + client.getPort());
            }
        }
        System.out.println("Total de clientes conectados: " + server.clients.size());
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

            synchronized (server.clients) {
                for (ClientInfo client : server.clients) {
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


