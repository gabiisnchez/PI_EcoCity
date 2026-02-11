package com.ecocity.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.R;
import com.ecocity.app.model.Mensaje;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>ChatActivity</h1>
 * <p>
 * Pantalla de Chat de Soporte.
 * Permite al usuario comunicarse con un asistente virtual o soporte técnico.
 * Simula una conversación mediante respuestas automáticas con retardo.
 * </p>
 * 
 * <h2>Funcionalidades:</h2>
 * <ul>
 * <li>Envío de mensajes de texto.</li>
 * <li>Visualización de historial de chat en RecyclerView.</li>
 * <li>Simulación de "Escribiendo..." y respuesta automática.</li>
 * </ul>
 */
public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ChatAdapter adapter;
    private List<Mensaje> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Ajustes Edge-to-Edge
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de Vistas
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend = findViewById(R.id.btnSend);

        // Configuración de datos
        messageList = new ArrayList<>();
        // Mensaje de bienvenida inicial
        messageList.add(new Mensaje("¡Hola! Soy el asistente virtual de EcoCity. ¿En qué puedo ayudarte hoy?", false));

        // Configuración del Adapter y RecyclerView
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Listener de envío
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    /**
     * Envía el mensaje escrito por el usuario.
     * Añade el mensaje a la lista, actualiza la UI y limpia el campo de texto.
     */
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            // 1. Añadir mensaje del Usuario
            messageList.add(new Mensaje(text, true));
            // Notificar al adaptador que se insertó un item al final
            adapter.notifyItemInserted(messageList.size() - 1);
            // Hacer scroll hasta el último mensaje
            rvChat.scrollToPosition(messageList.size() - 1);
            etMessage.setText("");

            // 2. Simular respuesta del sistema
            simulateSupportReply();
        }
    }

    /**
     * Simula una respuesta del soporte técnico tras un breve retraso.
     * Utiliza un Handler para ejecutar código en el hilo principal después de un
     * tiempo.
     */
    private void simulateSupportReply() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String reply = "Gracias por tu mensaje. Un operador revisará tu consulta en breve.";
                messageList.add(new Mensaje(reply, false));
                adapter.notifyItemInserted(messageList.size() - 1);
                rvChat.scrollToPosition(messageList.size() - 1);
            }
        }, 1500); // 1.5 segundos de retraso
    }
}
