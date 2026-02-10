package com.ecocity.app.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.R;
import com.ecocity.app.model.Incidencia;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador para el RecyclerView de Incidencias.
 * Gestiona una lista heterogénea con cabeceras de sección (Strings) e items de incidencia (Incidencia).
 * Soporta secciones expandibles/colapsables por estado.
 */
public class IncidenciaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> displayList; // Puede ser String (Cabecera) o Incidencia (Item)
    private Map<String, List<Incidencia>> groupedIncidencias;
    private Map<String, Boolean> expandedSections;

    // Claves ordenadas para las secciones
    private final String[] SECTIONS = { "Pendiente", "En Proceso", "Resuelta" };

    public IncidenciaAdapter(List<Incidencia> allIncidencias) {
        this.groupedIncidencias = new HashMap<>();
        this.expandedSections = new HashMap<>();
        this.displayList = new ArrayList<>();

        // Inicializar grupos
        for (String section : SECTIONS) {
            groupedIncidencias.put(section, new ArrayList<>());
            // Colapsar "Resuelta" por defecto, expandir otras
            expandedSections.put(section, !section.equals("Resuelta"));
        }

        // Agrupar datos
        for (Incidencia inc : allIncidencias) {
            String statusRaw = inc.getEstado() != null ? inc.getEstado() : "Pendiente";
            String statusKey = "Pendiente"; // Por defecto

            if (statusRaw.equalsIgnoreCase("En proceso")) {
                statusKey = "En Proceso";
            } else if (statusRaw.equalsIgnoreCase("Resuelta")) {
                statusKey = "Resuelta";
            }

            groupedIncidencias.get(statusKey).add(inc);
        }

        buildDisplayList();
    }

    private void buildDisplayList() {
        displayList.clear();
        for (String section : SECTIONS) {
            List<Incidencia> items = groupedIncidencias.get(section);
            // Siempre añadir cabecera, incluso si está vacía
            displayList.add(section);

            if (items != null && !items.isEmpty() && expandedSections.get(section)) {
                displayList.addAll(items);
            }
        }
    }

    public void updateData(List<Incidencia> newIncidencias) {
        // Limpiar agrupaciones actuales pero mantener claves
        for (String section : SECTIONS) {
            groupedIncidencias.put(section, new ArrayList<>());
        }

        // Re-agrupar datos
        for (Incidencia inc : newIncidencias) {
            String statusRaw = inc.getEstado() != null ? inc.getEstado() : "Pendiente";
            String statusKey = "Pendiente"; // Por defecto

            if (statusRaw.equalsIgnoreCase("En proceso")) {
                statusKey = "En Proceso";
            } else if (statusRaw.equalsIgnoreCase("Resuelta")) {
                statusKey = "Resuelta";
            }

            groupedIncidencias.get(statusKey).add(inc);
        }

        // Reconstruir lista con nuevos datos manteniendo estado de expansión
        buildDisplayList();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incidencia, parent, false);
            return new IncidenciaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            String sectionTitle = (String) displayList.get(position);
            ((HeaderViewHolder) holder).bind(sectionTitle);
        } else {
            Incidencia incidencia = (Incidencia) displayList.get(position);
            ((IncidenciaViewHolder) holder).bind(incidencia);
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // --- ViewHolders ---

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusTitle;
        ImageView ivExpand;
        CardView cardStatusColor;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatusTitle = itemView.findViewById(R.id.tvStatusTitle);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            cardStatusColor = itemView.findViewById(R.id.cardStatusColor);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    String section = (String) displayList.get(pos);
                    toggleSection(section);
                }
            });
        }

        void bind(String section) {
            int count = groupedIncidencias.get(section).size();
            tvStatusTitle.setText(section + " (" + count + ")");

            boolean isExpanded = expandedSections.get(section);
            ivExpand.setRotation(isExpanded ? 180f : 0f); // Estado inicial

            int color;
            switch (section) {
                case "En Proceso":
                    color = Color.parseColor("#1976D2");
                    break;
                case "Resuelta":
                    color = Color.parseColor("#388E3C");
                    break;
                default:
                    color = Color.parseColor("#616161");
                    break;
            }
            cardStatusColor.setCardBackgroundColor(color);
        }

        private void toggleSection(String section) {
            boolean isExpanded = expandedSections.get(section);
            List<Incidencia> items = groupedIncidencias.get(section);
            // Permitir toggle incluso si está vacío para rotar flecha

            int headerPosition = getAdapterPosition();
            if (headerPosition == RecyclerView.NO_POSITION)
                return;

            // Actualizar estado
            expandedSections.put(section, !isExpanded);

            // Animar flecha
            ivExpand.animate().rotation(!isExpanded ? 180f : 0f).setDuration(200).start();

            // Si está vacío, no hay cambios en la lista
            if (items == null || items.isEmpty())
                return;

            if (isExpanded) {
                // Colapsar: Remover items de displayList y notificar
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    displayList.remove(headerPosition + 1);
                }
                notifyItemRangeRemoved(headerPosition + 1, count);
            } else {
                // Expandir: Añadir items a displayList y notificar
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    displayList.add(headerPosition + 1 + i, items.get(i));
                }
                notifyItemRangeInserted(headerPosition + 1, count);
            }
        }
    }

    class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvUrgencia, tvEstado, tvLocationStatus;
        View statusBar;
        CardView chipUrgencia, chipEstado;
        ImageView ivLocationIcon, ivEstadoIcon;

        IncidenciaViewHolder(@NonNull View itemView) {
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

        void bind(Incidencia incidencia) {
            tvTitulo.setText(incidencia.getTitulo());
            tvDescripcion.setText(incidencia.getDescripcion());
            tvUrgencia.setText(incidencia.getUrgencia());
            tvEstado.setText(incidencia.getEstado());

            // Estilos dinámicos según urgencia
            int urgencyColor;
            String urgencia = incidencia.getUrgencia() != null ? incidencia.getUrgencia() : "Baja";

            switch (urgencia.toLowerCase()) {
                case "alta":
                    urgencyColor = Color.parseColor("#E53935");
                    break;
                case "media":
                    urgencyColor = Color.parseColor("#FFB300");
                    break;
                default:
                    urgencyColor = Color.parseColor("#43A047");
                    break;
            }

            statusBar.setBackgroundColor(urgencyColor);
            chipUrgencia.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            tvUrgencia.setTextColor(urgencyColor);

            // Estado de Ubicación
            if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
                tvLocationStatus.setText("Ubicación registrada");
                tvLocationStatus.setTextColor(Color.parseColor("#2E7D32"));
                ivLocationIcon.setColorFilter(Color.parseColor("#2E7D32"));
            } else {
                tvLocationStatus.setText("Ubicación pendiente");
                tvLocationStatus.setTextColor(Color.parseColor("#FF9800"));
                ivLocationIcon.setColorFilter(Color.parseColor("#FF9800"));
            }

            // Estilos de Estado
            int statusBgColor;
            int statusTextColor;
            int statusIconRes;
            String estado = incidencia.getEstado() != null ? incidencia.getEstado() : "Pendiente";

            if (estado.equalsIgnoreCase("En proceso")) {
                statusBgColor = Color.parseColor("#E3F2FD");
                statusTextColor = Color.parseColor("#1976D2");
                statusIconRes = android.R.drawable.ic_popup_sync;
            } else if (estado.equalsIgnoreCase("Resuelta")) {
                statusBgColor = Color.parseColor("#E8F5E9");
                statusTextColor = Color.parseColor("#388E3C");
                statusIconRes = R.drawable.ic_check; // Ensure this drawable exists or use android default
            } else {
                statusBgColor = Color.parseColor("#EEEEEE");
                statusTextColor = Color.parseColor("#616161");
                statusIconRes = android.R.drawable.ic_menu_help;
            }

            chipEstado.setCardBackgroundColor(statusBgColor);
            tvEstado.setTextColor(statusTextColor);
            ivEstadoIcon.setImageResource(statusIconRes);
            ivEstadoIcon.setColorFilter(statusTextColor);

            itemView.setOnClickListener(v -> {
                android.content.Context context = v.getContext();
                android.content.Intent intent = new android.content.Intent(context, DetailIncidenciaActivity.class);
                intent.putExtra("incidencia", incidencia);
                context.startActivity(intent);
            });
        }
    }
}
