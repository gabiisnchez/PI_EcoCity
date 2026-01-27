package com.ecocity.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ecocity.app.R;
import com.ecocity.app.database.IncidenciaDAO;
import com.ecocity.app.model.Incidencia;
import com.google.android.material.textfield.TextInputEditText;

public class AddIncidenciaActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion;
    private Spinner spinnerUrgencia;
    private android.widget.TextView tvLocationStatusDetail;
    private Button btnSave;
    private Button btnDelete;
    private IncidenciaDAO incidenciaDAO;
    private Incidencia incidenciaToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incidencia);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia);
        tvLocationStatusDetail = findViewById(R.id.tvLocationStatusDetail);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        setupSpinner();

        // Check for Intent extras
        if (getIntent().hasExtra("incidencia")) {
            incidenciaToEdit = (Incidencia) getIntent().getSerializableExtra("incidencia");
            setupEditMode();
        } else {
            // Default for new incidence
            tvLocationStatusDetail.setText("Pendiente (Se podrá añadir tras guardar)");
        }

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

    private void setupSpinner() {
        String[] urgencias = { "Baja", "Media", "Alta" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urgencias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgencia.setAdapter(adapter);
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

            // Location Status
            if (incidenciaToEdit.getLatitud() != 0.0 || incidenciaToEdit.getLongitud() != 0.0) {
                tvLocationStatusDetail.setText("Ubicación Registrada");
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

        if (incidenciaToEdit != null) {
            // Update existing
            incidenciaToEdit.setTitulo(titulo);
            incidenciaToEdit.setDescripcion(descripcion);
            incidenciaToEdit.setUrgencia(urgencia);

            int result = incidenciaDAO.updateIncidencia(incidenciaToEdit);
            if (result > 0) {
                Toast.makeText(this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Create new
            Incidencia incidencia = new Incidencia(titulo, descripcion, urgencia, "", 0.0, 0.0);
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
