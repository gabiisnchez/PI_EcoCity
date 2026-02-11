package com.ecocity.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.R;
import com.ecocity.app.model.Mensaje;
import java.util.List;

/**
 * <h1>ChatAdapter</h1>
 * <p>
 * Adaptador para la lista de mensajes del chat.
 * Maneja dos tipos de vistas diferentes: Mensajes enviados (Usuario) y Mensajes
 * recibidos (Soporte).
 * </p>
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constantes para identificar el tipo de vista
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Mensaje> messageList;

    public ChatAdapter(List<Mensaje> messageList) {
        this.messageList = messageList;
    }

    /**
     * Determina el tipo de vista para un elemento en una posición dada.
     * 
     * @return VIEW_TYPE_SENT si el mensaje es del usuario, VIEW_TYPE_RECEIVED si es
     *         del soporte.
     */
    @Override
    public int getItemViewType(int position) {
        Mensaje message = messageList.get(position);
        if (message.esUsuario()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    /**
     * Crea el ViewHolder correspondiente según el tipo de vista.
     * Infla layouts diferentes para mensajes enviados y recibidos.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    /**
     * Vincula los datos del mensaje con la vista.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje message = messageList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- ViewHolders Internos ---

    // ViewHolder para mensajes enviados (Alineados a la derecha)
    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        SentMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(Mensaje message) {
            tvMessage.setText(message.getTexto());
        }
    }

    // ViewHolder para mensajes recibidos (Alineados a la izquierda)
    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(Mensaje message) {
            tvMessage.setText(message.getTexto());
        }
    }
}
