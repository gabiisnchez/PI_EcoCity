package com.ecocity.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecocity.app.MainActivity;
import com.ecocity.app.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * <h1>LoginActivity</h1>
 * <p>
 * Clase encargada de la autenticación de usuarios.
 * Maneja tanto el Inicio de Sesión (Login) como el Registro de nuevos usuarios
 * en la misma pantalla.
 * </p>
 *
 * <h2>Características:</h2>
 * <ul>
 * <li>Intercambio dinámico entre vista de Login y vista de Registro.</li>
 * <li>Validación de campos (Email, Contraseña, Nombre, Apellidos).</li>
 * <li>Gestión de sesión utilizando SharedPreferences (via SessionManager).</li>
 * <li>Persistencia de usuarios en base de datos SQLite (vía UserDAO).</li>
 * </ul>
 */
public class LoginActivity extends AppCompatActivity {

    // --- Componentes de UI ---
    private TextInputEditText etEmail, etPassword, etName, etSurnames;
    private com.google.android.material.textfield.TextInputLayout tilName, tilSurnames, tilEmail, tilPassword;
    private Button btnLogin;
    private android.widget.TextView tvToggleMode, tvTitle;

    // --- Lógica de Negocio y Datos ---
    // private com.ecocity.app.database.UserDAO userDAO; // Comentado: Migración a
    // Firebase
    private boolean isLoginMode = true;
    private com.ecocity.app.utils.SessionManager session;

    // Firebase Auth
    private com.google.firebase.auth.FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Firebase Auth
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        // 1. Verificación de Sesión Activa
        session = new com.ecocity.app.utils.SessionManager(getApplicationContext());
        if (session.isLoggedIn() || mAuth.getCurrentUser() != null) {
            // Sincronizar sesión si Firebase tiene usuario pero SessionManager no (caso
            // borde)
            if (mAuth.getCurrentUser() != null && !session.isLoggedIn()) {
                String name = mAuth.getCurrentUser().getDisplayName();
                String email = mAuth.getCurrentUser().getEmail();
                session.createLoginSession(name != null ? name : "Usuario", email);
            }

            launchMainActivity();
            return;
        }

        /*
         * // 2. Inicialización de Base de Datos (SQLite - Legacy)
         * userDAO = new com.ecocity.app.database.UserDAO(this);
         * userDAO.open();
         */

        // 3. Configuración de UI
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Binding de Vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etSurnames = findViewById(R.id.etSurnames);

        tilName = findViewById(R.id.tilName);
        tilSurnames = findViewById(R.id.tilSurnames);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        btnLogin = findViewById(R.id.btnLogin);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        tvTitle = findViewById(R.id.tvTitle);

        // Listeners
        tvToggleMode.setOnClickListener(v -> toggleMode());

        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        };
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(v -> {
            tilEmail.setError(null);
            tilPassword.setError(null);

            if (validateInput()) {
                if (isLoginMode) {
                    performLogin();
                } else {
                    performRegistration();
                }
            }
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            tvTitle.setText(getString(R.string.title_welcome));
            btnLogin.setText(getString(R.string.btn_login));
            tvToggleMode.setText(getString(R.string.text_no_account));
            tilName.setVisibility(View.GONE);
            tilSurnames.setVisibility(View.GONE);
        } else {
            tvTitle.setText(getString(R.string.title_create_account));
            btnLogin.setText(getString(R.string.btn_register));
            tvToggleMode.setText(getString(R.string.text_has_account));
            tilName.setVisibility(View.VISIBLE);
            tilSurnames.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            return false;
        }

        if (!isLoginMode) {
            String name = etName.getText().toString().trim();
            String surnames = etSurnames.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_name_required));
                return false;
            }
            if (TextUtils.isEmpty(surnames)) {
                tilSurnames.setError(getString(R.string.error_surnames_required));
                return false;
            }
        }
        return true;
    }

    /**
     * Inicio de sesión usando Firebase Auth
     */
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Mostrar indicador de carga (opcional, por ahora bloqueamos el botón)
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Login exitoso
                        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
                        String name = user.getDisplayName();

                        // Guardar sesión local para compatibilidad
                        session.createLoginSession(name != null ? name : "Usuario", user.getEmail());

                        Toast.makeText(LoginActivity.this, "Bienvenido " + (name != null ? name : ""),
                                Toast.LENGTH_SHORT).show();
                        launchMainActivity();
                    } else {
                        // Login fallido
                        Toast.makeText(LoginActivity.this, getString(R.string.error_auth_failed),
                                Toast.LENGTH_SHORT).show();
                        tilPassword.setError(getString(R.string.error_auth_failed));
                    }
                });
    }

    /**
     * Registro usando Firebase Auth
     */
    private void performRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String apellidos = etSurnames.getText().toString().trim();
        String fullName = name + " " + apellidos;

        btnLogin.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso, actualizar perfil con nombre
                        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();

                        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    btnLogin.setEnabled(true);
                                    if (task1.isSuccessful()) {
                                        // IMPORTANTE: No iniciar sesión automáticamente.
                                        // Cerrar la sesión de Firebase inmediatamente.
                                        mAuth.signOut();

                                        Toast.makeText(LoginActivity.this,
                                                "Registro exitoso. Por favor, inicia sesión.",
                                                Toast.LENGTH_LONG).show();

                                        // Limpiar campos y volver al modo Login
                                        etEmail.setText("");
                                        etPassword.setText("");
                                        etName.setText("");
                                        etSurnames.setText("");
                                        toggleMode();
                                    }
                                });
                    } else {
                        // Error en registro
                        btnLogin.setEnabled(true);
                        String errorMsg = task.getException() != null ? task.getException().getMessage()
                                : "Error en el registro";
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
