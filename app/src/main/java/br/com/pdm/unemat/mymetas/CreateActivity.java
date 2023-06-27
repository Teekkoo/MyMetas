package br.com.pdm.unemat.mymetas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {
    EditText etNome, etDescricao;
    RadioGroup rgPrioridade;
    DatePicker dtFinalizar;
    ImageButton btnInicio;
    ImageButton btnSair;
    Button btnCadastrar;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String opcaoSelecionada;
    Date selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        etNome = findViewById(R.id.nome);
        etDescricao = findViewById(R.id.descricao);
        rgPrioridade = findViewById(R.id.groupRadio);
        dtFinalizar = findViewById(R.id.finalizar);
        btnCadastrar = findViewById(R.id.cadastrar);

        btnInicio = findViewById(R.id.homeButton);
        btnSair = findViewById(R.id.btnSair);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rgPrioridade.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Verifica qual RadioButton foi selecionado
                RadioButton radioButton = group.findViewById(checkedId);
                if (radioButton != null) {
                     opcaoSelecionada = radioButton.getText().toString();
                    }
            }
        });

        dtFinalizar.init(dtFinalizar.getYear(), dtFinalizar.getMonth(), dtFinalizar.getDayOfMonth(),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Atualize a variável selectedDate com a nova data selecionada
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        selectedDate = calendar.getTime();
                    }
                });

        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateActivity.this, MainActivity.class));
            }
        });

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(CreateActivity.this, LoginActivity.class));
                finish(); // Finalizar a atividade atual para impedir o retorno à MainActivity
            }
        });

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        // Definir a data mínima no DatePicker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Definir a data mínima no DatePicker
        dtFinalizar.setMinDate(calendar.getTimeInMillis());

        // Definir a data atual como a data selecionada no DatePicker
        dtFinalizar.init(year, month, day, null);
    }

    private void createTask() {
        String nome = etNome.getText().toString();
        String descricao = etDescricao.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        if (TextUtils.isEmpty(nome)){
            etNome.setError("O nome não pode ficar vazio");
            etNome.requestFocus();
        } else if (TextUtils.isEmpty(opcaoSelecionada)){
            Toast.makeText(CreateActivity.this, "Selecione a Prioridade", Toast.LENGTH_SHORT).show();
        } else if (selectedDate == null) {
            Toast.makeText(CreateActivity.this, "Selecione a Data", Toast.LENGTH_SHORT).show();
        } else {
            // Crie um mapa com os dados da tarefa
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("userId", userID);
            taskData.put("nome", nome);
            taskData.put("descricao", descricao);
            taskData.put("prioridade", opcaoSelecionada);
            taskData.put("data-fim", selectedDate);

            db.collection("Tarefas")
                    .add(taskData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CreateActivity.this, "Tarefa registrada com sucesso", Toast.LENGTH_SHORT).show();
                                etNome.setText("");
                                etDescricao.setText("");
                                etNome.clearFocus();
                                etDescricao.clearFocus();
                            } else {
                                Toast.makeText(CreateActivity.this, "Erro ao registrar a Tarefa: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
