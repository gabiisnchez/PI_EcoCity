import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor del Centro de Control (TCP Sockets).
 * Escucha conexiones de la app Android y reenvía (broadcast)
 * los mensajes a todos los miembros de la incidencia conectados.
 */
public class ControlCenterServer {

    private static final int PORT = 5000;
    // Lista de clientes conectados a nuestro chat grupal
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" INICIANDO CENTRO DE CONTROL (SERVIDOR TCP JAVA)");
        System.out.println("==================================================");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVIDOR] Escuchando en el puerto " + PORT + "...");
            System.out.println("[SERVIDOR] Esperando conexiones desde la App Android...\n");

            // Bucle infinito para aceptar múltiples clientes (multihilo)
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[NUEVA CONEXIÓN] Cliente conectado desde: " + socket.getRemoteSocketAddress());
                
                // Crear un manejador en un nuevo hilo para este cliente
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                
                System.out.println("[CONEXIONES ACTIVAS] Total: " + clients.size() + "\n");
            }

        } catch (IOException e) {
            System.err.println("[ERROR DEL SERVIDOR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados EXCEPTO al remitente.
     */
    public static synchronized void broadcastMessage(String message, ClientHandler sender) {
        System.out.println("[BROADCASTING] " + message);
        
        for (ClientHandler client : clients) {
            // Evitamos que te llegue el mensaje que tú mismo has enviado
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Elimina a un cliente de la lista cuando se desconecta.
     */
    public static synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("[DESCONECTADO] Cliente desconectado. Total activos: " + clients.size() + "\n");
    }

    // =========================================================================
    // Hilo interno para manejar la comunicación bidireccional de cada cliente
    // =========================================================================
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.dataInputStream = new DataInputStream(socket.getInputStream());
                this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.err.println("[ERROR CLIENTE] No se pudieron abrir los streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // Escuchar mensajes provenientes del cliente continuamente
                while (!socket.isClosed()) {
                    String incomingMessage = dataInputStream.readUTF();
                    System.out.println("[RECIBIDO] " + incomingMessage);
                    
                    // Reenviar a los demás (Grupo de WhatsApp)
                    broadcastMessage(incomingMessage, this);
                }
            } catch (IOException e) {
                // Esto salta normalmente cuando el usuario cierra la App / Chat
                // System.err.println("[INFO] Conexión cerrada por el cliente: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        /**
         * Envía un mensaje hacia el cliente Android usando writeUTF.
         */
        public void sendMessage(String message) {
            try {
                if (socket != null && !socket.isClosed() && dataOutputStream != null) {
                    dataOutputStream.writeUTF(message);
                    dataOutputStream.flush(); // Importante limpiar el buffer
                }
            } catch (IOException e) {
                System.err.println("[ERROR ENVIANDO] " + e.getMessage());
                closeConnection();
            }
        }

        /**
         * Libera los recursos asociados a esta conexión.
         */
        private void closeConnection() {
            removeClient(this);
            try {
                if (dataInputStream != null) dataInputStream.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
