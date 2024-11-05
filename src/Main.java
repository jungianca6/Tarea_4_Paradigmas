import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import Game.Partida;
import Server.Client.ClientInfo;
import Server.Messaging.MessageHandler;
import Server.Messaging.MessageSender;
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
    private JComboBox<String> levelDropdown;
    private JTextField filaField;
    private JTextField columnaField;
    private JTextField puntajeField;
    private JTextArea selectedClientTextArea;
    private JButton startServerButton;
    private ClientInfo selectedClientInfo;

    public Main() {
        this.bloqueFactory = new ConcreteBloqueFactory();
        JFrame frame = new JFrame("BreakOutTEC Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        // Panel principal con fondo blanco
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        frame.setContentPane(mainPanel);

        // Panel para la lista de clientes
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));
        clientPanel.setBackground(Color.WHITE);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setFont(new Font("Roboto", Font.PLAIN, 16));
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientScrollPane.setPreferredSize(new Dimension(330, 450));
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);

        startServerButton = new JButton("Iniciar Servidor");
        startServerButton.addActionListener(e -> startServer());
        styleButton(startServerButton);
        clientPanel.add(startServerButton, BorderLayout.SOUTH);

        mainPanel.add(clientPanel, BorderLayout.WEST);

        // Panel para el cliente seleccionado
        JPanel selectedClientPanel = new JPanel(new BorderLayout());
        selectedClientPanel.setBorder(BorderFactory.createTitledBorder("Cliente Seleccionado"));
        selectedClientPanel.setBackground(Color.WHITE);
        selectedClientTextArea = new JTextArea("Ninguno");
        selectedClientTextArea.setEditable(false);
        selectedClientTextArea.setLineWrap(true);
        selectedClientTextArea.setWrapStyleWord(true);
        selectedClientTextArea.setFont(new Font("Roboto", Font.PLAIN, 16));
        JScrollPane selectedClientScrollPane = new JScrollPane(selectedClientTextArea);
        selectedClientScrollPane.setPreferredSize(new Dimension(330, 100)); // Asegúrate de que ambos paneles tengan el mismo tamaño
        selectedClientPanel.add(selectedClientScrollPane, BorderLayout.CENTER);
        mainPanel.add(selectedClientPanel, BorderLayout.CENTER);

        // Panel para agrupar nivel y puntaje
        JPanel scorePanel = new JPanel(new GridBagLayout());
        scorePanel.setBorder(BorderFactory.createTitledBorder("Nivel y Puntaje"));
        scorePanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        scorePanel.add(new JLabel("Nivel:"), gbc);

        gbc.gridx = 1;
        levelDropdown = new JComboBox<>(new String[]{"Rojo", "Naranja", "Amarillo", "Verde"});
        scorePanel.add(levelDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        scorePanel.add(new JLabel("Puntaje:"), gbc);

        gbc.gridx = 1;
        puntajeField = new JTextField(10);
        scorePanel.add(puntajeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton modifyScoreButton = new JButton("Modificar Puntaje");
        modifyScoreButton.addActionListener(e -> modificarPuntaje());
        styleButton(modifyScoreButton);
        scorePanel.add(modifyScoreButton, gbc);

        // Panel para la creación de bloques
        JPanel blockPanel = new JPanel(new GridBagLayout());
        blockPanel.setBorder(BorderFactory.createTitledBorder("Crear Bloque"));
        blockPanel.setBackground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        blockPanel.add(new JLabel("Tipo de bloque:"), gbc);

        gbc.gridx = 1;
        blockTypeDropdown = new JComboBox<>(new String[]{"Normal", "MasVelocidad", "MenosVelocidad", "RaquetaDoble",
                "RaquetaMitad", "MasBolas", "MasVidas"});
        blockPanel.add(blockTypeDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        blockPanel.add(new JLabel("Fila:"), gbc);

        gbc.gridx = 1;
        filaField = new JTextField(10);
        blockPanel.add(filaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        blockPanel.add(new JLabel("Columna:"), gbc);

        gbc.gridx = 1;
        columnaField = new JTextField(10);
        blockPanel.add(columnaField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton createBlockButton = new JButton("Crear Bloque");
        createBlockButton.addActionListener(e -> crearBloque());
        styleButton(createBlockButton);
        blockPanel.add(createBlockButton, gbc);

        // Agregar los paneles al marco
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        lowerPanel.setBackground(Color.WHITE);
        lowerPanel.add(scorePanel);
        lowerPanel.add(blockPanel);
        mainPanel.add(lowerPanel, BorderLayout.SOUTH);

        // Listener para mostrar el cliente seleccionado
        clientList.addListSelectionListener(e -> mostrarClienteSeleccionado());

        // Estilo de la ventana
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Roboto", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 40)); // Tamaño de los botones
    }

    private void modificarPuntaje() {
        if (selectedClientInfo == null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un cliente antes de crear un bloque.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedClientInfo.getClientType().equals("Spectator")) {
            JOptionPane.showMessageDialog(null, "Este cliente solo es espectador.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String nivel = (String) levelDropdown.getSelectedItem(); // Obtener el nivel seleccionado
        String nuevoPuntajeStr = puntajeField.getText().toLowerCase(); // Obtener el puntaje directamente del campo de texto

        if (nuevoPuntajeStr != null && !nuevoPuntajeStr.trim().isEmpty()) { // Verificar que no esté vacío
            try {
                int nuevoPuntaje = Integer.parseInt(nuevoPuntajeStr); // Convertir a número
                UUID partidaId = selectedClientInfo.getClientId();
                // Aquí puedes agregar la lógica para modificar el puntaje en el servidor o en el modelo
                MessageSender messageSender = new MessageSender(server, selectedClientInfo.getClientId());
                messageSender.sendScoreLevelMessage(partidaId, nivel, nuevoPuntaje);

                JOptionPane.showMessageDialog(null, "Puntaje del nivel " + nivel + " modificado a: " + nuevoPuntaje);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "El puntaje debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "El puntaje no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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

        // Deshabilitar el botón de iniciar servidor
        startServerButton.setEnabled(false);

        server = new Server(ipAddress, port);
        server.addObserver(this); // Registra Main como observador
        new Thread(() -> server.start()).start();
    }

    private void crearBloque() {
        if (selectedClientInfo == null) {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un cliente antes de crear un bloque.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedClientInfo.getClientType().equals("Spectator")) {
            JOptionPane.showMessageDialog(null, "Este cliente solo es espectador.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UUID partidaId = selectedClientInfo.getClientId();
        Partida partida = server.getPartieById(partidaId);

        if (partida == null) {
            JOptionPane.showMessageDialog(null, "La partida del cliente seleccionado no existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tipoBloque = (String) blockTypeDropdown.getSelectedItem();
        tipoBloque = tipoBloque.toLowerCase();  // Convertimos a minúsculas
        String nivel = (String) levelDropdown.getSelectedItem();
        int fila, columna;

        try {
            fila = Integer.parseInt(filaField.getText());
            columna = Integer.parseInt(columnaField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Fila, columna y puntaje deben ser números enteros.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar que la fila y la columna estén en el rango permitido (0 a 7)
        if (fila < 0 || fila > 7 || columna < 0 || columna > 7) {
            JOptionPane.showMessageDialog(null, "Fila y columna deben estar entre 0 y 7.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el bloque está activo o no
        if (!partida.isBloqueActivo(columna, fila)) {
            JOptionPane.showMessageDialog(null, "El bloque en la posición (" + fila + ", " + columna + ") no existe o no está activo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MessageSender messageSender = new MessageSender(server, selectedClientInfo.getClientId());
        // Crear el bloque utilizando la fábrica
        AbstractBloque nuevoBloque = bloqueFactory.crearBloque(tipoBloque, fila, columna);

        // Verificar el tipo de bloque creado usando switch-case
        switch (nuevoBloque.getClass().getSimpleName()) {
            case "ConcreteBloqueNormal":
                JOptionPane.showMessageDialog(null, "Bloque normal creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "N");
                break;
            case "ConcreteBloqueMasVelocidad":
                JOptionPane.showMessageDialog(null, "Bloque de más velocidad creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "A");
                break;
            case "ConcreteBloqueMenosVelocidad":
                JOptionPane.showMessageDialog(null, "Bloque de menos velocidad creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "D");
                break;
            case "ConcreteRaquetaDoble":
                JOptionPane.showMessageDialog(null, "Bloque de raqueta doble creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "L");
                break;
            case "ConcreteRaquetaMitad":
                JOptionPane.showMessageDialog(null, "Bloque de raqueta a mitad creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "S");
                break;
            case "ConcreteBloqueMasVidas":
                JOptionPane.showMessageDialog(null, "Bloque de mas vidas creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "V");
                break;
            case "ConcreteBloqueMasBolas":
                JOptionPane.showMessageDialog(null, "Bloque de mas bolas creado en la posición: (" + fila + ", " + columna + ")", "Bloque Creado", JOptionPane.INFORMATION_MESSAGE);
                messageSender.sendPowerBlockMessage(partidaId, fila, columna, "E");
                break;
            default:
                JOptionPane.showMessageDialog(null, "Tipo de bloque desconocido después de la creación.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    private void mostrarClienteSeleccionado() {
        String selectedClient = clientList.getSelectedValue();
        if (selectedClient != null) {
            String clientId = extractClientId(selectedClient); // Extrae el ID

            // Obtener el cliente por ID
            selectedClientInfo = server.getClientById(UUID.fromString(clientId));
            selectedClientTextArea.setText("Cliente seleccionado:\n" + selectedClient);

        } else {
            selectedClientTextArea.setText("Ninguno");
        }
    }

    private String extractClientId(String selectedClient) {
        // Divide la cadena en líneas
        String[] lines = selectedClient.split("\n");
        // Revisa si hay al menos dos líneas
        if (lines.length > 1) {
            // Supone que la segunda línea contiene el ID, en el formato "ID: <valor>"
            String idLine = lines[1]; // La línea que contiene el ID
            // Divide la línea por ": " y devuelve el segundo elemento (el ID)
            return idLine.split(": ")[1].trim(); // Usa trim() para eliminar espacios en blanco
        }
        return null; // Devuelve null si no se puede encontrar el ID
    }

    @Override
    public void update(List<ClientInfo> clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear(); // Limpia el modelo
            for (ClientInfo client : clients) {
                String clientInfo = String.format(
                        "Tipo: %s\n" + "ID: %s\n" + "IP: %s\n" + "Puerto: %d",
                        client.getClientType(),
                        client.getClientId(),
                        client.getIpAddress(),
                        client.getPort()
                );
                if (client.getPartida() != null) {
                    clientInfo += "\nPartida: " + client.getPartida().getId_partida();
                }
                clientListModel.addElement(clientInfo); // Agrega la información del cliente al modelo
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}