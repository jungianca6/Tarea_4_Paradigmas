package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private String ipAddress; // Atributo para la dirección IP
    private int port;         // Atributo para el puerto

    // Constructor que recibe la dirección IP y el puerto
    public Server(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            System.out.println("Servidor escuchando en el puerto " + this.port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    new Thread(new ClientHandler(clientSocket)).start(); // Manejar cada cliente en un hilo separado
                } catch (IOException e) {
                    System.out.println("Error al aceptar la conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
