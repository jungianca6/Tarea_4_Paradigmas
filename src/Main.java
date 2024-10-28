import javax.swing.*;
import java.awt.*;
import Server.*; // Asegúrate de que la ruta de tu paquete sea correcta

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
        server = new Server(); // Inicializa el servidor
        new Thread(() -> server.start()).start(); // Inicia el servidor en un nuevo hilo
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new); // Inicia la interfaz gráfica en el hilo de eventos
    }
}