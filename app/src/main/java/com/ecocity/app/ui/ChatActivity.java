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

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ChatAdapter adapter;
    private List<Mensaje> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        // Welcome message
        messageList.add(new Mensaje("¡Hola! Soy el asistente virtual de EcoCity. ¿En qué puedo ayudarte hoy?", false));

        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            // Add User Message
            messageList.add(new Mensaje(text, true));
            adapter.notifyItemInserted(messageList.size() - 1);
            rvChat.scrollToPosition(messageList.size() - 1);
            etMessage.setText("");

            // Simulate Reply
            simulateSupportReply();
        }
    }

    private void simulateSupportReply() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String reply = "Gracias por tu mensaje. Un operador revisará tu consulta en breve.";
                messageList.add(new Mensaje(reply, false));
                adapter.notifyItemInserted(messageList.size() - 1);
                rvChat.scrollToPosition(messageList.size() - 1);
            }
        }, 1500); // 1.5 seconds delay
    }
}
