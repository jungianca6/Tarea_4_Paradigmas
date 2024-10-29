package Observer;

import Server.ClientInfo;
import java.util.List;
import Game.Partida;

// Interfaz del observador (Observer)
public interface Observer {
    void update(List<ClientInfo> clients); // Método que será llamado por el observable

}