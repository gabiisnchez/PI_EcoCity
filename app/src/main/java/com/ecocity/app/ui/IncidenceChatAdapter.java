package com.ecocity.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.R;
import com.ecocity.app.model.GroupMessage;
import java.util.List;

public class IncidenceChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<GroupMessage> messageList;

    public IncidenceChatAdapter(List<GroupMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessage message = messageList.get(position);
        return message.isMine() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            // Reutilizamos el layout original para mensajes enviados (el nuestro)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            // Layout especial para mensajes recibidos en grupo (incluye remitente)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_group_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMessage message = messageList.get(position);
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

    private static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        SentMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(GroupMessage message) {
            tvMessage.setText(message.getTexto());
        }
    }

    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvSender;
        TextView tvMessage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        void bind(GroupMessage message) {
            tvSender.setText(message.getSenderEmail());
            tvMessage.setText(message.getTexto());
        }
    }
}
