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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etSenha;

    EditText etNome;
    TextView tvLogin;
    Button btnRegistro;
    FirebaseAuth mAuth;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNome = findViewById(R.id.nome);
        etEmail = findViewById(R.id.email);
        etSenha = findViewById(R.id.senha);
        tvLogin = findViewById(R.id.login_txt);
        btnRegistro = findViewById(R.id.cadastrar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criaUsuario();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
            }
        });
    }

    private void criaUsuario() {
        String nome = etNome.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();

        if (TextUtils.isEmpty(nome)){
            etNome.setError("O nome não pode ficar vazio");
            etNome.requestFocus();
        } else if (TextUtils.isEmpty(email)){
            etEmail.setError("O e-mail não pode ficar vazio");
            etEmail.requestFocus();
        } else if (TextUtils.isEmpty(senha)){
            etSenha.setError("A senha não pode estar vazia");
            etSenha.requestFocus();
        } else{
            mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        // Registro bem-sucedido, adiciona nome do usuário ao Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userID = user.getUid();

                        // Cria um mapa com os dados do usuário
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("nome", nome);
                        userData.put("id_user", userID);

                        // Salva os dados do usuário no Firestore
                        db.collection("usuarios").document(userID)
                                .set(userData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegistroActivity.this, "Usuário registrado com sucesso", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
                                        } else {
                                            Toast.makeText(RegistroActivity.this, "Erro ao registrar o usuário: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else{
                        Toast.makeText(RegistroActivity.this, "Erro ao registrar o usuário: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
