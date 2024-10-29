package Server.Client;


import Server.Connection.ConnectionManager;
import Server.Messaging.MessageHandler;
import Server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * ClientHandler maneja la comunicación y el ciclo de vida de un cliente conectado.
 * Registra al cliente, procesa mensajes utilizando MessageHandler y gestiona la desconexión del cliente.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;         // Socket de la conexión del cliente
    private final UUID clientId;         // Identificador único del cliente
    private final Server server;         // Instancia del servidor que maneja la conexión

    /**
     * Constructor de ClientHandler.
     *
     * @param socket El socket del cliente conectado.
     * @param server La instancia del servidor que gestiona al cliente.
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;              // Asignación del socket
        this.server = server;              // Asignación de la instancia del servidor
        this.clientId = UUID.randomUUID(); // Generación de un ID único para el cliente
        new ClientRegistration(server, socket, clientId).registerClient(); // Registro del cliente
    }

    /**
     * Método que se ejecuta al iniciar el hilo.
     * Escucha continuamente los mensajes del cliente y los procesa.
     */
    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String messageJson;  // Variable para almacenar mensajes en formato JSON
            MessageHandler messageHandler = new MessageHandler(server, socket, clientId); // Manejador de mensajes

            // Escucha continuamente los mensajes del cliente
            while ((messageJson = input.readLine()) != null) {
                messageHandler.handleMessage(messageJson); // Procesa el mensaje
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente " + clientId + ": " + e.getMessage());
        } finally {
            // Maneja la desconexión del cliente cuando finaliza la comunicación
            new ConnectionManager(server, socket).disconnectClient();
        }
    }
}