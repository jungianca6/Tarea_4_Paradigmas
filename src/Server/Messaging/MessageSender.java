package Server.Messaging;

import Game.Ball;
import Server.Client.ClientInfo;
import Server.Comunication_Data.*;
import Server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class MessageSender {
    private final Server server;
    private final UUID clientId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageSender(Server server, UUID clientId) {
        this.clientId = clientId;
        this.server = server;
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

    public void sendPlayerDataMessage(UUID partidaId, float posx, float posy, float ancho, float alto) {
        Player_Data player_data = new Player_Data("player_data", posx, posy, ancho, alto); // Crea la instancia del objeto
        String jsonMessage = createJson(player_data); // Convierte a JSON
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Spectator")) {
                    sendMessageToClient(client, jsonMessage); // Envía el mensaje solo a clientes con la partida correcta
                }
            }
        }
    }

    public void sendPowerBlockMessage(UUID partidaId, int fila, int columna, String poder) {
        Bricks_Data bricksData = new Bricks_Data("power_block", columna, fila, poder); // Crea la instancia del objeto
        String jsonMessage = createJson(bricksData); // Convierte a JSON
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente tiene la partida asociada con el ID especificado
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Player")) {
                    sendMessageToClient(client, jsonMessage); // Envía el mensaje solo a clientes con la partida correcta
                    System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage); // Registra el mensaje enviado
                }
            }
        }
    }

    public void sendBallsDataMessage(UUID partidaId, Ball[] balls) {
        Balls_Data balls_data = new Balls_Data("balls_data", balls); // Crea la instancia del objeto
        String jsonMessage = createJson(balls_data); // Convierte a JSON
        synchronized (server.clients) {
            for (ClientInfo client : server.clients) {
                // Verifica si el cliente tiene la partida asociada con el ID especificado
                if (client.getPartida() != null && client.getPartida().getId_partida().equals(partidaId) &&
                        client.getClientType().equals("Spectator")) {
                    sendMessageToClient(client, jsonMessage); // Envía el mensaje solo a clientes con la partida correcta
                    System.out.println("Mensaje enviado al cliente " + client.getClientId() + ": " + jsonMessage); // Registra el mensaje enviado
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

    public void sendGameListToClientMessage(ClientInfo client) {
        Parties_Data partiesData = new Parties_Data("data_parties"); // Crea un objeto para almacenar la lista de partidas
        server.getParties().forEach(partiesData::addPartida); // Agrega las partidas al objeto de datos

        sendMessageToClient(client, partiesData.toJson()); // Envía la lista de partidas al cliente
    }


    private String createJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data); // Convierte el objeto a JSON
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null; // O maneja el error como prefieras
        }
    }
}
