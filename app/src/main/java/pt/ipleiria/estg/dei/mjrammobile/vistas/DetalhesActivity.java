package pt.ipleiria.estg.dei.mjrammobile.vistas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import pt.ipleiria.estg.dei.mjrammobile.R;

public class DetalhesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);
    }

    public void onClickListaTarefas(View view) {

        Intent intent = new Intent(this, ListaTarefasActivity.class);
        startActivity(intent);
    }

}