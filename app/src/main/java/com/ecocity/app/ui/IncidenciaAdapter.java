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
        switch (incidencia.getUrgencia().toLowerCase()) {
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context context = v.getContext();
                android.content.Intent intent = new android.content.Intent(context, AddIncidenciaActivity.class);
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
        CardView chipUrgencia;
        android.widget.ImageView ivLocationIcon;

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
        }
    }
}
