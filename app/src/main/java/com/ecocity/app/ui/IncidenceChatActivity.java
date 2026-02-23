package com.ecocity.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecocity.app.R;
import com.ecocity.app.model.GroupMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class IncidenceChatActivity extends AppCompatActivity {

    private static final String TAG = "IncidenceChat";

    // Configuración del Servidor (10.0.2.2 es el localhost del PC desde el emulador
    // Android)
    private static final String SERVER_IP = "10.0.2.2";
    private static final int SERVER_PORT = 5000;

    private RecyclerView rvGroupChat;
    private EditText etGroupMessage;
    private FloatingActionButton btnGroupSend;
    private TextView tvChatTitle;

    private IncidenceChatAdapter adapter;
    private List<GroupMessage> messageList;

    private String incidenciaId;
    private String currentUserEmail;

    // Networking
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidence_chat);

        // Obtener datos del Intent
        incidenciaId = getIntent().getStringExtra("incidencia_id");
        String incidenciaTitulo = getIntent().getStringExtra("incidencia_titulo");

        if (incidenciaId == null) {
            Toast.makeText(this, "Error: IDC de incidencia no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtener usuario actual
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserEmail = auth.getCurrentUser().getEmail();
        } else {
            currentUserEmail = "Anonimo"; // Fallback
        }

        // Inicializar vistas
        rvGroupChat = findViewById(R.id.rvGroupChat);
        etGroupMessage = findViewById(R.id.etGroupMessage);
        btnGroupSend = findViewById(R.id.btnGroupSend);
        tvChatTitle = findViewById(R.id.tvChatTitle);

        tvChatTitle.setText("Chat: " + (incidenciaTitulo != null ? incidenciaTitulo : incidenciaId));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Configurar RecyclerView
        messageList = new ArrayList<>();
        adapter = new IncidenceChatAdapter(messageList);
        rvGroupChat.setLayoutManager(new LinearLayoutManager(this));
        rvGroupChat.setAdapter(adapter);

        // Listener de enviar
        btnGroupSend.setOnClickListener(v -> sendMessage());

        // Conectar al servidor TCP
        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Conectando al servidor " + SERVER_IP + ":" + SERVER_PORT);
                socket = new Socket(SERVER_IP, SERVER_PORT);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                isConnected = true;

                Log.d(TAG, "Conexión exitosa. Iniciando hilo de lectura.");

                // Hilo para escuchar mensajes entrantes recurrentemente
                startListeningThread();

            } catch (Exception e) {
                Log.e(TAG, "Error conectando al servidor: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error de conexión al chat. El servidor podría estar apagado.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void startListeningThread() {
        new Thread(() -> {
            while (isConnected && socket != null && !socket.isClosed()) {
                try {
                    // Leemos el mensaje del servidor
                    String incomingMessage = dataInputStream.readUTF();
                    Log.d(TAG, "Mensaje recibido: " + incomingMessage);

                    // Parsear el mensaje. Formato esperado: [INC-ID] [email] texto
                    parseAndDisplayMessage(incomingMessage);

                } catch (Exception e) {
                    Log.e(TAG, "Error leyendo del servidor o conexión cerrada: " + e.getMessage());
                    isConnected = false;
                    break;
                }
            }
        }).start();
    }

    private void sendMessage() {
        String texto = etGroupMessage.getText().toString().trim();
        if (TextUtils.isEmpty(texto))
            return;

        if (!isConnected || socket == null || socket.isClosed()) {
            Toast.makeText(this, "No estás conectado al chat grupal.", Toast.LENGTH_SHORT).show();
            return;
        }

        etGroupMessage.setText("");

        new Thread(() -> {
            try {
                // Formato de envío: [INC-ID] [email] texto
                String messageToSend = "[" + incidenciaId + "] [" + currentUserEmail + "] " + texto;
                dataOutputStream.writeUTF(messageToSend);
                dataOutputStream.flush();
                Log.d(TAG, "Mensaje enviado: " + messageToSend);

            } catch (Exception e) {
                Log.e(TAG, "Error enviando mensaje: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error enviando", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseAndDisplayMessage(String rawMessage) {
        // Ejemplo rawMessage: "[id123] [juan@email.com] Hola a todos"
        try {
            if (rawMessage.startsWith("[" + incidenciaId + "]")) {
                // Es para esta incidencia
                int firstClosingBracket = rawMessage.indexOf(']');
                int secondOpeningBracket = rawMessage.indexOf('[', firstClosingBracket);
                int secondClosingBracket = rawMessage.indexOf(']', secondOpeningBracket);

                if (firstClosingBracket != -1 && secondOpeningBracket != -1 && secondClosingBracket != -1) {
                    String senderEmail = rawMessage.substring(secondOpeningBracket + 1, secondClosingBracket);
                    String testMessage = rawMessage.substring(secondClosingBracket + 1).trim();

                    boolean isMine = senderEmail.equals(currentUserEmail);

                    GroupMessage msg = new GroupMessage(testMessage, senderEmail, isMine);

                    runOnUiThread(() -> {
                        messageList.add(msg);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        rvGroupChat.scrollToPosition(messageList.size() - 1);
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parseando mensaje: " + rawMessage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
        try {
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (dataInputStream != null)
                dataInputStream.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error cerrando sockets: " + e.getMessage());
        }
    }
}
