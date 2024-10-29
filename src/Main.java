import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import Server.Client.ClientInfo;
import org.ini4j.Ini;
import Server.*;
import Observer.Observer;
import Fabrica_Bloques.*;
import Bloques.*;


public class Main implements Observer {
    private ConcreteBloqueFactory bloqueFactory;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;
    private Server server;
    private JComboBox<String> blockTypeDropdown;
    private JComboBox<String> colorDropdown;
    private JComboBox<Integer> levelDropdown;
    private JTextField scoreField;
    private JLabel selectedClientLabel; // JLabel para mostrar el cliente seleccionado

    public Main() {
        // Inicializar la fábrica de bloques
        this.bloqueFactory = new ConcreteBloqueFactory();
        JFrame frame = new JFrame("BreakOutTEC Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espaciado entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Botón para iniciar servidor
        JButton startServerButton = new JButton("Iniciar Servidor");
        startServerButton.addActionListener(e -> startServer());

        // Lista de clientes conectados
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientScrollPane.setPreferredSize(new Dimension(300, 200)); // Ajustar tamaño del JScrollPane

        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(new JLabel("Clientes conectados:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(clientScrollPane, gbc); // Añadir JList dentro de JScrollPane

        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(startServerButton, gbc);

        // Menú desplegable para seleccionar el tipo de bloque
        JLabel blockTypeLabel = new JLabel("Tipo de bloque:");
        blockTypeDropdown = new JComboBox<>(new String[]{"Normal", "MasVelocidad", "MenosVelocidad"});

        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(blockTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        frame.add(blockTypeDropdown, gbc);

        // Menú desplegable para seleccionar el color
        JLabel colorLabel = new JLabel("Color:");
        colorDropdown = new JComboBox<>(new String[]{"Rojo", "Azul", "Amarillo", "Naranja", "Morado"});

        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        frame.add(colorDropdown, gbc);

        // Menú desplegable para seleccionar el nivel
        JLabel levelLabel = new JLabel("Nivel:");
        levelDropdown = new JComboBox<>(new Integer[]{1, 2, 3, 4});

        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(levelLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        frame.add(levelDropdown, gbc);

        // Campo para ingresar el puntaje del bloque
        JLabel scoreLabel = new JLabel("Puntaje:");
        scoreField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 5;
        frame.add(scoreLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        frame.add(scoreField, gbc);

        // Botón para crear el bloque
        JButton createBlockButton = new JButton("Crear Bloque");
        createBlockButton.addActionListener(e -> crearBloque());

        gbc.gridx = 1;
        gbc.gridy = 6;
        frame.add(createBlockButton, gbc);

        // JLabel para mostrar el cliente seleccionado
        selectedClientLabel = new JLabel("Cliente seleccionado: Ninguno");
        gbc.gridx = 0;
        gbc.gridy = 7;
        frame.add(selectedClientLabel, gbc);

        // Agregar listener para actualizar la etiqueta cuando se selecciona un cliente
        clientList.addListSelectionListener(e -> mostrarClienteSeleccionado());

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

    private void crearBloque() {
        // Obtener los valores ingresados
        String tipoBloque = (String) blockTypeDropdown.getSelectedItem();
        String color = (String) colorDropdown.getSelectedItem();
        int nivel = (Integer) levelDropdown.getSelectedItem();
        int puntaje;

        try {
            puntaje = Integer.parseInt(scoreField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El puntaje debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear el bloque utilizando la fábrica
        AbstractBloque bloque = bloqueFactory.crearBloque(tipoBloque, color, puntaje, nivel);
        JOptionPane.showMessageDialog(null, "Bloque creado: " + bloque, "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarClienteSeleccionado() {
        String selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            selectedClientLabel.setText("Cliente seleccionado: " + selectedClient);
        } else {
            selectedClientLabel.setText("Cliente seleccionado: Ninguno");
        }
    }

    @Override
    public void update(List<ClientInfo> clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear(); // Limpia el modelo
            for (ClientInfo client : clients) {
                // Solo añadir clientes de tipo "Player" o "Spectator"
                String clientInfo = "Tipo: " + client.getClientType() + ", IP: " + client.getIpAddress() + ", Puerto: " + client.getPort();
                if (client.getPartida() != null) { // Verificar si el cliente tiene una partida asociada
                    clientInfo += ", Partida ID: " + client.getPartida().getId_partida(); // Agregar ID de partida
                }
                clientListModel.addElement(clientInfo); // Añadir información del cliente
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}