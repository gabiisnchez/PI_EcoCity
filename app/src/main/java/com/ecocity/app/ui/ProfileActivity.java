package com.ecocity.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ecocity.app.R;
import com.ecocity.app.utils.SessionManager;
import java.util.HashMap;

/**
 * <h1>ProfileActivity</h1>
 * <p>
 * Pantalla de Perfil de Usuario.
 * Muestra la información del usuario logueado y estadísticas de su actividad.
 * </p>
 * 
 * <h2>Funcionalidades:</h2>
 * <ul>
 * <li>Visualización de Nombre y Email del usuario actual.</li>
 * <li>Estadísticas de incidencias (Total, Resueltas, En Proceso,
 * Pendientes).</li>
 * <li>Botón de Cerrar Sesión (Logout).</li>
 * </ul>
 */
public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvUserName, tvUserEmail;
    private Button btnLogout;

    // Vistas de Estadísticas
    private TextView tvCountTotal, tvCountResolved, tvCountProcess, tvCountPending;

    // DAO para consultar estadísticas
    private com.ecocity.app.database.IncidenciaDAO incidenciaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Ajustes Edge-to-Edge
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar DAO
        incidenciaDAO = new com.ecocity.app.database.IncidenciaDAO(this);

        // Inicializar Sesión
        session = new SessionManager(getApplicationContext());
        // Verificar logueo (Redirige a Login si no está logueado)
        session.checkLogin();

        // Bindings
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);

        // Bindings de Stats
        tvCountTotal = findViewById(R.id.tvCountTotal);
        tvCountResolved = findViewById(R.id.tvCountResolved);
        tvCountProcess = findViewById(R.id.tvCountProcess);
        tvCountPending = findViewById(R.id.tvCountPending);

        // Mostrar datos de usuario desde la sesión (SharedPreferences)
        HashMap<String, String> user = session.getUserDetails();
        String name = user.get(SessionManager.KEY_NAME);
        String email = user.get(SessionManager.KEY_EMAIL);

        tvUserName.setText(name);
        tvUserEmail.setText(email);

        // Configurar Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
                finish(); // Cierra esta activity tras logout
            }
        });
    }

    /**
     * onResume: Recarga las estadísticas cada vez que la pantalla se muestra.
     * Esto asegura que si el usuario añade una incidencia y vuelve, el contador se
     * actualiza.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    /**
     * Consulta la base de datos para obtener el recuento de incidencias por estado
     * y actualiza la interfaz de usuario.
     */
    private void loadStats() {
        String email = session.getUserDetails().get(SessionManager.KEY_EMAIL);

        // Consultas COUNT asíncronas encadenadas (o paralelas, aquí encadenadas por
        // simplicidad)

        // 1. Total
        incidenciaDAO.getIncidenciasCount(email, null, new com.ecocity.app.database.IncidenciaDAO.CountCallback() {
            @Override
            public void onCountLoaded(int total) {
                tvCountTotal.setText(String.valueOf(total));

                // 2. Resueltas
                incidenciaDAO.getIncidenciasCount(email, "Resuelta",
                        new com.ecocity.app.database.IncidenciaDAO.CountCallback() {
                            @Override
                            public void onCountLoaded(int resolved) {
                                tvCountResolved.setText(String.valueOf(resolved));

                                // 3. En Proceso
                                incidenciaDAO.getIncidenciasCount(email, "En proceso",
                                        new com.ecocity.app.database.IncidenciaDAO.CountCallback() {
                                            @Override
                                            public void onCountLoaded(int process) {
                                                tvCountProcess.setText(String.valueOf(process));

                                                // 4. Pendientes
                                                incidenciaDAO.getIncidenciasCount(email, "Pendiente",
                                                        new com.ecocity.app.database.IncidenciaDAO.CountCallback() {
                                                            @Override
                                                            public void onCountLoaded(int pending) {
                                                                tvCountPending.setText(String.valueOf(pending));
                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            }
        });
    }

    /**
     * onDestroy: Cierra la conexión a la BD.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
