package com.ecocity.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.database.IncidenciaDAO;
import com.ecocity.app.model.Incidencia;
import com.ecocity.app.ui.AddIncidenciaActivity;
import com.ecocity.app.ui.IncidenciaAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <h1>MainActivity</h1>
 * <p>
 * Clase Principal de la aplicación EcoCity.
 * Actúa como el "Dashboard" o panel de control del usuario.
 * </p>
 * 
 * <h2>Funcionalidades:</h2>
 * <ul>
 * <li>Muestra el listado de incidencias mediante un RecyclerView.</li>
 * <li>Permite navegar a la pantalla de crear incidencia (Floating Action
 * Button).</li>
 * <li>Permite navegar al chat de soporte.</li>
 * <li>Muestra un estado vacío si no hay incidencias.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    // Componente para listas eficientes
    private RecyclerView recyclerView;

    // Botones de acción flotantes (Material Design) para añadir y chatear
    private FloatingActionButton fabAdd, fabChat;

    // Vista que se muestra cuando la lista está vacía (Feedback visual)
    private LinearLayout tvEmpty;

    // Acceso a datos
    private IncidenciaDAO incidenciaDAO;
    private IncidenciaAdapter adapter;
    private List<Incidencia> currentList; // Lista local para ordenar sin recargar

    // Ubicación y Ordenación
    private FusedLocationProviderClient fusedLocationClient;
    private android.location.Location userLocation;
    private ChipGroup chipGroupSort;

    // Monitorización de Red para Sincronización Offline-First
    private com.ecocity.app.utils.NetworkMonitor networkMonitor;

    /**
     * Inicialización de la Actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajustes visuales para pantallas completas modernos
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Binding de vistas
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabChat = findViewById(R.id.fabChat);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configuración del RecyclerView: Usamos un LayoutManager lineal (lista
        // vertical)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        // Inicializar Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar Chips de Ordenación
        chipGroupSort = findViewById(R.id.chipGroupSort);
        chipGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipNearby) {
                checkLocationPermissionAndSort();
            } else {
                // Chip "Urgencia": Ordenar por prioridad (Alta > Media > Baja)
                sortListByUrgency();
            }
        });

        // Listener: Botón Añadir -> Navegar a AddIncidenciaActivity
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddIncidenciaActivity.class);
                startActivity(intent);
            }
        });

        // Listener: Botón Perfil -> Navegar a ProfileActivity
        findViewById(R.id.btnProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.ecocity.app.ui.ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Listener: Botón Chat -> Navegar a ChatActivity
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.ecocity.app.ui.ChatActivity.class);
                startActivity(intent);
            }
        });

        // Iniciar Monitorización de Red (Offline-First Sync)
        networkMonitor = new com.ecocity.app.utils.NetworkMonitor(this);
        networkMonitor.startMonitoring();
    }

    /**
     * Ciclo de Vida: onResume
     * Se llama cada vez que la actividad vuelve a primer plano (ej. al volver de
     * Añadir Incidencia).
     * Ideal para recargar la lista de datos y mostrar los cambios más recientes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadIncidencias();
    }

    /**
     * Carga todas las incidencias de la base de datos y actualiza la lista visual.
     * Gestiona la visibilidad de la vista "Vacía" vs el RecyclerView.
     */
    private void loadIncidencias() {
        // Mostrar estado de carga (opcional, por ahora solo limpiamos/ocultamos)
        // Podríamos poner un ProgressBar aquí

        // Recuperar datos de Firestore asíncronamente
        incidenciaDAO.getAllIncidencias(new IncidenciaDAO.FirestoreCallback() {
            @Override
            public void onDataLoaded(List<Incidencia> lista) {
                currentList = lista; // Guardar referencia local
                if (lista.isEmpty()) {
                    // Si no hay datos: Mostrar mensaje de vacío y ocultar lista
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Si hay datos: Ocultar mensaje vacío y mostrar lista
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (adapter == null) {
                        // Primera vez: Crear adaptador y asignarlo
                        adapter = new IncidenciaAdapter(lista);
                        recyclerView.setAdapter(adapter);
                    } else {
                        // Veces subsecuentes: Actualizar datos en el adaptador existente
                        adapter.updateData(lista);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Manejar error (ej: sin conexión)
                android.widget.Toast.makeText(MainActivity.this,
                        "Error al cargar incidencias: " + e.getMessage(),
                        android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String result) {
            }
        });
    }

    /**
     * Verifica permisos de ubicación. Si los tiene, obtiene la ubicación y ordena.
     * Si no, los pide.
     */
    private void checkLocationPermissionAndSort() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            getUserLocationAndSort();
        } else {
            // Pedir permiso
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Obtiene la última ubicación conocida y ordena la lista.
     */
    private void getUserLocationAndSort() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = location;
                        sortListByProximity();
                    } else {
                        android.widget.Toast.makeText(this, "No se pudo obtener la ubicación actual",
                                android.widget.Toast.LENGTH_SHORT).show();
                        // Volver a selección "Urgencia" visualmente si falla
                        chipGroupSort.check(R.id.chipUrgency);
                    }
                });
    }

    private void sortListByProximity() {
        if (currentList == null || userLocation == null)
            return;

        Collections.sort(currentList, new Comparator<Incidencia>() {
            @Override
            public int compare(Incidencia o1, Incidencia o2) {
                float[] results1 = new float[1];
                float[] results2 = new float[1];

                android.location.Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                        o1.getLatitud(), o1.getLongitud(), results1);
                android.location.Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                        o2.getLatitud(), o2.getLongitud(), results2);

                return Float.compare(results1[0], results2[0]);
            }
        });

        if (adapter != null) {
            adapter.updateData(currentList);
            // Hacer scroll al inicio
            recyclerView.scrollToPosition(0);
        }
    }

    private void sortListByUrgency() {
        if (currentList != null && adapter != null) {
            Collections.sort(currentList, new Comparator<Incidencia>() {
                @Override
                public int compare(Incidencia o1, Incidencia o2) {
                    int p1 = getUrgencyPriority(o1.getUrgencia());
                    int p2 = getUrgencyPriority(o2.getUrgencia());
                    return Integer.compare(p1, p2);
                }

                private int getUrgencyPriority(String urgencia) {
                    if (urgencia == null)
                        return 4;
                    switch (urgencia.toLowerCase()) {
                        case "alta":
                            return 1;
                        case "media":
                            return 2;
                        case "baja":
                            return 3;
                        default:
                            return 4;
                    }
                }
            });
            adapter.updateData(currentList);
            recyclerView.scrollToPosition(0);
        }
    }

    // Launcher para permisos de ubicación
    private final androidx.activity.result.ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getUserLocationAndSort();
                } else {
                    android.widget.Toast.makeText(this, "Permiso de ubicación necesario para ordenar por cercanía",
                            android.widget.Toast.LENGTH_LONG).show();
                    chipGroupSort.check(R.id.chipUrgency);
                }
            });

    /**
     * Ciclo de vida: onDestroy
     * Limpieza de conexiones a base de datos.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }
    }
}
