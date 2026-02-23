package com.ecocity.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.ecocity.app.R;
import com.ecocity.app.model.Incidencia;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.net.Uri;
import java.io.File;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * <h1>DetailIncidenciaActivity</h1>
 * <p>
 * Pantalla de Detalle de una Incidencia.
 * Muestra toda la información registrada de una incidencia específica,
 * incluyendo
 * su ubicación en un mapa y su imagen.
 * </p>
 * 
 * <h2>Funcionalidades:</h2>
 * <ul>
 * <li>Visualización de datos (Título, Descripción, Autor).</li>
 * <li>Indicadores visuales (Chips) para Estado y Urgencia con código de
 * colores.</li>
 * <li>Visualización de ubicación en mini-mapa (Google Maps Lite Mode).</li>
 * <li>Navegación a Edición (AddIncidenciaActivity).</li>
 * </ul>
 */
public class DetailIncidenciaActivity extends AppCompatActivity {

    // Objeto de datos principal
    private Incidencia incidencia;

    // Componentes de UI
    private TextView tvTitulo, tvDescripcion, tvUrgencia, tvEstado, tvLocationText, tvUserEmail;
    private CardView cardUrgencia, cardEstado;
    private ImageView ivEstadoIcon, ivHeader;
    private FloatingActionButton fabEdit, fabGroupChat;

    // Componente de Mapa embebido
    private MapView mapView;

    /**
     * Inicialización de la actividad.
     * Recupera el objeto Incidencia pasado por el Intent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_incidencia);

        // Ajustes de UI para Edge-to-Edge
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets
                        .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 1. Vincular Vistas (Binding)
        tvTitulo = findViewById(R.id.tvTitulo);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvUrgencia = findViewById(R.id.tvUrgencia);
        tvEstado = findViewById(R.id.tvEstado);
        tvLocationText = findViewById(R.id.tvLocationText);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        cardUrgencia = findViewById(R.id.cardUrgencia);
        cardEstado = findViewById(R.id.cardEstado);
        ivEstadoIcon = findViewById(R.id.ivEstadoIcon);
        ivHeader = findViewById(R.id.ivHeader);
        fabEdit = findViewById(R.id.fabEdit);
        fabGroupChat = findViewById(R.id.fabGroupChat);

        // 2. Inicializar MapView (AHORA DINÁMICO en setupLocation)
        // No hacemos nada aquí para evitar crashes de inflación

        // Botón Atrás
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 3. Obtener Datos del Intent de forma segura
        try {
            if (getIntent().hasExtra("incidencia")) {
                incidencia = (Incidencia) getIntent().getSerializableExtra("incidencia");
            }
        } catch (Exception e) {
            android.util.Log.e("DetailIncidencia", "Error al recuperar incidencia del Intent: " + e.getMessage());
        }

        if (incidencia != null) {
            try {
                setupUI(); // Rellenar la interfaz con los datos
            } catch (Exception e) {
                android.util.Log.e("DetailIncidencia", "Error crítico en setupUI: " + e.getMessage());
                e.printStackTrace();
                android.widget.Toast.makeText(this, "Error mostrando detalles", android.widget.Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            android.widget.Toast.makeText(this, "Error cargando incidencia", android.widget.Toast.LENGTH_SHORT).show();
            finish(); // Si no hay datos, cerramos la actividad por seguridad
        }

        // 4. Configurar Botón de Edición
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a AddIncidenciaActivity pasando la incidencia actual para editarla
                Intent intent = new Intent(DetailIncidenciaActivity.this, AddIncidenciaActivity.class);
                intent.putExtra("incidencia", incidencia);
                intent.putExtra("incidencia_id", incidencia.getId()); // ID explícito por seguridad
                startActivity(intent);
                finish(); // Cerramos detalle para que al volver de editar vayamos a la lista renovada
            }
        });

        // 5. Configurar Botón de Chat Grupal (TCP Sockets)
        fabGroupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailIncidenciaActivity.this, IncidenceChatActivity.class);
                intent.putExtra("incidencia_id", incidencia.getId());
                intent.putExtra("incidencia_titulo", incidencia.getTitulo());
                startActivity(intent);
            }
        });
    }

    /**
     * Rellena todos los campos de la interfaz con los datos de la incidencia.
     */
    private void setupUI() {
        if (incidencia == null)
            return;

        tvTitulo.setText(incidencia.getTitulo());
        tvDescripcion.setText(incidencia.getDescripcion());

        // Cargar Imagen de Cabecera
        if (incidencia.getFotoPath() != null && !incidencia.getFotoPath().isEmpty()) {
            try {
                // Distinguir entre URI de contenido (galería/cámara) y ruta de archivo
                if (incidencia.getFotoPath().startsWith("content://")) {
                    ivHeader.setImageURI(Uri.parse(incidencia.getFotoPath()));
                } else {
                    ivHeader.setImageURI(Uri.fromFile(new File(incidencia.getFotoPath())));
                }
                ivHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                e.printStackTrace();
                // Si falla, se queda la imagen por defecto (placeholder) definida en XML
            }
        }

        // Configurar chips de estado y urgencia
        setupUrgencyChip();
        setupStatusChip();

        setupAuthorEmail();
        setupLocation();
    }

    /**
     * Muestra el email del autor de la incidencia.
     */
    private void setupAuthorEmail() {
        if (incidencia.getUserEmail() != null && !incidencia.getUserEmail().isEmpty()) {
            tvUserEmail.setText(incidencia.getUserEmail());
        } else {
            tvUserEmail.setText(getString(R.string.detail_unknown_user));
        }
    }

    // ...

    /**
     * Configura el indicador visual de Urgencia (Color y Texto).
     */
    private void setupUrgencyChip() {
        String urgency = incidencia.getUrgencia() != null ? incidencia.getUrgencia() : "Baja";
        tvUrgencia.setText(urgency.toUpperCase());

        int color;
        // Asignar colores según nivel de urgencia
        switch (urgency.toLowerCase()) {
            case "alta":
                color = androidx.core.content.ContextCompat.getColor(this, R.color.urgency_high_bg);
                tvUrgencia.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.urgency_high));
                break;
            case "media":
                color = androidx.core.content.ContextCompat.getColor(this, R.color.urgency_medium_bg);
                tvUrgencia.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.urgency_medium));
                break;
            default: // Baja
                color = androidx.core.content.ContextCompat.getColor(this, R.color.urgency_low_bg);
                tvUrgencia.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.urgency_low));
                break;
        }
        cardUrgencia.setCardBackgroundColor(color);
    }

    /**
     * Configura el indicador visual de Estado (Color, Texto e Icono).
     */
    private void setupStatusChip() {
        String estado = incidencia.getEstado() != null ? incidencia.getEstado() : "Pendiente";
        tvEstado.setText(estado.toUpperCase());

        int bgRes, textRes, iconRes;

        if (estado.equalsIgnoreCase("En proceso")) {
            bgRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatProcessBg);
            textRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatProcessText);
            iconRes = android.R.drawable.ic_popup_sync;
        } else if (estado.equalsIgnoreCase("Resuelta")) {
            bgRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatResolvedBg);
            textRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatResolvedText);
            iconRes = android.R.drawable.checkbox_on_background;
        } else { // Pendiente
            bgRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatPendingBg);
            textRes = androidx.core.content.ContextCompat.getColor(this, R.color.colorStatPendingText);
            iconRes = android.R.drawable.ic_menu_help;
        }

        cardEstado.setCardBackgroundColor(bgRes);
        tvEstado.setTextColor(textRes);
        ivEstadoIcon.setImageResource(iconRes);
        ivEstadoIcon.setColorFilter(textRes); // Tintar icono
    }

    /**
     * Configura el mapa si hay ubicación registrada.
     */
    private void setupLocation() {
        if (incidencia == null)
            return;

        try {
            if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
                // Mostrar coordenadas
                if (tvLocationText != null) {
                    tvLocationText.setText(String.format(getString(R.string.detail_location_title),
                            String.format("%.4f", incidencia.getLatitud()),
                            String.format("%.4f", incidencia.getLongitud())));
                    tvLocationText
                            .setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.urgency_low));
                }

                // Mostrar Mapa Dinámicamente
                android.widget.FrameLayout mapContainer = findViewById(R.id.mapContainer);
                if (mapContainer != null) {
                    try {
                        // Crear MapView programáticamente
                        com.google.android.gms.maps.GoogleMapOptions options = new com.google.android.gms.maps.GoogleMapOptions()
                                .liteMode(true);
                        mapView = new com.google.android.gms.maps.MapView(this, options);

                        // Añadir al contenedor
                        mapContainer.removeAllViews();
                        mapContainer.addView(mapView);

                        // Iniciar ciclo de vida MANUALMENTE
                        mapView.onCreate(null); // Bundle nulo es seguro aquí
                        mapView.onResume(); // Necesario para que se muestre inmediatamente

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                if (googleMap == null)
                                    return;
                                try {
                                    LatLng location = new LatLng(incidencia.getLatitud(), incidencia.getLongitud());
                                    googleMap.addMarker(
                                            new MarkerOptions().position(location)
                                                    .title(getString(R.string.subtitle_incidencia)));
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
                                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                                } catch (Exception ex) {
                                    android.util.Log.e("DetailIncidencia", "Error en onMapReady: " + ex.getMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("DetailIncidencia", "Error creando MapView dinámico: " + e.getMessage());
                        // Fallback visual si falla el mapa
                        if (tvLocationText != null) {
                            tvLocationText.setText(tvLocationText.getText() + " (Mapa no disponible)");
                        }
                    }
                }
            } else {
                // Ocultar sección si no hay coordenadas
                if (tvLocationText != null) {
                    tvLocationText.setText(getString(R.string.detail_no_location));
                }
                android.widget.FrameLayout mapContainer = findViewById(R.id.mapContainer);
                if (mapContainer != null) {
                    mapContainer.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("DetailIncidencia", "Error en setupLocation: " + e.getMessage());
        }
    }

    // --- Métodos del Ciclo de Vida para MapView ---
    // Es CRÍTICO reenviar estos eventos al MapView para que se renderice
    // correctamente.

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
    }
}
