package com.ecocity.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ecocity.app.database.IncidenciaDAO;
import com.ecocity.app.model.Incidencia;
import com.ecocity.app.ui.AddIncidenciaActivity;
import com.ecocity.app.ui.IncidenciaAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * Actividad Principal de EcoCity.
 * Muestra el listado de incidencias del usuario, permitiendo filtrar, navegar a detalles
 * o crear nuevas incidencias.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd, fabChat;
    private LinearLayout tvEmpty;
    private IncidenciaDAO incidenciaDAO;
    private IncidenciaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        fabChat = findViewById(R.id.fabChat);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        incidenciaDAO = new IncidenciaDAO(this);
        incidenciaDAO.open();

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddIncidenciaActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.ecocity.app.ui.ProfileActivity.class);
                startActivity(intent);
            }
        });

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.ecocity.app.ui.ChatActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Se llama cuando la actividad vuelve a estar visible.
     * Recarga el listado de incidencias para reflejar posibles cambios.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadIncidencias();
    }

    /**
     * Carga todas las incidencias de la base de datos y actualiza el adaptador del RecyclerView.
     * Si no hay incidencias, muestra una vista de estado vacío.
     */
    private void loadIncidencias() {
        List<Incidencia> lista = incidenciaDAO.getAllIncidencias();

        if (lista.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new IncidenciaAdapter(lista);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(lista);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
