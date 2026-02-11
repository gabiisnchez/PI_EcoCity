package com.ecocity.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.ecocity.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.view.inputmethod.EditorInfo;

/**
 * <h1>MapActivity</h1>
 * <p>
 * Actividad que muestra un mapa interactivo de Google Maps.
 * Permite al usuario seleccionar una ubicación geográfica para una incidencia.
 * </p>
 * 
 * <h2>Funcionalidades:</h2>
 * <ul>
 * <li>Visualización de mapa (Google Maps SDK).</li>
 * <li>Búsqueda de direcciones por texto (Geocoding).</li>
 * <li>Detección de ubicación actual del usuario (GPS/Red).</li>
 * <li>Selección visual de coordenadas mediante el centro del mapa.</li>
 * </ul>
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // UI Elements
    private Button btnConfirmLocation; // Botón flotante para confirmar
    private EditText etSearch; // Barra de búsqueda
    private ImageButton btnSearch; // Lupa

    // Cliente de Ubicación de Google Play Services
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    // Gestor de Permisos (Activity Result API)
    // Solicita permisos de ubicación fina (GPS) o aproximada (Red)
    private final androidx.activity.result.ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        false);
                Boolean coarseLocationGranted = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Permiso preciso concedido
                    enableUserLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Permiso aproximado concedido
                    enableUserLocation();
                } else {
                    // Permiso denegado: Mostrar ubicación por defecto (Madrid - Puerta del Sol)
                    LatLng madrid = new LatLng(40.416775, -3.703790);
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        // Ajustes Edge-to-Edge
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Binding de vistas
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Inicializar cliente de ubicación
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

        // Listeners de búsqueda
        btnSearch.setOnClickListener(v -> searchLocation());

        // Permitir buscar pulsando "Enter" o "Buscar" en el teclado virtual
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation();
                return true;
            }
            return false;
        });

        // Inicializar fragmento del mapa de forma asíncrona
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Listener de confirmación
        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    // Obtener las coordenadas del centro exacto de la pantalla (cámara)
                    LatLng target = mMap.getCameraPosition().target;

                    // Devolver resultado a la actividad padre
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", target.latitude);
                    resultIntent.putExtra("lng", target.longitude);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    /**
     * Callback: El mapa está listo para usarse.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Añadir padding inferior para que el logo de Google y los controles de zoom
        // no queden tapados por nuestro botón de confirmar.
        float density = getResources().getDisplayMetrics().density;
        int bottomPadding = (int) (100 * density);
        mMap.setPadding(0, 0, 0, bottomPadding);

        // Habilitar controles de zoom (+/-)
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Iniciar proceso de permisos y ubicación
        checkLocationPermission();
    }

    /**
     * Verifica si tenemos permisos de ubicación.
     * Si los tenemos, activa la capa de ubicación. Si no, los solicita.
     */
    private void checkLocationPermission() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ||
                androidx.core.content.ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            // Solicitar permisos en tiempo de ejecución
            locationPermissionRequest.launch(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Activa el botón "My Location" (punto azul) y mueve la cámara a la última
     * ubicación conocida.
     */
    private void enableUserLocation() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ||
                androidx.core.content.ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true); // Muestra punto azul

            // Obtener última ubicación conocida (puede ser null si el GPS estaba apagado)
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                        } else {
                            // Fallback (Respaldo): Si no hay ubicación, ir a Madrid
                            LatLng madrid = new LatLng(40.416775, -3.703790);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 15f));
                        }
                    });
        }
    }

    /**
     * Realiza una búsqueda de dirección usando Geocoder.
     * Traduce texto (ej: "Calle Gran Vía 1") a coordenadas.
     */
    private void searchLocation() {
        String location = etSearch.getText().toString();
        if (location == null || location.equals("")) {
            Toast.makeText(this, getString(R.string.map_msg_enter_address), Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Obtener hasta 1 resultado
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Mover cámara al resultado con animación
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

                // Ocultar teclado virtual
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                }
            } else {
                Toast.makeText(this, getString(R.string.map_msg_not_found), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.map_msg_error), Toast.LENGTH_SHORT).show();
        }
    }
}
