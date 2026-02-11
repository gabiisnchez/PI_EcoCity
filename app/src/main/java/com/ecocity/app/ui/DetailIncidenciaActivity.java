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
    private FloatingActionButton fabEdit;

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
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        ivHeader = findViewById(R.id.ivHeader);

        // 2. Inicializar MapView
        // Es necesario reenviar los métodos del ciclo de vida al MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Botón Atrás
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 3. Obtener Datos del Intent
        if (getIntent().hasExtra("incidencia")) {
            incidencia = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupUI(); // Rellenar la interfaz con los datos
        } else {
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
            tvUserEmail.setText("Desconocido");
        }
    }

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
                color = Color.parseColor("#FFEBEE"); // Rojo claro
                tvUrgencia.setTextColor(Color.parseColor("#D32F2F")); // Rojo oscuro
                break;
            case "media":
                color = Color.parseColor("#FFF8E1"); // Ámbar claro
                tvUrgencia.setTextColor(Color.parseColor("#FFA000")); // Ámbar oscuro
                break;
            default: // Baja
                color = Color.parseColor("#E8F5E9"); // Verde claro
                tvUrgencia.setTextColor(Color.parseColor("#388E3C")); // Verde oscuro
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
            bgRes = Color.parseColor("#E3F2FD"); // Azul claro
            textRes = Color.parseColor("#1976D2"); // Azul
            iconRes = android.R.drawable.ic_popup_sync;
        } else if (estado.equalsIgnoreCase("Resuelta")) {
            bgRes = Color.parseColor("#E8F5E9"); // Verde claro
            textRes = Color.parseColor("#388E3C"); // Verde
            iconRes = android.R.drawable.checkbox_on_background;
        } else { // Pendiente
            bgRes = Color.parseColor("#EEEEEE"); // Gris claro
            textRes = Color.parseColor("#616161"); // Gris
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
        if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
            // Mostrar coordenadas
            tvLocationText.setText(
                    "Ubicación Registrada\nLat: " + String.format("%.4f", incidencia.getLatitud()) + ", Lon: "
                            + String.format("%.4f", incidencia.getLongitud()));
            tvLocationText.setTextColor(Color.parseColor("#2E7D32"));

            // Mostrar Mapa
            mapView.setVisibility(View.VISIBLE);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Mover cámara y añadir marcador
                    LatLng location = new LatLng(incidencia.getLatitud(), incidencia.getLongitud());
                    googleMap.addMarker(new MarkerOptions().position(location).title("Incidencia"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

                    // Desactivar herramientas de mapa para modo "Lite"
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                }
            });
        } else {
            // Ocultar mapa si no hay coordenadas
            tvLocationText.setText("Sin ubicación");
            tvLocationText.setTextColor(Color.parseColor("#757575"));
            mapView.setVisibility(View.GONE);
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
