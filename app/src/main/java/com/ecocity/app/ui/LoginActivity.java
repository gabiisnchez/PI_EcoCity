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
    // Campos de texto para entrada de datos
    private TextInputEditText etEmail, etPassword, etName, etSurnames;

    // Contenedores de texto (TextInputLayout) para gestionar errores visuales y
    // etiquetas flotantes
    private com.google.android.material.textfield.TextInputLayout tilName, tilSurnames, tilEmail, tilPassword;

    // Botón principal de acción (Entrar / Registrarse)
    private Button btnLogin;

    // Texto interactivo para cambiar de modo (Login <-> Registro)
    private android.widget.TextView tvToggleMode, tvTitle;

    // --- Lógica de Negocio y Datos ---
    private com.ecocity.app.database.UserDAO userDAO; // Objeto de Acceso a Datos de Usuario (SQLite)
    private boolean isLoginMode = true; // Variable de estado: true = Login, false = Registro
    private com.ecocity.app.utils.SessionManager session; // Gestor de Sesión (SharedPreferences)

    /**
     * Método de ciclo de vida onCreate.
     * Se ejecuta al iniciar la actividad. Configura la UI y la lógica inicial.
     * 
     * @param savedInstanceState Estado guardado.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Verificación de Sesión Activa
        // Si el usuario ya está logueado, redirigimos directos a la pantalla principal
        // sin pasar por login
        session = new com.ecocity.app.utils.SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Evita volver al Login pulsando 'Atrás'
            return;
        }

        // 2. Inicialización de Base de Datos
        userDAO = new com.ecocity.app.database.UserDAO(this);
        userDAO.open();

        // 3. Configuración de UI
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajuste de insets para diseño edge-to-edge (pantalla completa)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets
                    .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Binding de Vistas (Enlace variables Java -> XML)
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

        // --- Listeners de Eventos ---

        // Listener para cambiar modo (Login <-> Registro) al hacer clic en el texto
        tvToggleMode.setOnClickListener(v -> toggleMode());

        // TextWatchers: Limpian los mensajes de error cuando el usuario empieza a
        // escribir para mejorar UX
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Al escribir, borramos los errores visuales rojos
                tilEmail.setError(null);
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        };
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        // Acción del botón principal
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpiar errores previos
                tilEmail.setError(null);
                tilPassword.setError(null);

                // Si la validación de campos es correcta, procedemos
                if (validateInput()) {
                    if (isLoginMode) {
                        performLogin();
                    } else {
                        performRegistration();
                    }
                }
            }
        });
    }

    /**
     * Alterna la interfaz entre Modo Login y Modo Registro.
     * Cambia textos, visibilidad de campos (Nombre/Apellidos) y títulos
     * dinámicamente.
     */
    private void toggleMode() {
        isLoginMode = !isLoginMode; // Invertir estado

        if (isLoginMode) {
            // Configuración visual para Login
            tvTitle.setText(getString(R.string.title_welcome));
            btnLogin.setText(getString(R.string.btn_login));
            tvToggleMode.setText(getString(R.string.text_no_account));

            // Ocultar campos innecesarios para Login (Nombre y Apellidos)
            tilName.setVisibility(View.GONE);
            tilSurnames.setVisibility(View.GONE);
        } else {
            // Configuración visual para Registro
            tvTitle.setText(getString(R.string.title_create_account));
            btnLogin.setText(getString(R.string.btn_register));
            tvToggleMode.setText(getString(R.string.text_has_account));

            // Mostrar campos adicionales necesarios para Registro
            tilName.setVisibility(View.VISIBLE);
            tilSurnames.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Valida los datos introducidos por el usuario en el formulario.
     * Verifica campos vacíos y formato de email.
     * 
     * @return true si todos los datos son válidos, false si hay errores.
     */
    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Validar Email vacio
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return false;
        } else {
            tilEmail.setError(null);
        }

        // 2. Validar Formato Email (usando Regex de Android)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return false;
        } else {
            tilEmail.setError(null);
        }

        // 3. Validar Contraseña vacía
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            return false;
        } else {
            tilPassword.setError(null);
        }

        // 4. Validaciones extra exclusivas para el modo Registro
        if (!isLoginMode) {
            String name = etName.getText().toString().trim();
            String surnames = etSurnames.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilName.setError(getString(R.string.error_name_required));
                return false;
            } else {
                tilName.setError(null);
            }

            if (TextUtils.isEmpty(surnames)) {
                tilSurnames.setError(getString(R.string.error_surnames_required));
                return false;
            } else {
                tilSurnames.setError(null);
            }
        }
        return true;
    }

    /**
     * Realiza la lógica de inicio de sesión.
     * Verifica credenciales contra la base de datos o usuario demo.
     */
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 1. Usuario Demo (Hardcoded para pruebas rápidas sin necesidad de registrarse)
        if (email.equals("usuario@ecocity.com") && password.equals("123456")) {
            session.createLoginSession("Usuario Demo", email);
            launchMainActivity();
            return;
        }

        // 2. Verificación Real contra Base de Datos SQLite
        com.ecocity.app.model.User user = userDAO.login(email, password);

        if (user != null) {
            // Login Exitoso: Guardar sesión y navegar
            session.createLoginSession(user.getName(), user.getEmail());
            launchMainActivity();
        } else {
            // Login Fallido: Mostrar error visual
            tilEmail.setError(" ");
            tilPassword.setError(getString(R.string.error_auth_failed));
            etPassword.requestFocus();
            Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Realiza la lógica de registro de nuevo usuario.
     * Guarda el nuevo usuario en la base de datos si el email no existe.
     */
    private void performRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();

        // 1. Verificar si el email ya existe en la BD
        if (userDAO.checkEmailExists(email)) {
            tilEmail.setError(getString(R.string.error_email_exists));
            return;
        }

        // 2. Guardar nuevo usuario en Base de Datos
        com.ecocity.app.model.User newUser = new com.ecocity.app.model.User(name, email, password);
        long result = userDAO.registerUser(newUser);

        if (result > 0) {
            // Registro Exitoso
            Toast.makeText(this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();

            // Limpiar campos y cambiar a modo Login automáticamente para que el usuario
            // entre
            etEmail.setText("");
            etPassword.setText("");
            etName.setText("");
            etSurnames.setText("");

            toggleMode();
        } else {
            // Error en Registro (ej: fallo de escritura en BBDD)
            Toast.makeText(this, getString(R.string.msg_register_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navega a la actividad principal (MainActivity).
     * Cierra la actividad de login para que no se pueda volver atrás.
     */
    private void launchMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cierra LoginActivity
    }

    /**
     * Limpieza de recursos al destruir la actividad.
     * Cerramos la conexión a la base de datos.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) {
            userDAO.close();
        }
    }
}
