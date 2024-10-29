package Server;

import Server.Comunication_Data.Data;
import Server.Comunication_Data.Parties_Data;
import Server.Comunication_Data.Party_Choice_Data;
import Server.Comunication_Data.Register_Data;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;
import java.util.UUID;
import Game.Partida;

class ClientHandler implements Runnable {
    private final Socket socket;
    private final UUID clientId; // Usar UUID como ID del cliente
    private final Server server;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID();
        registerClient();
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            String messageJson;
            while ((messageJson = input.readLine()) != null) {
                handleClientMessage(messageJson);
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente " + clientId + ": " + e.getMessage());
        } finally {
            disconnectClient();
        }
    }

    private void registerClient() {
        synchronized (Server.clients) {
            Server.clients.add(new ClientInfo(socket, clientId, socket.getInetAddress().getHostAddress(), socket.getPort()));
            server.notifyClientListUpdated(); // Notificar a los observadores
        }
    }

    private void handleClientMessage(String messageJson) {
        if (messageJson.isEmpty()) {
            System.out.println("El mensaje recibido está vacío.");
            return;
        }

        System.out.println("Cliente conectado: " + clientId + " desde " + socket.getInetAddress() + ":" + socket.getPort());
        System.out.println("Mensaje recibido: " + messageJson);

        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String messageType = jsonNode.path("type_message").asText();

            switch (messageType) {
                case "register":
                    handleRegistration(jsonNode);
                    break;
                case "data":
                    handleDataMessage(jsonNode);
                    break;
                case "choice":
                    handleChoiceMessage(jsonNode);
                    break;
                default:
                    System.out.println("Tipo de mensaje desconocido: " + messageType);
            }
        } catch (IOException e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
        }
    }

    private void handleRegistration(JsonNode jsonNode) {
        try {
            Register_Data registerData = objectMapper.treeToValue(jsonNode, Register_Data.class);
            System.out.println("Cliente ID: " + clientId);
            registerClientType(registerData);
        } catch (IOException e) {
            System.err.println("Error al procesar el registro: " + e.getMessage());
        }
    }

    private void registerClientType(Register_Data registerData) {
        String type = registerData.getType();

        if (!isValidClientType(type)) {
            System.out.println("Tipo de cliente inválido: " + type);
            return;
        }

        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo();
            clientInfo.setClientType(type);
            System.out.println("Cliente " + clientId + " registrado como " + type.toLowerCase() + ".");

            if ("Player".equals(type)) {
                createNewGameForClient(clientInfo);
            } else {
                sendGameListToClient(clientInfo);
            }
        }

        server.notifyClientListUpdated();
    }

    private boolean isValidClientType(String type) {
        return "Player".equals(type) || "Spectator".equals(type);
    }

    private ClientInfo getClientInfo() {
        return Server.clients.stream()
                .filter(client -> client.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));
    }

    private void createNewGameForClient(ClientInfo clientInfo) {
        UUID gameId = UUID.randomUUID();
        Partida newGame = new Partida(gameId, clientInfo.getIpAddress(), clientInfo.getPort());
        server.addPartie(newGame);
        clientInfo.setPartida(newGame);
        System.out.println("Partida creada con ID: " + newGame.getId_partida());
    }

    private void sendGameListToClient(ClientInfo client) {
        Parties_Data partiesData = new Parties_Data("data_parties");
        server.getParties().forEach(partiesData::addPartida);

        sendMessageToClient(client, partiesData.toJson());
    }

    private void handleDataMessage(JsonNode jsonNode) {
        try {
            Data data = objectMapper.treeToValue(jsonNode, Data.class);
            processClientData(data);
        } catch (IOException e) {
            System.err.println("Error al procesar el mensaje de datos: " + e.getMessage());
        }
    }

    private void processClientData(Data data) {
        System.out.println("Cliente " + clientId);
        System.out.println("Mensaje recibido: " + data.message);
        System.out.println("Número recibido: " + data.number);
        System.out.println("Estado recibido: " + data.status);

        sendResponseToAllClients(data);
    }

    private void handleChoiceMessage(JsonNode jsonNode) {
        try {
            Party_Choice_Data choiceData = objectMapper.treeToValue(jsonNode, Party_Choice_Data.class);
            Partida partida = new Partida(UUID.fromString(choiceData.getId_partida()), choiceData.getIp(), choiceData.getPuerto());
            updateClientChoice(partida);
        } catch (Exception e) {
            System.err.println("Error al procesar la elección de partida: " + e.getMessage());
        }
    }

    private void updateClientChoice(Partida partida) {
        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo();
            clientInfo.setPartida(partida);
            server.notifyClientListUpdated();

            System.out.println("Cliente " + clientId + " ha seleccionado la partida: ID: "
                    + partida.getId_partida() + ", IP: " + partida.getIp() + ", Puerto: " + partida.getPuerto());
        }
    }

    private void disconnectClient() {
        synchronized (server.clients) {
            server.clients.removeIf(client -> client.getSocket().equals(socket));
            server.notifyClientListUpdated(); // Notificar a los observadores
            printConnectedClients(); // Imprimir la lista de clientes al desconectarse
        }
        closeSocket();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket del cliente " + clientId + ": " + e.getMessage());
        }
    }

    private void printConnectedClients() {
        System.out.println("Clientes conectados:");
        synchronized (server.clients) {
            server.clients.forEach(client -> System.out.println("ID: " + client.getClientId() + ", IP: " + client.getIpAddress() + ", Puerto: " + client.getPort()));
        }
        System.out.println("Total de clientes conectados: " + server.clients.size());
    }

    private void sendResponseToAllClients(Data data) {
        String jsonResponse = createResponseJson(data);

        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                if (!client.getSocket().equals(socket)) {
                    sendMessageToClient(client, jsonResponse);
                }
            }
        }
    }

    private String createResponseJson(Data data) {
        try {
            Data responseData = new Data("data", "Mensaje recibido correctamente: " + data.message, data.number * 2, 1);
            return objectMapper.writeValueAsString(responseData);
        } catch (IOException e) {
            System.err.println("Error al crear la respuesta JSON: " + e.getMessage());
            return "{}";
        }
    }

    private void sendMessageToClient(ClientInfo client, String message) {
        try {
            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            out.println(message);
            System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + message);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje al cliente " + client.getClientId() + ": " + e.getMessage());
        }
    }
}

