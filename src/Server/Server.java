package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Observer.*;
import Game.Partida;
import Server.Client.ClientHandler;
import Server.Client.ClientInfo;
import java.util.UUID;

public class Server implements Observable{
    private String ipAddress; // Atributo para la dirección IP
    private int port;         // Atributo para el puerto
    private List<Observer> observers = new ArrayList<>(); // Lista de observadores
    public static final List<ClientInfo> clients = new ArrayList<>(); // Lista de clientes conectados
    public static List<Partida> parties = new ArrayList<>(); // Lista de partidas disponibles


    // Constructor que recibe la dirección IP y el puerto
    public Server(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado en " + ipAddress + ":" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public void addPartie(Partida partida) {
        synchronized (this.parties) {
            this.parties.add(partida);
        }
    }

    public ClientInfo getClientById(UUID clientId) {
        synchronized (clients) {
            for (ClientInfo client : clients) {
                if (client.getClientId().equals(clientId)) {
                    return client;
                }
            }
        }
        return null; // Retorna null si no se encuentra el cliente
    }

    public Partida getPartieById(UUID partidaId) {
        synchronized (parties) {
            for (Partida partida : parties) {
                if (partida.getId_partida().equals(partidaId)) {
                    return partida;
                }
            }
        }
        return null; // Retorna null si no se encuentra la partida
    }

    /**
     * Obtiene la información del cliente correspondiente al ID del cliente.
     *
     * @param clientId El UUID del cliente que se desea buscar.
     * @return El objeto ClientInfo asociado al cliente.
     * @throws IllegalStateException Si no se encuentra el cliente.
     */
    public ClientInfo getClientInfobyID(UUID clientId) {
        return Server.clients.stream()
                .filter(client -> client.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));
    }


    public List<Partida> getParties() {
        return this.parties;
    }


    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(clients);
        }
    }

    // Método para notificar a los observadores sobre la lista de clientes
    public void notifyClientListUpdated() {
        notifyObservers(); // Llama a notifyObservers para actualizar la lista
    }
}