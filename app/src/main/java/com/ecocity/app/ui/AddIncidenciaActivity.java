package com.ecocity.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import com.google.android.material.textfield.TextInputEditText;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ImageView;
import com.ecocity.app.model.Incidencia;
import com.ecocity.app.database.IncidenciaDAO;
import com.ecocity.app.R;

/**
 * Actividad para crear o editar una incidencia.
 * Permite capturar fotos, seleccionar ubicación y guardar los datos en la base de datos local.
 * Implementa lógica en segundo plano para no bloquear la UI durante el guardado.
 */
public class AddIncidenciaActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion;
    private Spinner spinnerUrgencia;
    private android.widget.LinearLayout layoutEstado;
    private Spinner spinnerEstado;

    private android.widget.TextView tvLocationStatusDetail;
    private Button btnSave;
    private Button btnDelete;

    // Multimedia
    private ImageView ivFoto;
    private Button btnCamera, btnGallery;
    private String currentPhotoPath;
    private Uri currentPhotoUri;

    private IncidenciaDAO incidenciaDAO;
    private Incidencia incidenciaToEdit;

    // Launchers para resultados de actividades (Cámara, Galería, Mapa, Permisos)
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // Coordenadas
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_incidencia);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitulo = findViewById(R.id.etTitulo);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia);

        layoutEstado = findViewById(R.id.layoutEstado);
        spinnerEstado = findViewById(R.id.spinnerEstado);

        tvLocationStatusDetail = findViewById(R.id.tvLocationStatusDetail);

        // Vistas Multimedia
        ivFoto = findViewById(R.id.ivFoto);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        // UI Ubicación
        Button btnAddLocation = findViewById(R.id.btnAddLocation);
        btnAddLocation.setEnabled(true);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        setupLaunchers();
        setupSpinner();

        // Comprobar si hay extras (Modo Edición)
        if (getIntent().hasExtra("incidencia")) {
            incidenciaToEdit = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupEditMode();
        } else {
            // Por defecto para nueva incidencia
            tvLocationStatusDetail.setText("Pendiente (Toca 'Añadir Ubicación')");
        }

        btnCamera.setOnClickListener(v -> checkCameraPermissionAndOpen());

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnAddLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddIncidenciaActivity.this, MapActivity.class);
            mapLauncher.launch(intent);
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIncidencia();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteIncidencia();
            }
        });
    }

    /**
     * Configura los ActivityResultLaunchers para manejar los retornos de:
     * - Cámara (Foto capturada)
     * - Galería (Imagen seleccionada)
     * - Mapa (Ubicación seleccionada)
     * - Permisos (Solicitud de permiso de cámara)
     */
    private void setupLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        ivFoto.setPadding(0, 0, 0, 0);
                        ivFoto.setImageURI(currentPhotoUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivFoto.setPadding(0, 0, 0, 0);
                        ivFoto.setImageURI(uri);
                        // Para simplificar en esta demo, guardamos el toString del URI.
                        // Lo ideal sería copiar el archivo a almacenamiento interno.
                        currentPhotoPath = uri.toString();

                        try {
                            // Intentar persistir el permiso de lectura del URI
                            getContentResolver().takePersistableUriPermission(uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        mapLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        currentLat = result.getData().getDoubleExtra("lat", 0.0);
                        currentLng = result.getData().getDoubleExtra("lng", 0.0);

                        tvLocationStatusDetail.setText("Ubicación Registrada (" + String.format("%.4f", currentLat)
                                + ", " + String.format("%.4f", currentLng) + ")");
                        tvLocationStatusDetail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                });

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dispatchTakePictureIntent();
                    } else {
                        Toast.makeText(this, "Permiso de cámara necesario para tomar fotos", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void dispatchTakePictureIntent() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creando archivo para foto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            try {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        "com.ecocity.app.fileprovider",
                        photoFile);
                cameraLauncher.launch(currentPhotoUri);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error reservando cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setupSpinner() {
        String[] urgencias = { "Baja", "Media", "Alta" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urgencias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgencia.setAdapter(adapter);

        // Configurar Spinner de Estado
        String[] estados = { "Pendiente", "En proceso", "Resuelta" };
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);
    }

    private void checkCameraPermissionAndOpen() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void setupEditMode() {
        if (incidenciaToEdit != null) {
            etTitulo.setText(incidenciaToEdit.getTitulo());
            etDescripcion.setText(incidenciaToEdit.getDescripcion());

            // Seleccionar Spinner Urgencia
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

            // Cargar Imagen
            if (incidenciaToEdit.getFotoPath() != null && !incidenciaToEdit.getFotoPath().isEmpty()) {
                currentPhotoPath = incidenciaToEdit.getFotoPath();
                try {
                    // Verificar si es content URI o ruta de archivo
                    if (currentPhotoPath.startsWith("content://")) {
                        ivFoto.setImageURI(Uri.parse(currentPhotoPath));
                    } else {
                        // Asumir ruta absoluta
                        ivFoto.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
                    }
                    ivFoto.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Estado de Ubicación
            if (incidenciaToEdit.getLatitud() != 0.0 || incidenciaToEdit.getLongitud() != 0.0) {
                currentLat = incidenciaToEdit.getLatitud();
                currentLng = incidenciaToEdit.getLongitud();
                tvLocationStatusDetail.setText("Ubicación Registrada (" + String.format("%.4f", currentLat) + ", "
                        + String.format("%.4f", currentLng) + ")");
                tvLocationStatusDetail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvLocationStatusDetail.setText("Pendiente (Toca para añadir)");
            }

            btnSave.setText("Actualizar");
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    private void deleteIncidencia() {
        if (incidenciaToEdit != null) {
            incidenciaDAO.deleteIncidencia(incidenciaToEdit.getId());
            Toast.makeText(this, "Incidencia eliminada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Guarda la incidencia en la base de datos.
     * Ejecuta la operación de inserción/actualización en un hilo secundario (Background Thread)
     * para cumplir con los requisitos de PSP y evitar bloquear la UI.
     */
    private void saveIncidencia() {
        final String titulo = etTitulo.getText().toString().trim();
        final String descripcion = etDescripcion.getText().toString().trim();
        final String urgencia = spinnerUrgencia.getSelectedItem().toString();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Usar ruta guardada o cadena vacía
        final String fotoPath = currentPhotoPath != null ? currentPhotoPath : "";
        final String estado = spinnerEstado.getSelectedItem().toString();

        // Deshabilitar botón para evitar múltiples clics
        btnSave.setEnabled(false);

        // Requisito PSP: Hilo en segundo plano para operaciones de BBDD/Multimedia
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = false;

                if (incidenciaToEdit != null) {
                    // Actualizar existente
                    incidenciaToEdit.setTitulo(titulo);
                    incidenciaToEdit.setDescripcion(descripcion);
                    incidenciaToEdit.setUrgencia(urgencia);
                    incidenciaToEdit.setFotoPath(fotoPath);
                    incidenciaToEdit.setLatitud(currentLat);
                    incidenciaToEdit.setLongitud(currentLng);
                    incidenciaToEdit.setEstado(estado);

                    int result = incidenciaDAO.updateIncidencia(incidenciaToEdit);
                    success = (result > 0);
                } else {
                    // Crear nueva
                    Incidencia incidencia = new Incidencia(titulo, descripcion, urgencia, fotoPath, currentLat, currentLng);

                    // Asignar Email de Usuario
                    com.ecocity.app.utils.SessionManager session = new com.ecocity.app.utils.SessionManager(getApplicationContext());
                    String email = session.getUserDetails().get(com.ecocity.app.utils.SessionManager.KEY_EMAIL);
                    incidencia.setUserEmail(email);

                    long result = incidenciaDAO.insertIncidencia(incidencia);
                    success = (result != -1);
                }

                // Las actualizaciones de UI deben ser en el Hilo Principal
                final boolean finalSuccess = success;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSave.setEnabled(true); // Reactivar por seguridad flujo
                        if (finalSuccess) {
                            Toast.makeText(AddIncidenciaActivity.this, 
                                    incidenciaToEdit != null ? "Actualizado correctamente" : getString(R.string.msg_saved), 
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddIncidenciaActivity.this, 
                                    "Error al guardar incidencia", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
