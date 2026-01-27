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
    private Button btnSave;
    private IncidenciaDAO incidenciaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_incidencia);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerUrgencia = findViewById(R.id.spinnerUrgencia);
        btnSave = findViewById(R.id.btnSave);

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        setupSpinner();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIncidencia();
            }
        });
    }

    private void setupSpinner() {
        String[] urgencias = {"Baja", "Media", "Alta"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, urgencias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgencia.setAdapter(adapter);
    }

    private void saveIncidencia() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String urgencia = spinnerUrgencia.getSelectedItem().toString();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // For Hito 1, photo and location are dummy values
        Incidencia incidencia = new Incidencia(titulo, descripcion, urgencia, "", 0.0, 0.0);
        long result = incidenciaDAO.insertIncidencia(incidencia);

        if (result != -1) {
            Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
            finish(); // Return to list
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
