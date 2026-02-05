package com.ecocity.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ecocity.app.R;
import com.ecocity.app.utils.SessionManager;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvUserName, tvUserEmail;
    private Button btnLogout;

    // Stats
    private TextView tvCountTotal, tvCountResolved, tvCountProcess, tvCountPending;
    private com.ecocity.app.database.IncidenciaDAO incidenciaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        incidenciaDAO = new com.ecocity.app.database.IncidenciaDAO(this);

        session = new SessionManager(getApplicationContext());
        // Check if user is logged in
        session.checkLogin();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);

        // Stats Views
        tvCountTotal = findViewById(R.id.tvCountTotal);
        tvCountResolved = findViewById(R.id.tvCountResolved);
        tvCountProcess = findViewById(R.id.tvCountProcess);
        tvCountPending = findViewById(R.id.tvCountPending);

        HashMap<String, String> user = session.getUserDetails();
        String name = user.get(SessionManager.KEY_NAME);
        String email = user.get(SessionManager.KEY_EMAIL);

        tvUserName.setText(name);
        tvUserEmail.setText(email);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logoutUser();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        incidenciaDAO.open();

        int total = incidenciaDAO.getIncidenciasCount(null);
        int resolved = incidenciaDAO.getIncidenciasCount("Resuelta");
        int process = incidenciaDAO.getIncidenciasCount("En proceso");
        int pending = incidenciaDAO.getIncidenciasCount("Pendiente");

        tvCountTotal.setText(String.valueOf(total));
        tvCountResolved.setText(String.valueOf(resolved));
        tvCountProcess.setText(String.valueOf(process));
        tvCountPending.setText(String.valueOf(pending));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        incidenciaDAO.close();
    }
}
