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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etName, etSurnames;
    private com.google.android.material.textfield.TextInputLayout tilName, tilSurnames, tilEmail, tilPassword;
    private Button btnLogin;
    private android.widget.TextView tvToggleMode, tvTitle;

    private boolean isLoginMode = true;
    private String registeredEmail = "";
    private String registeredPassword = "";
    private com.ecocity.app.utils.SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        session = new com.ecocity.app.utils.SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return; // Important so we don't continue creating the login UI
        }
        
        setContentView(R.layout.activity_login);

        // Bind Views
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

        // Toggle Mode Logic
        tvToggleMode.setOnClickListener(v -> toggleMode());

        // TextWatchers to clear errors when user types
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                tilPassword.setError(null);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Restaurar estado inicial
            tilEmail.setError(null);
            tilPassword.setError(null);
            
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

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText("Bienvenido");
            btnLogin.setText(getString(R.string.btn_login));
            tvToggleMode.setText("¿No tienes cuenta? Registrate aquí");
            
            tilName.setVisibility(View.GONE);
            tilSurnames.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Crear Cuenta");
            btnLogin.setText("Registrarse");
            tvToggleMode.setText("¿Ya tienes cuenta? Inicia Sesión");

            tilName.setVisibility(View.VISIBLE);
            tilSurnames.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateInput() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("El correo es requerido");
            return false;
        } else {
            tilEmail.setError(null);
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Correo inválido");
            return false;
        } else {
            tilEmail.setError(null);
        }
        
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("La contraseña es requerida");
            return false;
        } else {
            tilPassword.setError(null);
        }

        if (!isLoginMode) {
            String name = etName.getText().toString().trim();
            String surnames = etSurnames.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                tilName.setError("El nombre es requerido");
                return false;
            } else {
                tilName.setError(null);
            }
            
            if (TextUtils.isEmpty(surnames)) {
                tilSurnames.setError("Los apellidos son requeridos");
                return false;
            } else {
                tilSurnames.setError(null);
            }
        }
        return true;
    }

    private void performLogin() {
        // Simulación de autenticación
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        // Credenciales harcodeadas O las registradas recientemente
        if ((email.equals("usuario@ecocity.com") && password.equals("123456")) || 
            (!TextUtils.isEmpty(registeredEmail) && email.equals(registeredEmail) && password.equals(registeredPassword))) {
            // Crear sesión
            // Como es una simulacion, si usamos user hardcodeado, inventamos nombre
            if (email.equals("usuario@ecocity.com")) {
                session.createLoginSession("Usuario Demo", email);
            } else {
                // Si usamos el registrado, usamos el mail como nombre provisorio si no lo tenemos a mano (o deberiamos haberlo guardado)
                // Para simplificar, usamos "Usuario Registrado"
                session.createLoginSession("Usuario", email);
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Mostrar mensaje de error en AMBOS campos
            tilEmail.setError(" "); 
            tilPassword.setError("Correo o contraseña incorrectos");
            
            etPassword.requestFocus();
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRegistration() {
        // Guardar credenciales en memoria para la sesión actual
        registeredEmail = etEmail.getText().toString().trim();
        registeredPassword = etPassword.getText().toString().trim();
        
        // Guardar sesión directamente para entrar
        String name = etName.getText().toString().trim();
        session.createLoginSession(name, registeredEmail);
        
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
