import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import Server.*; // Asegúrate de que la ruta de tu paquete sea correcta
import org.ini4j.Ini; // Asegúrate de tener la biblioteca ini4j en tu proyecto

public class Main {
    private Server server; // Cambié a minúscula 's' para seguir la convención de nomenclatura en Java

    public Main() {

        // Configuración de la interfaz gráfica
        JFrame frame = new JFrame("Mi Aplicación");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JButton startServerButton = new JButton("Iniciar Servidor");
        startServerButton.addActionListener(e -> startServer());

        frame.add(startServerButton);
        frame.setVisible(true);
    }

    private void startServer() {
        String configFilePath = "src/config.ini"; // Ruta del archivo de configuración
        String ipAddress = "";
        int port = 0;

        // Cargar la configuración desde el archivo INI
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

        server = new Server(ipAddress, port); // Inicializa el servidor con los parámetros leídos
        new Thread(() -> server.start()).start(); // Inicia el servidor en un nuevo hilo
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new); // Inicia la interfaz gráfica en el hilo de eventos
    }
}