package com.ecocity.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ecocity.app.R;
import com.ecocity.app.database.IncidenciaDAO;
import com.ecocity.app.model.Incidencia;
import com.google.android.material.textfield.TextInputEditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * <h1>AddIncidenciaActivity</h1>
 * <p>
 * Clase principal para la gestión de incidencias (Creación y Edición).
 * Esta actividad permite al usuario interactuar con el sistema para reportar
 * nuevos problemas
 * o modificar los existentes.
 * </p>
 *
 * <h2>Funcionalidades Principales:</h2>
 * <ul>
 * <li><b>Formulario de Datos:</b> Título, Descripción, Urgencia.</li>
 * <li><b>Multimedia:</b> Captura de fotos con cámara o selección de
 * galería.</li>
 * <li><b>Geolocalización:</b> Selección de ubicación mediante mapa (Google
 * Maps).</li>
 * <li><b>Persistencia:</b> Guardado en base de datos SQLite local.</li>
 * <li><b>Concurrencia:</b> Uso de Hilos (Threads) para operaciones de E/S
 * (Entrada/Salida).</li>
 * </ul>
 *
 * @author EcoCity Dev Team
 * @version 1.0
 */
public class AddIncidenciaActivity extends AppCompatActivity {

    // --- Componentes de la Interfaz de Usuario (UI) ---
    // Usamos TextInputEditText de Material Design para mejor experiencia de usuario
    private TextInputEditText etTitulo, etDescripcion;

    // Spinners para selección de opciones predefinidas
    private Spinner spinnerUrgencia;
    private Spinner spinnerEstado;

    // Layout contenedor para el estado (solo visible en modo edición)
    private android.widget.LinearLayout layoutEstado;

    // TextView para mostrar coordenadas o estado de la ubicación
    private android.widget.TextView tvLocationStatusDetail;

    // Botones de acción principal
    private Button btnSave; // Guardar o Actualizar
    private Button btnDelete; // Eliminar (solo modo edición)

    // --- Componentes Multimedia ---
    private ImageView ivFoto; // Vista previa de la imagen
    private Button btnCamera; // Botón para abrir la cámara
    private Button btnGallery; // Botón para abrir la galería

    // Variables para gestionar la ruta de la imagen
    private String currentPhotoPath; // Ruta absoluta del archivo en disco
    private Uri currentPhotoUri; // URI content:// para compartir con otras apps (Cámara)

    // --- Acceso a Datos (DAO) ---
    private IncidenciaDAO incidenciaDAO; // Objeto para interactuar con la tabla 'incidencias'

    // Objeto que almacena la incidencia si estamos editando (null si es nueva)
    private Incidencia incidenciaToEdit;

    // --- Activity Result API Callbacks ---
    // Reemplazo moderno de startActivityForResult() para manejar respuestas de
    // otras Actividades
    private ActivityResultLauncher<Uri> cameraLauncher; // Resultado de la cámara
    private ActivityResultLauncher<String> galleryLauncher; // Resultado de la galería
    private ActivityResultLauncher<Intent> mapLauncher; // Resultado del mapa (Coordenadas)
    private ActivityResultLauncher<String> requestCameraPermissionLauncher; // Resultado de permisos

    // --- Variables de Estado ---
    private double currentLat = 0.0; // Latitud seleccionada
    private double currentLng = 0.0; // Longitud seleccionada

    // Mapa de previsualización
    private MapView mapView;

    /**
     * Método onCreate(): Punto de entrada de la Activity.
     * Se ejecuta al crear la instancia de la clase.
     *
     * @param savedInstanceState Estado previo guardado (si existe) para restaurar
     *                           la interfaz.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilita el diseño de borde a borde (Edge-to-Edge) para pantallas modernas
        androidx.activity.EdgeToEdge.enable(this);

        // Asocia el layout XML a esta actividad java
        setContentView(R.layout.activity_add_incidencia);

        // Ajusta los márgenes para evitar que la UI quede detrás de las barras del
        // sistema
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 1. Inicialización de Vistas (View Binding manual) ---
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia);
        layoutEstado = findViewById(R.id.layoutEstado);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        tvLocationStatusDetail = findViewById(R.id.tvLocationStatusDetail);

        // Botón Atrás (Toolbar personalizada)
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Cierra la actividad actual

        // Vistas Multimedia
        ivFoto = findViewById(R.id.ivFoto);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        // Botón Ubicación
        Button btnAddLocation = findViewById(R.id.btnAddLocation);
        btnAddLocation.setEnabled(true);

        // Botones de Acción
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // --- 2. Inicialización de Base de Datos ---
        // Abrimos la conexión writable con la BD
        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        // --- 3. Configuración Inicial ---
        setupLaunchers(); // Configurar callbacks de resultados
        setupSpinner(); // Configurar listas desplegables

        // --- 4. Lógica de Modo Edición vs Creación ---
        // Verificamos si el Intent trae un objeto 'incidencia'
        if (getIntent().hasExtra("incidencia")) {
            // MODO EDICIÓN: Recuperamos el objeto serializado
            incidenciaToEdit = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupEditMode(); // Rellenamos el formulario con los datos existentes
        } else {
            // MODO CREACIÓN: Estado inicial por defecto
            tvLocationStatusDetail.setText(getString(R.string.text_location_pending));
        }

        // --- 5. Configuración de Listeners (Eventos) ---

        // Botón Cámara: Verificamos permisos antes de intentar abrirla
        btnCamera.setOnClickListener(v -> checkCameraPermissionAndOpen());

        // Botón Galería: Lanzamos selector de contenido tipo imagen
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Botón Mapa: Abrimos MapActivity esperando un resultado
        btnAddLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddIncidenciaActivity.this, MapActivity.class);
            mapLauncher.launch(intent);
        });

        // Botón Guardar
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIncidencia(); // Llamada al método que valida y guarda
            }
        });

        // Botón Eliminar
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteIncidencia(); // Llamada al método que elimina el registro
            }
        });
    }

    /**
     * Inicializa los ActivityResultLaunchers.
     * Estos objetos manejan las respuestas asíncronas de otras actividades o
     * diálogos del sistema.
     */
    private void setupLaunchers() {
        // Callback para la Cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        // La foto se guardó exitosamente en 'currentPhotoUri'
                        ivFoto.setPadding(0, 0, 0, 0); // Ajuste visual
                        ivFoto.setImageURI(currentPhotoUri); // Mostrar en UI
                    }
                });

        // Callback para la Galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Usuario seleccionó una imagen
                        ivFoto.setPadding(0, 0, 0, 0);
                        ivFoto.setImageURI(uri);

                        // Guardamos la URI como cadena.
                        // IMPORTANTE: En producción, se recomienda copiar el archivo a un directorio
                        // local de la app
                        // para garantizar persistencia a largo plazo.
                        currentPhotoPath = uri.toString();

                        try {
                            // Solicitamos persistencia de permisos de lectura para la URI
                            // Esto evita SecurityExceptions al intentar leer la imagen tras reiniciar
                            getContentResolver().takePersistableUriPermission(uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Callback para el Mapa
        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Verificamos que la actividad terminó OK y tiene datos
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Extraemos coordenadas del Intent de retorno
                        currentLat = result.getData().getDoubleExtra("lat", 0.0);
                        currentLng = result.getData().getDoubleExtra("lng", 0.0);

                        // Actualizamos UI con feedback visual (Texto y Color)
                        tvLocationStatusDetail.setText(String.format(getString(R.string.text_location_registered),
                                String.format("%.4f", currentLat), String.format("%.4f", currentLng)));
                        tvLocationStatusDetail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        
                        // Mostrar visualmente en mapa
                        setupLocationPreview();
                    }
                });

        // Callback para Solicitud de Permisos
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permiso concedido -> Abrir cámara
                        dispatchTakePictureIntent();
                    } else {
                        // Permiso denegado -> Explicar al usuario
                        Toast.makeText(this, getString(R.string.msg_camera_permission), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    /**
     * Prepara el archivo y lanza la intención de captura de imagen.
     * Utiliza FileProvider para compartir seguramente el archivo con la app de
     * cámara externa.
     */
    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile(); // Crea el fichero físico temporal
        } catch (IOException ex) {
            Toast.makeText(this, getString(R.string.msg_error_camera_file), Toast.LENGTH_SHORT).show();
            return;
        }

        // Si el archivo se creó correctamente, procedemos
        if (photoFile != null) {
            try {
                // Obtener URI segura mediante FileProvider (com.ecocity.app.fileprovider)
                // Requerido desde Android 7.0 (API 24) para evitar FileUriExposedException
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.ecocity.app.fileprovider",
                        photoFile);

                // Lanzar la actividad de cámara del sistema
                cameraLauncher.launch(currentPhotoUri);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, getString(R.string.msg_no_camera_app), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error reservando cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * Crea un archivo de imagen temporal con nombre único.
     * 
     * @return Archivo File creado en el directorio de imágenes privado de la app.
     * @throws IOException Si falla la creación del archivo.
     */
    private File createImageFile() throws IOException {
        // Generar nombre de archivo único basado en fecha y hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Directorio de almacenamiento externo privado de la app (No requiere permiso
        // WRITE_EXTERNAL_STORAGE en Android 10+)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Crear archivo temporal
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );

        // Guardar ruta absoluta para persistencia en BD
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Configura los adaptadores de datos para los Spinners.
     */
    private void setupSpinner() {
        // 1. Spinner Urgencia
        String[] urgencias = { getString(R.string.urgency_low), getString(R.string.urgency_medium),
                getString(R.string.urgency_high) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urgencias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgencia.setAdapter(adapter);

        // 2. Spinner Estado
        String[] estados = { getString(R.string.status_pending), getString(R.string.status_in_process),
                getString(R.string.status_resolved) };
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);
    }

    /**
     * Verifica permiso de cámara y actúa segun el estado.
     */
    private void checkCameraPermissionAndOpen() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido
            dispatchTakePictureIntent();
        } else {
            // Solicitar permiso
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    /**
     * Muestra una previsualización de la ubicación en un mapa tras seleccionarla.
     */
    private void setupLocationPreview() {
        android.widget.FrameLayout mapContainer = findViewById(R.id.mapContainer);
        if (mapContainer == null) return;

        if (currentLat != 0.0 || currentLng != 0.0) {
            mapContainer.setVisibility(View.VISIBLE);
            
            if (mapView == null) {
                try {
                    com.google.android.gms.maps.GoogleMapOptions options = new com.google.android.gms.maps.GoogleMapOptions().liteMode(true);
                    mapView = new com.google.android.gms.maps.MapView(this, options);
                    mapContainer.addView(mapView);
                    mapView.onCreate(null);
                    mapView.onResume(); 
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (googleMap == null) return;
                    LatLng location = new LatLng(currentLat, currentLng);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(location));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                }
            });
        }
    }

    /**
     * Configura la interfaz con los valores de la incidencia a editar.
     */
    private void setupEditMode() {
        if (incidenciaToEdit != null) {
            // Rellenar campos de texto
            etTitulo.setText(incidenciaToEdit.getTitulo());
            etDescripcion.setText(incidenciaToEdit.getDescripcion());

            // Seleccionar Urgencia
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUrgencia.getAdapter();
            int position = adapter.getPosition(incidenciaToEdit.getUrgencia());
            if (position >= 0) {
                spinnerUrgencia.setSelection(position);
            }

            // Mostrar y configurar Spinner Estado
            layoutEstado.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapterEstado = (ArrayAdapter<String>) spinnerEstado.getAdapter();
            int posEstado = adapterEstado.getPosition(incidenciaToEdit.getEstado());
            if (posEstado >= 0) {
                spinnerEstado.setSelection(posEstado);
            }

            // Cargar Imagen Previa
            if (incidenciaToEdit.getFotoPath() != null && !incidenciaToEdit.getFotoPath().isEmpty()) {
                currentPhotoPath = incidenciaToEdit.getFotoPath();
                try {
                    // Gestionar carga según si es Content URI o File Path
                    if (currentPhotoPath.startsWith("content://")) {
                        ivFoto.setImageURI(Uri.parse(currentPhotoPath));
                    } else {
                        ivFoto.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
                    }
                    ivFoto.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Configurar visualización de Ubicación
            if (incidenciaToEdit.getLatitud() != 0.0 || incidenciaToEdit.getLongitud() != 0.0) {
                currentLat = incidenciaToEdit.getLatitud();
                currentLng = incidenciaToEdit.getLongitud();
                tvLocationStatusDetail.setText(String.format(getString(R.string.text_location_registered),
                        String.format("%.4f", currentLat), String.format("%.4f", currentLng)));
                tvLocationStatusDetail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                setupLocationPreview();
            } else {
                tvLocationStatusDetail.setText(getString(R.string.text_location_pending));
            }

            // Cambiar texto de botón para reflejar acción
            btnSave.setText(getString(R.string.btn_update));
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Elimina la incidencia de la base de datos.
     */
    private void deleteIncidencia() {
        if (incidenciaToEdit != null) {
            // Optimistic UI for delete
            Toast.makeText(this, getString(R.string.msg_deleted), Toast.LENGTH_SHORT).show();
            finish();

            incidenciaDAO.deleteIncidencia(incidenciaToEdit.getId(), new IncidenciaDAO.FirestoreCallback() {
                @Override
                public void onSuccess(String result) {
                    android.util.Log.d("AddIncidencia", "Incidencia eliminada: " + result);
                }

                @Override
                public void onFailure(Exception e) {
                    android.util.Log.e("AddIncidencia", "Error al eliminar: " + e.getMessage());
                }

                @Override
                public void onDataLoaded(java.util.List<Incidencia> incidencias) {
                }
            });
        }
    }

    /**
     * Guarda o actualiza la incidencia en la Base de Datos.
     * <p>
     * <b>ASPECTO CLAVE: Uso de Hilos (Threads)</b><br>
     * Las operaciones de base de datos son bloqueantes. Para mantener la interfaz
     * fluida (ANR free),
     * ejecutamos la inserción/actualización en un hilo separado (Worker Thread).
     * Luego, usamos runOnUiThread() para volver al hilo principal y mostrar el
     * resultado.
     * </p>
     */
    private void saveIncidencia() {
        // 1. Recoger datos de la UI
        final String titulo = etTitulo.getText().toString().trim();
        final String descripcion = etDescripcion.getText().toString().trim();
        final String urgencia = spinnerUrgencia.getSelectedItem().toString();

        // 2. Validación
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Datos opcionales
        final String fotoPath = currentPhotoPath != null ? currentPhotoPath : "";
        final String estado = spinnerEstado.getSelectedItem().toString();

        // 3. UI Optimista: Asumimos éxito y cerramos
        // Firestore maneja la persistencia offline y sincronización automáticamente.
        Toast.makeText(this,
                incidenciaToEdit != null ? getString(R.string.msg_updated)
                        : getString(R.string.msg_saved),
                Toast.LENGTH_SHORT).show();
        finish();

        // 4. Lógica de Negocio en segundo plano (Fire and Forget para la UI)
        IncidenciaDAO.FirestoreCallback callback = new IncidenciaDAO.FirestoreCallback() {
            @Override
            public void onSuccess(String result) {
                // Éxito en sincronización (puede ocurrir más tarde)
                android.util.Log.d("AddIncidencia", "Incidencia guardada/sincronizada ID: " + result);
            }

            @Override
            public void onFailure(Exception e) {
                // Error real (ej: permisos denegados).
                // Como la actividad ya se cerró, solo podemos loguear.
                android.util.Log.e("AddIncidencia", "Error al guardar en Firestore: " + e.getMessage());
            }

            @Override
            public void onDataLoaded(java.util.List<Incidencia> incidencias) {
            }
        };

        if (incidenciaToEdit != null) {
            // Update
            incidenciaToEdit.setTitulo(titulo);
            incidenciaToEdit.setDescripcion(descripcion);
            incidenciaToEdit.setUrgencia(urgencia);
            incidenciaToEdit.setFotoPath(fotoPath);
            incidenciaToEdit.setLatitud(currentLat);
            incidenciaToEdit.setLongitud(currentLng);
            incidenciaToEdit.setEstado(estado);

            incidenciaDAO.updateIncidencia(incidenciaToEdit, callback);
        } else {
            // Insert
            Incidencia incidencia = new Incidencia(titulo, descripcion, urgencia, fotoPath, currentLat,
                    currentLng);

            // Asignar al usuario actual
            com.ecocity.app.utils.SessionManager session = new com.ecocity.app.utils.SessionManager(
                    getApplicationContext());
            String email = session.getUserDetails().get(com.ecocity.app.utils.SessionManager.KEY_EMAIL);
            incidencia.setUserEmail(email);

            incidenciaDAO.insertIncidencia(incidencia, callback);
        }
    }

    /**
     * Método onDestroy(): Se llama cuando la actividad se destruye.
     * Ideal para limpieza de recursos.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cerramos conexión a base de datos para evitar fugas de memoria
        if (incidenciaDAO != null) {
            incidenciaDAO.close();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
}
