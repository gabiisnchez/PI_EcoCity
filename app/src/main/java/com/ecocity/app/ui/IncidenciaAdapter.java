package com.ecocity.app.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.R;
import com.ecocity.app.model.Incidencia;
import java.util.List;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    private List<Incidencia> incidenciaList;

    public IncidenciaAdapter(List<Incidencia> incidenciaList) {
        this.incidenciaList = incidenciaList;
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
        return new IncidenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia incidencia = incidenciaList.get(position);
        holder.tvTitulo.setText(incidencia.getTitulo());
        holder.tvDescripcion.setText(incidencia.getDescripcion());
        holder.tvUrgencia.setText(incidencia.getUrgencia());
        holder.tvEstado.setText(incidencia.getEstado());

        // Dynamic styling based on urgency
        int urgencyColor;
        String urgencia = incidencia.getUrgencia();
        if (urgencia == null) urgencia = "Baja"; // Valor por defecto

        switch (urgencia.toLowerCase()) {
            case "alta":
                urgencyColor = Color.parseColor("#E53935"); // Match colors.xml urgency_high
                break;
            case "media":
                urgencyColor = Color.parseColor("#FFB300"); // Match colors.xml urgency_medium
                break;
            default:
                urgencyColor = Color.parseColor("#43A047"); // Match colors.xml urgency_low
                break;
        }

        holder.statusBar.setBackgroundColor(urgencyColor);
        holder.chipUrgencia.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        holder.tvUrgencia.setTextColor(urgencyColor);

        // Location Status Logic
        if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
            holder.tvLocationStatus.setText("Ubicación registrada");
            holder.tvLocationStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
            holder.ivLocationIcon.setColorFilter(Color.parseColor("#2E7D32"));
        } else {
            holder.tvLocationStatus.setText("Ubicación pendiente");
            holder.tvLocationStatus.setTextColor(Color.parseColor("#FF9800")); // Orange warning
            holder.ivLocationIcon.setColorFilter(Color.parseColor("#FF9800"));
        }

        // Status Styling Logic
        int statusBgColor;
        int statusTextColor;
        int statusIconRes;

        String estado = incidencia.getEstado() != null ? incidencia.getEstado() : "Pendiente";

        if (estado.equalsIgnoreCase("En proceso")) {
            statusBgColor = Color.parseColor("#E3F2FD"); // Light Blue
            statusTextColor = Color.parseColor("#1976D2"); // Dark Blue
            statusIconRes = android.R.drawable.ic_popup_sync;
        } else if (estado.equalsIgnoreCase("Resuelta")) {
            statusBgColor = Color.parseColor("#E8F5E9"); // Light Green
            statusTextColor = Color.parseColor("#388E3C"); // Dark Green
            statusIconRes = R.drawable.ic_check;
        } else {
            // Pendiente (Default)
            statusBgColor = Color.parseColor("#EEEEEE"); // Light Grey
            statusTextColor = Color.parseColor("#616161"); // Dark Grey
            statusIconRes = android.R.drawable.ic_menu_help;
        }

        holder.chipEstado.setCardBackgroundColor(statusBgColor);
        holder.tvEstado.setTextColor(statusTextColor);
        holder.ivEstadoIcon.setImageResource(statusIconRes);
        holder.ivEstadoIcon.setColorFilter(statusTextColor);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context context = v.getContext();
                android.content.Intent intent = new android.content.Intent(context, DetailIncidenciaActivity.class);
                intent.putExtra("incidencia", incidencia);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incidenciaList.size();
    }

    public static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvUrgencia, tvEstado, tvLocationStatus;
        View statusBar;
        CardView chipUrgencia, chipEstado;
        android.widget.ImageView ivLocationIcon, ivEstadoIcon;

        public IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvUrgencia = itemView.findViewById(R.id.tvUrgencia);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            statusBar = itemView.findViewById(R.id.statusBar);
            chipUrgencia = itemView.findViewById(R.id.chipUrgencia);
            tvLocationStatus = itemView.findViewById(R.id.tvLocationStatus);
            ivLocationIcon = itemView.findViewById(R.id.ivLocationIcon);

            chipEstado = itemView.findViewById(R.id.chipEstado);
            ivEstadoIcon = itemView.findViewById(R.id.ivEstadoIcon);
        }
    }
}
