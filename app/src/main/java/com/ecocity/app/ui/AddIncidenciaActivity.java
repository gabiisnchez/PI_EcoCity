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

    // Launchers
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> mapLauncher;

    // Coords
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incidencia);

        etTitulo = findViewById(R.id.etTitulo);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia);

        layoutEstado = findViewById(R.id.layoutEstado);
        spinnerEstado = findViewById(R.id.spinnerEstado);

        tvLocationStatusDetail = findViewById(R.id.tvLocationStatusDetail);

        // Multimedia Views
        ivFoto = findViewById(R.id.ivFoto);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        // Location UI
        Button btnAddLocation = findViewById(R.id.btnAddLocation);
        btnAddLocation.setEnabled(true); // Enable button now!

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        setupLaunchers();
        setupSpinner();

        // Check for Intent extras
        if (getIntent().hasExtra("incidencia")) {
            incidenciaToEdit = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupEditMode();
        } else {
            // Default for new incidence
            tvLocationStatusDetail.setText("Pendiente (Toca 'Añadir Ubicación')");
        }

        btnCamera.setOnClickListener(v -> dispatchTakePictureIntent());

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
                        // For simplicity in this demo, strict file path might be tricky from gallery
                        // URI
                        // without copying file. We will just save URI string for now.
                        currentPhotoPath = uri.toString();

                        // Note: Persisting Gallery URI permissions across restarts is complex.
                        // Ideally copy stream to internal file, but for demo we assume ephemeral or
                        // simple.
                        // To make it robust:
                        try {
                            // Taking persistable permission if possible
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
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creando archivo para foto", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                try {
                    currentPhotoUri = FileProvider.getUriForFile(this,
                            "com.ecocity.app.fileprovider",
                            photoFile);
                    cameraLauncher.launch(currentPhotoUri);
                } catch (Exception e) {
                    Toast.makeText(this, "Error iniciando cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        } else {
            // Fallback or attempt to launch anyway inside try-catch if resolveActivity
            // returns null on some devices despite queries
            // But valid resolveActivity check is better UX
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
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

        // Setup Status Spinner
        String[] estados = { "Pendiente", "En proceso", "Resuelta" };
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estados);
        adapterEstado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstado);
    }

    private void setupEditMode() {
        if (incidenciaToEdit != null) {
            etTitulo.setText(incidenciaToEdit.getTitulo());
            etDescripcion.setText(incidenciaToEdit.getDescripcion());

            // Set spinner selection
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerUrgencia.getAdapter();
            int position = adapter.getPosition(incidenciaToEdit.getUrgencia());
            if (position >= 0) {
                spinnerUrgencia.setSelection(position);
            }

            // Show and set Status
            layoutEstado.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapterEstado = (ArrayAdapter<String>) spinnerEstado.getAdapter();
            int posEstado = adapterEstado.getPosition(incidenciaToEdit.getEstado());
            if (posEstado >= 0) {
                spinnerEstado.setSelection(posEstado);
            }

            // Load Image
            if (incidenciaToEdit.getFotoPath() != null && !incidenciaToEdit.getFotoPath().isEmpty()) {
                currentPhotoPath = incidenciaToEdit.getFotoPath();
                try {
                    // Check if it's a content URI or file path
                    if (currentPhotoPath.startsWith("content://")) {
                        ivFoto.setImageURI(Uri.parse(currentPhotoPath));
                    } else {
                        // Assume absolute file path
                        ivFoto.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
                    }
                    ivFoto.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Location Status
            if (incidenciaToEdit.getLatitud() != 0.0 || incidenciaToEdit.getLongitud() != 0.0) {
                currentLat = incidenciaToEdit.getLatitud();
                currentLng = incidenciaToEdit.getLongitud();
                tvLocationStatusDetail.setText("Ubicación Registrada (" + String.format("%.4f", currentLat) + ", "
                        + String.format("%.4f", currentLng) + ")");
                tvLocationStatusDetail.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvLocationStatusDetail.setText("Pendiente (Toca para añadir)");
                // Here we would enable the button theoretically
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

    private void saveIncidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String urgencia = spinnerUrgencia.getSelectedItem().toString();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Use saved path or empty string
        String fotoPath = currentPhotoPath != null ? currentPhotoPath : "";

        if (incidenciaToEdit != null) {
            // Update existing
            incidenciaToEdit.setTitulo(titulo);
            incidenciaToEdit.setDescripcion(descripcion);
            incidenciaToEdit.setUrgencia(urgencia);
            incidenciaToEdit.setFotoPath(fotoPath);
            incidenciaToEdit.setLatitud(currentLat);
            incidenciaToEdit.setLongitud(currentLng);

            // Update Status
            String estado = spinnerEstado.getSelectedItem().toString();
            incidenciaToEdit.setEstado(estado);

            int result = incidenciaDAO.updateIncidencia(incidenciaToEdit);
            if (result > 0) {
                Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Create new
            Incidencia incidencia = new Incidencia(titulo, descripcion, urgencia, fotoPath, currentLat, currentLng);

            // Set User Email
            com.ecocity.app.utils.SessionManager session = new com.ecocity.app.utils.SessionManager(
                    getApplicationContext());
            String email = session.getUserDetails().get(com.ecocity.app.utils.SessionManager.KEY_EMAIL);
            incidencia.setUserEmail(email);

            // Default status is already Pendiente via Constructor
            long result = incidenciaDAO.insertIncidencia(incidencia);

            if (result != -1) {
                Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
