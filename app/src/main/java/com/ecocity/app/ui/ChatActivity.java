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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.ecocity.app.api.GeminiApiService;
import com.ecocity.app.api.GeminiRequest;
import com.ecocity.app.api.GeminiResponse;
import com.ecocity.app.database.IncidenciaDAO;
import android.util.Log;

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

    // Configuración API Gemini
    private GeminiApiService geminiApiService;
    private static final String GEMINI_API_KEY = "AIzaSyDBOrbO99Jue5dIEPYubiubxrP_ZwCQUDs";

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
        messageList.add(new Mensaje(getString(R.string.chat_welcome_message), false));

        // Inicializar Retrofit para llamadas a Gemini
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        geminiApiService = retrofit.create(GeminiApiService.class);

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

            // 2. Llamar a Gemini API
            callGeminiAPI(text);
        }
    }

    /**
     * Llama a la API de Google Gemini enviándole el mensaje del usuario más un
     * contexto del sistema.
     */
    private void callGeminiAPI(String userText) {
        // Indicador visual de que el bot está pensando (simulado rápido)
        messageList.add(new Mensaje("Escribiendo...", false));
        final int typingIndex = messageList.size() - 1;
        adapter.notifyItemInserted(typingIndex);
        rvChat.scrollToPosition(typingIndex);

        // 1. Obtener contexto de la base de datos (cuántas incidencias hay para que
        // Gemini lo sepa)
        IncidenciaDAO dao = new IncidenciaDAO(this);
        dao.open();

        // La llamada a SQLite a través del DAO ahora es asíncrona mediante Callback
        dao.getIncidenciasCount(null, null, count -> {
            dao.close();

            // 2. Construir el Prompt dentro del contexto una vez sabemos el número
            String prompt = "Eres el asistente virtual de la aplicación Android EcoCity. " +
                    "Tu objetivo es dar soporte al ciudadano que reporta incidencias urbanas. " +
                    "Contexto actual secreto de la app: Hay " + count
                    + " incidencias registradas en la base de datos local " +
                    "del usuario ahora mismo. Responde de forma concisa, amable y en español a lo siguiente: "
                    + userText;

            proceedWithGeminiCall(prompt, typingIndex);
        });
    }

    /**
     * Refactorizado para continuar la cadena asíncrona después de obtener la BBDD.
     */
    private void proceedWithGeminiCall(String prompt, int typingIndex) {
        // 3. Crear el Body de la petición
        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest request = new GeminiRequest(Collections.singletonList(content));

        // 4. Ejecutar llamada HTTP asíncrona
        Call<GeminiResponse> call = geminiApiService.generateContent(GEMINI_API_KEY, request);
        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                // Borrar mensaje de "Escribiendo..."
                messageList.remove(typingIndex);
                adapter.notifyItemRemoved(typingIndex);

                if (response.isSuccessful() && response.body() != null) {
                    String geminiReply = response.body().extractText();

                    // Asegurar que actualizamos la UI en el hilo principal
                    runOnUiThread(() -> {
                        messageList.add(new Mensaje(geminiReply, false));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        rvChat.scrollToPosition(messageList.size() - 1);
                    });
                } else {
                    Log.e("GeminiAPI", "Error: " + response.code() + " " + response.message());
                    showErrorReply();
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e("GeminiAPI", "Failure: " + t.getMessage());
                // Borrar mensaje de "Escribiendo..." si falla por red
                runOnUiThread(() -> {
                    if (typingIndex < messageList.size()
                            && messageList.get(typingIndex).getTexto().equals("Escribiendo...")) {
                        messageList.remove(typingIndex);
                        adapter.notifyItemRemoved(typingIndex);
                    }
                    showErrorReply();
                });
            }
        });
    }

    private void showErrorReply() {
        runOnUiThread(() -> {
            messageList.add(new Mensaje(
                    "Lo siento, los servidores de Inteligencia Artificial están saturados. Inténtelo más tarde.",
                    false));
            adapter.notifyItemInserted(messageList.size() - 1);
            rvChat.scrollToPosition(messageList.size() - 1);
        });
    }
}
