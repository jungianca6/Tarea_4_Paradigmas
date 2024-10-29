import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.ini4j.Ini;
import Server.*;
import Observer.Observer;

public class Main implements Observer {
    private Server server;
    private JComboBox<String> clientListDropdown;
    private DefaultComboBoxModel<String> clientListModel;

    public Main() {
        JFrame frame = new JFrame("BreakOutTEC Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new FlowLayout());

        JButton startServerButton = new JButton("Iniciar Servidor");
        startServerButton.addActionListener(e -> startServer());

        clientListModel = new DefaultComboBoxModel<>();
        clientListDropdown = new JComboBox<>(clientListModel);
        frame.add(new JLabel("Clientes conectados:"));
        frame.add(clientListDropdown);
        frame.add(startServerButton);
        frame.setVisible(true);
    }

    private void startServer() {
        String configFilePath = "src/config.ini";
        String ipAddress;
        int port;

        try {
            Ini ini = new Ini(new File(configFilePath));
            ipAddress = ini.get("Settings", "ip_address");
            port = Integer.parseInt(ini.get("Settings", "port"));
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de configuración: " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.out.println("Error al convertir el puerto: " + e.getMessage());
            return;
        }

        server = new Server(ipAddress, port);
        server.addObserver(this); // Registra Main como observador
        new Thread(() -> server.start()).start();
    }

    @Override
    public void update(List<ClientInfo> clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.removeAllElements(); // Limpia el modelo
            for (ClientInfo client : clients) {
                clientListModel.addElement("Tipo: " + client.getClientType() + ", IP: " + client.getIpAddress() + ", Puerto: " + client.getPort());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}