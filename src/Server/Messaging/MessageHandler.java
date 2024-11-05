package Server.Messaging;

import Game.Ball;
import Game.Partida;
import Server.Client.ClientInfo;
import Server.Comunication_Data.*;
import Server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final MessageSender messageSender;

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
        this.messageSender = new MessageSender(server, clientId);
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
                case "choice": // Si el tipo es "choice"
                    handleChoiceMessage(jsonNode); // Maneja el mensaje de elección
                    break;
                case "player_data": // Si el tipo es "player_data"
                    handlePlayerMessage(jsonNode); // Maneja el mensaje de elección
                    break;
                case "bricks_data": // Si el tipo es "bricks_data"
                    handleBrickMessage(jsonNode); // Maneja el mensaje de un bloque destruido
                    break;
                case "balls_data": // Si el tipo es "bricks_data"
                    handleBallsDataMessage(jsonNode); // Maneja el mensaje de un bloque destruido
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
                messageSender.sendGameListToClientMessage(clientInfo);
                //sendGameListToClient(clientInfo);
            }
        }
        server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
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
            // Obtener las coordenadas del bloque del mensaje JSON
            float posx = (float) jsonNode.get("posx").asDouble();
            float posy = (float) jsonNode.get("posy").asDouble();
            float ancho = (float) jsonNode.get("ancho").asDouble();
            float alto = (float) jsonNode.get("largo").asDouble();
            // Buscar el cliente asociado al ID
            ClientInfo client = server.getClientById(clientId); // Método para obtener el cliente por ID
            if (client != null && client.getPartida() != null) {
                Partida partida = client.getPartida();
                messageSender.sendPlayerDataMessage(partida.getId_partida(), posx, posy, ancho, alto);
                //sendPlayerDataMessage(partida.getId_partida(), posx, posy, ancho, alto); // Asegúrate de que "poder" sea adecuado
            }
        } catch(Exception e){
                e.printStackTrace();
        }
    }

    /**
     * Maneja el mensaje de elección del cliente para seleccionar un juego.
     *
     * @param jsonNode El nodo JSON que representa el mensaje.
     */
    private void handleBrickMessage(JsonNode jsonNode) {
        try {
            // Obtener las coordenadas del bloque del mensaje JSON
            int fila = jsonNode.get("row").asInt();
            int columna = jsonNode.get("column").asInt();

            // Buscar el cliente asociado al ID
            ClientInfo client = server.getClientById(clientId); // Método para obtener el cliente por ID

            if (client != null && client.getPartida() != null) {
                // Obtener la partida asociada al cliente
                Partida partida = client.getPartida();

                // Desactivar el bloque específico en la partida
                partida.desactivarBloque(fila, columna);

                messageSender.sendPowerBlockMessage(partida.getId_partida(), fila, columna, "poder");

            } else {
                System.out.println("Cliente o partida no encontrados.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBallsDataMessage(JsonNode jsonNode) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convertir el nodo JSON en un objeto Balls_Data
            Balls_Data ballsData = objectMapper.treeToValue(jsonNode, Balls_Data.class);
            // Ahora puedes acceder a las bolas desde ballsData
            Ball[] balls = ballsData.getBalls();
            // Buscar el cliente asociado al ID
            ClientInfo client = server.getClientById(clientId); // Método para obtener el cliente por ID

            if (client != null && client.getPartida() != null ) {
                // Obtener la partida asociada al cliente
                Partida partida = client.getPartida();
                UUID partidaId = partida.getId_partida();
                if (client.getPartida().getId_partida().equals(partidaId) && client.getClientType().equals("Player")){
                    // Actualizar la lista de bolas
                    partida.setBolas(balls);
                    // Enviar el mensaje de desactivación de bloque a los clientes en la misma partida
                    messageSender.sendBallsDataMessage(partida.getId_partida(), balls);

            } else {
                System.out.println("Cliente o partida no encontrados.");
            }
                }
        } catch (Exception e) {
            e.printStackTrace();
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
     * Actualiza la elección del cliente en una partida.
     *
     * @param partida La partida seleccionada por el cliente.
     */
    private void updateClientChoice(Partida partida) {
        synchronized (Server.clients) {
            ClientInfo clientInfo = getClientInfo(); // Obtiene la información del cliente
            clientInfo.setPartida(partida); // Actualiza la partida del cliente
            server.notifyClientListUpdated(); // Notifica que la lista de clientes ha sido actualizada
            //System.out.println("Cliente " + clientId + " ha seleccionado el juego: ID: "
            //        + partida.getId_partida() + ", IP: " + partida.getIp() + ", Puerto: " + partida.getPuerto());
        }
    }

}

