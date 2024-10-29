package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Observer.*;
import Game.Partida;

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
