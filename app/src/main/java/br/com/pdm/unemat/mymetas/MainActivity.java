package br.com.pdm.unemat.mymetas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    ImageButton btnSair;
    ImageButton btnCreate;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    TextView tvTitulo, tvDescricao, tvPrioridade, tvData;
    Button btnRealizar, btnExcluir;

    private LinearLayout listaMain;
    private CollectionReference taskCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listaMain = findViewById(R.id.listaMain);
        createList();
        btnSair = findViewById(R.id.btnSair);
        btnCreate = findViewById(R.id.addButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish(); // Finalizar a atividade atual para impedir o retorno à MainActivity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuario = mAuth.getCurrentUser();
        if (usuario == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // Finalizar a atividade atual para impedir o retorno à MainActivity
        }
    }

    private void createList(){
        taskCollection = FirebaseFirestore.getInstance().collection("Tarefas");
        taskCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userID = user.getUid();
                    listaMain.removeAllViews(); // Remove os itens anteriores

                    for (DocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        String idUser = document.getString("userId");
                        String titulo = document.getString("nome");
                        String descricao = document.getString("descricao");
                        String prioridade = document.getString("prioridade");
                        Date dataFim = document.getDate("data-fim");
                        if(idUser.equals(userID))
                            addTaskToView(id, titulo, descricao, dataFim, prioridade);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Falha ao carregar as tarefas", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void addTaskToView(String taskId, String titulo, String descricao, Date dataFim, String prioridade) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_task, listaMain, false);

        TextView titleTask = cardView.findViewById(R.id.titleTask);
        TextView descriptionTask = cardView.findViewById(R.id.descriptionTask);
        TextView dateTask = cardView.findViewById(R.id.dateTask);
        TextView priorityTask = cardView.findViewById(R.id.priorityTask);
        Button finalizarButton = cardView.findViewById(R.id.finalizarButton);
        Button excluirButton = cardView.findViewById(R.id.excluirButton);

        titleTask.setText("Titulo: "+titulo);
        descriptionTask.setText("Descrição: "+descricao);
        dateTask.setText("Fazer Até: "+formatDate(dataFim));
        priorityTask.setText(prioridade);

        finalizarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTask(taskId);
            }
        });

        excluirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask(taskId);
            }
        });

        listaMain.addView(cardView);
    }

    private String formatDate(Date date) {
        // Lógica para formatar a data
        SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC-4"));
        return sdf.format(date);
    }

    private void deleteTask(String taskId) {
        DocumentReference taskRef = taskCollection.document(taskId);
        taskRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show();
                    createList();
                } else {
                    Toast.makeText(MainActivity.this, "Falha ao excluir a tarefa", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void finishTask(String taskId) {
        DocumentReference taskRef = taskCollection.document(taskId);
        taskRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Parabens! Tarefa Realizada!", Toast.LENGTH_SHORT).show();
                    createList();
                } else {
                    Toast.makeText(MainActivity.this, "Falha ao Realizar a tarefa", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
