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
 * <h1>IncidenciaAdapter</h1>
 * <p>
 * Adaptador personalizado para el RecyclerView que muestra la lista de
 * incidencias.
 * </p>
 * 
 * <h2>Características Principales:</h2>
 * <ul>
 * <li><b>Lista Heterogénea:</b> Gestiona dos tipos de vistas: Cabeceras de
 * Sección (Strings) e Ítems de Incidencia (Objetos Incidencia).</li>
 * <li><b>Secciones Agrupadas:</b> Agrupa las incidencias por estado (Pendiente,
 * En Proceso, Resuelta).</li>
 * <li><b>Expandible/Colapsable:</b> Permite al usuario abrir o cerrar secciones
 * para organizar la vista.</li>
 * </ul>
 */
public class IncidenciaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Constantes para tipos de vista
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // Lista principal de visualización (Contiene tanto Strings como Incidencias
    // ordenados)
    private List<Object> displayList;

    // Mapa auxiliar para agrupar incidencias por estado
    private Map<String, List<Incidencia>> groupedIncidencias;

    // Mapa para controlar qué secciones están expandidas
    private Map<String, Boolean> expandedSections;

    // Definición del orden de las secciones
    private final String[] SECTIONS = { "Pendiente", "En Proceso", "Resuelta" };

    /**
     * Constructor del adaptador.
     * Inicializa las estructuras de datos y procesa la lista inicial de
     * incidencias.
     * 
     * @param allIncidencias Lista completa de incidencias a mostrar.
     */
    public IncidenciaAdapter(List<Incidencia> allIncidencias) {
        this.groupedIncidencias = new HashMap<>();
        this.expandedSections = new HashMap<>();
        this.displayList = new ArrayList<>();

        // 1. Inicializar grupos vacíos y estado de expansión por defecto
        for (String section : SECTIONS) {
            groupedIncidencias.put(section, new ArrayList<>());
            // Por UX, colapsamos "Resuelta" para no saturar, y expandimos las activas
            expandedSections.put(section, !section.equals("Resuelta"));
        }

        // 2. Clasificar incidencias en los grupos correspondientes
        for (Incidencia inc : allIncidencias) {
            String statusRaw = inc.getEstado() != null ? inc.getEstado() : "Pendiente";
            String statusKey = "Pendiente"; // Fallback

            if (statusRaw.equalsIgnoreCase("En proceso")) {
                statusKey = "En Proceso";
            } else if (statusRaw.equalsIgnoreCase("Resuelta")) {
                statusKey = "Resuelta";
            }

            groupedIncidencias.get(statusKey).add(inc);
        }

        // 3. Construir la lista lineal para el RecyclerView
        buildDisplayList();
    }

    /**
     * Construye la lista lineal (displayList) basada en los grupos y su estado de
     * expansión.
     * Esta lista es la que "ve" el RecyclerView.
     */
    private void buildDisplayList() {
        displayList.clear();
        for (String section : SECTIONS) {
            List<Incidencia> items = groupedIncidencias.get(section);
            // Paso A: Añadir Cabecera de Sección
            displayList.add(section);

            // Paso B: Si la sección está expandida y tiene items, añadirlos
            if (items != null && !items.isEmpty() && expandedSections.get(section)) {
                displayList.addAll(items);
            }
        }
    }

    /**
     * Actualiza los datos del adaptador con una nueva lista.
     * Mantiene el estado de expansión de las secciones.
     * 
     * @param newIncidencias Nueva lista de incidencias.
     */
    public void updateData(List<Incidencia> newIncidencias) {
        // Limpiar agrupaciones actuales
        for (String section : SECTIONS) {
            groupedIncidencias.put(section, new ArrayList<>());
        }

        // Re-agrupar datos nuevos
        for (Incidencia inc : newIncidencias) {
            String statusRaw = inc.getEstado() != null ? inc.getEstado() : "Pendiente";
            String statusKey = "Pendiente";

            if (statusRaw.equalsIgnoreCase("En proceso")) {
                statusKey = "En Proceso";
            } else if (statusRaw.equalsIgnoreCase("Resuelta")) {
                statusKey = "Resuelta";
            }

            groupedIncidencias.get(statusKey).add(inc);
        }

        // Reconstruir lista y notificar cambios
        buildDisplayList();
        notifyDataSetChanged();
    }

    /**
     * Determina si la posición corresponde a una cabecera o a un item.
     */
    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    /**
     * Infla el layout correspondiente según el tipo de vista.
     */
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

    /**
     * Vincula datos a las vistas. Delega en los ViewHolders específicos.
     */
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

    /**
     * ViewHolder para las Cabeceras de Sección.
     * Maneja el título, el contador y la flecha de expansión.
     */
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusTitle;
        ImageView ivExpand;
        CardView cardStatusColor;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatusTitle = itemView.findViewById(R.id.tvStatusTitle);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            cardStatusColor = itemView.findViewById(R.id.cardStatusColor);

            // Listener para expandir/colapsar al pulsar la cabecera completa
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
            ivExpand.setRotation(isExpanded ? 180f : 0f); // Rotar flecha si está expandido

            // Color distintivo para la cabecera
            int color;
            switch (section) {
                case "En Proceso":
                    color = Color.parseColor("#1976D2");
                    break;
                case "Resuelta":
                    color = Color.parseColor("#388E3C");
                    break;
                default:
                    color = Color.parseColor("#616161"); // Gris para pendiente
                    break;
            }
            cardStatusColor.setCardBackgroundColor(color);
        }

        /**
         * Lógica para expandir o colapsar una sección.
         * Modifica la lista displayList y notifica las animaciones de
         * inserción/borrado.
         */
        private void toggleSection(String section) {
            boolean isExpanded = expandedSections.get(section);
            List<Incidencia> items = groupedIncidencias.get(section);

            int headerPosition = getAdapterPosition();
            if (headerPosition == RecyclerView.NO_POSITION)
                return;

            // Invertir estado
            expandedSections.put(section, !isExpanded);

            // Animar rotación de flecha
            ivExpand.animate().rotation(!isExpanded ? 180f : 0f).setDuration(200).start();

            // Si no hay items, solo rotamos la flecha visualmente, no cambiamos la lista
            if (items == null || items.isEmpty())
                return;

            if (isExpanded) {
                // ACCIÓN: COLAPSAR
                // Removemos los items que están debajo de la cabecera
                int count = items.size();
                // Eliminamos de la lista visual
                for (int i = 0; i < count; i++) {
                    displayList.remove(headerPosition + 1);
                }
                // Notificamos al adaptador para animar la eliminación
                notifyItemRangeRemoved(headerPosition + 1, count);
            } else {
                // ACCIÓN: EXPANDIR
                // Añadimos los items debajo de la cabecera
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    displayList.add(headerPosition + 1 + i, items.get(i));
                }
                // Notificamos al adaptador para animar la inserción
                notifyItemRangeInserted(headerPosition + 1, count);
            }
        }
    }

    /**
     * ViewHolder para los Ítems de Incidencia.
     * Muestra la tarjeta con la información de la incidencia.
     */
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

            // --- Lógica de Estilos Visuales ---

            // 1. Color según Urgencia
            int urgencyColor;
            String urgencia = incidencia.getUrgencia() != null ? incidencia.getUrgencia() : "Baja";

            switch (urgencia.toLowerCase()) {
                case "alta":
                    urgencyColor = Color.parseColor("#E53935"); // Rojo
                    break;
                case "media":
                    urgencyColor = Color.parseColor("#FFB300"); // Ámbar
                    break;
                default:
                    urgencyColor = Color.parseColor("#43A047"); // Verde
                    break;
            }

            statusBar.setBackgroundColor(urgencyColor);
            chipUrgencia.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
            tvUrgencia.setTextColor(urgencyColor);

            // 2. Indicador de Ubicación
            if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
                tvLocationStatus.setText("Ubicación registrada");
                tvLocationStatus.setTextColor(Color.parseColor("#2E7D32"));
                ivLocationIcon.setColorFilter(Color.parseColor("#2E7D32"));
            } else {
                tvLocationStatus.setText("Ubicación pendiente");
                tvLocationStatus.setTextColor(Color.parseColor("#FF9800"));
                ivLocationIcon.setColorFilter(Color.parseColor("#FF9800"));
            }

            // 3. Estilos según Estado
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
                statusIconRes = R.drawable.ic_check; // Se asume existencia de recurso o drawable
            } else {
                statusBgColor = Color.parseColor("#EEEEEE");
                statusTextColor = Color.parseColor("#616161");
                statusIconRes = android.R.drawable.ic_menu_help;
            }

            chipEstado.setCardBackgroundColor(statusBgColor);
            tvEstado.setTextColor(statusTextColor);
            ivEstadoIcon.setImageResource(statusIconRes);
            ivEstadoIcon.setColorFilter(statusTextColor);

            // Navegación al pulsar el item
            itemView.setOnClickListener(v -> {
                android.content.Context context = v.getContext();
                android.content.Intent intent = new android.content.Intent(context, DetailIncidenciaActivity.class);
                intent.putExtra("incidencia", incidencia);
                context.startActivity(intent);
            });
        }
    }
}
