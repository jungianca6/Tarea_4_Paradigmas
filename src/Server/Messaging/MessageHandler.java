package Server.Messaging;

import Game.Partida;
import Server.Client.ClientInfo;
import Server.Comunication_Data.*;
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

    /**
     * Constructor de MessageHandler.
     *
     * @param server La instancia del servidor que maneja la comunicación.
     * @param socket El socket del cliente para la comunicación.
     * @param clientId El identificador único del cliente.
     */
    public MessageHandler(Server server, Socket socket, UUID clientId) {
        this.server = server; // Inicializa la instancia del servidor
        this.socket = socket; // Inicializa el socket del cliente
        this.clientId = clientId; // Inicializa el identificador único del cliente
    }

    /**
     * Maneja los mensajes JSON entrantes del cliente.
     *
     * @param messageJson El mensaje recibido en formato JSON.
     */
    public void handleMessage(String messageJson) {
        // Verifica si el mensaje está vacío
        if (messageJson.isEmpty()) {
            System.out.println("Se recibió un mensaje vacío."); // Notifica que el mensaje está vacío
            return; // Sale del método
        }

        try {
            // Convierte el mensaje JSON en un nodo
            JsonNode jsonNode = objectMapper.readTree(messageJson);
            // Obtiene el tipo de mensaje del nodo
            String messageType = jsonNode.path("type_message").asText();

            // Dirige el mensaje al manejador correcto según el tipo
            switch (messageType) {
                case "register": // Si el tipo es "register"
                    handleRegistration(jsonNode); // Maneja el registro
                    break;
                case "data": // Si el tipo es "data"
                    handleDataMessage(jsonNode); // Maneja el mensaje de datos
                    break;
                case "choice": // Si el tipo es "choice"
                    handleChoiceMessage(jsonNode); // Maneja el mensaje de elección
                    break;
                case "player_data": // Si el tipo es "player_data"
                    handlePlayerMessage(jsonNode); // Maneja el mensaje de elección
                    break;
                case "bricks_data": // Si el tipo es "bricks_data"
                    handleBrickMessage(jsonNode); // Maneja el mensaje de un bloque destruido
                    break;
                default: // Si el tipo de mensaje es desconocido
                    System.out.println("Tipo de mensaje desconocido: " + messageType);
            }
        } catch (IOException e) {
            // Maneja excepciones al procesar el mensaje
            System.err.println("Error procesando el mensaje: " + e.getMessage());
        }
    }

    /**
     * Maneja el mensaje de registro del cliente.
     *
     * @param jsonNode El nodo JSON que representa el mensaje.
     */
    private void handleRegistration(JsonNode jsonNode) {
        try {
            // Convierte el nodo JSON en un objeto Register_Data
            Register_Data registerData = objectMapper.treeToValue(jsonNode, Register_Data.class);
            System.out.println("ID del cliente: " + clientId); // Muestra el ID del cliente
            // Registra el tipo de cliente (Player o Spectator)
            registerClientType(registerData);
        } catch (IOException e) {
            // Maneja excepciones al procesar el registro
            System.err.println("Error procesando el registro: " + e.getMessage());
        }
    }

    /**
     * Registra el tipo de cliente (Player o Spectator) y maneja la creación de juegos o la unión de espectadores.
     *
     * @param registerData Los datos del mensaje de registro.
     */
    private void registerClientType(Register_Data registerData) {
        String type = registerData.getType(); // Obtiene el tipo de cliente

        // Verifica si el tipo de cliente es válido
        if (!isValidClientType(type)) {
            System.out.println("Tipo de cliente inválido: " + type);
            return; // Sale del método si el tipo es inválido
        }

        synchronized (Server.clients) { // Sincroniza el acceso a la lista de clientes
            ClientInfo clientInfo = getClientInfo(); // Obtiene la información del cliente
            clientInfo.setClientType(type); // Establece el tipo de cliente
            System.out.println("Cliente " + clientId + " registrado como " + type.toLowerCase() + ".");

            // Si es un jugador, crea un nuevo juego
            if ("Player".equals(type)) {
                createNewGameForClient(clientInfo);
            } else { // Si es un espectador, envía la lista de juegos disponibles
                sendGameListToClient(clientInfo);
            }
        }
        server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
    }

    /**
     * Maneja el mensaje de datos del cliente.
     *
     * @param jsonNode El nodo JSON que representa el mensaje.
     */
    private void handleDataMessage(JsonNode jsonNode) {
        try {
            // Convierte el nodo JSON en un objeto Data
            Data data = objectMapper.treeToValue(jsonNode, Data.class);
            processClientData(data); // Procesa los datos del cliente
        } catch (IOException e) {
            // Maneja excepciones al procesar el mensaje de datos
            System.err.println("Error procesando el mensaje de datos: " + e.getMessage());
        }
    }

    /**
     * Maneja el mensaje de elección del cliente para seleccionar un juego.
     *
     * @param jsonNode El nodo JSON que representa el mensaje.
     */
    private void handleChoiceMessage(JsonNode jsonNode) {
        try {
            // Convierte el nodo JSON en un objeto Party_Choice_Data
            Party_Choice_Data choiceData = objectMapper.treeToValue(jsonNode, Party_Choice_Data.class);
            // Crea una nueva partida con la información de elección
            Partida partida = new Partida(UUID.fromString(choiceData.getId_partida()), choiceData.getIp(), choiceData.getPuerto());
            updateClientChoice(partida); // Actualiza la elección del cliente
        } catch (Exception e) {
            // Maneja excepciones al procesar la elección de la partida
            System.err.println("Error procesando la elección de la partida: " + e.getMessage());
        }
    }

    /**
     * Maneja el mensaje de elección del cliente para seleccionar un juego.
     *
     * @param jsonNode El nodo JSON que representa el mensaje.
     */
    private void handlePlayerMessage(JsonNode jsonNode) {
        try {
            // Convierte el nodo JSON en un objeto Party_Choice_Data
            Party_Choice_Data choiceData = objectMapper.treeToValue(jsonNode, Party_Choice_Data.class);
            // Crea una nueva partida con la información de elección
            Partida partida = new Partida(UUID.fromString(choiceData.getId_partida()), choiceData.getIp(), choiceData.getPuerto());
            updateClientChoice(partida); // Actualiza la elección del cliente
        } catch (Exception e) {
            // Maneja excepciones al procesar la elección de la partida
            System.err.println("Error procesando la elección de la partida: " + e.getMessage());
        }
    }

    /**
     * Verifica si el tipo de cliente es válido (Player o Spectator).
     *
     * @param type El tipo de cliente.
     * @return true si el tipo es válido, false de lo contrario.
     */
    private boolean isValidClientType(String type) {
        return "Player".equals(type) || "Spectator".equals(type);
    }


    /**
     * Obtiene la información del cliente correspondiente al ID del cliente.
     *
     * @return El objeto ClientInfo asociado al cliente.
     * @throws IllegalStateException Si no se encuentra el cliente.
     */
    private ClientInfo getClientInfo() {
        return Server.clients.stream()
                .filter(client -> client.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));
    }

    /**
     * Crea un nuevo juego para el cliente.
     *
     * @param clientInfo La información del cliente que está creando el juego.
     */
    private void createNewGameForClient(ClientInfo clientInfo) {
        UUID gameId = clientInfo.getClientId(); // Genera un ID único para la nueva partida
        Partida newGame = new Partida(gameId, clientInfo.getIpAddress(), clientInfo.getPort()); // Crea la nueva partida
        server.addPartie(newGame); // Agrega la partida al servidor
        clientInfo.setPartida(newGame); // Asocia la partida al cliente
        System.out.println("Juego creado con ID: " + newGame.getId_partida());
    }

    /**
     * Envía la lista de juegos disponibles al cliente.
     *
     * @param client La información del cliente al que se enviará la lista.
     */
    private void sendGameListToClient(ClientInfo client) {
        Parties_Data partiesData = new Parties_Data("data_parties"); // Crea un objeto para almacenar la lista de partidas
        server.getParties().forEach(partiesData::addPartida); // Agrega las partidas al objeto de datos

        sendMessageToClient(client, partiesData.toJson()); // Envía la lista de partidas al cliente
    }

    /**
     * Procesa los datos recibidos del cliente.
     *
     * @param data Los datos enviados por el cliente.
     */
    private void processClientData(Data data) {
        System.out.println("Cliente " + clientId);
        System.out.println("Mensaje recibido: " + data.message);
        System.out.println("Número recibido: " + data.number);
        System.out.println("Estado recibido: " + data.status);

        sendResponseToAllClients(data); // Envía la respuesta a todos los clientes
    }

    /**
     * Actualiza la elección del cliente en una partida.
     *
     * @param partida La partida seleccionada por el cliente.
     */
    private void updateClientChoice(Partida partida) {
        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo(); // Obtiene la información del cliente
            clientInfo.setPartida(partida); // Actualiza la partida del cliente
            server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada

            System.out.println("Cliente " + clientId + " ha seleccionado el juego: ID: "
                    + partida.getId_partida() + ", IP: " + partida.getIp() + ", Puerto: " + partida.getPuerto());
        }
    }

    /**
     * Envía una respuesta a todos los clientes, excepto al que envió el mensaje.
     *
     * @param data Los datos a enviar como respuesta.
     */
    private void sendResponseToAllClients(Data data) {
        String jsonResponse = createResponseJson(data); // Crea la respuesta en formato JSON

        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                if (!client.getSocket().equals(socket)) { // No envía respuesta al cliente que envió el mensaje
                    sendMessageToClient(client, jsonResponse);
                }
            }
        }
    }

    /**
     * Envía un mensaje de finalización de juego a todos los clientes.
     */
    private void sendGameEndMessage() {
        Game_end_Data gameEndData = new Game_end_Data(); // Crea una instancia con el tipo de mensaje "end"
        String jsonMessage = createGameEndResponseJson(gameEndData); // Convierte a JSON

        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                sendMessageToClient(client, jsonMessage); // Envía el mensaje a todos los clientes
            }
        }
    }

    /**
     * Crea un JSON de respuesta basado en el mensaje de finalización del juego.
     *
     * @param gameEndData Los datos del juego que se utilizarán en la respuesta.
     * @return La respuesta en formato JSON.
     */
    private String createGameEndResponseJson(Game_end_Data gameEndData) {
        try {
            return objectMapper.writeValueAsString(gameEndData); // Convierte a JSON
        } catch (IOException e) {
            System.err.println("Error al crear la respuesta JSON de finalización de juego: " + e.getMessage());
            return "{}"; // Retorna un JSON vacío en caso de error
        }
    }


    /**
     * Crea un JSON de respuesta basado en los datos recibidos.
     *
     * @param data Los datos del cliente que se utilizarán en la respuesta.
     * @return La respuesta en formato JSON.
     */
    private String createResponseJson(Data data) {
        try {
            Data responseData = new Data("data", "Mensaje recibido: " + data.message, data.number * 2, 1); // Prepara los datos de respuesta
            return objectMapper.writeValueAsString(responseData); // Convierte a JSON
        } catch (IOException e) {
            System.err.println("Error al crear la respuesta JSON: " + e.getMessage());
            return "{}"; // Retorna un JSON vacío en caso de error
        }
    }

    /**
     * Envía un mensaje JSON al cliente.
     *
     * @param client La información del cliente al que se enviará el mensaje.
     * @param message El mensaje a enviar en formato JSON.
     */
    private void sendMessageToClient(ClientInfo client, String message) {
        try {
            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true); // Prepara el flujo de salida
            out.println(message); // Envía el mensaje
            System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + message); // Registra el mensaje enviado
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje al cliente " + client.getClientId() + ": " + e.getMessage()); // Registra el error durante el envío
        }
    }
}