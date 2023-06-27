package br.com.pdm.unemat.mymetas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    // DECLARA VARIAVEIS
    EditText etEmail, etPwd;
    TextView tvRegistro;
    Button btnSend;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // INSTANCIA AS VARIAVEIS
        etEmail = findViewById(R.id.email);
        etPwd = findViewById(R.id.senha);
        tvRegistro = findViewById(R.id.registro);
        btnSend = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUsuario();
            }
        });

        tvRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
            }
        });

    }

    private void loginUsuario() {
        String email = etEmail.getText().toString();
        String senha = etPwd.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("E-mail não pode ser vazio!");
            etEmail.requestFocus();
        } else if (TextUtils.isEmpty(senha)) {
            etPwd.setError("Senha não pode ser vazia!");
            etPwd.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Usuário logado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro de login: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
