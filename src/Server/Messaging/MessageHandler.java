package Server.Messaging;

import Game.Partida;
import Server.Client.ClientInfo;
import Server.Comunication_Data.Data;
import Server.Comunication_Data.Parties_Data;
import Server.Comunication_Data.Party_Choice_Data;
import Server.Comunication_Data.Register_Data;
import Server.Server;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * MessageHandler handles all client messages: registration, data, and choice.
 */
public class MessageHandler {
    private final Server server;
    private final Socket socket;
    private final UUID clientId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageHandler(Server server, Socket socket, UUID clientId) {
        this.server = server;
        this.socket = socket;
        this.clientId = clientId;
    }

    /**
     * Handles incoming JSON messages from the client.
     * @param messageJson The received message in JSON format.
     */
    public void handleMessage(String messageJson) {
        if (messageJson.isEmpty()) {
            System.out.println("Received an empty message.");
            return;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            String messageType = jsonNode.path("type_message").asText();

            // Route the message to the correct handler based on the type
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
                    System.out.println("Unknown message type: " + messageType);
            }
        } catch (IOException e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Handles the registration message from the client.
     * @param jsonNode The JSON node representing the message.
     */
    private void handleRegistration(JsonNode jsonNode) {
        try {
            Register_Data registerData = objectMapper.treeToValue(jsonNode, Register_Data.class);
            System.out.println("Client ID: " + clientId);
            registerClientType(registerData);
        } catch (IOException e) {
            System.err.println("Error processing registration: " + e.getMessage());
        }
    }

    /**
     * Registers the client type (Player or Spectator) and handles game creation or spectator joining.
     * @param registerData The data from the registration message.
     */
    private void registerClientType(Register_Data registerData) {
        String type = registerData.getType();

        if (!isValidClientType(type)) {
            System.out.println("Invalid client type: " + type);
            return;
        }

        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo();
            clientInfo.setClientType(type);
            System.out.println("Client " + clientId + " registered as " + type.toLowerCase() + ".");

            if ("Player".equals(type)) {
                createNewGameForClient(clientInfo);
            } else {
                sendGameListToClient(clientInfo);
            }
        }

        server.notifyClientListUpdated();
    }

    /**
     * Handles the data message from the client.
     * @param jsonNode The JSON node representing the message.
     */
    private void handleDataMessage(JsonNode jsonNode) {
        try {
            Data data = objectMapper.treeToValue(jsonNode, Data.class);
            processClientData(data);
        } catch (IOException e) {
            System.err.println("Error processing data message: " + e.getMessage());
        }
    }

    /**
     * Handles the client's choice message for selecting a game.
     * @param jsonNode The JSON node representing the message.
     */
    private void handleChoiceMessage(JsonNode jsonNode) {
        try {
            Party_Choice_Data choiceData = objectMapper.treeToValue(jsonNode, Party_Choice_Data.class);
            Partida partida = new Partida(UUID.fromString(choiceData.getId_partida()), choiceData.getIp(), choiceData.getPuerto());
            updateClientChoice(partida);
        } catch (Exception e) {
            System.err.println("Error processing party choice: " + e.getMessage());
        }
    }

    private boolean isValidClientType(String type) {
        return "Player".equals(type) || "Spectator".equals(type);
    }

    private ClientInfo getClientInfo() {
        return Server.clients.stream()
                .filter(client -> client.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Client not found"));
    }

    private void createNewGameForClient(ClientInfo clientInfo) {
        UUID gameId = UUID.randomUUID();
        Partida newGame = new Partida(gameId, clientInfo.getIpAddress(), clientInfo.getPort());
        server.addPartie(newGame);
        clientInfo.setPartida(newGame);
        System.out.println("Game created with ID: " + newGame.getId_partida());
    }

    private void sendGameListToClient(ClientInfo client) {
        Parties_Data partiesData = new Parties_Data("data_parties");
        server.getParties().forEach(partiesData::addPartida);

        sendMessageToClient(client, partiesData.toJson());
    }

    private void processClientData(Data data) {
        System.out.println("Client " + clientId);
        System.out.println("Message received: " + data.message);
        System.out.println("Number received: " + data.number);
        System.out.println("Status received: " + data.status);

        sendResponseToAllClients(data);
    }

    private void updateClientChoice(Partida partida) {
        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo();
            clientInfo.setPartida(partida);
            server.notifyClientListUpdated();

            System.out.println("Client " + clientId + " has selected game: ID: "
                    + partida.getId_partida() + ", IP: " + partida.getIp() + ", Port: " + partida.getPuerto());
        }
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
            Data responseData = new Data("data", "Message received: " + data.message, data.number * 2, 1);
            return objectMapper.writeValueAsString(responseData);
        } catch (IOException e) {
            System.err.println("Error creating JSON response: " + e.getMessage());
            return "{}";
        }
    }

    private void sendMessageToClient(ClientInfo client, String message) {
        try {
            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
            out.println(message);
            System.out.println("Message sent to client " + client.getClientId() + ": " + message);
        } catch (IOException e) {
            System.err.println("Error sending message to client " + client.getClientId() + ": " + e.getMessage());
        }
    }
}