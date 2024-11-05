package Server.Messaging;

import Game.Ball;
import Game.Partida;
import Game.Partie_Without_Elements;
import Server.Client.ClientInfo;
import Server.Comunication_Data.*;
import Server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * La clase MessageSender es responsable de enviar mensajes desde el servidor a los clientes conectados.
 * Utiliza un objeto ObjectMapper para convertir objetos a formato JSON.
 */
public class MessageSender {
    private final Server server; // El servidor que gestiona las conexiones de los clientes
    private final UUID clientId; // El ID único del cliente al que se envían los mensajes
    private final ObjectMapper objectMapper = new ObjectMapper(); // El ObjectMapper para convertir objetos a JSON

    /**
     * Constructor de la clase MessageSender.
     *
     * @param server El servidor que se utilizará para enviar mensajes.
     * @param clientId El ID del cliente al que se enviarán los mensajes.
     */
    public MessageSender(Server server, UUID clientId) {
        this.server = server; // Inicializa el servidor
        this.clientId = clientId; // Inicializa el ID del cliente
    }

    /**
     * Envía un mensaje de desactivación de bloque a los clientes en la partida específica.
     *
     * @param partidaId El ID de la partida a la que pertenece el bloque desactivado.
     * @param fila      La fila del bloque desactivado.
     * @param columna   La columna del bloque desactivado.
     * @param poder     El poder asociado al bloque (o cualquier otro dato relevante).
     */
    public void sendBreakBlockMessage(UUID partidaId, int fila, int columna, String poder) {
        Bricks_Data bricksData = new Bricks_Data("break_block", columna, fila, poder); // Crea la instancia del objeto
        String jsonMessage = createJson(bricksData); // Convierte a JSON
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId)) {
                    sendMessageToClient(client, jsonMessage); // Envía el mensaje solo a clientes con la partida correcta
                }
            }
        }
    }

    /**
     * Envía los datos del jugador a los clientes que son espectadores en la partida especificada.
     *
     * @param partidaId El ID de la partida a la que pertenece el jugador.
     * @param posx La posición X del jugador.
     * @param posy La posición Y del jugador.
     * @param ancho El ancho del jugador.
     * @param alto La altura del jugador.
     */
    public void sendPlayerDataMessage(UUID partidaId, float posx, float posy, float ancho, float alto) {
        // Crea la instancia del objeto Player_Data con la información del jugador
        Player_Data player_data = new Player_Data("player_data", posx, posy, ancho, alto);

        // Convierte el objeto Player_Data a formato JSON
        String jsonMessage = createJson(player_data);

        // Sincroniza el acceso a la lista de clientes
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente está en la partida correcta y es un espectador
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Spectator")) {
                    // Envía el mensaje al cliente correspondiente
                    sendMessageToClient(client, jsonMessage);
                }
            }
        }
    }

    /**
     * Envía la información de un bloque de poder a los clientes que son jugadores en la partida especificada.
     *
     * @param partidaId El ID de la partida donde se encuentra el bloque de poder.
     * @param fila La fila donde se encuentra el bloque de poder.
     * @param columna La columna donde se encuentra el bloque de poder.
     * @param poder El tipo de poder del bloque.
     */
    public void sendPowerBlockMessage(UUID partidaId, int fila, int columna, String poder) {
        // Crea la instancia del objeto Bricks_Data con la información del bloque de poder
        Bricks_Data bricksData = new Bricks_Data("power_block", columna, fila, poder);

        // Convierte el objeto Bricks_Data a formato JSON
        String jsonMessage = createJson(bricksData);

        // Sincroniza el acceso a la lista de clientes
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente está en la partida correcta y es un jugador
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Player")) {
                    // Envía el mensaje al cliente correspondiente
                    sendMessageToClient(client, jsonMessage);
                    // Registra el mensaje enviado
                    System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage);
                }
            }
        }
    }


    public void sendScoreLevelMessage(UUID partidaId, String nivel, int score) {
        // Crea la instancia del objeto Bricks_Data con la información del bloque de poder
        Score_Level_Data score_level_data = new Score_Level_Data("score_level_data", nivel, score);

        // Convierte el objeto Bricks_Data a formato JSON
        String jsonMessage = createJson(score_level_data);

        // Sincroniza el acceso a la lista de clientes
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente está en la partida correcta y es un jugador
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Player")) {
                    // Envía el mensaje al cliente correspondiente
                    sendMessageToClient(client, jsonMessage);
                    // Registra el mensaje enviado
                    System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage);
                }
            }
        }
    }

    public void sendMatrixBlockMessage(Partida partida) {
        ClientInfo client = server.getClientInfobyID(clientId); // Obtiene la información del cliente
        // Crea la instancia del objeto Bricks_Data con la información del bloque de poder
        Matrix_Brick_Data matrix_brick_data = new Matrix_Brick_Data("brick_matrix", partida.getBloques());
        // Convierte el objeto Bricks_Data a formato JSON
        String jsonMessage = createJson(matrix_brick_data);
        // Convierte el objeto Parties_Data a JSON y lo envía al cliente especificado
        sendMessageToClient(client, jsonMessage);
        System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage);
    }

    /**
     * Envía los datos de las bolas a los clientes que son espectadores en la partida especificada.
     *
     * @param partidaId El ID de la partida a la que pertenecen las bolas.
     * @param balls Un arreglo de objetos Ball que representan las bolas en la partida.
     */
    public void sendBallsDataMessage(UUID partidaId, Ball[] balls) {
        // Crea la instancia del objeto Balls_Data con la información de las bolas
        Balls_Data balls_data = new Balls_Data("balls_data", balls);

        // Convierte el objeto Balls_Data a formato JSON
        String jsonMessage = createJson(balls_data);

        // Sincroniza el acceso a la lista de clientes
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente está en la partida correcta y es un espectador
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Spectator")) {
                    // Envía el mensaje al cliente correspondiente
                    sendMessageToClient(client, jsonMessage);
                    // Registra el mensaje enviado
                    System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage);
                }
            }
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
            //System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + message); // Registra el mensaje enviado
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje al cliente " + client.getClientId() + ": " + e.getMessage()); // Registra el error durante el envío
        }
    }

    /**
     * Envía la lista de partidas disponibles al cliente especificado.
     *
     * @param client El objeto ClientInfo que representa al cliente al que se enviará la lista de partidas.
     */
    public void sendGameListToClientMessage(ClientInfo client) {
    // Crea un objeto Parties_Data para almacenar el mensaje de tipo "data_parties"
        Parties_Data partiesData = new Parties_Data("data_parties");

        // Recorre la lista de partidas en el servidor y las añade al objeto Parties_Data
        server.getParties().forEach(partida -> {
            // Crea una versión simplificada de la partida sin el atributo bloque
            Partie_Without_Elements simplifiedPartida = new Partie_Without_Elements(partida.getId_partida(), partida.getIp(), partida.getPuerto());
            partiesData.addPartida(simplifiedPartida);
        });

        // Convierte el objeto Parties_Data a JSON y lo envía al cliente especificado
        sendMessageToClient(client, partiesData.toJson());
    }

    /**
     * Convierte un objeto en una cadena JSON.
     *
     * @param data El objeto que se desea convertir a formato JSON.
     * @return Una cadena JSON que representa el objeto, o null si ocurre un error durante la conversión.
     */
    private String createJson(Object data) {
        try {
            // Convierte el objeto a JSON usando el ObjectMapper
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // Imprime el stack trace del error y retorna null en caso de fallo
            e.printStackTrace();
            return null;
        }
    }
}
