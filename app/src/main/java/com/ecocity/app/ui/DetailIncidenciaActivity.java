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

public class DetailIncidenciaActivity extends AppCompatActivity {

    private Incidencia incidencia;

    private TextView tvTitulo, tvDescripcion, tvUrgencia, tvEstado, tvLocationText;
    private CardView cardUrgencia, cardEstado;
    private ImageView ivEstadoIcon, ivHeader;
    private FloatingActionButton fabEdit;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_incidencia);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind Views
        tvTitulo = findViewById(R.id.tvTitulo);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvUrgencia = findViewById(R.id.tvUrgencia);
        tvEstado = findViewById(R.id.tvEstado);
        tvLocationText = findViewById(R.id.tvLocationText);

        cardUrgencia = findViewById(R.id.cardUrgencia);
        cardEstado = findViewById(R.id.cardEstado);
        ivEstadoIcon = findViewById(R.id.ivEstadoIcon);
        ivHeader = findViewById(R.id.ivHeader);

        ivHeader = findViewById(R.id.ivHeader);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        fabEdit = findViewById(R.id.fabEdit);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Get Data
        if (getIntent().hasExtra("incidencia")) {
            incidencia = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupUI();
        } else {
            finish(); // Cannot show details without data
        }

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailIncidenciaActivity.this, AddIncidenciaActivity.class);
                intent.putExtra("incidencia", incidencia);
                startActivity(intent);
                finish(); // Finish detail so back from Edit goes to List (or we could reload Detail)
            }
        });
    }

    private void setupUI() {
        if (incidencia == null)
            return;

        tvTitulo.setText(incidencia.getTitulo());
        tvDescripcion.setText(incidencia.getDescripcion());

        // Load Image
        if (incidencia.getFotoPath() != null && !incidencia.getFotoPath().isEmpty()) {
            try {
                if (incidencia.getFotoPath().startsWith("content://")) {
                    ivHeader.setImageURI(Uri.parse(incidencia.getFotoPath()));
                } else {
                    ivHeader.setImageURI(Uri.fromFile(new File(incidencia.getFotoPath())));
                }
                ivHeader.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setupUrgencyChip();
        setupStatusChip();
        setupLocation();
    }

    private void setupUrgencyChip() {
        String urgency = incidencia.getUrgencia() != null ? incidencia.getUrgencia() : "Baja";
        tvUrgencia.setText(urgency.toUpperCase());

        int color;
        switch (urgency.toLowerCase()) {
            case "alta":
                color = Color.parseColor("#FFEBEE"); // Light Red
                tvUrgencia.setTextColor(Color.parseColor("#D32F2F"));
                break;
            case "media":
                color = Color.parseColor("#FFF8E1"); // Light Amber
                tvUrgencia.setTextColor(Color.parseColor("#FFA000"));
                break;
            default:
                color = Color.parseColor("#E8F5E9"); // Light Green
                tvUrgencia.setTextColor(Color.parseColor("#388E3C"));
                break;
        }
        cardUrgencia.setCardBackgroundColor(color);
    }

    private void setupStatusChip() {
        String estado = incidencia.getEstado() != null ? incidencia.getEstado() : "Pendiente";
        tvEstado.setText(estado.toUpperCase());

        int bgRes, textRes, iconRes;

        if (estado.equalsIgnoreCase("En proceso")) {
            bgRes = Color.parseColor("#E3F2FD");
            textRes = Color.parseColor("#1976D2");
            iconRes = android.R.drawable.ic_popup_sync;
        } else if (estado.equalsIgnoreCase("Resuelta")) {
            bgRes = Color.parseColor("#E8F5E9");
            textRes = Color.parseColor("#388E3C");
            iconRes = android.R.drawable.checkbox_on_background;
        } else {
            bgRes = Color.parseColor("#EEEEEE");
            textRes = Color.parseColor("#616161");
            iconRes = android.R.drawable.ic_menu_help;
        }

        cardEstado.setCardBackgroundColor(bgRes);
        tvEstado.setTextColor(textRes);
        ivEstadoIcon.setImageResource(iconRes);
        ivEstadoIcon.setColorFilter(textRes);
    }

    private void setupLocation() {
        if (incidencia.getLatitud() != 0.0 || incidencia.getLongitud() != 0.0) {
            tvLocationText.setText(
                    "Ubicación Registrada\nLat: " + String.format("%.4f", incidencia.getLatitud()) + ", Lon: "
                            + String.format("%.4f", incidencia.getLongitud()));
            tvLocationText.setTextColor(Color.parseColor("#2E7D32"));

            mapView.setVisibility(View.VISIBLE);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    LatLng location = new LatLng(incidencia.getLatitud(), incidencia.getLongitud());
                    googleMap.addMarker(new MarkerOptions().position(location).title("Incidencia"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                }
            });
        } else {
            tvLocationText.setText("Sin ubicación");
            tvLocationText.setTextColor(Color.parseColor("#757575"));
            mapView.setVisibility(View.GONE);
        }
    }

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
