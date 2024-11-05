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
    private JButton startServerButton; // Botón de iniciar servidor
    private ClientInfo selectedClientInfo; // Almacena la información del cliente seleccionado

    public Main() {


        this.bloqueFactory = new ConcreteBloqueFactory();
        JFrame frame = new JFrame("BreakOutTEC Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500); // Tamaño de ventana ajustado
        frame.setLayout(new BorderLayout());

        // Panel para la lista de clientes
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientScrollPane.setPreferredSize(new Dimension(300, 450));
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);

        startServerButton = new JButton("Iniciar Servidor");
        startServerButton.addActionListener(e -> startServer());
        clientPanel.add(startServerButton, BorderLayout.SOUTH);

        frame.add(clientPanel, BorderLayout.WEST);

        // Panel para el cliente seleccionado
        JPanel selectedClientPanel = new JPanel(new BorderLayout());
        selectedClientPanel.setBorder(BorderFactory.createTitledBorder("Cliente Seleccionado"));
        selectedClientTextArea = new JTextArea("Ninguno");
        selectedClientTextArea.setEditable(false);
        selectedClientTextArea.setLineWrap(true);
        selectedClientTextArea.setWrapStyleWord(true);
        JScrollPane selectedClientScrollPane = new JScrollPane(selectedClientTextArea);
        selectedClientScrollPane.setPreferredSize(new Dimension(300, 100));
        selectedClientPanel.add(selectedClientScrollPane, BorderLayout.CENTER);
        frame.add(selectedClientPanel, BorderLayout.CENTER);

        // Panel para agrupar nivel y puntaje
        JPanel scorePanel = new JPanel(new GridBagLayout());
        scorePanel.setBorder(BorderFactory.createTitledBorder("Nivel y Puntaje"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10); // Espaciado ajustado

        // Nivel
        gbc.gridx = 0;
        gbc.gridy = 0;
        scorePanel.add(new JLabel("Nivel:"), gbc);

        gbc.gridx = 1;
        levelDropdown = new JComboBox<>(new String[]{"Rojo", "Naranja", "Amarillo", "Verde"});
        scorePanel.add(levelDropdown, gbc);

        // Puntaje
        gbc.gridx = 0;
        gbc.gridy = 1;
        scorePanel.add(new JLabel("Puntaje:"), gbc);

        gbc.gridx = 1;
        puntajeField = new JTextField(10);
        scorePanel.add(puntajeField, gbc);

        // Botón para modificar puntaje
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Abarcar ambas columnas
        JButton modifyScoreButton = new JButton("Modificar Puntaje");
        modifyScoreButton.addActionListener(e -> modificarPuntaje());
        scorePanel.add(modifyScoreButton, gbc);

        // Panel para la creación de bloques
        JPanel blockPanel = new JPanel(new GridBagLayout());
        blockPanel.setBorder(BorderFactory.createTitledBorder("Crear Bloque"));

        // Tipo de bloque
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        blockPanel.add(new JLabel("Tipo de bloque:"), gbc);

        gbc.gridx = 1;
        blockTypeDropdown = new JComboBox<>(new String[]{"Normal", "MasVelocidad", "MenosVelocidad","RaquetaDoble",
        "RaquetaMitad", "MasBolas", "MasVidas"});
        blockPanel.add(blockTypeDropdown, gbc);

        // Fila
        gbc.gridx = 0;
        gbc.gridy = 1;
        blockPanel.add(new JLabel("Fila:"), gbc);

        gbc.gridx = 1;
        filaField = new JTextField(10);
        blockPanel.add(filaField, gbc);

        // Columna
        gbc.gridx = 0;
        gbc.gridy = 2;
        blockPanel.add(new JLabel("Columna:"), gbc);

        gbc.gridx = 1;
        columnaField = new JTextField(10);
        blockPanel.add(columnaField, gbc);

        // Botón para crear el bloque
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Abarcar ambas columnas
        JButton createBlockButton = new JButton("Crear Bloque");
        createBlockButton.addActionListener(e -> crearBloque());
        blockPanel.add(createBlockButton, gbc);

        // Agregar los paneles al marco
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Panel principal para organizar
        mainPanel.add(scorePanel);
        mainPanel.add(blockPanel);
        frame.add(mainPanel, BorderLayout.SOUTH);

        // Listener para mostrar el cliente seleccionado
        clientList.addListSelectionListener(e -> mostrarClienteSeleccionado());

        frame.setVisible(true);
        frame.setLocationRelativeTo(null); // Centrar la ventana
    }

    private void modificarPuntaje() {
        String nivel = (String) levelDropdown.getSelectedItem();
        String nuevoPuntajeStr = JOptionPane.showInputDialog(null, "Ingrese el nuevo puntaje para el nivel " + nivel + ":");

        if (nuevoPuntajeStr != null) {
            try {
                int nuevoPuntaje = Integer.parseInt(nuevoPuntajeStr);
                puntajeField.setText(String.valueOf(nuevoPuntaje)); // Actualiza el campo de puntaje
                JOptionPane.showMessageDialog(null, "Puntaje del nivel " + nivel + " modificado a: " + nuevoPuntaje);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "El puntaje debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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