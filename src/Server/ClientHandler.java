package Server;

import Observer.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.util.UUID;
import Game.Partida;

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

                    try {
                        // Deserializa el JSON a JsonNode para acceder a sus propiedades
                        JsonNode jsonNode = objectMapper.readTree(lectura_json);
                        String typeMessage = jsonNode.has("type_message") ? jsonNode.get("type_message").asText() : "";
                        System.out.println(typeMessage);
                        // Determinar el tipo de mensaje
                        switch (typeMessage) {
                            case "register":
                                Register_Data registerData = objectMapper.treeToValue(jsonNode, Register_Data.class);
                                clientRegister(registerData); // Lógica para manejar el registro
                                break;

                            case "data":
                                Data data = objectMapper.treeToValue(jsonNode, Data.class);
                                receive_client_Data(data); // Lógica para manejar el mensaje de datos
                                break;

                            case "choice":
                                try {
                                    // Deserializa el JSON a Party_Choice_Data
                                    Party_Choice_Data choiceData = objectMapper.treeToValue(jsonNode, Party_Choice_Data.class);

                                    // Extrae la información de la partida
                                    String idPartidaString = choiceData.getId_partida();
                                    String ip = choiceData.getIp();
                                    int puerto = choiceData.getPuerto();

                                    // Convertir el String a UUID
                                    UUID idPartida = UUID.fromString(idPartidaString);

                                    // Crea la instancia de Partida con la información extraída
                                    Partida partida = new Partida(idPartida, ip, puerto);

                                    // Llama a la función que maneja la lógica de la elección de partida
                                    receive_client_partie_choice(partida);
                                } catch (Exception e) {
                                    System.err.println("Error al procesar el mensaje: " + e.getMessage());
                                }
                            default:
                                System.out.println("Tipo de mensaje desconocido: " + typeMessage);
                                break;
                        }
                    } catch (Exception e) {
                        System.out.println("Error al procesar el mensaje: " + e.getMessage());
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

                        // Si es un Player, crear una nueva partida y asociarla al cliente
                        if ("Player".equals(type)) {
                            UUID partidaId = UUID.randomUUID(); // Generar un nuevo ID para la partida
                            Partida nuevaPartida = new Partida(partidaId, client.getIpAddress(), client.getPort());
                            server.addPartie(nuevaPartida); // Agregar la nueva partida a la lista del servidor
                            client.setPartida(nuevaPartida); // Asocia la partida al cliente
                            System.out.println("Partida creada con ID: " + nuevaPartida.getId_partida());
                        }

                        // Si es un Spectator, enviar la lista de partidas
                        if ("Spectator".equals(type)) {
                            // Llama al método modificado para enviar la lista de partidas
                            send_client_partie_list(client); // Ahora solo se pasa el cliente
                        }


                        break; // Salir del bucle una vez encontrado

                    }
                }
            }
        }

        // Notificar a los observadores sobre la actualización
        server.notifyClientListUpdated();
    }

    private void send_client_partie_list(ClientInfo client) {
        try {
            // Crear una nueva instancia de Parties_Data
            Parties_Data partiesData = new Parties_Data("data_parties"); // Definir el tipo de mensaje

            // Llenar la lista de partidas desde el servidor
            for (Partida partida : server.getParties()) { // Método para obtener la lista de partidas
                partiesData.addPartida(partida);
            }

            // Convertir partiesData a JSON
            String jsonParties = partiesData.toJson();

            // Enviar el JSON al cliente
            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            out.println(jsonParties); // Enviar el JSON al cliente
            System.out.println("Lista de partidas enviada al cliente " + client.getClientId() + ": " + jsonParties);
        } catch (IOException e) {
            System.out.println("Error al enviar la lista de partidas al cliente: " + e.getMessage());
        }
    }

    private void receive_client_partie_choice(Partida partida) {
        // Busca el cliente actual en la lista de clientes
        synchronized (Server.clients) {
            for (ClientInfo client : Server.clients) {
                // Verifica si el ID del cliente coincide
                if (client.getClientId().equals(this.clientId)) {
                    // Actualiza el atributo partida del cliente actual
                    client.setPartida(partida); // Asigna la partida recibida al cliente
                    server.notifyClientListUpdated();

                    // Imprimir el estado actualizado para fines de depuración
                    System.out.println("Cliente " + clientId + " ha seleccionado la partida: ID: "
                            + partida.getId_partida() + ", IP: " + partida.getIp() + ", Puerto: " + partida.getPuerto());
                    break; // Sale del bucle después de encontrar y actualizar al cliente
                }
            }
        }
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
            Data responseData = new Data("data","Mensaje recibido correctamente", data.number * 2, 1);
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
            Data responseData = new Data("data","Mensaje recibido correctamente: " + data.message, data.number * 2, 1);
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


