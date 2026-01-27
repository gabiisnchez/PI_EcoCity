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
    private com.google.android.material.textfield.TextInputLayout tilName, tilSurnames;
    private Button btnLogin;
    private android.widget.TextView tvToggleMode, tvTitle;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etSurnames = findViewById(R.id.etSurnames);
        
        tilName = findViewById(R.id.tilName);
        tilSurnames = findViewById(R.id.tilSurnames);

        btnLogin = findViewById(R.id.btnLogin);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        tvTitle = findViewById(R.id.tvTitle);

        // Toggle Mode Logic
        tvToggleMode.setOnClickListener(v -> toggleMode());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            etEmail.setError("El correo es requerido");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo inválido");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("La contraseña es requerida");
            return false;
        }

        if (!isLoginMode) {
            String name = etName.getText().toString().trim();
            String surnames = etSurnames.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("El nombre es requerido");
                return false;
            }
            if (TextUtils.isEmpty(surnames)) {
                etSurnames.setError("Los apellidos son requeridos");
                return false;
            }
        }
        return true;
    }

    private void performLogin() {
        // Simulation for now
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void performRegistration() {
        // Simulation for now - In future save to DB
        Toast.makeText(this, "Registro exitoso. Por favor inicia sesión.", Toast.LENGTH_SHORT).show();
        toggleMode(); // Switch back to login
    }
}
